package nl.evolutioncoding.AreaShop.regions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.milkbowl.vault.economy.EconomyResponse;
import nl.evolutioncoding.AreaShop.AreaShop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class BuyRegion extends GeneralRegion {

	private UUID owner = null;
	private double price;
	
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
	
	public BuyRegion(AreaShop plugin, Map<String, String> settings) {
		super(plugin, settings);
		
		if(settings.get(AreaShop.keyPlayerUUID) != null) {
			owner = UUID.fromString(settings.get(AreaShop.keyPlayerUUID));
		}
		price = Double.parseDouble(settings.get(AreaShop.keyPrice));
		AreaShop.debug("BuyRegion: " + getName() + ", map: " + settings.toString());
	}
	
	public BuyRegion(AreaShop plugin, String name, Location signLocation, double price) {
		super(plugin, name, signLocation);
		
		this.price = price;
	}
	
	/**
	 * Get the UUID of the owner of this region
	 * @return The UUID of the owner of this region
	 */
	public UUID getOwner() {
		return owner;
	}
	
	/**
	 * Get the name of the player that owns this region
	 * @return The name of the player that owns this region
	 */
	public String getPlayerName() {
		return plugin.toName(owner);
	}
	
	/**
	 * Check if the region is sold
	 * @return true if the region is sold, otherwise false
	 */
	public boolean isSold() {
		return owner != null;
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
	 * Change the price of the region
	 * @param price
	 */
	public void setPrice(double price) {
		this.price = price;
		updateSigns();
		updateRegionFlags();
	}
	
	@Override
	public HashMap<String, String> toMap() {
		HashMap<String, String> result = super.toMap();
		
		if(isSold()) {
			result.put(AreaShop.keyPlayerUUID, owner.toString());
		}
		result.put(AreaShop.keyPrice, String.valueOf(price));
		
		return result;
	}
	
	/**
	 * Save this buy to a file (currently all buys will be saved again)
	 */
	@Override
	public void save() {
		getFileManager().saveBuys();
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
				int buyNumber = 0;
				Iterator<String> it = getFileManager().getBuys().keySet().iterator();
				while(it.hasNext()) {
					String next = it.next();
					if(player.getUniqueId().equals(getFileManager().getBuy(next).getOwner())) {
						buyNumber++;
					}
				}
				int rentNumber = 0;
				it = getFileManager().getRents().keySet().iterator();
				while(it.hasNext()) {
					String next = it.next();
					if(player.getUniqueId().equals(getFileManager().getRent(next).getRenter())) {
						rentNumber++;
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
				if(plugin.getEconomy().has(player, getWorldName(), price)) {
					
					/* Substract the money from the players balance */
					EconomyResponse r = plugin.getEconomy().withdrawPlayer(player, price);
					if(!r.transactionSuccess()) {
						plugin.message(player, "buy-payError");
						return false;
					}										
					
					/* Set the owner */
					owner = player.getUniqueId();
	
					/* Update everything */
					handleSchematicEvent(BuyEvent.BOUGHT);
					updateSigns();
					updateRegionFlags();

					/* Send message to the player */
					plugin.message(player, "buy-succes", getName());
					AreaShop.debug(player.getName() + " has bought region " + getName() + " for " + plugin.formatCurrency(price));
					
					plugin.getFileManager().saveBuys();
					return true;
				} else {
					/* Player has not enough money */
					plugin.message(player, "buy-lowMoney", plugin.formatCurrency(plugin.getEconomy().getBalance(player, getWorldName())), plugin.formatCurrency(price));
				}
			} else {
				if(player.getUniqueId().equals(owner)) {
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
		double moneyBack =  price * percentage;
		if(moneyBack > 0 && giveMoneyBack) {
			/* Give back the money */
			EconomyResponse r = plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(owner), moneyBack);
			if(!r.transactionSuccess()) {
				plugin.getLogger().info("Something went wrong with paying back money while unrenting");
			}	
		}
		
		/* Debug message */
		AreaShop.debug(getPlayerName() + " has sold " + getName() + ", got " + plugin.formatCurrency(moneyBack) + " money back");
		
		/* Remove the player */
		owner = null;
		
		/* Update everything */
		handleSchematicEvent(BuyEvent.SOLD);
		updateSigns();
		updateRegionFlags();	
		plugin.getFileManager().saveBuys();
	}
	
	
	public String[] getSignLines() {
		String[] lines = new String[3];
		if(isSold()) {
			lines[0] = plugin.fixColors(plugin.config().getString("signBuyed"));
			lines[1] = getName();
			lines[2] = plugin.toName(owner);
		} else {			
			lines[0] = plugin.fixColors(plugin.config().getString("signBuyable"));
			lines[1] = getName();
			lines[2] = plugin.formatCurrency(price);
		}		
		return lines;
	}
	

	@Override
	public void updateRegionFlags() {
		HashMap<String, String> replacements = new HashMap<String, String>();
		replacements.put(AreaShop.tagRegionName, getName());
		replacements.put(AreaShop.tagPrice, plugin.formatCurrency(price));
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






