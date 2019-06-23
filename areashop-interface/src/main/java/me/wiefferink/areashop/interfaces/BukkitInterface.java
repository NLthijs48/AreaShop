package me.wiefferink.areashop.interfaces;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public abstract class BukkitInterface {
	protected final AreaShopInterface pluginInterface;

	public BukkitInterface(AreaShopInterface pluginInterface) {
		this.pluginInterface = pluginInterface;
	}

	/**
	 * Get the direciton a sign is facing.
	 * @param block Sign block to get the facing from
	 * @return direction the sign is facing
	 */
	public abstract BlockFace getSignFacing(Block block);

	/**
	 * Set the direction a sign is facing.
	 * @param block Sign block to update
	 * @param facing direction to let the sign face
	 * @return true when successful, otherwise false
	 */
	public abstract boolean setSignFacing(Block block, BlockFace facing);

	/**
	 * Get the block a sign is attached to.
	 * @param block Sign block
	 * @return Block the sign is attached to, or null when not a sign or not attached
	 */
	public abstract Block getSignAttachedTo(Block block);

}
