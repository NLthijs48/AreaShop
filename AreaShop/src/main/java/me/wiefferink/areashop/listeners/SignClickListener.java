package me.wiefferink.areashop.listeners;

import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.features.SignsFeature;
import me.wiefferink.areashop.regions.GeneralRegion.ClickType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignClickListener implements Listener {
	private AreaShop plugin;
	
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
		if(event.isCancelled()) {
			return;
		}
		Block block = event.getClickedBlock();
		// Check for clicking a sign and rightclicking
		if((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) 
				&& (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN)) {
			// Check if the rent sign is really the same as a saved rent
			SignsFeature.RegionSign regionSign = SignsFeature.getSignByLocation(block.getLocation());
			if(regionSign == null) {
				return;
			}
			Player player = event.getPlayer();
			if(plugin.getSignlinkerManager().isInSignLinkMode(player)) {
				return;
			}
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
			boolean ran = regionSign.runSignCommands(player, clickType);
			// Only cancel event if at least one command has been executed
			event.setCancelled(ran);
		}
	}
}





























