package nl.evolutioncoding.AreaShop;

import java.util.HashMap;

import nl.evolutioncoding.AreaShop.AreaShop.RegionEventType;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * Checks for placement of signs for this plugin
 * @author NLThijs48
 */
public final class SignChangeListener implements Listener {
	AreaShop plugin;
	
	/**
	 * Constructor
	 * @param plugin The AreaShop plugin
	 */
	public SignChangeListener(AreaShop plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Called when a sign is changed
	 * @param event The event
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onSignChange(SignChangeEvent event) {
		Player player = event.getPlayer();
		
		/* Check if the sign is meant for this plugin */
		if(event.getLine(0).contains(plugin.config().getString("rentSign"))) {
			if(!player.hasPermission("areashop.createrent")) {
				plugin.message(player, "setup-noPermissionRent");				
				return;
			}
			
			/* Get the other lines */
			String secondLine = event.getLine(1);
			String thirdLine = event.getLine(2);
			String fourthLine = event.getLine(3);
			
			/* Get the regionManager for accessing regions */
			RegionManager regionManager = plugin.getWorldGuard().getRegionManager(event.getPlayer().getWorld());
			
			/* If the secondLine does not contain a name try to find the region by location */
			if(secondLine == null || secondLine.length() == 0) {
				ApplicableRegionSet regions = regionManager.getApplicableRegions(event.getBlock().getLocation());
				if(regions != null) {
					boolean first = true;
					ProtectedRegion candidate = null;
					for(ProtectedRegion pr : regions) {
						if(first) {
							candidate = pr;
							first = false;
						} else {
							if(pr.getPriority() > candidate.getPriority()) {
								candidate = pr;
							} else if(pr.getParent() != null && pr.getParent().equals(candidate)) {
								candidate = pr;
							} else {
								plugin.message(player, "setup-couldNotDetect", candidate.getId(), pr.getId());
								return;
							}
						}
					}
					if(candidate != null) {
						secondLine = candidate.getId();
					}
				}
			}
		
			/* check if all the lines are correct */			
			if(secondLine == null || secondLine.length() == 0) {
				plugin.message(player, "setup-noRegion");
				return;
			} else if(regionManager.getRegion(secondLine) == null) {
				plugin.message(player, "setup-wrongRegion");
				return;
			} else if(plugin.getFileManager().getRent(secondLine) != null) {
				plugin.message(player, "setup-alreadyRentSign");
				return;
			} else if(thirdLine == null || thirdLine.length() == 0) {
				plugin.message(player, "setup-noDuration");
				return;
			} else if(!plugin.checkTimeFormat(thirdLine)) {
				plugin.message(player, "setup-wrongDuration");
				return;
			} else if(fourthLine == null || fourthLine.length() == 0) {
				plugin.message(player, "setup-noPrice");
				return;
			} else {
				/* Check the fourth line */
				try {
					Double.parseDouble(fourthLine);
				} catch (NumberFormatException e) {
					plugin.message(player, "setup-wrongPrice");
					return;
				}
				
				/* Add rent to the FileManager */
				HashMap<String,String> rent = new HashMap<String,String>();
				rent.put(AreaShop.keyWorld, event.getBlock().getWorld().getName());
				rent.put(AreaShop.keyX, String.valueOf(event.getBlock().getX()));
				rent.put(AreaShop.keyY, String.valueOf(event.getBlock().getY()));
				rent.put(AreaShop.keyZ, String.valueOf(event.getBlock().getZ()));
				rent.put(AreaShop.keyDuration, thirdLine);
				rent.put(AreaShop.keyPrice, fourthLine);
				rent.put(AreaShop.keyName, regionManager.getRegion(secondLine).getId());
				rent.put(AreaShop.keyRestore, "general");
				rent.put(AreaShop.keySchemProfile, "default");
				
				plugin.getFileManager().addRent(secondLine, rent);
				plugin.getFileManager().handleSchematicEvent(secondLine, true, RegionEventType.CREATED);
				event.setLine(0, plugin.fixColors(plugin.config().getString("signRentable")));
				event.setLine(1, regionManager.getRegion(secondLine).getId());
				event.setLine(2, thirdLine);
				event.setLine(3, plugin.formatCurrency(fourthLine));
				
				/* Set the flags for the region */
				plugin.getFileManager().setRegionFlags(secondLine, plugin.config().getConfigurationSection("flagsForRent"), true);

				plugin.message(player, "setup-rentSuccess", regionManager.getRegion(secondLine).getId());
			}
		} else if (event.getLine(0).contains(plugin.config().getString("buySign"))) {
			/* Check for permission */
			if(!player.hasPermission("areashop.createbuy")) {
				plugin.message(player, "setup-noPermissionBuy");				
				return;
			}
			
			/* Get the other lines */
			String secondLine = event.getLine(1);
			String thirdLine = event.getLine(2);
			
			/* Get the regionManager for accessing regions */
			RegionManager regionManager = plugin.getWorldGuard().getRegionManager(event.getPlayer().getWorld());
		
			/* If the secondLine does not contain a name try to find the region by location */
			if(secondLine == null || secondLine.length() == 0) {
				ApplicableRegionSet regions = regionManager.getApplicableRegions(event.getBlock().getLocation());
				if(regions != null) {
					boolean first = true;
					ProtectedRegion candidate = null;
					for(ProtectedRegion pr : regions) {
						if(first) {
							candidate = pr;
							first = false;
						} else {
							if(pr.getPriority() > candidate.getPriority()) {
								candidate = pr;
							} else if(pr.getParent() != null && pr.getParent().equals(candidate)) {
								candidate = pr;
							} else if(pr.getPriority() == candidate.getPriority()) {
								plugin.message(player, "setup-couldNotDetect", candidate.getId(), pr.getId());
								return;
							}
						}
					}
					if(candidate != null) {
						secondLine = candidate.getId();
					}
				}
			}
			
			/* Check if all the lines are correct */			
			if(secondLine == null || secondLine.length() == 0) {
				plugin.message(player, "setup-noRegion");
				return;
			} else if(regionManager.getRegion(secondLine) == null) {
				plugin.message(player, "setup-wrongRegion");
				return;
			} else if(plugin.getFileManager().getBuy(secondLine) != null) {
				plugin.message(player, "setup-alreadyBuySign");
				return;
			} else if(thirdLine == null || thirdLine.length() == 0) {
				plugin.message(player, "setup-noPrice");
				return;
			} else {
				/* Check the fourth line */
				try {
					Double.parseDouble(thirdLine);
				} catch (NumberFormatException e) {
					plugin.message(player, "setup-wrongPrice");
					return;
				}
				
				/* Add buy to the FileManager */
				HashMap<String,String> buy = new HashMap<String,String>();
				buy.put(AreaShop.keyWorld, event.getBlock().getWorld().getName());
				buy.put(AreaShop.keyX, String.valueOf(event.getBlock().getX()));
				buy.put(AreaShop.keyY, String.valueOf(event.getBlock().getY()));
				buy.put(AreaShop.keyZ, String.valueOf(event.getBlock().getZ()));
				buy.put(AreaShop.keyPrice, thirdLine);
				buy.put(AreaShop.keyName, regionManager.getRegion(secondLine).getId());
				buy.put(AreaShop.keyRestore, "general");
				buy.put(AreaShop.keySchemProfile, "default");
				
				plugin.getFileManager().addBuy(secondLine, buy);
				plugin.getFileManager().handleSchematicEvent(secondLine, false, RegionEventType.CREATED);
				event.setLine(0, plugin.fixColors(plugin.config().getString("signBuyable")));
				event.setLine(1, regionManager.getRegion(secondLine).getId());
				event.setLine(2, plugin.formatCurrency(thirdLine));
				
				/* Set the flags for the region */
				plugin.getFileManager().setRegionFlags(secondLine, plugin.config().getConfigurationSection("flagsForSale"), false);
				
				plugin.message(player, "setup-buySuccess", regionManager.getRegion(secondLine).getId());
			}
		}
	}
}
































