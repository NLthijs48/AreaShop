package nl.evolutioncoding.AreaShop.regions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import net.milkbowl.vault.economy.EconomyResponse;
import nl.evolutioncoding.AreaShop.AreaShop;
import nl.evolutioncoding.AreaShop.Exceptions.RegionCreateException;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class RentRegion extends GeneralRegion {	
	/**
	 * Constructor
	 * @param plugin The areashop plugin
	 * @param settings All settings of this region
	 * @throws RegionCreateException 
	 */
	public RentRegion(AreaShop plugin, YamlConfiguration config) throws RegionCreateException {
		super(plugin, config);
	}
	
	/**
	 * Create a new RentRegion
	 * @param plugin The AreaShop plugin
	 * @param name The name of the region (correct casing)
	 * @param world The world of the WorldGuard region
	 */
	public RentRegion(AreaShop plugin, String name, World world) {
		super(plugin, name, world);
	}
	
	@Override
	public RegionType getType() {
		return RegionType.RENT;
	}
	
	@Override
	public RegionState getState() {
		if(isRented()) {
			return RegionState.RENTED;
		} else {
			return RegionState.FORRENT;
		}
	}
	
	/**
	 * Get the UUID of the player renting the region
	 * @return The UUID of the renter
	 */
	public UUID getRenter() {
		String renter = getStringSetting("rent.renter");
		if(renter != null) {
			try {
				return UUID.fromString(renter);
			} catch(IllegalArgumentException e) {}
		}
		return null;
	}
	
	/**
	 * Check if a player is the renter of this region
	 * @param player Player to check
	 * @return true if this player rents this region, otherwise false
	 */
	public boolean isRenter(Player player) {
		UUID renter = getRenter();
		if(player == null || renter == null) {
			return false;
		} else {
			return renter.equals(player.getUniqueId());
		}
	}
	
	/**
	 * Set the renter of this region
	 * @param renter The UUID of the player that should be set as the renter
	 */
	public void setRenter(UUID renter) {
		if(renter == null) {
			setSetting("rent.renter", null);
			setSetting("rent.renterName", null);
		} else {
			setSetting("rent.renter", renter.toString());
			setSetting("rent.renterName", plugin.toName(renter));
		}
	}
	
	/**
	 * Get the max number of extends of this region
	 * @return -1 if infinite otherwise the maximum number
	 */
	public int getMaxExtends() {
		return getIntegerSetting("rent.maxExtends");
	}
	
	/**
	 * Get how many times the rent has already been extended
	 * @return The number of times extended
	 */
	public int getTimesExtended() {
		return config.getInt("rent.timesExtended");
	}
	
	/**
	 * Set the number of times the region has been extended
	 * @param times The number of times the region has been extended
	 */
	public void setTimesExtended(int times) {
		if(times < 0) {
			config.set("rent.timesExtended", null);
		} else {
			config.set("rent.timesExtended", times);
		}
	}
	
	@Override
	public HashMap<String, Object> getSpecificReplacements() {
		// Fill the replacements map with things specific to a RentRegion
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put(AreaShop.tagPrice, getFormattedPrice());
		result.put(AreaShop.tagDuration, getDurationString());
		result.put(AreaShop.tagPlayerName, getPlayerName());
		result.put(AreaShop.tagPlayerUUID, getRenter());
			SimpleDateFormat date = new SimpleDateFormat(plugin.config().getString("timeFormatChat"));
			String dateString = date.format(new Date(getRentedUntil()));	
		result.put(AreaShop.tagRentedUntil, dateString);
			date = new SimpleDateFormat(plugin.config().getString("timeFormatSign"));
			dateString = date.format(new Date(getRentedUntil()));	
		result.put(AreaShop.tagRentedUntilShort, dateString);
		
		return result;
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
		return getLongSetting("rent.rentedUntil");
	}
	
	/**
	 * Set the time until the region is rented (milliseconds from 1970, system time)
	 * @param rentedUntil The time until the region is rented
	 */
	public void setRentedUntil(Long rentedUntil) {
		if(rentedUntil == null) {
			setSetting("rent.rentedUntil", null);
		} else {
			setSetting("rent.rentedUntil", rentedUntil);
		}
	}
	
	/**
	 * Get the price of the region
	 * @return The price of the region
	 */
	public double getPrice() {
		return getDoubleSetting("rent.price");
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
		return getStringSetting("rent.duration");
	}
	
	/**
	 * Change the price of the region
	 * @param price
	 */
	public void setPrice(double price) {
		setSetting("rent.price", price);
	}
	
	/**
	 * Set the duration of the rent
	 * @param duration The duration of the rent (as specified on the documentation pages)
	 */
	public void setDuration(String duration) {
		setSetting("rent.duration", duration);
	}
	
	/**
	 * Rent a region
	 * @param player The player that wants to rent the region
	 * @param regionName The name of the region you want to rent
	 * @return true if it succeeded and false if not
	 */
	public boolean rent(Player player) {		
		/* Check if the player has permission */
		if(player.hasPermission("areashop.rent")) {	
			boolean extend = false;
			if(getRenter() != null && player.getUniqueId().equals(getRenter())) {
				extend = true;
			}
			// Check if the region is available for renting or if the player wants to extend the rent
			if(!isRented() || extend) {				
				if(!extend) {
					// Check if the player can still rent more regions
					int maximumRents = getMaxRentRegions(player);
					if(getCurrentRentRegions(player) >= maximumRents) {
						plugin.message(player, "rent-maximum", maximumRents);
						return false;
					}
					int maximumTotal = getMaxTotalRegions(player);
					if(getCurrentTotalRegions(player) >= maximumTotal) {
						plugin.message(player, "total-maximum", maximumTotal);
						return false;
					}
				}
				
				// Check if the player can still extend this rent
				if(extend && !player.hasPermission("areashop.rentextendbypass")) {
					if(getMaxExtends() >= 0 && getTimesExtended() >= getMaxExtends()) {
						plugin.message(player, "rent-maxExtends", getMaxExtends());
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
					if(!extend) {
						// Run commands
						runEventCommands(RegionEvent.RENTED, true);
					} else {
						// Run commands
						runEventCommands(RegionEvent.EXTENDED, true);
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
					
					// Fire schematic event and updated times extended
					if(!extend) {
						this.handleSchematicEvent(RegionEvent.RENTED);
						setTimesExtended(0);
					} else {
						setTimesExtended(getTimesExtended() + 1);
					}
					
					/* Change the sign and the region flags */
					updateSigns();
					updateRegionFlags(RegionState.RENTED);
					
					/* Send message to the player */
					if(extend) {
						plugin.message(player, "rent-extended", getName(), dateFull.format(calendar.getTime()));
					} else {
						plugin.message(player, "rent-rented", getName(), dateFull.format(calendar.getTime()));
						plugin.message(player, "rent-extend");
					}
					AreaShop.debug(player.getName() + " has rented region " + getName() + " for " + getFormattedPrice() + " until " + dateFull.format(calendar.getTime()));
					
					this.save();
					if(!extend) {
						// Run commands
						this.runEventCommands(RegionEvent.RENTED, false);
					} else {
						// Run commands
						this.runEventCommands(RegionEvent.EXTENDED, false);
					}
					return true;
				} else {
					/* Player has not enough money */
					if(extend) {
						plugin.message(player, "rent-lowMoneyExtend", plugin.formatCurrency(plugin.getEconomy().getBalance(player, getWorldName())), getFormattedPrice());
					} else {
						plugin.message(player, "rent-lowMoneyRent", plugin.formatCurrency(plugin.getEconomy().getBalance(player, getWorldName())), getFormattedPrice());
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
		// Run commands
		this.runEventCommands(RegionEvent.UNRENTED, true);
		/* Get the time until the region will be rented */
		Long currentTime = Calendar.getInstance().getTimeInMillis();
		Double timeLeft = (double) ((getRentedUntil() - currentTime));
		double percentage = (getDoubleSetting("rent.moneyBack")) / 100.0;

		Double timePeriod = (double) (getDuration());
		double periods = timeLeft / timePeriod;
		double moneyBack =  periods * getPrice() * percentage;
		if(moneyBack > 0 && giveMoneyBack) {
			/* Give back the money */
			OfflinePlayer player = Bukkit.getOfflinePlayer(getRenter());
			if(player != null) {
				EconomyResponse r = null;
				boolean error = false;
				try {
					r = plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(getRenter()), moneyBack);
				} catch(Exception e) {
					error = true;
				}
				if(error || r == null || !r.transactionSuccess()) {
					plugin.getLogger().info("Something went wrong with paying back to " + getPlayerName() + " money while unrenting region " + getName());
				}
			}
		}
		
		/* Debug message */
		AreaShop.debug(getPlayerName() + " has unrented " + getName() + ", got " + plugin.formatCurrency(moneyBack) + " money back");
		
		/* Update the signs and region flags */
		handleSchematicEvent(RegionEvent.UNRENTED);
		updateRegionFlags(RegionState.FORRENT);
		
		/* Remove the player and renteduntil values */
		setRenter(null);
		setRentedUntil(null);
		setTimesExtended(-1);
		
		updateSigns();
		
		this.save();
		// Run commands
		this.runEventCommands(RegionEvent.UNRENTED, false);
	}

}













