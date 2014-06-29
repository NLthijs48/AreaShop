package nl.evolutioncoding.AreaShop.regions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import nl.evolutioncoding.AreaShop.AreaShop;
import nl.evolutioncoding.AreaShop.FileManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;

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
	
	private String name = null;
	private Location signLocation = null;
	private Location teleportLocation = null;
	private String restoreSetting = null;
	private String restoreProfile = null;
	private static ArrayList<Material> canSpawnIn  = new ArrayList<Material>(Arrays.asList(Material.WOOD_DOOR, Material.WOODEN_DOOR, Material.SIGN_POST, Material.WALL_SIGN, Material.STONE_PLATE, Material.IRON_DOOR_BLOCK, Material.WOOD_PLATE, Material.TRAP_DOOR, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.DRAGON_EGG, Material.GOLD_PLATE, Material.IRON_PLATE));
	private static ArrayList<Material> cannotSpawnOn = new ArrayList<Material>(Arrays.asList(Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE, Material.SIGN_POST, Material.WALL_SIGN, Material.STONE_PLATE, Material.IRON_DOOR_BLOCK, Material.WOOD_PLATE, Material.TRAP_DOOR, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.CACTUS, Material.IRON_FENCE, Material.FENCE_GATE, Material.THIN_GLASS, Material.NETHER_FENCE, Material.DRAGON_EGG, Material.GOLD_PLATE, Material.IRON_PLATE, Material.STAINED_GLASS_PANE));
	private static ArrayList<Material> cannotSpawnBeside = new ArrayList<Material>(Arrays.asList(Material.LAVA, Material.STATIONARY_LAVA, Material.CACTUS));
	protected AreaShop plugin = null;

	public GeneralRegion(AreaShop plugin, Map<String, String> settings) {
		this.plugin = plugin;
		
		name = settings.get(AreaShop.keyName);
		signLocation = new Location(Bukkit.getWorld(settings.get(AreaShop.keyWorld)), Integer.parseInt(settings.get(AreaShop.keyX)), Integer.parseInt(settings.get(AreaShop.keyY)), Integer.parseInt(settings.get(AreaShop.keyZ)));
		if(settings.get(AreaShop.keyTPX) != null) {
			teleportLocation = new Location(Bukkit.getWorld(settings.get(AreaShop.keyWorld)), Double.parseDouble(settings.get(AreaShop.keyTPX)), Double.parseDouble(settings.get(AreaShop.keyTPY)), Double.parseDouble(settings.get(AreaShop.keyTPZ)), Float.parseFloat(settings.get(AreaShop.keyTPYaw)), Float.parseFloat(settings.get(AreaShop.keyTPPitch)));
		}
		restoreSetting = settings.get(AreaShop.keyRestore);
		restoreProfile = settings.get(AreaShop.keySchemProfile);
	}
	
	public GeneralRegion(AreaShop plugin, String name, Location signLocation) {
		this.plugin = plugin;
		
		this.name = name;
		this.signLocation = signLocation;
		restoreSetting = "general";
		restoreProfile = "default";
	}
	
	public HashMap<String, String> toMap() {
		HashMap<String, String> result = new HashMap<String, String>();
		// Basic info
		result.put(AreaShop.keyName, this.getName());
		result.put(AreaShop.keyWorld, getSignLocation().getWorld().getName());
		result.put(AreaShop.keyX, String.valueOf(getSignLocation().getBlockX()));
		result.put(AreaShop.keyY, String.valueOf(getSignLocation().getBlockY()));
		result.put(AreaShop.keyZ, String.valueOf(getSignLocation().getBlockZ()));
		// Teleport location
		if(hasTeleportLocation()) {
			result.put(AreaShop.keyTPX, String.valueOf(getTeleportLocation().getX()));
			result.put(AreaShop.keyTPY, String.valueOf(getTeleportLocation().getY()));
			result.put(AreaShop.keyTPZ, String.valueOf(getTeleportLocation().getZ()));
			result.put(AreaShop.keyTPYaw, String.valueOf(getTeleportLocation().getYaw()));
			result.put(AreaShop.keyTPPitch, String.valueOf(getTeleportLocation().getPitch()));
		}
		// Schematic Saving/Restoring
		result.put(AreaShop.keyRestore, restoreSetting);
		result.put(AreaShop.keySchemProfile, restoreProfile);
		
		return result;
	}
	
	
	
	// GETTERS
	public Location getSignLocation() {
		return signLocation;
	}
	
	public Location getTeleportLocation() {
		return teleportLocation;
	}
	
	public String getName() {
		return name;
	}
	
	public String getRestoreSetting() {
		return restoreSetting;
	}
	
	public String getRestoreProfile() {
		return restoreProfile;
	}
	
	public String getWorldName() {
		return getSignLocation().getWorld().getName();
	}
	
	public World getWorld() {
		return getSignLocation().getWorld();
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
		return this.teleportLocation != null;
	}
	
	
	/**
	 * Update the signs connected to this region
	 * @return true if the update was successful, otherwise false
	 */
	public boolean updateSigns() {
		boolean result = false;
		if(signLocation.getWorld() == null) {
			return false;
		}
		Block block = signLocation.getBlock();			
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
		this.restoreSetting = restore;
	}
	
	/**
	 * Change the restore profile
	 * @param profile default or the name of the profile as set in the config
	 */
	public void setRestoreProfile(String profile) {
		this.restoreProfile = profile;
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
							region.setFlag((StringFlag)flagType, (String)flagValue);
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
	
	/**
	 * Save this region to the disk
	 */
	public abstract void save();
	
	
	/**
	 * 
	 * @param regionName
	 * @param location
	 */
	
	/*
	public void setTeleport(String regionName, Location location, boolean isRent) {
		HashMap<String, String> info;
		if(isRent) {
			info = this.getRent(regionName);
		} else {
			info = this.getBuy(regionName);
		}
		if(info != null) {
			if(location == null) {
				HashMap<String, String> rent, buy;
				rent = this.getRent(regionName);
				buy = this.getBuy(regionName);
				if(rent != null) {
					rent.remove(AreaShop.keyTPX);
					rent.remove(AreaShop.keyTPY);
					rent.remove(AreaShop.keyTPZ);
					rent.remove(AreaShop.keyTPPitch);
					rent.remove(AreaShop.keyTPYaw);
				}
				if(buy != null) {
					buy.remove(AreaShop.keyTPX);
					buy.remove(AreaShop.keyTPY);
					buy.remove(AreaShop.keyTPZ);
					buy.remove(AreaShop.keyTPPitch);
					buy.remove(AreaShop.keyTPYaw);
				}
			} else {
				info.put(AreaShop.keyTPX, String.valueOf(location.getX()));
				info.put(AreaShop.keyTPY, String.valueOf(location.getY()));
				info.put(AreaShop.keyTPZ, String.valueOf(location.getZ()));
				info.put(AreaShop.keyTPPitch, String.valueOf(location.getPitch()));
				info.put(AreaShop.keyTPYaw, String.valueOf(location.getYaw()));
			}
			this.saveRents();
		}
	}
	*/
	
	/**
	 * Teleport a player to the region
	 * @param player Player that should be teleported
	 * @param regionName The name of the region the player should be teleported to
	 */
	/*
	public boolean teleportPlayer(Player player) {
		int checked = 1;
		boolean owner = false;
		HashMap<String, String> rent = this.getRent(regionName);
		HashMap<String, String> buy = this.getBuy(regionName);
		Location startLocation = null;
		String world = null;
		ProtectedRegion region = null;
		if(rent != null) {
			world = rent.get(AreaShop.keyWorld);
			region = plugin.getWorldGuard().getRegionManager(Bukkit.getWorld(world)).getRegion(regionName);
			if(rent.get(AreaShop.keyTPX) != null && rent.get(AreaShop.keyTPY) != null && rent.get(AreaShop.keyTPZ) != null && rent.get(AreaShop.keyTPPitch) != null && rent.get(AreaShop.keyTPYaw) != null) {
				startLocation = new Location(
						Bukkit.getWorld(rent.get(AreaShop.keyWorld)), 
						Double.parseDouble(rent.get(AreaShop.keyTPX)), 
						Double.parseDouble(rent.get(AreaShop.keyTPY)), 
						Double.parseDouble(rent.get(AreaShop.keyTPZ)), 
						Float.parseFloat(rent.get(AreaShop.keyTPYaw)), 
						Float.parseFloat(rent.get(AreaShop.keyTPPitch)));
			}
			owner = player.getUniqueId().toString().equals(rent.get(AreaShop.keyPlayerUUID));
			if(buy != null) {
				owner = owner || player.getUniqueId().toString().equals(rent.get(AreaShop.keyPlayerUUID));
			}
		} else if(buy != null) {
			world = buy.get(AreaShop.keyWorld);
			region = plugin.getWorldGuard().getRegionManager(Bukkit.getWorld(world)).getRegion(regionName);
			if(buy.get(AreaShop.keyTPX) != null && buy.get(AreaShop.keyTPY) != null && buy.get(AreaShop.keyTPZ) != null && buy.get(AreaShop.keyTPPitch) != null && buy.get(AreaShop.keyTPYaw) != null) {
				startLocation = new Location(
						Bukkit.getWorld(buy.get(AreaShop.keyWorld)), 
						Double.parseDouble(buy.get(AreaShop.keyTPX)), 
						Double.parseDouble(buy.get(AreaShop.keyTPY)), 
						Double.parseDouble(buy.get(AreaShop.keyTPZ)), 
						Float.parseFloat(buy.get(AreaShop.keyTPYaw)), 
						Float.parseFloat(buy.get(AreaShop.keyTPPitch)));
				
			}
			owner = player.getUniqueId().toString().equals(buy.get(AreaShop.keyPlayerUUID));
		} else {
			plugin.message(player, "teleport-noRentOrBuy", regionName);
			return false;
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
			region = plugin.getWorldGuard().getRegionManager(Bukkit.getWorld(world)).getRegion(regionName);
			if(region != null) {
				// Set to block in the middel, y configured in the config
				Vector middle = Vector.getMidpoint(region.getMaximumPoint(), region.getMinimumPoint());
				String configSetting = plugin.config().getString("teleportLocationY");
				if("bottom".equalsIgnoreCase(configSetting)) {
					middle = middle.setY(region.getMinimumPoint().getBlockY());
				} else if("top".equalsIgnoreCase(configSetting)) {
					middle = middle.setY(region.getMaximumPoint().getBlockY());
				} else {
					middle = middle.setY(middle.getBlockY());
				}
				startLocation = new Location(Bukkit.getWorld(world), middle.getX(), middle.getY(), middle.getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
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
			plugin.message(player, "teleport-success", regionName);
			player.teleport(saveLocation);
			AreaShop.debug("Found location: " + saveLocation.toString() + " Tries: " + checked);
			return true;
		} else {
			plugin.message(player, "teleport-noSafe", regionName);
			AreaShop.debug("No location found, checked " + checked + " spots");
			return false;
		}	
	}
	*/
	
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
	

}















