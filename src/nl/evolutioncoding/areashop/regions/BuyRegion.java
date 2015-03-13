package nl.evolutioncoding.areashop.regions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

import net.milkbowl.vault.economy.EconomyResponse;
import nl.evolutioncoding.areashop.AreaShop;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class BuyRegion extends GeneralRegion {

	public BuyRegion(AreaShop plugin, YamlConfiguration config) {
		super(plugin, config);
	}
	
	public BuyRegion(AreaShop plugin, String name, World world) {
		super(plugin, name, world);
	}
	
	@Override
	public RegionType getType() {
		return RegionType.BUY;
	}
	
	@Override
	public RegionState getState() {
		if(isSold() && isInResellingMode()) {
			return RegionState.RESELL;
		} else if(isSold() && !isInResellingMode()) {
			return RegionState.SOLD;
		} else {
			return RegionState.FORSALE;
		}
	}
	
	/**
	 * Get the UUID of the owner of this region
	 * @return The UUID of the owner of this region
	 */
	public UUID getBuyer() {
		String buyer = getStringSetting("buy.buyer");
		if(buyer != null) {
			try {
				return UUID.fromString(buyer);
			} catch(IllegalArgumentException e) {}
		}
		return null;
	}
	
	/**
	 * Check if a player is the buyer of this region
	 * @param player Player to check
	 * @return true if this player owns this region, otherwise false
	 */
	public boolean isBuyer(Player player) {
		if(player == null) {
			return false;
		} else {
			return isBuyer(player.getUniqueId());
		}
	}
	public boolean isBuyer(UUID player) {
		UUID buyer = getBuyer();
		if(buyer == null || player == null) {
			return false;
		} else {
			return buyer.equals(player);
		}
	}
	
	/**
	 * Set the buyer of this region
	 * @param buyer The UUID of the player that should be set as buyer
	 */
	public void setLandlord(UUID buyer) {
		if(buyer == null) {
			setSetting("buy.buyer", null);
			setSetting("buy.buyerName", null);
		} else {
			setSetting("buy.buyer", buyer.toString());
			setSetting("buy.buyerName", plugin.toName(buyer));
		}
	}
	
	/**
	 * Get the name of the player that owns this region
	 * @return The name of the player that owns this region, if unavailable by UUID it will return the old cached name, if that is unavailable it will return <UNKNOWN>
	 */
	public String getPlayerName() {
		String result = plugin.toName(getBuyer());
		if(result == null || result.isEmpty()) {
			result = config.getString("buy.buyerName");
			if(result == null || result.isEmpty()) {
				result = "<UNKNOWN>";
			}
		}
		return result;
	}
	
	/**
	 * Check if the region is sold
	 * @return true if the region is sold, otherwise false
	 */
	public boolean isSold() {
		return getBuyer() != null;
	}
	
	/**
	 * Check if the region is being resold
	 * @return true if the region is available for reselling, otherwise false
	 */
	public boolean isInResellingMode() {
		return config.getBoolean("buy.resellMode");
	}
	
	/**
	 * Get the price of the region
	 * @return The price of the region
	 */
	public double getPrice() {
		return getDoubleSetting("buy.price");
	}
	
	/**
	 * Get the resell price of this region
	 * @return The resell price if isInResellingMode(), otherwise 0.0
	 */
	public double getResellPrice() {
		return config.getDouble("buy.resellPrice");
	}
	
	/**
	 * Get the formatted string of the price (includes prefix and suffix)
	 * @return The formatted string of the price
	 */
	public String getFormattedPrice() {
		return plugin.formatCurrency(getPrice());
	}
	
	/**
	 * Get the formatted string of the resellprice (includes prefix and suffix)
	 * @return The formatted string of the resellprice
	 */
	public String getFormattedResellPrice() {
		return plugin.formatCurrency(getResellPrice());
	}
	
	/**
	 * Change the price of the region
	 * @param price
	 */
	public void setPrice(double price) {
		setSetting("buy.price", price);
	}
	
	/**
	 * Set the region into resell mode with the given price
	 * @param price The price this region should be put up for sale
	 */
	public void enableReselling(double price) {
		setSetting("buy.resellMode", true);
		setSetting("buy.resellPrice", price);
	}
	
	/**
	 * Stop this region from being in resell mode
	 */
	public void disableReselling() {
		setSetting("buy.resellMode", null);
		setSetting("buy.resellPrice", null);
	}
	
	/**
	 * Get the moneyBack percentage
	 * @return The % of money the player will get back when selling
	 */
	public double getMoneyBackPercentage() {
		return getDoubleSetting("buy.moneyBack");
	}
	
	/**
	 * Get the amount of money that should be paid to the player when selling the region
	 * @return The amount of money the player should get back
	 */
	public double getMoneyBackAmount() {
		return getPrice() * (getMoneyBackPercentage() / 100.0);
	}
	
	/**
	 * Get the formatted string of the amount of the moneyBack amount
	 * @return String with currency symbols and proper fractional part
	 */
	public String getFormattedMoneyBackAmount() {
		return plugin.formatCurrency(getMoneyBackAmount());
	}
	
	@Override
	public HashMap<String, Object> getSpecificReplacements() {
		// Fill the replacements map with things specific to a BuyRegion
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put(AreaShop.tagPrice, getFormattedPrice());
		result.put(AreaShop.tagPlayerName, getPlayerName());
		result.put(AreaShop.tagPlayerUUID, getBuyer());
		result.put(AreaShop.tagResellPrice, getFormattedResellPrice());
		result.put(AreaShop.tagMoneyBackAmount, getFormattedMoneyBackAmount());
		double moneyBackPercent = getMoneyBackPercentage();
		if((moneyBackPercent%1.0) == 0.0) {
			result.put(AreaShop.tagMoneyBackPercentage, (int)moneyBackPercent);
		} else {
			result.put(AreaShop.tagMoneyBackPercentage, moneyBackPercent);
		}
		result.put(AreaShop.tagMaxInactiveTime, this.getFormattedInactiveTimeUntilSell());
		return result;
	}
	
	/**
	 * Minutes until automatic unrent when player is offline
	 * @return The number of minutes until the region is unrented while player is offline
	 */
	public long getInactiveTimeUntilSell() {
		return getLongSetting("buy.inactiveTimeUntilSell");
	}
	
	/**
	 * Get a human readable string indicating how long the player can be offline until automatic unrent
	 * @return String indicating the inactive time until unrent
	 */
	public String getFormattedInactiveTimeUntilSell() {
		return this.millisToHumanFormat(getInactiveTimeUntilSell()*60*1000);
	}
	
	/**
	 * Buy a region
	 * @param player The player that wants to buy the region
	 * @return true if it succeeded and false if not
	 */
	public boolean buy(Player player) {
		/* Check if the player has permission */
		if(player.hasPermission("areashop.buy")) {
			if(plugin.getEconomy() == null) {
				plugin.message(player, "general-noEconomy");
				return false;
			}
			if(getWorld() == null) {
				plugin.message(player, "general-noWorld", getWorldName());
				return false;
			}
			if(getRegion() == null) {
				plugin.message(player, "general-noRegion", getName());
				return false;
			}			
			if(!isSold() || (isInResellingMode() && !isBuyer(player))) {
				boolean isResell = isInResellingMode();
				// Check if the players needs to be in the world or region for buying
				if(restrictedToRegion() && (!player.getWorld().getName().equals(getWorldName()) 
						|| !getRegion().contains(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()))) {
					plugin.message(player, "buy-restrictedToRegion", getName());
					return false;
				}	
				if(restrictedToWorld() && !player.getWorld().getName().equals(getWorldName())) {
					plugin.message(player, "buy-restrictedToWorld", getWorldName(), player.getWorld().getName());
					return false;
				}			
				// Check region limits
				LimitResult limitResult = this.limitsAllowBuying(player);
				AreaShop.debug("LimitResult: " + limitResult.toString());
				if(!limitResult.actionAllowed()) {
					if(limitResult.getLimitingFactor() == LimitType.TOTAL) {
						plugin.message(player, "total-maximum", limitResult.getMaximum(), limitResult.getCurrent(), limitResult.getLimitingGroup());
						return false;
					}
					if(limitResult.getLimitingFactor() == LimitType.BUYS) {
						plugin.message(player, "buy-maximum", limitResult.getMaximum(), limitResult.getCurrent(), limitResult.getLimitingGroup());
						return false;
					}
					// Should not be reached, but is safe like this
					return false;
				}
				
				/* Check if the player has enough money */
				if((!isResell && plugin.getEconomy().has(player, getWorldName(), getPrice())) || (isResell && plugin.getEconomy().has(player, getWorldName(), getResellPrice()))) {
					UUID oldOwner = getBuyer();
					if(isResell && oldOwner != null) {
						clearFriends();
						double resellPrice = getResellPrice();
						/* Transfer the money to the previous owner */
						EconomyResponse r = plugin.getEconomy().withdrawPlayer(player, getWorldName(), getResellPrice());
						if(!r.transactionSuccess()) {
							plugin.message(player, "buy-payError");
							return false;
						}
						OfflinePlayer oldOwnerPlayer = Bukkit.getOfflinePlayer(oldOwner);
						if(oldOwnerPlayer != null) {
							r = plugin.getEconomy().depositPlayer(oldOwnerPlayer, getWorldName(), getResellPrice());
							if(!r.transactionSuccess()) {
								plugin.getLogger().warning("Something went wrong with paying '" + oldOwnerPlayer.getName() + "' " + getFormattedPrice() + " for his resell of region " + getName() + " to " + player.getName());
							}
						}
						// Resell is done, disable that now
						disableReselling();
						// Run commands
						this.runEventCommands(RegionEvent.RESELL, true);
						// Set the owner
						setLandlord(player.getUniqueId());
		
						// Update everything
						handleSchematicEvent(RegionEvent.RESELL);
						updateSigns();
						updateRegionFlags();

						// Send message to the player
						plugin.message(player, "buy-successResale", getName(), oldOwnerPlayer.getName());
						Player seller = Bukkit.getPlayer(oldOwner);
						if(seller != null) {
							plugin.message(player, "buy-successSeller", getName(), getPlayerName(), resellPrice);
						}						
						AreaShop.debug(player.getName() + " has bought region " + getName() + " for " + getFormattedPrice() + " from " + oldOwnerPlayer.getName());
		
						this.saveRequired();
						// Run commands
						this.runEventCommands(RegionEvent.RESELL, false);
					} else {
						// Substract the money from the players balance
						EconomyResponse r = plugin.getEconomy().withdrawPlayer(player, getWorldName(), getPrice());
						if(!r.transactionSuccess()) {
							plugin.message(player, "buy-payError");
							return false;
						}
						// Optionally give money to the landlord
						if(getLandlord() != null) {
							OfflinePlayer landlord = Bukkit.getOfflinePlayer(getLandlord());
							if(landlord != null) {
								r = plugin.getEconomy().depositPlayer(landlord, getWorldName(), getPrice());
								if(!r.transactionSuccess()) {
									plugin.getLogger().warning("Something went wrong with paying '" + landlord.getName() + "' " + getFormattedPrice() + " for his sell of region " + getName() + " to " + player.getName());
								}
							}
						}
						AreaShop.debug(player.getName() + " has bought region " + getName() + " for " + getFormattedPrice());
						
						// Run commands
						this.runEventCommands(RegionEvent.BOUGHT, true);
						// Set the owner
						setLandlord(player.getUniqueId());
		
						// Update everything
						handleSchematicEvent(RegionEvent.BOUGHT);
						updateSigns();
						updateRegionFlags();

						// Send message to the player
						plugin.message(player, "buy-succes", getName());
						this.saveRequired();
						// Run commands
						this.runEventCommands(RegionEvent.BOUGHT, false);
					}				
					return true;
				} else {
					/* Player has not enough money */
					String requiredMoney = "";
					if(isResell) {
						requiredMoney = getFormattedResellPrice();
					} else {
						requiredMoney = getFormattedPrice();
					}					
					plugin.message(player, "buy-lowMoney", plugin.formatCurrency(plugin.getEconomy().getBalance(player, getWorldName())), requiredMoney);
				}
			} else {
				if(isBuyer(player)) {
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
	 * Sell a buyed region, get part of the money back
	 * @param regionName
	 */
	public void sell(boolean giveMoneyBack) {
		// Run commands
		this.runEventCommands(RegionEvent.SOLD, true);
		
		disableReselling();
		/* Give part of the buying price back */
		double moneyBack =  getMoneyBackAmount();
		if(moneyBack > 0 && giveMoneyBack) {
			/* Give back the money */
			OfflinePlayer player = Bukkit.getOfflinePlayer(getBuyer());
			if(player != null) {
				EconomyResponse response = null;
				boolean error = false;
				try {
					response = plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(getBuyer()), getWorldName(), moneyBack);
				} catch(Exception e) {
					error = true;
				}
				if(error || response == null || !response.transactionSuccess()) {
					plugin.getLogger().warning("Something went wrong with paying back money to " + getPlayerName() + " while selling region " + getName());
				}	
			}
		}
		
		/* Debug message */
		AreaShop.debug(getPlayerName() + " has sold " + getName() + ", got " + plugin.formatCurrency(moneyBack) + " money back");

		/* Update everything */
		handleSchematicEvent(RegionEvent.SOLD);
		updateRegionFlags(RegionState.FORSALE);
		
		/* Remove friends and the owner */
		clearFriends();
		setLandlord(null);		
		
		updateSigns();
		
		this.saveRequired();
		// Run commands
		this.runEventCommands(RegionEvent.SOLD, false);
	}

	@Override
	public boolean checkInactive() {
		if(isDeleted() || !isSold()) {
			return false;
		}
		OfflinePlayer player = Bukkit.getOfflinePlayer(getBuyer());
		long inactiveSetting = getInactiveTimeUntilSell();
		if(inactiveSetting <= 0 || player.isOp()) {
			return false;
		}
		//AreaShop.debug("currentTime=" + Calendar.getInstance().getTimeInMillis() + ", getLastPlayed()=" + player.getLastPlayed() + ", timeInactive=" + (Calendar.getInstance().getTimeInMillis()-player.getLastPlayed()) + ", inactiveSetting*60*1000=" + inactiveSetting * 60 * 1000);
		if(Calendar.getInstance().getTimeInMillis() > (player.getLastPlayed() + inactiveSetting * 60 * 1000)) {
			plugin.getLogger().info("Region " + getName() + " sold because of inactivity for player " + getPlayerName());
			AreaShop.debug("currentTime=" + Calendar.getInstance().getTimeInMillis() + ", getLastPlayed()=" + player.getLastPlayed() + ", timeInactive=" + (Calendar.getInstance().getTimeInMillis()-player.getLastPlayed()) + ", inactiveSetting*60*1000=" + inactiveSetting * 60 * 1000);
			this.sell(true);
			return true;
		}
		return false;
	}

}

























