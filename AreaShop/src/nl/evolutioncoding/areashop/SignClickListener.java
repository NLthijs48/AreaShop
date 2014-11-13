package nl.evolutioncoding.areashop;

import nl.evolutioncoding.areashop.regions.GeneralRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion.ClickType;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignClickListener implements Listener {
	AreaShop plugin;
	
	/**
	 * Constructor
	 * @param plugin The AreaShop plugin
	 */
	public SignClickListener(AreaShop plugin) {
		this.plugin = plugin;
	}

	/**
	 * Called when a player interacts
	 * @param event The event
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onSignClick(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		/* Check for clicking a sign and rightclicking */
		if(		   (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) 
				&& (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN)) {
			/* Check if the rent sign is really the same as a saved rent */
			GeneralRegion result = plugin.getFileManager().getRegionBySignLocation(block.getLocation());
			if(result == null) {
				return;
			}
			String signName = result.getSignName(block.getLocation());
			Player player = event.getPlayer();
			// Get the clicktype
			ClickType clickType = null;
			if(player.isSneaking() && event.getAction() == Action.LEFT_CLICK_BLOCK) {
				clickType = ClickType.SHIFTLEFTCLICK;
			} else if(!player.isSneaking() && event.getAction() == Action.LEFT_CLICK_BLOCK) {
				clickType = ClickType.LEFTCLICK;
			} else if(player.isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				clickType = ClickType.SHIFTRIGHTCLICK;
			} else if(!player.isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				clickType = ClickType.RIGHTCLICK;
			}
			// Run the commands
			boolean runned = result.runSignCommands(signName, player, clickType);
			// Only cancel event if at least one command has been executed
			event.setCancelled(runned);
		}
	}
}





























