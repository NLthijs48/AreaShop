package nl.evolutioncoding.AreaShop.regions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
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
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;

public abstract class GeneralRegion {
	protected YamlConfiguration config;
	private static ArrayList<Material> canSpawnIn  = new ArrayList<Material>(Arrays.asList(Material.WOOD_DOOR, Material.WOODEN_DOOR, Material.SIGN_POST, Material.WALL_SIGN, Material.STONE_PLATE, Material.IRON_DOOR_BLOCK, Material.WOOD_PLATE, Material.TRAP_DOOR, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.DRAGON_EGG, Material.GOLD_PLATE, Material.IRON_PLATE));
	private static ArrayList<Material> cannotSpawnOn = new ArrayList<Material>(Arrays.asList(Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE, Material.SIGN_POST, Material.WALL_SIGN, Material.STONE_PLATE, Material.IRON_DOOR_BLOCK, Material.WOOD_PLATE, Material.TRAP_DOOR, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.CACTUS, Material.IRON_FENCE, Material.FENCE_GATE, Material.THIN_GLASS, Material.NETHER_FENCE, Material.DRAGON_EGG, Material.GOLD_PLATE, Material.IRON_PLATE, Material.STAINED_GLASS_PANE));
	private static ArrayList<Material> cannotSpawnBeside = new ArrayList<Material>(Arrays.asList(Material.LAVA, Material.STATIONARY_LAVA, Material.CACTUS));
	protected AreaShop plugin = null;

	public GeneralRegion(AreaShop plugin, YamlConfiguration config) throws RegionCreateException {
		this.plugin = plugin;
		this.config = config;
		
		// If region is gone delete the rent and the sign 
		if(getWorld() == null 
				|| plugin.getWorldGuard().getRegionManager(getWorld()) == null 
				|| plugin.getWorldGuard().getRegionManager(getWorld()).getRegion(getName()) == null) {
			
			throw new RegionCreateException("Region of " + getName() + " does not exist anymore");
		} else {
			// If the sign is gone remove the rent 
			if(getSignLocation() == null) {
				throw new RegionCreateException("Sign of region " + getName() + " does not exist anymore");
			}				
			Block block = getWorld().getBlockAt(getSignLocation());
			if(!(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST)) {
				throw new RegionCreateException("Sign of region " + getName() + " does not exist anymore");
			}
		}
	}
	
	public GeneralRegion(AreaShop plugin, String name, World world, Location signLocation) {
		this.plugin = plugin;
		
		config = new YamlConfiguration();
		config.set("name", name);
		setSetting("world", world.getName());
		setSetting("signLocation", Utils.locationToConfig(signLocation));
	}
		
	// GETTERS
	public Location getSignLocation() {
		Location result = null;
		if(isRentRegion()) {
			result = Utils.configToLocation(config.getConfigurationSection("rent.signLocation"));
		} else {
			result = Utils.configToLocation(config.getConfigurationSection("buy.signLocation"));
		}
		return result;
	}
	
	public Location getTeleportLocation() {
		Location result = null;
		if(isRentRegion()) {
			result = Utils.configToLocation(config.getConfigurationSection("rent.teleportLocation"));
		} else {
			result = Utils.configToLocation(config.getConfigurationSection("buy.teleportLocation"));
		}
		return result;
	}
	
	public String getName() {
		return config.getString("name");
	}
	
	public String getRestoreSetting() {
		return getStringSetting("enableRestore");
	}
	
	public String getRestoreProfile() {
		return getStringSetting("restoreProfile");
	}
	
	public String getWorldName() {
		if(getStringSetting("world") == null) {
			return null;
		}
		return getStringSetting("world");
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
	
	public boolean hasTeleportLocation() {
		if(isRentRegion()) {
			return config.isSet("rent.teleportLocation");
		} else {
			return config.isSet("buy.teleportLocation");
		}
	}
	
	/**
	 * Check if this region is a RentRegion
	 * @return true if this region is a RentRegion otherwise false
	 */
	public boolean isRentRegion() {
		return this instanceof RentRegion;
	}
	
	/**
	 * Check if this region is a BuyRegion
	 * @return true if this region is a BuyRegion otherwise false
	 */
	public boolean isBuyRegion() {
		return this instanceof BuyRegion;
	}
	
	
	/**
	 * Update the signs connected to this region
	 * @return true if the update was successful, otherwise false
	 */
	public boolean updateSigns() {
		boolean result = false;
		if(getSignLocation() == null || getSignLocation().getWorld() == null) {
			return false;
		}
		Block block = getSignLocation().getBlock();			
		if(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
			Sign sign = (Sign)block.getState();
			String[] lines = getSignLines();
			for(int i=0; i<lines.length; i++) {
				sign.setLine(i, lines[i]);
			}
			sign.update();
			result = true;
		}		
		return result;
	}
	
	/**
	 * Get the lines for the sign that represent the current state of the region
	 * @return Array of lines that the sign should have
	 */
	public abstract String[] getSignLines();

	public abstract void updateRegionFlags();
	
	/**
	 * Change the restore setting
	 * @param restore true, false or general
	 */
	public void setRestoreSetting(String restore) {
		setSetting("enableRestore", restore);
	}
	
	/**
	 * Change the restore profile
	 * @param profile default or the name of the profile as set in the config
	 */
	public void setRestoreProfile(String profile) {
		setSetting("restoreProfile", profile);
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
		EditSession editSession = new EditSession(new BukkitWorld(getSignLocation().getWorld()), plugin.config().getInt("maximumBlocks"));
		ProtectedRegion region = plugin.getWorldGuard().getRegionManager(getSignLocation().getWorld()).getRegion(getName());
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
		EditSession editSession = new EditSession(new BukkitWorld(getSignLocation().getWorld()), plugin.config().getInt("maximumBlocks"));
		ProtectedRegion region = plugin.getWorldGuard().getRegionManager(getSignLocation().getWorld()).getRegion(getName());
		if(region == null) {
			AreaShop.debug("Region '" + getName() + "' does not exist in WorldGuard, restore failed");
			return false;
		}
		// Get the origin and size of the region
		Vector origin = new Vector(region.getMinimumPoint().getBlockX(), region.getMinimumPoint().getBlockY(), region.getMinimumPoint().getBlockZ());
		
		// The path to save the schematic
		File saveFile = new File(plugin.getFileManager().getSchematicFolder() + File.separator + fileName + AreaShop.schematicExtension);
		
		LocalSession localSession = new LocalSession(plugin.getWorldEdit().getLocalConfiguration());
		editSession.enableQueue();
		try {
			localSession.setClipboard(SchematicFormat.MCEDIT.load(saveFile));
			localSession.getClipboard().place(editSession, origin, false);
		} catch (MaxChangedBlocksException | EmptyClipboardException | IOException | DataException e) {
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
	 * Set the region flags/options to the values of a ConfigurationSection
	 * @param player The player that does it
	 * @param region The region 
	 * @param flags
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected boolean setRegionFlags(ConfigurationSection flags, Map<String, String> valueReplacements) {
		boolean result = true;
		
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
		
		Iterator<String> it = flagNames.iterator();
		while(it.hasNext()) {
			String flagName = it.next();
			String value = flags.getString(flagName);
			
			// Apply replacements
			if(value != null) {
				for(String key : valueReplacements.keySet()) {
					if(valueReplacements.get(key) != null) {
						value = value.replace(key, valueReplacements.get(key));
					}
				}
			}
			
			/* Check for a couple of options or use as flag */
			if(flagName.equalsIgnoreCase("members")) {
				/* Split the string and parse all values */
				String[] names = value.split("\\s*,\\s*");
				DefaultDomain members = region.getMembers();
				for(int i=0; i<names.length; i++) {
					if(names[i].charAt(0) == '+') {
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
					if(names[i].charAt(0) == '+') {
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
		} catch (ProtectionDatabaseException e) {
			plugin.getLogger().info("Error: regions could not be saved");
		}
		return result;
	}
	
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
			setSetting("teleportLocation", null);
		} else {
			setSetting("teleportLocation", Utils.locationToConfig(location, true));
		}
		this.save();
	}
	
	/**
	 * Teleport a player to the region
	 * @param player Player that should be teleported
	 * @param regionName The name of the region the player should be teleported to
	 */
	public boolean teleportPlayer(Player player) {
		int checked = 1;
		boolean owner = false;
		Location startLocation = null;
		ProtectedRegion region = getRegion();
		if(this.hasTeleportLocation()) {
			startLocation = getTeleportLocation();
		}
		if(isRentRegion()) {
			owner = player.getUniqueId().equals(((RentRegion)this).getRenter());
		} else {
			owner = player.getUniqueId().equals(((BuyRegion)this).getBuyer());
		}
		// Check permissions
		if(!player.hasPermission("areashop.teleport")) {
			plugin.message(player, "teleport-noPermission");
			return false;
		} else if(!owner && !player.hasPermission("areashop.teleportall")) {
			plugin.message(player, "teleport-noPermissionOther");
			return false;
		}
		// Set default startLocation if not set
		if(startLocation == null) {
			if(region != null) {
				// Set to block in the middle, y configured in the config
				Vector middle = Vector.getMidpoint(region.getMaximumPoint(), region.getMinimumPoint());
				String configSetting = plugin.config().getString("teleportLocationY");
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
		
		// set location in the center of the block
		startLocation.setX(startLocation.getBlockX() + 0.5);
		startLocation.setZ(startLocation.getBlockZ() + 0.5);
		
		// Check locations starting from startLocation and then a cube that increases
		// radius around that (until no block in the region is found at all cube sides)
		Location saveLocation = startLocation;
		int radius = 1;
		boolean blocksInRegion = region.contains(startLocation.getBlockX(), startLocation.getBlockY(), startLocation.getBlockZ());
		boolean done = isSave(saveLocation) && blocksInRegion;
		boolean north=false, east=false, south=false, west=false, top=false, bottom=false;
		boolean track;
		while(blocksInRegion && !done) {
			blocksInRegion = false;
			// North side
			track = false;
			for(int x=-radius+1; x<=radius && !done && !north; x++) {
				for(int y=-radius+1; y<radius && !done; y++) {
					saveLocation = startLocation.clone().add(x, y, -radius);
					if(region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) {
						done = isSave(saveLocation);
						blocksInRegion = true;
						track = true;
					}
					checked++;
				}
			}
			north = north || !track;
			
			// East side
			track = false;
			for(int z=-radius+1; z<=radius && !done && !east; z++) {
				for(int y=-radius+1; y<radius && !done; y++) {
					saveLocation = startLocation.clone().add(radius, y, z);
					if(region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) {
						done = isSave(saveLocation);
						blocksInRegion = true;
						track = true;
					}
					checked++;
				}
			}
			east = east || !track;
			
			// South side
			track = false;
			for(int x=radius-1; x>=-radius && !done && !south; x--) {
				for(int y=-radius+1; y<radius && !done; y++) {
					saveLocation = startLocation.clone().add(x, y, radius);
					if(region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) {
						done = isSave(saveLocation);
						blocksInRegion = true;
						track = true;
					}
					checked++;
				}
			}
			south = south || !track;
			
			// West side
			track = false;
			for(int z=radius-1; z>=-radius && !done && !west; z--) {
				for(int y=-radius+1; y<radius && !done; y++) {
					saveLocation = startLocation.clone().add(-radius, y, z);
					
					if(region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) {
						done = isSave(saveLocation);
						blocksInRegion = true;
						track = true;
					}
					AreaShop.debug(saveLocation.toString());
					checked++;
				}
			}
			west = west || !track;
			
			// Top side
			track = false;
			// Middle block of the top
			if(!done && !top) {
				saveLocation = startLocation.clone().add(0, radius, 0);
				if(region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) {
					done = isSave(saveLocation);
					blocksInRegion = true;
					track = true;
				}
				checked++;
			}
			for(int r=1; r<=radius && !done && !top; r++) {
				// North
				for(int x=-r+1; x<=r && !done; x++) {
					saveLocation = startLocation.clone().add(x, radius, -r);
					if(region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) {
						done = isSave(saveLocation);
						blocksInRegion = true;
						track = true;
					}
					checked++;
				}
				// East
				for(int z=-r+1; z<=r && !done; z++) {
					saveLocation = startLocation.clone().add(r, radius, z);
					if(region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) {
						done = isSave(saveLocation);
						blocksInRegion = true;
						track = true;
					}
					checked++;
				}
				// South side
				for(int x=r-1; x>=-r && !done; x--) {
					saveLocation = startLocation.clone().add(x, radius, r);
					if(region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) {
						done = isSave(saveLocation);
						blocksInRegion = true;
						track = true;
					}
					checked++;
				}
				// West side
				for(int z=r-1; z>=-r && !done; z--) {
					saveLocation = startLocation.clone().add(-r, radius, z);
					if(region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) {
						done = isSave(saveLocation);
						blocksInRegion = true;
						track = true;
					}			
					checked++;
				}			
			}
			top = top || !track;
			
			// Bottom side
			track = false;
			// Middle block of the bottom
			if(!done && !bottom) {
				saveLocation = startLocation.clone().add(0, -radius, 0);
				if(region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) {
					done = isSave(saveLocation);
					blocksInRegion = true;
					track = true;
				}
				checked++;
			}
			for(int r=1; r<=radius && !done && !bottom; r++) {
				// North
				for(int x=-r+1; x<=r && !done; x++) {
					saveLocation = startLocation.clone().add(x, -radius, -r);
					if(region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) {
						done = isSave(saveLocation);
						blocksInRegion = true;
						track = true;
					}
					checked++;
				}
				// East
				for(int z=-r+1; z<=r && !done; z++) {
					saveLocation = startLocation.clone().add(r, -radius, z);
					if(region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) {
						done = isSave(saveLocation);
						blocksInRegion = true;
						track = true;
					}
					checked++;
				}
				// South side
				for(int x=r-1; x>=-r && !done; x--) {
					saveLocation = startLocation.clone().add(x, -radius, r);
					if(region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) {
						done = isSave(saveLocation);
						blocksInRegion = true;
						track = true;
					}
					checked++;
				}
				// West side
				for(int z=r-1; z>=-r && !done; z--) {
					saveLocation = startLocation.clone().add(-r, -radius, z);
					if(region.contains(saveLocation.getBlockX(), saveLocation.getBlockY(), saveLocation.getBlockZ())) {
						done = isSave(saveLocation);
						blocksInRegion = true;
						track = true;
					}
					checked++;
				}			
			}
			bottom = bottom || !track;
			
			// Increase cube radius
			radius++;
		}
		if(done) {
			plugin.message(player, "teleport-success", getName());
			player.teleport(saveLocation);
			AreaShop.debug("Found location: " + saveLocation.toString() + " Tries: " + checked);
			return true;
		} else {
			plugin.message(player, "teleport-noSafe", getName());
			AreaShop.debug("No location found, checked " + checked + " spots");
			return false;
		}	
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
		if(isRentRegion()) {
			path = "rent." + path;
		} else {
			path = "buy." + path;
		}
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
	
	public double getDoubleSetting(String path) {
		if(isRentRegion()) {
			path = "rent." + path;
		} else {
			path = "buy." + path;
		}
		if(config.isDouble(path)) {
			return config.getDouble(path);
		}
		double result = 0;
		int priority = Integer.MIN_VALUE;
		boolean found = false;
		for(RegionGroup group : plugin.getFileManager().getGroups()) {
			if(group.isMember(this) && group.getSettings().isBoolean(path) && group.getPriority() > priority) {
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
		if(isRentRegion()) {
			path = "rent." + path;
		} else {
			path = "buy." + path;
		}
		if(config.isLong(path)) {
			return config.getLong(path);
		}
		long result = 0;
		int priority = Integer.MIN_VALUE;
		boolean found = false;
		for(RegionGroup group : plugin.getFileManager().getGroups()) {
			if(group.isMember(this) && group.getSettings().isBoolean(path) && group.getPriority() > priority) {
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
		if(isRentRegion()) {
			path = "rent." + path;
		} else {
			path = "buy." + path;
		}
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
	
	public void setSetting(String path, Object value) {
		if(isRentRegion()) {
			path = "rent." + path;
		} else {
			path = "buy." + path;
		}
		config.set(path, value);
	}
	

}
















