package nl.evolutioncoding.AreaShop.regions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import net.milkbowl.vault.economy.EconomyResponse;
import nl.evolutioncoding.AreaShop.AreaShop;
import nl.evolutioncoding.AreaShop.Exceptions.RegionCreateException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class BuyRegion extends GeneralRegion {
	/* Enum for schematic event types */
	public enum BuyEvent {		
		CREATED("created"),
		DELETED("deleted"),
		BOUGHT("bought"),
		SOLD("sold");
		
		private final String value;
		private BuyEvent(String value) {
			this.value = value;
		}
		public String getValue() {
			return value;
		}
	} 
	
	public BuyRegion(AreaShop plugin, YamlConfiguration config) throws RegionCreateException {
		super(plugin, config);
	}
	
	public BuyRegion(AreaShop plugin, String name, World world, Location signLocation, double price) {
		super(plugin, name, world, signLocation);
		setSetting("price", price);
	}
	
	/**
	 * Get the UUID of the owner of this region
	 * @return The UUID of the owner of this region
	 */
	public UUID getBuyer() {
		String buyer = getStringSetting("buyer");
		if(buyer != null) {
			try {
				return UUID.fromString(buyer);
			} catch(IllegalArgumentException e) {}
		}
		return null;
	}
	
	public void setBuyer(UUID buyer) {
		if(buyer == null) {
			setSetting("buyer", null);
			setSetting("buyerName", null);
		} else {
			setSetting("buyer", buyer.toString());
			setSetting("buyerName", plugin.toName(buyer));
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
	 * Change the price of the region
	 * @param price
	 */
	public void setPrice(double price) {
		setSetting("price", price);
		updateSigns();
		updateRegionFlags();
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
				/* Check if the player can still buy */
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
				if(plugin.getEconomy().has(player, getWorldName(), getPrice())) {
					
					/* Substract the money from the players balance */
					EconomyResponse r = plugin.getEconomy().withdrawPlayer(player, getPrice());
					if(!r.transactionSuccess()) {
						plugin.message(player, "buy-payError");
						return false;
					}										
					
					/* Set the owner */
					setBuyer(player.getUniqueId());
	
					/* Update everything */
					handleSchematicEvent(BuyEvent.BOUGHT);
					updateSigns();
					updateRegionFlags();

					/* Send message to the player */
					plugin.message(player, "buy-succes", getName());
					AreaShop.debug(player.getName() + " has bought region " + getName() + " for " + getFormattedPrice());
					
					this.save();
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
		/* Give part of the buying price back */
		double percentage = plugin.config().getDouble("buyMoneyBack") / 100.0;
		double moneyBack =  getPrice() * percentage;
		if(moneyBack > 0 && giveMoneyBack) {
			/* Give back the money */
			EconomyResponse r = plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(getBuyer()), moneyBack);
			if(!r.transactionSuccess()) {
				plugin.getLogger().info("Something went wrong with paying back money while unrenting");
			}	
		}
		
		/* Debug message */
		AreaShop.debug(getPlayerName() + " has sold " + getName() + ", got " + plugin.formatCurrency(moneyBack) + " money back");
		
		/* Remove the player */
		setBuyer(null);
		
		/* Update everything */
		handleSchematicEvent(BuyEvent.SOLD);
		updateSigns();
		updateRegionFlags();	
		this.save();
	}
	
	
	public String[] getSignLines() {
		String[] lines = new String[3];
		if(isSold()) {
			lines[0] = plugin.fixColors(plugin.config().getString("signBuyed"));
			lines[1] = getName();
			lines[2] = plugin.toName(getBuyer());
		} else {			
			lines[0] = plugin.fixColors(plugin.config().getString("signBuyable"));
			lines[1] = getName();
			lines[2] = getFormattedPrice();
		}		
		return lines;
	}
	

	@Override
	public void updateRegionFlags() {
		HashMap<String, String> replacements = new HashMap<String, String>();
		replacements.put(AreaShop.tagRegionName, getName());
		replacements.put(AreaShop.tagPrice, getFormattedPrice());
		replacements.put(AreaShop.tagPlayerName, getPlayerName());
		if(isSold()) {
			this.setRegionFlags(plugin.config().getConfigurationSection("flagsSold"), replacements);
		} else {
			this.setRegionFlags(plugin.config().getConfigurationSection("flagsForSale"), replacements);
		}		
	}
	
	
	/**
	 * Checks an event and handles saving to and restoring from schematic for it
	 * @param type The type of event
	 */
	public void handleSchematicEvent(BuyEvent type) {
		// Check for the general killswitch
		if(!plugin.config().getBoolean("enableSchematics")) {
			return;
		}
		// Check the individual options
		if("false".equalsIgnoreCase(getRestoreSetting())) {
			return;
		} else if("true".equalsIgnoreCase(getRestoreSetting())) {
		} else {
			if(!plugin.config().getBoolean("useBuyRestore")) {
				return;
			}
		}
		// Get the safe and restore names		
		String save = plugin.config().getString("buySchematicProfiles." + getRestoreProfile() + "." + type.getValue() + ".save");
		if(save == null) {
			plugin.config().getString("buySchematicProfiles.default." + type.getValue() + ".save");
		}
		String restore = plugin.config().getString("buySchematicProfiles." + getRestoreProfile() + "." + type.getValue() + ".restore");
		if(restore == null) {
			plugin.config().getString("buySchematicProfiles.default." + type.getValue() + ".restore");
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






