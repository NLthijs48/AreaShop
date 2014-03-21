package nl.evolutioncoding.AreaShop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

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

public class ShopManager {
	private AreaShop plugin = null;
	private ObjectInputStream input = null;
	private ObjectOutputStream output = null;
	private HashMap<String,HashMap<String,String>> rents = null;
	private HashMap<String,HashMap<String,String>> buys = null;
	private String rentPath = null;
	private String buyPath = null;
	
	/**
	 * Constructor, initialize variabeles
	 * @param plugin
	 */
	public ShopManager(AreaShop plugin) {
		this.plugin = plugin;
		rents = new HashMap<>();
		buys = new HashMap<>();
		rentPath = plugin.getDataFolder().getPath() + File.separator + "rents";
		buyPath = plugin.getDataFolder().getPath() + File.separator + "buys";
		
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
	public void unRent(String regionName) {
		regionName = regionName.toLowerCase();
		HashMap<String,String> rent = rents.get(regionName);
	
		/* Get the time until the region will be rented */
		Long rentedUntil = Long.parseLong(rent.get(plugin.keyRentedUntil));
		Long currentTime = Calendar.getInstance().getTimeInMillis();
		Double timeLeft = (double) ((rentedUntil - currentTime));
		double price = Double.parseDouble(rent.get(plugin.keyPrice));
		double percentage = Integer.parseInt(plugin.config().getString("rentMoneyBack")) / 100;
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(0);

		String duration = rent.get(plugin.keyDuration);
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
		if(moneyBack > 0) {
			/* Give back the money */
			EconomyResponse r = plugin.getEconomy().depositPlayer(rent.get(plugin.keyPlayer), moneyBack);
			if(!r.transactionSuccess()) {
				plugin.getLogger().info("Something went wrong with paying back money while unrenting");
			}	
		}
		
		/* Set the flags and options for the region */
		plugin.getShopManager().setRegionFlags(regionName, plugin.config().getConfigurationSection("flagsForRent"), true);
		
		/* Debug message */
		plugin.debug(rent.get(plugin.keyPlayer) + " has unrented " + rent.get(plugin.keyName) + ", got " + plugin.getCurrencyCharacter() + moneyBack + " money back");
		
		/* Remove the player and renteduntil values */
		rent.remove(plugin.keyPlayer);
		rent.remove(plugin.keyRentedUntil);
		this.addRent(regionName, rent);
		
		/* Change the sign to [Rentable] */
		this.updateRentSign(regionName);
		
		this.saveRents();
	}
	
	/**
	 * Sell a buyed region, get part of the money back
	 * @param regionName
	 */
	public void unBuy(String regionName) {
		regionName = regionName.toLowerCase();
		HashMap<String,String> buy = buys.get(regionName);
		
		/* Give part of the buying price back */
		double price = Double.parseDouble(buy.get(plugin.keyPrice));
		double percentage = Integer.parseInt(plugin.config().getString("buyMoneyBack")) / 100;
		double moneyBack =  price * percentage;
		if(moneyBack > 0) {
			/* Give back the money */
			EconomyResponse r = plugin.getEconomy().depositPlayer(buy.get(plugin.keyPlayer), moneyBack);
			if(!r.transactionSuccess()) {
				plugin.getLogger().info("Something went wrong with paying back money while unrenting");
			}	
		} 
		
		/* Set the flags and options for the region */
		plugin.getShopManager().setRegionFlags(regionName, plugin.config().getConfigurationSection("flagsForSale"), false);
		
		/* Debug message */
		plugin.debug(buy.get(plugin.keyPlayer) + " has sold " + buy.get(plugin.keyName) + ", got " + plugin.getCurrencyCharacter() + moneyBack + " money back");
		
		/* Remove the player and buyeduntil values */
		buy.remove(plugin.keyPlayer);
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
		for(int i=0; i<rentNames.length; i++) {
			HashMap<String,String> rent = rents.get((String)rentNames[i]);
			String rentedUntil = rent.get(plugin.keyRentedUntil);
			if(rentedUntil != null) {
				Calendar now = Calendar.getInstance();
				Calendar until = Calendar.getInstance();
				until.setTime(new Date(Long.parseLong(rent.get(plugin.keyRentedUntil))));
				if(now.after(until)) {
					/* Send message to the player if online */
					Player player = Bukkit.getPlayer(rent.get(plugin.keyPlayer));
					if(player != null) {
						plugin.message(player, "unrent-expired", rent.get(plugin.keyName));
					}
					this.unRent(rent.get(plugin.keyName));
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
		HashMap<String,String> rent = plugin.getShopManager().getRent(regionName);
		if(rent == null) {
			plugin.message(player, "rent-regionNotRentable");
			return false;
		}
		Block block = Bukkit.getWorld(rent.get(plugin.keyWorld)).getBlockAt(Integer.parseInt(rent.get(plugin.keyX)), Integer.parseInt(rent.get(plugin.keyY)), Integer.parseInt(rent.get(plugin.keyZ)));
		
		/* Check if the player has permission */
		if(player.hasPermission("areashop.rent")) {	
			boolean extend = player.getName().equals(rent.get(plugin.keyPlayer));
			/* Check if the region is available for renting */
			if(rent.get(plugin.keyPlayer) == null || extend) {	
				
				if(!extend) {
					/* Check if the player can still rent */
					int rentNumber = 0;
					Iterator<String> it = rents.keySet().iterator();
					while(it.hasNext()) {
						if(player.getName().equals(rents.get(it.next()).get(plugin.keyPlayer))) {
							rentNumber++;
						}
					}
					int buyNumber = 0;
					it = buys.keySet().iterator();
					while(it.hasNext()) {
						if(player.getName().equals(buys.get(it.next()).get(plugin.keyPlayer))) {
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
				
				Double price = Double.parseDouble(rent.get(plugin.keyPrice));
				if(plugin.getEconomy().has(player.getName(), block.getWorld().getName(), price)) {
					Sign sign = (Sign)block.getState();
					
					/* Substract the money from the players balance */
					EconomyResponse r = plugin.getEconomy().withdrawPlayer(player.getName(), price);
					if(!r.transactionSuccess()) {
						plugin.message(player, "rent-payError");
						return false;
					}										
					
					/* Get the time until the region will be rented */
					Calendar calendar = Calendar.getInstance();
					if(extend) {
						calendar.setTimeInMillis(Long.parseLong(rent.get(plugin.keyRentedUntil)));
					}
			
					String duration = rent.get(plugin.keyDuration);
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
					SimpleDateFormat dateFull = new SimpleDateFormat("dd MMMMMMMMMMMMMMMMM yyyy HH:mm");
					
					/* Add values to the rent and send it to ShopManager */
					rent.put(plugin.keyRentedUntil, String.valueOf(calendar.getTimeInMillis()));
					rent.put(plugin.keyPlayer, player.getName());
					plugin.getShopManager().addRent(sign.getLine(1), rent);
	
					/* Change the sign */
					this.updateRentSign(regionName);
					
					/* Set the flags and options for the region */
					plugin.getShopManager().setRegionFlags(regionName, plugin.config().getConfigurationSection("flagsRented"), true);
					
					/* Send message to the player */
					if(extend) {
						plugin.message(player, "rent-extended", sign.getLine(1), dateFull.format(calendar.getTime()));
					} else {
						plugin.message(player, "rent-rented", sign.getLine(1), dateFull.format(calendar.getTime()));
						plugin.message(player, "rent-extend");
					}
					plugin.debug(player.getName() + " has rented region " + rent.get(plugin.keyName) + " for " + plugin.getCurrencyCharacter() + price + " until " + dateFull.format(calendar.getTime()));
					
					this.saveRents();
					return true;
				} else {
					/* Player has not enough money */
					if(extend) {
						plugin.message(player, "rent-lowMoneyExtend", plugin.getCurrencyCharacter() + plugin.getEconomy().getBalance(player.getName(), block.getWorld().getName()), plugin.getCurrencyCharacter() + price);
					} else {
						plugin.message(player, "rent-lowMoneyRent", plugin.getCurrencyCharacter() + plugin.getEconomy().getBalance(player.getName(), block.getWorld().getName()), plugin.getCurrencyCharacter() + price);
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
		HashMap<String,String> buy = plugin.getShopManager().getBuy(regionName);
		if(buy == null) {
			plugin.message(player, "rent-notBuyable");
			return true;
		}
		Block block = Bukkit.getWorld(buy.get(plugin.keyWorld)).getBlockAt(Integer.parseInt(buy.get(plugin.keyX)), Integer.parseInt(buy.get(plugin.keyY)), Integer.parseInt(buy.get(plugin.keyZ)));
		
		/* Check if the player has permission */
		if(player.hasPermission("areashop.buy")) {	
			if(buy.get(plugin.keyPlayer) == null) {					
	
				/* Check if the player can still buy */
				int buyNumber = 0;
				Iterator<String> it = buys.keySet().iterator();
				while(it.hasNext()) {
					if(player.getName().equals(buys.get(it.next()).get(plugin.keyPlayer))) {
						buyNumber++;
					}
				}
				int rentNumber = 0;
				it = rents.keySet().iterator();
				while(it.hasNext()) {
					if(player.getName().equals(rents.get(it.next()).get(plugin.keyPlayer))) {
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
				Double price = Double.parseDouble(buy.get(plugin.keyPrice));
				if(plugin.getEconomy().has(player.getName(), block.getWorld().getName(), price)) {
					Sign sign = (Sign)block.getState();
					
					/* Substract the money from the players balance */
					EconomyResponse r = plugin.getEconomy().withdrawPlayer(player.getName(), price);
					if(!r.transactionSuccess()) {
						plugin.message(player, "buy-payError");
						return false;
					}										
					
					/* Add values to the buy and send it to ShopManager */
					buy.put(plugin.keyPlayer, player.getName());
					plugin.getShopManager().addBuy(sign.getLine(1), buy);
	
					/* Change the sign */
					this.updateBuySign(regionName);
					
					/* Set the flags and options for the region */
					plugin.getShopManager().setRegionFlags(regionName, plugin.config().getConfigurationSection("flagsSold"), false);
					
					/* Send message to the player */
					plugin.message(player, "buy-succes", sign.getLine(1));
					plugin.debug(player.getName() + " has bought region " + buy.get(plugin.keyName) + " for " + plugin.getCurrencyCharacter() + price);
					
					this.saveBuys();
					return true;
				} else {
					/* Player has not enough money */
					plugin.message(player, "buy-lowMoney", plugin.getCurrencyCharacter() + plugin.getEconomy().getBalance(player.getName(), block.getWorld().getName()), plugin.getCurrencyCharacter() + price);
				}
			} else {
				if(player.getName().equals(buy.get(plugin.keyPlayer))) {
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
	public boolean removeRent(String regionName) {
		regionName = regionName.toLowerCase();
		boolean result = false;
		HashMap<String,String> rent = rents.get(regionName);
		if(rent != null) {
			if(rent.get(plugin.keyPlayer) != null) {
				this.unRent(regionName);
			}
			/* Delete the sign and the variable */
			Bukkit.getWorld(rent.get(plugin.keyWorld)).getBlockAt(Integer.parseInt(rent.get(plugin.keyX)), Integer.parseInt(rent.get(plugin.keyY)), Integer.parseInt(rent.get(plugin.keyZ))).setType(Material.AIR);
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
	public boolean removeBuy(String regionName) {
		regionName = regionName.toLowerCase();
		boolean result = false;
		HashMap<String,String> buy = buys.get(regionName);
		if(buy != null) {
			if(buy.get(plugin.keyPlayer) != null) {
				this.unBuy(regionName);
			}
			/* Delete the sign and the variable */
			Bukkit.getWorld(buy.get(plugin.keyWorld)).getBlockAt(Integer.parseInt(buy.get(plugin.keyX)), Integer.parseInt(buy.get(plugin.keyY)), Integer.parseInt(buy.get(plugin.keyZ))).setType(Material.AIR);
			buys.remove(regionName);
			this.saveBuys();
			result = true;
		}		
		return result;
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
			} catch (IOException | ClassNotFoundException e) {
				plugin.getLogger().info("Error: Something went wrong reading file: " + rentPath);
				error = true;
			}
			
			if(!error) {
				/* Check if regions and signs are still present */
				Object[] rentNames = rents.keySet().toArray();			
				for(int i=0; i<rentNames.length; i++) {
					String name = (String)rentNames[i];
					HashMap<String,String> rent = rents.get(name);
					
					/* If region is gone delete the rent and the sign */
					if(plugin.getWorldGuard().getRegionManager(Bukkit.getWorld(rent.get(plugin.keyWorld))).getRegion(name) == null) {
						this.removeRent(name);
						plugin.getLogger().info(name + " does not exist anymore, rent has been deleted");
					}
					
					/* If the sign is gone remove the rent */
					Block block = Bukkit.getWorld(rent.get(plugin.keyWorld)).getBlockAt(Integer.parseInt(rent.get(plugin.keyX)), Integer.parseInt(rent.get(plugin.keyY)), Integer.parseInt(rent.get(plugin.keyZ)));
					boolean remove = true;
					if(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
							remove = false;
					}
					if(remove) {
						/* remove the rent */
						if(this.removeRent(name)) {
							plugin.getLogger().info("Rent for " + name + " has been deleted, sign is not present");
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
			this.saveRents();
			plugin.getLogger().info("New file for rents created, should only happen when starting for the first time");
		}
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
			} catch (IOException | ClassNotFoundException e) {
				plugin.getLogger().info("Error: Something went wrong reading file: " + buyPath);
				error = true;
			}
			
			if(!error) {
				/* Check if regions and signs are still present */
				Object[] buyNames = buys.keySet().toArray();			
				for(int i=0; i<buyNames.length; i++) {
					String name = (String)buyNames[i];
					HashMap<String,String> buy = buys.get(name);
					
					/* If region is gone delete the buy and the sign */
					if(plugin.getWorldGuard().getRegionManager(Bukkit.getWorld(buy.get(plugin.keyWorld))).getRegion(name) == null) {
						this.removeBuy(name);
						plugin.getLogger().info("Region '" + name + "' does not exist anymore, buy has been deleted");
					}
					
					/* If the sign is gone remove the buy */
					Block block = Bukkit.getWorld(buy.get(plugin.keyWorld)).getBlockAt(Integer.parseInt(buy.get(plugin.keyX)), Integer.parseInt(buy.get(plugin.keyY)), Integer.parseInt(buy.get(plugin.keyZ)));
					boolean remove = true;
					if(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
							remove = false;
					}
					if(remove) {
						/* remove the buy */
						if(this.removeBuy(name)) {
							plugin.getLogger().info("Buy for region '" + name + "' has been deleted, sign is not present");
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
			this.saveBuys();
			plugin.getLogger().info("New file for buys created, should only happen when starting for the first time");
		}
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
			String world = rent.get(plugin.keyWorld);
			String x = rent.get(plugin.keyX);
			String y = rent.get(plugin.keyY);
			String z = rent.get(plugin.keyZ);
			String duration = rent.get(plugin.keyDuration);
			String price = rent.get(plugin.keyPrice);
			String player = rent.get(plugin.keyPlayer);
			String until = rent.get(plugin.keyRentedUntil);
			String name = rent.get(plugin.keyName);
			Block block = Bukkit.getWorld(world).getBlockAt(Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z));
			
			if(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
				Sign sign = (Sign)block.getState();
				if(player == null) {
					/* Not rented */
					sign.setLine(0, plugin.fixColors(plugin.config().getString("signRentable")));
					sign.setLine(1, name);
					sign.setLine(2, duration);
					sign.setLine(3, plugin.getCurrencyCharacter() + price);

				} else {
					/* Rented */
					SimpleDateFormat date = new SimpleDateFormat("dd-MM HH:mm");
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
			String world = buy.get(plugin.keyWorld);
			String x = buy.get(plugin.keyX);
			String y = buy.get(plugin.keyY);
			String z = buy.get(plugin.keyZ);
			String price = buy.get(plugin.keyPrice);
			String player = buy.get(plugin.keyPlayer);
			String name = buy.get(plugin.keyName);
			Block block = Bukkit.getWorld(world).getBlockAt(Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z));
			
			if(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
				Sign sign = (Sign)block.getState();
				if(player == null) {
					/* Not buyed */
					sign.setLine(0, plugin.fixColors(plugin.config().getString("signBuyable")));
					sign.setLine(1, name);
					sign.setLine(2, plugin.getCurrencyCharacter() + price);

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
			if(rent.get(plugin.keyPlayer) == null) {
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
			if(buy.get(plugin.keyPlayer) == null) {
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
		
		ProtectedRegion region = plugin.getWorldGuard().getRegionManager(Bukkit.getWorld(info.get(plugin.keyWorld))).getRegion(regionName);

		String playerName = info.get(plugin.keyPlayer);
		String price = plugin.getCurrencyCharacter() + info.get(plugin.keyPrice);
		String duration = info.get(plugin.keyDuration);
		String until = null;
		if(isRent && playerName != null) {
			SimpleDateFormat dateFull = new SimpleDateFormat("dd MMMMMMMMMMMMMMMMM yyyy HH:mm");
			until = dateFull.format(Long.parseLong(info.get(plugin.keyRentedUntil)));
		}
		
		Iterator<String> it = flagNames.iterator();
		while(it.hasNext()) {
			String flagName = it.next();
			String value = flags.getString(flagName);
			
			if(value != null && playerName != null) {
				value = value.replace(plugin.tagPlayerName, playerName);
			}
			if(value != null) {
				value = value.replace(plugin.tagRegionName, info.get(plugin.keyName));
			}
			if(value != null && price != null) {
				value = value.replace(plugin.tagPrice, price);
			}
			if(value != null && duration != null) {
				value = value.replace(plugin.tagDuration, duration);
			}
			if(value != null && until != null) {
				value = value.replace(plugin.tagRentedUntil, until);
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
				ProtectedRegion parentRegion = worldGuard.getRegionManager(Bukkit.getWorld(info.get(plugin.keyWorld))).getRegion(value);
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
					if(flagType != null) {
						flagValue = flagType.parseInput(worldGuard, null, value);
					}
				} catch (InvalidFlagFormat e) {
					plugin.getLogger().info("The value of flag " + flagName + " is wrong");
					result = false;
				}
				if(flagValue != null && flagType != null) {
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
			worldGuard.getRegionManager(Bukkit.getWorld(info.get(plugin.keyWorld))).save();
			plugin.debug("Regions saved");
		} catch (ProtectionDatabaseException e) {
			plugin.getLogger().info("Error: regions could not be saved");
		}
		return result;
	}
}















