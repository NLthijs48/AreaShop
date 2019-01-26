package me.wiefferink.areashop.interfaces;

import org.bukkit.Location;
import org.bukkit.World;

public class WorldEditSelection {
	private final World world;
	private final Location minimum;
	private final Location maximum;

	/**
	 * Craete a WorldEditSelection.
	 * @param world World the selection is in
	 * @param a Primary selection location
	 * @param b Secondary selection location
	 */
	public WorldEditSelection(World world, Location a, Location b) {
		this.world = world;
		this.minimum = new Location(world, Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
		this.maximum = new Location(world, Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
	}

	public World getWorld() {
		return world;
	}

	/**
	 * Get the minimum Location of the selection.
	 * @return Location with the lowest x, y and z
	 */
	public Location getMinimumLocation() {
		return minimum;
	}

	/**
	 * Get the maximum Location of the selection.
	 * @return Location with the highest x, y and z
	 */
	public Location getMaximumLocation() {
		return maximum;
	}

	/**
	 * Get X-size.
	 *
	 * @return width
	 */
	public int getWidth() {
		return maximum.getBlockX() - minimum.getBlockX() + 1;
	}

	/**
	 * Get Y-size.
	 *
	 * @return height
	 */
	public int getHeight() {
		return maximum.getBlockY() - minimum.getBlockY() + 1;
	}

	/**
	 * Get Z-size.
	 *
	 * @return length
	 */
	public int getLength() {
		return maximum.getBlockZ() - minimum.getBlockZ() + 1;
	}

}
