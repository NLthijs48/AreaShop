package me.wiefferink.areashop.listeners;

import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.features.SignsFeature;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;

/**
 * Checks for placement of signs for this plugin
 */
public final class SignBreakListener implements Listener {
	private AreaShop plugin;
	
	/**
	 * Constructor
	 * @param plugin The AreaShop plugin
	 */
	public SignBreakListener(AreaShop plugin) {
		this.plugin = plugin;
	}
	
	
	/**
	 * Called when a block is broken
	 * @param event The event
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onSignBreak(BlockBreakEvent event) {
		if(event.isCancelled()) {
			return;
		}
		Block block = event.getBlock();
		// Check if it is a sign
		if(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
			// Check if the rent sign is really the same as a saved rent
			SignsFeature.RegionSign regionSign = SignsFeature.getSignByLocation(block.getLocation());
			if(regionSign == null) {
				return;
			}
			// Remove the sign of the rental region if the player has permission
			if(event.getPlayer().hasPermission("areashop.delsign")) {
				regionSign.remove();
				plugin.message(event.getPlayer(), "delsign-success", regionSign.getRegion());
			} else { // Cancel the breaking of the sign
				event.setCancelled(true);
				plugin.message(event.getPlayer(), "delsign-noPermission", regionSign.getRegion());
			}
		}
	}
	
	/**
	 * Called when the physics of a block change
	 * @param event The event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
    public void onIndirectSignBreak(BlockPhysicsEvent event){
        Block block = event.getBlock();
        if(event.isCancelled()) {
        	return;
        }
        if(block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN){
            Sign sign = (Sign)block.getState();
            Block attachedTo = block.getRelative(((org.bukkit.material.Sign)sign.getData()).getAttachedFace());
            if(attachedTo.getType() == Material.AIR){
				// Check if the rent sign is really the same as a saved rent
				SignsFeature.RegionSign regionSign = SignsFeature.getSignByLocation(block.getLocation());
				if(regionSign != null) {
					event.setCancelled(true); // Cancel the sign breaking, will create a floating sign but at least it is not disconnected/gone
				}
            }
        }
    }
}



















































