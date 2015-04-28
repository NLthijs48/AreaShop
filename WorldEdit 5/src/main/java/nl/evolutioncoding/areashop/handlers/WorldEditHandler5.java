package nl.evolutioncoding.areashop.handlers;

import java.io.File;
import java.io.IOException;

import nl.evolutioncoding.areashop.interfaces.AreaShopInterface;
import nl.evolutioncoding.areashop.interfaces.GeneralRegionInterface;
import nl.evolutioncoding.areashop.interfaces.WorldEditInterface;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldEditHandler5 extends WorldEditInterface {

	public WorldEditHandler5(AreaShopInterface pluginInterface) {
		super(pluginInterface);
	}

	@Override
	public boolean restoreRegionBlocks(File file, GeneralRegionInterface regionInterface) {
		boolean result = true;
		EditSession editSession = pluginInterface.getWorldEdit().getWorldEdit().getEditSessionFactory().getEditSession(new BukkitWorld(regionInterface.getWorld()), pluginInterface.getConfig().getInt("maximumBlocks"));
		// Get the origin and size of the region
		Vector origin = new Vector(regionInterface.getRegion().getMinimumPoint().getBlockX(), regionInterface.getRegion().getMinimumPoint().getBlockY(), regionInterface.getRegion().getMinimumPoint().getBlockZ());
		
		editSession.enableQueue();
		Exception otherException = null;
		try {
			CuboidClipboard clipBoard = SchematicFormat.MCEDIT.load(file);
			if(clipBoard.getHeight() != regionInterface.getHeight()
					|| clipBoard.getWidth() != regionInterface.getWidth()
					|| clipBoard.getLength() != regionInterface.getDepth()) {
				pluginInterface.getLogger().warning("Size of the region " + regionInterface.getName() + " is not the same as the schematic to restore!");
				pluginInterface.debugI("schematic|region, x:" + clipBoard.getWidth() + "|" + regionInterface.getWidth() + ", y:" + clipBoard.getHeight() + "|" + regionInterface.getHeight() + ", z:" + clipBoard.getLength() + "|" + regionInterface.getDepth());
			}
			clipBoard.place(editSession, origin, false);
		} catch(MaxChangedBlocksException e) {
			pluginInterface.getLogger().warning("Exeeded the block limit while restoring schematic of " + regionInterface.getName());
			result = false;
		} catch(DataException e) {
			otherException = e;
		} catch(IOException e) {
			otherException = e;
		}
		if(otherException != null) {
			pluginInterface.getLogger().warning("Failed to restore schematic for region " + regionInterface.getName());
			pluginInterface.debugI(ExceptionUtils.getStackTrace(otherException));
			result = false;
		}
		editSession.flushQueue();
		return result;
	}

	@Override
	public boolean saveRegionBlocks(File file, GeneralRegionInterface regionInterface) {
		boolean result = true;
		ProtectedRegion region = regionInterface.getRegion();
		// Get the origin and size of the region
		Vector origin = new Vector(region.getMinimumPoint().getBlockX(), region.getMinimumPoint().getBlockY(), region.getMinimumPoint().getBlockZ());
		Vector size = (new Vector(region.getMaximumPoint().getBlockX(), region.getMaximumPoint().getBlockY(), region.getMaximumPoint().getBlockZ()).subtract(origin)).add(new Vector(1,1,1));
		EditSession editSession = new EditSession(new BukkitWorld(regionInterface.getWorld()), pluginInterface.getConfig().getInt("maximumBlocks"));
		// Save the schematic
		editSession.enableQueue();
		CuboidClipboard clipboard = new CuboidClipboard(size, origin);
		clipboard.copy(editSession);
		Exception otherException = null;
		try {
			SchematicFormat.MCEDIT.save(clipboard, file);
		} catch (DataException e) {
			otherException = e;
		} catch (IOException e) {
			otherException = e;
		}
		if(otherException != null) {
			pluginInterface.getLogger().warning("Failed to save schematic for region " + regionInterface.getName());
			pluginInterface.debugI(ExceptionUtils.getStackTrace(otherException));
			result = false;
		}
		editSession.flushQueue();
		return result;
	}


	
}