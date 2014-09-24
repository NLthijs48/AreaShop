package nl.evolutioncoding.AreaShop.regions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.evolutioncoding.AreaShop.AreaShop;
import nl.evolutioncoding.AreaShop.FileManager;
import nl.evolutioncoding.AreaShop.Utils;
import nl.evolutioncoding.AreaShop.Exceptions.RegionCreateException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.extension.factory.PatternFactory;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.DoubleFlag;
import com.sk89q.worldguard.protection.flags.EnumFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;

public abstract class GeneralRegion {
	protected YamlConfiguration config;
	private static ArrayList<Material> canSpawnIn  = new ArrayList<Material>(Arrays.asList(Material.WOOD_DOOR, Material.WOODEN_DOOR, Material.SIGN_POST, Material.WALL_SIGN, Material.STONE_PLATE, Material.IRON_DOOR_BLOCK, Material.WOOD_PLATE, Material.TRAP_DOOR, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.DRAGON_EGG, Material.GOLD_PLATE, Material.IRON_PLATE));
	private static ArrayList<Material> cannotSpawnOn = new ArrayList<Material>(Arrays.asList(Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE, Material.SIGN_POST, Material.WALL_SIGN, Material.STONE_PLATE, Material.IRON_DOOR_BLOCK, Material.WOOD_PLATE, Material.TRAP_DOOR, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.CACTUS, Material.IRON_FENCE, Material.FENCE_GATE, Material.THIN_GLASS, Material.NETHER_FENCE, Material.DRAGON_EGG, Material.GOLD_PLATE, Material.IRON_PLATE, Material.STAINED_GLASS_PANE));
	private static ArrayList<Material> cannotSpawnBeside = new ArrayList<Material>(Arrays.asList(Material.LAVA, Material.STATIONARY_LAVA, Material.CACTUS));
	protected AreaShop plugin = null;

	/* Enum for region types */
	public enum RegionType {		
		RENT("rent"),
		BUY("buy");
		
		private final String value;
		private RegionType(String value) {
			this.value = value;
		}
		public String getValue() {
			return value;
		}
	} 
	
	/* Enum for schematic event types */
	public enum RegionEvent {		
		CREATED("created"),
		DELETED("deleted"),
		RENTED("rented"),
		EXTENDED("extended"),
		UNRENTED("unrented"),
		BOUGHT("bought"),
		SOLD("sold");
		
		private final String value;
		private RegionEvent(String value) {
			this.value = value;
		}
		public String getValue() {
			return value;
		}
	} 
	
	/* Enum for Region states */
	public enum RegionState {
		FORRENT("forrent"),
		RENTED("rented"),
		FORSALE("forsale"),
		SOLD("sold");
		
		private final String value;
		private RegionState(String value) {
			this.value = value;
		}
		public String getValue() {
			return value;
		}
	}
	
	/* Enum for Region states */
	public enum ClickType {
		RIGHTCLICK("rightClick"),
		LEFTCLICK("leftClick"),
		SHIFTRIGHTCLICK("shiftRightClick"),
		SHIFTLEFTCLICK("shiftLeftClick");
		
		private final String value;
		private ClickType(String value) {
			this.value = value;
		}
		public String getValue() {
			return value;
		}
	}
	
	public GeneralRegion(AreaShop plugin, YamlConfiguration config) throws RegionCreateException {
		this.plugin = plugin;
		this.config = config;
		
		// If region is gone delete the rent and the sign 
		if(getWorld() == null 
				|| plugin.getWorldGuard().getRegionManager(getWorld()) == null 
				|| plugin.getWorldGuard().getRegionManager(getWorld()).getRegion(getName()) == null) {
			
			throw new RegionCreateException("Region of " + getName() + " does not exist anymore");
		}
	}
	
	public GeneralRegion(AreaShop plugin, String name, World world) {
		this.plugin = plugin;
		
		config = new YamlConfiguration();
		config.set("general.name", name);
		setSetting("general.world", world.getName());
		setSetting("general.type", getType().getValue().toLowerCase());
	}
		
	// ABSTRACT
	/**
	 * Get the region type of the region
	 * @return The RegionType of this region
	 */
	public abstract RegionType getType();	

	/**
	 * Update the region flags according the region data
	 */
	public void updateRegionFlags(RegionState toState) {
		// Get state setting
		String setting = toState.getValue();
		ConfigurationSection section = plugin.config().getConfigurationSection("flagProfiles." + getStringSetting("general.flagProfile") + "." + setting);
		setRegionFlags(section);
	}

	/**
	 * Update the region flags according the region data
	 */
	public void updateRegionFlags() {
		// Get state setting
		RegionState toState = null;
		if(isRentRegion()) {
			if(((RentRegion)this).isRented()) {
				toState = RegionState.RENTED;
			} else {
				toState = RegionState.FORRENT;
			}	
		} else if(isBuyRegion()) {
			if(((BuyRegion)this).isSold()) {
				toState = RegionState.SOLD;
			} else {
				toState = RegionState.FORSALE;
			}
		}
		updateRegionFlags(toState);
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
	public List<Location> getSignLocations() {
		List<Location> result = new ArrayList<Location>();
		if(config.getConfigurationSection("general.signs") == null) {
			return result;
		}
		for(String signName : config.getConfigurationSection("general.signs").getKeys(false)) {
			result.add(Utils.configToLocation(config.getConfigurationSection("general.signs." + signName + ".location")));
		}
		return result;
	}
	
	public Location getTeleportLocation() {
		Location result = null;
		result = Utils.configToLocation(config.getConfigurationSection("general.teleportLocation"));
		return result;
	}
	
	public String getName() {
		return config.getString("general.name");
	}
	public String getLowerCaseName() {
		return getName().toLowerCase();
	}
	
	public boolean isRestoreEnabled() {
		return getBooleanSetting("general.enableRestore");
	}
	
	public String getRestoreProfile() {
		return getStringSetting("general.schematicProfile");
	}
	
	public String getWorldName() {
		String world = getStringSetting("general.world");
		if(world == null) {
			return null;
		}
		return world;
	}
	
	public World getWorld() {
		return Bukkit.getWorld(getWorldName());
	}
	
	public FileManager getFileManager() {
		return plugin.getFileManager();
	}
	
	public ProtectedRegion getRegion() {
		if(getWorld() == null 
				|| plugin.getWorldGuard() == null 
				|| plugin.getWorldGuard().getRegionManager(getWorld()) == null
				|| plugin.getWorldGuard().getRegionManager(getWorld()).getRegion(getName()) == null) {
			return null;
		}
		return plugin.getWorldGuard().getRegionManager(getWorld()).getRegion(getName());
	}
	
	public HashMap<String, Object> getAllReplacements() {
		HashMap<String, Object> result = getSpecificReplacements();
		
		result.put(AreaShop.tagRegionName, getName());
		result.put(AreaShop.tagRegionType, getType().getValue().toLowerCase());
		result.put(AreaShop.tagWorldName, getWorldName());
		// TODO: add more?
		
		return result;
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
		config.set(signPath + "location", Utils.locationToConfig(location));
		config.set(signPath + "facing", facing.name());
		config.set(signPath + "signType", signType.name());
		if(profile != null && profile.length() != 0) {
			config.set(signPath + "profile", profile);
		}
	}
	
	/**
	 * Remove a sign
	 * @param name Name of the sign to be removed
	 */
	public void removeSign(String name) {
		config.set("general.signs." + name, null);
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
		Set<String> signs = null;
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
	 * Update the signs connected to this region
	 * @return true if the update was successful, otherwise false
	 */
	public boolean updateSigns() {
		boolean result = true;
		Set<String> signs = null;
		if(config.getConfigurationSection("general.signs") != null) {
			signs = config.getConfigurationSection("general.signs").getKeys(false);
		}
		if(signs == null) {
			return true;
		}
		HashMap<String, Object> replacements = getAllReplacements();
		for(String sign : signs) {
			AreaShop.debug("sign name: " + sign);
			Location location = Utils.configToLocation(config.getConfigurationSection("general.signs." + sign + ".location"));
			if(location == null) {
				// TODO: Remove the sign if the location is wrong?
				AreaShop.debug("  location null");
				result = false;
				continue;
			}
			// Get the profile set in the config
			String profile = config.getString("general.signs." + sign + ".profile");
			if(profile == null || profile.length() == 0) {
				profile = getStringSetting("general.signProfile");
			}			
			AreaShop.debug("  profile=" + profile);
			// Get the prefix
			String prefix = "signProfiles." + profile + "." + getState().getValue().toLowerCase() + ".";			
			// Get the lines
			String[] signLines = new String[4];
			signLines[0] = plugin.config().getString(prefix + "line1");
			signLines[1] = plugin.config().getString(prefix + "line2");
			signLines[2] = plugin.config().getString(prefix + "line3");
			signLines[3] = plugin.config().getString(prefix + "line4");
				// DEBUG
				for(String line : signLines) {
					AreaShop.debug("  signLine: " + line);
				}
			// Check if the sign should be present
			Block block = location.getBlock();
			if(!plugin.config().isSet(prefix) 
					|| (	   (signLines[0] == null || signLines[0].length() == 0) 
							&& (signLines[1] == null || signLines[1].length() == 0) 
							&& (signLines[2] == null || signLines[2].length() == 0) 
							&& (signLines[3] == null || signLines[3].length() == 0) )) {
				AreaShop.debug("  set to air");
				block.setType(Material.AIR);
			} else {				
				Sign signState = null;
				if(block.getType() != Material.WALL_SIGN && block.getType() != Material.SIGN_POST) {
					Material signType = null;
					try {
						signType = Material.valueOf(config.getString("general.signs." + sign + ".signType"));
					} catch(NullPointerException | IllegalArgumentException e) {
						signType = null;
					}
					if(signType != Material.WALL_SIGN && signType != Material.SIGN_POST) {
						block.setType(Material.AIR);
						AreaShop.debug("  setting sign failed");
						continue;
					}
					block.setType(signType);
					signState = (Sign)block.getState();
					org.bukkit.material.Sign signData = (org.bukkit.material.Sign)signState.getData();
					BlockFace signFace;
					try {
						signFace = BlockFace.valueOf(config.getString("general.signs." + sign + ".signType"));
					} catch(NullPointerException | IllegalArgumentException e) {
						signFace = null;
					}
					if(signFace != null) {
						signData.setFacingDirection(signFace);
						signState.setData(signData);
					}
				}
				signState = (Sign)block.getState();
				org.bukkit.material.Sign signData = (org.bukkit.material.Sign)signState.getData();
				if(!config.isString("general.signs." + sign + ".signType")) {
					config.set("general.signs." + sign + ".signType", signState.getType().toString());
					this.save();
				}
				if(!config.isString("general.signs." + sign + ".facing")) {
					config.set("general.signs." + sign + ".facing", signData.getFacing().toString());
					this.save();
				}
				// Apply replacements and color and then set it on the sign
				for(int i=0; i<signLines.length; i++) {
					if(signLines[i] == null) {
						signState.setLine(i, "");
						continue;
					}					
					for(String tag : replacements.keySet()) {
						if(replacements.get(tag) != null) {
							signLines[i] = signLines[i].replace(tag, replacements.get(tag).toString());
						}						
					}
					signLines[i] = plugin.fixColors(signLines[i]);		
					signState.setLine(i, signLines[i]);
				}
				signState.update();
			}		
		}
		return result;
	}
	
	/**
	 * Change the restore setting
	 * @param restore true, false or general
	 */
	public void setRestoreSetting(String restore) {
		setSetting("general.enableRestore", restore);
	}
	
	/**
	 * Change the restore profile
	 * @param profile default or the name of the profile as set in the config
	 */
	public void setRestoreProfile(String profile) {
		setSetting("general.restoreProfile", profile);
	}
	
	/**
	 * Save all blocks in a region for restoring later
	 * @param regionName The name of the region
	 * @param world The world that it is in
	 * @param fileName The name of the file to save to (extension and folder will be added)
	 * @return
	 */
	public boolean saveRegionBlocks(String fileName) {
		boolean result = true;
		EditSession editSession = new EditSession(new BukkitWorld(getWorld()), plugin.config().getInt("maximumBlocks"));
		ProtectedRegion region = plugin.getWorldGuard().getRegionManager(getWorld()).getRegion(getName());
		if(region == null) {
			AreaShop.debug("Region '" + getName() + "' does not exist in WorldGuard, save failed");
			return false;
		}
		
		// Get the origin and size of the region
		Vector origin = new Vector(region.getMinimumPoint().getBlockX(), region.getMinimumPoint().getBlockY(), region.getMinimumPoint().getBlockZ());
		Vector size = (new Vector(region.getMaximumPoint().getBlockX(), region.getMaximumPoint().getBlockY(), region.getMaximumPoint().getBlockZ()).subtract(origin)).add(new Vector(1,1,1));
		
		// The path to save the schematic
		File saveFile = new File(plugin.getFileManager().getSchematicFolder() + File.separator + fileName + AreaShop.schematicExtension);
		
		// Save the schematic
		editSession.enableQueue();
		CuboidClipboard clipboard = new CuboidClipboard(size, origin);
		clipboard.copy(editSession);
		try {
			SchematicFormat.MCEDIT.save(clipboard, saveFile);
		} catch (IOException | DataException e) {
			result = false;
		}
		editSession.flushQueue();
		if(result) {
			AreaShop.debug("Saved schematic for " + getName());
		} else {
			AreaShop.debug("Not saved " + getName());
		}
		return result;
	}
	
	/**
	 * Restore all blocks in a region for restoring later
	 * @param regionName The name of the region
	 * @param world The world that it is in
	 * @param fileName The name of the file to save to (extension and folder will be added)
	 * @return
	 */
	public boolean restoreRegionBlocks(String fileName) {
		boolean result = true;
		EditSession editSession = new EditSession(new BukkitWorld(getWorld()), plugin.config().getInt("maximumBlocks"));
		ProtectedRegion region = plugin.getWorldGuard().getRegionManager(getWorld()).getRegion(getName());
		if(region == null) {
			AreaShop.debug("Region '" + getName() + "' does not exist in WorldGuard, restore failed");
			return false;
		}
		// Get the origin and size of the region
		Vector origin = new Vector(region.getMinimumPoint().getBlockX(), region.getMinimumPoint().getBlockY(), region.getMinimumPoint().getBlockZ());
		
		// The path to save the schematic
		File saveFile = new File(plugin.getFileManager().getSchematicFolder() + File.separator + fileName + AreaShop.schematicExtension);
		editSession.enableQueue();
		try {
			SchematicFormat.MCEDIT.load(saveFile).place(editSession, origin, false);
		} catch (MaxChangedBlocksException | IOException | DataException e) {
			result = false;
		}
		editSession.flushQueue();
		
		//we.flushBlockBag(localPlayer, editSession);
		if(result) {
			AreaShop.debug("Restored schematic for " + getName());
		} else {
			AreaShop.debug("Not restored " + getName());
		}
		return result;
	}
	
	/**
	 * Reset all flags of the region
	 */
	public void resetRegionFlags() {
		ProtectedRegion region = getRegion();
		if(region != null) {
			for(Flag<?> flag : DefaultFlag.getFlags()) {
				region.setFlag(flag, null);			
			}
		}
	}
	
	/**
	 * Set the region flags/options to the values of a ConfigurationSection
	 * @param player The player that does it
	 * @param region The region 
	 * @param flags
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected boolean setRegionFlags(ConfigurationSection flags) {
		boolean result = true;
		
		if(flags == null) {
			AreaShop.debug("Flags section is null");
			return false;
		}
		Set<String> flagNames = flags.getKeys(false);
		WorldGuardPlugin worldGuard = plugin.getWorldGuard();
		Flag<?> flagType = null;
		Object flagValue = null;		
		
		/* Get the region */
		ProtectedRegion region = getRegion();
		if(region == null) {
			AreaShop.debug("Region '" + getName() + "' does not exist, setting flags failed");
			return false;
		}
		
		Map<String, Object> replacements = getAllReplacements();
		
		Iterator<String> it = flagNames.iterator();
		while(it.hasNext()) {
			String flagName = it.next();
			String value = flags.getString(flagName);
			
			// Apply replacements
			if(value != null) {
				for(String key : replacements.keySet()) {
					if(replacements.get(key) != null) {
						value = value.replace(key, replacements.get(key).toString());
					}
				}
			}
			
			/* Check for a couple of options or use as flag */
			if(flagName.equalsIgnoreCase("members")) {
				/* Split the string and parse all values */
				String[] names = value.split("\\s*,\\s*");
				DefaultDomain members = region.getMembers();
				for(int i=0; i<names.length; i++) {
					if(names[i].equals("clear")) {
						members.removeAll();
					} else if(names[i].charAt(0) == '+') {
						members.addPlayer(names[i].substring(1));;
					} else if(names[i].charAt(0) == '-') {
						members.removePlayer(names[i].substring(1));;
					}
				}
				region.setMembers(members);
			} else if(flagName.equalsIgnoreCase("owners")) {
				/* Split the string and parse all values */
				String[] names = value.split("\\s*,\\s*");
				DefaultDomain owners = region.getOwners();
				for(int i=0; i<names.length; i++) {
					if(names[i].equals("clear")) {
						owners.removeAll();
					} else if(names[i].charAt(0) == '+') {
						owners.addPlayer(names[i].substring(1));;
					} else if(names[i].charAt(0) == '-') {
						owners.removePlayer(names[i].substring(1));;
					}
				}
				region.setOwners(owners);
			} else if(flagName.equalsIgnoreCase("priority")) {
				try {
					int priority = Integer.parseInt(value);
					region.setPriority(priority);				
					AreaShop.debug("Flag set: " + flagName + " --> " + value);
				} catch(NumberFormatException e) {
					plugin.getLogger().info("The value of flag " + flagName + " is not a number");
					result = false;
				}
			} else if(flagName.equalsIgnoreCase("parent")) {
				ProtectedRegion parentRegion = worldGuard.getRegionManager(getWorld()).getRegion(value);
				if(parentRegion != null) {
					try {
						region.setParent(parentRegion);
					} catch (CircularInheritanceException e) {
						plugin.getLogger().info("The parent set in the config is not correct (circular inheritance)");
					}
				} else {
					plugin.getLogger().info("The parent set in the config is not correct (region does not exist)");
				}				
			} else {
				flagType = null;
				flagValue = null;
			
				try {
					flagType = DefaultFlag.fuzzyMatchFlag(flagName);
					if(flagType != null && !(value.equals("") || value.equals("none"))) {
						flagValue = flagType.parseInput(worldGuard, null, value);
					}
				} catch (InvalidFlagFormat e) {
					plugin.getLogger().info("The value of flag " + flagName + " is wrong");
					result = false;
				}
				if(flagType != null) {
					if(flagType instanceof StateFlag) {
						if(value.equals("")) {
							region.setFlag((StateFlag)flagType, null);
						} else {
							region.setFlag((StateFlag)flagType, (State)flagValue);
						}
					} else if(flagType instanceof BooleanFlag) {
						if(value.equals("")) {
							region.setFlag((BooleanFlag)flagType, null);
						} else {
							region.setFlag((BooleanFlag)flagType, (Boolean)flagValue);
						}
					} else if(flagType instanceof IntegerFlag) {
						if(value.equals("")) {
							region.setFlag((IntegerFlag)flagType, null);
						} else {
							region.setFlag((IntegerFlag)flagType, (Integer)flagValue);
						}
					} else if(flagType instanceof DoubleFlag) {
						if(value.equals("")) {
							region.setFlag((DoubleFlag)flagType, null);
						} else {
							region.setFlag((DoubleFlag)flagType, (Double)flagValue);
						}
					} else if(flagType instanceof StringFlag) {
						if(value.equals("")) {
							region.setFlag((StringFlag)flagType, null);
						} else {
							String coloredValue = translateBukkitToWorldGuardColors((String)flagValue);
							region.setFlag((StringFlag)flagType, coloredValue);
						}
					} else if(flagType instanceof SetFlag<?>) {
						if(value.equals("")) {
							region.setFlag((SetFlag)flagType, null);
						} else {
							region.setFlag((SetFlag)flagType, (Set<String>)flagValue);
						}
					} /* else if(flagType instanceof LocationFlag) {
						region.setFlag((LocationFlag)flagType, (Location)flagValue);
					} */ else if(flagType instanceof EnumFlag) {
						if(value.equals("")) {
							region.setFlag((EnumFlag)flagType, null);
						} else {
							region.setFlag((EnumFlag)flagType, (Enum)flagValue);
						}
					} else {
						result = false;
					}
					AreaShop.debug("Region " + region.getId() + ", flag " + flagName + " --> " + value);
				} else {
					result = false;
				}
			}			
		}

		try {
			worldGuard.getRegionManager(getWorld()).save();
		} catch (StorageException e) {
			plugin.getLogger().info("Error: regions could not be saved");
		}
		return result;
	}
	
	/**
	 * Translate the color codes you put in greeting/farwell messages to the weird color codes of WorldGuard
	 * @param message The message where the color codes should be translated (this message has bukkit color codes)
	 * @return The string with the WorldGuard color codes
	 */
	public String translateBukkitToWorldGuardColors(String message) {
		String result = message;
		result = result.replace("&c", "&r");
        result = result.replace("&4", "&R");
        result = result.replace("&e", "&y");
        result = result.replace("&6", "&Y");
        result = result.replace("&a", "&g");
        result = result.replace("&2", "&G");
        result = result.replace("&b", "&c");
        result = result.replace("&3", "&C");
        result = result.replace("&9", "&b");
        result = result.replace("&1", "&B");
        result = result.replace("&d", "&p");
        result = result.replace("&5", "&P");
        result = result.replace("&0", "&0");
        result = result.replace("&8", "&1");
        result = result.replace("&7", "&2");
        result = result.replace("&f", "&w");
        result = result.replace("&r", "&x");
		return result;
	}
	
	/**
	 * Save this region to the disk
	 */
	public boolean save() {
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
	 * @param regionName
	 * @param location
	 */
	public void setTeleport(Location location) {
		if(location == null) {
			setSetting("general.teleportLocation", null);
		} else {
			setSetting("general.teleportLocation", Utils.locationToConfig(location, true));
		}
		this.save();
	}
	
	
	/**
	 * Teleport a player to the region
	 * @param player Player that should be teleported
	 * @param regionName The name of the region the player should be teleported to
	 */
	public boolean teleportPlayer(Player player, boolean toSign) {
		int checked = 1;
		boolean owner = false;
		Location startLocation = null;
		ProtectedRegion region = getRegion();
		if(toSign) {
			List<Location> signs = getSignLocations();
			if(!signs.isEmpty()) {
				// Use the location 1 below the sign to prevent weird spawing above the sign
				startLocation = signs.get(0).subtract(0.0, 1.0, 0.0);
				startLocation.setPitch(player.getLocation().getPitch());
				startLocation.setYaw(player.getLocation().getYaw());
			}
			
		} else if(this.hasTeleportLocation()) {
			startLocation = getTeleportLocation();
		}
		if(isRentRegion()) {
			owner = player.getUniqueId().equals(((RentRegion)this).getRenter());
		} else {
			owner = player.getUniqueId().equals(((BuyRegion)this).getBuyer());
		}
		// Check permissions
		if((!player.hasPermission("areashop.teleport") && !toSign) || (!player.hasPermission("areashop.teleportsign") && toSign)) {
			plugin.message(player, "teleport-noPermission");
			return false;
		} else if(!owner && !player.hasPermission("areashop.teleportall") && !toSign) {
			plugin.message(player, "teleport-noPermissionOther");
			return false;
		}
		// Set default startLocation if not set
		if(startLocation == null) {
			if(region != null) {
				// Set to block in the middle, y configured in the config
				Vector middle = Vector.getMidpoint(region.getMaximumPoint(), region.getMinimumPoint());
				String configSetting = getStringSetting("general.teleportLocationY");
				AreaShop.debug("teleportLocationY = " + configSetting);
				if("bottom".equalsIgnoreCase(configSetting)) {
					middle = middle.setY(region.getMinimumPoint().getBlockY());
				} else if("top".equalsIgnoreCase(configSetting)) {
					middle = middle.setY(region.getMaximumPoint().getBlockY());
				} else {
					middle = middle.setY(middle.getBlockY());
				}
				startLocation = new Location(getWorld(), middle.getX(), middle.getY(), middle.getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
			} else {
				return false;
			}
		}
		boolean insideRegion = getBooleanSetting("general.teleportIntoRegion");
		AreaShop.debug("insideRegion = " + insideRegion);
		int maxTries = plugin.config().getInt("maximumTries");
		AreaShop.debug("maxTries = " + maxTries);
		
		// set location in the center of the block
		startLocation.setX(startLocation.getBlockX() + 0.5);
		startLocation.setZ(startLocation.getBlockZ() + 0.5);
		
		// Check locations starting from startLocation and then a cube that increases
		// radius around that (until no block in the region is found at all cube sides)
		Location saveLocation = startLocation;
		int radius = 1;
		boolean blocksInRegion = region.contains(startLocation.getBlockX(), startLocation.getBlockY(), startLocation.getBlockZ());
		boolean done = isSave(saveLocation) && ((blocksInRegion && insideRegion) || (!insideRegion));
		boolean north=false, east=false, south=false, west=false, top=false, bottom=false;
		boolean track;
		while(((blocksInRegion && insideRegion) || (!insideRegion)) && !done) {
			blocksInRegion = false;
			// North side
			track = false;
			for(int x=-radius+1; x<=radius && !done && !north; x++) {
				for(int y=-radius+1; y<radius && !done; y++) {
					saveLocation = startLocation.clone().add(x, y, -radius);
					if(saveLocation.getBlockY()>256 || saveLocation.getBlockY()<0) {
						continue;
					}
					if((insideRegion && region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) || !insideRegion) {
						checked++;
						done = isSave(saveLocation) || checked > maxTries;
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
					saveLocation = startLocation.clone().add(radius, y, z);
					if(saveLocation.getBlockY()>256 || saveLocation.getBlockY()<0) {
						continue;
					}
					if((insideRegion && region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) || !insideRegion) {
						checked++;
						done = isSave(saveLocation) || checked > maxTries;
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
					saveLocation = startLocation.clone().add(x, y, radius);
					if(saveLocation.getBlockY()>256 || saveLocation.getBlockY()<0) {
						continue;
					}
					if((insideRegion && region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) || !insideRegion) {
						checked++;
						done = isSave(saveLocation) || checked > maxTries;
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
					saveLocation = startLocation.clone().add(-radius, y, z);
					if(saveLocation.getBlockY()>256 || saveLocation.getBlockY()<0) {
						continue;
					}
					if((insideRegion && region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) || !insideRegion) {
						checked++;
						done = isSave(saveLocation) || checked > maxTries;
						blocksInRegion = true;
						track = true;
					}
				}
			}
			west = west || !west;
			
			// Top side
			track = false;
			// Middle block of the top
			if((startLocation.getBlockY() + radius) > 256) {
				top = true;
			}
			if(!done && !top) {
				saveLocation = startLocation.clone().add(0, radius, 0);
				if((insideRegion && region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) || !insideRegion) {
					checked++;
					done = isSave(saveLocation) || checked > maxTries;
					blocksInRegion = true;
					track = true;
				}
			}
			for(int r=1; r<=radius && !done && !top; r++) {
				// North
				for(int x=-r+1; x<=r && !done; x++) {
					saveLocation = startLocation.clone().add(x, radius, -r);
					if((insideRegion && region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) || !insideRegion) {
						checked++;
						done = isSave(saveLocation) || checked > maxTries;
						blocksInRegion = true;
						track = true;
					}
				}
				// East
				for(int z=-r+1; z<=r && !done; z++) {
					saveLocation = startLocation.clone().add(r, radius, z);
					if((insideRegion && region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) || !insideRegion) {
						checked++;
						done = isSave(saveLocation) || checked > maxTries;
						blocksInRegion = true;
						track = true;
					}
				}
				// South side
				for(int x=r-1; x>=-r && !done; x--) {
					saveLocation = startLocation.clone().add(x, radius, r);
					if((insideRegion && region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) || !insideRegion) {
						checked++;
						done = isSave(saveLocation) || checked > maxTries;
						blocksInRegion = true;
						track = true;
					}
				}
				// West side
				for(int z=r-1; z>=-r && !done; z--) {
					saveLocation = startLocation.clone().add(-r, radius, z);
					if((insideRegion && region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) || !insideRegion) {
						checked++;
						done = isSave(saveLocation) || checked > maxTries;
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
				saveLocation = startLocation.clone().add(0, -radius, 0);
				if((insideRegion && region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) || !insideRegion) {
					checked++;
					done = isSave(saveLocation) || checked > maxTries;
					blocksInRegion = true;
					track = true;
				}
			}
			for(int r=1; r<=radius && !done && !bottom; r++) {
				// North
				for(int x=-r+1; x<=r && !done; x++) {
					saveLocation = startLocation.clone().add(x, -radius, -r);
					if((insideRegion && region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) || !insideRegion) {
						checked++;
						done = isSave(saveLocation) || checked > maxTries;
						blocksInRegion = true;
						track = true;
					}
				}
				// East
				for(int z=-r+1; z<=r && !done; z++) {
					saveLocation = startLocation.clone().add(r, -radius, z);
					if((insideRegion && region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) || !insideRegion) {
						checked++;
						done = isSave(saveLocation) || checked > maxTries;
						blocksInRegion = true;
						track = true;
					}
				}
				// South side
				for(int x=r-1; x>=-r && !done; x--) {
					saveLocation = startLocation.clone().add(x, -radius, r);
					if((insideRegion && region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) || !insideRegion) {
						checked++;
						done = isSave(saveLocation) || checked > maxTries;
						blocksInRegion = true;
						track = true;
					}
				}
				// West side
				for(int z=r-1; z>=-r && !done; z--) {
					saveLocation = startLocation.clone().add(-r, -radius, z);
					if((insideRegion && region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) || !insideRegion) {
						checked++;
						done = isSave(saveLocation) || checked > maxTries;
						blocksInRegion = true;
						track = true;
					}
				}			
			}
			bottom = bottom || !track;
			
			// Increase cube radius
			radius++;
		}
		if(done && isSave(saveLocation)) {
			plugin.message(player, "teleport-success", getName());
			player.teleport(saveLocation);
			AreaShop.debug("Found location: " + saveLocation.toString() + " Tries: " + (checked-1));
			return true;
		} else {
			plugin.message(player, "teleport-noSafe", getName(), checked-1, maxTries);
			AreaShop.debug("No location found, checked " + (checked-1) + " spots of max " + maxTries);
			return false;
		}	
	}
	public boolean teleportPlayer(Player player) {
		return teleportPlayer(player, false);
	}
	
	/**
	 * Checks if a certain location is safe to teleport to
	 * @param location The location to check
	 * @return true if it is safe, otherwise false
	 */
	protected boolean isSave(Location location) {
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
		ArrayList<Material> around = new ArrayList<Material>(Arrays.asList(
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
	public boolean getBooleanSetting(String path) {
		if(config.isBoolean(path)) {
			return config.getBoolean(path);
		}
		boolean result = false;
		int priority = Integer.MIN_VALUE;
		boolean found = false;
		for(RegionGroup group : plugin.getFileManager().getGroups()) {
			if(group.isMember(this) && group.getSettings().isBoolean(path) && group.getPriority() > priority) {
				result = group.getSettings().getBoolean(path);
				priority = group.getPriority();
				found = true;
			}
		}
		if(found) {
			return result;
		}
		return this.getFileManager().getDefaultSettings().getBoolean(path);
	}
	
	public int getIntegerSetting(String path) {
		if(config.isInt(path)) {
			return config.getInt(path);
		}
		int result = 0;
		int priority = Integer.MIN_VALUE;
		boolean found = false;
		for(RegionGroup group : plugin.getFileManager().getGroups()) {
			if(group.isMember(this) && group.getSettings().isInt(path) && group.getPriority() > priority) {
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
		if(config.isDouble(path)) {
			return config.getDouble(path);
		}
		double result = 0;
		int priority = Integer.MIN_VALUE;
		boolean found = false;
		for(RegionGroup group : plugin.getFileManager().getGroups()) {
			if(group.isMember(this) && group.getSettings().isDouble(path) && group.getPriority() > priority) {
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
		if(config.isLong(path)) {
			return config.getLong(path);
		}
		long result = 0;
		int priority = Integer.MIN_VALUE;
		boolean found = false;
		for(RegionGroup group : plugin.getFileManager().getGroups()) {
			if(group.isMember(this) && group.getSettings().isLong(path) && group.getPriority() > priority) {
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
		if(config.isString(path)) {
			return config.getString(path);
		}
		String result = null;
		int priority = Integer.MIN_VALUE;
		boolean found = false;
		for(RegionGroup group : plugin.getFileManager().getGroups()) {
			if(group.isMember(this) && group.getSettings().isString(path) && group.getPriority() > priority) {
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
		if(config.isList(path)) {
			return config.getStringList(path);
		}
		List<String> result = null;
		int priority = Integer.MIN_VALUE;
		boolean found = false;
		for(RegionGroup group : plugin.getFileManager().getGroups()) {
			if(group.isMember(this) && group.getSettings().isString(path) && group.getPriority() > priority) {
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
	}
	

	
	// LIMIT FUNCTIONS
	/**
	 * Get the total number of regions a player has
	 * @param player The player to check
	 * @return The total number of regions the player has
	 */
	public int getCurrentTotalRegions(Player player) {
		int result = 0;
		for(GeneralRegion region : plugin.getFileManager().getRegions()) {
			if(region.isRentRegion() && ((RentRegion)region).isRenter(player)) {
				result++;
			} else if(region.isBuyRegion() && ((BuyRegion)region).isBuyer(player)) {
				result++;
			}
		}
		return result;
	}
	
	/**
	 * Get the number of rents a player has
	 * @param player The player to check
	 * @return The number of rent regions a player has
	 */
	public int getCurrentRentRegions(Player player) {
		int result = 0;
		for(RentRegion region : plugin.getFileManager().getRents()) {
			if(region.isRenter(player)) {
				result++;
			}
		}
		return result;
	}

	/**
	 * Get the number of buys a player has
	 * @param player The player to check
	 * @return The number of buy regions a player has
	 */
	public int getCurrentBuyRegions(Player player) {
		int result = 0;
		for(BuyRegion region : plugin.getFileManager().getBuys()) {
			if(region.isBuyer(player)) {
				result++;
			}
		}
		return result;
	}
	

	/**
	 * Get the total number of regions the player can have
	 * @param player The player to check
	 * @return Integer.MAX_VALUE if unlimited, otherwise a number
	 */
	public int getMaxRegions(Player player, String setting) {
		int result = 0;		
		Set<String> groups = plugin.config().getConfigurationSection("limitGroups").getKeys(false);
		for(String group : groups) {
			if(player.hasPermission("areashop.limits." + group)) {
				int w = plugin.config().getInt("limitGroups." + group + "." + setting);
				if(w > result) {
					result = w;
				} else if(w == -1) {
					result = Integer.MAX_VALUE;
				}
			}
		}		
		return result;
	}	
	// Functions for accessing the different limits
	public int getMaxTotalRegions(Player player) {
		return getMaxRegions(player, "total");
	}
	public int getMaxRentRegions(Player player) {
		return getMaxRegions(player, "rents");
	}
	public int getMaxBuyRegions(Player player) {
		return getMaxRegions(player, "buys");
	}	
	
	
	
	
	/**
	 * Checks an event and handles saving to and restoring from schematic for it
	 * @param type The type of event
	 */
	public void handleSchematicEvent(RegionEvent type) {
		// Check for the general killswitch
		if(!plugin.config().getBoolean("enableSchematics")) {
			return;
		}
		// Check the individual>group>default setting
		if(!isRestoreEnabled()) {
			return;
		}
		// Get the safe and restore names		
		String save = plugin.config().getString("schematicProfiles." + getRestoreProfile() + "." + type.getValue() + ".save");
		String restore = plugin.config().getString("schematicProfiles." + getRestoreProfile() + "." + type.getValue() + ".restore");
		// Save the region if needed
		if(save != null && save.length() != 0) {
			save = save.replace(AreaShop.tagRegionName, getName());
			save = save.replace(AreaShop.tagRegionType, getType().getValue().toLowerCase());
			this.saveRegionBlocks(save);			
		}
		// Restore the region if needed
		if(restore != null && restore.length() != 0) {
			restore = restore.replace(AreaShop.tagRegionName, getName());
			restore = restore.replace(AreaShop.tagRegionType, getType().getValue().toLowerCase());
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
		
		boolean postCommandErrors = plugin.config().getBoolean("postCommandErrors");
		for(String command : commands) {
			if(command == null || command.length() == 0) {
				continue;
			}
			
			// Apply replacements
			HashMap<String, Object> replacements = getAllReplacements();	
			for(String tag : replacements.keySet()) {
				Object replacement = replacements.get(tag);
				if(replacement != null) {
					command = command.replace(tag, replacement.toString());
				}
			}
			
			boolean result = false;
			String error = null;
			try {
				result = plugin.getServer().dispatchCommand(sender, command);
			} catch(CommandException e) {
				result = false;
				error = e.getMessage();				
			}
			if(error == null) {
				AreaShop.debug("Command run, executor=" + sender.getName() + ", command=" + command + ", result=" + result);
			} else {
				AreaShop.debug("Command run, executor=" + sender.getName() + ", command=" + command + ", result=" + result + ", error=" + error);
			}
			if(!result && postCommandErrors) {
				if(error != null) {
					plugin.getLogger().info("Command execution failed, command=" + command + ", error=" + error);
				} else {
					plugin.getLogger().info("Command execution failed, command=" + command);
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
		List<String> commands = plugin.config().getStringList(path);
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
	 */
	public boolean runSignCommands(String signName, Player clicker, ClickType clickType) {
		// Get the profile set in the config
		String profile = config.getString("general.signs." + signName + ".profile");
		if(profile == null || profile.length() == 0) {
			profile = getStringSetting("general.signProfile");
		}	
		// Run player commands if specified
		String playerPath = "signProfiles." + profile + "." + getState().getValue().toLowerCase() + "." + clickType.getValue() + "Player";
		List<String> playerCommands = plugin.config().getStringList(playerPath);
		runCommands(clicker, playerCommands);
		
		// Run console commands if specified
		String consolePath = "signProfiles." + profile + "." + getState().getValue().toLowerCase() + "." + clickType.getValue() + "Console";
		List<String> consoleCommands = plugin.config().getStringList(consolePath);
		runCommands(Bukkit.getConsoleSender(), consoleCommands);		
		
		return !playerCommands.isEmpty() || !consoleCommands.isEmpty();
	}
	
}






















