package nl.evolutioncoding.areashop;

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
		Player player = event.getPlayer();
		
		for(RentRegion region : plugin.getFileManager().getRents()) {
			if(region.isRenter(player)) {
				String warningSetting = region.getStringSetting("rent.warningOnLoginTime");
				if(warningSetting == null || warningSetting.isEmpty()) {
					continue;
				}
				long warningTime = region.durationStringToLong(warningSetting);
				if(region.getTimeLeft() < warningTime) {
					// Send the warning message later to let it appear after general MOTD messages
					final Player finalPlayer = player;
					final AreaShop finalPlugin = plugin;
					final RentRegion finalRegion = region;
			        new BukkitRunnable() {
						@Override
						public void run() {
							finalPlugin.message(finalPlayer, "rent-expireWarning", finalRegion);
						}
			        }.runTaskLater(plugin, 2);			        
				}				
			}
		}
	}
}
































