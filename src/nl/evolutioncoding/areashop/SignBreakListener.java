package nl.evolutioncoding.areashop;

import nl.evolutioncoding.areashop.regions.BuyRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import nl.evolutioncoding.areashop.regions.RentRegion;

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
 * @author NLThijs48
 */
public final class SignBreakListener implements Listener {
	AreaShop plugin;
	
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
		Block block = event.getBlock();
		/* Check if it is a sign */
		if(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
			/* Check if the rent sign is really the same as a saved rent */
			GeneralRegion region = plugin.getFileManager().getRegionBySignLocation(block.getLocation());
			if(region == null) {
				return;
			}
			if(region.isRentRegion()) {
				/* Remove the rent if the player has permission */
				if(event.getPlayer().hasPermission("areashop.destroyrent")) {
					plugin.getFileManager().removeRent((RentRegion)region, true);
					plugin.message(event.getPlayer(), "destroy-successRent", region.getName());
				} else { /* Cancel the breaking of the sign */
					event.setCancelled(true);
					plugin.message(event.getPlayer(), "destroy-noPermissionRent");
				}
			} else if(region.isBuyRegion()) {
				/* Remove the buy if the player has permission */
				if(event.getPlayer().hasPermission("areashop.destroybuy")) {
					plugin.getFileManager().removeBuy((BuyRegion)region, true);
					plugin.message(event.getPlayer(), "destroy-successBuy", region.getName());
				} else { /* Cancel the breaking of the sign */
					event.setCancelled(true);
					plugin.message(event.getPlayer(), "destroy-noPermissionBuy");
				}
			}
		}
	}
	
	/**
	 * Called when the physics of a block change
	 * @param event The event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
    public void onIndirectSignBreak(BlockPhysicsEvent event){
        Block block = event.getBlock();
        if(event.isCancelled()) {
        	return;
        }
        if(block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN){
            Sign sign = (Sign)block.getState();
            Block attachedTo = block.getRelative(((org.bukkit.material.Sign)sign.getData()).getAttachedFace());
            if(attachedTo.getType() == Material.AIR){
				/* Check if the rent sign is really the same as a saved rent */
				GeneralRegion region = plugin.getFileManager().getRegionBySignLocation(block.getLocation());
				if(region == null) {
					return;
				}
				region.removeSign(block.getLocation());
				plugin.getLogger().info("A sign of region " + region.getName() + " has been removed by indirectly breaking it (block below/behind is destroyed)");
            }
        }
    }
}



















































