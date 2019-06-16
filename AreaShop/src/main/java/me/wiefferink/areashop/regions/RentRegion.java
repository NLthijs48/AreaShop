package me.wiefferink.areashop.regions;

import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.events.ask.RentingRegionEvent;
import me.wiefferink.areashop.events.ask.UnrentingRegionEvent;
import me.wiefferink.areashop.events.notify.RentedRegionEvent;
import me.wiefferink.areashop.events.notify.UnrentedRegionEvent;
import me.wiefferink.areashop.tools.Utils;
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
import java.util.List;
import java.util.UUID;

import static me.wiefferink.areashop.tools.Utils.millisToHumanFormat;

public class RentRegion extends GeneralRegion {
	private long warningsDoneUntil = Calendar.getInstance().getTimeInMillis();

	/**
	 * Constructor.
	 * @param config All settings of this region
	 */
	public RentRegion(YamlConfiguration config) {
		super(config);
	}

	/**
	 * Create a new RentRegion.
	 * @param name  The name of the region (correct casing)
	 * @param world The world of the WorldGuard region
	 */
	public RentRegion(String name, World world) {
		super(name, world);
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

	@Override
	public boolean isAvailable() {
		return !isRented();
	}

	/**
	 * Get the UUID of the player renting the region.
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
	 * Check if a player is the renter of this region.
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
	 * Set the renter of this region.
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
	 * Get the max number of extends of this region.
	 * @return -1 if infinite otherwise the maximum number
	 */
	public int getMaxExtends() {
		return getIntegerSetting("rent.maxExtends");
	}

	/**
	 * Get how many times the rent has already been extended.
	 * @return The number of times extended
	 */
	public int getTimesExtended() {
		return config.getInt("rent.timesExtended");
	}

	/**
	 * Set the number of times the region has been extended.
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
	public Object provideReplacement(String variable) {
		switch(variable) {
			case AreaShop.tagPrice:
				return getFormattedPrice();
			case AreaShop.tagRawPrice:
				return getPrice();
			case AreaShop.tagDuration:
				return getDurationString();
			case AreaShop.tagPlayerName:
				return getPlayerName();
			case AreaShop.tagPlayerUUID:
				return getRenter();
			case AreaShop.tagRentedUntil:
				return new SimpleDateFormat(plugin.getConfig().getString("timeFormatChat")).format(new Date(getRentedUntil()));
			case AreaShop.tagRentedUntilShort:
				return new SimpleDateFormat(plugin.getConfig().getString("timeFormatSign")).format(new Date(getRentedUntil()));
			case AreaShop.tagTimeLeft:
				return getTimeLeftString();
			case AreaShop.tagMoneyBackAmount:
				return getFormattedMoneyBackAmount();
			case AreaShop.tagRawMoneyBackAmount:
				return getMoneyBackAmount();
			case AreaShop.tagMoneyBackPercentage:
				return (getMoneyBackPercentage() % 1.0) == 0.0 ? (int)getMoneyBackPercentage() : getMoneyBackPercentage();
			case AreaShop.tagTimesExtended:
				return this.getTimesExtended();
			case AreaShop.tagMaxExtends:
				return this.getMaxExtends();
			case AreaShop.tagExtendsLeft:
				return getMaxExtends() - getTimesExtended();
			case AreaShop.tagMaxRentTime:
				return millisToHumanFormat(getMaxRentTime());
			case AreaShop.tagMaxInactiveTime:
				return this.getFormattedInactiveTimeUntilUnrent();

			default:
				return super.provideReplacement(variable);
		}
	}

	/**
	 * Check if the region is rented.
	 * @return true if the region is rented, otherwise false
	 */
	public boolean isRented() {
		return getRenter() != null;
	}

	/**
	 * Get the name of the player renting this region.
	 * @return Name of the player renting this region, if unavailable by UUID it will return the old cached name, if that is unavailable it will return &lt;UNKNOWN&gt;
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
	 * Get the time until this region is rented (time from 1970 epoch).
	 * @return The epoch time until which this region is rented
	 */
	public long getRentedUntil() {
		return getLongSetting("rent.rentedUntil");
	}

	/**
	 * Set the time until the region is rented (milliseconds from 1970, system time).
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
	 * Get the price of the region.
	 * @return The price of the region
	 */
	public double getPrice() {
		return Math.max(0, Utils.evaluateToDouble(getStringSetting("rent.price"), this));
	}

	/**
	 * Get the formatted string of the price (includes prefix and suffix).
	 * @return The formatted string of the price
	 */
	public String getFormattedPrice() {
		return Utils.formatCurrency(getPrice());
	}

	/**
	 * Get the duration of 1 rent period.
	 * @return The duration in milliseconds of 1 rent period
	 */
	public long getDuration() {
		return Utils.durationStringToLong(getDurationString());
	}

	/**
	 * Get the duration string, includes 'number indentifier'.
	 * @return The duration string
	 */
	public String getDurationString() {
		return getStringSetting("rent.duration");
	}

	/**
	 * Get the time that is left on the region.
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
	 * Get a formatted string indicating the rent time that is left.
	 * @return Time left on the rent, for example '29 days', '3 months', '1 second'
	 */
	public String getTimeLeftString() {
		return Utils.millisToHumanFormat(getTimeLeft());
	}

	/**
	 * Minutes until automatic unrent when player is offline.
	 * @return The number of milliseconds until the region is unrented while player is offline
	 */
	public long getInactiveTimeUntilUnrent() {
		return Utils.getDurationFromMinutesOrStringInput(getStringSetting("rent.inactiveTimeUntilUnrent"));
	}

	/**
	 * Get a human readable string indicating how long the player can be offline until automatic unrent.
	 * @return String indicating the inactive time until unrent
	 */
	public String getFormattedInactiveTimeUntilUnrent() {
		return Utils.millisToHumanFormat(getInactiveTimeUntilUnrent());
	}

	/**
	 * Change the price of the region.
	 * @param price The price of the region
	 */
	public void setPrice(Double price) {
		setSetting("rent.price", price);
	}

	/**
	 * Set the duration of the rent.
	 * @param duration The duration of the rent (as specified on the documentation pages)
	 */
	public void setDuration(String duration) {
		setSetting("rent.duration", duration);
	}

	/**
	 * Get the moneyBack percentage.
	 * @return The % of money the player will get back when unrenting
	 */
	public double getMoneyBackPercentage() {
		return Utils.evaluateToDouble(getStringSetting("rent.moneyBack"), this);
	}

	/**
	 * Get the amount of money that should be paid to the player when unrenting the region.
	 * @return The amount of money the player should get back
	 */
	public double getMoneyBackAmount() {
		Long currentTime = Calendar.getInstance().getTimeInMillis();
		Double timeLeft = (double)(getRentedUntil() - currentTime);
		double percentage = (getMoneyBackPercentage()) / 100.0;
		Double timePeriod = (double)(getDuration());
		double periods = timeLeft / timePeriod;
		return Math.max(0, periods * getPrice() * percentage);
	}

	/**
	 * Get the formatted string of the amount of the moneyBack amount.
	 * @return String with currency symbols and proper fractional part
	 */
	public String getFormattedMoneyBackAmount() {
		return Utils.formatCurrency(getMoneyBackAmount());
	}

	/**
	 * Get the maximum time the player can rent the region in advance (milliseconds).
	 * @return The maximum rent time in milliseconds
	 */
	public long getMaxRentTime() {
		return Utils.getDurationFromMinutesOrStringInput(getStringSetting("rent.maxRentTime"));
	}

	/**
	 * Check if the rent should expire.
	 * @return true if the rent has expired and has been unrented, false otherwise
	 */
	public boolean checkExpiration() {
		long now = Calendar.getInstance().getTimeInMillis();
		if(!isDeleted() && isRented() && now > getRentedUntil()) {
			// Extend rent if configured for that
			if(getBooleanSetting("rent.autoExtend") && extend()) {
				return false;
			}

			// Send message to the player if online
			Player player = Bukkit.getPlayer(getRenter());
			if(unRent(false, null)) {
				if(player != null) {
					message(player, "unrent-expired");
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Send the expiration warnings from the selected profile which is specified in the config.
	 * Sends all warnings since previous call until (now + normal delay), delay can be found in the config as well.
	 */
	public void sendExpirationWarnings() {
		// Send from warningsDoneUntil to current+delay
		if(isDeleted() || !isRented()) {
			return;
		}
		ConfigurationSection profileSection = getConfigurationSectionSetting("rent.expirationWarningProfile", "expirationWarningProfiles");
		if(profileSection == null) {
			return;
		}

		// Check if a warning needs to be send for each defined point in time
		Player player = Bukkit.getPlayer(getRenter());
		long sendUntil = Calendar.getInstance().getTimeInMillis() + (plugin.getConfig().getInt("expireWarning.delay") * 60 * 1000);
		for(String timeBefore : profileSection.getKeys(false)) {
			long timeBeforeParsed = Utils.durationStringToLong(timeBefore);
			if(timeBeforeParsed <= 0) {
				return;
			}
			long checkTime = getRentedUntil() - timeBeforeParsed;

			if(checkTime > warningsDoneUntil && checkTime <= sendUntil) {
				List<String> commands;
				if(profileSection.isConfigurationSection(timeBefore)) {
					/* Legacy config layout:
					 *   "1 minute":
					 *     warnPlayer: true
					 *     commands: ["say hi"]
					 */
					commands = profileSection.getStringList(timeBefore + ".commands");
					// Warn player
					if(profileSection.getBoolean(timeBefore + ".warnPlayer") && player != null) {
						message(player, "rent-expireWarning");
					}
				} else {
					commands = profileSection.getStringList(timeBefore);
				}
				this.runCommands(Bukkit.getConsoleSender(), commands);
			}
		}
		warningsDoneUntil = sendUntil;
	}

	/**
	 * Try to extend the rent for the current owner, respecting all restrictions.
	 * @return true if successful, otherwise false
	 */
	public boolean extend() {
		if(!isRented()) {
			return false;
		}

		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(getRenter());
		return offlinePlayer != null && rent(offlinePlayer);
	}

	/**
	 * Rent a region.
	 * @param offlinePlayer The player that wants to rent the region
	 * @return true if it succeeded and false if not
	 */
	public boolean rent(OfflinePlayer offlinePlayer) {
		if(plugin.getEconomy() == null) {
			message(offlinePlayer, "general-noEconomy");
			return false;
		}

		// Check if the player has permission
		if(!plugin.hasPermission(offlinePlayer, "areashop.rent")) {
			message(offlinePlayer, "rent-noPermission");
			return false;
		}

		// Check location restrictions
		if(getWorld() == null) {
			message(offlinePlayer, "general-noWorld");
			return false;
		}
		if(getRegion() == null) {
			message(offlinePlayer, "general-noRegion");
			return false;
		}
		boolean extend = false;
		if(getRenter() != null && offlinePlayer.getUniqueId().equals(getRenter())) {
			extend = true;
		}

		// Check if available or extending
		if (isRented() && !extend) {
			message(offlinePlayer, "rent-someoneElse");
			return false;
		}

		// These checks are only relevant for online players doing the renting/buying themselves
		Player player = offlinePlayer.getPlayer();
		if(player != null) {
			// Check if the players needs to be in the region for renting
			if(restrictedToRegion() && (!player.getWorld().getName().equals(getWorldName())
					|| !getRegion().contains(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()))) {
				message(offlinePlayer, "rent-restrictedToRegion");
				return false;
			}

			// Check if the players needs to be in the world for renting
			if(restrictedToWorld() && !player.getWorld().getName().equals(getWorldName())) {
				message(offlinePlayer, "rent-restrictedToWorld", player.getWorld().getName());
				return false;
			}
		}

		// Check region limits if this is not extending
		if(!(extend && config.getBoolean("allowRegionExtendsWhenAboveLimits"))) {

			LimitResult limitResult;
			if(extend) {
				limitResult = this.limitsAllow(RegionType.RENT, offlinePlayer, true);
			} else {
				limitResult = this.limitsAllow(RegionType.RENT, offlinePlayer);
			}
			AreaShop.debug("LimitResult: " + limitResult.toString());
			if(!limitResult.actionAllowed()) {
				if(limitResult.getLimitingFactor() == LimitType.TOTAL) {
					message(offlinePlayer, "total-maximum", limitResult.getMaximum(), limitResult.getCurrent(), limitResult.getLimitingGroup());
					return false;
				}
				if(limitResult.getLimitingFactor() == LimitType.RENTS) {
					message(offlinePlayer, "rent-maximum", limitResult.getMaximum(), limitResult.getCurrent(), limitResult.getLimitingGroup());
					return false;
				}
				if(limitResult.getLimitingFactor() == LimitType.EXTEND) {
					message(offlinePlayer, "rent-maximumExtend", limitResult.getMaximum(), limitResult.getCurrent() + 1, limitResult.getLimitingGroup());
					return false;
				}
				return false;
			}
		}

		// Check if the player can still extend this rent
		if(extend && !plugin.hasPermission(offlinePlayer, "areashop.rentextendbypass")) {
			if(getMaxExtends() >= 0 && getTimesExtended() >= getMaxExtends()) {
				message(offlinePlayer, "rent-maxExtends");
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
				&& !plugin.hasPermission(offlinePlayer, "areashop.renttimebypass")
				&& maxRentTime != -1) {
			// Extend to the maximum instead of adding a full period
			if(getBooleanSetting("rent.extendToFullWhenAboveMaxRentTime")) {
				if(timeRented >= maxRentTime) {
					message(offlinePlayer, "rent-alreadyAtFull");
					return false;
				} else {
					long toRentPart = maxRentTime - timeRented;
					extendToMax = true;
					price = ((double)toRentPart) / getDuration() * price;
				}
			} else {
				message(offlinePlayer, "rent-maxRentTime");
				return false;
			}
		}

		// Check if the player has enough money
		if(!plugin.getEconomy().has(offlinePlayer, getWorldName(), price)) {
			if(extend) {
				message(offlinePlayer, "rent-lowMoneyExtend", Utils.formatCurrency(plugin.getEconomy().getBalance(offlinePlayer, getWorldName())));
			} else {
				message(offlinePlayer, "rent-lowMoneyRent", Utils.formatCurrency(plugin.getEconomy().getBalance(offlinePlayer, getWorldName())));
			}
			return false;
		}

		// Broadcast and check event
		RentingRegionEvent event = new RentingRegionEvent(this, offlinePlayer, extend);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) {
			message(offlinePlayer, "general-cancelled", event.getReason());
			return false;
		}

		// Substract the money from the players balance
		EconomyResponse r = plugin.getEconomy().withdrawPlayer(offlinePlayer, getWorldName(), price);
		if(!r.transactionSuccess()) {
			message(offlinePlayer, "rent-payError");
			AreaShop.debug("Something went wrong with getting money from " + offlinePlayer.getName() + " while renting " + getName() + ": " + r.errorMessage);
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
				AreaShop.warn("Something went wrong with paying '" + landlordName + "' " + Utils.formatCurrency(price) + " for his rent of region " + getName() + " to " + offlinePlayer.getName());
			}
		}

		// Get the time until the region will be rented
		Calendar calendar = Calendar.getInstance();
		if(extendToMax) {
			calendar.setTimeInMillis(calendar.getTimeInMillis() + getMaxRentTime());
		} else if(extend) {
			calendar.setTimeInMillis(getRentedUntil() + getDuration());
		} else {
			calendar.setTimeInMillis(calendar.getTimeInMillis() + getDuration());
		}

		// Add values to the rent and send it to FileManager
		setRentedUntil(calendar.getTimeInMillis());
		setRenter(offlinePlayer.getUniqueId());
		updateLastActiveTime();

		// Fire schematic event and updated times extended
		if(!extend) {
			this.handleSchematicEvent(RegionEvent.RENTED);
			setTimesExtended(0);
		} else {
			setTimesExtended(getTimesExtended() + 1);
		}

		// Send message to the player
		if(extendToMax) {
			message(offlinePlayer, "rent-extendedToMax");
		} else if(extend) {
			message(offlinePlayer, "rent-extended");
		} else {
			message(offlinePlayer, "rent-rented");
		}

		// Notify about updates
		this.notifyAndUpdate(new RentedRegionEvent(this, extend));
		return true;
	}

	/**
	 * Unrent a region, reset to unrented.
	 * @param giveMoneyBack true if money should be given back to the player, false otherwise
	 * @param executor      The CommandSender that should get the cancelled message if there is any, or null
	 * @return true if unrenting succeeded, othwerwise false
	 */
	@SuppressWarnings("deprecation")
	public boolean unRent(boolean giveMoneyBack, CommandSender executor) {
		boolean own = executor instanceof Player && this.isRenter((Player) executor);
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

		// Do a payback
		double moneyBack = getMoneyBackAmount();
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
					AreaShop.warn("Something went wrong with paying back to " + getPlayerName() + " money while unrenting region " + getName());
				}
			}
		}

		// Handle schematic save/restore (while %uuid% is still available)
		handleSchematicEvent(RegionEvent.UNRENTED);

		// Send message: before actual removal of the renter so that it is still available for variables
		message(executor, "unrent-unrented");

		// Remove friends, the owner and renteduntil values
		getFriendsFeature().clearFriends();
		UUID oldRenter = getRenter();
		setRenter(null);
		setRentedUntil(null);
		setTimesExtended(-1);
		removeLastActiveTime();

		// Notify about updates
		this.notifyAndUpdate(new UnrentedRegionEvent(this, oldRenter, Math.max(0, moneyBack)));
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
			AreaShop.info("Region " + getName() + " unrented because of inactivity for player " + getPlayerName());
			AreaShop.debug("currentTime=" + Calendar.getInstance().getTimeInMillis() + ", getLastPlayed()=" + lastPlayed + ", timeInactive=" + (Calendar.getInstance().getTimeInMillis() - player.getLastPlayed()) + ", inactiveSetting=" + inactiveSetting);
			return this.unRent(true, null);
		}
		return false;
	}

}













