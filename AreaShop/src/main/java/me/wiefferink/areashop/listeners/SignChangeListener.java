package me.wiefferink.areashop.listeners;

import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.Utils;
import me.wiefferink.areashop.managers.FileManager;
import me.wiefferink.areashop.regions.BuyRegion;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.regions.RentRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.material.Sign;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Set;

/**
 * Checks for placement of signs for this plugin
 * @author NLThijs48
 */
public final class SignChangeListener implements Listener {
	private AreaShop plugin;
	
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
			if(!player.hasPermission("areashop.createrent") && !player.hasPermission("areashop.createrent.member") && !player.hasPermission("areashop.createrent.owner")) {
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
				Set<ProtectedRegion> regions = plugin.getWorldGuardHandler().getApplicableRegionsSet(event.getBlock().getLocation());
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
							} else if(pr.getPriority() < candidate.getPriority()) {
								// Already got the correct one
							} else if(pr.getParent() != null && pr.getParent().equals(candidate)) {
								candidate = pr;
							} else if(candidate.getParent() != null && candidate.getParent().equals(pr)) {
								// Already got the correct one
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
			}
			ProtectedRegion region = regionManager.getRegion(secondLine);
			if(region == null) {
				plugin.message(player, "cmd-noRegion", secondLine);
				return;
			}

			FileManager.AddResult addResult = plugin.getFileManager().checkRegionAdd(player, regionManager.getRegion(secondLine), GeneralRegion.RegionType.RENT);
			if(addResult == FileManager.AddResult.BLACKLISTED) {
				plugin.message(player, "setup-blacklisted", secondLine);
			} else if(addResult == FileManager.AddResult.ALREADYADDED) {
				plugin.message(player, "setup-alreadyRentSign");
			} else if(addResult == FileManager.AddResult.NOPERMISSION) {
				plugin.message(player, "setup-noPermission", secondLine);
			} else if(thirdLine != null && thirdLine.length() != 0 && !Utils.checkTimeFormat(thirdLine)) {
				plugin.message(player, "setup-wrongDuration");
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
				boolean isMember = plugin.getWorldGuardHandler().containsMember(rent.getRegion(), player.getUniqueId());
				boolean isOwner = plugin.getWorldGuardHandler().containsOwner(rent.getRegion(), player.getUniqueId());
				boolean landlord = (!player.hasPermission("areashop.createrent")
						&& ((player.hasPermission("areashop.createrent.owner") && isOwner)
						|| (player.hasPermission("areashop.createrent.member") && isMember)));					

				if(landlord) {
					rent.setLandlord(player.getUniqueId(), player.getName());
				}
				if(priceSet) {
					rent.setPrice(price);
				}
				if(durationSet) {
					rent.setDuration(thirdLine);
				}
				Sign sign = (Sign)event.getBlock().getState().getData();
				rent.getSignsFeature().addSign(event.getBlock().getLocation(), event.getBlock().getType(), sign.getFacing(), null);

				// Run commands
				rent.runEventCommands(GeneralRegion.RegionEvent.CREATED, true);
				
				plugin.getFileManager().addRent(rent);
				rent.handleSchematicEvent(GeneralRegion.RegionEvent.CREATED);
				plugin.message(player, "setup-rentSuccess", rent);
				// Update the region after the event has written its lines
				new BukkitRunnable() {
					@Override
					public void run() {
						rent.update();
					}
				}.runTaskLater(plugin, 1);
				// Run commands
				rent.runEventCommands(GeneralRegion.RegionEvent.CREATED, false);
			}
		} else if (event.getLine(0).contains(plugin.getConfig().getString("signTags.buy"))) {
			// Check for permission
			if(!player.hasPermission("areashop.createbuy") && !player.hasPermission("areashop.createbuy.member") && !player.hasPermission("areashop.createbuy.owner")) {
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
				Set<ProtectedRegion> regions = plugin.getWorldGuardHandler().getApplicableRegionsSet(event.getBlock().getLocation());
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
							} else if(pr.getPriority() < candidate.getPriority()) {
								// Already got the correct one
							} else if(pr.getParent() != null && pr.getParent().equals(candidate)) {
								candidate = pr;
							} else if(candidate.getParent() != null && candidate.getParent().equals(pr)) {
								// Already got the correct one
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

			boolean priceSet = thirdLine != null && thirdLine.length() != 0;
			// Check if all the lines are correct			
			if(secondLine == null || secondLine.length() == 0) {
				plugin.message(player, "setup-noRegion");
				return;
			}
			ProtectedRegion region = regionManager.getRegion(secondLine);
			if(region == null) {
				plugin.message(player, "cmd-noRegion", secondLine);
				return;
			}
			FileManager.AddResult addResult = plugin.getFileManager().checkRegionAdd(player, region, GeneralRegion.RegionType.BUY);
			if(addResult == FileManager.AddResult.BLACKLISTED) {
				plugin.message(player, "setup-blacklisted", secondLine);
			} else if(addResult == FileManager.AddResult.ALREADYADDED) {
				plugin.message(player, "setup-alreadyRentSign");
			} else if(addResult == FileManager.AddResult.NOPERMISSION) {
				plugin.message(player, "setup-noPermission", secondLine);
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
				boolean isMember = plugin.getWorldGuardHandler().containsMember(buy.getRegion(), player.getUniqueId());
				boolean isOwner = plugin.getWorldGuardHandler().containsOwner(buy.getRegion(), player.getUniqueId());
				boolean landlord = (!player.hasPermission("areashop.createbuy")
						&& ((player.hasPermission("areashop.createbuy.owner") && isOwner)
						|| (player.hasPermission("areashop.createbuy.member") && isMember)));					

				if(landlord) {
					buy.setLandlord(player.getUniqueId(), player.getName());
				}
				if(priceSet) {
					buy.setPrice(price);
				}
				Sign sign = (Sign)event.getBlock().getState().getData();
				buy.getSignsFeature().addSign(event.getBlock().getLocation(), event.getBlock().getType(), sign.getFacing(), null);
				// Run commands
				buy.runEventCommands(GeneralRegion.RegionEvent.CREATED, true);
				
				plugin.getFileManager().addBuy(buy);
				buy.handleSchematicEvent(GeneralRegion.RegionEvent.CREATED);
				plugin.message(player, "setup-buySuccess", buy);
				// Update the region after the event has written its lines
				new BukkitRunnable() {
					@Override
					public void run() {
						buy.update();
					}
				}.runTaskLater(plugin, 1);

				// Run commands
				buy.runEventCommands(GeneralRegion.RegionEvent.CREATED, false);
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

			GeneralRegion region;
			if(secondLine != null && secondLine.length() != 0) {
				// Get region by secondLine of the sign
				region = plugin.getFileManager().getRegion(secondLine);
				if(region == null) {
					plugin.message(player, "cmd-noRegion", secondLine);
					return;
				}			
			} else {
				// Get region by sign position
				List<GeneralRegion> regions = Utils.getASRegionsInSelection(new CuboidSelection(event.getBlock().getWorld(), event.getBlock().getLocation(), event.getBlock().getLocation()));
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
				region.getSignsFeature().addSign(event.getBlock().getLocation(), event.getBlock().getType(), sign.getFacing(), null);
				plugin.message(player, "addsign-success", region);
			} else {
				region.getSignsFeature().addSign(event.getBlock().getLocation(), event.getBlock().getType(), sign.getFacing(), thirdLine);
				plugin.message(player, "addsign-successProfile", thirdLine, region);
			}

			// Update the region later because this event will do it first
			final GeneralRegion regionUpdate = region;
			new BukkitRunnable() {
				@Override
				public void run() {
					regionUpdate.update();
				}
			}.runTaskLater(plugin, 1);
		}
	}
}
































