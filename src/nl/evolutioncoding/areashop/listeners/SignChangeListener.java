package nl.evolutioncoding.areashop.listeners;

import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.regions.BuyRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import nl.evolutioncoding.areashop.regions.RentRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion.RegionEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.material.Sign;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
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
	@EventHandler(priority = EventPriority.MONITOR)
	public void onSignChange(SignChangeEvent event) {
		if(event.isCancelled()) {
			return;
		}
		Player player = event.getPlayer();
		if(!plugin.isReady()) {
			plugin.message(player, "general-notReady");
			return;
		}
		
		// Check if the sign is meant for this plugin
		if(event.getLine(0).contains(plugin.getConfig().getString("signTags.rent"))) {
			if(!player.hasPermission("areashop.createrent")) {
				plugin.message(player, "setup-noPermissionRent");				
				return;
			}
			
			// Get the other lines
			String secondLine = event.getLine(1);
			String thirdLine = event.getLine(2);
			String fourthLine = event.getLine(3);
			
			// Get the regionManager for accessing regions
			RegionManager regionManager = plugin.getWorldGuard().getRegionManager(event.getPlayer().getWorld());
			
			// If the secondLine does not contain a name try to find the region by location
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
		
			boolean priceSet = fourthLine != null && fourthLine.length() != 0;
			boolean durationSet = thirdLine != null && thirdLine.length() != 0;
			// check if all the lines are correct			
			if(secondLine == null || secondLine.length() == 0) {
				plugin.message(player, "setup-noRegion");
				return;
			} else if(regionManager.getRegion(secondLine) == null) {
				plugin.message(player, "setup-wrongRegion");
				return;
			} else if(plugin.getFileManager().isBlacklisted(secondLine)) {
				plugin.message(player, "setup-blacklisted", secondLine);
				return;
			} else if(plugin.getFileManager().getRent(secondLine) != null) {
				plugin.message(player, "setup-alreadyRentSign");
				return;
			} else if(thirdLine != null && thirdLine.length() != 0 && !plugin.checkTimeFormat(thirdLine)) {
				plugin.message(player, "setup-wrongDuration");
				return;
			} else {
				double price = 0.0;
				if(priceSet) {
					// Check the fourth line
					try {
						price = Double.parseDouble(fourthLine);
					} catch (NumberFormatException e) {
						plugin.message(player, "setup-wrongPrice");
						return;
					}
				}
				
				// Add rent to the FileManager
				final RentRegion rent = new RentRegion(plugin, secondLine, event.getPlayer().getWorld());
				if(priceSet) {
					rent.setPrice(price);
				}
				if(durationSet) {
					rent.setDuration(thirdLine);
				}
				Sign sign = (Sign)event.getBlock().getState().getData();
				rent.addSign(event.getBlock().getLocation(), event.getBlock().getType(), sign.getFacing(), null);

				// Run commands
				rent.runEventCommands(RegionEvent.CREATED, true);				
				
				plugin.getFileManager().addRent(rent);
				rent.handleSchematicEvent(RegionEvent.CREATED);
				// Update the sign later because this event will do it first
				new BukkitRunnable() {
					@Override
					public void run() {
						rent.updateSigns();						
					}
				}.runTaskLater(plugin, 1);
				
				// Set the flags for the region
				rent.updateRegionFlags();
				plugin.message(player, "setup-rentSuccess", rent.getName());
				// Run commands
				rent.runEventCommands(RegionEvent.CREATED, false);
				rent.saveRequired();
			}
		} else if (event.getLine(0).contains(plugin.getConfig().getString("signTags.buy"))) {
			// Check for permission
			if(!player.hasPermission("areashop.createbuy")) {
				plugin.message(player, "setup-noPermissionBuy");				
				return;
			}
			
			// Get the other lines
			String secondLine = event.getLine(1);
			String thirdLine = event.getLine(2);
			
			// Get the regionManager for accessing regions
			RegionManager regionManager = plugin.getWorldGuard().getRegionManager(event.getPlayer().getWorld());
		
			// If the secondLine does not contain a name try to find the region by location
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
			
			boolean priceSet = thirdLine != null && thirdLine.length() != 0;
			// Check if all the lines are correct			
			if(secondLine == null || secondLine.length() == 0) {
				plugin.message(player, "setup-noRegion");
				return;
			} else if(regionManager.getRegion(secondLine) == null) {
				plugin.message(player, "setup-wrongRegion");
				return;
			} else if(plugin.getFileManager().isBlacklisted(secondLine)) {
				plugin.message(player, "setup-blacklisted", secondLine);
				return;
			} else if(plugin.getFileManager().getBuy(secondLine) != null) {
				plugin.message(player, "setup-alreadyBuySign");
				return;
			} else {
				double price = 0.0;
				if(priceSet) {
					// Check the fourth line
					try {
						price = Double.parseDouble(thirdLine);
					} catch (NumberFormatException e) {
						plugin.message(player, "setup-wrongPrice");
						return;
					}
				}
				
				// Add buy to the FileManager
				final BuyRegion buy = new BuyRegion(plugin, secondLine, event.getPlayer().getWorld());
				if(priceSet) {
					buy.setPrice(price);
				}
				Sign sign = (Sign)event.getBlock().getState().getData();
				buy.addSign(event.getBlock().getLocation(), event.getBlock().getType(), sign.getFacing(), null);
				// Run commands
				buy.runEventCommands(RegionEvent.CREATED, true);
				
				plugin.getFileManager().addBuy(buy);
				buy.handleSchematicEvent(RegionEvent.CREATED);
				// Update the sign later because this event will do it first
				new BukkitRunnable() {
					@Override
					public void run() {
						buy.updateSigns();						
					}
				}.runTaskLater(plugin, 1);
				
				// Set the flags for the region
				buy.updateRegionFlags();				
				plugin.message(player, "setup-buySuccess", regionManager.getRegion(secondLine).getId());
				
				// Run commands
				buy.runEventCommands(RegionEvent.CREATED, false);
				buy.saveRequired();
			}
		} else if(event.getLine(0).contains(plugin.getConfig().getString("signTags.add"))) {
			// Check for permission
			if(!player.hasPermission("areashop.addsign")) {
				plugin.message(player, "addsign-noPermission");				
				return;
			}
			
			// Get the other lines
			String secondLine = event.getLine(1);
			String thirdLine = event.getLine(2);
			
			GeneralRegion region = null;
			if(secondLine != null && secondLine.length() != 0) {
				// Get region by secondLine of the sign
				region = plugin.getFileManager().getRegion(secondLine);
				if(region == null) {
					plugin.message(player, "addsign-noRegion", secondLine);
					return;
				}			
			} else {
				// Get region by sign position
				List<GeneralRegion> regions = plugin.getFileManager().getASRegionsInSelection(new CuboidSelection(event.getBlock().getWorld(), event.getBlock().getLocation(), event.getBlock().getLocation()));
				if(regions.isEmpty()) {
					plugin.message(player, "addsign-noRegions");
					return;
				} else if(regions.size() > 1) {
					plugin.message(player, "addsign-couldNotDetectSign", regions.get(0).getName(), regions.get(1).getName());
					return;
				}
				region = regions.get(0);
			}
			Sign sign = (Sign)event.getBlock().getState().getData();
			if(thirdLine == null || thirdLine.length() == 0) {
				region.addSign(event.getBlock().getLocation(), event.getBlock().getType(), sign.getFacing(), null);
				plugin.message(player, "addsign-success", region);
			} else {
				region.addSign(event.getBlock().getLocation(), event.getBlock().getType(), sign.getFacing(), thirdLine);
				plugin.message(player, "addsign-successProfile", region, thirdLine);
			}
			region.saveRequired();
			
			// Update the sign later because this event will do it first
			final GeneralRegion regionUpdate = region;
			new BukkitRunnable() {
				@Override
				public void run() {
					regionUpdate.updateSigns();						
				}
			}.runTaskLater(plugin, 1);
			
		}
	}
}
































