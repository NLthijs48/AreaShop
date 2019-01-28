package me.wiefferink.areashop.managers;

import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.features.CommandsFeature;
import me.wiefferink.areashop.features.DebugFeature;
import me.wiefferink.areashop.features.FriendsFeature;
import me.wiefferink.areashop.features.RegionFeature;
import me.wiefferink.areashop.features.TeleportFeature;
import me.wiefferink.areashop.features.WorldGuardRegionFlagsFeature;
import me.wiefferink.areashop.features.signs.SignsFeature;
import me.wiefferink.areashop.regions.GeneralRegion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FeatureManager extends Manager {

	// List of defined features
	private static final Set<Class<? extends RegionFeature>> featureClasses = new HashSet<>(Arrays.asList(
			DebugFeature.class,
			SignsFeature.class,
			FriendsFeature.class,
			WorldGuardRegionFlagsFeature.class,
			TeleportFeature.class,
			CommandsFeature.class
	));
	// One instance of each feature, registered for event handling
	private final Set<RegionFeature> globalFeatures;
	private final Map<Class<? extends RegionFeature>, Constructor<? extends RegionFeature>> regionFeatureConstructors;

	/**
	 * Constructor.
	 */
	public FeatureManager() {
		// Instantiate and register global features (one per type, for event handling)
		globalFeatures = new HashSet<>();
		for(Class<? extends RegionFeature> clazz : featureClasses) {
			try {
				Constructor<? extends RegionFeature> constructor = clazz.getConstructor();
				RegionFeature feature = constructor.newInstance();
				feature.listen();
				globalFeatures.add(feature);
			} catch(InstantiationException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
				AreaShop.error("Failed to instantiate global feature:", clazz, e);
			} catch(NoSuchMethodException e) {
				// Feature does not have a global part
			}
		}

		// Setup constructors for region specific features
		regionFeatureConstructors = new HashMap<>();
		for(Class<? extends RegionFeature> clazz : featureClasses) {
			try {
				regionFeatureConstructors.put(clazz, clazz.getConstructor(GeneralRegion.class));
			} catch(NoSuchMethodException | IllegalArgumentException e) {
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
	 * Instanciate a feature for a certain region.
	 * @param region       The region to create a feature for
	 * @param featureClazz The class of the feature to create
	 * @return The feature class
	 */
	public RegionFeature getRegionFeature(GeneralRegion region, Class<? extends RegionFeature> featureClazz) {
		try {
			return regionFeatureConstructors.get(featureClazz).newInstance(region);
		} catch(InstantiationException | InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
			AreaShop.error("Failed to instantiate feature", featureClazz, "for region", region, e, e.getCause());
		}
		return null;
	}

}
