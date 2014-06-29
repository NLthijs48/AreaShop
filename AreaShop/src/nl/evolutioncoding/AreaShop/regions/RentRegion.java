package nl.evolutioncoding.AreaShop.regions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.milkbowl.vault.economy.EconomyResponse;
import nl.evolutioncoding.AreaShop.AreaShop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class RentRegion extends GeneralRegion {

	private UUID rentingPlayer = null;
	private double price;
	private String duration = null;
	private long rentedUntil;
	
	/* Enum for schematic event types */
	public enum RentEvent {		
		CREATED("created"),
		DELETED("deleted"),
		RENTED("rented"),
		UNRENTED("unrented");
		
		private final String value;
		private RentEvent(String value) {
			this.value = value;
		}
		public String getValue() {
			return value;
		}
	} 
	
	/**
	 * Constructor
	 * @param plugin The areashop plugin
	 * @param settings All settings of this region
	 */
	public RentRegion(AreaShop plugin, Map<String, String> settings) {
		super(plugin, settings);
		
		if(settings.get(AreaShop.keyPlayerUUID) != null) {
			rentingPlayer = UUID.fromString(settings.get(AreaShop.keyPlayerUUID));
		}
		price = Double.parseDouble(settings.get(AreaShop.keyPrice));
		duration = settings.get(AreaShop.keyDuration);
		if(settings.get(AreaShop.keyRentedUntil) != null) {
			rentedUntil = Long.parseLong(settings.get(AreaShop.keyRentedUntil));
		}
		AreaShop.debug("RentRegion: " + getName() + ", map: " + settings.toString());
	}
	
	public RentRegion(AreaShop plugin, String name, Location signLocation, double price, String duration) {
		super(plugin, name, signLocation);
		
		this.price = price;
		this.duration = duration;
	}
	
	/**
	 * Get the UUID of the player renting the region
	 * @return The UUID of the renter
	 */
	public UUID getRenter() {
		return rentingPlayer;
	}
	
	/**
	 * Check if the region is rented
	 * @return true if the region is rented, otherwise false
	 */
	public boolean isRented() {
		return rentingPlayer != null;
	}
	
	/**
	 * Get the name of the player renting this region
	 * @return Name of the player renting this region
	 */
	public String getPlayerName() {
		return plugin.toName(rentingPlayer);
	}
	
	/**
	 * Get the time until this region is rented (time from 1970 epoch)
	 * @return
	 */
	public long getRentedUntil() {
		return rentedUntil;
	}
	
	/**
	 * Get the price of the region
	 * @return The price of the region
	 */
	public double getPrice() {
		return price;
	}
	
	/**
	 * Get the formatted string of the price (includes prefix and suffix)
	 * @return The formatted string of the price
	 */
	public String getFormattedPrice() {
		return plugin.formatCurrency(price);
	}

	/**
	 * Get the duration of 1 rent period
	 * @return The duration in milliseconds of 1 rent period
	 */
	public long getDuration() {
		/* Get the time until the region will be rented */
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(0);

		ArrayList<String> minutes = new ArrayList<String>(plugin.config().getStringList("minutes"));
		ArrayList<String> hours = new ArrayList<String>(plugin.config().getStringList("hours"));
		ArrayList<String> days = new ArrayList<String>(plugin.config().getStringList("days"));
		ArrayList<String> months = new ArrayList<String>(plugin.config().getStringList("months"));
		ArrayList<String> years = new ArrayList<String>(plugin.config().getStringList("years"));
		
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
		
		return calendar.getTimeInMillis();
	}
	
	/**
	 * Get the duration string, includes number<space>indentifier
	 * @return The duration string
	 */
	public String getDurationString() {
		return duration;
	}
	
	/**
	 * Change the price of the region
	 * @param price
	 */
	public void setPrice(double price) {
		this.price = price;
		updateSigns();
		updateRegionFlags();
	}
	
	public void setDuration(String duration) {
		this.duration = duration;
		updateSigns();
		updateRegionFlags();
	}

	@Override
	public HashMap<String, String> toMap() {
		HashMap<String, String> result = super.toMap();
		if(isRented()) {
			result.put(AreaShop.keyPlayerUUID, rentingPlayer.toString());
		}
		result.put(AreaShop.keyPrice, String.valueOf(price));
		result.put(AreaShop.keyDuration, duration);
		result.put(AreaShop.keyRentedUntil, String.valueOf(rentedUntil));
		return result;
	}
	
	/**
	 * Save this rent to a file (currently all rents will be saved again)
	 */
	@Override
	public void save() {
		getFileManager().saveRents();
	}
	
	public String[] getSignLines() {
		String[] lines = new String[4];
		if(isRented()) {
			SimpleDateFormat date = new SimpleDateFormat(plugin.config().getString("timeFormatSign"));
			String dateString = date.format(new Date(rentedUntil));					

			lines[0] = plugin.fixColors(plugin.config().getString("signRented"));
			lines[1] = getName();
			lines[2] = getPlayerName();
			lines[3] = dateString;		
		} else {			
			lines[0] = plugin.fixColors(plugin.config().getString("signRentable"));
			lines[1] = getName();
			lines[2] = duration;
			lines[3] = plugin.formatCurrency(price);			
		}		
		return lines;
	}

	@Override
	public void updateRegionFlags() {
		HashMap<String, String> replacements = new HashMap<String, String>();
		replacements.put(AreaShop.tagRegionName, getName());
		replacements.put(AreaShop.tagPrice, plugin.formatCurrency(price));
		replacements.put(AreaShop.tagDuration, duration);
		replacements.put(AreaShop.tagPlayerName, getPlayerName());
		if(isRented()) {
			replacements.put(AreaShop.tagRentedUntil, new SimpleDateFormat(plugin.config().getString("timeFormatChat")).format(rentedUntil));
			this.setRegionFlags(plugin.config().getConfigurationSection("flagsRented"), replacements);
		} else {
			this.setRegionFlags(plugin.config().getConfigurationSection("flagsForRent"), replacements);
		}		
	}
	
	/**
	 * Rent a region
	 * @param player The player that wants to rent the region
	 * @param regionName The name of the region you want to rent
	 * @return true if it succeeded and false if not
	 */
	public boolean rent(Player player) {
		Block block = getSignLocation().getBlock();
		
		/* Check if the player has permission */
		if(player.hasPermission("areashop.rent")) {	
			boolean extend = false;
			if(rentingPlayer != null && player.getUniqueId().equals(rentingPlayer)) {
				extend = true;
			}
			/* Check if the region is available for renting */
			if(!isRented() || extend) {				
				if(!extend) {
					/* Check if the player can still rent */
					int rentNumber = 0;
					Iterator<String> it = getFileManager().getRents().keySet().iterator();
					while(it.hasNext()) {
						String next = it.next();
						if(player.getUniqueId().equals(getFileManager().getRent(next).getRenter())) {
							rentNumber++;
						}
					}
					int buyNumber = 0;
					it = getFileManager().getBuys().keySet().iterator();
					while(it.hasNext()) {
						String next = it.next();
						if(player.getUniqueId().equals(getFileManager().getBuy(next).getOwner())) {
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

				if(plugin.getEconomy().has(player, getWorldName(), price)) {
					/* Substract the money from the players balance */
					EconomyResponse r = plugin.getEconomy().withdrawPlayer(player, price);
					if(!r.transactionSuccess()) {
						plugin.message(player, "rent-payError");
						return false;
					}										
					
					/* Get the time until the region will be rented */
					Calendar calendar = Calendar.getInstance();
					if(extend) {
						calendar.setTimeInMillis(rentedUntil);
					}
					calendar.setTimeInMillis(calendar.getTimeInMillis() + getDuration());
			
					SimpleDateFormat dateFull = new SimpleDateFormat(plugin.config().getString("timeFormatChat"));
					
					/* Add values to the rent and send it to FileManager */
					rentedUntil = calendar.getTimeInMillis();
					rentingPlayer = player.getUniqueId();
					
					if(!extend) {
						this.handleSchematicEvent(RentEvent.RENTED);
					}
					
					/* Change the sign and the region flags */
					updateSigns();
					updateRegionFlags();
					
					/* Send message to the player */
					if(extend) {
						plugin.message(player, "rent-extended", getName(), dateFull.format(calendar.getTime()));
					} else {
						plugin.message(player, "rent-rented", getName(), dateFull.format(calendar.getTime()));
						plugin.message(player, "rent-extend");
					}
					AreaShop.debug(player.getName() + " has rented region " + getName() + " for " + plugin.formatCurrency(price) + " until " + dateFull.format(calendar.getTime()));
					
					plugin.getFileManager().saveRents();
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
	 * Unrent a region, reset to unrented
	 * @param regionName Region that should be unrented
	 */
	public void unRent(boolean giveMoneyBack) {	
		/* Get the time until the region will be rented */
		Long currentTime = Calendar.getInstance().getTimeInMillis();
		Double timeLeft = (double) ((rentedUntil - currentTime));
		double percentage = (plugin.config().getDouble("rentMoneyBack")) / 100.0;

		Double timePeriod = (double) (getDuration());
		double periods = timeLeft / timePeriod;
		double moneyBack =  periods * price * percentage;
		if(moneyBack > 0 && giveMoneyBack) {
			/* Give back the money */
			EconomyResponse r = plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(rentingPlayer), moneyBack);
			if(!r.transactionSuccess()) {
				plugin.getLogger().info("Something went wrong with paying back money while unrenting");
			}	
		}

		
		/* Debug message */
		AreaShop.debug(getPlayerName() + " has unrented " + getName() + ", got " + plugin.formatCurrency(moneyBack) + " money back");
		
		/* Remove the player and renteduntil values */
		rentingPlayer = null;
		rentedUntil = 0;
		
		/* Update the signs and region flags */
		handleSchematicEvent(RentEvent.UNRENTED);
		updateSigns();
		updateRegionFlags();
		
		plugin.getFileManager().saveRents();
	}
	
	
	/**
	 * Checks an event and handles saving to and restoring from schematic for it
	 * @param type The type of event
	 */
	public void handleSchematicEvent(RentEvent type) {
		// Check for the general killswitch
		if(!plugin.config().getBoolean("enableSchematics")) {
			return;
		}
		// Check the individual options
		if("false".equalsIgnoreCase(getRestoreSetting())) {
			return;
		} else if("true".equalsIgnoreCase(getRestoreSetting())) {
		} else {
			if(!plugin.config().getBoolean("useRentRestore")) {
				return;
			}
		}
		// Get the safe and restore names		
		String save = plugin.config().getString("rentSchematicProfiles." + getRestoreProfile() + "." + type.getValue() + ".save");
		if(save == null) {
			plugin.config().getString("rentSchematicProfiles.default." + type.getValue() + ".save");
		}
		String restore = plugin.config().getString("rentSchematicProfiles." + getRestoreProfile() + "." + type.getValue() + ".restore");
		if(restore == null) {
			plugin.config().getString("rentSchematicProfiles.default." + type.getValue() + ".restore");
		}
		// Save the region if needed
		if(save != null && save.length() != 0) {
			save = save.replace(AreaShop.tagRegionName, getName());
			this.saveRegionBlocks(save);			
		}
		// Restore the region if needed
		if(restore != null && restore.length() != 0) {
			restore = restore.replace(AreaShop.tagRegionName, getName());
			this.restoreRegionBlocks(restore);		
		}
	}
	
	

}













