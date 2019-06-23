package me.wiefferink.areashop.interfaces;

import org.bukkit.entity.Player;

import java.io.File;

public abstract class WorldEditInterface {
	protected final AreaShopInterface pluginInterface;

	public WorldEditInterface(AreaShopInterface pluginInterface) {
		this.pluginInterface = pluginInterface;
	}

	/**
	 * Different way to restore blocks per implementation, newer ones support entities as well.
	 * Why: the schematic api has changed between WorldEdit 5 and 6, and the schematic format changed between 6 and 7
	 * @param file File to try restoring from to the location of the region
	 * @param regionInterface Region to restore from
	 * @return true when successful, otherwise false
	 */
	public abstract boolean restoreRegionBlocks(File file, GeneralRegionInterface regionInterface);

	/**
	 * Different way to save blocks per implementation, newer ones support entities as well.
	 * Why: the schematic api has changed between WorldEdit 5 and 6, and the schematic format changed between 6 and 7
	 * @param file File to try saving the region to
	 * @param regionInterface Region to restore from
	 * @return true when successful, otherwise false
	 */
	public abstract boolean saveRegionBlocks(File file, GeneralRegionInterface regionInterface);

	/**
	 * Get the selection of the player.
	 * Why: the underlying WorldEdit selection class has changed from interface <-> class
	 * @param player Player to get the selection for
	 * @return WorldEditSelection if the player has selected something, otherwise null
	 */
	public abstract WorldEditSelection getPlayerSelection(Player player);

}
