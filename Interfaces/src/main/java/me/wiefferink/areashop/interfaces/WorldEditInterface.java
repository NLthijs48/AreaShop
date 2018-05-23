package me.wiefferink.areashop.interfaces;

import java.io.File;

public abstract class WorldEditInterface {
	protected final AreaShopInterface pluginInterface;

	public WorldEditInterface(AreaShopInterface pluginInterface) {
		this.pluginInterface = pluginInterface;
	}

	// Different way to restore blocks per implementation, newer ones support entities as well
	public abstract boolean restoreRegionBlocks(File file, GeneralRegionInterface regionInterface);

	// Different way to save blocks per implementation, newer ones support entities as well
	public abstract boolean saveRegionBlocks(File file, GeneralRegionInterface regionInterface);
}