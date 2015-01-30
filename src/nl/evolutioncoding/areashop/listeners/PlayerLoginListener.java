package nl.evolutioncoding.areashop.listeners;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.regions.RentRegion;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Checks for placement of signs for this plugin
 * @author NLThijs48
 */
public final class PlayerLoginListener implements Listener {
	AreaShop plugin;
	
	/**
	 * Constructor
	 * @param plugin The AreaShop plugin
	 */
	public PlayerLoginListener(AreaShop plugin) {
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
		// Notify for rents that almost run out
		for(RentRegion region : plugin.getFileManager().getRents()) {
			if(region.isRenter(player)) {
				String warningSetting = region.getStringSetting("rent.warningOnLoginTime");
				if(warningSetting == null || warningSetting.isEmpty()) {
					continue;
				}
				long warningTime = region.durationStringToLong(warningSetting);
				if(region.getTimeLeft() < warningTime) {
					// Send the warning message later to let it appear after general MOTD messages
					final RentRegion finalRegion = region;
			        new BukkitRunnable() {
						@Override
						public void run() {
							AreaShop.getInstance().message(player, "rent-expireWarning", finalRegion);
						}
			        }.runTaskLater(plugin, 2);			        
				}				
			}
		}
		// Notify admins for plugin updates
		if(plugin.updateAvailable() && player.hasPermission("areashop.notifyupdate")) {
	        new BukkitRunnable() {
				@Override
				public void run() {
					AreaShop.getInstance().message(player, "update-playerNotify", AreaShop.getInstance().getDescription().getVersion(), AreaShop.getInstance().getUpdater().getLatestName());
				}
	        }.runTaskLater(plugin, 20);			
		}
	}
}
































