package me.wiefferink.areashop.regions;

import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.events.NotifyRegionEvent;
import me.wiefferink.areashop.events.notify.UpdateRegionEvent;
import me.wiefferink.areashop.features.FriendsFeature;
import me.wiefferink.areashop.features.RegionFeature;
import me.wiefferink.areashop.features.TeleportFeature;
import me.wiefferink.areashop.features.signs.SignsFeature;
import me.wiefferink.areashop.interfaces.GeneralRegionInterface;
import me.wiefferink.areashop.managers.FileManager;
import me.wiefferink.areashop.tools.Utils;
import me.wiefferink.bukkitdo.Do;
import me.wiefferink.interactivemessenger.processing.Message;
import me.wiefferink.interactivemessenger.processing.ReplacementProvider;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class GeneralRegion implements GeneralRegionInterface, Comparable<GeneralRegion>, ReplacementProvider {
	static final AreaShop plugin = AreaShop.getInstance();

	final YamlConfiguration config;
	private boolean saveRequired = false;
	private boolean deleted = false;
	private long volume = -1;

	private Map<Class<? extends RegionFeature>, RegionFeature> features;

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

	/**
	 * Constructor, used to restore regions from disk at startup.
	 * @param config The configuration of the region
	 */
	public GeneralRegion(YamlConfiguration config) {
		this.config = config;
		setup();
	}

	/**
	 * Constructor, used for adding new regions.
	 * @param name  Name of the WorldGuard region that this region is attached to
	 * @param world The world of the WorldGuard region
	 */
	public GeneralRegion(String name, World world) {
		config = new YamlConfiguration();
		setSetting("general.name", name);
		setSetting("general.world", world.getName());
		setSetting("general.type", getType().getValue().toLowerCase());
		setup();
	}

	/**
	 * Shared setup of all constructors.
	 */
	public void setup() {
		features = new HashMap<>();
	}

	/**
	 * Deregister everything.
	 */
	public void destroy() {
		for(RegionFeature feature : features.values()) {
			feature.shutdown();
		}
	}

	/**
	 * Get a feature of this region.
	 * @param clazz The class of the feature to get
	 * @param <T> The feature to get
	 * @return The feature (either just instantiated or cached)
	 */
	public <T extends RegionFeature> T getFeature(Class<T> clazz) {
		RegionFeature result = features.get(clazz);
		if(result == null) {
			result = plugin.getFeatureManager().getRegionFeature(this, clazz);
			features.put(clazz, result);
		}
		return clazz.cast(result);
	}

	/**
	 * Get the friends feature to query and manipulate friends of this region.
	 * @return The FriendsFeature of this region
	 */
	public FriendsFeature getFriendsFeature() {
		return getFeature(FriendsFeature.class);
	}

	/**
	 * Get the signs feature to manipulate and update signs.
	 * @return The SignsFeature of this region
	 */
	public SignsFeature getSignsFeature() {
		return getFeature(SignsFeature.class);
	}

	/**
	 * Get the teleport feature to teleport players to the region and signs.
	 * @return The TeleportFeature
	 */
	public TeleportFeature getTeleportFeature() {
		return getFeature(TeleportFeature.class);
	}

	// ABSTRACT

	/**
	 * Get the region type of the region.
	 * @return The RegionType of this region
	 */
	public abstract RegionType getType();

	/**
	 * Get the region availability.
	 * @return true/false if region cant be rented or sell
	 */
	public abstract boolean isAvailable();

	// Sorting by name

	/**
	 * Compare this region to another region by name.
	 * @param o The region to compare to
	 * @return 0 if the names are the same, below zero if this region is earlier in the alphabet, otherwise above zero
	 */
	@Override
	public int compareTo(GeneralRegion o) {
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

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	/**
	 * Get the config file that is used to store the region information.
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
		Bukkit.getServer().getPluginManager().callEvent(new UpdateRegionEvent(this));
	}

	/**
	 * Broadcast the given event and update the region status.
	 * @param event The update event that should be broadcasted
	 */
	public void notifyAndUpdate(NotifyRegionEvent event) {
		Bukkit.getPluginManager().callEvent(event);
		update();
	}

	/**
	 * Get the state of a region.
	 * @return The RegionState of the region
	 */
	public abstract RegionState getState();

	// GETTERS

	/**
	 * Check if the region has been deleted.
	 * @return true if the region has been deleted, otherwise false
	 */
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * Indicate that this region has been deleted.
	 */
	public void setDeleted() {
		deleted = true;
	}

	/**
	 * Get the name of the region.
	 * @return The region name
	 */
	@Override
	public String getName() {
		return config.getString("general.name");
	}

	/**
	 * Get the lowercase region name.
	 * @return The region name in lowercase
	 */
	public String getLowerCaseName() {
		return getName().toLowerCase();
	}

	/**
	 * Check if restoring is enabled.
	 * @return true if restoring is enabled, otherwise false
	 */
	public boolean isRestoreEnabled() {
		return getBooleanSetting("general.enableRestore");
	}

	/**
	 * Get the time that the player was last active.
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
	 * Set the last active time of the player to the current time.
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
	 * Get the World of the region.
	 * @return The World where the region is located
	 */
	@Override
	public World getWorld() {
		return Bukkit.getWorld(getWorldName());
	}

	/**
	 * Get the name of the world where the region is located.
	 * @return The name of the world of the region
	 */
	@Override
	public String getWorldName() {
		return getStringSetting("general.world");
	}

	/**
	 * Get the FileManager from the plugin.
	 * @return The FileManager (responsible for saving/loading regions and getting them)
	 */
	public FileManager getFileManager() {
		return plugin.getFileManager();
	}

	/**
	 * Check if the players is owner of this region.
	 * @param player Player to check ownership for
	 * @return true if the player currently rents or buys this region
	 */
	public boolean isOwner(OfflinePlayer player) {
		return isOwner(player.getUniqueId());
	}

	/**
	 * Check if the players is owner of this region.
	 * @param player Player to check ownership for
	 * @return true if the player currently rents or buys this region
	 */
	public boolean isOwner(UUID player) {
		return (this instanceof RentRegion && ((RentRegion)this).isRenter(player)) || (this instanceof BuyRegion && ((BuyRegion)this).isBuyer(player));
	}

	/**
	 * Get the player that is currently the owner of this region (either bought or rented it).
	 * @return The UUID of the owner of this region
	 */
	public UUID getOwner() {
		if(this instanceof RentRegion) {
			return ((RentRegion)this).getRenter();
		} else {
			return ((BuyRegion)this).getBuyer();
		}
	}

	/**
	 * Change the owner of the region.
	 * @param player The player that should be the owner
	 */
	public void setOwner(UUID player) {
		if(this instanceof RentRegion) {
			((RentRegion)this).setRenter(player);
		} else {
			((BuyRegion)this).setBuyer(player);
		}
	}

	/**
	 * Get the landlord of this region (the player that receives any revenue from this region).
	 * @return The UUID of the landlord of this region
	 */
	public UUID getLandlord() {
		String landlord = getStringSetting("general.landlord");
		if(landlord != null && !landlord.isEmpty()) {
			try {
				return UUID.fromString(landlord);
			} catch(IllegalArgumentException e) {
				// Incorrect UUID
			}
		}
		String landlordName = getStringSetting("general.landlordName");
		if(landlordName != null && !landlordName.isEmpty()) {
			@SuppressWarnings("deprecation")
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(landlordName);
			if(offlinePlayer != null) {
				return offlinePlayer.getUniqueId();
			}
		}
		return null;
	}

	/**
	 * Get the name of the landlord.
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
	 * Set the landlord of this region (the player that receives all revenue of this region).
	 * @param landlord The UUID of the player that should be set as landlord
	 * @param name     The backup name of the player (for in case that the UUID cannot be resolved to a playername)
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
	 * Remove the landlord from this region.
	 */
	public void removelandlord() {
		setSetting("general.landlord", null);
		setSetting("general.landlordName", null);
	}

	/**
	 * Check if the specified player is the landlord of this region.
	 * @param landlord The UUID of the players to check for landlord
	 * @return true if the player is the landlord, otherwise false
	 */
	public boolean isLandlord(UUID landlord) {
		return landlord != null && getLandlord() != null && getLandlord().equals(landlord);
	}

	/**
	 * Get the WorldGuard region associated with this AreaShop region.
	 * @return The ProtectedRegion of WorldGuard or null if the region does not exist anymore
	 */
	@Override
	public ProtectedRegion getRegion() {
		if(getWorld() == null
				|| plugin.getWorldGuard() == null
				|| plugin.getRegionManager(getWorld()) == null
				|| plugin.getRegionManager(getWorld()).getRegion(getName()) == null) {
			return null;
		}
		return plugin.getRegionManager(getWorld()).getRegion(getName());
	}

	/**
	 * Get the minimum corner of the region.
	 * @return Vector
	 */
	public Vector getMinimumPoint() {
		return plugin.getWorldGuardHandler().getMinimumPoint(getRegion());
	}

	/**
	 * Get the maximum corner of the region.
	 * @return Vector
	 */
	public Vector getMaximumPoint() {
		return plugin.getWorldGuardHandler().getMaximumPoint(getRegion());
	}

	/**
	 * Get the width of the region (x-axis).
	 * @return The width of the region (x-axis)
	 */
	@Override
	public int getWidth() {
		if(getRegion() == null) {
			return 0;
		}
		return getMaximumPoint().getBlockX() - getMinimumPoint().getBlockX() + 1;
	}

	/**
	 * Get the depth of the region (z-axis).
	 * @return The depth of the region (z-axis)
	 */
	@Override
	public int getDepth() {
		if(getRegion() == null) {
			return 0;
		}
		return getMaximumPoint().getBlockZ() - getMinimumPoint().getBlockZ() + 1;
	}

	/**
	 * Get the height of the region (y-axis).
	 * @return The height of the region (y-axis)
	 */
	@Override
	public int getHeight() {
		if(getRegion() == null) {
			return 0;
		}
		return getMaximumPoint().getBlockY() - getMinimumPoint().getBlockY() + 1;
	}

	/**
	 * Get the groups that this region is added to.
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
	 * Get a list of names from groups this region is in.
	 * @return A list of groups this region is part of
	 */
	public List<String> getGroupNames() {
		List<String> result = new ArrayList<>();
		for(RegionGroup group : getGroups()) {
			result.add(group.getName());
		}
		return result;
	}

	@Override
	public Object provideReplacement(String variable) {
		switch(variable) {

			// Basics
			case AreaShop.tagRegionName:
				return getName();
			case AreaShop.tagRegionType:
				return getType().getValue().toLowerCase();
			case AreaShop.tagWorldName:
				return getWorldName();
			case AreaShop.tagWidth:
				return getWidth();
			case AreaShop.tagDepth:
				return getDepth();
			case AreaShop.tagHeight:
				return getHeight();
			case AreaShop.tagFriends:
				return Utils.createCommaSeparatedList(getFriendsFeature().getFriendNames());
			case AreaShop.tagFriendsUUID:
				return Utils.createCommaSeparatedList(getFriendsFeature().getFriends());
			case AreaShop.tagLandlord:
				return getLandlordName();
			case AreaShop.tagLandlordUUID:
				return getLandlord();
			case AreaShop.tagVolume:
				return getVolume();

			// Date/time
			case AreaShop.tagEpoch:
				return Calendar.getInstance().getTimeInMillis();
			case AreaShop.tagMillisecond:
				return Calendar.getInstance().get(Calendar.MILLISECOND);
			case AreaShop.tagSecond:
				return Calendar.getInstance().get(Calendar.SECOND);
			case AreaShop.tagMinute:
				return Calendar.getInstance().get(Calendar.MINUTE);
			case AreaShop.tagHour:
				return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
			case AreaShop.tagDay:
				return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
			case AreaShop.tagMonth:
				return Calendar.getInstance().get(Calendar.MONTH) + 1;
			case AreaShop.tagYear:
				return Calendar.getInstance().get(Calendar.YEAR);
			case AreaShop.tagDateTime:
				return new SimpleDateFormat(plugin.getConfig().getString("timeFormatChat")).format(Calendar.getInstance().getTime());
			case AreaShop.tagDateTimeShort:
				return new SimpleDateFormat(plugin.getConfig().getString("timeFormatSign")).format(Calendar.getInstance().getTime());

			// Teleport locations
			default:
				Location tp = getTeleportFeature().getTeleportLocation();
				if(tp == null) {
					return null;
				}
				switch(variable) {
					case AreaShop.tagTeleportBlockX:
						return tp.getBlockX();
					case AreaShop.tagTeleportBlockY:
						return tp.getBlockY();
					case AreaShop.tagTeleportBlockZ:
						return tp.getBlockZ();
					case AreaShop.tagTeleportX:
						return tp.getX();
					case AreaShop.tagTeleportY:
						return tp.getY();
					case AreaShop.tagTeleportZ:
						return tp.getZ();
					case AreaShop.tagTeleportPitch:
						return tp.getPitch();
					case AreaShop.tagTeleportYaw:
						return tp.getYaw();
					case AreaShop.tagTeleportPitchRound:
						return Math.round(tp.getPitch());
					case AreaShop.tagTeleportYawRound:
						return Math.round(tp.getYaw());
					case AreaShop.tagTeleportWorld:
						return tp.getWorld().getName();
					default:
						return null;
				}
		}
	}

	/**
	 * Check if for renting this region you should be inside of it.
	 * @return true if you need to be inside, otherwise false
	 */
	public boolean restrictedToRegion() {
		return getBooleanSetting("general.restrictedToRegion");
	}

	/**
	 * Check if for renting you need to be in the correct world.
	 * @return true if you need to be in the same world as the region, otherwise false
	 */
	public boolean restrictedToWorld() {
		return getBooleanSetting("general.restrictedToWorld") || restrictedToRegion();
	}

	/**
	 * Check now if the player has been inactive for too long, unrent/sell will happen when true.
	 * @return true if the region has been unrented/sold, otherwise false
	 */
	public abstract boolean checkInactive();

	/**
	 * Method to send a message to a CommandSender, using chatprefix if it is a player.
	 * Automatically includes the region in the message, enabling the use of all variables.
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
	 * Check if a sign needs periodic updating.
	 * @return true if the signs of this region need periodic updating, otherwise false
	 */
	public boolean needsPeriodicUpdate() {
		return !(isDeleted() || !(this instanceof RentRegion)) && getSignsFeature().needsPeriodicUpdate();
	}

	/**
	 * Change the restore setting.
	 * @param restore true, false or general
	 */
	public void setRestoreSetting(Boolean restore) {
		setSetting("general.enableRestore", restore);
	}

	/**
	 * Change the restore profile.
	 * @param profile default or the name of the profile as set in the config
	 */
	public void setSchematicProfile(String profile) {
		setSetting("general.schematicProfile", profile);
	}

	/**
	 * Save all blocks in a region for restoring later.
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
		File saveFile = new File(plugin.getFileManager().getSchematicFolder() + File.separator + fileName);
		// Create parent directories
		File parent = saveFile.getParentFile();
		if(parent != null && !parent.exists()) {
			if(!parent.mkdirs()) {
				AreaShop.warn("Did not save region " + getName() + ", schematic directory could not be created: " + saveFile.getAbsolutePath());
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
	 * Restore all blocks in a region for restoring later.
	 * @param fileName The name of the file to save to (extension and folder will be added)
	 * @return true if the region has been restored properly, otherwise false
	 */
	public boolean restoreRegionBlocks(String fileName) {
		if(getRegion() == null) {
			AreaShop.debug("Region '" + getName() + "' does not exist in WorldGuard, restore failed");
			return false;
		}
		// The path to save the schematic
		File restoreFile = new File(plugin.getFileManager().getSchematicFolder() + File.separator + fileName);
		boolean result = plugin.getWorldEditHandler().restoreRegionBlocks(restoreFile, this);
		if(result) {
			AreaShop.debug("Restored schematic for region " + getName());

			// Workaround for signs inside the region in combination with async restore of plugins like AsyncWorldEdit and FastAsyncWorldEdit
			Do.syncLater(10, getSignsFeature()::update);
		}
		return result;
	}

	/**
	 * Reset all flags of the region.
	 */
	public void resetRegionFlags() {
		ProtectedRegion region = getRegion();
		if(region != null) {
			region.setFlag(plugin.getWorldGuardHandler().fuzzyMatchFlag("greeting"), null);
			region.setFlag(plugin.getWorldGuardHandler().fuzzyMatchFlag("farewell"), null);
		}
	}

	/**
	 * Indicate this region needs to be saved, saving will happen by a repeating task.
	 */
	public void saveRequired() {
		saveRequired = true;
	}

	/**
	 * Check if a save is required.
	 * @return true if a save is required because some data changed, otherwise false
	 */
	public boolean isSaveRequired() {
		return saveRequired && !isDeleted();
	}

	/**
	 * Save this region to disk now, using this method could slow down the plugin, normally saveRequired() should be used.
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
		} catch(IOException e) {
			return false;
		}
	}


	// CONFIG

	/**
	 * Get a boolean setting for this region, defined as follows
	 * - If the region has the setting in its own file (/regions/regionName.yml), use that
	 * - If the region has groups, use the setting defined by the most important group, if any
	 * - Otherwise fallback to the default.yml file setting
	 * @param path The path to get the setting of
	 * @return The value of the setting (strings are handled as booleans)
	 */
	public boolean getBooleanSetting(String path) {
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

		if(this.getFileManager().getRegionSettings().isString(path)) {
			return this.getFileManager().getRegionSettings().getString(path).equalsIgnoreCase("true");
		}
		if(this.getFileManager().getRegionSettings().isSet(path)) {
			return this.getFileManager().getRegionSettings().getBoolean(path);
		} else {
			return this.getFileManager().getFallbackRegionSettings().getBoolean(path);
		}
	}

	/**
	 * Get a boolean setting for this region, defined as follows
	 * - If the region has the setting in its own file (/regions/regionName.yml), use that
	 * - If the region has groups, use the setting defined by the most important group, if any
	 * - Otherwise fallback to the default.yml file setting
	 * @param path The path to get the setting of
	 * @return The value of the setting (strings are handled as booleans)
	 */
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

		if(this.getFileManager().getRegionSettings().isSet(path)) {
			return this.getFileManager().getRegionSettings().getInt(path);
		} else {
			return this.getFileManager().getFallbackRegionSettings().getInt(path);
		}
	}

	/**
	 * Get a double setting for this region, defined as follows
	 * - If the region has the setting in its own file (/regions/regionName.yml), use that
	 * - If the region has groups, use the setting defined by the most important group, if any
	 * - Otherwise fallback to the default.yml file setting
	 * @param path The path to get the setting of
	 * @return The value of the setting
	 */
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

		if(this.getFileManager().getRegionSettings().isSet(path)) {
			return this.getFileManager().getRegionSettings().getDouble(path);
		} else {
			return this.getFileManager().getFallbackRegionSettings().getDouble(path);
		}
	}

	/**
	 * Get a long setting for this region, defined as follows
	 * - If the region has the setting in its own file (/regions/regionName.yml), use that
	 * - If the region has groups, use the setting defined by the most important group, if any
	 * - Otherwise fallback to the default.yml file setting
	 * @param path The path to get the setting of
	 * @return The value of the setting
	 */
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

		if(this.getFileManager().getRegionSettings().isSet(path)) {
			return this.getFileManager().getRegionSettings().getLong(path);
		} else {
			return this.getFileManager().getFallbackRegionSettings().getLong(path);
		}
	}

	/**
	 * Get a string setting for this region, defined as follows
	 * - If the region has the setting in its own file (/regions/regionName.yml), use that
	 * - If the region has groups, use the setting defined by the most important group, if any
	 * - Otherwise fallback to the default.yml file setting
	 * @param path The path to get the setting of
	 * @return The value of the setting
	 */
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

		if(this.getFileManager().getRegionSettings().isSet(path)) {
			return this.getFileManager().getRegionSettings().getString(path);
		} else {
			return this.getFileManager().getFallbackRegionSettings().getString(path);
		}
	}

	/**
	 * Get a string list setting for this region, defined as follows
	 * - If the region has the setting in its own file (/regions/regionName.yml), use that
	 * - If the region has groups, use the setting defined by the most important group, if any
	 * - Otherwise fallback to the default.yml file setting
	 * @param path The path to get the setting of
	 * @return The value of the setting
	 */
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

		if(this.getFileManager().getRegionSettings().isSet(path)) {
			return this.getFileManager().getRegionSettings().getStringList(path);
		} else {
			return this.getFileManager().getFallbackRegionSettings().getStringList(path);
		}
	}

	/**
	 * Get a configuration section setting for this region, defined as follows
	 * - If the region has the setting in its own file (/regions/regionName.yml), use that
	 * - If the region has groups, use the setting defined by the most important group, if any
	 * - Otherwise fallback to the default.yml file setting
	 * @param path The path to get the setting of
	 * @return The value of the setting
	 */
	public ConfigurationSection getConfigurationSectionSetting(String path) {
		if(config.isSet(path)) {
			return config.getConfigurationSection(path);
		}
		ConfigurationSection result = null;
		int priority = Integer.MIN_VALUE;
		boolean found = false;
		for(RegionGroup group : plugin.getFileManager().getGroups()) {
			if(group.isMember(this) && group.getSettings().isSet(path) && group.getPriority() > priority) {
				result = group.getSettings().getConfigurationSection(path);
				priority = group.getPriority();
				found = true;
			}
		}
		if(found) {
			return result;
		}

		if(this.getFileManager().getRegionSettings().isSet(path)) {
			return this.getFileManager().getRegionSettings().getConfigurationSection(path);
		} else {
			return this.getFileManager().getFallbackRegionSettings().getConfigurationSection(path);
		}
	}

	/**
	 * Get a configuration section setting for this region, defined as follows
	 * - If the region has the setting in its own file (/regions/regionName.yml), use that
	 * - If the region has groups, use the setting defined by the most important group, if any
	 * - Otherwise fallback to the default.yml file setting
	 * @param path                 The path to get the setting of
	 * @param translateProfileName The name of the profile section in the plugin config file to translate result strings into sections
	 * @return The value of the setting
	 */
	public ConfigurationSection getConfigurationSectionSetting(String path, String translateProfileName) {
		return getConfigurationSectionSetting(path, translateProfileName, null);
	}

	/**
	 * Get a configuration section setting for this region, defined as follows
	 * - If earlyResult is non-null, use that
	 * - Else if the region has the setting in its own file (/regions/regionName.yml), use that
	 * - Else if the region has groups, use the setting defined by the most important group, if any
	 * - Otherwise fallback to the default.yml file setting
	 * @param path                 The path to get the setting of
	 * @param translateProfileName The name of the profile section in the plugin config file to translate result strings into sections
	 * @param earlyResult          Result that should have priority over the rest
	 * @return The value of the setting
	 */
	public ConfigurationSection getConfigurationSectionSetting(String path, String translateProfileName, Object earlyResult) {
		Object result = null;
		if(earlyResult != null) {
			result = earlyResult;
		} else if(config.isSet(path)) {
			result = config.get(path);
		} else {
			boolean found = false;
			int priority = Integer.MIN_VALUE;
			for(RegionGroup group : plugin.getFileManager().getGroups()) {
				if(group.isMember(this) && group.getSettings().isSet(path) && group.getPriority() > priority) {
					result = group.getSettings().get(path);
					priority = group.getPriority();
					found = true;
				}
			}
			if(!found) {
				if(this.getFileManager().getRegionSettings().isSet(path)) {
					result = this.getFileManager().getRegionSettings().get(path);
				} else {
					result = this.getFileManager().getFallbackRegionSettings().get(path);
				}
			}
		}

		// Either result is a ConfigurationSection or is used as key in the plugin config to get a ConfigurationSection
		if(result == null) {
			return null;
		} else if(result instanceof ConfigurationSection) {
			return (ConfigurationSection)result;
		} else {
			return plugin.getConfig().getConfigurationSection(translateProfileName + "." + result.toString());
		}
	}

	/**
	 * Set a setting in the file of the region itself.
	 * @param path  The path to set
	 * @param value The value to set it to, null to remove the setting
	 */
	public void setSetting(String path, Object value) {
		config.set(path, value);
		this.saveRequired();
	}


	// LIMIT FUNCTIONS

	/**
	 * Check if the player can buy/rent this region, detailed info in the result object.
	 * @param type   The type of region to check
	 * @param player The player to check it for
	 * @return LimitResult containing if it is allowed, why and limiting factor
	 */
	public LimitResult limitsAllow(RegionType type, OfflinePlayer player) {
		return limitsAllow(type, player, false);
	}

	/**
	 * Check if the player can buy/rent this region, detailed info in the result object.
	 * @param type   The type of region to check
	 * @param offlinePlayer The player to check it for
	 * @param extend Check for extending of rental regions
	 * @return LimitResult containing if it is allowed, why and limiting factor
	 */
	public LimitResult limitsAllow(RegionType type, OfflinePlayer offlinePlayer, boolean extend) {
		if(plugin.hasPermission(offlinePlayer, "areashop.limitbypass")) {
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
			if(plugin.hasPermission(offlinePlayer, "areashop.limits." + group) && this.matchesLimitGroup(group)) {
				String pathPrefix = "limitGroups." + group + ".";
				if(!plugin.getConfig().isInt(pathPrefix + "total")) {
					AreaShop.warn("Limit group " + group + " in the config.yml file does not correctly specify the number of total regions (should be specified as total: <number>)");
				}
				if(!plugin.getConfig().isInt(pathPrefix + typePath)) {
					AreaShop.warn("Limit group " + group + " in the config.yml file does not correctly specify the number of " + typePath + " regions (should be specified as " + typePath + ": <number>)");
				}
				int totalLimit = plugin.getConfig().getInt("limitGroups." + group + ".total");
				int typeLimit = plugin.getConfig().getInt("limitGroups." + group + "." + typePath);
				//AreaShop.debug("typeLimitOther="+typeLimit+", typePath="+typePath);
				int totalCurrent = hasRegionsInLimitGroup(offlinePlayer, group, plugin.getFileManager().getRegions(), exclude);
				int typeCurrent;
				if(type == RegionType.RENT) {
					typeCurrent = hasRegionsInLimitGroup(offlinePlayer, group, plugin.getFileManager().getRents(), exclude);
				} else {
					typeCurrent = hasRegionsInLimitGroup(offlinePlayer, group, plugin.getFileManager().getBuys(), exclude);
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
					if(plugin.hasPermission(offlinePlayer, "areashop.limits." + checkGroup) && this.matchesLimitGroup(checkGroup)) {
						if(limitGroupsOfSameCategory(group, checkGroup)) {
							groups.remove(checkGroup);
							int totalLimitOther = plugin.getConfig().getInt("limitGroups." + checkGroup + ".total");
							int typeLimitOther = plugin.getConfig().getInt("limitGroups." + checkGroup + "." + typePath);
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
	 * Class to store the result of a limits check.
	 */
	public class LimitResult {
		private final boolean actionAllowed;
		private final LimitType limitingFactor;
		private final int maximum;
		private final int current;
		private final String limitingGroup;

		/**
		 * Constructor.
		 * @param actionAllowed  has the action been allowed?
		 * @param limitingFactor The LimitType that has prevented the action (if actionAllowed is false)
		 * @param maximum        The maximum number of regions allowed (if actionAllowed is false)
		 * @param current        The current number of regions the player has (if actionAllowed is false)
		 * @param limitingGroup  The group that is enforcing this limit (if actionAllowed is false)
		 */
		public LimitResult(boolean actionAllowed, LimitType limitingFactor, int maximum, int current, String limitingGroup) {
			this.actionAllowed = actionAllowed;
			this.limitingFactor = limitingFactor;
			this.maximum = maximum;
			this.current = current;
			this.limitingGroup = limitingGroup;
		}

		/**
		 * Check if the action is allowed.
		 * @return true if the actions is allowed, otherwise false
		 */
		public boolean actionAllowed() {
			return actionAllowed;
		}

		/**
		 * Get the type of the factor that is limiting the action, assuming actionAllowed() is false.
		 * @return The type of the limiting factor
		 */
		public LimitType getLimitingFactor() {
			return limitingFactor;
		}

		/**
		 * Get the maximum number of the group that is the limiting factor, assuming actionAllowed() is false.
		 * @return The maximum
		 */
		public int getMaximum() {
			return maximum;
		}

		/**
		 * Get the current number of regions in the group that is the limiting factor, assuming actionAllowed() is false.
		 * @return The current number of regions the player has
		 */
		public int getCurrent() {
			return current;
		}

		/**
		 * Get the name of the group that is limiting the action, assuming actionAllowed() is false.
		 * @return The name of the group
		 */
		public String getLimitingGroup() {
			return limitingGroup;
		}

		@Override
		public String toString() {
			return "actionAllowed=" + actionAllowed + ", limitingFactor=" + limitingFactor + ", maximum=" + maximum + ", current=" + current + ", limitingGroup=" + limitingGroup;
		}
	}

	/**
	 * Checks if two limitGroups are of the same category (same groups and worlds lists).
	 * @param firstGroup  The first group
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
	 * @param player     The player to check the amount for
	 * @param limitGroup The group to check
	 * @param regions    All the regions a player has bought or rented
	 * @param exclude    Exclude this region from the count
	 * @return The number of regions that the player has bought or rented matching the limit group (worlds and groups filters)
	 */
	public int hasRegionsInLimitGroup(OfflinePlayer player, String limitGroup, List<? extends GeneralRegion> regions, GeneralRegion exclude) {
		int result = 0;
		for(GeneralRegion region : regions) {
			if(region.getBooleanSetting("general.countForLimits")
					&& region.isOwner(player)
					&& region.matchesLimitGroup(limitGroup)
					&& (exclude == null || !exclude.getName().equals(region.getName()))) {
				result++;
			}
		}
		return result;
	}

	/**
	 * Check if this region matches the filters of a limit group.
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
	 * Checks an event and handles saving to and restoring from schematic for it.
	 * @param type The type of event
	 */
	public void handleSchematicEvent(RegionEvent type) {
		// Check the individual>group>default setting
		if(!isRestoreEnabled()) {
			AreaShop.debug("Schematic operations for " + getName() + " not enabled, skipped");
			return;
		}
		// Get the safe and restore names
		ConfigurationSection profileSection = getConfigurationSectionSetting("general.schematicProfile", "schematicProfiles");
		if(profileSection == null) {
			return;
		}

		String save = profileSection.getString(type.getValue() + ".save");
		String restore = profileSection.getString(type.getValue() + ".restore");
		// Save the region if needed
		if(save != null && !save.isEmpty()) {
			save = Message.fromString(save).replacements(this).getSingle();
			saveRegionBlocks(save);
		}
		// Restore the region if needed
		if(restore != null && !restore.isEmpty()) {
			restore = Message.fromString(restore).replacements(this).getSingle();
			restoreRegionBlocks(restore);
		}
	}

	// COMMAND EXECUTING

	/**
	 * Run commands as the CommandsSender, replacing all tags with the relevant values.
	 * @param sender   The sender that should perform the command
	 * @param commands A list of the commands to run (without slash and with tags)
	 */
	public void runCommands(CommandSender sender, List<String> commands) {
		if(commands == null || commands.isEmpty()) {
			return;
		}

		for(String command : commands) {
			if(command == null || command.isEmpty()) {
				continue;
			}
			// It is not ideal we have to disable language replacements here, but otherwise giving language variables
			// to '/areashop message' by a command in the config gets replaced and messes up the fancy formatting.
			command = Message.fromString(command).replacements(this).noLanguageReplacements().getSingle();

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
					AreaShop.warn("Command execution failed, command=" + command + ", error=" + error + ", stacktrace:");
					AreaShop.warn(stacktrace);
					AreaShop.warn("--- End of stacktrace ---");
				} else {
					AreaShop.warn("Command execution failed, command=" + command);
				}
			}
			if(!printed) {
				AreaShop.debug("Command run, executor=" + sender.getName() + ", command=" + command);
			}
		}
	}

	/**
	 * Get the volume of the region (number of blocks inside it).
	 * @return Number of blocks in the region
	 */
	public long getVolume() {
		// Cache volume, important for polygon regions
		if(volume < 0) {
			volume = calculateVolume();
		}

		return volume;
	}

	/**
	 * Calculate the volume of the region (could be expensive for polygon regions).
	 * @return Number of blocks in the region
	 */
	private long calculateVolume() {
		// Use own calculation for polygon regions, as WorldGuard does not implement it and returns 0
		ProtectedRegion region = getRegion();
		if(region instanceof ProtectedPolygonalRegion) {
			Vector min = getMinimumPoint();
			Vector max = getMaximumPoint();

			// Exact, but slow algorithm
			if(getWidth() * getDepth() < 100) {
				long surface = 0;
				for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
					for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
						if (region.contains(x, min.getBlockY(), z)) {
							surface++;
						}
					}
				}
				return surface * getHeight();
			}
			// Estimate, but quick algorithm
			else {
				List<Vector> points = plugin.getWorldGuardHandler().getRegionPoints(region);
				int numPoints = points.size();
				if(numPoints < 3) {
					return 0;
				}

				double area = 0;
				int x1, x2, z1, z2;
				for(int i = 0; i <= numPoints - 2; i++) {
					x1 = points.get(i).getBlockX();
					z1 = points.get(i).getBlockZ();

					x2 = points.get(i + 1).getBlockX();
					z2 = points.get(i + 1).getBlockZ();

					area += ((z1 + z2) * (x1 - x2));
				}

				x1 = points.get(numPoints - 1).getBlockX();
				z1 = points.get(numPoints - 1).getBlockZ();
				x2 = points.get(0).getBlockX();
				z2 = points.get(0).getBlockZ();

				area += ((z1 + z2) * (x1 - x2));
				area = Math.ceil(Math.abs(area) / 2);
				return (long)(area * getHeight());
			}
		} else {
			return region.volume();
		}
	}
}






















