package nl.evolutioncoding.areashop.regions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

import net.milkbowl.vault.economy.EconomyResponse;
import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.exceptions.RegionCreateException;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class BuyRegion extends GeneralRegion {

	public BuyRegion(AreaShop plugin, YamlConfiguration config) throws RegionCreateException {
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
		if(isSold()) {
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
	public void setBuyer(UUID buyer) {
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
	 * @return The name of the player that owns this region
	 */
	public String getPlayerName() {
		return plugin.toName(getBuyer());
	}
	
	/**
	 * Check if the region is sold
	 * @return true if the region is sold, otherwise false
	 */
	public boolean isSold() {
		return getBuyer() != null;
	}
	
	/**
	 * Get the price of the region
	 * @return The price of the region
	 */
	public double getPrice() {
		return getDoubleSetting("buy.price");
	}
	
	/**
	 * Get the formatted string of the price (includes prefix and suffix)
	 * @return The formatted string of the price
	 */
	public String getFormattedPrice() {
		return plugin.formatCurrency(getPrice());
	}
	
	/**
	 * Change the price of the region
	 * @param price
	 */
	public void setPrice(double price) {
		setSetting("buy.price", price);
	}
	
	@Override
	public HashMap<String, Object> getSpecificReplacements() {
		// Fill the replacements map with things specific to a BuyRegion
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put(AreaShop.tagPrice, getFormattedPrice());
		result.put(AreaShop.tagPlayerName, getPlayerName());
		result.put(AreaShop.tagPlayerUUID, getBuyer());
		// TODO: Add more?
		
		return result;
	}
	
	/**
	 * Buy a region
	 * @param player The player that wants to buy the region
	 * @return true if it succeeded and false if not
	 */
	public boolean buy(Player player) {
		/* Check if the player has permission */
		if(player.hasPermission("areashop.buy")) {	
			if(!isSold()) {
				// Check if the players needs to be in the world or region for buying
				if(!player.getWorld().getName().equals(getWorldName()) && getBooleanSetting("general.restrictedToWorld")) {
					plugin.message(player, "buy-restrictedToWorld", getWorldName(), player.getWorld().getName());
					return false;
				}
				if((!player.getWorld().getName().equals(getWorldName()) 
						|| !getRegion().contains(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ())
					) && getBooleanSetting("general.restrictedToRegion")) {
					plugin.message(player, "buy-restrictedToRegion", getName());
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
				if(plugin.getEconomy().has(player, getWorldName(), getPrice())) {
					
					/* Substract the money from the players balance */
					EconomyResponse r = plugin.getEconomy().withdrawPlayer(player, getWorldName(), getPrice());
					if(!r.transactionSuccess()) {
						plugin.message(player, "buy-payError");
						return false;
					}										
					// Run commands
					this.runEventCommands(RegionEvent.BOUGHT, true);
					
					/* Set the owner */
					setBuyer(player.getUniqueId());
	
					/* Update everything */
					handleSchematicEvent(RegionEvent.BOUGHT);
					updateSigns();
					updateRegionFlags(RegionState.SOLD);

					/* Send message to the player */
					plugin.message(player, "buy-succes", getName());
					AreaShop.debug(player.getName() + " has bought region " + getName() + " for " + getFormattedPrice());
					
					this.saveRequired();
					// Run commands
					this.runEventCommands(RegionEvent.BOUGHT, false);
					return true;
				} else {
					/* Player has not enough money */
					plugin.message(player, "buy-lowMoney", plugin.formatCurrency(plugin.getEconomy().getBalance(player, getWorldName())), getFormattedPrice());
				}
			} else {
				if(player.getUniqueId().equals(getBuyer())) {
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
		
		/* Give part of the buying price back */
		double percentage = getDoubleSetting("buy.moneyBack") / 100.0;
		double moneyBack =  getPrice() * percentage;
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
					plugin.getLogger().info("Something went wrong with paying back money to " + getPlayerName() + " while selling region " + getName());
				}	
			}
		}
		
		/* Debug message */
		AreaShop.debug(getPlayerName() + " has sold " + getName() + ", got " + plugin.formatCurrency(moneyBack) + " money back");

		/* Update everything */
		handleSchematicEvent(RegionEvent.SOLD);
		updateRegionFlags(RegionState.FORSALE);
		
		/* Remove the player */
		setBuyer(null);		
		
		updateSigns();
		
		this.saveRequired();
		// Run commands
		this.runEventCommands(RegionEvent.SOLD, false);
	}

	@Override
	public boolean checkInactive() {
		if(!isSold()) {
			return false;
		}
		OfflinePlayer player = Bukkit.getOfflinePlayer(getBuyer());
		int inactiveSetting = getIntegerSetting("buy.inactiveTimeUntilSell");
		if(inactiveSetting <= 0 || player.isOp()) {
			return false;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(player.getLastPlayed() + inactiveSetting * 60 * 1000);
		if(Calendar.getInstance().getTimeInMillis() > calendar.getTimeInMillis()) {
			plugin.getLogger().info("Region " + getName() + " sold because of inactivity for player " + getPlayerName());
			this.sell(true);
			return true;
		}
		return false;
	}

}

























