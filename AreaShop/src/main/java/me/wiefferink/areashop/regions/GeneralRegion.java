package me.wiefferink.areashop.regions;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.Utils;
import me.wiefferink.areashop.events.NotifyAreaShopEvent;
import me.wiefferink.areashop.events.askandnotify.AddFriendEvent;
import me.wiefferink.areashop.events.askandnotify.DeleteFriendEvent;
import me.wiefferink.areashop.events.notify.RegionUpdateEvent;
import me.wiefferink.areashop.interfaces.GeneralRegionInterface;
import me.wiefferink.areashop.managers.FileManager;
import me.wiefferink.areashop.messages.Message;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class GeneralRegion implements GeneralRegionInterface, Comparable<GeneralRegion> {
	YamlConfiguration config;
	private static ArrayList<Material> canSpawnIn = new ArrayList<>(Arrays.asList(Material.WOOD_DOOR, Material.WOODEN_DOOR, Material.SIGN_POST, Material.WALL_SIGN, Material.STONE_PLATE, Material.IRON_DOOR_BLOCK, Material.WOOD_PLATE, Material.TRAP_DOOR, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.DRAGON_EGG, Material.GOLD_PLATE, Material.IRON_PLATE));
	private static ArrayList<Material> cannotSpawnOn = new ArrayList<>(Arrays.asList(Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE, Material.SIGN_POST, Material.WALL_SIGN, Material.STONE_PLATE, Material.IRON_DOOR_BLOCK, Material.WOOD_PLATE, Material.TRAP_DOOR, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.CACTUS, Material.IRON_FENCE, Material.FENCE_GATE, Material.THIN_GLASS, Material.NETHER_FENCE, Material.DRAGON_EGG, Material.GOLD_PLATE, Material.IRON_PLATE, Material.STAINED_GLASS_PANE));
	private static ArrayList<Material> cannotSpawnBeside = new ArrayList<>(Arrays.asList(Material.LAVA, Material.STATIONARY_LAVA, Material.CACTUS));
	AreaShop plugin = null;
	private boolean saveRequired = false;
	private boolean deleted = false;
	
	private HashMap<String, Object> replacementsCache = null;
	private long replacementsCacheTime = 0;

	// Enum for region types
	public enum RegionType {		
		RENT("rent"),
		BUY("buy");
		
		private final String value;

		RegionType(String value) {
			this.value = value;
		}
		public String getValue() {
			return value;
		}
	}

	// Enum for schematic event types
	public enum RegionEvent {		
		CREATED("created"),
		DELETED("deleted"),
		RENTED("rented"),
		EXTENDED("extended"),
		UNRENTED("unrented"),
		BOUGHT("bought"),
		SOLD("sold"),
		RESELL("resell");
		
		private final String value;

		RegionEvent(String value) {
			this.value = value;
		}
		public String getValue() {
			return value;
		}
	}

	// Enum for Region states
	public enum RegionState {
		FORRENT("forrent"),
		RENTED("rented"),
		FORSALE("forsale"),
		SOLD("sold"),
		RESELL("resell");

		private final String value;

		RegionState(String value) {
			this.value = value;
		}
		public String getValue() {
			return value;
		}
	}

	// Enum for click types
	public enum ClickType {
		RIGHTCLICK("rightClick"),
		LEFTCLICK("leftClick"),
		SHIFTRIGHTCLICK("shiftRightClick"),
		SHIFTLEFTCLICK("shiftLeftClick");
		
		private final String value;

		ClickType(String value) {
			this.value = value;
		}
		public String getValue() {
			return value;
		}
	}

	// Enum for limit types
	public enum LimitType {		
		RENTS("rents"),
		BUYS("buys"),
		TOTAL("total"),
		EXTEND("extend");
		
		private final String value;

		LimitType(String value) {
			this.value = value;
		}
		public String getValue() {
			return value;
		}
	}

	GeneralRegion(AreaShop plugin, YamlConfiguration config) {
		this.plugin = plugin;
		this.config = config;
	}

	GeneralRegion(AreaShop plugin, String name, World world) {
		this.plugin = plugin;
		
		config = new YamlConfiguration();
		setSetting("general.name", name);
		setSetting("general.world", world.getName());
		setSetting("general.type", getType().getValue().toLowerCase());
	}
		
	// ABSTRACT
	/**
	 * Get the region type of the region
	 * @return The RegionType of this region
	 */
	public abstract RegionType getType();	

	// Sorting by name

	/**
	 * Compare this region to another region by name
	 * @param o The region to compare to
	 * @return 0 if the names are the same, below zero if this region is earlier in the alphabet, otherwise above zero
	 */
	@Override
	public int compareTo(@Nonnull GeneralRegion o) {
		return getName().compareTo(o.getName());
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public boolean equals(Object region) {
		return region instanceof GeneralRegion && ((GeneralRegion)region).getName().equals(getName());
	}

	/**
	 * Get the config file that is used to store the region information
	 * @return The config file that stores the region information
	 */
	public YamlConfiguration getConfig() {
		return config;
	}

	/**
	 * Broadcast an event to indicate that region settings have been changed.
	 * This will update region flags, signs, etc.
	 */
	public void update() {
		Bukkit.getServer().getPluginManager().callEvent(new RegionUpdateEvent(this));
	}

	/**
	 * Broadcast the given event and update the region status
	 * @param event The update event that should be broadcasted
	 */
	public void notifyAndUpdate(NotifyAreaShopEvent event) {
		Bukkit.getPluginManager().callEvent(event);
		update();
	}
	
	/**
	 * Get the tag replacements, used in commands or signs
	 * @return A map with strings like '%region%' linking to the value to replace it with
	 */
	public abstract HashMap<String, Object> getSpecificReplacements();
	
	/**
	 * Get the state of a region
	 * @return The RegionState of the region
	 */
	public abstract RegionState getState();
	
	// GETTERS
	/**
	 * Get a list with all sign locations
	 * @return A List with all sign locations
	 */
	public List<Location> getSignLocations() {
		List<Location> result = new ArrayList<>();
		if(config.getConfigurationSection("general.signs") == null) {
			return result;
		}
		for(String signName : config.getConfigurationSection("general.signs").getKeys(false)) {
			result.add(Utils.configToLocation(config.getConfigurationSection("general.signs." + signName + ".location")));
		}
		return result;
	}
	
	/**
	 * Get the teleportlocation set for this region
	 * @return The teleport location, or null if not set
	 */
	public Location getTeleportLocation() {
		Location result;
		result = Utils.configToLocation(config.getConfigurationSection("general.teleportLocation"));
		return result;
	}
	
	/**
	 * Check if the region has been deleted
	 * @return true if the region has been deleted, otherwise false
	 */
	public boolean isDeleted() {
		return deleted;
	}
	
	/**
	 * Indicate that this region has been deleted
	 */
	public void setDeleted() {
		deleted = true;
	}
	
	/**
	 * Get the name of the region
	 * @return The region name
	 */
	public String getName() {
		return config.getString("general.name");
	}
	/**
	 * Get the lowercase region name
	 * @return The region name in lowercase
	 */
	public String getLowerCaseName() {
		return getName().toLowerCase();
	}
	
	/**
	 * Check if restoring is enabled
	 * @return true if restoring is enabled, otherwise false
	 */
	public boolean isRestoreEnabled() {
		return getBooleanSetting("general.enableRestore");
	}
	/**
	 * Get the restoreprofile as defined in config.yml
	 * @return The restoreprofile
	 */
	public String getRestoreProfile() {
		return getStringSetting("general.schematicProfile");
	}
	
	/**
	 * Get the time that the player was last active
	 * @return Current time if he is online, last online time if offline, -1 if the region has no owner
	 */
	public long getLastActiveTime() {
		if(getOwner() == null) {
			return -1;
		}
		Player player = Bukkit.getPlayer(getOwner());
		long savedTime = getLongSetting("general.lastActive");
		// Check if he is online currently
		if(player != null || savedTime == 0) {
			return Calendar.getInstance().getTimeInMillis();
		}
		return savedTime;
	}
	
	/**
	 * Set the last active time of the player to the current time
	 */
	public void updateLastActiveTime() {
		if(getOwner() != null) {
			setSetting("general.lastActive", Calendar.getInstance().getTimeInMillis());
		}
	}
	
	public void removeLastActiveTime() {
		setSetting("general.lastActive", null);
	}
	
	/**
	 * Get the World of the region
	 * @return The World where the region is located
	 */
	public World getWorld() {
		return Bukkit.getWorld(getWorldName());
	}
	/**
	 * Get the name of the world where the region is located
	 * @return The name of the world of the region
	 */
	public String getWorldName() {
		return getStringSetting("general.world");
	}
	
	/**
	 * Get the FileManager from the plugin
	 * @return The FileManager (responsible for saving/loading regions and getting them)
	 */
	public FileManager getFileManager() {
		return plugin.getFileManager();
	}
	
	/**
	 * Check if the players is owner of this region
	 * @param player Player to check ownership for
	 * @return true if the player currently rents or buys this region
	 */
	public boolean isOwner(OfflinePlayer player) {
		return isOwner(player.getUniqueId());
	}
	
	/**
	 * Check if the players is owner of this region
	 * @param player Player to check ownership for
	 * @return true if the player currently rents or buys this region
	 */
	public boolean isOwner(UUID player) {
		return (isRentRegion() && ((RentRegion)this).isRenter(player)) || (isBuyRegion() && ((BuyRegion)this).isBuyer(player));
	}
	
	/**
	 * Get the player that is currently the owner of this region (either bought or rented it)
	 * @return The UUID of the owner of this region
	 */
	public UUID getOwner() {
		if(isRentRegion()) {
			return ((RentRegion)this).getRenter();
		} else {
			return ((BuyRegion)this).getBuyer();
		}
	}
	
	/**
	 * Get the landlord of this region (the player that receives any revenue from this region)
	 * @return The UUID of the landlord of this region
	 */
	public UUID getLandlord() {
		String landlord = getStringSetting("general.landlord");
		if(landlord != null) {
			try {
				return UUID.fromString(landlord);
			} catch(IllegalArgumentException e) {
				// Incorrect UUID
			}
		}
		return null;
	}
	
	/**
	 * Get the name of the landlord
	 * @return The name of the landlord, if unavailable by UUID it will return the old cached name, if that is unavailable it will return &lt;UNKNOWN&gt;
	 */
	public String getLandlordName() {
		String result = Utils.toName(getLandlord());
		if(result == null || result.isEmpty()) {
			result = config.getString("general.landlordName");
			if(result == null || result.isEmpty()) {
				result = null;
			}
		}
		return result;
	}
	
	/**
	 * Set the landlord of this region (the player that receives all revenue of this region)
	 * @param landlord The UUID of the player that should be set as landlord
	 * @param name The backup name of the player (for in case that the UUID cannot be resolved to a playername)
	 */
	public void setLandlord(UUID landlord, String name) {
		if(landlord != null) {
			setSetting("general.landlord", landlord.toString());
		}
		String properName = Utils.toName(landlord);
		if(properName == null) {
			properName = name;
		}
		setSetting("general.landlordName", properName);
	}
	
	/**
	 * Remove the landlord from this region
	 */
	public void removelandlord() {
		setSetting("general.landlord", null);
		setSetting("general.landlordName", null);
	}
	
	/**
	 * Check if the specified player is the landlord of this region
	 * @param landlord The UUID of the players to check for landlord
	 * @return true if the player is the landlord, otherwise false
	 */
	public boolean isLandlord(UUID landlord) {
		return landlord !=null && getLandlord() != null && getLandlord().equals(landlord);
	}
	
	/**
	 * Get the WorldGuard region associated with this AreaShop region
	 * @return The ProtectedRegion of WorldGuard or null if the region does not exist anymore
	 */
	public ProtectedRegion getRegion() {
		if(getWorld() == null 
				|| plugin.getWorldGuard() == null 
				|| plugin.getWorldGuard().getRegionManager(getWorld()) == null
				|| plugin.getWorldGuard().getRegionManager(getWorld()).getRegion(getName()) == null) {
			return null;
		}
		return plugin.getWorldGuard().getRegionManager(getWorld()).getRegion(getName());
	}
	
	/**
	 * Get the width of the region (x-axis)
	 * @return The width of the region (x-axis)
	 */
	public int getWidth() {
		if(getRegion() == null) {
			return 0;
		}
		return getRegion().getMaximumPoint().getBlockX() - getRegion().getMinimumPoint().getBlockX() +1;
	}
	/**
	 * Get the depth of the region (z-axis)
	 * @return The depth of the region (z-axis)
	 */
	public int getDepth() {
		if(getRegion() == null) {
			return 0;
		}
		return getRegion().getMaximumPoint().getBlockZ() - getRegion().getMinimumPoint().getBlockZ() +1;
	}
	/**
	 * Get the height of the region (y-axis)
	 * @return The height of the region (y-axis)
	 */
	public int getHeight() {
		if(getRegion() == null) {
			return 0;
		}
		return getRegion().getMaximumPoint().getBlockY() - getRegion().getMinimumPoint().getBlockY() +1;
	}
	
	/**
	 * Get the groups that this region is added to
	 * @return A Set with all groups of this region
	 */
	public Set<RegionGroup> getGroups() {
		Set<RegionGroup> result = new HashSet<>();
		for(RegionGroup group : plugin.getFileManager().getGroups()) {
			if(group.isMember(this)) {
				result.add(group);
			}
		}	
		return result;
	}
	
	/**
	 * Get a list of names from groups this region is in
	 * @return A list of groups this region is part of
	 */
	public List<String> getGroupNames() {
		List<String> result = new ArrayList<>();
		for(RegionGroup group : getGroups()) {
			result.add(group.getName());
		}
		return result;
	}
	
	/**
	 * Get all the replacements for this region
	 * @return Map with the keys that need to be replaced with the value of the object
	 */
	public HashMap<String, Object> getAllReplacements() {
		// Reply with cache if we have one
		if(replacementsCache != null && (Calendar.getInstance().getTimeInMillis() - replacementsCacheTime) < 1000) {
			return replacementsCache;
		}
		
		HashMap<String, Object> result = getSpecificReplacements();
		
		result.put(AreaShop.tagRegionName, getName());
		result.put(AreaShop.tagRegionType, getType().getValue().toLowerCase());
		result.put(AreaShop.tagWorldName, getWorldName());
		result.put(AreaShop.tagWidth, getWidth());
		result.put(AreaShop.tagDepth, getDepth());
		result.put(AreaShop.tagHeight, getHeight());
		result.put(AreaShop.tagFriends, Utils.createCommaSeparatedList(getFriendNames()));
		result.put(AreaShop.tagFriendsUUID, Utils.createCommaSeparatedList(getFriends()));
		result.put(AreaShop.tagLandlord, getLandlordName());
		result.put(AreaShop.tagLandlordUUID, getLandlord());
		// Date/time stuff
		Calendar calendar = Calendar.getInstance();
		result.put(AreaShop.tagEpoch, calendar.getTimeInMillis());
		result.put(AreaShop.tagMillisecond, calendar.get(Calendar.MILLISECOND));
		result.put(AreaShop.tagSecond, calendar.get(Calendar.SECOND));
		result.put(AreaShop.tagMinute, calendar.get(Calendar.MINUTE));
		result.put(AreaShop.tagHour, calendar.get(Calendar.HOUR_OF_DAY));
		result.put(AreaShop.tagDay, calendar.get(Calendar.DAY_OF_MONTH));
		result.put(AreaShop.tagMonth, calendar.get(Calendar.MONTH));
		result.put(AreaShop.tagYear, calendar.get(Calendar.YEAR));
		// In specified format
		SimpleDateFormat date = new SimpleDateFormat(plugin.getConfig().getString("timeFormatChat"));
		String dateString = date.format(Calendar.getInstance().getTime());
		result.put(AreaShop.tagDateTime, dateString);
		date = new SimpleDateFormat(plugin.getConfig().getString("timeFormatSign"));
		dateString = date.format(Calendar.getInstance().getTime());
		result.put(AreaShop.tagDateTimeShort, dateString);
		// Teleport location
		Location tp = getTeleportLocation();
		if(tp != null) {
			result.put(AreaShop.tagTeleportBlockX, tp.getBlockX());
			result.put(AreaShop.tagTeleportBlockY, tp.getBlockY());
			result.put(AreaShop.tagTeleportBlockZ, tp.getBlockZ());
			result.put(AreaShop.tagTeleportX, tp.getX());
			result.put(AreaShop.tagTeleportY, tp.getY());
			result.put(AreaShop.tagTeleportZ, tp.getZ());
			result.put(AreaShop.tagTeleportPitch, tp.getPitch());
			result.put(AreaShop.tagTeleportYaw, tp.getYaw());
			result.put(AreaShop.tagTeleportPitchRound, Math.round(tp.getPitch()));
			result.put(AreaShop.tagTeleportYawRound, Math.round(tp.getYaw()));
			result.put(AreaShop.tagTeleportWorld, tp.getWorld().getName());
		}

		replacementsCache = result;
		replacementsCacheTime = Calendar.getInstance().getTimeInMillis();
		return result;
	}
	
	/**
	 * Applies all replacements to the given string
	 * @param source The source string to replace things in
	 * @return The result string with all the replacements applied
	 */
	public String applyAllReplacements(String source) {
		if(source == null || source.length() == 0) {
			return "";
		}
		// Apply static replacements
		HashMap<String, Object> replacements = getAllReplacements();
		for(String tag : replacements.keySet()) {
			Object replacement = replacements.get(tag);
			if(replacement != null) {
				source = source.replace(tag, replacement.toString());
			}
		}
		return source;		
	}
	
	/**
	 * Check if the region has a teleportLocation specified
	 * @return true if the region has a teleportlocation, false otherwise
	 */
	public boolean hasTeleportLocation() {
		return config.isSet("general.teleportLocation");
	}
	
	/**
	 * Check if this region is a RentRegion
	 * @return true if this region is a RentRegion otherwise false
	 */
	public boolean isRentRegion() {
		return getType() == RegionType.RENT;
	}
	
	/**
	 * Check if this region is a BuyRegion
	 * @return true if this region is a BuyRegion otherwise false
	 */
	public boolean isBuyRegion() {
		return getType() == RegionType.BUY;
	}
	
	/**
	 * Check if for renting this region you should be inside of it
	 * @return true if you need to be inside, otherwise false
	 */
	public boolean restrictedToRegion() {
		return getBooleanSetting("general.restrictedToRegion");
	}
	
	/**
	 * Check if for renting you need to be in the correct world
	 * @return true if you need to be in the same world as the region, otherwise false
	 */
	public boolean restrictedToWorld() {
		return getBooleanSetting("general.restrictedToWorld") || restrictedToRegion();
	}


	/**
	 * Add a friend to the region
	 * @param player The UUID of the player to add
	 * @param by The CommandSender that is adding the friend, or null
	 * @return true if the friend has been added, false if adding a friend was cancelled by another plugin
	 */
	public boolean addFriend(UUID player, CommandSender by) {
		// Fire and check event
		AddFriendEvent event = new AddFriendEvent(this, Bukkit.getOfflinePlayer(player), by);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) {
			plugin.message(by, "general-cancelled", event.getReason(), this);
			return false;
		}

		Set<String> friends = new HashSet<>(config.getStringList("general.friends"));
		friends.add(player.toString());
		List<String> list = new ArrayList<>(friends);
		setSetting("general.friends", list);
		return true;
	}

	/**
	 * Delete a friend from the region
	 * @param player The UUID of the player to delete
	 * @param by The CommandSender that is adding the friend, or null
	 * @return true if the friend has been added, false if adding a friend was cancelled by another plugin
	 */
	public boolean deleteFriend(UUID player, CommandSender by) {
		// Fire and check event
		DeleteFriendEvent event = new DeleteFriendEvent(this, Bukkit.getOfflinePlayer(player), by);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) {
			plugin.message(by, "general-cancelled", event.getReason(), this);
			return false;
		}

		Set<String> friends = new HashSet<>(config.getStringList("general.friends"));
		friends.remove(player.toString());
		List<String> list = new ArrayList<>(friends);
		if(list.isEmpty()) {
			setSetting("general.friends", null);
		} else {
			setSetting("general.friends", list);
		}
		return true;
	}
	
	/**
	 * Get the list of friends added to this region
	 * @return Friends added to this region
	 */
	public Set<UUID> getFriends() {
		HashSet<UUID> result = new HashSet<>();
		for(String friend : config.getStringList("general.friends")) {
			try {
				UUID id = UUID.fromString(friend);
				result.add(id);
			} catch(IllegalArgumentException e) {
				// Don't add it
			}
		}
		return result;
	}
	
	/**
	 * Get the list of friends added to this region
	 * @return Friends added to this region
	 */
	public Set<String> getFriendNames() {
		HashSet<String> result = new HashSet<>();
		for(UUID friend : getFriends()) {
			OfflinePlayer player = Bukkit.getOfflinePlayer(friend);
			if(player != null) {
				result.add(player.getName());
			}
		}
		return result;
	}
	
	/**
	 * Remove all friends that are added to this region
	 */
	public void clearFriends() {
		setSetting("general.friends", null);
	}
	
	/**
	 * Check now if the player has been inactive for too long, unrent/sell will happen when true
	 * @return true if the region has been unrented/sold, otherwise false
	 */
	public abstract boolean checkInactive();
	
	/**
	 * Add a sign to this region
	 * @param location The location of the sign
	 * @param signType The type of the sign (WALL_SIGN or SIGN_POST)
	 * @param facing The orientation of the sign
	 * @param profile The profile to use with this sign (null for default)
	 */
	public void addSign(Location location, Material signType, BlockFace facing, String profile) {
		int i = 0;
		while(config.isSet("general.signs." + i)) {
			i++;
		}
		String signPath = "general.signs." + i + ".";
		setSetting(signPath + "location", Utils.locationToConfig(location));
		setSetting(signPath + "facing", facing.name());
		setSetting(signPath + "signType", signType.name());
		if(profile != null && profile.length() != 0) {
			setSetting(signPath + "profile", profile);
		}
	}
	
	/**
	 * Get the name of the sign at the specified location
	 * @param location The location to check
	 * @return The name of the sign if found, otherwise null
	 */
	public String getSignName(Location location) {
		String result = null;
		if(config.getConfigurationSection("general.signs") == null) {
			return null;
		}
		for(String signName : config.getConfigurationSection("general.signs").getKeys(false)) {
			if(location.equals(Utils.configToLocation(config.getConfigurationSection("general.signs." + signName + ".location")))) {
				result = signName;
			}
		}
		return result;
	}
	
	/**
	 * Remove a sign
	 * @param name Name of the sign to be removed
	 */
	public void removeSign(String name) {
		setSetting("general.signs." + name, null);
	}
	public void removeSign(Location location) {
		if(location == null) {
			return;
		}
		String name = getSignName(location);
		location.getBlock().setType(Material.AIR);
		if(name != null) {
			removeSign(name);
		}
	}
	
	/**
	 * Checks if there is a sign from this region at the specified location
	 * @param location Location to check
	 * @return true if this region has a sign at the location, otherwise false
	 */
	public boolean isSignOfRegion(Location location) {
		Set<String> signs;
		if(config.getConfigurationSection("general.signs") == null) {
			return false;
		}
		signs = config.getConfigurationSection("general.signs").getKeys(false);
		for(String sign : signs) {
			Location signLocation = Utils.configToLocation(config.getConfigurationSection("general.signs." + sign + ".location"));
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
	 * Method to send a message to a CommandSender, using chatprefix if it is a player
	 * Automatically includes the region in the message, enabling the use of all variables
	 * @param target The CommandSender you wan't to send the message to (e.g. a player)
	 * @param key    The key to get the translation
	 * @param prefix Specify if the message should have a prefix
	 * @param params The parameters to inject into the message string
	 */
	public void configurableMessage(Object target, String key, boolean prefix, Object... params) {
		Object[] newParams = new Object[params.length + 1];
		newParams[0] = this;
		System.arraycopy(params, 0, newParams, 1, params.length);
		Message.fromKey(key).prefix(prefix).replacements(newParams).send(target);
	}

	public void messageNoPrefix(Object target, String key, Object... params) {
		configurableMessage(target, key, false, params);
	}

	public void message(Object target, String key, Object... params) {
		configurableMessage(target, key, true, params);
	}
	
	/**
	 * Check if a sign needs periodic updating
	 * @return true if the signs of this region need periodic updating, otherwise false
	 */
	public boolean needsPeriodicUpdating() {
		if(isDeleted() || !isRentRegion()) {
			return false;
		}
		Set<String> signs = new HashSet<>();
		if(config.getConfigurationSection("general.signs") != null) {
			signs = config.getConfigurationSection("general.signs").getKeys(false);
		}
		for(String sign : signs) {
			// Get the profile set in the config
			String profile = config.getString("general.signs." + sign + ".profile");
			if(profile == null || profile.length() == 0) {
				profile = getStringSetting("general.signProfile");
			}			
			// Get the prefix
			String prefix = "signProfiles." + profile + "." + getState().getValue().toLowerCase() + ".line";
			String line;
			// Get the lines
			for(int i=1; i<5; i++) {
				line = plugin.getConfig().getString(prefix + i);
				if(line != null && line.length() != 0 && line.contains(AreaShop.tagTimeLeft)) {
					return true;
				}
			}	
		}
		return false;
	}
	
	/**
	 * Change the restore setting
	 * @param restore true, false or general
	 */
	public void setRestoreSetting(Boolean restore) {
		setSetting("general.enableRestore", restore);
	}
	
	/**
	 * Change the restore profile
	 * @param profile default or the name of the profile as set in the config
	 */
	public void setSchematicProfile(String profile) {
		setSetting("general.schematicProfile", profile);
	}
	
	/**
	 * Save all blocks in a region for restoring later
	 * @param fileName The name of the file to save to (extension and folder will be added)
	 * @return true if the region has been saved properly, otherwise false
	 */
	public boolean saveRegionBlocks(String fileName) {
		// Check if the region is correct
		ProtectedRegion region = getRegion();
		if(region == null) {
			AreaShop.debug("Region '" + getName() + "' does not exist in WorldGuard, save failed");
			return false;
		}
		// The path to save the schematic
		File saveFile = new File(plugin.getFileManager().getSchematicFolder() + File.separator + fileName + AreaShop.schematicExtension);
        // Create parent directories
        File parent = saveFile.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
            	plugin.getLogger().warning("Did not save region " + getName() + ", schematic directory could not be created: " + saveFile);
                return false;
            }
        }		
		boolean result = plugin.getWorldEditHandler().saveRegionBlocks(saveFile, this);
		if(result) {
			AreaShop.debug("Saved schematic for region " + getName());
		}
		return true;
	}
	
	/**
	 * Restore all blocks in a region for restoring later
	 * @param fileName The name of the file to save to (extension and folder will be added)
	 * @return true if the region has been restored properly, otherwise false
	 */
	public boolean restoreRegionBlocks(String fileName) {
		if(getRegion() == null) {
			AreaShop.debug("Region '" + getName() + "' does not exist in WorldGuard, restore failed");
			return false;
		}
		// The path to save the schematic
		File restoreFile = new File(plugin.getFileManager().getSchematicFolder() + File.separator + fileName + AreaShop.schematicExtension);
		if(!restoreFile.exists() || !restoreFile.isFile()) {
			plugin.getLogger().info("Did not restore region " + getName() + ", schematic file does not exist: " + restoreFile);
			return false;
		}
		boolean result = plugin.getWorldEditHandler().restoreRegionBlocks(restoreFile, this);
		if(result) {
			AreaShop.debug("Restored schematic for region " + getName());
		}
		return result;
	}
	
	/**
	 * Reset all flags of the region
	 */
	public void resetRegionFlags() {
		ProtectedRegion region = getRegion();
		if(region != null) {
			region.setFlag(DefaultFlag.GREET_MESSAGE, null);
			region.setFlag(DefaultFlag.FAREWELL_MESSAGE, null);
		}
	}
	
	/**
	 * Indicate this region needs to be saved, saving will happen by a repeating task
	 */
	public void saveRequired() {
		replacementsCache = null; // Remove cache
		saveRequired = true;
	}
	
	/**
	 * Check if a save is required
	 * @return true if a save is required because some data changed, otherwise false
	 */
	public boolean isSaveRequired() {
		return saveRequired && !isDeleted();
	}
	
	/**
	 * Save this region to disk now, using this method could slow down the plugin, normally saveRequired() should be used
	 * @return true if the region is saved successfully, otherwise false
	 */
	public boolean saveNow() {
		if(isDeleted()) {
			return false;
		}
		saveRequired = false;
		File file = new File(plugin.getFileManager().getRegionFolder() + File.separator + getName().toLowerCase() + ".yml");
		try {
			config.save(file);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	
	/**
	 * Set the teleport location of this region
	 * @param location The location to set as teleport location
	 */
	public void setTeleport(Location location) {
		if(location == null) {
			setSetting("general.teleportLocation", null);
		} else {
			setSetting("general.teleportLocation", Utils.locationToConfig(location, true));
		}
	}
	
	
	/**
	 * Teleport a player to the region
	 * @param player Player that should be teleported
	 * @param toSign true to teleport to the first sign of the region, false for teleporting to the region itself
	 * @param checkPermissions Set to true if teleport permissions should be checked, false otherwise
	 * @return true if the teleport succeeded, otherwise false
	 */
	public boolean teleportPlayer(Player player, boolean toSign, boolean checkPermissions) {
		int checked = 1;
		boolean owner;
		boolean friend = getFriends().contains(player.getUniqueId());
		Location startLocation = null;
		ProtectedRegion region = getRegion();
		if(getWorld() == null) {
			message(player, "general-noWorld");
			return false;
		}
		if(getRegion() == null) {
			message(player, "general-noRegion");
			return false;
		}	
		if(isRentRegion()) {
			owner = player.getUniqueId().equals(((RentRegion)this).getRenter());
		} else {
			owner = player.getUniqueId().equals(((BuyRegion)this).getBuyer());
		}
		
		if(checkPermissions) {
			// Teleport to sign instead if they dont have permission for teleporting to region
			if(		  (!toSign && owner && !player.hasPermission("areashop.teleport") && player.hasPermission("areashop.teleportsign")
					|| !toSign && !owner && !friend && !player.hasPermission("areashop.teleportall") && player.hasPermission("areashop.teleportsignall")
					|| !toSign && !owner && friend && !player.hasPermission("areashop.teleportfriend") && player.hasPermission("areashop.teleportfriendsign"))) {
				message(player, "teleport-changedToSign");
				toSign = true;
			}
			// Check permissions
			if(owner && !player.hasPermission("areashop.teleport") && !toSign) {
				message(player, "teleport-noPermission");
				return false;
			} else if(!owner && !player.hasPermission("areashop.teleportall") && !toSign && !friend) {
				message(player, "teleport-noPermissionOther");
				return false;
			} else if(!owner && !player.hasPermission("areashop.teleportfriend") && !toSign && friend) {
				message(player, "teleport-noPermissionFriend");
				return false;
			} else if(owner && !player.hasPermission("areashop.teleportsign") && toSign) {
				message(player, "teleport-noPermissionSign");
				return false;
			} else if(!owner && !player.hasPermission("areashop.teleportsignall") && toSign && !friend) {
				message(player, "teleport-noPermissionOtherSign");
				return false;
			} else if(!owner && !player.hasPermission("areashop.teleportfriendsign") && toSign && friend) {
				message(player, "teleport-noPermissionFriendSign");
				return false;
			}
		}
	
		List<Location> signs = getSignLocations();
		boolean signAvailable = !signs.isEmpty();
		if(toSign) {
			if(signAvailable) {
				// Use the location 1 below the sign to prevent weird spawing above the sign
				startLocation = signs.get(0).subtract(0.0, 1.0, 0.0);
				startLocation.setPitch(player.getLocation().getPitch());
				startLocation.setYaw(player.getLocation().getYaw());
			} else {
				// No sign available
				message(player, "teleport-changedToNoSign");
				toSign = false;
			}
		}		

		if(startLocation == null && this.hasTeleportLocation()) {
			startLocation = getTeleportLocation();
		}
		// Set default startLocation if not set
		if(startLocation == null) {
			// Set to block in the middle, y configured in the config
			Vector middle = Vector.getMidpoint(region.getMaximumPoint(), region.getMinimumPoint());
			String configSetting = getStringSetting("general.teleportLocationY");
			if("bottom".equalsIgnoreCase(configSetting)) {
				middle = middle.setY(region.getMinimumPoint().getBlockY());
			} else if("top".equalsIgnoreCase(configSetting)) {
				middle = middle.setY(region.getMaximumPoint().getBlockY());
			} else if("middle".equalsIgnoreCase(configSetting)) {
				middle = middle.setY(middle.getBlockY());
			} else {
				try {
					int vertical = Integer.parseInt(configSetting);
					middle = middle.setY(vertical);
				} catch(NumberFormatException e) {
					plugin.getLogger().warning("Could not parse general.teleportLocationY: '"+configSetting+"'");
				}
			}
			startLocation = new Location(getWorld(), middle.getX(), middle.getY(), middle.getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
		}
		boolean insideRegion;
		if(toSign) {
			insideRegion = getBooleanSetting("general.teleportToSignIntoRegion");
		} else {
			insideRegion = getBooleanSetting("general.teleportIntoRegion");
		}
		int maxTries = plugin.getConfig().getInt("maximumTries");

		// set location in the center of the block
		startLocation.setX(startLocation.getBlockX() + 0.5);
		startLocation.setZ(startLocation.getBlockZ() + 0.5);
		
		// Check locations starting from startLocation and then a cube that increases
		// radius around that (until no block in the region is found at all cube sides)
		Location safeLocation = startLocation;
		int radius = 1;
		boolean blocksInRegion = region.contains(startLocation.getBlockX(), startLocation.getBlockY(), startLocation.getBlockZ());
		if(!blocksInRegion && insideRegion) {
			message(player, "teleport-blocked");
			return false;
		}
		boolean done = isSafe(safeLocation) && ((blocksInRegion && insideRegion) || (!insideRegion));
		boolean north=false, east=false, south=false, west=false, top=false, bottom=false;
		boolean track;
		while(((blocksInRegion && insideRegion) || (!insideRegion)) && !done) {
			blocksInRegion = false;
			// North side
			track = false;
			for(int x=-radius+1; x<=radius && !done && !north; x++) {
				for(int y=-radius+1; y<radius && !done; y++) {
					safeLocation = startLocation.clone().add(x, y, -radius);
					if(safeLocation.getBlockY()>256 || safeLocation.getBlockY()<0) {
						continue;
					}
					if(region.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						track = true;
					}
				}
			}
			north = north || !track;
			
			// East side
			track = false;
			for(int z=-radius+1; z<=radius && !done && !east; z++) {
				for(int y=-radius+1; y<radius && !done; y++) {
					safeLocation = startLocation.clone().add(radius, y, z);
					if(safeLocation.getBlockY()>256 || safeLocation.getBlockY()<0) {
						continue;
					}
					if(region.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						track = true;
					}
				}
			}
			east = east || !track;
			
			// South side
			track = false;
			for(int x=radius-1; x>=-radius && !done && !south; x--) {
				for(int y=-radius+1; y<radius && !done; y++) {
					safeLocation = startLocation.clone().add(x, y, radius);
					if(safeLocation.getBlockY()>256 || safeLocation.getBlockY()<0) {
						continue;
					}
					if(region.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						track = true;
					}
				}
			}
			south = south || !track;
			
			// West side
			track = false;
			for(int z=radius-1; z>=-radius && !done && !west; z--) {
				for(int y=-radius+1; y<radius && !done; y++) {
					safeLocation = startLocation.clone().add(-radius, y, z);
					if(safeLocation.getBlockY()>256 || safeLocation.getBlockY()<0) {
						continue;
					}
					if(region.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						track = true;
					}
				}
			}
			west = west || !track;
			
			// Top side
			track = false;
			// Middle block of the top
			if((startLocation.getBlockY() + radius) > 256) {
				top = true;
			}
			if(!done && !top) {
				safeLocation = startLocation.clone().add(0, radius, 0);
				if(region.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
					checked++;
					done = isSafe(safeLocation) || checked > maxTries;
					blocksInRegion = true;
					track = true;
				}
			}
			for(int r=1; r<=radius && !done && !top; r++) {
				// North
				for(int x=-r+1; x<=r && !done; x++) {
					safeLocation = startLocation.clone().add(x, radius, -r);
					if(region.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						track = true;
					}
				}
				// East
				for(int z=-r+1; z<=r && !done; z++) {
					safeLocation = startLocation.clone().add(r, radius, z);
					if(region.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						track = true;
					}
				}
				// South side
				for(int x=r-1; x>=-r && !done; x--) {
					safeLocation = startLocation.clone().add(x, radius, r);
					if(region.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						track = true;
					}
				}
				// West side
				for(int z=r-1; z>=-r && !done; z--) {
					safeLocation = startLocation.clone().add(-r, radius, z);
					if(region.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						track = true;
					}
				}			
			}
			top = top || !track;
			
			// Bottom side
			track = false;
			// Middle block of the bottom
			if(startLocation.getBlockY() - radius < 0) {
				bottom = true;
			}
			if(!done && !bottom) {
				safeLocation = startLocation.clone().add(0, -radius, 0);
				if(region.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
					checked++;
					done = isSafe(safeLocation) || checked > maxTries;
					blocksInRegion = true;
					track = true;
				}
			}
			for(int r=1; r<=radius && !done && !bottom; r++) {
				// North
				for(int x=-r+1; x<=r && !done; x++) {
					safeLocation = startLocation.clone().add(x, -radius, -r);
					if(region.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						track = true;
					}
				}
				// East
				for(int z=-r+1; z<=r && !done; z++) {
					safeLocation = startLocation.clone().add(r, -radius, z);
					if(region.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						track = true;
					}
				}
				// South side
				for(int x=r-1; x>=-r && !done; x--) {
					safeLocation = startLocation.clone().add(x, -radius, r);
					if(region.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						track = true;
					}
				}
				// West side
				for(int z=r-1; z>=-r && !done; z--) {
					safeLocation = startLocation.clone().add(-r, -radius, z);
					if(region.contains(safeLocation.getBlockX(), safeLocation.getBlockY(), safeLocation.getBlockZ()) || !insideRegion) {
						checked++;
						done = isSafe(safeLocation) || checked > maxTries;
						blocksInRegion = true;
						track = true;
					}
				}			
			}
			bottom = bottom || !track;
			
			// Increase cube radius
			radius++;
		}
		if(done && isSafe(safeLocation)) {
			if(toSign) {
				message(player, "teleport-successSign");
			} else {
				message(player, "teleport-success");
			}			
			player.teleport(safeLocation);
			AreaShop.debug("Found location: " + safeLocation.toString() + " Tries: " + (checked-1));
			return true;
		} else {
			message(player, "teleport-noSafe", checked-1, maxTries);
			AreaShop.debug("No location found, checked " + (checked-1) + " spots of max " + maxTries);
			return false;
		}	
	}
	public boolean teleportPlayer(Player player, boolean toSign) {
		return teleportPlayer(player, toSign, true);
	}
	public boolean teleportPlayer(Player player) {
		return teleportPlayer(player, false, true);
	}
	
	/**
	 * Checks if a certain location is safe to teleport to
	 * @param location The location to check
	 * @return true if it is safe, otherwise false
	 */
	protected boolean isSafe(Location location) {
		Block feet = location.getBlock();
		Block head = feet.getRelative(BlockFace.UP);
		Block below = feet.getRelative(BlockFace.DOWN);
		Block above = head.getRelative(BlockFace.UP);
		// Check the block at the feet of the player
		if((feet.getType().isSolid() && !canSpawnIn.contains(feet.getType())) || feet.isLiquid()) {
			return false;
		} else if((head.getType().isSolid() && !canSpawnIn.contains(head.getType())) || head.isLiquid()) {
			return false;
		} else if(!below.getType().isSolid() || cannotSpawnOn.contains(below.getType()) || below.isLiquid()) {
			return false;
		} else if(above.isLiquid() || cannotSpawnBeside.contains(above.getType())) {
			return false;
		}
		// Check all blocks around
		ArrayList<Material> around = new ArrayList<>(Arrays.asList(
				feet.getRelative(BlockFace.NORTH).getType(),
				feet.getRelative(BlockFace.NORTH_EAST).getType(),
				feet.getRelative(BlockFace.EAST).getType(),
				feet.getRelative(BlockFace.SOUTH_EAST).getType(),
				feet.getRelative(BlockFace.SOUTH).getType(),
				feet.getRelative(BlockFace.SOUTH_WEST).getType(),
				feet.getRelative(BlockFace.WEST).getType(),
				feet.getRelative(BlockFace.NORTH_WEST).getType(),
				below.getRelative(BlockFace.NORTH).getType(),
				below.getRelative(BlockFace.NORTH_EAST).getType(),
				below.getRelative(BlockFace.EAST).getType(),
				below.getRelative(BlockFace.SOUTH_EAST).getType(),
				below.getRelative(BlockFace.SOUTH).getType(),
				below.getRelative(BlockFace.SOUTH_WEST).getType(),
				below.getRelative(BlockFace.WEST).getType(),
				below.getRelative(BlockFace.NORTH_WEST).getType(),
				head.getRelative(BlockFace.NORTH).getType(),
				head.getRelative(BlockFace.NORTH_EAST).getType(),
				head.getRelative(BlockFace.EAST).getType(),
				head.getRelative(BlockFace.SOUTH_EAST).getType(),
				head.getRelative(BlockFace.SOUTH).getType(),
				head.getRelative(BlockFace.SOUTH_WEST).getType(),
				head.getRelative(BlockFace.WEST).getType(),
				head.getRelative(BlockFace.NORTH_WEST).getType(),
				above.getRelative(BlockFace.NORTH).getType(),
				above.getRelative(BlockFace.NORTH_EAST).getType(),
				above.getRelative(BlockFace.EAST).getType(),
				above.getRelative(BlockFace.SOUTH_EAST).getType(),
				above.getRelative(BlockFace.SOUTH).getType(),
				above.getRelative(BlockFace.SOUTH_WEST).getType(),
				above.getRelative(BlockFace.WEST).getType(),
				above.getRelative(BlockFace.NORTH_WEST).getType()
				));
		for(Material material : around) {
			if(cannotSpawnBeside.contains(material)) {
				return false;
			}
		}
		return true;
	}
	
	
	// CONFIG
	public boolean getBooleanSetting(String path) { // Handles strings as booleans
		if(config.isSet(path)) {
			if(config.isString(path)) {
				return config.getString(path).equalsIgnoreCase("true");
			}			
			return config.getBoolean(path);
		}
		boolean result = false;
		int priority = Integer.MIN_VALUE;
		boolean found = false;
		for(RegionGroup group : plugin.getFileManager().getGroups()) {
			if(group.isMember(this) && group.getSettings().isSet(path) && group.getPriority() > priority) {
				if(group.getSettings().isString(path)) {
					result = group.getSettings().getString(path).equalsIgnoreCase("true");
				} else {
					result = group.getSettings().getBoolean(path);
				}
				priority = group.getPriority();
				found = true;
			}
		}
		if(found) {
			return result;
		}
		
		if(this.getFileManager().getDefaultSettings().isString(path)) {
			return this.getFileManager().getDefaultSettings().getString(path).equalsIgnoreCase("true");
		}
		return this.getFileManager().getDefaultSettings().getBoolean(path);
	}
	
	public int getIntegerSetting(String path) {
		if(config.isSet(path)) {
			return config.getInt(path);
		}
		int result = 0;
		int priority = Integer.MIN_VALUE;
		boolean found = false;
		for(RegionGroup group : plugin.getFileManager().getGroups()) {
			if(group.isMember(this) && group.getSettings().isSet(path) && group.getPriority() > priority) {
				result = group.getSettings().getInt(path);
				priority = group.getPriority();
				found = true;
			}
		}
		if(found) {
			return result;
		}
		return this.getFileManager().getDefaultSettings().getInt(path);
	}
	
	public double getDoubleSetting(String path) {
		if(config.isSet(path)) {
			return config.getDouble(path);
		}
		double result = 0;
		int priority = Integer.MIN_VALUE;
		boolean found = false;
		for(RegionGroup group : plugin.getFileManager().getGroups()) {
			if(group.isMember(this) && group.getSettings().isSet(path) && group.getPriority() > priority) {
				result = group.getSettings().getDouble(path);
				priority = group.getPriority();
				found = true;
			}
		}
		if(found) {
			return result;
		}
		return this.getFileManager().getDefaultSettings().getDouble(path);
	}
	
	public long getLongSetting(String path) {
		if(config.isSet(path)) {
			return config.getLong(path);
		}
		long result = 0;
		int priority = Integer.MIN_VALUE;
		boolean found = false;
		for(RegionGroup group : plugin.getFileManager().getGroups()) {
			if(group.isMember(this) && group.getSettings().isSet(path) && group.getPriority() > priority) {
				result = group.getSettings().getLong(path);
				priority = group.getPriority();
				found = true;
			}
		}
		if(found) {
			return result;
		}
		return this.getFileManager().getDefaultSettings().getLong(path);
	}
	
	public String getStringSetting(String path) {
		if(config.isSet(path)) {
			return config.getString(path);
		}
		String result = null;
		int priority = Integer.MIN_VALUE;
		boolean found = false;
		for(RegionGroup group : plugin.getFileManager().getGroups()) {
			if(group.isMember(this) && group.getSettings().isSet(path) && group.getPriority() > priority) {
				result = group.getSettings().getString(path);
				priority = group.getPriority();
				found = true;
			}
		}
		if(found) {
			return result;
		}
		return this.getFileManager().getDefaultSettings().getString(path);
	}
	
	public List<String> getStringListSetting(String path) {
		if(config.isSet(path)) {
			return config.getStringList(path);
		}
		List<String> result = null;
		int priority = Integer.MIN_VALUE;
		boolean found = false;
		for(RegionGroup group : plugin.getFileManager().getGroups()) {
			if(group.isMember(this) && group.getSettings().isSet(path) && group.getPriority() > priority) {
				result = group.getSettings().getStringList(path);
				priority = group.getPriority();
				found = true;
			}
		}
		if(found) {
			return result;
		}
		return this.getFileManager().getDefaultSettings().getStringList(path);
	}
	
	public void setSetting(String path, Object value) {
		config.set(path, value);
		this.saveRequired();
	}
	

	
	// LIMIT FUNCTIONS
	/**
	 * Check if the player can buy/rent this region, detailed info in the result object
	 * @param type The type of region to check
	 * @param player The player to check it for
	 * @return LimitResult containing if it is allowed, why and limiting factor
	 */
	public LimitResult limitsAllow(RegionType type, Player player) {
		return limitsAllow(type, player, false);
	}

	/**
	 * Check if the player can buy/rent this region, detailed info in the result object
	 * @param type The type of region to check
	 * @param player The player to check it for
	 * @param extend Check for extending of rental regions
	 * @return LimitResult containing if it is allowed, why and limiting factor
	 */
	public LimitResult limitsAllow(RegionType type, Player player, boolean extend) {
		if(player.hasPermission("areashop.limitbypass")) {
			return new LimitResult(true, null, 0, 0, null);
		}
		GeneralRegion exclude = null;
		if(extend) {
			exclude = this;
		}
		String typePath;
		if(type == RegionType.RENT) {
			typePath = "rents";
		} else {
			typePath = "buys";
		}
		// Check all limitgroups the player has
		List<String> groups = new ArrayList<>(plugin.getConfig().getConfigurationSection("limitGroups").getKeys(false));
		while(!groups.isEmpty()) {
			String group = groups.get(0);
			if(player.hasPermission("areashop.limits." + group) && this.matchesLimitGroup(group)) {
				int totalLimit = plugin.getConfig().getInt("limitGroups." + group + ".total");
				int typeLimit = plugin.getConfig().getInt("limitGroups." + group + "."+typePath);
				//AreaShop.debug("typeLimitOther="+typeLimit+", typePath="+typePath);
				int totalCurrent = hasRegionsInLimitGroup(player, group, plugin.getFileManager().getRegions(), exclude);
				int typeCurrent;
				if(type == RegionType.RENT) {
					typeCurrent = hasRegionsInLimitGroup(player, group, plugin.getFileManager().getRents(), exclude);
				} else {
					typeCurrent = hasRegionsInLimitGroup(player, group, plugin.getFileManager().getBuys(), exclude);
				}
				if(totalLimit == -1) {
					totalLimit = Integer.MAX_VALUE;
				}
				if(typeLimit == -1) {
					typeLimit = Integer.MAX_VALUE;
				}
				String totalHighestGroup = group;
				String typeHighestGroup = group;
				groups.remove(group);
				// Get the highest number from the groups of the same category
				List<String> groupsCopy = new ArrayList<>(groups);
				for(String checkGroup : groupsCopy) {
					if(player.hasPermission("areashop.limits." + checkGroup) && this.matchesLimitGroup(checkGroup)) {
						if(limitGroupsOfSameCategory(group, checkGroup)) {
							groups.remove(checkGroup);
							int totalLimitOther = plugin.getConfig().getInt("limitGroups." + checkGroup + ".total");
							int typeLimitOther = plugin.getConfig().getInt("limitGroups." + checkGroup + "."+typePath);
							if(totalLimitOther > totalLimit) {
								totalLimit = totalLimitOther; 
								totalHighestGroup = checkGroup;
							} else if(totalLimitOther == -1) {
								totalLimit = Integer.MAX_VALUE;
							}
							if(typeLimitOther > typeLimit) {
								typeLimit = typeLimitOther;
								typeHighestGroup = checkGroup;
							} else if(typeLimitOther == -1) {
								typeLimit = Integer.MAX_VALUE;
							}
						}
					} else {
						groups.remove(checkGroup);
					}
				}
				// Check if the limits stop the player from buying the region
				if(typeCurrent >= typeLimit) {
					LimitType limitType;
					if(type == RegionType.RENT) {
						if(extend) {
							limitType = LimitType.EXTEND;
						} else {
							limitType = LimitType.RENTS;
						}
					} else {
						limitType = LimitType.BUYS;
					}
					return new LimitResult(false, limitType, typeLimit, typeCurrent, typeHighestGroup);					
				}
				if(totalCurrent >= totalLimit) {
					return new LimitResult(false, LimitType.TOTAL, totalLimit, totalCurrent, totalHighestGroup);					
				}		
			}
			groups.remove(group);
		}		
		return new LimitResult(true, null, 0, 0, null);
	}
	
	/**
	 * Class to store the result of a limits check
	 */
	public class LimitResult {
		private boolean actionAllowed;
		private LimitType limitingFactor;
		private int maximum;
		private int current;
		private String limitingGroup;
		public LimitResult(boolean actionAllowed, LimitType limitingFactor, int maximum, int current, String limitingGroup) {
			this.actionAllowed = actionAllowed;
			this.limitingFactor = limitingFactor;
			this.maximum = maximum;
			this.current = current;
			this.limitingGroup = limitingGroup;
		}
		public boolean actionAllowed() {
			return actionAllowed;
		}		
		public LimitType getLimitingFactor() {
			return limitingFactor;
		}
		public int getMaximum() {
			return maximum;
		}
		public int getCurrent() {
			return current;
		}
		public String getLimitingGroup() {
			return limitingGroup;
		}
		
		@Override
		public String toString() {
			return "actionAllowed=" + actionAllowed + ", limitingFactor=" + limitingFactor + ", maximum=" + maximum + ", current=" + current + ", limitingGroup=" + limitingGroup;
		}
	}
	
	/**
	 * Checks if two limitGroups are of the same category (same groups and worlds lists)
	 * @param firstGroup The first group
	 * @param secondGroup The second group
	 * @return true if the groups and worlds lists are the same, otherwise false
	 */
	private boolean limitGroupsOfSameCategory(String firstGroup, String secondGroup) {
		List<String> firstGroups = plugin.getConfig().getStringList("limitGroups." + firstGroup + ".groups");
		List<String> secondGroups = plugin.getConfig().getStringList("limitGroups." + secondGroup + ".groups");
		if(!firstGroups.containsAll(secondGroups) || !secondGroups.containsAll(firstGroups)) {
			return false;
		}
		List<String> firstWorlds = plugin.getConfig().getStringList("limitGroups." + firstGroup + ".worlds");
		List<String> secondWorlds = plugin.getConfig().getStringList("limitGroups." + secondGroup + ".worlds");
		return !(!firstWorlds.containsAll(secondWorlds) || !secondWorlds.containsAll(firstWorlds));
	}
	
	/**
	 * Get the amount of regions a player has matching a certain limits group (config.yml -- limitGroups)
	 * @param player The player to check the amount for
	 * @param limitGroup The group to check
	 * @param regions All the regions a player has bought or rented
	 * @param exclude Exclude this region from the count
	 * @return The number of regions that the player has bought or rented matching the limit group (worlds and groups filters)
	 */
	public int hasRegionsInLimitGroup(Player player, String limitGroup, List<? extends GeneralRegion> regions, GeneralRegion exclude) {
		int result = 0;
		for(GeneralRegion region : regions) {
			if(region.isOwner(player) && region.matchesLimitGroup(limitGroup) && (exclude == null || !exclude.getName().equals(region.getName()))) {
				result++;
			}
		}
		return result;
	}
	
	/**
	 * Check if this region matches the filters of a limit group
	 * @param group The group to check
	 * @return true if the region applies to the limit group, otherwise false
	 */
	public boolean matchesLimitGroup(String group) {
		List<String> worlds = plugin.getConfig().getStringList("limitGroups." + group + ".worlds");
		List<String> groups = plugin.getConfig().getStringList("limitGroups." + group + ".groups");
		if((worlds == null || worlds.isEmpty() || worlds.contains(getWorldName()))) {
			if(groups == null || groups.isEmpty()) {
				return true;
			} else {
				boolean inGroups = false;
				for(RegionGroup checkGroup : plugin.getFileManager().getGroups()) {
					inGroups = inGroups || (groups.contains(checkGroup.getName()) && checkGroup.isMember(this));
				}
				return inGroups;					
			}
		}
		return false;
	}
	
	
	
	/**
	 * Checks an event and handles saving to and restoring from schematic for it
	 * @param type The type of event
	 */
	public void handleSchematicEvent(RegionEvent type) {
		// Check the individual>group>default setting
		if(!isRestoreEnabled()) {
			AreaShop.debug("Schematic operations for " + getName() + " not enabled, skipped"); 
			return;
		}
		// Get the safe and restore names		
		String save = plugin.getConfig().getString("schematicProfiles." + getRestoreProfile() + "." + type.getValue() + ".save");
		String restore = plugin.getConfig().getString("schematicProfiles." + getRestoreProfile() + "." + type.getValue() + ".restore");
		// Save the region if needed
		if(save != null && save.length() != 0) {
			save = applyAllReplacements(save);
			this.saveRegionBlocks(save);			
		}
		// Restore the region if needed
		if(restore != null && restore.length() != 0) {
			restore = applyAllReplacements(restore);
			this.restoreRegionBlocks(restore);		
		}
	}
	
	// COMMAND EXECUTING	
	/**
	 * Run commands as the CommandsSender, replacing all tags with the relevant values
	 * @param sender The sender that should perform the command
	 * @param commands A list of the commands to run (without slash and with tags)
	 */
	public void runCommands(CommandSender sender, List<String> commands) {
		if(commands == null || commands.isEmpty()) {
			return;
		}
		
		for(String command : commands) {
			if(command == null || command.length() == 0) {
				continue;
			}			
			command = applyAllReplacements(command);

			boolean result;
			String error = null;
			String stacktrace = null;
			try {
				result = plugin.getServer().dispatchCommand(sender, command);
			} catch(CommandException e) {
				result = false;
				error = e.getMessage();
				stacktrace = ExceptionUtils.getStackTrace(e);
			}
			boolean printed = false;
			if(!result) {
				printed = true;
				if(error != null) {
					plugin.getLogger().warning("Command execution failed, command=" + command + ", error=" + error + ", stacktrace:");
					plugin.getLogger().warning(stacktrace);
					plugin.getLogger().warning("--- End of stacktrace ---");
				} else {
					plugin.getLogger().warning("Command execution failed, command=" + command);
				}
			}
			if(!printed) {
				if(error == null) {
					AreaShop.debug("Command run, executor=" + sender.getName() + ", command=" + command + ", result=" + result);
				} else {
					AreaShop.debug("Command run, executor=" + sender.getName() + ", command=" + command + ", error=" + error + ", stacktrace:");
					AreaShop.debug(stacktrace);
					AreaShop.debug("--- End of stacktrace ---");
				}
			}
		}
	}
	
	/**
	 * Run command for a certain event
	 * @param event The event
	 * @param before The 'before' or 'after' commands
	 */
	public void runEventCommands(RegionEvent event, boolean before) {
		String profile = getStringSetting("general.eventCommandProfile");
		if(profile == null || profile.length() == 0) {
			return;
		}
		String path = "eventCommandProfiles." + profile + "." + event.getValue().toLowerCase();
		if(before) {
			path += ".before";
		} else {
			path += ".after";
		}
		List<String> commands = plugin.getConfig().getStringList(path);
		// Don't waste time if there are no commands to be run
		if(commands == null || commands.isEmpty()) {
			return;
		}		
		runCommands(Bukkit.getConsoleSender(), commands);
	}
	
	/**
	 * Run commands when a player clicks a sign
	 * @param signName The name of the sign
	 * @param clicker The player that clicked the sign
	 * @param clickType The type of clicking
	 * @return true if the commands ran successfully, false if any of them failed
	 */
	public boolean runSignCommands(String signName, Player clicker, ClickType clickType) {
		// Get the profile set in the config
		String profile = config.getString("general.signs." + signName + ".profile");
		if(profile == null || profile.length() == 0) {
			profile = getStringSetting("general.signProfile");
		}

		// Get paths (state may change after running them)
		String playerPath = "signProfiles." + profile + "." + getState().getValue().toLowerCase() + "." + clickType.getValue() + "Player";
		String consolePath = "signProfiles."+profile+"."+getState().getValue().toLowerCase()+"."+clickType.getValue()+"Console";

		// Run player commands if specified
		List<String> playerCommands = new ArrayList<>();
		for(String command : plugin.getConfig().getStringList(playerPath)) {
			playerCommands.add(command.replace(AreaShop.tagClicker, clicker.getName()));
		}
		runCommands(clicker, playerCommands);

		// Run console commands if specified
		List<String> consoleCommands = new ArrayList<>();
		for(String command : plugin.getConfig().getStringList(consolePath)) {
			consoleCommands.add(command.replace(AreaShop.tagClicker, clicker.getName()));
		}
		runCommands(Bukkit.getConsoleSender(), consoleCommands);		
		
		return !playerCommands.isEmpty() || !consoleCommands.isEmpty();
	}
}






















