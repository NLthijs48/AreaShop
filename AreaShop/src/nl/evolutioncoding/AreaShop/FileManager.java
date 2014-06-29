package nl.evolutioncoding.AreaShop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import net.minecraft.util.org.apache.commons.io.FileUtils;
import nl.evolutioncoding.AreaShop.regions.BuyRegion;
import nl.evolutioncoding.AreaShop.regions.RentRegion;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class FileManager {
	private static FileManager instance = null;
	
	private AreaShop plugin = null;
	private ObjectInputStream input = null;
	private ObjectOutputStream output = null;
	private HashMap<String, RentRegion> rents = null;
	private HashMap<String, BuyRegion> buys = null;
	private HashMap<String,Integer> versions = null;
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
	
	public RentRegion getRent(String name) {
		return rents.get(name.toLowerCase());
	}
	
	public BuyRegion getBuy(String name) {
		return buys.get(name.toLowerCase());
	}
	
	public HashMap<String, RentRegion> getRents() {
		return rents;
	}
	
	public HashMap<String, BuyRegion> getBuys() {
		return buys;
	}
	
	public List<String> getBuyNames() {
		ArrayList<String> result = new ArrayList<String>();
		for(BuyRegion region : plugin.getFileManager().getBuys().values()) {
			result.add(region.getName());
		}
		return result;
	}
	
	public List<String> getRentNames() {
		ArrayList<String> result = new ArrayList<String>();
		for(RentRegion region : plugin.getFileManager().getRents().values()) {
			result.add(region.getName());
		}
		return result;
	}
	
	/**
	 * Add a rent to the list
	 * @param regionName Name of the region that can be rented
	 * @param rent Map containing all the info for a rent
	 */
	public void addRent(String regionName, RentRegion rent) {
		rents.put(regionName.toLowerCase(), rent);
		plugin.getFileManager().saveRents();
	}
	
	/**
	 * Add a buy to the list
	 * @param regionName Name of the region that can be buyed
	 * @param buy Map containing all the info for a buy
	 */
	public void addBuy(String regionName, BuyRegion buy) {
		buys.put(regionName.toLowerCase(), buy);
		plugin.getFileManager().saveBuys();
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
		BuyRegion buy = getBuy(regionName);
		if(buy != null) {
			if(buy.isSold()) {
				buy.sell(giveMoneyBack);
			}
			/* Delete the sign and the variable */
			if(buy.getWorld() != null) {
				buy.getWorld().getBlockAt(buy.getSignLocation()).setType(Material.AIR);
			}			
			buys.remove(regionName);
			this.saveBuys();
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
		for(String name : rents.keySet()) {
			result = result & rents.get(name).updateSigns();
		}		
		return result;		
	}
	
	/**
	 * Update all buy signs
	 * @return true if all signs are updated, otherwise false
	 */
	public boolean updateBuySigns() {
		boolean result = true;			
		for(String name : buys.keySet()) {
			result = result & buys.get(name).updateSigns();
		}		
		return result;		
	}
	
	/**
	 * Update all regions registered for renting
	 */
	public void updateRentRegions() {
		Object[] rentNames = rents.keySet().toArray();			
		for(int i=0; i<rentNames.length; i++) {
			rents.get((String)rentNames[i]).updateRegionFlags();
		}
	}
	
	/**
	 * Update all regions registered for buying
	 */
	public void updateBuyRegions() {
		Object[] buyNames = buys.keySet().toArray();			
		for(int i=0; i<buyNames.length; i++) {
			buys.get((String)buyNames[i]).updateRegionFlags();
		}
	}
	
		
	/**
	 * Save all rents to disk
	 */
	public void saveRents() {
		try {
			HashMap<String, HashMap<String, String>> outputMap = new HashMap<String, HashMap<String, String>>();
			for(String rent : rents.keySet()) {
				outputMap.put(rent, rents.get(rent).toMap());
			}
			output = new ObjectOutputStream(new FileOutputStream(rentPath));
			output.writeObject(outputMap);
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
			HashMap<String, HashMap<String, String>> outputMap = new HashMap<String, HashMap<String, String>>();
			for(String buy : buys.keySet()) {
				outputMap.put(buy, buys.get(buy).toMap());
			}
			output = new ObjectOutputStream(new FileOutputStream(buyPath));
			output.writeObject(outputMap);
			output.close();
		} catch (IOException e) {
			plugin.getLogger().info("File could not be saved: " + buyPath);
		}
		
	}
	
	/**
	 * Unrent region that have no time left
	 */
	public void checkRents() {
		/* Check if regions and signs are still present */
		Object[] rentNames = rents.keySet().toArray();			
		long now = Calendar.getInstance().getTimeInMillis();
		for(int i=0; i<rentNames.length; i++) {
			RentRegion rent = getRent((String)rentNames[i]);
			if(rent.isRented()) {
				if(now > rent.getRentedUntil()) {
					/* Send message to the player if online */
					Player player = Bukkit.getPlayer(rent.getRenter());
					if(player != null) {
						plugin.message(player, "unrent-expired", rent.getName());
					}
					rent.unRent(true);
				}
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
	 * Loads the rents from disk
	 * @return true if the file is read successfully, else false
	 */
	@SuppressWarnings("unchecked")
	public boolean loadRents() {
		// TODO do this somewhere, exeption in constructor of RentRegion?
		/*
					// If region is gone delete the rent and the sign 
					if(Bukkit.getWorld(rent.get(AreaShop.keyWorld)) == null 
							|| plugin.getWorldGuard().getRegionManager(Bukkit.getWorld(rent.get(AreaShop.keyWorld))) == null 
							|| plugin.getWorldGuard().getRegionManager(Bukkit.getWorld(rent.get(AreaShop.keyWorld))).getRegion(rentName) == null) {
						this.removeRent(rentName, false);
						plugin.getLogger().info(rentName + " does not exist anymore, rent has been deleted");
					} else {
						// If the sign is gone remove the rent 
						Block block = Bukkit.getWorld(rent.get(AreaShop.keyWorld)).getBlockAt(Integer.parseInt(rent.get(AreaShop.keyX)), Integer.parseInt(rent.get(AreaShop.keyY)), Integer.parseInt(rent.get(AreaShop.keyZ)));
						if(!(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST)) {
							// remove the rent 
							if(this.removeRent(rentName, false)) {
								plugin.getLogger().info("Rent for " + rentName + " has been deleted, sign is not present");
							}
						}
					}	
					
		 */
		
		// Read file
		HashMap<String,HashMap<String,String>> rents = null;
		String rentPath = plugin.getDataFolder().getPath() + File.separator + "rents";
		File file = new File(rentPath);
		if(!file.exists()) {
			rents = new HashMap<String, HashMap<String, String>>();
			this.saveRents();
			plugin.getLogger().info("New file for rents created, should only happen when starting for the first time");
		} else {
			try {
				ObjectInputStream input = new ObjectInputStream(new FileInputStream(rentPath));
		    	rents = (HashMap<String,HashMap<String,String>>)input.readObject();
				input.close();
			} catch (IOException | ClassNotFoundException | ClassCastException e) {
				plugin.getLogger().info("Error: Something went wrong reading file: " + rentPath);
				return false;
			}
			
			// Check if a conversion is needed
			if(versions.get(AreaShop.versionRentKey) < AreaShop.versionRentCurrent) {
				if(versions.get(AreaShop.versionRentKey) < 1) {
					// Backup current file
					try {
						FileUtils.copyFile(new File(rentPath), new File(rentPath + ".old"));
					} catch (IOException e) {
						plugin.getLogger().info("Could not create a backup of '" + rentPath + "', check the file permissions (conversion to next version continues)");
					}
					
					/* Upgrade the rent to the latest version */
					if(versions.get(AreaShop.versionRentKey) < 0) {
						for(String rentName : rents.keySet()) {
							HashMap<String,String> rent = rents.get(rentName);
							/* Save the rentName in the hashmap and use a small caps rentName as key */
							if(rent.get(AreaShop.keyName) == null) {
								rent.put(AreaShop.keyName, rentName);
								rents.remove(rentName);
								rents.put(rentName.toLowerCase(), rent);
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
						plugin.getLogger().info("Updated version of '" + buyPath + "' from -1 to 0 (switch to using lowercase region names, adding default schematic enabling and profile)");
					}
					if(versions.get(AreaShop.versionRentKey) < 1) {
						plugin.getLogger().info("Starting UUID conversion of '" + buyPath + "', could take a while");
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
						plugin.getLogger().info("Updated version of '" + rentPath + "' from 0 to 1 (switch to UUID's for player identification)");
					}		
				}
			}
		}
		
		// Add them all to the RegionManager
		for(String rent : rents.keySet()) {
			HashMap<String, String> map = rents.get(rent);
			addRent(rent, new RentRegion(plugin, map));
			
		}	
				
		/* Output info to console */
		if(rents.keySet().size() == 1) {
			AreaShop.debug(rents.keySet().size() + " rent loaded");
		} else {
			AreaShop.debug(rents.keySet().size() + " rents loaded");
		}
		return true;
	}
	
	/**
	 * Load the buys file from disk
	 * @return true if the file is read successfully, else false
	 */
	@SuppressWarnings("unchecked")
	public boolean loadBuys() {
		// TODO do this somewhere, exeption in constructor of BuyRegion?
				/*
					// If region is gone delete the buy and the sign
					if(Bukkit.getWorld(buy.get(AreaShop.keyWorld)) == null 
							|| plugin.getWorldGuard().getRegionManager(Bukkit.getWorld(buy.get(AreaShop.keyWorld))) == null 
							|| plugin.getWorldGuard().getRegionManager(Bukkit.getWorld(buy.get(AreaShop.keyWorld))).getRegion(buyName) == null) {
						this.removeBuy(buyName, false);
						plugin.getLogger().info("Region '" + buyName + "' does not exist anymore, buy has been deleted");
					} else {
						// If the sign is gone remove the buy
						Block block = Bukkit.getWorld(buy.get(AreaShop.keyWorld)).getBlockAt(Integer.parseInt(buy.get(AreaShop.keyX)), Integer.parseInt(buy.get(AreaShop.keyY)), Integer.parseInt(buy.get(AreaShop.keyZ)));
						if(!(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST)) {
							// remove the buy
							if(this.removeBuy(buyName, false)) {
								plugin.getLogger().info("Buy for region '" + buyName + "' has been deleted, sign is not present");
							}
						}

							
				 */
				
		// Read file
		HashMap<String,HashMap<String,String>> buys = null;
		String buyPath = plugin.getDataFolder().getPath() + File.separator + "buys";
		File file = new File(buyPath);
		if(!file.exists()) {
			buys = new HashMap<String, HashMap<String, String>>();
			this.saveBuys();
			plugin.getLogger().info("New file for buys created, should only happen when starting for the first time");
		} else {
			try {
				ObjectInputStream input = new ObjectInputStream(new FileInputStream(buyPath));
		    	buys = (HashMap<String,HashMap<String,String>>)input.readObject();
				input.close();
			} catch (IOException | ClassNotFoundException | ClassCastException e) {
				plugin.getLogger().info("Error: Something went wrong reading file: " + buyPath);
				return false;
			}
			
			// Check if a conversion is needed
			if(versions.get(AreaShop.versionRentKey) < AreaShop.versionRentCurrent) {
				if(versions.get(AreaShop.versionRentKey) < 1) {
					// Backup current file
					try {
						FileUtils.copyFile(new File(buyPath), new File(buyPath + ".old"));
					} catch (IOException e) {
						plugin.getLogger().info("Could not create a backup of '" + buyPath + "', check the file permissions (conversion to next version continues)");
					}
					
					/* Upgrade the rent to the latest version */
					if(versions.get(AreaShop.versionBuyKey) < 0) {
						for(String rentName : buys.keySet()) {
							HashMap<String,String> rent = buys.get(rentName);
							/* Save the rentName in the hashmap and use a small caps rentName as key */
							if(rent.get(AreaShop.keyName) == null) {
								rent.put(AreaShop.keyName, rentName);
								buys.remove(rentName);
								buys.put(rentName.toLowerCase(), rent);
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
							versions.put(AreaShop.versionBuyKey, 0);
							this.saveVersions();
						}
						plugin.getLogger().info("Updated version of '" + buyPath + "' from -1 to 0 (switch to using lowercase region names, adding default schematic enabling and profile)");
					}
					if(versions.get(AreaShop.versionBuyKey) < 1) {
						plugin.getLogger().info("Starting UUID conversion of '" + buyPath + "', could take a while");
						for(String rentName : buys.keySet()) {
							HashMap<String,String> rent = buys.get(rentName);
							if(rent.get(AreaShop.oldKeyPlayer) != null) {
								@SuppressWarnings("deprecation")  // Fake deprecation by Bukkit to inform developers, method will stay
								OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(rent.get(AreaShop.oldKeyPlayer));
								rent.put(AreaShop.keyPlayerUUID, offlinePlayer.getUniqueId().toString());		
								rent.remove(AreaShop.oldKeyPlayer);
							}					
							/* Change version to 1 */
							versions.put(AreaShop.versionBuyKey, 1);
							this.saveVersions();
						}
						plugin.getLogger().info("Updated version of '" + buyPath + "' from 0 to 1 (switch to UUID's for player identification)");
					}		
				}
			}
		}
		
		// Add them all to the RegionManager
		for(String rent : buys.keySet()) {
			HashMap<String, String> map = buys.get(rent);
			addBuy(rent, new BuyRegion(plugin, map));
			
		}	
				
		/* Output info to console */
		if(buys.keySet().size() == 1) {
			AreaShop.debug(buys.keySet().size() + " buy loaded");
		} else {
			AreaShop.debug(buys.keySet().size() + " buys loaded");
		}
		return true;
	}
}















