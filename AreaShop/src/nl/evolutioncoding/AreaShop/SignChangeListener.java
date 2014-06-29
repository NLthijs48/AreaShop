package nl.evolutioncoding.AreaShop;

import nl.evolutioncoding.AreaShop.regions.BuyRegion;
import nl.evolutioncoding.AreaShop.regions.BuyRegion.BuyEvent;
import nl.evolutioncoding.AreaShop.regions.RentRegion;
import nl.evolutioncoding.AreaShop.regions.RentRegion.RentEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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
				RentRegion rent = new RentRegion(plugin, secondLine, event.getBlock().getLocation(), Double.parseDouble(fourthLine), thirdLine);
				plugin.getFileManager().addRent(secondLine, rent);
				rent.handleSchematicEvent(RentEvent.CREATED);
				String[] signLines = rent.getSignLines();
				for(int i=0; i<signLines.length; i++) {
					event.setLine(i, signLines[i]);
				}
				
				/* Set the flags for the region */
				rent.updateRegionFlags();
				plugin.message(player, "setup-rentSuccess", rent.getName());
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
				BuyRegion buy = new BuyRegion(plugin, secondLine, event.getBlock().getLocation(), Double.parseDouble(thirdLine));
				
				plugin.getFileManager().addBuy(secondLine, buy);
				buy.handleSchematicEvent(BuyEvent.CREATED);
				String[] signLines = buy.getSignLines();
				for(int i=0; i<signLines.length; i++) {
					event.setLine(i, signLines[i]);
				}
				
				/* Set the flags for the region */
				buy.updateRegionFlags();
				
				plugin.message(player, "setup-buySuccess", regionManager.getRegion(secondLine).getId());
			}
		}
	}
}
































