package nl.evolutioncoding.AreaShop.regions;

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
		UUID buyer = getBuyer();
		if(buyer == null || player == null) {
			return false;
		} else {
			return buyer.equals(player.getUniqueId());
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
				// Check if the player can still buy
				int maximumBuys = getMaxBuyRegions(player);
				AreaShop.debug("maximumBuys=" + maximumBuys);
				if(getCurrentBuyRegions(player) >= maximumBuys) {
					plugin.message(player, "buy-maximum", maximumBuys);
					return false;
				}
				int maximumTotal = getMaxTotalRegions(player);
				if(getCurrentTotalRegions(player) >= maximumTotal) {
					plugin.message(player, "total-maximum", maximumTotal);
					return false;
				}
				
				/* Check if the player has enough money */
				if(plugin.getEconomy().has(player, getWorldName(), getPrice())) {
					
					/* Substract the money from the players balance */
					EconomyResponse r = plugin.getEconomy().withdrawPlayer(player, getPrice());
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
					
					this.save();
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
				EconomyResponse r = null;
				boolean error = false;
				try {
					r = plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(getBuyer()), moneyBack);
				} catch(Exception e) {
					error = true;
				}
				if(error || r == null || !r.transactionSuccess()) {
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
		
		this.save();
		// Run commands
		this.runEventCommands(RegionEvent.SOLD, false);
	}

}






