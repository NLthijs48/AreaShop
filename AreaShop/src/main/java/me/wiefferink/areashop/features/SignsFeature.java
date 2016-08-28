package me.wiefferink.areashop.features;

import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.Utils;
import me.wiefferink.areashop.events.notify.UpdateRegionEvent;
import me.wiefferink.areashop.regions.GeneralRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.*;

public class SignsFeature extends Feature {

	private static Map<String, RegionSign> allSigns = new HashMap<>();

	private GeneralRegion region;
	private Map<String, RegionSign> signs;

	public SignsFeature() {

	}

	public SignsFeature(GeneralRegion region) {
		this.region = region;
		signs = new HashMap<>();
		// Setup current signs
		ConfigurationSection signSection = region.getConfig().getConfigurationSection("general.signs");
		if(signSection != null) {
			for(String signKey : signSection.getKeys(false)) {
				RegionSign sign = new RegionSign(region, this, signKey);
				Location location = sign.getLocation();
				if(location == null) {
					AreaShop.warn("Sign with key "+signKey+" of region "+region.getName()+" does not have a proper location");
					continue;
				}
				signs.put(sign.getStringLocation(), sign);
			}
			allSigns.putAll(signs);
		}
	}

	/**
	 * Convert a location to a string to use as map key
	 * @param location The location to get the key for
	 * @return A string to use in a map for a location
	 */
	public static String locationToString(Location location) {
		return location.getWorld().getName()+";"+location.getBlockX()+";"+location.getBlockY()+";"+location.getBlockZ();
	}

	/**
	 * Get a sign by a location
	 * @param location The location to get the sign for
	 * @return The RegionSign that is at the location, or null if none
	 */
	public static RegionSign getSignByLocation(Location location) {
		return allSigns.get(locationToString(location));
	}

	@EventHandler
	public void regionUpdate(UpdateRegionEvent event) {
		event.getRegion().getSignsFeature().update();
	}

	/**
	 * Update all signs connected to this region
	 * @return true if all signs are updated correctly, false if one or more updates failed
	 */
	public boolean update() {
		boolean result = true;
		for(RegionSign sign : signs.values()) {
			result = result&sign.update();
		}
		return result;
	}

	/**
	 * Check if any of the signs need periodic updating
	 * @return true if one or more of the signs need periodic updating, otherwise false
	 */
	public boolean needsPeriodicUpdate() {
		boolean result = false;
		for(RegionSign sign : signs.values()) {
			result = result|sign.needsPeriodicUpdate();
		}
		return result;
	}

	/**
	 * Get a list with all sign locations
	 * @return A List with all sign locations
	 */
	public List<Location> getSignLocations() {
		List<Location> result = new ArrayList<>();
		for(RegionSign sign : signs.values()) {
			result.add(sign.getLocation());
		}
		return result;
	}

	/**
	 * Add a sign to this region
	 * @param location The location of the sign
	 * @param signType The type of the sign (WALL_SIGN or SIGN_POST)
	 * @param facing   The orientation of the sign
	 * @param profile  The profile to use with this sign (null for default)
	 */
	public void addSign(Location location, Material signType, BlockFace facing, String profile) {
		int i = 0;
		while(region.getConfig().isSet("general.signs."+i)) {
			i++;
		}
		String signPath = "general.signs."+i+".";
		region.setSetting(signPath+"location", Utils.locationToConfig(location));
		region.setSetting(signPath+"facing", facing.name());
		region.setSetting(signPath+"signType", signType.name());
		if(profile != null && profile.length() != 0) {
			region.setSetting(signPath+"profile", profile);
		}
		// Add to the map
		RegionSign sign = new RegionSign(region, this, i+"");
		signs.put(sign.getStringLocation(), sign);
		allSigns.put(sign.getStringLocation(), sign);
	}

	/**
	 * Checks if there is a sign from this region at the specified location
	 * @param location Location to check
	 * @return true if this region has a sign at the location, otherwise false
	 */
	public boolean isSignOfRegion(Location location) {
		Set<String> signs;
		if(region.getConfig().getConfigurationSection("general.signs") == null) {
			return false;
		}
		signs = region.getConfig().getConfigurationSection("general.signs").getKeys(false);
		for(String sign : signs) {
			Location signLocation = Utils.configToLocation(region.getConfig().getConfigurationSection("general.signs."+sign+".location"));
			if(signLocation != null
					&& signLocation.getWorld().equals(location.getWorld())
					&& signLocation.getBlockX() == location.getBlockX()
					&& signLocation.getBlockY() == location.getBlockY()
					&& signLocation.getBlockZ() == location.getBlockZ()) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Sign that is connected to a region to display information and interact with the region
	 */
	public class RegionSign {

		private GeneralRegion region;
		private SignsFeature signsFeature;
		private String key;

		public RegionSign(GeneralRegion region, SignsFeature signsFeature, String key) {
			this.region = region;
			this.signsFeature = signsFeature;
			this.key = key;
		}

		/**
		 * Get the location of this sign
		 * @return The location of this sign
		 */
		public Location getLocation() {
			return Utils.configToLocation(region.getConfig().getConfigurationSection("general.signs."+key+".location"));
		}

		/**
		 * Location string to be used as key in maps
		 * @return Location string
		 */
		public String getStringLocation() {
			return locationToString(getLocation());
		}

		/**
		 * Get the region this sign is linked to
		 * @return The region this sign is linked to
		 */
		public GeneralRegion getRegion() {
			return region;
		}

		/**
		 * Remove this sign from the region
		 */
		public void remove() {
			getLocation().getBlock().setType(Material.AIR);
			signs.remove(getStringLocation());
			allSigns.remove(getStringLocation());
			region.setSetting("general.signs."+key, null);
		}

		public String getProfile() {
			String profile = region.getConfig().getString("general.signs."+key+".profile");
			if(profile == null || profile.length() == 0) {
				profile = region.getStringSetting("general.signProfile");
			}
			return profile;
		}

		/**
		 * Update this sign
		 * @return true if the update was successful, otherwise false
		 */
		public boolean update() {
			if(region.isDeleted()) {
				return false;
			}
			YamlConfiguration config = region.getConfig();
			// Get the prefix
			String prefix = "signProfiles."+getProfile()+"."+region.getState().getValue().toLowerCase()+".";
			// Get the lines
			String[] signLines = new String[4];
			signLines[0] = plugin.getConfig().getString(prefix+"line1");
			signLines[1] = plugin.getConfig().getString(prefix+"line2");
			signLines[2] = plugin.getConfig().getString(prefix+"line3");
			signLines[3] = plugin.getConfig().getString(prefix+"line4");
			// Check if the sign should be present
			Block block = getLocation().getBlock();
			if(!plugin.getConfig().isSet(prefix)
					|| ((signLines[0] == null || signLines[0].length() == 0)
					&& (signLines[1] == null || signLines[1].length() == 0)
					&& (signLines[2] == null || signLines[2].length() == 0)
					&& (signLines[3] == null || signLines[3].length() == 0))) {
				block.setType(Material.AIR);
			} else {
				Sign signState = null;
				if(block.getType() != Material.WALL_SIGN && block.getType() != Material.SIGN_POST) {
					Material signType;
					try {
						signType = Material.valueOf(config.getString("general.signs."+key+".signType"));
					} catch(NullPointerException|IllegalArgumentException e) {
						signType = null;
					}
					if(signType != Material.WALL_SIGN && signType != Material.SIGN_POST) {
						AreaShop.debug("  setting sign failed");
						return false;
					}
					block.setType(signType);
					signState = (Sign)block.getState();
					org.bukkit.material.Sign signData = (org.bukkit.material.Sign)signState.getData();
					BlockFace signFace;
					try {
						signFace = BlockFace.valueOf(config.getString("general.signs."+key+".facing"));
					} catch(NullPointerException|IllegalArgumentException e) {
						signFace = null;
					}
					if(signFace != null) {
						signData.setFacingDirection(signFace);
						signState.setData(signData);
					}
				}
				if(signState == null) {
					signState = (Sign)block.getState();
				}
				org.bukkit.material.Sign signData = (org.bukkit.material.Sign)signState.getData();
				if(!config.isString("general.signs."+key+".signType")) {
					region.setSetting("general.signs."+key+".signType", signState.getType().toString());
				}
				if(!config.isString("general.signs."+key+".facing")) {
					region.setSetting("general.signs."+key+".facing", signData.getFacing().toString());
				}
				// Apply replacements and color and then set it on the sign
				for(int i = 0; i < signLines.length; i++) {
					if(signLines[i] == null) {
						signState.setLine(i, "");
						continue;
					}
					signLines[i] = region.applyAllReplacements(signLines[i]);
					signLines[i] = Utils.applyColors(signLines[i]);
					signState.setLine(i, signLines[i]);
				}
				signState.update();
			}
			return true;
		}

		/**
		 * Check if the sign needs to update periodically
		 * @return true if it needs periodic updates, otherwise false
		 */
		public boolean needsPeriodicUpdate() {
			// Get the prefix
			String prefix = "signProfiles."+getProfile()+"."+region.getState().getValue().toLowerCase()+".line";
			String line;
			// Get the lines
			for(int i = 1; i < 5; i++) {
				line = plugin.getConfig().getString(prefix+i);
				if(line != null && line.length() != 0 && line.contains(AreaShop.tagTimeLeft)) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Run commands when a player clicks a sign
		 * @param clicker   The player that clicked the sign
		 * @param clickType The type of clicking
		 * @return true if the commands ran successfully, false if any of them failed
		 */
		public boolean runSignCommands(Player clicker, GeneralRegion.ClickType clickType) {
			// Get the profile set in the config
			String profile = region.getConfig().getString("general.signs."+key+".profile");
			if(profile == null || profile.length() == 0) {
				profile = region.getStringSetting("general.signProfile");
			}

			// Get paths (state may change after running them)
			String playerPath = "signProfiles."+profile+"."+region.getState().getValue().toLowerCase()+"."+clickType.getValue()+"Player";
			String consolePath = "signProfiles."+profile+"."+region.getState().getValue().toLowerCase()+"."+clickType.getValue()+"Console";

			// Run player commands if specified
			List<String> playerCommands = new ArrayList<>();
			for(String command : plugin.getConfig().getStringList(playerPath)) {
				playerCommands.add(command.replace(AreaShop.tagClicker, clicker.getName()));
			}
			region.runCommands(clicker, playerCommands);

			// Run console commands if specified
			List<String> consoleCommands = new ArrayList<>();
			for(String command : plugin.getConfig().getStringList(consolePath)) {
				consoleCommands.add(command.replace(AreaShop.tagClicker, clicker.getName()));
			}
			region.runCommands(Bukkit.getConsoleSender(), consoleCommands);

			return !playerCommands.isEmpty() || !consoleCommands.isEmpty();
		}
	}
}
