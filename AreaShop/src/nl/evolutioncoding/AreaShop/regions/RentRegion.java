package nl.evolutioncoding.AreaShop.regions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import net.milkbowl.vault.economy.EconomyResponse;
import nl.evolutioncoding.AreaShop.AreaShop;
import nl.evolutioncoding.AreaShop.Exceptions.RegionCreateException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class RentRegion extends GeneralRegion {
	
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
	 * @throws RegionCreateException 
	 */
	public RentRegion(AreaShop plugin, YamlConfiguration config) throws RegionCreateException {
		super(plugin, config);
	}
	
	public RentRegion(AreaShop plugin, String name, World world, Location signLocation, double price, String duration) {
		super(plugin, name, world, signLocation);
		setSetting("price", price);
		setSetting("duration", duration);
	}
	
	/**
	 * Get the UUID of the player renting the region
	 * @return The UUID of the renter
	 */
	public UUID getRenter() {
		String renter = getStringSetting("renter");
		if(renter != null) {
			try {
				return UUID.fromString(renter);
			} catch(IllegalArgumentException e) {}
		}
		return null;
	}
	
	public void setRenter(UUID renter) {
		if(renter == null) {
			setSetting("renter", null);
			setSetting("renterName", null);
		} else {
			setSetting("renter", renter.toString());
			setSetting("renterName", plugin.toName(renter));
		}
	}
	
	/**
	 * Check if the region is rented
	 * @return true if the region is rented, otherwise false
	 */
	public boolean isRented() {
		return getRenter() != null;
	}
	
	/**
	 * Get the name of the player renting this region
	 * @return Name of the player renting this region
	 */
	public String getPlayerName() {
		return plugin.toName(getRenter());
	}
	
	/**
	 * Get the time until this region is rented (time from 1970 epoch)
	 * @return
	 */
	public long getRentedUntil() {
		return getLongSetting("rentedUntil");
	}
	
	public void setRentedUntil(Long rentedUntil) {
		if(rentedUntil == null) {
			setSetting("rentedUntil", null);
		} else {
			setSetting("rentedUntil", rentedUntil);
		}
	}
	
	/**
	 * Get the price of the region
	 * @return The price of the region
	 */
	public double getPrice() {
		return getDoubleSetting("price");
	}
	
	/**
	 * Get the formatted string of the price (includes prefix and suffix)
	 * @return The formatted string of the price
	 */
	public String getFormattedPrice() {
		return plugin.formatCurrency(getPrice());
	}

	/**
	 * Get the duration of 1 rent period
	 * @return The duration in milliseconds of 1 rent period
	 */
	public long getDuration() {
		/* Get the time until the region will be rented */
		String duration = getDurationString();
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
		return getStringSetting("duration");
	}
	
	/**
	 * Change the price of the region
	 * @param price
	 */
	public void setPrice(double price) {
		setSetting("price", price);
		updateSigns();
		updateRegionFlags();
	}
	
	public void setDuration(String duration) {
		setSetting("duration", duration);
		updateSigns();
		updateRegionFlags();
	}
	
	public String[] getSignLines() {
		String[] lines = new String[4];
		if(isRented()) {
			SimpleDateFormat date = new SimpleDateFormat(plugin.config().getString("timeFormatSign"));
			String dateString = date.format(new Date(getRentedUntil()));					

			lines[0] = plugin.fixColors(plugin.config().getString("signRented"));
			lines[1] = getName();
			lines[2] = getPlayerName();
			lines[3] = dateString;		
		} else {			
			lines[0] = plugin.fixColors(plugin.config().getString("signRentable"));
			lines[1] = getName();
			lines[2] = getDurationString();
			lines[3] = getFormattedPrice();			
		}		
		return lines;
	}

	@Override
	public void updateRegionFlags() {
		HashMap<String, String> replacements = new HashMap<String, String>();
		replacements.put(AreaShop.tagRegionName, getName());
		replacements.put(AreaShop.tagPrice, getFormattedPrice());
		replacements.put(AreaShop.tagDuration, getDurationString());
		replacements.put(AreaShop.tagPlayerName, getPlayerName());
		if(isRented()) {
			replacements.put(AreaShop.tagRentedUntil, new SimpleDateFormat(plugin.config().getString("timeFormatChat")).format(getRentedUntil()));
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
			if(getRenter() != null && player.getUniqueId().equals(getRenter())) {
				extend = true;
			}
			/* Check if the region is available for renting */
			if(!isRented() || extend) {				
				if(!extend) {
					/* Check if the player can still rent */
					int rentNumber = 0;
					Iterator<RentRegion> itRent = getFileManager().getRents().iterator();
					while(itRent.hasNext()) {
						RentRegion next = itRent.next();
						if(player.getUniqueId().equals(next.getRenter())) {
							rentNumber++;
						}
					}
					int buyNumber = 0;
					Iterator<BuyRegion> itBuy = getFileManager().getBuys().iterator();
					while(itBuy.hasNext()) {
						BuyRegion next = itBuy.next();
						if(player.getUniqueId().equals(next.getBuyer())) {
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

				if(plugin.getEconomy().has(player, getWorldName(), getPrice())) {
					/* Substract the money from the players balance */
					EconomyResponse r = plugin.getEconomy().withdrawPlayer(player, getPrice());
					if(!r.transactionSuccess()) {
						plugin.message(player, "rent-payError");
						return false;
					}										
					
					/* Get the time until the region will be rented */
					Calendar calendar = Calendar.getInstance();
					if(extend) {
						calendar.setTimeInMillis(getRentedUntil());
					}
					calendar.setTimeInMillis(calendar.getTimeInMillis() + getDuration());
			
					SimpleDateFormat dateFull = new SimpleDateFormat(plugin.config().getString("timeFormatChat"));
					
					/* Add values to the rent and send it to FileManager */
					setRentedUntil(calendar.getTimeInMillis());
					setRenter(player.getUniqueId());
					
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
					AreaShop.debug(player.getName() + " has rented region " + getName() + " for " + getFormattedPrice() + " until " + dateFull.format(calendar.getTime()));
					
					this.save();
					return true;
				} else {
					/* Player has not enough money */
					if(extend) {
						plugin.message(player, "rent-lowMoneyExtend", plugin.formatCurrency(plugin.getEconomy().getBalance(player, block.getWorld().getName())), getFormattedPrice());
					} else {
						plugin.message(player, "rent-lowMoneyRent", plugin.formatCurrency(plugin.getEconomy().getBalance(player, block.getWorld().getName())), getFormattedPrice());
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
		Double timeLeft = (double) ((getRentedUntil() - currentTime));
		double percentage = (plugin.config().getDouble("rentMoneyBack")) / 100.0;

		Double timePeriod = (double) (getDuration());
		double periods = timeLeft / timePeriod;
		double moneyBack =  periods * getPrice() * percentage;
		if(moneyBack > 0 && giveMoneyBack) {
			/* Give back the money */
			EconomyResponse r = plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(getRenter()), moneyBack);
			if(!r.transactionSuccess()) {
				plugin.getLogger().info("Something went wrong with paying back money while unrenting");
			}	
		}

		
		/* Debug message */
		AreaShop.debug(getPlayerName() + " has unrented " + getName() + ", got " + plugin.formatCurrency(moneyBack) + " money back");
		
		/* Remove the player and renteduntil values */
		setRenter(null);
		setRentedUntil(null);
		
		/* Update the signs and region flags */
		handleSchematicEvent(RentEvent.UNRENTED);
		updateSigns();
		updateRegionFlags();
		
		this.save();
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













