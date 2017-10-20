package me.wiefferink.areashop.features;

import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.events.notify.UpdateRegionEvent;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.tools.Utils;
import me.wiefferink.interactivemessenger.processing.Message;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SignsFeature extends RegionFeature {

	private static Map<String, RegionSign> allSigns = new HashMap<>();

	private Map<String, RegionSign> signs;

	public SignsFeature() {

	}

	/**
	 * Constructor.
	 * @param region The region to bind to
	 */
	public SignsFeature(GeneralRegion region) {
		this.region = region;
		signs = new HashMap<>();
		// Setup current signs
		ConfigurationSection signSection = region.getConfig().getConfigurationSection("general.signs");
		if(signSection != null) {
			for(String signKey : signSection.getKeys(false)) {
				RegionSign sign = new RegionSign(region, signKey);
				Location location = sign.getLocation();
				if(location == null) {
					AreaShop.warn("Sign with key " + signKey + " of region " + region.getName() + " does not have a proper location");
					continue;
				}
				signs.put(sign.getStringLocation(), sign);
			}
			allSigns.putAll(signs);
		}
	}

	@Override
	public void shutdown() {
		// Deregister signs from the registry
		if(signs != null) {
			for(String key : signs.keySet()) {
				allSigns.remove(key);
			}
		}
	}

	/**
	 * Convert a location to a string to use as map key.
	 * @param location The location to get the key for
	 * @return A string to use in a map for a location
	 */
	private static String locationToString(Location location) {
		return location.getWorld().getName() + ";" + location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ();
	}

	/**
	 * Get a sign by a location.
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
	 * Update all signs connected to this region.
	 * @return true if all signs are updated correctly, false if one or more updates failed
	 */
	public boolean update() {
		boolean result = true;
		for(RegionSign sign : signs.values()) {
			result = result & sign.update();
		}
		return result;
	}

	/**
	 * Check if any of the signs need periodic updating.
	 * @return true if one or more of the signs need periodic updating, otherwise false
	 */
	public boolean needsPeriodicUpdate() {
		boolean result = false;
		for(RegionSign sign : signs.values()) {
			result = result | sign.needsPeriodicUpdate();
		}
		return result;
	}

	/**
	 * Get a list with all sign locations.
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
	 * Add a sign to this region.
	 * @param location The location of the sign
	 * @param signType The type of the sign (WALL_SIGN or SIGN_POST)
	 * @param facing   The orientation of the sign
	 * @param profile  The profile to use with this sign (null for default)
	 */
	public void addSign(Location location, Material signType, BlockFace facing, String profile) {
		int i = 0;
		while(region.getConfig().isSet("general.signs." + i)) {
			i++;
		}
		String signPath = "general.signs." + i + ".";
		region.setSetting(signPath + "location", Utils.locationToConfig(location));
		region.setSetting(signPath + "facing", facing != null ? facing.name() : null);
		region.setSetting(signPath + "signType", signType != null ? signType.name() : null);
		if(profile != null && profile.length() != 0) {
			region.setSetting(signPath + "profile", profile);
		}
		// Add to the map
		RegionSign sign = new RegionSign(region, i + "");
		signs.put(sign.getStringLocation(), sign);
		allSigns.put(sign.getStringLocation(), sign);
	}

	/**
	 * Checks if there is a sign from this region at the specified location.
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
			Location signLocation = Utils.configToLocation(region.getConfig().getConfigurationSection("general.signs." + sign + ".location"));
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
	 * Sign that is connected to a region to display information and interact with the region.
	 */
	public class RegionSign {

		private GeneralRegion region;
		private String key;

		public RegionSign(GeneralRegion region, String key) {
			this.region = region;
			this.key = key;
		}

		/**
		 * Get the location of this sign.
		 * @return The location of this sign
		 */
		public Location getLocation() {
			return Utils.configToLocation(region.getConfig().getConfigurationSection("general.signs." + key + ".location"));
		}

		/**
		 * Location string to be used as key in maps.
		 * @return Location string
		 */
		public String getStringLocation() {
			return locationToString(getLocation());
		}

		/**
		 * Get the region this sign is linked to.
		 * @return The region this sign is linked to
		 */
		public GeneralRegion getRegion() {
			return region;
		}

		/**
		 * Remove this sign from the region.
		 */
		public void remove() {
			getLocation().getBlock().setType(Material.AIR);
			signs.remove(getStringLocation());
			allSigns.remove(getStringLocation());
			region.setSetting("general.signs." + key, null);
		}

		/**
		 * Get the ConfigurationSection defining the sign layout.
		 * @return The sign layout config
		 */
		public ConfigurationSection getProfile() {
			return region.getConfigurationSectionSetting("general.signProfile", "signProfiles", region.getConfig().get("general.signs." + key + ".profile"));
		}

		/**
		 * Update this sign.
		 * @return true if the update was successful, otherwise false
		 */
		public boolean update() {
			if(region.isDeleted()) {
				return false;
			}

			YamlConfiguration regionConfig = region.getConfig();
			ConfigurationSection signConfig = getProfile();
			Block block = getLocation().getBlock();
			if(signConfig == null || !signConfig.isSet(region.getState().getValue())) {
				block.setType(Material.AIR);
				return true;
			}

			ConfigurationSection stateConfig = signConfig.getConfigurationSection(region.getState().getValue());

			// Get the lines
			String[] signLines = new String[4];
			boolean signEmpty = true;
			for(int i = 0; i < 4; i++) {
				signLines[i] = stateConfig.getString("line" + (i + 1));
				signEmpty &= (signLines[i] == null || signLines[i].isEmpty());
			}
			if(signEmpty) {
				block.setType(Material.AIR);
				return true;
			}

			Sign signState = null;
			// Place the sign back (with proper rotation and type) after it has been hidden or (indirectly) destroyed
			if(block.getType() != Material.WALL_SIGN && block.getType() != Material.SIGN_POST) {
				Material signType;
				try {
					signType = Material.valueOf(regionConfig.getString("general.signs." + key + ".signType"));
				} catch(NullPointerException | IllegalArgumentException e) {
					signType = null;
				}
				if(signType != Material.WALL_SIGN && signType != Material.SIGN_POST) {
					AreaShop.debug("Setting sign", key, "of region", region.getName(), "failed, could not set sign block back");
					return false;
				}
				block.setType(signType);
				signState = (Sign)block.getState();
				org.bukkit.material.Sign signData = (org.bukkit.material.Sign)signState.getData();
				BlockFace signFace;
				try {
					signFace = BlockFace.valueOf(regionConfig.getString("general.signs." + key + ".facing"));
				} catch(NullPointerException | IllegalArgumentException e) {
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

			// Save current rotation and type
			org.bukkit.material.Sign signData = (org.bukkit.material.Sign)signState.getData();
			if(!regionConfig.isString("general.signs." + key + ".signType")) {
				region.setSetting("general.signs." + key + ".signType", signState.getType().toString());
			}
			if(!regionConfig.isString("general.signs." + key + ".facing")) {
				region.setSetting("general.signs." + key + ".facing", signData.getFacing().toString());
			}

			// Apply replacements and color and then set it on the sign
			for(int i = 0; i < signLines.length; i++) {
				if(signLines[i] == null) {
					signState.setLine(i, "");
					continue;
				}
				signLines[i] = Message.fromString(signLines[i]).replacements(region).getSingle();
				signLines[i] = Utils.applyColors(signLines[i]);
				signState.setLine(i, signLines[i]);
			}
			signState.update();
			return true;
		}

		/**
		 * Check if the sign needs to update periodically.
		 * @return true if it needs periodic updates, otherwise false
		 */
		public boolean needsPeriodicUpdate() {
			ConfigurationSection signConfig = getProfile();
			if(signConfig == null || !signConfig.isSet(region.getState().getValue().toLowerCase())) {
				return false;
			}
			ConfigurationSection stateConfig = signConfig.getConfigurationSection(region.getState().getValue().toLowerCase());
			// Check the lines for the timeleft tag
			for(int i = 1; i <= 4; i++) {
				String line = stateConfig.getString("line" + i);
				if(line != null && !line.isEmpty() && line.contains(Message.VARIABLE_START + AreaShop.tagTimeLeft + Message.VARIABLE_END)) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Run commands when a player clicks a sign.
		 * @param clicker   The player that clicked the sign
		 * @param clickType The type of clicking
		 * @return true if the commands ran successfully, false if any of them failed
		 */
		public boolean runSignCommands(Player clicker, GeneralRegion.ClickType clickType) {
			ConfigurationSection signConfig = getProfile();
			if(signConfig == null) {
				return false;
			}
			ConfigurationSection stateConfig = signConfig.getConfigurationSection(region.getState().getValue().toLowerCase());

			// Run player commands if specified
			List<String> playerCommands = new ArrayList<>();
			for(String command : stateConfig.getStringList(clickType.getValue() + "Player")) {
				// TODO move variable checking code to InteractiveMessenger?
				playerCommands.add(command.replace(Message.VARIABLE_START + AreaShop.tagClicker + Message.VARIABLE_END, clicker.getName()));
			}
			region.runCommands(clicker, playerCommands);

			// Run console commands if specified
			List<String> consoleCommands = new ArrayList<>();
			for(String command : stateConfig.getStringList(clickType.getValue() + "Console")) {
				consoleCommands.add(command.replace(Message.VARIABLE_START + AreaShop.tagClicker + Message.VARIABLE_END, clicker.getName()));
			}
			region.runCommands(Bukkit.getConsoleSender(), consoleCommands);

			return !playerCommands.isEmpty() || !consoleCommands.isEmpty();
		}
	}
}
