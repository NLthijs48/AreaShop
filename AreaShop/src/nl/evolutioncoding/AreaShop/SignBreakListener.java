package nl.evolutioncoding.AreaShop;

import java.util.HashMap;

import nl.evolutioncoding.AreaShop.AreaShop.RegionEventType;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
			HashMap<String,String> rent = plugin.getFileManager().getRent(sign.getLine(1));
			HashMap<String,String> buy = plugin.getFileManager().getBuy(sign.getLine(1));
			if(rent != null && rent.get(AreaShop.keyWorld).equals(block.getWorld().getName())	
					&& rent.get(AreaShop.keyX).equals(String.valueOf(block.getX()))
					&& rent.get(AreaShop.keyY).equals(String.valueOf(block.getY()))
					&& rent.get(AreaShop.keyZ).equals(String.valueOf(block.getZ())) ) {
				/* Remove the rent if the player has permission */
				if(event.getPlayer().hasPermission("areashop.destroyrent")) {
					plugin.getFileManager().handleSchematicEvent(sign.getLine(1), true, RegionEventType.DELETED);
					boolean result = plugin.getFileManager().removeRent(sign.getLine(1), true);
					
					if(result) {
						event.getPlayer().sendMessage(plugin.fixColors(plugin.config().getString("chatPrefix")) + "Renting of the region succesfully removed");
					}
				} else { /* Cancel the breaking of the sign */
					event.setCancelled(true);
					event.getPlayer().sendMessage(plugin.fixColors(plugin.config().getString("chatPrefix")) + "You don't have permission for destroying a sign for renting a region");
				}
			} else if(buy != null && buy.get(AreaShop.keyWorld).equals(block.getWorld().getName())	
					&& buy.get(AreaShop.keyX).equals(String.valueOf(block.getX()))
					&& buy.get(AreaShop.keyY).equals(String.valueOf(block.getY()))
					&& buy.get(AreaShop.keyZ).equals(String.valueOf(block.getZ())) ) {
				/* Remove the buy if the player has permission */
				if(event.getPlayer().hasPermission("areashop.destroybuy")) {
					plugin.getFileManager().handleSchematicEvent(sign.getLine(1), false, RegionEventType.DELETED);
					boolean result = plugin.getFileManager().removeBuy(sign.getLine(1), true);
					if(result) {
						event.getPlayer().sendMessage(plugin.fixColors(plugin.config().getString("chatPrefix")) + "Buying of the region succesfully removed");
					}
				} else { /* Cancel the breaking of the sign */
					event.setCancelled(true);
					event.getPlayer().sendMessage(plugin.fixColors(plugin.config().getString("chatPrefix")) + "You don't have permission for destroying a sign for buying a region");
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
				HashMap<String,String> rent = plugin.getFileManager().getRent(sign.getLine(1));
				HashMap<String,String> buy = plugin.getFileManager().getBuy(sign.getLine(1));
				if(rent != null && rent.get(AreaShop.keyWorld).equals(block.getWorld().getName())	
						&& rent.get(AreaShop.keyX).equals(String.valueOf(block.getX()))
						&& rent.get(AreaShop.keyY).equals(String.valueOf(block.getY()))
						&& rent.get(AreaShop.keyZ).equals(String.valueOf(block.getZ())) ) {
					/* Remove the rent */
					boolean result = plugin.getFileManager().removeRent(sign.getLine(1), true);
					if(result) {
						plugin.getLogger().info("Renting of region '" + sign.getLine(1) + "' has been removed by indirectly breaking the sign");
					}
				} else if(buy != null && buy.get(AreaShop.keyWorld).equals(block.getWorld().getName())	
						&& buy.get(AreaShop.keyX).equals(String.valueOf(block.getX()))
						&& buy.get(AreaShop.keyY).equals(String.valueOf(block.getY()))
						&& buy.get(AreaShop.keyZ).equals(String.valueOf(block.getZ())) ) {
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



















































