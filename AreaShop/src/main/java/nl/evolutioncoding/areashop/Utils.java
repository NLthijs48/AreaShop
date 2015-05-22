package nl.evolutioncoding.areashop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import nl.evolutioncoding.areashop.regions.BuyRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion.RegionType;
import nl.evolutioncoding.areashop.regions.RentRegion;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Utils {

	// Not used
	private Utils() {}

	
	/**
	 * Create a map from a location, to save it in the config
	 * @param location The location to transform
	 * @return The map with the location values
	 */
	public static ConfigurationSection locationToConfig(Location location, boolean setPitchYaw) {
		if(location == null) {
			return null;
		}
		ConfigurationSection result = new YamlConfiguration();
		result.set("world", location.getWorld().getName());
		result.set("x", location.getX());
		result.set("y", location.getY());
		result.set("z", location.getZ());
		if(setPitchYaw) {
			result.set("yaw", Float.toString(location.getYaw()));
			result.set("pitch", Float.toString(location.getPitch()));		
		}
		return result;
	}
	public static ConfigurationSection locationToConfig(Location location) {
		return locationToConfig(location, false);
	}
	
	/**
	 * Create a location from a map, reconstruction from the config values
	 * @param map The map to reconstruct from
	 * @return The location
	 */
	public static Location configToLocation(ConfigurationSection config) {
		if(config == null
				|| !config.isString("world")
				|| !config.isDouble("x")
				|| !config.isDouble("y")
				|| !config.isDouble("z")
				|| Bukkit.getWorld(config.getString("world")) == null) {
			return null;
		}
		Location result = new Location(
				Bukkit.getWorld(config.getString("world")), 
				(Double)config.getDouble("x"), 
				(Double)config.getDouble("y"), 
				(Double)config.getDouble("z"));
		if(config.isString("yaw") && config.isString("pitch")) {
			result.setPitch(Float.parseFloat(config.getString("pitch")));
			result.setYaw(Float.parseFloat(config.getString("yaw")));
		}
		return result;
	}
	
	/**
	 * Create a comma-separated list
	 * @param input Collection of object which should be concatenated with comma's in between
	 * @return Innput object concatenated with comma's in between
	 */
	public static String createCommaSeparatedList(Collection<? extends Object> input) {
		String result = "";
		boolean first = true;
		for(Object object : input) {
			if(object != null) {
				if(first) {
					first = false;
					result += object.toString();
				} else {
					result += ", " + object.toString();
				}
			}
		}		
		return result;
	}
	
	/**
	 * Convert milliseconds to ticks
	 * @param milliseconds Milliseconds to convert
	 * @return milliseconds divided by 50 (20 ticks per second)
	 */
	public static long millisToTicks(long milliseconds) {
		return milliseconds/50;
	}
	
	public static final BlockFace[] facings = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };
    /**
	* Get the facing direction based on the yaw
	* @param yaw The horizontal angle that for example the player is looking
	* @return The Block Face of the angle
	*/
	public static BlockFace yawToFacing(float yaw) {
		return facings[Math.round(yaw / 45f) & 0x7];
	}
	
	// ======================================================================
	// Methods to get WorldGuard or AreaShop regions by location or selection
	// ======================================================================
	/**
	 * Get all AreaShop regions intersecting with a WorldEdit selection
	 * @param selection The selection to check
	 * @return A list with all the AreaShop regions intersecting with the selection
	 */
	public static List<GeneralRegion> getASRegionsInSelection(Selection selection) {
		ArrayList<GeneralRegion> result = new ArrayList<GeneralRegion>();
		for(ProtectedRegion region : getWERegionsInSelection(selection)) {
			GeneralRegion asRegion = AreaShop.getInstance().getFileManager().getRegion(region.getId());
			if(asRegion != null) {
				result.add(asRegion);
			}
		}
		return result;
	}
	/**
	 * Get all AreaShop regions containing a location
	 * @param location The location to check
	 * @return A list with all the AreaShop regions that contain the location
	 */
	public static List<GeneralRegion> getASRegionsByLocation(Location location) {
		Selection selection = new CuboidSelection(location.getWorld(), location, location);
		return getASRegionsInSelection(selection);
	}
	
	/**
	 * Get all WorldGuard regions intersecting with a WorldEdit selection
	 * @param selection The selection to check
	 * @return A list with all the WorldGuard regions intersecting with the selection
	 */
	public static List<ProtectedRegion> getWERegionsInSelection(Selection selection) {
		// Get all regions inside or intersecting with the WorldEdit selection of the player
		World world = selection.getWorld();
		RegionManager regionManager = AreaShop.getInstance().getWorldGuard().getRegionManager(world);
		ArrayList<ProtectedRegion> result = new ArrayList<ProtectedRegion>();
		Location selectionMin = selection.getMinimumPoint();
		Location selectionMax = selection.getMaximumPoint();
		for(ProtectedRegion region : regionManager.getRegions().values()) {
			BlockVector regionMin = region.getMinimumPoint();
			BlockVector regionMax = region.getMaximumPoint();			
			if( 
					(      // x part, resolves to true if the selection and region overlap anywhere on the x-axis
						   (regionMin.getBlockX() <= selectionMax.getBlockX() && regionMin.getBlockX() >= selectionMin.getBlockX())
						|| (regionMax.getBlockX() <= selectionMax.getBlockX() && regionMax.getBlockX() >= selectionMin.getBlockX())
						|| (selectionMin.getBlockX() >= regionMin.getBlockX() && selectionMin.getBlockX() <= regionMax.getBlockX())
						|| (selectionMax.getBlockX() >= regionMin.getBlockX() && selectionMax.getBlockX() <= regionMax.getBlockX())
					) && ( // Y part, resolves to true if the selection and region overlap anywhere on the y-axis
					       (regionMin.getBlockY() <= selectionMax.getBlockY() && regionMin.getBlockY() >= selectionMin.getBlockY())
						|| (regionMax.getBlockY() <= selectionMax.getBlockY() && regionMax.getBlockY() >= selectionMin.getBlockY())
						|| (selectionMin.getBlockY() >= regionMin.getBlockY() && selectionMin.getBlockY() <= regionMax.getBlockY())
						|| (selectionMax.getBlockY() >= regionMin.getBlockY() && selectionMax.getBlockY() <= regionMax.getBlockY())
					) && ( // Z part, resolves to true if the selection and region overlap anywhere on the z-axis
				           (regionMin.getBlockZ() <= selectionMax.getBlockZ() && regionMin.getBlockZ() >= selectionMin.getBlockZ())
						|| (regionMax.getBlockZ() <= selectionMax.getBlockZ() && regionMax.getBlockZ() >= selectionMin.getBlockZ())
						|| (selectionMin.getBlockZ() >= regionMin.getBlockZ() && selectionMin.getBlockZ() <= regionMax.getBlockZ())
						|| (selectionMax.getBlockZ() >= regionMin.getBlockZ() && selectionMax.getBlockZ() <= regionMax.getBlockZ())
					)
				) {
				result.add(region);
			}
		}
		return result;
	}
	
	/**
	 * Get a list of regions around a location
	 *  - Returns highest priority, child instead of parent regions
	 * @param location The location to check for regions
	 * @return empty list if no regions found, 1 member if 1 region is a priority, more if regions with the same priority
	 */
	public static List<ProtectedRegion> getApplicableRegions(Location location) {
		List<ProtectedRegion> result = new ArrayList<ProtectedRegion>();
		Set<ProtectedRegion> regions = AreaShop.getInstance().getWorldGuardHandler().getApplicableRegionsSet(location);
		if(regions != null) {
			boolean first = true;
			for(ProtectedRegion pr : regions) {
				if(first) {
					result.add(pr);
					first = false;
				} else {
					if(pr.getPriority() > result.get(0).getPriority()) {
						result.clear();
						result.add(pr);
					} else if(pr.getParent() != null && pr.getParent().equals(result.get(0))) {
						result.clear();
						result.add(pr);
					} else {
						result.add(pr);
					}
				}
			}
		}
		return result;
	}
	
	// Methods to get the most important AreaShop regions at a certain location
	public static List<RentRegion> getApplicableRentRegions(Location location) {
		List<RentRegion> result = new ArrayList<RentRegion>();
		for(GeneralRegion region : getApplicableASRegions(location, RegionType.RENT)) {
			result.add((RentRegion)region);
		}
		return result;
	}
	public static List<BuyRegion> getApplicableBuyRegions(Location location) {
		List<BuyRegion> result = new ArrayList<BuyRegion>();
		for(GeneralRegion region : getApplicableASRegions(location, RegionType.BUY)) {
			result.add((BuyRegion)region);
		}
		return result;
	}
	public static List<GeneralRegion> getAllApplicableRegions(Location location) {
		return getApplicableASRegions(location, null);
	}	
	public static List<GeneralRegion> getApplicableASRegions(Location location, RegionType type) {
		List<GeneralRegion> result = new ArrayList<GeneralRegion>();
		// TODO move to version specific classes
		Set<ProtectedRegion> regions = AreaShop.getInstance().getWorldGuardHandler().getApplicableRegionsSet(location);
		if(regions != null) {
			List<GeneralRegion> candidates = new ArrayList<GeneralRegion>();
			for(ProtectedRegion pr : regions) {
				GeneralRegion region = AreaShop.getInstance().getFileManager().getRegion(pr.getId());
				if(region != null && (
						(type == RegionType.RENT && region.isRentRegion())
						|| (type == RegionType.BUY && region.isBuyRegion())
						|| type == null)) {
					candidates.add(region);
				}
			}		
			boolean first = true;
			for(GeneralRegion region : candidates) {
				if(region == null) {
					AreaShop.debug("skipped null region");
					continue;
				}
				if(first) {
					result.add(region);
					first = false;
				} else {
					if(region.getRegion().getPriority() > result.get(0).getRegion().getPriority()) {
						result.clear();
						result.add(region);
					} else if(region.getRegion().getParent() != null && region.getRegion().getParent().equals(result.get(0))) {
						result.clear();
						result.add(region);
					} else {
						result.add(region);
					}
				}
			}
		}
		return new ArrayList<GeneralRegion>(result);
	}
	
}















