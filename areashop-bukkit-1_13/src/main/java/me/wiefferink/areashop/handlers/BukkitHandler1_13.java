package me.wiefferink.areashop.handlers;

import me.wiefferink.areashop.interfaces.AreaShopInterface;
import me.wiefferink.areashop.interfaces.BukkitInterface;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.WallSign;

public class BukkitHandler1_13 extends BukkitInterface {

	public BukkitHandler1_13(AreaShopInterface pluginInterface) {
		super(pluginInterface);
	}

	// Uses BlockData, which does not yet exist in 1.12-
	@Override
	public BlockFace getSignFacing(Block block) {
		if (block == null) {
			return null;
		}

		BlockState blockState = block.getState();
		if (blockState == null) {
			return null;
		}

		BlockData blockData = blockState.getBlockData();
		if (blockData == null) {
			return null;
		}

		if(blockData instanceof WallSign) {
			return ((WallSign) blockData).getFacing();
		} else if(blockData instanceof Sign) {
			return ((Sign) blockData).getRotation();
		}

		return null;
	}

	// Uses BlockData, WallSign and Sign which don't exist in 1.12-
	@Override
	public boolean setSignFacing(Block block, BlockFace facing) {
		if (block == null || facing == null) {
			return false;
		}

		BlockState blockState = block.getState();
		if (blockState == null) {
			return false;
		}

		BlockData blockData = blockState.getBlockData();
		if (blockData == null) {
			return false;
		}

		if(blockData instanceof WallSign) {
			((WallSign) blockData).setFacing(facing);
		} else if(blockData instanceof Sign) {
			((Sign) blockData).setRotation(facing);
		} else {
			return false;
		}
		block.setBlockData(blockData);
		return true;
	}

	@Override
	public Block getSignAttachedTo(Block block) {
		if (block == null) {
			return null;
		}

		BlockState blockState = block.getState();
		if (blockState == null) {
			return null;
		}

		org.bukkit.block.data.BlockData blockData = blockState.getBlockData();
		if (blockData == null) {
			return null;
		}

		if(blockData instanceof WallSign) {
			return block.getRelative(((WallSign) blockData).getFacing().getOppositeFace());
		} else if(blockData instanceof Sign) {
			return block.getRelative(BlockFace.DOWN);
		}

		return null;
	}

}
