package nl.evolutioncoding.areashop.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.Utils;
import nl.evolutioncoding.areashop.regions.BuyRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion.RegionEvent;
import nl.evolutioncoding.areashop.regions.GeneralRegion.RegionType;
import nl.evolutioncoding.areashop.regions.RegionGroup;
import nl.evolutioncoding.areashop.regions.RentRegion;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class FileManager {
	private static FileManager instance = null;
	
	private AreaShop plugin = null;
	private ObjectInputStream input = null;
	private ObjectOutputStream output = null;
	private HashMap<String, GeneralRegion> regions = null;
	private String regionsPath = null;
	private HashMap<String, RegionGroup> groups = null;
	private String configPath = null;
	private YamlConfiguration config = null;
	private String groupsPath = null;
	private YamlConfiguration groupsConfig = null;
	private String defaultPath = null;
	private YamlConfiguration defaultConfig = null;
	private boolean saveGroupsRequired = false;
	private Set<String> worldRegionsRequireSaving;
	
	private HashMap<String,Integer> versions = null;
	private String versionPath = null;
	private String schemFolder = null;
	
	/* Enum for region types */
	public enum AddResult {		
		BLACKLISTED("blacklisted"),
		NOPERMISSION("nopermission"),
		ALREADYADDED("alreadyadded"),
		SUCCESS("success");
		
		private final String value;
		private AddResult(String value) {
			this.value = value;
		}
		public String getValue() {
			return value;
		}
	} 
	
	/**
	 * Constructor, initialize variabeles
	 * @param plugin
	 */
	public FileManager(AreaShop plugin) {
		this.plugin = plugin;
		regions = new HashMap<String, GeneralRegion>();
		regionsPath = plugin.getDataFolder() + File.separator + AreaShop.regionsFolder;
		configPath = plugin.getDataFolder() + File.separator + "config.yml";
		groups = new HashMap<String, RegionGroup>();
		groupsPath = plugin.getDataFolder() + File.separator + AreaShop.groupsFile;
		defaultPath = plugin.getDataFolder() + File.separator + AreaShop.defaultFile;
		versionPath = plugin.getDataFolder().getPath() + File.separator + AreaShop.versionFile;
		schemFolder = plugin.getDataFolder() + File.separator + AreaShop.schematicFolder;
		worldRegionsRequireSaving = new HashSet<String>();
		File schemFile = new File(schemFolder);
		if(!schemFile.exists()) {
			schemFile.mkdirs();
		}
		loadVersions();
	}
	
	public static FileManager getInstance() {
		if(instance == null) {
			instance = new FileManager(AreaShop.getInstance());
		}
		return instance;
	}
	
	
	//////////////////////////////////////////////////////////
	// GETTERS
	//////////////////////////////////////////////////////////
	
	public AreaShop getPlugin() {
		return plugin;
	}
	
	public String getSchematicFolder() {
		return schemFolder;
	}
	
	public RegionGroup getGroup(String name) {
		return groups.get(name.toLowerCase());
	}
	
	public Collection<RegionGroup> getGroups() {
		return groups.values();
	}
	
	public YamlConfiguration getDefaultSettings() {
		return defaultConfig;
	}
	
	public YamlConfiguration getConfig() {
		return config;
	}
	
	public GeneralRegion getRegion(String name) {
		return regions.get(name.toLowerCase());
	}
	
	public RentRegion getRent(String name) {
		GeneralRegion region = regions.get(name.toLowerCase());
		if(region != null && region.isRentRegion()) {
			return (RentRegion)region;
		}
		return null;
	}
	
	public BuyRegion getBuy(String name) {
		GeneralRegion region = regions.get(name.toLowerCase());
		if(region != null && region.isBuyRegion()) {
			return (BuyRegion)region;
		}
		return null;
	}
	
	public List<RentRegion> getRents() {
		List<RentRegion> result = new ArrayList<RentRegion>();
		for(GeneralRegion region : regions.values()) {
			if(region.isRentRegion()) {
				result.add((RentRegion)region);
			}
		}
		return result;
	}
	
	public List<BuyRegion> getBuys() {
		List<BuyRegion> result = new ArrayList<BuyRegion>();
		for(GeneralRegion region : regions.values()) {
			if(region.isBuyRegion()) {
				result.add((BuyRegion)region);
			}
		}
		return result;
	}
	
	public List<GeneralRegion> getRegions() {
		return new ArrayList<GeneralRegion>(regions.values());
	}
	
	/**
	 * Get a list of names of a certain group of things
	 * @return A String list with all the names
	 */
	public List<String> getBuyNames() {
		ArrayList<String> result = new ArrayList<String>();
		for(BuyRegion region : getBuys()) {
			result.add(region.getName());
		}
		return result;
	}
	public List<String> getRentNames() {
		ArrayList<String> result = new ArrayList<String>();
		for(RentRegion region : getRents()) {
			result.add(region.getName());
		}
		return result;
	}
	public List<String> getRegionNames() {
		ArrayList<String> result = new ArrayList<String>();
		for(GeneralRegion region : getRegions()) {
			result.add(region.getName());
		}
		return result;
	}
	public List<String> getGroupNames() {
		ArrayList<String> result = new ArrayList<String>();
		for(RegionGroup group : getGroups()) {
			result.add(group.getName());
		}
		return result;
	}
	
	/**
	 * Add a rent to the list without saving it to disk (useful for loading at startup)
	 * @param rent The rental region to add
	 */
	public void addRentNoSave(RentRegion rent) {
		if(rent == null) {
			AreaShop.debug("Tried adding a null rent!");
			return;
		}
		regions.put(rent.getName().toLowerCase(), rent);
	}
	/**
	 * Add a rent to the list and mark it as to-be-saved
	 * @param rent Then rental region to add
	 */
	public void addRent(RentRegion rent) {
		addRentNoSave(rent);
		rent.saveRequired();
	}
	
	/**
	 * Add a buy to the list without saving it to disk (useful for laoding at startup)
	 * @param buy The buy region to add
	 */
	public void addBuyNoSave(BuyRegion buy) {
		if(buy == null) {
			AreaShop.debug("Tried adding a null buy!");
			return;
		}
		regions.put(buy.getName().toLowerCase(), buy);
	}
	/**
	 * Add a buy to the list and mark it as to-be-saved
	 * @param buy The buy region to add
	 */
	public void addBuy(BuyRegion buy) {
		addBuyNoSave(buy);
		buy.saveRequired();
	}
	
	/**
	 * Add a RegionGroup
	 * @param group The RegionGroup to add
	 */
	public void addGroup(RegionGroup group) {
		groups.put(group.getName().toLowerCase(), group);
		String lowGroup = group.getName().toLowerCase();
		ConfigurationSection result = groupsConfig.getConfigurationSection(lowGroup);
		if(result == null) {
			result = groupsConfig.createSection(lowGroup);
			groupsConfig.set(lowGroup + ".name", group.getName());
			groupsConfig.set(lowGroup + ".priority", 0);
		}
		saveGroupsIsRequired();
	}
	
	/**
	 * Check if a player can add a certain region as rent or buy region
	 * @param sender The player/console that wants to add a region
	 * @param region The WorldGuard region to add
	 * @param type The type the region should have in AreaShop
	 * @return The result if a player would want to add this region
	 */
	public AddResult checkRegionAdd(CommandSender sender, ProtectedRegion region, RegionType type) {
		Player player = null;
		if(sender instanceof Player) {
			player = (Player)sender;
		}
		// Determine if the player is an owner or member of the region
		boolean isMember = player != null && plugin.getWorldGuardHandler().containsMember(region, player.getUniqueId());
		boolean isOwner = player != null && plugin.getWorldGuardHandler().containsOwner(region, player.getUniqueId());		
		AreaShop.debug("checkRegionAdd: isOwner=" + isOwner + ", isMember=" + isMember);
		String typeString = null;
		if(type == RegionType.RENT) {
			typeString = "rent";
		} else {
			typeString = "buy";
		}
		AreaShop.debug("  permissions: .create=" + sender.hasPermission("areashop.create" + typeString) + ", .create.owner=" + sender.hasPermission("areashop.create" + typeString + ".owner") + ", .create.member=" + sender.hasPermission("areashop.create" + typeString + ".member"));
		if(!(sender.hasPermission("areashop.create" + typeString)
				|| (sender.hasPermission("areashop.create" + typeString + ".owner") && isOwner)
				|| (sender.hasPermission("areashop.create" + typeString + ".member") && isMember))) {
			return AddResult.NOPERMISSION;
		}
		GeneralRegion asRegion = plugin.getFileManager().getRegion(region.getId());
		if(asRegion != null) {
			return AddResult.ALREADYADDED;
		} else if(plugin.getFileManager().isBlacklisted(region.getId())) {
			return AddResult.BLACKLISTED;
		} else {
			return AddResult.SUCCESS;
		}
	}
	
	/**
	 * Remove a rent from the list
	 * @param rent The region to remove
	 * @param giveMoneyBack use true to give money back to the player if someone is currently renting this region, otherwise false
	 */
	public boolean removeRent(RentRegion rent, boolean giveMoneyBack) {
		boolean result = false;
		if(rent != null) {
			rent.setDeleted();
			if(rent.isRented()) {
				rent.unRent(giveMoneyBack);
			}
			// Handle schematics and run commands
			rent.handleSchematicEvent(RegionEvent.DELETED);
			rent.runEventCommands(RegionEvent.DELETED, true);
			
			/* Delete the signs and the variable */
			if(rent.getWorld() != null) {
				for(Location sign : rent.getSignLocations()) {
					sign.getBlock().setType(Material.AIR);
					AreaShop.debug("Removed sign at: " + sign.toString());
				}
			}
			RegionGroup[] groups = getGroups().toArray(new RegionGroup[getGroups().size()]);
			for(RegionGroup group : groups) {
				group.removeMember(rent);
			}
			rent.resetRegionFlags();
			regions.remove(rent.getLowerCaseName());
			File file = new File(plugin.getDataFolder() + File.separator + AreaShop.regionsFolder + File.separator + rent.getLowerCaseName() + ".yml");
			boolean deleted = true;
			if(file.exists()) {
				try {
					deleted = file.delete();
				} catch(Exception e) {
					deleted = false;
				}
				if(!deleted) {
					plugin.getLogger().warning("File could not be deleted: " + file.toString());
				}
			}
			result = true;
			
			// Run commands
			rent.runEventCommands(RegionEvent.DELETED, false);
		}		
		return result;
	}
	
	/**
	 * Get a region by providing a location of the sign
	 * @param location The locatin of the sign
	 * @return The generalRegion that has a sign at this location
 	 */
	public GeneralRegion getRegionBySignLocation(Location location) {
		for(GeneralRegion region : getRegions()) {
			if(region.isSignOfRegion(location)) {
				return region;
			}
		}
		return null;
	}
	
	/**
	 * Remove a buy from the list
	 * @param regionName
	 */
	public boolean removeBuy(BuyRegion buy, boolean giveMoneyBack) {
		boolean result = false;
		if(buy != null) {
			buy.setDeleted();
			if(buy.isSold()) {
				buy.sell(giveMoneyBack);
			}
			// Handle schematics and run commands
			buy.handleSchematicEvent(RegionEvent.DELETED);
			buy.runEventCommands(RegionEvent.DELETED, true);
			
			// Delete the sign and the variable
			if(buy.getWorld() != null) {
				for(Location sign : buy.getSignLocations()) {
					sign.getBlock().setType(Material.AIR);
				}
			}
			regions.remove(buy.getLowerCaseName());
			buy.resetRegionFlags();
			
			// Removing from groups
			for(RegionGroup group : getGroups()) {
				group.removeMember(buy);
			}
			
			// Deleting the file
			File file = new File(plugin.getDataFolder() + File.separator + AreaShop.regionsFolder + File.separator + buy.getLowerCaseName() + ".yml");
			boolean deleted = true;
			if(file.exists()) {
				try {
					deleted = file.delete();
				} catch(Exception e) {
					deleted = false;
				}
				if(!deleted) {
					plugin.getLogger().warning("File could not be deleted: " + file.toString());
				}
			}
			
			result = true;
			
			// Run commands
			buy.runEventCommands(RegionEvent.DELETED, false);
		}		
		return result;
	}
	
	/**
	 * Remove a group
	 * @param group Group to remove
	 */
	public void removeGroup(RegionGroup group) {
		groups.remove(group.getLowerCaseName());
		groupsConfig.set(group.getLowerCaseName(), null);
		saveGroupsIsRequired();
	}
	
	/**
	 * Update all signs that need periodic updating
	 */
	public void performPeriodicSignUpdate() {
		final List<RentRegion> regions = new ArrayList<RentRegion>(getRents());
		new BukkitRunnable() {
			private int current = 0;
			
			@Override
			public void run() {
				for(int i=0; i<plugin.getConfig().getInt("signs.regionsPerTick"); i++) {
					if(current < regions.size()) {
						if(regions.get(current).needsPeriodicUpdating()) {
							regions.get(current).updateSigns();
						}
						current++;
					}
				}
				if(current >= regions.size()) {
					this.cancel();
				}
			}
		}.runTaskTimer(plugin, 1, 1);		
	}
	
	/**
	 * Send out rent expire warnings
	 */
	public void sendRentExpireWarnings() {
		final List<RentRegion> regions = new ArrayList<RentRegion>(getRents());
		new BukkitRunnable() {
			private int current = 0;
			
			@Override
			public void run() {
				for(int i=0; i<plugin.getConfig().getInt("expireWarning.regionsPerTick"); i++) {
					if(current < regions.size()) {
						regions.get(current).sendExpirationWarnings();
						current++;
					}
				}
				if(current >= regions.size()) {
					this.cancel();
				}
			}
		}.runTaskTimer(plugin, 1, 1);
	}
	
	/**
	 * Update all rent signs
	 * @return true if all signs are updated, otherwise false
	 * @param confirmationReceiver who needs to get the confirmation message, null if nobody
	 */
	public void updateRentSignsAndFlags(final CommandSender confirmationReceiver) {
		final List<RentRegion> regions = new ArrayList<RentRegion>(getRents());
		new BukkitRunnable() {
			private int current = 0;
			private boolean result = true;
			@Override
			public void run() {
				for(int i=0; i<plugin.getConfig().getInt("update.regionsPerTick"); i++) {
					if(current < regions.size()) {
						result = regions.get(current).updateSigns() && result;
						regions.get(current).updateRegionFlags();
						current++;
					}
				}
				if(current >= regions.size()) {
					if(confirmationReceiver != null) {
						if(result) {
							plugin.message(confirmationReceiver, "rents-updated");
						} else {
							plugin.message(confirmationReceiver, "rents-notUpdated");
						}
					}
					this.cancel();
				}
			}
		}.runTaskTimer(plugin, 1, 1);
	}
	
	/**
	 * Update all buy signs
	 * @return true if all signs are updated, otherwise false
	 * @param confirmationReceiver who needs to get the confirmation message, null if nobody
	 */
	public void updateBuySignsAndFlags(final CommandSender confirmationReceiver) {
		final List<BuyRegion> regions = new ArrayList<BuyRegion>(getBuys());
		new BukkitRunnable() {
			private int current = 0;
			private boolean result = true;
			
			@Override
			public void run() {
				for(int i=0; i<plugin.getConfig().getInt("update.regionsPerTick"); i++) {
					if(current < regions.size()) {
						regions.get(current).updateSigns();
						regions.get(current).updateRegionFlags();
						current++;
					} 
				}
				if(current >= regions.size()) {
					if(confirmationReceiver != null) {
						if(result) {
							plugin.message(confirmationReceiver, "buys-updated");
						} else {
							plugin.message(confirmationReceiver, "buys-notUpdated");
						}
					}
					this.cancel();
				}
			}
		}.runTaskTimer(plugin, 1, 1);		
	}
	
	/**
	 * Update regions in a task to minimize lag
	 * @param regions Regions to update
	 */
	public void updateRegions(final List<GeneralRegion> regions, final CommandSender confirmationReceiver) {
		final int regionsPerTick = plugin.getConfig().getInt("update.regionsPerTick");
		if(confirmationReceiver != null) {
			plugin.message(confirmationReceiver, "reload-updateStart", regions.size(), regionsPerTick*20);
		}
		new BukkitRunnable() {
			private int current = 0;
			@Override
			public void run() {
				for(int i=0; i<regionsPerTick; i++) {
					if(current < regions.size()) {
						regions.get(current).updateSigns();
						regions.get(current).updateRegionFlags();
						current++;
					} 
				}
				if(current >= regions.size()) {
					if(confirmationReceiver != null) {
						plugin.message(confirmationReceiver, "reload-updateComplete");
					}
					this.cancel();
				}
			}
		}.runTaskTimer(plugin, 1, 1);
	}
	public void updateRegions(List<GeneralRegion> regions) {
		updateRegions(regions, null);
	}
	/**
	 * Update all regions, happens in a task to minimize lag
	 */
	public void updateAllRegions() {
		updateRegions(getRegions(), null);
	}
	public void updateAllRegions(CommandSender confirmationReceiver) {
		updateRegions(getRegions(), confirmationReceiver);
	}

	
	/**
	 * Save the group file to disk
	 */
	public void saveGroupsIsRequired() {
		saveGroupsRequired = true;
	}
	public boolean isSaveGroupsRequired() {
		return saveGroupsRequired;
	}
	
	public void saveGroupsNow() {
		AreaShop.debug("saveGroupsNow() done");
		saveGroupsRequired = false;
		try {
			groupsConfig.save(groupsPath);
		} catch (IOException e) {
			plugin.getLogger().warning("Groups file could not be saved: " + groupsPath);
		}
	}
	
	
	/**
	 * Save all region related files spread over time (low load)
	 */
	public void saveRequiredFiles() {
		if(isSaveGroupsRequired()) {
			saveGroupsNow();
		}
		this.saveWorldGuardRegions();
		
		final List<GeneralRegion> regions = new ArrayList<GeneralRegion>(getRegions());
		new BukkitRunnable() {
			private int current = 0;
			
			@Override
			public void run() {
				for(int i=0; i<plugin.getConfig().getInt("saving.regionsPerTick"); i++) {
					if(current < regions.size()) {
						if(regions.get(current).isSaveRequired()) {
							regions.get(current).saveNow();
						}
						current++;
					}
				}
				if(current >= regions.size()) {
					this.cancel();
				}
			}
		}.runTaskTimer(plugin, 1, 1);
	}
	
	/**
	 * Save all region related files directly (only for cases like onDisable())
	 */
	public void saveRequiredFilesAtOnce() {
		if(isSaveGroupsRequired()) {
			saveGroupsNow();
		}
		for(GeneralRegion region : getRegions()) {
			if(region.isSaveRequired()) {
				region.saveNow();
			}						
		}
		this.saveWorldGuardRegions();
	}
	
	/**
	 * Indicates that a/multiple WorldGuard regions need to be saved
	 * @param worldName The world where the regions that should be saved is in
	 */
	public void saveIsRequiredForRegionWorld(String worldName) {
		worldRegionsRequireSaving.add(worldName);
	}
	
	/**
	 * Save all worldGuard regions that need saving
	 */
	public void saveWorldGuardRegions() {
		for(String world : worldRegionsRequireSaving) {
			World bukkitWorld = Bukkit.getWorld(world);
			if(bukkitWorld != null) {
				RegionManager manager = plugin.getWorldGuard().getRegionManager(bukkitWorld);
				if(manager != null) {
					try {
						if(plugin.getWorldGuard().getDescription().getVersion().startsWith("5.")) {
							manager.save();
						} else {							
							manager.saveChanges();
						}
					} catch(Exception e) {
						plugin.getLogger().warning("WorldGuard regions in world " + world + " could not be saved");
					}
				}
			}
		}
	}
	
	/**
	 * Get the folder the region files are located in
	 * @return The folder where the <region>.yml files are in
	 */
	public String getRegionFolder() {
		return regionsPath;
	}
	
	/**
	 * Check if a region is on the adding blacklist
	 * @param region The region name to check
	 * @return true if the region may not be added, otherwise false
	 */
	public boolean isBlacklisted(String region) {
		for(String line : plugin.getConfig().getStringList("blacklist")) {
			Pattern pattern = Pattern.compile(line, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(region);
			if(matcher.matches()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Unrent regions that have no time left, regions to check per tick is in the config
	 */
	public void checkRents() {
		final List<RentRegion> regions = new ArrayList<RentRegion>(getRents());
		new BukkitRunnable() {
			private int current = 0;
			
			@Override
			public void run() {
				for(int i=0; i<plugin.getConfig().getInt("expiration.regionsPerTick"); i++) {
					if(current < regions.size()) {
						regions.get(current).checkExpiration();
						current++;
					}
				}
				if(current >= regions.size()) {
					this.cancel();
				}
			}
		}.runTaskTimer(plugin, 1, 1);
	}
	
	/**
	 * Check all regions and unrent/sell them if the player is inactive for too long
	 */
	public void checkForInactiveRegions() {
		final List<GeneralRegion> regions = new ArrayList<GeneralRegion>(getRegions());
		new BukkitRunnable() {
			private int current = 0;
			
			@Override
			public void run() {
				for(int i=0; i<plugin.getConfig().getInt("inactive.regionsPerTick"); i++) {
					if(current < regions.size()) {
						regions.get(current).checkInactive();
						current++;
					}
				}
				if(current >= regions.size()) {
					this.cancel();
				}
			}
		}.runTaskTimer(plugin, 1, 1);
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
				plugin.getLogger().warning("Something went wrong reading file: " + versionPath);
				versions = null;
			}
		}
		if(versions == null || versions.size() == 0) {
			versions = new HashMap<String, Integer>();
			versions.put(AreaShop.versionFiles, 0);
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
			plugin.getLogger().warning("File could not be saved: " + versionPath);
		}
	}

	/**
	 * Load all files from disk
	 * @return true
	 */
	public boolean loadFiles() {
		boolean result = false;		
		// Load config.yml + add defaults from .jar
		result = result & loadConfigFile();
		// Load default.yml + add defaults from .jar
		result = result & loadDefaultFile();
		// Convert old formats to the latest
		preUpdateFiles();
		// Load region files (regions folder)
		result = result & loadRegionFiles();
		postUpdateFiles();
		// Load groups.yml
		result = result & loadGroupsFile();

		return result;
	}
	
	/**
	 * Load the default.yml file
	 * @return true if it has been loaded successfully, otherwise false
	 */
	public boolean loadDefaultFile() {
		File defaultFile = new File(defaultPath);
		// Safe the file from the jar to disk if it does not exist
		if(!defaultFile.exists()) {
			InputStream input = null;
			OutputStream output = null;
			try {
				input = plugin.getResource(AreaShop.defaultFile);
				output = new FileOutputStream(defaultFile);
		 
				int read = 0;
				byte[] bytes = new byte[1024];		 
				while ((read = input.read(bytes)) != -1) {
					output.write(bytes, 0, read);
				} 
				input.close();
				output.close();
				plugin.getLogger().info("File with default region settings has been saved, should only happen on first startup");
			} catch(IOException e) {
				try {
					input.close();
					output.close();
				} catch (IOException e1) {} catch (NullPointerException e2) {}
				
				plugin.getLogger().warning("Something went wrong saving the default region settings: " + defaultFile.getPath());
			}
		}
		// Load default.yml from the plugin folder
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(new FileInputStream(defaultFile), Charsets.UTF_8);
		} catch (FileNotFoundException e) {}
		if(reader != null) {
			defaultConfig = YamlConfiguration.loadConfiguration(reader);
		}
		try {
			reader.close();
		} catch (IOException e) {}
		if(defaultConfig == null) {
			defaultConfig = new YamlConfiguration();
		}
		
		// Addding the defaults from the normal file that is inside the jar is disabled, not nice when removing lines for things you don't want
		InputStream inputStream = plugin.getResource(AreaShop.defaultFile);
		if(inputStream != null) {
			reader = new InputStreamReader(inputStream, Charsets.UTF_8);
		}
		if(reader != null) {
			defaultConfig.addDefaults(YamlConfiguration.loadConfiguration(reader));
		}
		try {
			reader.close();
		} catch (IOException e) {}
		return defaultConfig != null;
	}
	
	/**
	 * Load the default.yml file
	 * @return true if it has been loaded successfully, otherwise false
	 */
	public boolean loadConfigFile() {
		File configFile = new File(configPath);
		// Safe the file from the jar to disk if it does not exist
		if(!configFile.exists()) {
			InputStream input = null;
			OutputStream output = null;
			try {
				input = plugin.getResource(AreaShop.configFile);
				output = new FileOutputStream(configFile);
		 
				int read = 0;
				byte[] bytes = new byte[1024];		 
				while ((read = input.read(bytes)) != -1) {
					output.write(bytes, 0, read);
				} 
				input.close();
				output.close();
				plugin.getLogger().info("Default config file has been saved, should only happen on first startup");
			} catch(IOException e) {
				try {
					input.close();
					output.close();
				} catch (IOException e1) {} catch (NullPointerException e2) {}
				
				plugin.getLogger().warning("Something went wrong saving the config file: " + configFile.getPath());
			}
		}
		// Load config.yml from the plugin folder
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(new FileInputStream(configFile), Charsets.UTF_8);
		} catch (FileNotFoundException e) {}
		if(reader != null) {
			config = YamlConfiguration.loadConfiguration(reader);
		}
		try {
			reader.close();
		} catch (IOException e) {}
		if(config == null) {
			config = new YamlConfiguration();
		}
		// Add the values from the config.yml file inside of the .jar as defaults
		InputStream inputStream = plugin.getResource(AreaShop.configFile);
		if(inputStream != null) {
			reader = new InputStreamReader(inputStream, Charsets.UTF_8);
		}
		if(reader != null) {
			config.addDefaults(YamlConfiguration.loadConfiguration(reader));
		}
		try {
			reader.close();
		} catch (IOException e) {}
		
	    // Set the debug and chatprefix variables
		plugin.setDebug(this.getConfig().getBoolean("debug"));
	    plugin.setChatprefix(this.getConfig().getString("chatPrefix"));
		
		return config != null;
	}
	
	/**
	 * Load the groups.yml file from disk
	 * @return
	 */
	public boolean loadGroupsFile() {
		File groupFile = new File(groupsPath);
		InputStreamReader reader = null;
		if(groupFile.exists() && groupFile.isFile()) {
			try {
				reader = new InputStreamReader(new FileInputStream(groupFile), Charsets.UTF_8);
			} catch (FileNotFoundException e) {}
			if(reader != null) {
				groupsConfig = YamlConfiguration.loadConfiguration(reader);
			}
			try {
				reader.close();
			} catch (IOException e) {}

		}
		if(groupsConfig == null) {
			groupsConfig = new YamlConfiguration();
		}
		for(String groupName : groupsConfig.getKeys(false)) {
			RegionGroup group = new RegionGroup(plugin, groupName);
			groups.put(groupName, group);
		}
		return true;
	}
	
	/**
	 * Load all region files
	 * @return true
	 */
	public boolean loadRegionFiles() {
		regions.clear();
		File file = new File(regionsPath);
		if(!file.exists()) {
			file.mkdirs();
			plugin.setReady(true);
		} else if(file.isDirectory()) {
			File[] regionFiles = file.listFiles();
			for(File regionFile : regionFiles) {
				if(regionFile.exists() && regionFile.isFile()) {
					// Load the region file from disk in UTF8 mode
					InputStreamReader reader = null;
					YamlConfiguration config = null;
					try {
						reader = new InputStreamReader(new FileInputStream(regionFile), Charsets.UTF_8);
					} catch (FileNotFoundException e) {}
					if(reader != null) {
						config = YamlConfiguration.loadConfiguration(reader);
					}
					try {
						reader.close();
					} catch (IOException e) {}
					// Construct the correct type of region
					if(RegionType.RENT.getValue().equals(config.getString("general.type"))) {
						RentRegion rent = new RentRegion(plugin, config);
						addRentNoSave(rent);
					} else if(RegionType.BUY.getValue().equals(config.getString("general.type"))) {
						BuyRegion buy = new BuyRegion(plugin, config);
						addBuyNoSave(buy);
					}
				}						
			}
			plugin.setReady(true);			
			new BukkitRunnable() {				
				@Override
				public void run() {
					List<GeneralRegion> noWorld = new ArrayList<GeneralRegion>();
					List<GeneralRegion> noRegion = new ArrayList<GeneralRegion>();
					for(GeneralRegion region : AreaShop.getInstance().getFileManager().getRegions()) {
						// Add broken regions to a list
						if(region != null) {
							if(region.getWorld() == null) {
								noWorld.add(region);
							}
							if(region.getRegion() == null) {
								noRegion.add(region);
							}
						}
					}					
					// All files are loaded, print possible problems to the console
					if(!noRegion.isEmpty()) {
						List<String> noRegionNames = new ArrayList<String>();
						for(GeneralRegion region : noRegion) {
							noRegionNames.add(region.getName());
						}
						plugin.getLogger().warning("AreaShop regions that are missing their WorldGuard region: " + Utils.createCommaSeparatedList(noRegionNames));
						plugin.getLogger().warning("Remove these regions from AreaShop with '/as del' or recreate their regions in WorldGuard.");
					}
					boolean noWorldRegions = !noWorld.isEmpty();
					while(!noWorld.isEmpty()) {
						List<GeneralRegion> toDisplay = new ArrayList<GeneralRegion>();
						String missingWorld = noWorld.get(0).getWorldName();
						toDisplay.add(noWorld.get(0));
						for(int i=1; i<noWorld.size(); i++) {
							if(noWorld.get(i).getWorldName().equalsIgnoreCase(missingWorld)) {
								toDisplay.add(noWorld.get(i));
							}
						}
						List<String> noWorldNames = new ArrayList<String>();
						for(GeneralRegion region : noRegion) {
							noWorldNames.add(region.getName());
						}
						plugin.getLogger().warning("World " + missingWorld + " is not loaded, the following AreaShop regions are not functional now: " + Utils.createCommaSeparatedList(noWorldNames));
						noWorld.removeAll(toDisplay);
					}
					if(noWorldRegions) {
						plugin.getLogger().warning("Remove these regions from AreaShop with '/as del' or load the world(s) on the server again.");
					}
				}
			}.runTask(plugin);
		}
		// Warnings will be printed in console, no other things can go wrong
		return true;
	}
	
	
	/**
	 * Checks for old file formats and converts them to the latest format.
	 * After conversion the region files need to be loaded
	 */
	@SuppressWarnings("unchecked")
	public void preUpdateFiles() {
		Integer fileStatus = versions.get(AreaShop.versionFiles);
		
		// If the the files are already the current version
		if(fileStatus != null && fileStatus == AreaShop.versionFilesCurrent) {
			return;
		}
		plugin.getLogger().info("Updating AreaShop data to the latest format:");
		
		// Update to YAML based format
		if(fileStatus == null || fileStatus < 2) {
			String rentPath = plugin.getDataFolder() + File.separator + "rents";
			String buyPath = plugin.getDataFolder() + File.separator + "buys";
			File rentFile = new File(rentPath);
			File buyFile = new File(buyPath);
			String oldFolderPath = plugin.getDataFolder() + File.separator + "#old" + File.separator;
			File oldFolderFile = new File(oldFolderPath);
			// Convert old rent files
			boolean buyFileFound = false, rentFileFound = false;
			if(rentFile.exists()) {
				rentFileFound = true;
				if(!oldFolderFile.exists()) {
					oldFolderFile.mkdirs();
				}
				
				if(versions.get("rents") == null) {
					versions.put("rents", -1);
				}
				
				HashMap<String, HashMap<String, String>> rents = null;
				try {
					ObjectInputStream input = new ObjectInputStream(new FileInputStream(rentPath));
			    	rents = (HashMap<String,HashMap<String,String>>)input.readObject();
					input.close();
				} catch (IOException | ClassNotFoundException | ClassCastException e) {
					plugin.getLogger().warning("  Error: Something went wrong reading file: " + rentPath);
				}
				// Delete the file if it is totally wrong
				if(rents == null) {
					try {
						rentFile.delete();
					} catch(Exception e) {}
				} else {
					// Move old file
					try {
						Files.move(new File(rentPath), new File(oldFolderPath + "rents"));
					} catch (Exception e) {
						plugin.getLogger().warning("  Could not create a backup of '" + rentPath + "', check the file permissions (conversion to next version continues)");
					}
					// Check if conversion is needed
					if(versions.get("rents") < 1) {					
						/* Upgrade the rent to the latest version */
						if(versions.get("rents") < 0) {
							for(String rentName : rents.keySet()) {
								HashMap<String,String> rent = rents.get(rentName);
								/* Save the rentName in the hashmap and use a small caps rentName as key */
								if(rent.get("name") == null) {
									rent.put("name", rentName);
									rents.remove(rentName);
									rents.put(rentName.toLowerCase(), rent);
								}
								/* Save the default setting for region restoring */
								if(rent.get("restore") == null) {
									rent.put("restore", "general");
								}
								/* Save the default setting for the region restore profile */
								if(rent.get("profile") == null) {
									rent.put("profile", "default");
								}
								/* Change to version 0 */
								versions.put("rents", 0);
							}
							plugin.getLogger().info("  Updated version of '" + buyPath + "' from -1 to 0 (switch to using lowercase region names, adding default schematic enabling and profile)");
						}
						if(versions.get("rents") < 1) {
							for(String rentName : rents.keySet()) {
								HashMap<String,String> rent = rents.get(rentName);
								if(rent.get("player") != null) {
									@SuppressWarnings("deprecation")  // Fake deprecation by Bukkit to inform developers, method will stay
									OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(rent.get("player"));
									rent.put("playeruuid", offlinePlayer.getUniqueId().toString());		
									rent.remove("player");
								}					
								/* Change version to 1 */
								versions.put("rents", 1);
							}
							plugin.getLogger().info("  Updated version of '" + rentPath + "' from 0 to 1 (switch to UUID's for player identification)");
						}				
					}		
					// Save rents to new format
					File regionsFile = new File(regionsPath);
					if(!regionsFile.exists()) {
						regionsFile.mkdirs();
					}
					for(HashMap<String, String> rent : rents.values()) {
						YamlConfiguration config = new YamlConfiguration();
						config.set("general.name", rent.get("name").toLowerCase());
						config.set("general.type", "rent");
						config.set("general.world", rent.get("world"));
						config.set("general.signs.0.location.world", rent.get("world"));
						config.set("general.signs.0.location.x", Double.parseDouble(rent.get("x")));
						config.set("general.signs.0.location.y", Double.parseDouble(rent.get("y")));
						config.set("general.signs.0.location.z", Double.parseDouble(rent.get("z")));
						config.set("rent.price", Double.parseDouble(rent.get("price")));
						config.set("rent.duration", rent.get("duration"));
						if(rent.get("restore") != null && !rent.get("restore").equals("general")) {
							config.set("general.enableRestore", rent.get("restore"));
						}
						if(rent.get("profile") != null && !rent.get("profile").equals("default")) {
							config.set("general.schematicProfile", rent.get("profile"));
						}
						if(rent.get("tpx") != null) {
							config.set("general.teleportLocation.world", rent.get("world"));
							config.set("general.teleportLocation.x", Double.parseDouble(rent.get("tpx")));
							config.set("general.teleportLocation.y", Double.parseDouble(rent.get("tpy")));
							config.set("general.teleportLocation.z", Double.parseDouble(rent.get("tpz")));
							config.set("general.teleportLocation.yaw", rent.get("tpyaw"));
							config.set("general.teleportLocation.pitch", rent.get("tppitch"));
						}
						if(rent.get("playeruuid") != null) {
							config.set("rent.renter", rent.get("playeruuid"));
							config.set("rent.renterName", plugin.toName(rent.get("playeruuid")));
							config.set("rent.rentedUntil", Long.parseLong(rent.get("rented")));
						}
						try {
							config.save(new File(regionsPath + File.separator + rent.get("name").toLowerCase() + ".yml"));
						} catch (IOException e) {
							plugin.getLogger().warning("  Error: Could not save region file while converting: " + regionsPath + File.separator + rent.get("name").toLowerCase() + ".yml");
						}
					}
					plugin.getLogger().info("  Updated rent regions to new .yml format (check the /regions folder)");
				}
				
				// Change version number
				versions.remove("rents");
				versions.put(AreaShop.versionFiles, AreaShop.versionFilesCurrent);			
				saveVersions();			
			}
			if(buyFile.exists()) {
				buyFileFound = true;
				if(!oldFolderFile.exists()) {
					oldFolderFile.mkdirs();
				}
				
				if(versions.get("buys") == null) {
					versions.put("buys", -1);
				}
				
				HashMap<String, HashMap<String, String>> buys = null;
				try {
					ObjectInputStream input = new ObjectInputStream(new FileInputStream(buyPath));
			    	buys = (HashMap<String,HashMap<String,String>>)input.readObject();
					input.close();
				} catch (IOException | ClassNotFoundException | ClassCastException e) {
					plugin.getLogger().warning("  Something went wrong reading file: " + buyPath);
				}
				// Delete the file if it is totally wrong
				if(buys == null) {
					try {
						buyFile.delete();
					} catch(Exception e) {}
				} else {
					// Backup current file
					try {
						Files.move(new File(buyPath), new File(oldFolderPath + "buys"));
					} catch (Exception e) {
						plugin.getLogger().warning("  Could not create a backup of '" + buyPath + "', check the file permissions (conversion to next version continues)");
					}
					// Check if conversion is needed
					if(versions.get("buys") < 1) {				
						/* Upgrade the buy to the latest version */
						if(versions.get("buys") < 0) {
							for(String buyName : buys.keySet()) {
								HashMap<String,String> buy = buys.get(buyName);
								/* Save the buyName in the hashmap and use a small caps buyName as key */
								if(buy.get("name") == null) {
									buy.put("name", buyName);
									buys.remove(buyName);
									buys.put(buyName.toLowerCase(), buy);
								}
								/* Save the default setting for region restoring */
								if(buy.get("restore") == null) {
									buy.put("restore", "general");
								}
								/* Save the default setting for the region restore profile */
								if(buy.get("profile") == null) {
									buy.put("profile", "default");
								}
								/* Change to version 0 */
								versions.put("buys", 0);
							}
							plugin.getLogger().info("  Updated version of '" + buyPath + "' from -1 to 0 (switch to using lowercase region names, adding default schematic enabling and profile)");
						}
						if(versions.get("buys") < 1) {
							for(String buyName : buys.keySet()) {
								HashMap<String,String> buy = buys.get(buyName);
								if(buy.get("player") != null) {
									@SuppressWarnings("deprecation")  // Fake deprecation by Bukkit to inform developers, method will stay
									OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(buy.get("player"));
									buy.put("playeruuid", offlinePlayer.getUniqueId().toString());		
									buy.remove("player");
								}					
								/* Change version to 1 */
								versions.put("buys", 1);
							}
							plugin.getLogger().info("  Updated version of '" + buyPath + "' from 0 to 1 (switch to UUID's for player identification)");
						}				
					}		
				
					// Save buys to new format
					File regionsFile = new File(regionsPath);
					if(!regionsFile.exists()) {
						regionsFile.mkdirs();
					}
					for(HashMap<String, String> buy : buys.values()) {
						YamlConfiguration config = new YamlConfiguration();
						config.set("general.name", buy.get("name").toLowerCase());
						config.set("general.type", "buy");
						config.set("general.world", buy.get("world"));
						config.set("general.signs.0.location.world", buy.get("world"));
						config.set("general.signs.0.location.x", Double.parseDouble(buy.get("x")));
						config.set("general.signs.0.location.y", Double.parseDouble(buy.get("y")));
						config.set("general.signs.0.location.z", Double.parseDouble(buy.get("z")));
						config.set("buy.price", Double.parseDouble(buy.get("price")));
						if(buy.get("restore") != null && !buy.get("restore").equals("general")) {
							config.set("general.enableRestore", buy.get("restore"));
						}
						if(buy.get("profile") != null && !buy.get("profile").equals("default")) {
							config.set("general.schematicProfile", buy.get("profile"));
						}
						if(buy.get("tpx") != null) {
							config.set("general.teleportLocation.world", buy.get("world"));
							config.set("general.teleportLocation.x", Double.parseDouble(buy.get("tpx")));
							config.set("general.teleportLocation.y", Double.parseDouble(buy.get("tpy")));
							config.set("general.teleportLocation.z", Double.parseDouble(buy.get("tpz")));
							config.set("general.teleportLocation.yaw", buy.get("tpyaw"));
							config.set("general.teleportLocation.pitch", buy.get("tppitch"));
						}
						if(buy.get("playeruuid") != null) {
							config.set("buy.buyer", buy.get("playeruuid"));
							config.set("buy.buyerName", plugin.toName(buy.get("playeruuid")));
						}
						try {
							config.save(new File(regionsPath + File.separator + buy.get("name").toLowerCase() + ".yml"));
						} catch (IOException e) {
							plugin.getLogger().warning("  Error: Could not save region file while converting: " + regionsPath + File.separator + buy.get("name").toLowerCase() + ".yml");
						}
					}
					plugin.getLogger().info("  Updated buy regions to new .yml format (check the /regions folder)");
				}
	
				// Change version number
				versions.remove("buys");		
			}

			// Separate try-catch blocks to try them all individually (don't stop after 1 has failed)
			try {
				Files.move(new File(rentPath + ".old"), new File(oldFolderPath + "rents.old"));
			} catch (Exception e) {}
			try {
				Files.move(new File(buyPath + ".old"), new File(oldFolderPath + "buys.old"));
			} catch (Exception e) {}
			if(buyFileFound || rentFileFound) {
				try {
					Files.move(new File(plugin.getDataFolder() + File.separator + "config.yml"), new File(oldFolderPath + "config.yml"));
				} catch (Exception e) {}
			}
			// Update versions file to 2
			versions.put(AreaShop.versionFiles, 2);			
			saveVersions();
			plugin.getLogger().info("  Updated to YAML based storage (v1 to v2)");
		}
	}
	
	/**
	 * Checks for old file formats and converts them to the latest format.
	 * This is to be triggered after the load of the region files
	 */
	public void postUpdateFiles() {
		Integer fileStatus = versions.get(AreaShop.versionFiles);
		
		// If the the files are already the current version
		if(fileStatus != null && fileStatus == AreaShop.versionFilesCurrent) {
			return;
		}
		
		// Add 'general.lastActive' to rented/bought regions (initialize at current time)
		if(fileStatus == null || fileStatus < 3) {
			for(GeneralRegion region : getRegions()) {
				region.updateLastActiveTime();
			}
			// Update versions file to 3
			versions.put(AreaShop.versionFiles, 3);			
			saveVersions();
			plugin.getLogger().info("  Added last active time to regions (v2 to v3)");
		}
	}
	
	/**
	 * Get the settings of a group
	 * @param groupName Name of the group to get the settings from
	 * @return The settings of the group
	 */
	public ConfigurationSection getGroupSettings(String groupName) {
		return groupsConfig.getConfigurationSection(groupName.toLowerCase());
	}
	
	/**
	 * Set a setting for a group
	 * @param group The group to set it for
	 * @param path The path to set
	 * @param setting The value to set
	 */
	public void setGroupSetting(RegionGroup group, String path, Object setting) {
		groupsConfig.set(group.getName().toLowerCase() + "." + path, setting);
	}
}















