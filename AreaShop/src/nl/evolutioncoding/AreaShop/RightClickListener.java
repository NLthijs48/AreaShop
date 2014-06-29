package nl.evolutioncoding.AreaShop;

import nl.evolutioncoding.AreaShop.regions.BuyRegion;
import nl.evolutioncoding.AreaShop.regions.RentRegion;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class RightClickListener implements Listener {
	AreaShop plugin;
	
	/**
	 * Constructor
	 * @param plugin The AreaShop plugin
	 */
	public RightClickListener(AreaShop plugin) {
		this.plugin = plugin;
	}

	/**
	 * Called when a player interacts
	 * @param event The event
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onRightClick(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		/* Check for clicking a sign and rightclicking */
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN)) {
			/* Check if the rent sign is really the same as a saved rent */
			String regionName = ((Sign)(event.getClickedBlock().getState())).getLine(1);
			RentRegion rent = plugin.getFileManager().getRent(regionName);
			BuyRegion buy = plugin.getFileManager().getBuy(regionName);			
			if(rent != null && block.getLocation().equals(rent.getSignLocation())) {				
				rent.rent(event.getPlayer());
				/* Cancel placing a block */
				event.setCancelled(true);					
			} else if(buy != null && block.getLocation().equals(buy.getSignLocation())) {				
				buy.buy(event.getPlayer());
				/* Cancel placing a block */
				event.setCancelled(true);					
			}
		}
	}
}





























