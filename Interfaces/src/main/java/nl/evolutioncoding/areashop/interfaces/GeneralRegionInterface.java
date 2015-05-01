package nl.evolutioncoding.areashop.interfaces;

import org.bukkit.World;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public interface GeneralRegionInterface {
	public ProtectedRegion getRegion();
	public String getName();
	public World getWorld();
	public String getWorldName();
	public int getWidth();
	public int getDepth();
	public int getHeight();
}
