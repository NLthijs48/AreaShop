package nl.evolutioncoding.AreaShop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import net.milkbowl.vault.economy.EconomyResponse;
import net.minecraft.util.org.apache.commons.io.FileUtils;
import nl.evolutioncoding.AreaShop.AreaShop.RegionEventType;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
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
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;
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

public class FileManager {
	private AreaShop plugin = null;
	private ObjectInputStream input = null;
	private ObjectOutputStream output = null;
	private HashMap<String,HashMap<String,String>> rents = null;
	private HashMap<String,HashMap<String,String>> buys = null;
	private HashMap<String,Integer> versions = null;
	private ArrayList<Material> canSpawnIn;
	private ArrayList<Material> cannotSpawnOn;
	private String rentPath = null;
	private String buyPath = null;
	private String versionPath = null;
	private String schemFolder = null;
	
	/**
	 * Constructor, initialize variabeles
	 * @param plugin
	 */
	public FileManager(AreaShop plugin) {
		this.plugin = plugin;
		rents = new HashMap<>();
		buys = new HashMap<>();
		rentPath = plugin.getDataFolder().getPath() + File.separator + AreaShop.rentsFile;
		buyPath = plugin.getDataFolder().getPath() + File.separator + AreaShop.buysFile;
		versionPath = plugin.getDataFolder().getPath() + File.separator + AreaShop.versionFile;
		schemFolder = plugin.getDataFolder() + File.separator + AreaShop.schematicFolder;
		canSpawnIn  = new ArrayList<Material>(Arrays.asList(Material.WOOD_DOOR, Material.WOODEN_DOOR, Material.SIGN_POST, Material.WALL_SIGN, Material.STONE_PLATE, Material.IRON_DOOR_BLOCK, Material.WOOD_PLATE, Material.TRAP_DOOR, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.DRAGON_EGG, Material.GOLD_PLATE, Material.IRON_PLATE));
		cannotSpawnOn = new ArrayList<Material>(Arrays.asList(Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE, Material.SIGN_POST, Material.WALL_SIGN, Material.STONE_PLATE, Material.IRON_DOOR_BLOCK, Material.WOOD_PLATE, Material.TRAP_DOOR, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.CACTUS, Material.IRON_FENCE, Material.FENCE_GATE, Material.THIN_GLASS, Material.NETHER_FENCE, Material.DRAGON_EGG, Material.GOLD_PLATE, Material.IRON_PLATE, Material.STAINED_GLASS_PANE));
		File schemFile = new File(schemFolder);
		if(!schemFile.exists()) {
			schemFile.mkdirs();
		}
	}
	
	/**
	 * Add a rent to the list
	 * @param regionName Name of the region that can be rented
	 * @param rent Map containing all the info for a rent
	 */
	public void addRent(String regionName, HashMap<String,String> rent) {
		rents.put(regionName.toLowerCase(), rent);
		this.saveRents();
	}
	
	/**
	 * Add a buy to the list
	 * @param regionName Name of the region that can be buyed
	 * @param buy Map containing all the info for a buy
	 */
	public void addBuy(String regionName, HashMap<String,String> buy) {
		buys.put(regionName.toLowerCase(), buy);
		this.saveBuys();
	}
	
	/**
	 * 
	 * @param regionName
	 * @param location
	 */
	public void setTeleport(String regionName, Location location, boolean rent) {
		HashMap<String, String> info;
		if(rent) {
			info = this.getRent(regionName);
		} else {
			info = this.getBuy(regionName);
		}
		if(info != null) {
			info.put(AreaShop.keyTPX, String.valueOf(location.getX()));
			info.put(AreaShop.keyTPY, String.valueOf(location.getY()));
			info.put(AreaShop.keyTPZ, String.valueOf(location.getZ()));
			info.put(AreaShop.keyTPPitch, String.valueOf(location.getPitch()));
			info.put(AreaShop.keyTPYaw, String.valueOf(location.getYaw()));
			this.saveRents();
		}
	}
	
	/**
	 * Teleport a player to the region
	 * @param player Player that should be teleported
	 * @param regionName The name of the region the player should be teleported to
	 */
	public boolean teleportToRegion(Player player, String regionName) {
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
				// Set to lowest block in the middle
				Vector middle = Vector.getMidpoint(region.getMaximumPoint(), region.getMinimumPoint());
				middle = middle.setY(region.getMinimumPoint().getY());
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
		plugin.debug(startLocation.toString());
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
					checked++;
				}
			}
			west = west || !west;
			
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
			plugin.debug("Found location: " + saveLocation.toString() + " Tries: " + checked);
			return true;
		} else {
			plugin.message(player, "teleport-noSafe", regionName);
			plugin.debug("No location found, checked " + checked + " spots");
			return false;
		}	
	}
	
	/**
	 * Checks if a certain location is safe to teleport to
	 * @param location The location to check
	 * @return true if it is safe, otherwise false
	 */
	public boolean isSave(Location location) {
		Block feet = location.getBlock();
		Block head = feet.getRelative(BlockFace.UP);
		Block below = feet.getRelative(BlockFace.DOWN);
		// Check the block at the feet of the player
		if((feet.getType().isSolid() && !canSpawnIn.contains(feet.getType())) || feet.isLiquid()) {
			return false;
		} else if((head.getType().isSolid() && !canSpawnIn.contains(head.getType())) || head.isLiquid()) {
			return false;
		} else if(!below.getType().isSolid() || cannotSpawnOn.contains(below.getType()) || below.isLiquid()) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * Checks an event and handles saving to and restoring from schematic for it
	 * @param regionName The name of the region for which this is the event
	 * @param isRent Is it a rent or niet?
	 * @param type The type of event
	 */
	public void handleSchematicEvent(String regionName, boolean isRent, RegionEventType type) {
		// Check for the general killswitch
		if(!plugin.config().getBoolean("enableSchematics")) {
			return;
		}		
		// Get the info about the region
		HashMap<String, String> info;
		if(isRent) {
			info = this.getRent(regionName);
		} else {
			info = this.getBuy(regionName);
		}
		// Check the individual options
		if(isRent) {
			if("false".equalsIgnoreCase(info.get(AreaShop.keyRestore))) {
				return;
			} else if("true".equalsIgnoreCase(info.get(AreaShop.keyRestore))) {
			} else {
				if(!plugin.config().getBoolean("useRentRestore")) {
					return;
				}
			}
		} else {
			if("false".equalsIgnoreCase(info.get(AreaShop.keyRestore))) {
				return;
			} else if("true".equalsIgnoreCase(info.get(AreaShop.keyRestore))) {
			} else {
				if(!plugin.config().getBoolean("useBuyRestore")) {
					return;
				}
			}
		}
		// Get the safe and restore names
		String save = null;
		String restore = null;				
		if(type == RegionEventType.CREATED) {
			if(isRent) {
				save = plugin.config().getString("rentSchematicProfiles."+info.get(AreaShop.keySchemProfile)+".created.save");
				if(save == null) {
					plugin.config().getString("rentSchematicProfiles.default.created.save");
				}
				restore = plugin.config().getString("rentSchematicProfiles."+info.get(AreaShop.keySchemProfile)+".created.restore");
				if(restore == null) {
					plugin.config().getString("rentSchematicProfiles.default.created.restore");
				}
			} else {
				save = plugin.config().getString("buySchematicProfiles."+info.get(AreaShop.keySchemProfile)+".created.save");
				if(save == null) {
					plugin.config().getString("buySchematicProfiles.default.created.save");
				}
				restore = plugin.config().getString("buySchematicProfiles."+info.get(AreaShop.keySchemProfile)+".created.restore");
				if(restore == null) {
					plugin.config().getString("buySchematicProfiles.default.created.restore");
				}
			}
		} else if(type == RegionEventType.DELETED) {
			if(isRent) {
				save = plugin.config().getString("rentSchematicProfiles."+info.get(AreaShop.keySchemProfile)+".deleted.save");
				if(save == null) {
					plugin.config().getString("rentSchematicProfiles.default.deleted.save");
				}
				restore = plugin.config().getString("rentSchematicProfiles."+info.get(AreaShop.keySchemProfile)+".deleted.restore");
				if(restore == null) {
					plugin.config().getString("rentSchematicProfiles.default.deleted.restore");
				}
			} else {
				save = plugin.config().getString("buySchematicProfiles."+info.get(AreaShop.keySchemProfile)+".deleted.save");
				if(save == null) {
					plugin.config().getString("buySchematicProfiles.default.deleted.save");
				}
				restore = plugin.config().getString("buySchematicProfiles."+info.get(AreaShop.keySchemProfile)+".deleted.restore");
				if(restore == null) {
					plugin.config().getString("buySchematicProfiles.default.deleted.restore");
				}
			}
		} else if(type == RegionEventType.BOUGHT) {
			if(isRent) {
				save = plugin.config().getString("rentSchematicProfiles."+info.get(AreaShop.keySchemProfile)+".rented.save");
				if(save == null) {
					plugin.config().getString("rentSchematicProfiles.default.rented.save");
				}
				restore = plugin.config().getString("rentSchematicProfiles."+info.get(AreaShop.keySchemProfile)+".rented.restore");
				if(restore == null) {
					plugin.config().getString("rentSchematicProfiles.default.rented.restore");
				}
			} else {
				save = plugin.config().getString("buySchematicProfiles."+info.get(AreaShop.keySchemProfile)+".bought.save");
				if(save == null) {
					plugin.config().getString("buySchematicProfiles.default.bought.save");
				}
				restore = plugin.config().getString("buySchematicProfiles."+info.get(AreaShop.keySchemProfile)+".bought.restore");
				if(restore == null) {
					plugin.config().getString("buySchematicProfiles.default.bought.restore");
				}
			}
		} else if(type == RegionEventType.SOLD) {
			if(isRent) {
				save = plugin.config().getString("rentSchematicProfiles."+info.get(AreaShop.keySchemProfile)+".unrented.save");
				if(save == null) {
					plugin.config().getString("rentSchematicProfiles.default.unrented.save");
				}
				restore = plugin.config().getString("rentSchematicProfiles."+info.get(AreaShop.keySchemProfile)+".unrented.restore");
				if(restore == null) {
					plugin.config().getString("rentSchematicProfiles.default.unrented.restore");
				}
			} else {
				save = plugin.config().getString("buySchematicProfiles."+info.get(AreaShop.keySchemProfile)+".sold.save");
				if(save == null) {
					plugin.config().getString("buySchematicProfiles.default.sold.save");
				}
				restore = plugin.config().getString("buySchematicProfiles."+info.get(AreaShop.keySchemProfile)+".sold.restore");
				if(restore == null) {
					plugin.config().getString("buySchematicProfiles.default.sold.restore");
				}
			}
		}
		// Save the region if needed
		if(save != null && save.length() != 0) {
			save = save.replace(AreaShop.tagRegionName, info.get(AreaShop.keyName));
			this.saveRegionBlocks(regionName, info.get(AreaShop.keyWorld), save);			
		}
		// Restore the region if needed
		if(restore != null && restore.length() != 0) {
			restore = restore.replace(AreaShop.tagRegionName, info.get(AreaShop.keyName));
			this.restoreRegionBlocks(regionName, info.get(AreaShop.keyWorld), restore);			
		}
	}
	
	/**
	 * Save all blocks in a region for restoring later
	 * @param regionName The name of the region
	 * @param world The world that it is in
	 * @param fileName The name of the file to save to (extension and folder will be added)
	 * @return
	 */
	public boolean saveRegionBlocks(String regionName, String world, String fileName) {
		regionName = regionName.toLowerCase();
		boolean result = true;
		EditSession editSession = new EditSession(new BukkitWorld(Bukkit.getWorld(world)), plugin.config().getInt("maximumBlocks"));
		ProtectedRegion region = plugin.getWorldGuard().getRegionManager(Bukkit.getWorld(world)).getRegion(regionName);
		if(region == null) {
			plugin.debug("Region '" + regionName + "' does not exist, save failed");
			return false;
		}
		
		// Get the origin and size of the region
		Vector origin = new Vector(region.getMinimumPoint().getBlockX(), region.getMinimumPoint().getBlockY(), region.getMinimumPoint().getBlockZ());
		Vector size = (new Vector(region.getMaximumPoint().getBlockX(), region.getMaximumPoint().getBlockY(), region.getMaximumPoint().getBlockZ()).subtract(origin)).add(new Vector(1,1,1));
		
		// The path to save the schematic
		File saveFile = new File(schemFolder + File.separator + fileName + AreaShop.schematicExtension);
		
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
			plugin.debug("Saved schematic for " + regionName);
		} else {
			plugin.debug("Not saved " + regionName);
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
	public boolean restoreRegionBlocks(String regionName, String world, String fileName) {
		regionName = regionName.toLowerCase();
		boolean result = true;
		EditSession editSession = new EditSession(new BukkitWorld(Bukkit.getWorld(world)), plugin.config().getInt("maximumBlocks"));
		ProtectedRegion region = plugin.getWorldGuard().getRegionManager(Bukkit.getWorld(world)).getRegion(regionName);
		if(region == null) {
			plugin.debug("Region '" + regionName + "' does not exist, restore failed");
			return false;
		}
		// Get the origin and size of the region
		Vector origin = new Vector(region.getMinimumPoint().getBlockX(), region.getMinimumPoint().getBlockY(), region.getMinimumPoint().getBlockZ());
		
		// The path to save the schematic
		File saveFile = new File(schemFolder + File.separator + fileName + AreaShop.schematicExtension);
		
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
			plugin.debug("Restored schematic for " + regionName);
		} else {
			plugin.debug("Not restored " + regionName);
		}
		return result;
	}
	
	/**
	 * Get a rent from the list
	 * @param regionName Name of the rent you want to get
	 * @return The Map with all the values from the rent
	 */
	public HashMap<String,String> getRent(String regionName) {
		return rents.get(regionName.toLowerCase());
	}
	
	/**
	 * Get a buy from the list
	 * @param regionName Name of the buy you want to get
	 * @return The Map with all the values from the buy
	 */
	public HashMap<String,String> getBuy(String regionName) {
		return buys.get(regionName.toLowerCase());
	}
	
	/**
	 * Get all rents
	 * @return The Map with all the values from the rents
	 */
	public HashMap<String,HashMap<String,String>> getRents() {
		return rents;
	}
	
	/**
	 * Get all buys
	 * @return The Map with all the values from the buys
	 */
	public HashMap<String,HashMap<String,String>> getBuys() {
		return buys;
	}
		
	/**
	 * Save all rents to disk
	 */
	public void saveRents() {
		try {
			output = new ObjectOutputStream(new FileOutputStream(rentPath));
			output.writeObject(rents);
			output.close();
		} catch (IOException e) {
			plugin.getLogger().info("File could not be saved: " + rentPath);
		}
		
	}
	
	/**
	 * Save all buys to disk
	 */
	public void saveBuys() {
		try {
			output = new ObjectOutputStream(new FileOutputStream(buyPath));
			output.writeObject(buys);
			output.close();
		} catch (IOException e) {
			plugin.getLogger().info("File could not be saved: " + buyPath);
		}
		
	}
	
	/**
	 * Unrent a region, reset to unrented
	 * @param regionName Region that should be unrented
	 */
	public void unRent(String regionName, boolean giveMoneyBack) {
		regionName = regionName.toLowerCase();
		HashMap<String,String> rent = rents.get(regionName);
	
		/* Get the time until the region will be rented */
		Long rentedUntil = Long.parseLong(rent.get(AreaShop.keyRentedUntil));
		Long currentTime = Calendar.getInstance().getTimeInMillis();
		Double timeLeft = (double) ((rentedUntil - currentTime));
		double price = Double.parseDouble(rent.get(AreaShop.keyPrice));
		double percentage = (plugin.config().getDouble("rentMoneyBack")) / 100.0;
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(0);

		String duration = rent.get(AreaShop.keyDuration);
		duration = duration.replace("month", "M");
		duration = duration.replace("months", "M");
		char durationChar = duration.charAt(duration.indexOf(' ')+1);
		int durationInt = Integer.parseInt(duration.substring(0, duration.indexOf(' ')));	
		
		if(durationChar == 'm') {
			calendar.add(Calendar.MINUTE, durationInt);
		} else if(durationChar == 'h') {
			calendar.add(Calendar.HOUR, durationInt);
		} else if(durationChar == 'd') {
			calendar.add(Calendar.DAY_OF_MONTH, durationInt);
		} else if(durationChar == 'M') {
			calendar.add(Calendar.MONTH, durationInt);
		} else if(durationChar == 'y') {
			calendar.add(Calendar.YEAR, durationInt);
		}
		Double timePeriod = (double) (calendar.getTimeInMillis());
		double periods = timeLeft / timePeriod;
		double moneyBack =  periods * price * percentage;
		if(moneyBack > 0 && giveMoneyBack) {
			/* Give back the money */
			EconomyResponse r = plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(UUID.fromString(rent.get(AreaShop.keyPlayerUUID))), moneyBack);
			if(!r.transactionSuccess()) {
				plugin.getLogger().info("Something went wrong with paying back money while unrenting");
			}	
		}
		
		this.handleSchematicEvent(regionName, true, RegionEventType.SOLD);
		
		/* Set the flags and options for the region */
		plugin.getFileManager().setRegionFlags(regionName, plugin.config().getConfigurationSection("flagsForRent"), true);
		
		/* Debug message */
		plugin.debug(plugin.toName(rent.get(AreaShop.keyPlayerUUID)) + " has unrented " + rent.get(AreaShop.keyName) + ", got " + plugin.formatCurrency(moneyBack) + " money back");
		
		/* Remove the player and renteduntil values */
		rent.remove(AreaShop.keyPlayerUUID);
		rent.remove(AreaShop.keyRentedUntil);
		this.addRent(regionName, rent);
		
		/* Change the sign to [Rentable] */
		this.updateRentSign(regionName);
		
		this.saveRents();
	}
	
	/**
	 * Sell a buyed region, get part of the money back
	 * @param regionName
	 */
	public void unBuy(String regionName, boolean giveMoneyBack) {
		regionName = regionName.toLowerCase();
		HashMap<String,String> buy = buys.get(regionName);
		
		/* Give part of the buying price back */
		double price = Double.parseDouble(buy.get(AreaShop.keyPrice));
		double percentage = plugin.config().getDouble("buyMoneyBack") / 100.0;
		double moneyBack =  price * percentage;
		if(moneyBack > 0 && giveMoneyBack) {
			/* Give back the money */
			EconomyResponse r = plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(UUID.fromString(buy.get(AreaShop.keyPlayerUUID))), moneyBack);
			if(!r.transactionSuccess()) {
				plugin.getLogger().info("Something went wrong with paying back money while unrenting");
			}	
		}
		
		this.handleSchematicEvent(regionName, false, RegionEventType.SOLD);
		
		/* Set the flags and options for the region */
		plugin.getFileManager().setRegionFlags(regionName, plugin.config().getConfigurationSection("flagsForSale"), false);
		
		/* Debug message */
		plugin.debug(plugin.toName(buy.get(AreaShop.keyPlayerUUID)) + " has sold " + buy.get(AreaShop.keyName) + ", got " + plugin.formatCurrency(moneyBack) + " money back");
		
		/* Remove the player and buyeduntil values */
		buy.remove(AreaShop.keyPlayerUUID);
		this.addBuy(regionName, buy);
		
		/* Change the sign to [Buyable] */
		this.updateBuySign(regionName);
		
		this.saveBuys();
	}
	
	/**
	 * Unrent region that have no time left
	 */
	public void checkRents() {
		/* Check if regions and signs are still present */
		Object[] rentNames = rents.keySet().toArray();			
		long now = Calendar.getInstance().getTimeInMillis();
		for(int i=0; i<rentNames.length; i++) {
			HashMap<String,String> rent = rents.get((String)rentNames[i]);
			String rentedUntil = rent.get(AreaShop.keyRentedUntil);
			if(rentedUntil != null) {
				long until = Long.parseLong(rentedUntil);
				if(now > until) {
					/* Send message to the player if online */
					Player player = Bukkit.getPlayer(UUID.fromString(rent.get(AreaShop.keyPlayerUUID)));
					if(player != null) {
						plugin.message(player, "unrent-expired", rent.get(AreaShop.keyName));
					}
					this.unRent(rent.get(AreaShop.keyName), true);
				}
			}
		}	
	}
	
	/**
	 * Rent a region
	 * @param player The player that wants to rent the region
	 * @param regionName The name of the region you want to rent
	 * @return true if it succeeded and false if not
	 */
	public boolean rent(Player player, String regionName) {
		regionName = regionName.toLowerCase();
		HashMap<String,String> rent = plugin.getFileManager().getRent(regionName);
		if(rent == null) {
			plugin.message(player, "rent-regionNotRentable");
			return false;
		}
		Block block = Bukkit.getWorld(rent.get(AreaShop.keyWorld)).getBlockAt(Integer.parseInt(rent.get(AreaShop.keyX)), Integer.parseInt(rent.get(AreaShop.keyY)), Integer.parseInt(rent.get(AreaShop.keyZ)));
		
		/* Check if the player has permission */
		if(player.hasPermission("areashop.rent")) {	
			boolean extend = false;
			if(rent.get(AreaShop.keyPlayerUUID) != null && UUID.fromString(rent.get(AreaShop.keyPlayerUUID)) != null && player.getUniqueId().equals(UUID.fromString(rent.get(AreaShop.keyPlayerUUID)))) {
				extend = true;
			}
			/* Check if the region is available for renting */
			if(rent.get(AreaShop.keyPlayerUUID) == null || extend) {	
				
				if(!extend) {
					/* Check if the player can still rent */
					int rentNumber = 0;
					Iterator<String> it = rents.keySet().iterator();
					while(it.hasNext()) {
						String next = it.next();
						if(rents.get(next).get(AreaShop.keyPlayerUUID) != null && player.getUniqueId().equals(UUID.fromString(rents.get(next).get(AreaShop.keyPlayerUUID)))) {
							rentNumber++;
						}
					}
					int buyNumber = 0;
					it = buys.keySet().iterator();
					while(it.hasNext()) {
						String next = it.next();
						if(buys.get(next).get(AreaShop.keyPlayerUUID) != null && player.getUniqueId().equals(UUID.fromString(buys.get(next).get(AreaShop.keyPlayerUUID)))) {
							buyNumber++;
						}
					}
					int maximumRents = Integer.parseInt(plugin.config().getString("maximumRents"));
					if(maximumRents != -1 && rentNumber >= maximumRents) {
						plugin.message(player, "rent-maximum", maximumRents);
						return false;
					}
					int maximumTotal = Integer.parseInt(plugin.config().getString("maximumTotal"));
					if(maximumTotal != -1 && (rentNumber+buyNumber) >= maximumTotal) {
						plugin.message(player, "total-maximum", maximumTotal);
						return false;
					}
				}				
				
				Double price = Double.parseDouble(rent.get(AreaShop.keyPrice));
				if(plugin.getEconomy().has(player, block.getWorld().getName(), price)) {
					Sign sign = (Sign)block.getState();
					
					/* Substract the money from the players balance */
					EconomyResponse r = plugin.getEconomy().withdrawPlayer(player, price);
					if(!r.transactionSuccess()) {
						plugin.message(player, "rent-payError");
						return false;
					}										
					
					/* Get the time until the region will be rented */
					Calendar calendar = Calendar.getInstance();
					if(extend) {
						calendar.setTimeInMillis(Long.parseLong(rent.get(AreaShop.keyRentedUntil)));
					}
			
					ArrayList<String> minutes = new ArrayList<String>(plugin.config().getStringList("minutes"));
					ArrayList<String> hours = new ArrayList<String>(plugin.config().getStringList("hours"));
					ArrayList<String> days = new ArrayList<String>(plugin.config().getStringList("days"));
					ArrayList<String> months = new ArrayList<String>(plugin.config().getStringList("months"));
					ArrayList<String> years = new ArrayList<String>(plugin.config().getStringList("years"));
					
					String duration = rent.get(AreaShop.keyDuration);
					String durationString = duration.substring(duration.indexOf(' ')+1, duration.length());
					int durationInt = Integer.parseInt(duration.substring(0, duration.indexOf(' ')));
					
					if(minutes.contains(durationString)) {
						calendar.add(Calendar.MINUTE, durationInt);
					} else if(hours.contains(durationString)) {
						calendar.add(Calendar.HOUR, durationInt);
					} else if(days.contains(durationString)) {
						calendar.add(Calendar.DAY_OF_MONTH, durationInt);
					} else if(months.contains(durationString)) {
						calendar.add(Calendar.MONTH, durationInt);
					} else if(years.contains(durationString)) {
						calendar.add(Calendar.YEAR, durationInt);
					}
					SimpleDateFormat dateFull = new SimpleDateFormat(plugin.config().getString("timeFormatChat"));
					
					/* Add values to the rent and send it to FileManager */
					rent.put(AreaShop.keyRentedUntil, String.valueOf(calendar.getTimeInMillis()));
					rent.put(AreaShop.keyPlayerUUID, player.getUniqueId().toString());
					plugin.getFileManager().addRent(sign.getLine(1), rent);
					
					if(!extend) {
						this.handleSchematicEvent(regionName, true, RegionEventType.BOUGHT);
					}
					
					/* Change the sign */
					this.updateRentSign(regionName);
					
					/* Set the flags and options for the region */
					plugin.getFileManager().setRegionFlags(regionName, plugin.config().getConfigurationSection("flagsRented"), true);
					
					/* Send message to the player */
					if(extend) {
						plugin.message(player, "rent-extended", sign.getLine(1), dateFull.format(calendar.getTime()));
					} else {
						plugin.message(player, "rent-rented", sign.getLine(1), dateFull.format(calendar.getTime()));
						plugin.message(player, "rent-extend");
					}
					plugin.debug(player.getName() + " has rented region " + rent.get(AreaShop.keyName) + " for " + plugin.formatCurrency(price) + " until " + dateFull.format(calendar.getTime()));
					
					this.saveRents();
					return true;
				} else {
					/* Player has not enough money */
					if(extend) {
						plugin.message(player, "rent-lowMoneyExtend", plugin.formatCurrency(plugin.getEconomy().getBalance(player, block.getWorld().getName())), plugin.formatCurrency(price));
					} else {
						plugin.message(player, "rent-lowMoneyRent", plugin.formatCurrency(plugin.getEconomy().getBalance(player, block.getWorld().getName())), plugin.formatCurrency(price));
					}
				}
			} else {
				plugin.message(player, "rent-someoneElse");			
			}	
		} else {
			plugin.message(player, "rent-noPermission");
		}
		return false;
	}
	
	
	/**
	 * Buy a region
	 * @param player The player that wants to buy the region
	 * @param regionName The name of the region you want to buy
	 * @return true if it succeeded and false if not
	 */
	public boolean buy(Player player, String regionName) {
		regionName = regionName.toLowerCase();
		HashMap<String,String> buy = plugin.getFileManager().getBuy(regionName);
		if(buy == null) {
			plugin.message(player, "rent-notRentable");
			return true;
		}
		Block block = Bukkit.getWorld(buy.get(AreaShop.keyWorld)).getBlockAt(Integer.parseInt(buy.get(AreaShop.keyX)), Integer.parseInt(buy.get(AreaShop.keyY)), Integer.parseInt(buy.get(AreaShop.keyZ)));
		
		/* Check if the player has permission */
		if(player.hasPermission("areashop.buy")) {	
			if(buy.get(AreaShop.keyPlayerUUID) == null) {					
	
				/* Check if the player can still buy */
				int buyNumber = 0;
				Iterator<String> it = buys.keySet().iterator();
				while(it.hasNext()) {
					String next = it.next();
					if(buys.get(next).get(AreaShop.keyPlayerUUID) != null && player.getUniqueId().equals(UUID.fromString(buys.get(next).get(AreaShop.keyPlayerUUID)))) {
						buyNumber++;
					}
				}
				int rentNumber = 0;
				it = rents.keySet().iterator();
				while(it.hasNext()) {
					String next = it.next();
					if(rents.get(next).get(AreaShop.keyPlayerUUID) != null && player.getUniqueId().equals(UUID.fromString(rents.get(next).get(AreaShop.keyPlayerUUID)))) {
						rentNumber++;
					}
				}
				int maximumBuys = Integer.parseInt(plugin.config().getString("maximumBuys"));
				if(maximumBuys != -1 && buyNumber >= maximumBuys) {
					plugin.message(player, "buy-maximum", maximumBuys);
					return false;
				}
				int maximumTotal = Integer.parseInt(plugin.config().getString("maximumTotal"));
				if(maximumTotal != -1 && (rentNumber+buyNumber) >= maximumTotal) {
					plugin.message(player, "total-maximum", maximumTotal);
					return false;
				}
				
				/* Check if the player has enough money */
				Double price = Double.parseDouble(buy.get(AreaShop.keyPrice));
				if(plugin.getEconomy().has(player, block.getWorld().getName(), price)) {
					Sign sign = (Sign)block.getState();
					
					/* Substract the money from the players balance */
					EconomyResponse r = plugin.getEconomy().withdrawPlayer(player, price);
					if(!r.transactionSuccess()) {
						plugin.message(player, "buy-payError");
						return false;
					}										
					
					/* Add values to the buy and send it to FileManager */
					buy.put(AreaShop.keyPlayerUUID, player.getUniqueId().toString());
					plugin.getFileManager().addBuy(sign.getLine(1), buy);
	
					this.handleSchematicEvent(regionName, false, RegionEventType.BOUGHT);
					
					/* Change the sign */
					this.updateBuySign(regionName);
					
					/* Set the flags and options for the region */
					plugin.getFileManager().setRegionFlags(regionName, plugin.config().getConfigurationSection("flagsSold"), false);
					
					/* Send message to the player */
					plugin.message(player, "buy-succes", sign.getLine(1));
					plugin.debug(player.getName() + " has bought region " + buy.get(AreaShop.keyName) + " for " + plugin.formatCurrency(price));
					
					this.saveBuys();
					return true;
				} else {
					/* Player has not enough money */
					plugin.message(player, "buy-lowMoney", plugin.formatCurrency(plugin.getEconomy().getBalance(player, block.getWorld().getName())), plugin.formatCurrency(price));
				}
			} else {
				if(player.getUniqueId().equals(UUID.fromString(buy.get(AreaShop.keyPlayerUUID)))) {
					plugin.message(player, "buy-yours");
				} else {
					plugin.message(player, "buy-someoneElse");
				}
			}	
		} else {
			plugin.message(player, "buy-noPermission");
		}
		return false;
	}
	
	/**
	 * Remove a rent from the list
	 * @param regionName
	 */
	public boolean removeRent(String regionName, boolean giveMoneyBack) {
		regionName = regionName.toLowerCase();
		boolean result = false;
		HashMap<String,String> rent = rents.get(regionName);
		if(rent != null) {
			if(rent.get(AreaShop.keyPlayerUUID) != null) {
				this.unRent(regionName, giveMoneyBack);
			}
			/* Delete the sign and the variable */
			if(Bukkit.getWorld(rent.get(AreaShop.keyWorld)) != null) {
				Bukkit.getWorld(rent.get(AreaShop.keyWorld)).getBlockAt(Integer.parseInt(rent.get(AreaShop.keyX)), Integer.parseInt(rent.get(AreaShop.keyY)), Integer.parseInt(rent.get(AreaShop.keyZ))).setType(Material.AIR);
			}
			rents.remove(regionName);
			this.saveRents();
			result = true;
		}		
		return result;
	}
	
	/**
	 * Remove a buy from the list
	 * @param regionName
	 */
	public boolean removeBuy(String regionName, boolean giveMoneyBack) {
		regionName = regionName.toLowerCase();
		boolean result = false;
		HashMap<String,String> buy = buys.get(regionName);
		if(buy != null) {
			if(buy.get(AreaShop.keyPlayerUUID) != null) {
				this.unBuy(regionName, giveMoneyBack);
			}
			/* Delete the sign and the variable */
			if(Bukkit.getWorld(buy.get(AreaShop.keyWorld)) != null) {
				Bukkit.getWorld(buy.get(AreaShop.keyWorld)).getBlockAt(Integer.parseInt(buy.get(AreaShop.keyX)), Integer.parseInt(buy.get(AreaShop.keyY)), Integer.parseInt(buy.get(AreaShop.keyZ))).setType(Material.AIR);
			}			
			buys.remove(regionName);
			this.saveBuys();
			result = true;
		}		
		return result;
	}
	
	/**
	 * Load the file with the versions, used to check if the other files need conversion
	 */
	@SuppressWarnings("unchecked")
	public void loadVersions() {
		File file = new File(versionPath);
		if(file.exists()) {
			/* Load versions from the file */
			try {
				input = new ObjectInputStream(new FileInputStream(versionPath));
		    	versions = (HashMap<String,Integer>)input.readObject();
				input.close();
			} catch (IOException | ClassNotFoundException | ClassCastException e) {
				plugin.getLogger().info("Error: Something went wrong reading file: " + versionPath);
				versions = null;
			}
		}
		if(versions == null || versions.size() == 0) {
			versions = new HashMap<String, Integer>();
			versions.put(AreaShop.versionRentKey, -1);
			versions.put(AreaShop.versionBuyKey, -1);
			this.saveVersions();
		}
	}
	
	/**
	 * Save the versions file to disk
	 */
	public void saveVersions() {
		if(!(new File(versionPath).exists())) {
			plugin.getLogger().info("versions file created, this should happen only after installing or upgrading the plugin");
		}
		try {
			output = new ObjectOutputStream(new FileOutputStream(versionPath));
			output.writeObject(versions);
			output.close();
		} catch (IOException e) {
			plugin.getLogger().info("File could not be saved: " + versionPath);
		}
	}
	
	/**
	 * Checks the version of all files and converts them if needed
	 */
	public void convertFiles() {
		this.loadVersions();
		
		/* Check if the rents file needs conversion */
		if(versions.get(AreaShop.versionRentKey) < AreaShop.versionRentCurrent) {
			/* Backup current files */
			try {
				FileUtils.copyFile(new File(rentPath), new File(rentPath + ".old"));
				FileUtils.copyFile(new File(buyPath), new File(buyPath + ".old"));
			} catch (IOException e) {
				plugin.getLogger().info("Could not create a backup of '" + rentPath + "' and '" + buyPath + "', check the file permissions (conversion to next version continues)");
			}

			/* Upgrade the rent to the latest version */
			if(versions.get(AreaShop.versionRentKey) < 0) {
				for(String rentName : rents.keySet()) {
					HashMap<String,String> rent = rents.get(rentName);
					/* Save the rentName in the hashmap and use a small caps rentName as key */
					if(rent.get(AreaShop.keyName) == null) {
						rent.put(AreaShop.keyName, rentName);
						this.removeRent(rentName, false);
						this.addRent(rentName.toLowerCase(), rent);
					}
					/* Save the default setting for region restoring */
					if(rent.get(AreaShop.keyRestore) == null) {
						rent.put(AreaShop.keyRestore, "general");
					}
					/* Save the default setting for the region restore profile */
					if(rent.get(AreaShop.keySchemProfile) == null) {
						rent.put(AreaShop.keySchemProfile, "default");
					}
					/* Change to version 0 */
					versions.put(AreaShop.versionRentKey, 0);
					this.saveVersions();
				}
				plugin.getLogger().info("Updated version of '" + AreaShop.rentsFile + "' from -1 to 0 (switch to using lowercase region names, adding default schematic enabling and profile)");
			}
			if(versions.get(AreaShop.versionRentKey) < 1) {
				plugin.getLogger().info("Starting UUID conversion of '" + AreaShop.rentsFile + "', could take a while");
				for(String rentName : rents.keySet()) {
					HashMap<String,String> rent = rents.get(rentName);
					if(rent.get(AreaShop.oldKeyPlayer) != null) {
						@SuppressWarnings("deprecation")  // Fake deprecation by Bukkit to inform developers, method will stay
						OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(rent.get(AreaShop.oldKeyPlayer));
						rent.put(AreaShop.keyPlayerUUID, offlinePlayer.getUniqueId().toString());		
						rent.remove(AreaShop.oldKeyPlayer);
					}					
					/* Change version to 1 */
					versions.put(AreaShop.versionRentKey, 1);
					this.saveVersions();
				}
				plugin.getLogger().info("Updated version of '" + AreaShop.rentsFile + "' from 0 to 1 (switch to UUID's for player identification)");
			}				
			this.saveRents();			
		}
		
		/* Check if the buys file needs conversion */
		if(versions.get(AreaShop.versionBuyKey) < AreaShop.versionBuyCurrent) {
			/* Upgrade the buy to the latest version */
			if(versions.get(AreaShop.versionBuyKey) < 0) {
				for(String buyName : buys.keySet()) {
					HashMap<String,String> buy = buys.get(buyName);
					/* Save the buyName in the hashmap and use small caps buyName as key */
					if(buy.get(AreaShop.keyName) == null) {
						buy.put(AreaShop.keyName, buyName);
						this.removeBuy(buyName, false);
						this.addBuy(buyName.toLowerCase(), buy);
					}
					/* Save the default setting for region restoring */
					if(buy.get(AreaShop.keyRestore) == null) {
						buy.put(AreaShop.keyRestore, "general");
					}
					/* Save the default setting for the region restore profile */
					if(buy.get(AreaShop.keySchemProfile) == null) {
						buy.put(AreaShop.keySchemProfile, "default");
					}
					versions.put(AreaShop.versionBuyKey, 0);
					this.saveVersions();		
				}
				plugin.getLogger().info("Updated version of '" + AreaShop.buysFile + "' from -1 to 0 (switch to using lowercase region names, adding default schematic enabling and profile)");
			}
			if(versions.get(AreaShop.versionBuyKey) < 1) {
				plugin.getLogger().info("Starting UUID conversion of '" + AreaShop.buysFile + "', could take a while");
				for(String buyName : buys.keySet()) {
					HashMap<String,String> buy = buys.get(buyName);
					if(buy.get(AreaShop.oldKeyPlayer) != null) {
						/* One time conversion so this method can just be used */
						@SuppressWarnings("deprecation")
						OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(buy.get(AreaShop.oldKeyPlayer));
						buy.put(AreaShop.keyPlayerUUID, offlinePlayer.getUniqueId().toString());		
						buy.remove(AreaShop.oldKeyPlayer);
					}					
					/* Change version to 1 */
					versions.put(AreaShop.versionBuyKey, 1);
					this.saveVersions();
				}
				plugin.getLogger().info("Updated version of '" + AreaShop.buysFile + "' from 0 to 1 (switch to UUID's for player identification)");
			}
	
			this.saveBuys();
		}		
	}	
	
	/**
	 * Loads the rents from disk
	 * @return true if the file is read successfully, else false
	 */
	@SuppressWarnings("unchecked")
	public boolean loadRents() {
		boolean error = false;
		rents.clear();
		File file = new File(rentPath);
		if(file.exists()) {
			/* Load all rents from file */
			try {
				input = new ObjectInputStream(new FileInputStream(rentPath));
		    	rents = (HashMap<String,HashMap<String,String>>)input.readObject();
				input.close();
			} catch (IOException | ClassNotFoundException | ClassCastException e) {
				plugin.getLogger().info("Error: Something went wrong reading file: " + rentPath);
				error = true;
			}
			
			if(!error) {	
				Object names[] = rents.keySet().toArray();
				/* Check if regions and signs are still present */		
				for(Object objectName : names) {
					String rentName = (String)objectName;
					HashMap<String,String> rent = rents.get(rentName);
					
					/* If region is gone delete the rent and the sign */
					if(Bukkit.getWorld(rent.get(AreaShop.keyWorld)) == null 
							|| plugin.getWorldGuard().getRegionManager(Bukkit.getWorld(rent.get(AreaShop.keyWorld))) == null 
							|| plugin.getWorldGuard().getRegionManager(Bukkit.getWorld(rent.get(AreaShop.keyWorld))).getRegion(rentName) == null) {
						this.removeRent(rentName, false);
						plugin.getLogger().info(rentName + " does not exist anymore, rent has been deleted");
					} else {
						/* If the sign is gone remove the rent */
						Block block = Bukkit.getWorld(rent.get(AreaShop.keyWorld)).getBlockAt(Integer.parseInt(rent.get(AreaShop.keyX)), Integer.parseInt(rent.get(AreaShop.keyY)), Integer.parseInt(rent.get(AreaShop.keyZ)));
						if(!(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST)) {
							/* remove the rent */
							if(this.removeRent(rentName, false)) {
								plugin.getLogger().info("Rent for " + rentName + " has been deleted, sign is not present");
							}
						}
					}	
				}	
				
				/* Output info to console */
				if(rents.keySet().size() == 1) {
					plugin.debug(rents.keySet().size() + " rent loaded");
				} else {
					plugin.debug(rents.keySet().size() + " rents loaded");
				}
			}
		} else {
			plugin.getLogger().info("New file for rents created, should only happen when starting for the first time");
		}
		this.saveRents();
		return !error;
	}
	
	/**
	 * Load the buys file from disk
	 * @return true if the file is read successfully, else false
	 */
	@SuppressWarnings("unchecked")
	public boolean loadBuys() {
		boolean error = false;
		buys.clear();
		File file = new File(buyPath);
		if(file.exists()) {
			/* Load all buys from file */
			try {
				input = new ObjectInputStream(new FileInputStream(buyPath));
		    	buys = (HashMap<String,HashMap<String,String>>)input.readObject();
				input.close();
			} catch (IOException | ClassNotFoundException | ClassCastException e) {
				plugin.getLogger().info("Error: Something went wrong reading file: " + buyPath);
				error = true;
			}
			
			if(!error) {
				Object names[] = buys.keySet().toArray();
				/* Check if regions and signs are still present */		
				for(Object objectName : names) {
					String buyName = (String)objectName;
					HashMap<String,String> buy = buys.get(buyName);
					
					/* If region is gone delete the buy and the sign */
					if(Bukkit.getWorld(buy.get(AreaShop.keyWorld)) == null 
							|| plugin.getWorldGuard().getRegionManager(Bukkit.getWorld(buy.get(AreaShop.keyWorld))) == null 
							|| plugin.getWorldGuard().getRegionManager(Bukkit.getWorld(buy.get(AreaShop.keyWorld))).getRegion(buyName) == null) {
						this.removeBuy(buyName, false);
						plugin.getLogger().info("Region '" + buyName + "' does not exist anymore, buy has been deleted");
					} else {
						/* If the sign is gone remove the buy */
						Block block = Bukkit.getWorld(buy.get(AreaShop.keyWorld)).getBlockAt(Integer.parseInt(buy.get(AreaShop.keyX)), Integer.parseInt(buy.get(AreaShop.keyY)), Integer.parseInt(buy.get(AreaShop.keyZ)));
						if(!(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST)) {
							/* remove the buy */
							if(this.removeBuy(buyName, false)) {
								plugin.getLogger().info("Buy for region '" + buyName + "' has been deleted, sign is not present");
							}
						}
					}					
				}
				
				/* Output info to console */
				if(buys.keySet().size() == 1) {
					plugin.debug(buys.keySet().size() + " buy loaded");
				} else {
					plugin.debug(buys.keySet().size() + " buys loaded");
				}
			}
		} else {
			plugin.getLogger().info("New file for buys created, should only happen when starting for the first time");
		}
		this.saveBuys();
		return !error;
	}
	
	/**
	 * Update the sign linked to the rent
	 * @param regionName The region name of wich the sign has to be updated
	 * @return true if it succeeded, false if not
	 */
	public boolean updateRentSign(String regionName) {
		regionName = regionName.toLowerCase();
		boolean result = false;
		HashMap<String,String> rent = this.getRent(regionName);
		if(rent !=  null) {
			/* Get values */
			String world = rent.get(AreaShop.keyWorld);
			String x = rent.get(AreaShop.keyX);
			String y = rent.get(AreaShop.keyY);
			String z = rent.get(AreaShop.keyZ);
			String duration = rent.get(AreaShop.keyDuration);
			String price = rent.get(AreaShop.keyPrice);
			String player = plugin.toName(rent.get(AreaShop.keyPlayerUUID));
			String until = rent.get(AreaShop.keyRentedUntil);
			String name = rent.get(AreaShop.keyName);
			if(Bukkit.getWorld(world) == null) {
				return false;
			}
			Block block = Bukkit.getWorld(world).getBlockAt(Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z));
			
			if(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
				Sign sign = (Sign)block.getState();
				if(player == null) {
					/* Not rented */
					sign.setLine(0, plugin.fixColors(plugin.config().getString("signRentable")));
					sign.setLine(1, name);
					sign.setLine(2, duration);
					sign.setLine(3, plugin.formatCurrency(price));

				} else {
					/* Rented */
					SimpleDateFormat date = new SimpleDateFormat(plugin.config().getString("timeFormatSign"));
					String dateString = date.format(new Date(Long.parseLong(until)));					

					sign.setLine(0, plugin.fixColors(plugin.config().getString("signRented")));
					sign.setLine(1, name);
					sign.setLine(2, player);
					sign.setLine(3, dateString);					
				}
				sign.update();
				result = true;
			}
		}
		return result;
	}	
	
	/**
	 * Update the sign linked to the buy
	 * @param regionName The region name of wich the sign has to be updated
	 * @return true if it succeeded, false if not
	 */
	public boolean updateBuySign(String regionName) {
		regionName = regionName.toLowerCase();
		boolean result = false;
		HashMap<String,String> buy = this.getBuy(regionName);
		if(buy !=  null) {
			/* Get values */
			String world = buy.get(AreaShop.keyWorld);
			String x = buy.get(AreaShop.keyX);
			String y = buy.get(AreaShop.keyY);
			String z = buy.get(AreaShop.keyZ);
			String price = buy.get(AreaShop.keyPrice);
			String player = plugin.toName(buy.get(AreaShop.keyPlayerUUID));
			String name = buy.get(AreaShop.keyName);
			if(Bukkit.getWorld(world) == null) {
				return false;
			}
			Block block = Bukkit.getWorld(world).getBlockAt(Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z));
			
			if(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
				Sign sign = (Sign)block.getState();
				if(player == null) {
					/* Not buyed */
					sign.setLine(0, plugin.fixColors(plugin.config().getString("signBuyable")));
					sign.setLine(1, name);
					sign.setLine(2, plugin.formatCurrency(price));

				} else {
					/* Buyed */	
					sign.setLine(0, plugin.fixColors(plugin.config().getString("signBuyed")));
					sign.setLine(1, name);
					sign.setLine(2, player);				
				}
				sign.update();
				result = true;
			} 
		}
		return result;
	}
	
	/**
	 * Update all rent signs
	 * @return true if all signs are updated, otherwise false
	 */
	public boolean updateRentSigns() {
		boolean result = true;
		
		Object[] rentNames = rents.keySet().toArray();			
		for(int i=0; i<rentNames.length; i++) {
			String name = (String)rentNames[i];
			result = result & this.updateRentSign(name);
		}
		
		return result;		
	}
	
	/**
	 * Update all buy signs
	 * @return true if all signs are updated, otherwise false
	 */
	public boolean updateBuySigns() {
		boolean result = true;
		
		Object[] buyNames = buys.keySet().toArray();			
		for(int i=0; i<buyNames.length; i++) {
			String name = (String)buyNames[i];
			result = result & this.updateBuySign(name);
		}
		
		return result;		
	}
	
	/**
	 * Update a region that is rented
	 * @param regionName Region that should be updated
	 */
	public void updateRentRegion(String regionName) {
		regionName = regionName.toLowerCase();
		HashMap<String,String> rent = this.getRent(regionName);
		if(rent != null) {
			if(rent.get(AreaShop.keyPlayerUUID) == null) {
				this.setRegionFlags(regionName, plugin.config().getConfigurationSection("flagsForRent"), true);
			} else {
				this.setRegionFlags(regionName, plugin.config().getConfigurationSection("flagsRented"), true);
			}
		}
	}
	
	
	/**
	 * Update all regions registered for renting
	 */
	public void updateRentRegions() {
		Object[] rentNames = rents.keySet().toArray();			
		for(int i=0; i<rentNames.length; i++) {
			this.updateRentRegion(((String)rentNames[i]));
		}
	}

	
	/**
	 * Update a region that is bought
	 * @param regionName Region that should be updated
	 */
	public void updateBuyRegion(String regionName) {
		regionName = regionName.toLowerCase();
		HashMap<String,String> buy = this.getBuy(regionName);
		if(buy != null) {
			if(buy.get(AreaShop.keyPlayerUUID) == null) {
				this.setRegionFlags(regionName, plugin.config().getConfigurationSection("flagsForSale"), false);
			} else {
				this.setRegionFlags(regionName, plugin.config().getConfigurationSection("flagsSold"), false);
			}
		}
	}
	
	/**
	 * Update all regions registered for buying
	 */
	public void updateBuyRegions() {
		Object[] buyNames = buys.keySet().toArray();			
		for(int i=0; i<buyNames.length; i++) {
			this.updateBuyRegion(((String)buyNames[i]));
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
	public boolean setRegionFlags(String regionName, ConfigurationSection flags, boolean isRent) {
		boolean result = true;
		regionName = regionName.toLowerCase();
		
		Set<String> flagNames = flags.getKeys(false);
		WorldGuardPlugin worldGuard = plugin.getWorldGuard();
		Flag<?> flagType = null;
		Object flagValue = null;		

		/* Replace tags with values, for example %player% to NLThijs48 */
		HashMap<String,String> info;
		if(isRent) {
			info = this.getRent(regionName);
		} else {
			info = this.getBuy(regionName);
		}
		if(info == null) {
			plugin.debug("Buy/rent '" + regionName + "' does not exist, setting flags failed");
			return false;
		}
		
		/* Get the region */
		if(Bukkit.getWorld(info.get(AreaShop.keyWorld)) == null
				|| plugin.getWorldGuard().getRegionManager(Bukkit.getWorld(info.get(AreaShop.keyWorld))) == null
				|| plugin.getWorldGuard().getRegionManager(Bukkit.getWorld(info.get(AreaShop.keyWorld))).getRegion(regionName) == null) {
			plugin.debug("Region '" + regionName + "' does not exist, setting flags failed");
			return false;
		}		
		ProtectedRegion region = plugin.getWorldGuard().getRegionManager(Bukkit.getWorld(info.get(AreaShop.keyWorld))).getRegion(regionName);


		String playerName = plugin.toName(info.get(AreaShop.keyPlayerUUID));
		String price = plugin.formatCurrency(info.get(AreaShop.keyPrice));
		String duration = info.get(AreaShop.keyDuration);
		String until = null;
		if(isRent && playerName != null) {
			SimpleDateFormat dateFull = new SimpleDateFormat(plugin.config().getString("timeFormatChat"));
			until = dateFull.format(Long.parseLong(info.get(AreaShop.keyRentedUntil)));
		}
		
		Iterator<String> it = flagNames.iterator();
		while(it.hasNext()) {
			String flagName = it.next();
			String value = flags.getString(flagName);
			
			if(value != null && playerName != null) {
				value = value.replace(AreaShop.tagPlayerName, playerName);
			}
			if(value != null) {
				value = value.replace(AreaShop.tagRegionName, info.get(AreaShop.keyName));
			}
			if(value != null && price != null) {
				value = value.replace(AreaShop.tagPrice, price);
			}
			if(value != null && duration != null) {
				value = value.replace(AreaShop.tagDuration, duration);
			}
			if(value != null && until != null) {
				value = value.replace(AreaShop.tagRentedUntil, until);
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
					plugin.debug("Flag set: " + flagName + " --> " + value);
				} catch(NumberFormatException e) {
					plugin.getLogger().info("The value of flag " + flagName + " is not a number");
					result = false;
				}
			} else if(flagName.equalsIgnoreCase("parent")) {
				ProtectedRegion parentRegion = worldGuard.getRegionManager(Bukkit.getWorld(info.get(AreaShop.keyWorld))).getRegion(value);
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
					plugin.debug("Region " + region.getId() + ", flag " + flagName + " --> " + value);
				} else {
					result = false;
				}
			}			
		}

		try {
			worldGuard.getRegionManager(Bukkit.getWorld(info.get(AreaShop.keyWorld))).save();
		} catch (ProtectionDatabaseException e) {
			plugin.getLogger().info("Error: regions could not be saved");
		}
		return result;
	}
}















