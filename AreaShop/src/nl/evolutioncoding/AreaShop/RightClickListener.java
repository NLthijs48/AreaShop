package nl.evolutioncoding.AreaShop;

import java.util.List;

import nl.evolutioncoding.AreaShop.regions.BuyRegion;
import nl.evolutioncoding.AreaShop.regions.GeneralRegion;
import nl.evolutioncoding.AreaShop.regions.RentRegion;

import org.bukkit.Material;
import org.bukkit.block.Block;
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
			GeneralRegion result = null;
			List<GeneralRegion> regions = plugin.getFileManager().getRegions();
			for(int i=0; i<regions.size(); i++) {
				if(regions.get(i).getSignLocation().equals(block.getLocation())) {
					result = regions.get(i);
				}				
			}
			if(result != null && result.isRentRegion()) {	
				((RentRegion)result).rent(event.getPlayer());
				/* Cancel placing a block */
				event.setCancelled(true);					
			} else if(result != null && result.isBuyRegion()) {		
				((BuyRegion)result).buy(event.getPlayer());
				/* Cancel placing a block */
				event.setCancelled(true);					
			}
		}
	}
}





























