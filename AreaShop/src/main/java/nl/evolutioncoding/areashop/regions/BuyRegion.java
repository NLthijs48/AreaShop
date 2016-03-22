package nl.evolutioncoding.areashop.regions;

import net.milkbowl.vault.economy.EconomyResponse;
import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.Utils;
import nl.evolutioncoding.areashop.events.ask.BuyingRegionEvent;
import nl.evolutioncoding.areashop.events.ask.ResellingRegionEvent;
import nl.evolutioncoding.areashop.events.ask.SellingRegionEvent;
import nl.evolutioncoding.areashop.events.notify.BoughtRegionEvent;
import nl.evolutioncoding.areashop.events.notify.ResoldRegionEvent;
import nl.evolutioncoding.areashop.events.notify.SoldRegionEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
		String buyer = config.getString("buy.buyer");
		if(buyer != null) {
			try {
				return UUID.fromString(buyer);
			} catch(IllegalArgumentException e) {
				// Incorrect UUID
			}
		}
		return null;
	}
	
	/**
	 * Check if a player is the buyer of this region
	 * @param player Player to check
	 * @return true if this player owns this region, otherwise false
	 */
	public boolean isBuyer(Player player) {
		return player != null && isBuyer(player.getUniqueId());
	}
	public boolean isBuyer(UUID player) {
		UUID buyer = getBuyer();
		return !(buyer == null || player == null) && buyer.equals(player);
	}
	
	/**
	 * Set the buyer of this region
	 * @param buyer The UUID of the player that should be set as buyer
	 */
	public void setBuyer(UUID buyer) {
		if(buyer == null) {
			setSetting("buy.buyer", null);
			setSetting("buy.buyerName", null);
		} else {
			setSetting("buy.buyer", buyer.toString());
			setSetting("buy.buyerName", Utils.toName(buyer));
		}
	}
	
	/**
	 * Get the name of the player that owns this region
	 * @return The name of the player that owns this region, if unavailable by UUID it will return the old cached name, if that is unavailable it will return <UNKNOWN>
	 */
	public String getPlayerName() {
		String result = Utils.toName(getBuyer());
		if(result == null || result.isEmpty()) {
			result = getStringSetting("buy.buyerName");
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
		return Utils.formatCurrency(getPrice());
	}
	
	/**
	 * Get the formatted string of the resellprice (includes prefix and suffix)
	 * @return The formatted string of the resellprice
	 */
	public String getFormattedResellPrice() {
		return Utils.formatCurrency(getResellPrice());
	}
	
	/**
	 * Change the price of the region
	 * @param price The price to set this region to
	 */
	public void setPrice(double price) {
		setSetting("buy.price", price);
	}
	
	/**
	 * Remove the price so that the price will be taken from a group or the default.yml file
	 */
	public void removePrice() {
		setSetting("buy.price", null);
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
		return Utils.formatCurrency(getMoneyBackAmount());
	}
	
	@Override
	public Map<String, Object> getSpecificReplacements() {
		// Fill the replacements map with things specific to a BuyRegion
		HashMap<String, Object> result = new HashMap<>();
		result.put(AreaShop.tagPrice, getFormattedPrice());
		result.put(AreaShop.tagRawPrice, getPrice());
		result.put(AreaShop.tagPlayerName, getPlayerName());
		result.put(AreaShop.tagPlayerUUID, getBuyer());
		result.put(AreaShop.tagResellPrice, getFormattedResellPrice());
		result.put(AreaShop.tagRawResellPrice, getResellPrice());
		result.put(AreaShop.tagMoneyBackAmount, getFormattedMoneyBackAmount());
		result.put(AreaShop.tagRawMoneyBackAmount, getMoneyBackAmount());
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
	 * @return The number of milliseconds until the region is unrented while player is offline
	 */
	public long getInactiveTimeUntilSell() {
		return Utils.getDurationFromMinutesOrStringInput(getStringSetting("buy.inactiveTimeUntilSell"));
	}
	
	/**
	 * Get a human readable string indicating how long the player can be offline until automatic unrent
	 * @return String indicating the inactive time until unrent
	 */
	public String getFormattedInactiveTimeUntilSell() {
		return this.millisToHumanFormat(getInactiveTimeUntilSell());
	}
	
	/**
	 * Buy a region
	 * @param player The player that wants to buy the region
	 * @return true if it succeeded and false if not
	 */
	@SuppressWarnings("deprecation")
	public boolean buy(Player player) {
		// Check if the player has permission
		if(player.hasPermission("areashop.buy")) {
			if(plugin.getEconomy() == null) {
				message(player, "general-noEconomy");
				return false;
			}
			if(isInResellingMode()) {
				if(!player.hasPermission("areashop.buyresell")) {
					message(player, "buy-noPermissionResell");
					return false;
				}
			} else {
				if(!player.hasPermission("areashop.buynormal")) {
					message(player, "buy-noPermissionNoResell");
					return false;
				}
			}
			if(getWorld() == null) {
				message(player, "general-noWorld");
				return false;
			}
			if(getRegion() == null) {
				message(player, "general-noRegion");
				return false;
			}
			if(!isSold() || (isInResellingMode() && !isBuyer(player))) {
				boolean isResell = isInResellingMode();
				// Check if the players needs to be in the world or region for buying
				if(restrictedToRegion() && (!player.getWorld().getName().equals(getWorldName()) 
						|| !getRegion().contains(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()))) {
					message(player, "buy-restrictedToRegion");
					return false;
				}	
				if(restrictedToWorld() && !player.getWorld().getName().equals(getWorldName())) {
					message(player, "buy-restrictedToWorld", player.getWorld().getName());
					return false;
				}			
				// Check region limits
				LimitResult limitResult = this.limitsAllow(RegionType.BUY, player);
				AreaShop.debug("LimitResult: " + limitResult.toString());
				if(!limitResult.actionAllowed()) {
					if(limitResult.getLimitingFactor() == LimitType.TOTAL) {
						message(player, "total-maximum", limitResult.getMaximum(), limitResult.getCurrent(), limitResult.getLimitingGroup());
						return false;
					}
					if(limitResult.getLimitingFactor() == LimitType.BUYS) {
						message(player, "buy-maximum", limitResult.getMaximum(), limitResult.getCurrent(), limitResult.getLimitingGroup());
						return false;
					}
					// Should not be reached, but is safe like this
					return false;
				}

				// Check if the player has enough money
				if((!isResell && plugin.getEconomy().has(player, getWorldName(), getPrice())) || (isResell && plugin.getEconomy().has(player, getWorldName(), getResellPrice()))) {
					UUID oldOwner = getBuyer();
					if(isResell && oldOwner != null) {
						// Broadcast and check event
						ResellingRegionEvent event = new ResellingRegionEvent(this, player);
						Bukkit.getPluginManager().callEvent(event);
						if(event.isCancelled()) {
							message(player, "general-cancelled", event.getReason());
							return false;
						}

						clearFriends();
						double resellPrice = getResellPrice();
						// Transfer the money to the previous owner
						EconomyResponse r = plugin.getEconomy().withdrawPlayer(player, getWorldName(), getResellPrice());
						if(!r.transactionSuccess()) {
							message(player, "buy-payError");
							AreaShop.debug("Something went wrong with getting money from " + player.getName() + " while buying " + getName() + ": " + r.errorMessage);
							return false;
						}
						r = null;
						OfflinePlayer oldOwnerPlayer = Bukkit.getOfflinePlayer(oldOwner);
						String oldOwnerName = getPlayerName();
						if(oldOwnerPlayer != null && oldOwnerPlayer.getName() != null) {
							r = plugin.getEconomy().depositPlayer(oldOwnerPlayer, getWorldName(), getResellPrice());
							oldOwnerName = oldOwnerPlayer.getName();
						} else if (oldOwnerName != null) {
							r = plugin.getEconomy().depositPlayer(oldOwnerName, getWorldName(), getResellPrice());
						}
						if(r == null || !r.transactionSuccess()) {
							plugin.getLogger().warning("Something went wrong with paying '" + oldOwnerName + "' " + getFormattedPrice() + " for his resell of region " + getName() + " to " + player.getName());
						}
						// Resell is done, disable that now
						disableReselling();
						// Run commands
						this.runEventCommands(RegionEvent.RESELL, true);
						// Set the owner
						setBuyer(player.getUniqueId());
						updateLastActiveTime();

						// Update everything
						handleSchematicEvent(RegionEvent.RESELL);

						// Notify about updates
						this.notifyAndUpdate(new ResoldRegionEvent(this, oldOwner));

						// Send message to the player
						message(player, "buy-successResale", oldOwnerName);
						Player seller = Bukkit.getPlayer(oldOwner);
						if(seller != null) {
							message(player, "buy-successSeller", resellPrice);
						}
						// Run commands
						this.runEventCommands(RegionEvent.RESELL, false);
					} else {
						// Broadcast and check event
						BuyingRegionEvent event = new BuyingRegionEvent(this, player);
						Bukkit.getPluginManager().callEvent(event);
						if(event.isCancelled()) {
							message(player, "general-cancelled", event.getReason());
							return false;
						}

						// Substract the money from the players balance
						EconomyResponse r = plugin.getEconomy().withdrawPlayer(player, getWorldName(), getPrice());
						if(!r.transactionSuccess()) {
							message(player, "buy-payError");
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
								r = plugin.getEconomy().depositPlayer(landlordPlayer, getWorldName(), getPrice());
							} else {
								r = plugin.getEconomy().depositPlayer(landlordName, getWorldName(), getPrice());
							}
							if(r != null && !r.transactionSuccess()) {
								plugin.getLogger().warning("Something went wrong with paying '" + landlordName + "' " + getFormattedPrice() + " for his sell of region " + getName() + " to " + player.getName());
							}
						}

						// Run commands
						this.runEventCommands(RegionEvent.BOUGHT, true);
						// Set the owner
						setBuyer(player.getUniqueId());
						updateLastActiveTime();

						// Notify about updates
						this.notifyAndUpdate(new BoughtRegionEvent(this));

						// Update everything
						handleSchematicEvent(RegionEvent.BOUGHT);

						// Send message to the player
						message(player, "buy-succes");
						// Run commands
						this.runEventCommands(RegionEvent.BOUGHT, false);
					}				
					return true;
				} else {
					// Player has not enough money
					if(isResell) {
						message(player, "buy-lowMoneyResell", Utils.formatCurrency(plugin.getEconomy().getBalance(player, getWorldName())));
					} else {
						message(player, "buy-lowMoney", Utils.formatCurrency(plugin.getEconomy().getBalance(player, getWorldName())));
					}
				}
			} else {
				if(isBuyer(player)) {
					message(player, "buy-yours");
				} else {
					message(player, "buy-someoneElse");
				}
			}	
		} else {
			message(player, "buy-noPermission");
		}
		return false;
	}
	
	/**
	 * Sell a buyed region, get part of the money back
	 * @param giveMoneyBack true if the player should be given money back, otherwise false
	 * @param executor CommandSender to receive a message when the sell fails, or null
	 */
	@SuppressWarnings("deprecation")
	public boolean sell(boolean giveMoneyBack, CommandSender executor) {
		boolean own = executor != null && executor instanceof Player && this.isBuyer((Player)executor);
		if(executor != null) {
			if(!executor.hasPermission("areashop.sell") && !own) {
				message(executor, "sell-noPermissionOther");
				return false;
			}
			if(!executor.hasPermission("areashop.sell") && !executor.hasPermission("areashop.sellown") && own) {
				message(executor, "sell-noPermission");
				return false;
			}
		}

		if(plugin.getEconomy() == null) {
			return false;
		}

		// Broadcast and check event
		SellingRegionEvent event = new SellingRegionEvent(this);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) {
			message(executor, "general-cancelled", event.getReason());
			return false;
		}

		// Run commands
		this.runEventCommands(RegionEvent.SOLD, true);
		
		disableReselling();
		// Give part of the buying price back
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
			OfflinePlayer player = Bukkit.getOfflinePlayer(getBuyer());
			if(player != null && !noPayBack) {
				EconomyResponse response = null;
				boolean error = false;
				try {
					if(player.getName() != null) {
						response = plugin.getEconomy().depositPlayer(player, getWorldName(), moneyBack);
					} else if(getPlayerName() != null) {
						response = plugin.getEconomy().depositPlayer(getPlayerName(), getWorldName(), moneyBack);
					}
				} catch(Exception e) {
					error = true;
				}
				if(error || response == null || !response.transactionSuccess()) {
					plugin.getLogger().warning("Something went wrong with paying back money to " + getPlayerName() + " while selling region " + getName());
				}	
			}
		}

		if(own) {
			message(executor, "sell-soldYours");
		} else {
			message(executor, "sell-sold");
		}

		// Remove friends and the owner
		clearFriends();
		UUID oldBuyer = getBuyer();
		setBuyer(null);
		removeLastActiveTime();

		// Notify about updates
		this.notifyAndUpdate(new SoldRegionEvent(this, oldBuyer));

		// Update everything
		handleSchematicEvent(RegionEvent.SOLD);

		// Run commands
		this.runEventCommands(RegionEvent.SOLD, false);
		return true;
	}

	@Override
	public boolean checkInactive() {
		if(isDeleted() || !isSold()) {
			return false;
		}
		long inactiveSetting = getInactiveTimeUntilSell();
		OfflinePlayer player = Bukkit.getOfflinePlayer(getBuyer());
		if(inactiveSetting <= 0 || player.isOp()) {
			return false;
		}
		long lastPlayed = getLastActiveTime();
		//AreaShop.debug("currentTime=" + Calendar.getInstance().getTimeInMillis() + ", getLastPlayed()=" + lastPlayed + ", timeInactive=" + (Calendar.getInstance().getTimeInMillis()-player.getLastPlayed()) + ", inactiveSetting=" + inactiveSetting);
		if(Calendar.getInstance().getTimeInMillis() > (lastPlayed + inactiveSetting)) {
			plugin.getLogger().info("Region " + getName() + " unrented because of inactivity for player " + getPlayerName());
			AreaShop.debug("currentTime=" + Calendar.getInstance().getTimeInMillis() + ", getLastPlayed()=" + lastPlayed + ", timeInactive=" + (Calendar.getInstance().getTimeInMillis()-player.getLastPlayed()) + ", inactiveSetting=" + inactiveSetting);
			return this.sell(true, null);
		}
		return false;
	}

}

























