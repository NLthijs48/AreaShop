package nl.evolutioncoding.AreaShop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import nl.evolutioncoding.AreaShop.Exceptions.RegionCreateException;
import nl.evolutioncoding.AreaShop.regions.BuyRegion;
import nl.evolutioncoding.AreaShop.regions.GeneralRegion;
import nl.evolutioncoding.AreaShop.regions.RegionGroup;
import nl.evolutioncoding.AreaShop.regions.RentRegion;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.google.common.io.Files;
import com.sk89q.worldguard.protection.flags.DefaultFlag;

public class FileManager {
	private static FileManager instance = null;
	
	private AreaShop plugin = null;
	private ObjectInputStream input = null;
	private ObjectOutputStream output = null;
	private HashMap<String, GeneralRegion> regions = null;
	private String regionsPath = null;
	private HashMap<String, RegionGroup> groups = null;
	private String groupsPath = null;
	private YamlConfiguration groupsConfig = null;
	private String defaultPath = null;
	private YamlConfiguration defaultConfig = null;
	
	private HashMap<String,Integer> versions = null;
	private String versionPath = null;
	private String schemFolder = null;
	
	/**
	 * Constructor, initialize variabeles
	 * @param plugin
	 */
	public FileManager(AreaShop plugin) {
		this.plugin = plugin;
		regions = new HashMap<String, GeneralRegion>();
		regionsPath = plugin.getDataFolder() + File.separator + AreaShop.regionsFolder;
		groups = new HashMap<String, RegionGroup>();
		groupsPath = plugin.getDataFolder() + File.separator + AreaShop.groupsFile;
		defaultPath = plugin.getDataFolder() + File.separator + AreaShop.defaultFile;
		versionPath = plugin.getDataFolder().getPath() + File.separator + AreaShop.versionFile;
		schemFolder = plugin.getDataFolder() + File.separator + AreaShop.schematicFolder;
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
	
	/**
	 * Add a rent to the list
	 * @param regionName Name of the region that can be rented
	 * @param rent Map containing all the info for a rent
	 */
	public void addRent(RentRegion rent) {
		regions.put(rent.getName().toLowerCase(), rent);
		rent.save();
	}
	
	/**
	 * Add a buy to the list
	 * @param regionName Name of the region that can be buyed
	 * @param buy Map containing all the info for a buy
	 */
	public void addBuy(BuyRegion buy) {
		regions.put(buy.getName().toLowerCase(), buy);
		buy.save();
	}
	
	/**
	 * Remove a rent from the list
	 * @param regionName
	 */
	public boolean removeRent(String regionName, boolean giveMoneyBack) {
		regionName = regionName.toLowerCase();
		boolean result = false;
		RentRegion rent = getRent(regionName);
		if(rent != null) {
			if(rent.isRented()) {
				rent.unRent(giveMoneyBack);
			}
			/* Delete the sign and the variable */
			if(rent.getWorld() != null) {
				rent.getWorld().getBlockAt(rent.getSignLocation()).setType(Material.AIR);
			}
			if(rent.getRegion() != null) {
				rent.getRegion().setFlag(DefaultFlag.GREET_MESSAGE, null);
				rent.getRegion().setFlag(DefaultFlag.FAREWELL_MESSAGE, null);
			}
			regions.remove(regionName);
			File file = new File(plugin.getDataFolder() + File.separator + AreaShop.regionsFolder + File.separator + regionName + ".yml");
			boolean deleted = true;
			try {
				deleted = file.delete();
			} catch(Exception e) {
				deleted = false;
			}
			if(!deleted) {
				plugin.getLogger().warning("File could not be deleted: " + file.toString());
			}
			for(RegionGroup group : getGroups()) {
				group.removeMember(rent);
			}
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
		BuyRegion buy = getBuy(regionName);
		if(buy != null) {
			if(buy.isSold()) {
				buy.sell(giveMoneyBack);
			}
			// Delete the sign and the variable
			if(buy.getWorld() != null) {
				buy.getWorld().getBlockAt(buy.getSignLocation()).setType(Material.AIR);
			}			
			regions.remove(regionName);
			if(buy.getRegion() != null) {
				buy.getRegion().setFlag(DefaultFlag.GREET_MESSAGE, null);
				buy.getRegion().setFlag(DefaultFlag.FAREWELL_MESSAGE, null);
			}
			// Deleting the file
			File file = new File(plugin.getDataFolder() + File.separator + AreaShop.regionsFolder + File.separator + regionName + ".yml");
			boolean deleted = true;
			try {
				deleted = file.delete();
			} catch(Exception e) {
				deleted = false;
			}
			if(!deleted) {
				plugin.getLogger().warning("File could not be deleted: " + file.toString());
			}
			
			// Removing from groups
			for(RegionGroup group : getGroups()) {
				group.removeMember(buy);
			}
			result = true;
		}		
		return result;
	}
	
	/**
	 * Update all rent signs
	 * @return true if all signs are updated, otherwise false
	 */
	public boolean updateRentSigns() {
		boolean result = true;			
		for(RentRegion rent : getRents()) {
			result = result & rent.updateSigns();
		}		
		return result;		
	}
	
	/**
	 * Update all buy signs
	 * @return true if all signs are updated, otherwise false
	 */
	public boolean updateBuySigns() {
		boolean result = true;			
		for(BuyRegion buy : getBuys()) {
			result = result & buy.updateSigns();
		}		
		return result;		
	}
	
	/**
	 * Update all regions registered for renting
	 */
	public void updateRentRegions() {		
		for(RentRegion rent : getRents()) {
			rent.updateRegionFlags();
		}
	}
	
	/**
	 * Update all regions registered for buying
	 */
	public void updateBuyRegions() {			
		for(BuyRegion buy : getBuys()) {
			buy.updateRegionFlags();
		}
	}
	
	public void saveGroups() {
		try {
			groupsConfig.save(groupsPath);
		} catch (IOException e) {
			plugin.getLogger().warning("Groups file could not be saved: " + groupsPath);
		}
	}
	
	/**
	 * Save all region related files
	 */
	public void saveAll() {
		// Safe regions
		for(GeneralRegion region : getRegions()) {
			region.save();
		}
		
		// Safe groups and default config
		this.saveGroups();
		try {
			defaultConfig.save(defaultPath);
		} catch (IOException e) {
			plugin.getLogger().warning("Default file could not be saved: " + defaultPath);
		}	
		
	}
	
	public String getRegionFolder() {
		return regionsPath;
	}
	
	/**
	 * Unrent region that have no time left
	 */
	public void checkRents() {
		/* Check if regions and signs are still present */		
		long now = Calendar.getInstance().getTimeInMillis();
		for(RentRegion rent : getRents()) {
			if(rent.isRented() && now > rent.getRentedUntil()) {
				/* Send message to the player if online */
				Player player = Bukkit.getPlayer(rent.getRenter());
				if(player != null) {
					plugin.message(player, "unrent-expired", rent.getName());
				}
				rent.unRent(false);				
			}
		}	
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
			plugin.getLogger().info("File could not be saved: " + versionPath);
		}
	}

	public boolean loadFiles() {
		boolean result = false;
		
		convertFiles();
		
		// Load regions
		File file = new File(regionsPath);
		if(!file.exists()) {
			file.mkdirs();
		} else if(file.isDirectory()) {
			for(File region : file.listFiles()) {
				if(region.isFile()) {
					YamlConfiguration config = YamlConfiguration.loadConfiguration(region);					
					if(config.isSet("rent")) {
						try {
							RentRegion rent = new RentRegion(plugin, config);
							addRent(rent);
						} catch (RegionCreateException exception) {
							plugin.getLogger().warning(exception.getMessage());
							try {
								region.delete();
							} catch(Exception e) {}
						}
						
					} else if(config.isSet("buy")) {
						try {
							BuyRegion buy = new BuyRegion(plugin, config);
							addBuy(buy);
						} catch (RegionCreateException exception) {
							plugin.getLogger().warning(exception.getMessage());
							try {
								region.delete();
							} catch(Exception e) {}
						}						
					}
				}
			}
		}
		
		// Load groups
		File groupFile = new File(groupsPath);
		if(groupFile.exists() && groupFile.isFile()) {
			groupsConfig = YamlConfiguration.loadConfiguration(groupFile);
		} else {
			groupsConfig = new YamlConfiguration();
		}
		for(String groupName : groupsConfig.getKeys(false)) {
			RegionGroup group = new RegionGroup(plugin, groupName);
			for(String region : groupsConfig.getConfigurationSection(groupName).getStringList("regions")) {
				group.addMember(regions.get(region.toLowerCase()));
			}
			groups.put(groupName, group);
		}
		
		// Load default settings
		File defaultFile = new File(defaultPath);
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
				plugin.getLogger().info("File with default region settings has been saved, should only happen the first time");
			} catch(IOException e) {
				try {
					input.close();
					output.close();
				} catch (IOException e1) {} catch (NullPointerException e2) {}
				
				plugin.getLogger().warning("Something went wrong saving the default region settings: " + defaultFile.getPath());
			}
		}		
		defaultConfig = YamlConfiguration.loadConfiguration(defaultFile);		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public void convertFiles() {
		String rentPath = plugin.getDataFolder() + File.separator + "rents";
		String buyPath = plugin.getDataFolder() + File.separator + "buys";
		File rentFile = new File(rentPath);
		File buyFile = new File(buyPath);
		String oldFolderPath = plugin.getDataFolder() + File.separator + "#old" + File.separator;
		File oldFolderFile = new File(oldFolderPath);
				
		// If the the files are already the current version
		if(versions.get(AreaShop.versionFiles) != null && versions.get(AreaShop.versionFiles) == AreaShop.versionFilesCurrent) {
			return;
		}
		
		// Convert old rent files
		if(rentFile.exists()) {
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
				plugin.getLogger().warning("Error: Something went wrong reading file: " + rentPath);
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
					plugin.getLogger().info("Could not create a backup of '" + rentPath + "', check the file permissions (conversion to next version continues)");
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
						plugin.getLogger().info("Updated version of '" + buyPath + "' from -1 to 0 (switch to using lowercase region names, adding default schematic enabling and profile)");
					}
					if(versions.get("rents") < 1) {
						plugin.getLogger().info("Starting UUID conversion of '" + buyPath + "', could take a while");
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
						plugin.getLogger().info("Updated version of '" + rentPath + "' from 0 to 1 (switch to UUID's for player identification)");
					}				
				}		
				// Save rents to new format
				File regionsFile = new File(regionsPath);
				if(!regionsFile.exists()) {
					regionsFile.mkdirs();
				}
				for(HashMap<String, String> rent : rents.values()) {
					YamlConfiguration config = new YamlConfiguration();
					config.set("name", rent.get("name").toLowerCase());
					config.set("rent.world", rent.get("world"));
					config.set("rent.signLocation.world", rent.get("world"));
					config.set("rent.signLocation.x", Double.parseDouble(rent.get("x")));
					config.set("rent.signLocation.y", Double.parseDouble(rent.get("y")));
					config.set("rent.signLocation.z", Double.parseDouble(rent.get("z")));
					config.set("rent.price", Double.parseDouble(rent.get("price")));
					config.set("rent.duration", rent.get("duration"));
					if(rent.get("restore") != null && !rent.get("restore").equals("general")) {
						config.set("rent.enableRestore", rent.get("restore"));
					}
					if(rent.get("profile") != null && !rent.get("profile").equals("default")) {
						config.set("rent.restoreProfile", rent.get("profile"));
					}
					if(rent.get("tpx") != null) {
						config.set("rent.teleportLocation.world", rent.get("world"));
						config.set("rent.teleportLocation.x", Double.parseDouble(rent.get("tpx")));
						config.set("rent.teleportLocation.y", Double.parseDouble(rent.get("tpy")));
						config.set("rent.teleportLocation.z", Double.parseDouble(rent.get("tpz")));
						config.set("rent.teleportLocation.yaw", Double.parseDouble(rent.get("tpyaw")));
						config.set("rent.teleportLocation.pitch", Double.parseDouble(rent.get("tppitch")));
					}
					if(rent.get("playeruuid") != null) {
						config.set("rent.renter", rent.get("playeruuid"));
						config.set("rent.rentedUntil", Long.parseLong(rent.get("rented")));
					}
					try {
						config.save(new File(regionsPath + File.separator + rent.get("name").toLowerCase() + ".yml"));
					} catch (IOException e) {
						plugin.getLogger().warning("Error: Could not save region file while converting: " + regionsPath + File.separator + rent.get("name").toLowerCase() + ".yml");
					}
				}
			}
			
			// Change version number
			versions.remove("rents");
			versions.put(AreaShop.versionFiles, AreaShop.versionFilesCurrent);			
			saveVersions();			
		}
		
		if(buyFile.exists()) {
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
				plugin.getLogger().warning("Error: Something went wrong reading file: " + buyPath);
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
					plugin.getLogger().info("Could not create a backup of '" + buyPath + "', check the file permissions (conversion to next version continues)");
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
						plugin.getLogger().info("Updated version of '" + buyPath + "' from -1 to 0 (switch to using lowercase region names, adding default schematic enabling and profile)");
					}
					if(versions.get("buys") < 1) {
						plugin.getLogger().info("Starting UUID conversion of '" + buyPath + "', could take a while");
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
						plugin.getLogger().info("Updated version of '" + buyPath + "' from 0 to 1 (switch to UUID's for player identification)");
					}				
				}		
			
				// Save buys to new format
				File regionsFile = new File(regionsPath);
				if(!regionsFile.exists()) {
					regionsFile.mkdirs();
				}
				for(HashMap<String, String> buy : buys.values()) {
					YamlConfiguration config = new YamlConfiguration();
					config.set("name", buy.get("name").toLowerCase());
					config.set("buy.world", buy.get("world"));
					config.set("buy.signLocation.world", buy.get("world"));
					config.set("buy.signLocation.x", Double.parseDouble(buy.get("x")));
					config.set("buy.signLocation.y", Double.parseDouble(buy.get("y")));
					config.set("buy.signLocation.z", Double.parseDouble(buy.get("z")));
					config.set("buy.price", Double.parseDouble(buy.get("price")));
					config.set("buy.duration", buy.get("duration"));
					if(buy.get("restore") != null && !buy.get("restore").equals("general")) {
						config.set("buy.enableRestore", buy.get("restore"));
					}
					if(buy.get("profile") != null && !buy.get("profile").equals("default")) {
						config.set("buy.restoreProfile", buy.get("profile"));
					}
					if(buy.get("tpx") != null) {
						config.set("buy.teleportLocation.world", buy.get("world"));
						config.set("buy.teleportLocation.x", Double.parseDouble(buy.get("tpx")));
						config.set("buy.teleportLocation.y", Double.parseDouble(buy.get("tpy")));
						config.set("buy.teleportLocation.z", Double.parseDouble(buy.get("tpz")));
						config.set("buy.teleportLocation.yaw", Double.parseDouble(buy.get("tpyaw")));
						config.set("buy.teleportLocation.pitch", Double.parseDouble(buy.get("tppitch")));
					}
					if(buy.get("playeruuid") != null) {
						config.set("buy.buyer", buy.get("playeruuid"));
					}
					try {
						config.save(new File(regionsPath + File.separator + buy.get("name").toLowerCase() + ".yml"));
					} catch (IOException e) {
						plugin.getLogger().warning("Error: Could not save region file while converting: " + regionsPath + File.separator + buy.get("name").toLowerCase() + ".yml");
					}
				}
			}

			// Change version number
			versions.remove("buys");
			versions.put(AreaShop.versionFiles, AreaShop.versionFilesCurrent);			
			saveVersions();			
		}
		
		
		try {
			Files.move(new File(rentPath + ".old"), new File(oldFolderPath + "rents.old"));
			Files.move(new File(buyPath + ".old"), new File(oldFolderPath + "buys.old"));
		} catch (Exception e) {}
	}
	
	/**
	 * Get the settings of a group
	 * @param groupName Name of the group to get the settings from
	 * @return The settings of the group
	 */
	public ConfigurationSection getGroupSettings(String groupName) {
		return groupsConfig.getConfigurationSection(groupName.toLowerCase());
	}
}















