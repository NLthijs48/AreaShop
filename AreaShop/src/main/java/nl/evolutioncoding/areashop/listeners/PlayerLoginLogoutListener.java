package nl.evolutioncoding.areashop.listeners;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.Utils;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import nl.evolutioncoding.areashop.regions.RentRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks for placement of signs for this plugin
 * @author NLThijs48
 */
public final class PlayerLoginLogoutListener implements Listener {
	private AreaShop plugin;
	
	/**
	 * Constructor
	 * @param plugin The AreaShop plugin
	 */
	public PlayerLoginLogoutListener(AreaShop plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Called when a sign is changed
	 * @param event The event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogin(PlayerLoginEvent event) {
		if(event.getResult() != Result.ALLOWED) {
			return;
		}
		final Player player = event.getPlayer();
		// Notify admins for plugin updates
		if(plugin.updateAvailable() && player.hasPermission("areashop.notifyupdate")) {
			AreaShop.getInstance().message(player, "update-playerNotify", AreaShop.getInstance().getDescription().getVersion(), AreaShop.getInstance().getUpdater().getLatestName());	
		}
		// Schedule task to check for notifications, prevents a lag spike at login
        new BukkitRunnable() {
			@Override
			public void run() {
				// Delay until all regions are loaded
				if(!plugin.isReady()) {
					return;
				}	
				if(!player.isOnline()) {
					this.cancel();
					return;
				}
				// Notify for rents that almost run out
				for(RentRegion region : plugin.getFileManager().getRents()) {
					if(region.isRenter(player)) {
						String warningSetting = region.getStringSetting("rent.warningOnLoginTime");
						if(warningSetting == null || warningSetting.isEmpty()) {
							continue;
						}
						long warningTime = Utils.durationStringToLong(warningSetting);
						if(region.getTimeLeft() < warningTime) {
							// Send the warning message later to let it appear after general MOTD messages
							AreaShop.getInstance().message(player, "rent-expireWarning", region);		        
						}				
					}
				}
				this.cancel();
			}
        }.runTaskTimer(plugin, 25, 25);	
		// Check if the player has regions that use an old name of him and update them
		final List<GeneralRegion> regions = new ArrayList<>(plugin.getFileManager().getRegions());
		new BukkitRunnable() {
			private int current = 0;
			
			@Override
			public void run() {
				// Delay until all regions are loaded
				if(!plugin.isReady()) {
					return;
				}
				// Check all regions
				for(int i=0; i<plugin.getConfig().getInt("nameupdate.regionsPerTick"); i++) {
					if(current < regions.size()) {
						GeneralRegion region = regions.get(current);
						if(region.isOwner(player)) {
							if(region.isBuyRegion()) {
								if(!player.getName().equals(region.getStringSetting("buy.buyerName"))) {
									region.setSetting("buy.buyerName", player.getName());
									region.update();
								}
							} else if(region.isRentRegion() && !player.getName().equals(region.getStringSetting("rent.renterName"))) {
								region.setSetting("rent.renterName", player.getName());
								region.update();
							}
						}
						current++;
					}
				}
				if(current >= regions.size()) {
					this.cancel();
				}
			}
		}.runTaskTimer(plugin, 22, 1); // Wait a bit before starting to prevent a lot of stress on the server when a player joins (a lot of plugins already do stuff then)
	}
	
	// Active time updates
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogout(PlayerQuitEvent event) {
		updateLastActive(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerKick(PlayerKickEvent event) {
		updateLastActive(event.getPlayer());
	}
	
	/**
	 * Update the last active time for all regions the player is owner off
	 * @param player The player to update the active times for
	 */
	private void updateLastActive(Player player) {
		for(GeneralRegion region : plugin.getFileManager().getRegions()) {
			if(region.isOwner(player)) {
				region.updateLastActiveTime();
			}
		}
	}
}
































