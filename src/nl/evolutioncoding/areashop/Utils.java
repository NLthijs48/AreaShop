package nl.evolutioncoding.areashop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import nl.evolutioncoding.areashop.regions.BuyRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import nl.evolutioncoding.areashop.regions.RentRegion;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

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
				|| !config.isDouble("z")) {
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
	
	public static final BlockFace[] facings = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };
    /**
	* Get the facing direction based on the yaw
	* @param yaw The horizontal angle that for example the player is looking
	* @return The Block Face of the angle
	*/
	public static BlockFace yawToFacing(float yaw) {
		return facings[Math.round(yaw / 45f) & 0x7];
	}

	/**
	 * 
	 * @param uuid player
	 * @return list regions where player is owner
	 */
    public static List<GeneralRegion> getPlayerRegins(UUID uuid) {
        List<GeneralRegion> regions = new ArrayList<GeneralRegion>();
        List<GeneralRegion> all = AreaShop.getInstance().getFileManager().getRegions();
        for(GeneralRegion r: all)
            if(r.isOwner(uuid))
                regions.add(r); 
        return regions;
    }
    
    /**
     * Get only the names of regions
     * @param regions
     * @return 
     */
    public static List<String> getRegionsNames(List<GeneralRegion> regions) {
        List<String> list = new ArrayList<String>();
        for(GeneralRegion r:regions) 
            list.add(r.getName());            
        return list;
    }


    public static List<RentRegion> getActiveRequiredRegions(List<String> req) {
        List<RentRegion> list = new ArrayList<RentRegion>();
        for(String r:req){
            GeneralRegion rent = AreaShop.getInstance().getFileManager().getRegion(r);            
            if(rent!=null && rent instanceof RentRegion)
                list.add((RentRegion)rent);
            }
        return list;
    }
    
    public static List<GeneralRegion> getParentChilds(GeneralRegion region){
        List<GeneralRegion> parents = new ArrayList<GeneralRegion>();
        for(GeneralRegion r:AreaShop.getInstance().getFileManager().getRegions()){
            if(r.getRequireShops().contains(region.getName()))
                parents.add(r);
        }
        return parents;
    }
    
    public static List<GeneralRegion> getActiveRegions(List<GeneralRegion> list){
        List<GeneralRegion> active = new ArrayList<GeneralRegion>();
        for(GeneralRegion r:list){
            if(r instanceof BuyRegion && ((BuyRegion)r).getBuyer()!=null)
                {
                active.add(r);
                continue;
                }
            if(r instanceof RentRegion && ((RentRegion)r).getRenter()!=null)
            {
                active.add(r);
                continue;
            }
        }
        return active;
    }
  
    /**
     * Convert milliseconds to a human readable format
     * @param milliseconds The amount of milliseconds to convert
     * @return A formatted string based on the language file
     */
    public static String toTimeHumanFormat(long milliseconds) {
        long timeLeft = milliseconds + 500;
        // To seconds
        timeLeft = timeLeft/1000;
        if(timeLeft <= 0) {
            return AreaShop.getInstance().getLanguageManager().getLang("timeleft-ended");
        } else if(timeLeft == 1) {
            return AreaShop.getInstance().getLanguageManager().getLang("timeleft-second", timeLeft);
        } else if(timeLeft <= 120) {
            return AreaShop.getInstance().getLanguageManager().getLang("timeleft-seconds", timeLeft);
        }
        // To minutes
        timeLeft = timeLeft/60;
        if(timeLeft <= 120) {
            return AreaShop.getInstance().getLanguageManager().getLang("timeleft-minutes", timeLeft);
        }
        // To hours
        timeLeft = timeLeft/60;
        if(timeLeft <= 48) {
            return AreaShop.getInstance().getLanguageManager().getLang("timeleft-hours", timeLeft);
        }
        // To days
        timeLeft = timeLeft/24;
        if(timeLeft <= 60) {
            return AreaShop.getInstance().getLanguageManager().getLang("timeleft-days", timeLeft);
        }
        // To months
        timeLeft = timeLeft/30;
        if(timeLeft <= 24) {
            return AreaShop.getInstance().getLanguageManager().getLang("timeleft-months", timeLeft);
        }
        // To years
        timeLeft = timeLeft/12;
        return AreaShop.getInstance().getLanguageManager().getLang("timeleft-years", timeLeft);
    }
    
    public static boolean checkRequirements(GeneralRegion result, UUID uuid, Object target){
        List<String> req = result.getRequireShops();
        boolean cancel = true;
        if(!req.isEmpty()){
            List<String> hasnames = Utils.getRegionsNames(Utils.getPlayerRegins(uuid));
            for(String r:req)
                if(!hasnames.contains(r)){
                    if(target!=null)
                        AreaShop.getInstance().message(target, "require-failClick", result.getName(), StringUtils.join(result.getRequireShops(),", "));                    
                    return false;
                }
            if(result.isRentRegion()){                
                RentRegion rentresult = (RentRegion) result;
                List<RentRegion> rentlist = Utils.getActiveRequiredRegions(req);
                    for(RentRegion rent:rentlist) {
                        long left = rent.getRentedUntil() - rentresult.getRentedUntil();
                        if(left<=0){
                            if(target!=null)
                                AreaShop.getInstance().message(target, "require-failParentTime",rent.getName(), rentresult.getName(),Utils.toTimeHumanFormat(Math.abs(left)),Utils.toTimeHumanFormat(rent.getDuration()));
                            cancel = false;
                        }
                    }
            }
        }
    return cancel;
    }


    public static boolean checkAreaPermissions(GeneralRegion region, UUID uniqueId, Player player) {
        String perm = region.getPermission();
        if(perm!=null){
            if(!player.hasPermission(perm)){
                AreaShop.getInstance().message(player, "require-failPerm", region.getName(), perm);
                return false;
            }
        }
        return true;
    }
}















