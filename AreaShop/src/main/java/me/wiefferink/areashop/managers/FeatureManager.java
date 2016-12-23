package me.wiefferink.areashop.managers;

import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.features.*;
import me.wiefferink.areashop.regions.GeneralRegion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class FeatureManager extends Manager {

	// List of defined features
	private static final Set<Class<? extends RegionFeature>> featureClasses = new HashSet<>(Arrays.asList(
			DebugFeature.class,
			SignsFeature.class,
			FriendsFeature.class,
			WorldGuardRegionFlagsFeature.class,
			TeleportFeature.class
	));
	// One instance of each feature, registered for event handling
	private Set<RegionFeature> globalFeatures;
	private Map<Class<? extends RegionFeature>, Constructor<? extends RegionFeature>> regionFeatureConstructors;

	public FeatureManager() {
		// Instantiate and register global features (one per type, for event handling)
		globalFeatures = new HashSet<>();
		for(Class<? extends RegionFeature> clazz : featureClasses) {
			try {
				Constructor<? extends RegionFeature> constructor = clazz.getConstructor();
				try {
					RegionFeature feature = constructor.newInstance();
					feature.listen();
					globalFeatures.add(feature);
				} catch(InstantiationException|IllegalAccessException|InvocationTargetException|IllegalArgumentException e) {
					AreaShop.error("Failed to instantiate global feature:", clazz);
				}
			} catch(NoSuchMethodException e) {
				// Feature does not have a global part
			}
		}

		// Setup constructors for region specific features
		regionFeatureConstructors = new HashMap<>();
		for(Class<? extends RegionFeature> clazz : featureClasses) {
			try {
				regionFeatureConstructors.put(clazz, clazz.getConstructor(GeneralRegion.class));
			} catch(NoSuchMethodException|IllegalArgumentException e) {
				// The feature does not have a region specific part
			}
		}
	}

	@Override
	public void shutdown() {
		for(RegionFeature feature : globalFeatures) {
			feature.shutdown();
		}
	}

	/**
	 * Get instances of all features for a specific region
	 * @param region The region to create features for
	 * @return A map with all features
	 */
	public Map<Class<? extends RegionFeature>, RegionFeature> getRegionFeatures(GeneralRegion region) {
		Map<Class<? extends RegionFeature>, RegionFeature> result = new HashMap<>();
		for(Class<? extends RegionFeature> clazz : regionFeatureConstructors.keySet()) {
			try {
				result.put(clazz, regionFeatureConstructors.get(clazz).newInstance(region));
			} catch(InstantiationException|InvocationTargetException|IllegalAccessException|IllegalArgumentException e) {
				AreaShop.error("Failed to instanciate feature", clazz, "for region", region);
			}
		}
		return result;
	}

}
