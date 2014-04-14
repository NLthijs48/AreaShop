package nl.evolutioncoding.AreaShop;

import java.util.HashMap;

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
			HashMap<String,String> rent = plugin.getFileManager().getRent(regionName);
			HashMap<String,String> buy = plugin.getFileManager().getBuy(regionName);
			
			if(rent != null && block.getWorld().getName().equals(rent.get(plugin.keyWorld))	
					&& rent.get(plugin.keyX).equals(String.valueOf(block.getX()))
					&& rent.get(plugin.keyY).equals(String.valueOf(block.getY()))
					&& rent.get(plugin.keyZ).equals(String.valueOf(block.getZ())) ) {
				
				plugin.getFileManager().rent(event.getPlayer(), regionName);
				/* Cancel placing a block */
				event.setCancelled(true);	
				
			} else if(buy != null && block.getWorld().getName().equals(buy.get(plugin.keyWorld))
					&& buy.get(plugin.keyX).equals(String.valueOf(block.getX()))
					&& buy.get(plugin.keyY).equals(String.valueOf(block.getY()))
					&& buy.get(plugin.keyZ).equals(String.valueOf(block.getZ())) ) {
				
				plugin.getFileManager().buy(event.getPlayer(), regionName);
				/* Cancel placing a block */
				event.setCancelled(true);	
				
			}
		}
	}
}





























