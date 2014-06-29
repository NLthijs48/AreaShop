package nl.evolutioncoding.AreaShop;

import nl.evolutioncoding.AreaShop.regions.BuyRegion;
import nl.evolutioncoding.AreaShop.regions.RentRegion;

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
			Sign sign = (Sign)(block.getState());
			/* Check if the rent sign is really the same as a saved rent */
			RentRegion rent = plugin.getFileManager().getRent(sign.getLine(1));
			BuyRegion buy = plugin.getFileManager().getBuy(sign.getLine(1));
			if(rent != null && block.getLocation().equals(rent.getSignLocation())) {
				/* Remove the rent if the player has permission */
				if(event.getPlayer().hasPermission("areashop.destroyrent")) {
					rent.handleSchematicEvent(RentRegion.RentEvent.DELETED);
					boolean result = plugin.getFileManager().removeRent(sign.getLine(1), true);
					if(result) {
						plugin.message(event.getPlayer(), "destroy-successRent", sign.getLine(1));
					}
				} else { /* Cancel the breaking of the sign */
					event.setCancelled(true);
					plugin.message(event.getPlayer(), "destroy-noPermissionRent");
				}
			} else if(buy != null && block.getLocation().equals(buy.getSignLocation())) {
				/* Remove the buy if the player has permission */
				if(event.getPlayer().hasPermission("areashop.destroybuy")) {
					buy.handleSchematicEvent(BuyRegion.BuyEvent.DELETED);
					boolean result = plugin.getFileManager().removeBuy(sign.getLine(1), true);
					if(result) {
						plugin.message(event.getPlayer(), "destroy-successBuy", sign.getLine(1));
					}
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
	@EventHandler(priority = EventPriority.HIGH)
    public void onIndirectSignBreak(BlockPhysicsEvent event){
        Block block = event.getBlock();
        if(block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN){
            Sign sign = (Sign)block.getState();
            Block attachedTo = block.getRelative(((org.bukkit.material.Sign)sign.getData()).getAttachedFace());
            if(attachedTo.getType() == Material.AIR){
				/* Check if the rent sign is really the same as a saved rent */
				RentRegion rent = plugin.getFileManager().getRent(sign.getLine(1));
				BuyRegion buy = plugin.getFileManager().getBuy(sign.getLine(1));
				if(rent != null && block.getLocation().equals(rent.getSignLocation())) {
					/* Remove the rent */
					boolean result = plugin.getFileManager().removeRent(sign.getLine(1), true);
					if(result) {
						plugin.getLogger().info("Renting of region '" + sign.getLine(1) + "' has been removed by indirectly breaking the sign");
					}
				} else if(buy != null && block.getLocation().equals(buy.getSignLocation())) {
					/* Remove the buy */
					boolean result = plugin.getFileManager().removeBuy(sign.getLine(1), true);
					if(result) {
						plugin.getLogger().info("Buying of region '" + sign.getLine(1) + "' has been removed by indirectly breaking the sign");
					}
				}
            }
        }
    }
}



















































