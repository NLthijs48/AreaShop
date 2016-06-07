package me.wiefferink.areashop.regions;

import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.Utils;
import me.wiefferink.areashop.events.ask.RentingRegionEvent;
import me.wiefferink.areashop.events.ask.UnrentingRegionEvent;
import me.wiefferink.areashop.events.notify.RentedRegionEvent;
import me.wiefferink.areashop.events.notify.UnrentedRegionEvent;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import static me.wiefferink.areashop.Utils.millisToHumanFormat;

public class RentRegion extends GeneralRegion {
	private long warningsDoneUntil = Calendar.getInstance().getTimeInMillis();
	
	/**
	 * Constructor
	 * @param plugin The areashop plugin
	 * @param config All settings of this region
	 */
	public RentRegion(AreaShop plugin, YamlConfiguration config) {
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
		String renter = config.getString("rent.renter");
		if(renter != null) {
			try {
				return UUID.fromString(renter);
			} catch(IllegalArgumentException e) {
				// Incorrect UUID
			}
		}
		return null;
	}
	
	/**
	 * Check if a player is the renter of this region
	 * @param player Player to check
	 * @return true if this player rents this region, otherwise false
	 */
	public boolean isRenter(Player player) {
		return player != null && isRenter(player.getUniqueId());
	}
	public boolean isRenter(UUID player) {
		UUID renter = getRenter();
		return !(player == null || renter == null) && renter.equals(player);
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
			setSetting("rent.renterName", Utils.toName(renter));
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
			setSetting("rent.timesExtended", null);
		} else {
			setSetting("rent.timesExtended", times);
		}
	}
	
	@Override
	public HashMap<String, Object> getSpecificReplacements() {
		// Fill the replacements map with things specific to a RentRegion
		HashMap<String, Object> result = new HashMap<>();
		result.put(AreaShop.tagPrice, getFormattedPrice());
		result.put(AreaShop.tagRawPrice, getPrice());
		result.put(AreaShop.tagDuration, getDurationString());
		result.put(AreaShop.tagPlayerName, getPlayerName());
		result.put(AreaShop.tagPlayerUUID, getRenter());
			SimpleDateFormat date = new SimpleDateFormat(plugin.getConfig().getString("timeFormatChat"));
			String dateString = date.format(new Date(getRentedUntil()));	
		result.put(AreaShop.tagRentedUntil, dateString);
			date = new SimpleDateFormat(plugin.getConfig().getString("timeFormatSign"));
			dateString = date.format(new Date(getRentedUntil()));	
		result.put(AreaShop.tagRentedUntilShort, dateString);
		result.put(AreaShop.tagTimeLeft, getTimeLeftString());
		result.put(AreaShop.tagMoneyBackAmount, getFormattedMoneyBackAmount());
		result.put(AreaShop.tagRawMoneyBackAmount, getMoneyBackAmount());
		double moneyBackPercent = getMoneyBackPercentage();
		if((moneyBackPercent%1.0) == 0.0) {
			result.put(AreaShop.tagMoneyBackPercentage, (int)moneyBackPercent);
		} else {
			result.put(AreaShop.tagMoneyBackPercentage, moneyBackPercent);
		}
		result.put(AreaShop.tagMaxExtends, this.getMaxExtends());
		result.put(AreaShop.tagExtendsLeft, getMaxExtends() - getTimesExtended());
		result.put(AreaShop.tagMaxRentTime, millisToHumanFormat(getMaxRentTime()));
		result.put(AreaShop.tagMaxInactiveTime, this.getFormattedInactiveTimeUntilUnrent());
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
	 * @return Name of the player renting this region, if unavailable by UUID it will return the old cached name, if that is unavailable it will return <UNKNOWN>
	 */
	public String getPlayerName() {
		String result = Utils.toName(getRenter());
		if(result == null || result.isEmpty()) {
			result = config.getString("rent.renterName");
			if(result == null || result.isEmpty()) {
				result = "<UNKNOWN>";
			}
		}
		return result;
	}
	
	/**
	 * Get the time until this region is rented (time from 1970 epoch)
	 * @return The epoch time until which this region is rented
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
		return Utils.formatCurrency(getPrice());
	}

	/**
	 * Get the duration of 1 rent period
	 * @return The duration in milliseconds of 1 rent period
	 */
	public long getDuration() {
		return Utils.durationStringToLong(getDurationString());
	}
	
	/**
	 * Get the duration string, includes number<space>indentifier
	 * @return The duration string
	 */
	public String getDurationString() {
		return getStringSetting("rent.duration");
	}
	
	/**
	 * Get the time that is left on the region
	 * @return The time left on the region
	 */
	public long getTimeLeft() {
		if(isRented()) {
			return this.getRentedUntil() - Calendar.getInstance().getTimeInMillis();
		} else {
			return 0;
		}
	}
	
	/**
	 * Get a formatted string indicating the rent time that is left
	 * @return Time left on the rent, for example '29 days', '3 months', '1 second'
	 */
	public String getTimeLeftString() {
		return Utils.millisToHumanFormat(getTimeLeft());
	}
	
	/**
	 * Minutes until automatic unrent when player is offline
	 * @return The number of milliseconds until the region is unrented while player is offline
	 */
	public long getInactiveTimeUntilUnrent() {
		return Utils.getDurationFromMinutesOrStringInput(getStringSetting("rent.inactiveTimeUntilUnrent"));
	}
	
	/**
	 * Get a human readable string indicating how long the player can be offline until automatic unrent
	 * @return String indicating the inactive time until unrent
	 */
	public String getFormattedInactiveTimeUntilUnrent() {
		return Utils.millisToHumanFormat(getInactiveTimeUntilUnrent());
	}
	
	/**
	 * Change the price of the region
	 * @param price The price of the region
	 */
	public void setPrice(double price) {
		setSetting("rent.price", price);
	}
	
	/**
	 * Remove the price so that the price will be taken from a group or the default.yml file
	 */
	public void removePrice() {
		setSetting("rent.price", null);
	}
	
	/**
	 * Set the duration of the rent
	 * @param duration The duration of the rent (as specified on the documentation pages)
	 */
	public void setDuration(String duration) {
		setSetting("rent.duration", duration);
	}
	
	/**
	 * Get the moneyBack percentage
	 * @return The % of money the player will get back when unrenting
	 */
	public double getMoneyBackPercentage() {
		return getDoubleSetting("rent.moneyBack");
	}
	
	/**
	 * Get the amount of money that should be paid to the player when unrenting the region
	 * @return The amount of money the player should get back
	 */
	public double getMoneyBackAmount() {
		Long currentTime = Calendar.getInstance().getTimeInMillis();
		Double timeLeft = (double) ((getRentedUntil() - currentTime));
		double percentage = (getMoneyBackPercentage()) / 100.0;
		Double timePeriod = (double) (getDuration());
		double periods = timeLeft / timePeriod;
		return periods * getPrice() * percentage;
	}
	
	/**
	 * Get the formatted string of the amount of the moneyBack amount
	 * @return String with currency symbols and proper fractional part
	 */
	public String getFormattedMoneyBackAmount() {
		return Utils.formatCurrency(getMoneyBackAmount());
	}
	
	/**
	 * Get the maximum time the player can rent the region in advance (milliseconds)
	 * @return The maximum rent time in milliseconds
	 */
	public long getMaxRentTime() {
		return Utils.getDurationFromMinutesOrStringInput(getStringSetting("rent.maxRentTime"));
	}
	
	/**
	 * Check if the rent should expire
	 * @return true if the rent has expired and has been unrented, false otherwise
	 */
	public boolean checkExpiration() {
		long now = Calendar.getInstance().getTimeInMillis();
		if(!isDeleted() && isRented() && now > getRentedUntil()) {
			// Send message to the player if online
			Player player = Bukkit.getPlayer(getRenter());
			if(unRent(false, player)) {
				if(player != null) {
					message(player, "unrent-expired");
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Send the expiration warnings from the selected profile which is specified in the config
	 * Sends all warnings since previous call until now+<normal delay>, delay can be found in the config as well
	 */
	public void sendExpirationWarnings() {
		// send from warningsDoneUntil to current+delay
		if(isDeleted() || !isRented()) {
			return;
		}
		Player player = Bukkit.getPlayer(getRenter());
		if(player != null) {
			long sendUntil = Calendar.getInstance().getTimeInMillis() + (plugin.getConfig().getInt("expireWarning.delay") * 60 * 1000);
			// loop through warning defined in the config for the profile that is set for this region
			String configPath = "expirationWarningProfiles." + getStringSetting("rent.expirationWarningProfile");
			ConfigurationSection section = plugin.getConfig().getConfigurationSection(configPath);
			if(section == null) {
				return;
			}
			for(String timeBefore : section.getKeys(false)) {
				long timeBeforeParsed = Utils.durationStringToLong(timeBefore);
				if(timeBeforeParsed <= 0) {
					return;
				}
				long checkTime = getRentedUntil() - timeBeforeParsed;
				
				if(checkTime > warningsDoneUntil && checkTime <= sendUntil) {
					if(plugin.getConfig().getBoolean(configPath + "." + timeBefore + ".warnPlayer")) {
						message(player, "rent-expireWarning");
					}
					this.runCommands(Bukkit.getConsoleSender(), plugin.getConfig().getStringList(configPath + "." + timeBefore + ".commands"));					
				}		
			}
			warningsDoneUntil = sendUntil;
		}		
	}
	
	/**
	 * Rent a region
	 * @param player The player that wants to rent the region
	 * @return true if it succeeded and false if not
	 */
	@SuppressWarnings("deprecation")
	public boolean rent(Player player) {
		if(plugin.getEconomy() == null) {
			message(player, "general-noEconomy");
			return false;
		}
		//Check if the player has permission
		if(player.hasPermission("areashop.rent")) {
			if(getWorld() == null) {
				message(player, "general-noWorld");
				return false;
			}
			if(getRegion() == null) {
				message(player, "general-noRegion");
				return false;
			}			
			boolean extend = false;
			if(getRenter() != null && player.getUniqueId().equals(getRenter())) {
				extend = true;
			}
			// Check if the region is available for renting or if the player wants to extend the rent
			if(!isRented() || extend) {
				// Check if the players needs to be in the world or region for buying
				if(restrictedToRegion() && (!player.getWorld().getName().equals(getWorldName()) 
						|| !getRegion().contains(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()))) {
					message(player, "rent-restrictedToRegion");
					return false;
				}
				if(restrictedToWorld() && !player.getWorld().getName().equals(getWorldName())) {
					message(player, "rent-restrictedToWorld", player.getWorld().getName());
					return false;
				}				
				// Check region limits if this is not extending
				if(!(extend && config.getBoolean("allowRegionExtendsWhenAboveLimits"))) {

					LimitResult limitResult;
					if(extend) {
						limitResult = this.limitsAllow(RegionType.RENT, player, true);
					} else {
						limitResult = this.limitsAllow(RegionType.RENT, player);
					}
					AreaShop.debug("LimitResult: " + limitResult.toString());
					if(!limitResult.actionAllowed()) {
						if(limitResult.getLimitingFactor() == LimitType.TOTAL) {
							message(player, "total-maximum", limitResult.getMaximum(), limitResult.getCurrent(), limitResult.getLimitingGroup());
							return false;
						}
						if(limitResult.getLimitingFactor() == LimitType.RENTS) {
							message(player, "rent-maximum", limitResult.getMaximum(), limitResult.getCurrent(), limitResult.getLimitingGroup());
							return false;
						}
						if(limitResult.getLimitingFactor() == LimitType.EXTEND) {
							message(player, "rent-maximumExtend", limitResult.getMaximum(), limitResult.getCurrent() + 1, limitResult.getLimitingGroup());
							return false;
						}
						return false;
					}
				}
				
				// Check if the player can still extend this rent
				if(extend && !player.hasPermission("areashop.rentextendbypass")) {
					if(getMaxExtends() >= 0 && getTimesExtended() >= getMaxExtends()) {
						message(player, "rent-maxExtends");
						return false;
					}
				}
				
				// Check if there is enough time left before hitting maxRentTime
				boolean extendToMax = false;
				double price = getPrice();
				long timeNow = Calendar.getInstance().getTimeInMillis();
				long timeRented = 0;
				long maxRentTime = getMaxRentTime();
				if(isRented()) {
					timeRented = getRentedUntil() - timeNow;
				}
				if((timeRented + getDuration()) > (maxRentTime) 
						&& !player.hasPermission("areashop.renttimebypass")
						&& maxRentTime != -1) {
					// Extend to the maximum instead of adding a full period
					if(getBooleanSetting("rent.extendToFullWhenAboveMaxRentTime")) {
						if(timeRented >= maxRentTime) {
							message(player, "rent-alreadyAtFull");
							return false;
						} else {
							long toRentPart = maxRentTime - timeRented;
							extendToMax = true;
							price = ((double)toRentPart)/getDuration()*price;
						}
					} else {
						message(player, "rent-maxRentTime");
						return false;
					}
				}

				if(plugin.getEconomy().has(player, getWorldName(), price)) {
					// Broadcast and check event
					RentingRegionEvent event = new RentingRegionEvent(this, player, extend);
					Bukkit.getPluginManager().callEvent(event);
					if(event.isCancelled()) {
						message(player, "general-cancelled", event.getReason());
						return false;
					}

					// Substract the money from the players balance
					EconomyResponse r = plugin.getEconomy().withdrawPlayer(player, getWorldName(), price);
					if(!r.transactionSuccess()) {
						message(player, "rent-payError");
						AreaShop.debug("Something went wrong with getting money from " + player.getName() + " while renting " + getName() + ": " + r.errorMessage);
						return false;
					}
					// Optionally give money to the landlord
					OfflinePlayer landlordPlayer = null;
					if(getLandlord() != null) {
						landlordPlayer = Bukkit.getOfflinePlayer(getLandlord());
					}
					String landlordName = getLandlordName();
					if(landlordName != null) {
						if(landlordPlayer != null && landlordPlayer.getName() != null) {
							r = plugin.getEconomy().depositPlayer(landlordPlayer, getWorldName(), price);
						} else {
							r = plugin.getEconomy().depositPlayer(landlordName, getWorldName(), price);
						}
						if(r == null || !r.transactionSuccess()) {
							plugin.getLogger().warning("Something went wrong with paying '"+landlordName+"' "+Utils.formatCurrency(price)+" for his rent of region "+getName()+" to "+player.getName());
						}
					}
						
					if(!extend) {
						// Run commands
						runEventCommands(RegionEvent.RENTED, true);
					} else {
						// Run commands
						runEventCommands(RegionEvent.EXTENDED, true);
					}
					
					// Get the time until the region will be rented
					Calendar calendar = Calendar.getInstance();
					if(extendToMax) {
						calendar.setTimeInMillis(calendar.getTimeInMillis() + getMaxRentTime());
					} else if(extend) {
						calendar.setTimeInMillis(getRentedUntil()+getDuration());
					} else {
						calendar.setTimeInMillis(calendar.getTimeInMillis() + getDuration());
					}

					// Add values to the rent and send it to FileManager
					setRentedUntil(calendar.getTimeInMillis());
					setRenter(player.getUniqueId());
					updateLastActiveTime();

					// Fire schematic event and updated times extended
					if(!extend) {
						this.handleSchematicEvent(RegionEvent.RENTED);
						setTimesExtended(0);
					} else {
						setTimesExtended(getTimesExtended() + 1);
					}

					// Notify about updates
					this.notifyAndUpdate(new RentedRegionEvent(this, extend));
					
					// Send message to the player
					if(extendToMax) {
						message(player, "rent-extendedToMax");
					} else if(extend) {
						message(player, "rent-extended");
					} else {
						message(player, "rent-rented");
					}
					if(!extend) {
						// Run commands
						this.runEventCommands(RegionEvent.RENTED, false);
					} else {
						// Run commands
						this.runEventCommands(RegionEvent.EXTENDED, false);
					}
					return true;
				} else {
					// Player has not enough money
					if(extend) {
						message(player, "rent-lowMoneyExtend", Utils.formatCurrency(plugin.getEconomy().getBalance(player, getWorldName())));
					} else {
						message(player, "rent-lowMoneyRent", Utils.formatCurrency(plugin.getEconomy().getBalance(player, getWorldName())));
					}
				}
			} else {
				message(player, "rent-someoneElse");
			}	
		} else {
			message(player, "rent-noPermission");
		}
		return false;
	}
	
	/**
	 * Unrent a region, reset to unrented
	 * @param giveMoneyBack true if money should be given back to the player, false otherwise
	 * @param executor The CommandSender that should get the cancelled message if there is any, or null
	 */
	@SuppressWarnings("deprecation")
	public boolean unRent(boolean giveMoneyBack, CommandSender executor) {
		boolean own = executor != null && executor instanceof Player && this.isRenter((Player)executor);
		if(executor != null) {
			if(!executor.hasPermission("areashop.unrent") && !own) {
				message(executor, "unrent-noPermissionOther");
				return false;
			}
			if(!executor.hasPermission("areashop.unrent") && !executor.hasPermission("areashop.unrentown") && own) {
				message(executor, "unrent-noPermission");
				return false;
			}
		}

		if(plugin.getEconomy() == null) {
			return false;
		}

		// Broadcast and check event
		UnrentingRegionEvent event = new UnrentingRegionEvent(this);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) {
			message(executor, "general-cancelled", event.getReason());
			return false;
		}

		// Run commands
		this.runEventCommands(RegionEvent.UNRENTED, true);
		double moneyBack =  getMoneyBackAmount();
		if(moneyBack > 0 && giveMoneyBack) {
			boolean noPayBack = false;
			OfflinePlayer landlordPlayer = null;
			if(getLandlord() != null) {
				landlordPlayer = Bukkit.getOfflinePlayer(getLandlord());
			}
			String landlordName = getLandlordName();
			EconomyResponse r;
			if(landlordName != null) {
				if(landlordPlayer != null && landlordPlayer.getName() != null) {
					r = plugin.getEconomy().withdrawPlayer(landlordPlayer, getWorldName(), moneyBack);
				} else {
					r = plugin.getEconomy().withdrawPlayer(landlordName, getWorldName(), moneyBack);
				}
				if(r == null || !r.transactionSuccess()) {
					noPayBack = true;
				}
			}			
			
			// Give back the money
			OfflinePlayer player = Bukkit.getOfflinePlayer(getRenter());
			if(player != null && !noPayBack) {
				r = null;
				boolean error = false;
				try {
					if(player.getName() != null) {
						r = plugin.getEconomy().depositPlayer(player, getWorldName(), moneyBack);
					} else if(getPlayerName() != null) {
						r = plugin.getEconomy().depositPlayer(getPlayerName(), getWorldName(), moneyBack);
					}
				} catch(Exception e) {
					error = true;
				}
				if(error || r == null || !r.transactionSuccess()) {
					plugin.getLogger().warning("Something went wrong with paying back to " + getPlayerName() + " money while unrenting region " + getName());
				}
			}
		}

		// Update the signs and region flags
		handleSchematicEvent(RegionEvent.UNRENTED);

		// Send messages
		message(executor, "unrent-unrented");

		// Remove friends, the owner and renteduntil values
		clearFriends();
		UUID oldRenter = getRenter();
		setRenter(null);
		setRentedUntil(null);
		setTimesExtended(-1);
		removeLastActiveTime();

		// Notify about updates
		this.notifyAndUpdate(new UnrentedRegionEvent(this, oldRenter, Math.max(0, moneyBack)));

		// Run commands
		this.runEventCommands(RegionEvent.UNRENTED, false);
		return true;
	}
	
	@Override
	public boolean checkInactive() {
		if(isDeleted() || !isRented()) {
			return false;
		}
		long inactiveSetting = getInactiveTimeUntilUnrent();
		OfflinePlayer player = Bukkit.getOfflinePlayer(getRenter());
		if(inactiveSetting <= 0 || player.isOp()) {
			return false;
		}
		long lastPlayed = getLastActiveTime();
		//AreaShop.debug("currentTime=" + Calendar.getInstance().getTimeInMillis() + ", getLastPlayed()=" + lastPlayed + ", timeInactive=" + (Calendar.getInstance().getTimeInMillis()-player.getLastPlayed()) + ", inactiveSetting=" + inactiveSetting);
		if(Calendar.getInstance().getTimeInMillis() > (lastPlayed + inactiveSetting)) {
			plugin.getLogger().info("Region " + getName() + " unrented because of inactivity for player " + getPlayerName());
			AreaShop.debug("currentTime=" + Calendar.getInstance().getTimeInMillis() + ", getLastPlayed()=" + lastPlayed + ", timeInactive=" + (Calendar.getInstance().getTimeInMillis()-player.getLastPlayed()) + ", inactiveSetting=" + inactiveSetting);
			return this.unRent(true, null);
		}
		return false;
	}

}













