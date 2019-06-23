package me.wiefferink.areashop.handlers;

import me.wiefferink.areashop.interfaces.AreaShopInterface;
import me.wiefferink.areashop.interfaces.BukkitInterface;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;

public class BukkitHandler1_12 extends BukkitInterface {

	public BukkitHandler1_12(AreaShopInterface pluginInterface) {
		super(pluginInterface);
	}

	// Uses Sign, which is deprecated in 1.13+, broken in 1.14+
	@Override
	public BlockFace getSignFacing(Block block) {
		if (block == null) {
			return null;
		}

		BlockState blockState = block.getState();
		if (blockState == null) {
			return null;
		}

		MaterialData materialData = blockState.getData();
		if(materialData instanceof org.bukkit.material.Sign) {
			return ((org.bukkit.material.Sign)materialData).getFacing();
		}

		return null;
	}

	@Override
	public boolean setSignFacing(Block block, BlockFace facing) {
		if (block == null || facing == null) {
			return false;
		}

		BlockState blockState = block.getState();
		if (blockState == null) {
			return false;
		}

		org.bukkit.material.Sign signData = (org.bukkit.material.Sign) blockState.getData();
		if (signData == null) {
			return false;
		}

		signData.setFacingDirection(facing);
		blockState.setData(signData);
		blockState.update(true, true);
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

		org.bukkit.material.Sign signData = (org.bukkit.material.Sign) blockState.getData();
		if (signData == null) {
			return null;
		}

		return block.getRelative(signData.getAttachedFace());
	}
}
