package me.wiefferink.areashop.interfaces;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;

import java.util.Set;
import java.util.UUID;

public abstract class WorldGuardInterface {
	protected AreaShopInterface pluginInterface;

	public WorldGuardInterface(AreaShopInterface pluginInterface) {
		this.pluginInterface = pluginInterface;
	}

	/**
	 * Get a set of ProtectedRegion's that are present on a certain location.
	 * @param location The location to check
	 * @return A set containing all regions present at that location
	 */
	public abstract Set<ProtectedRegion> getApplicableRegionsSet(Location location);

	// DefaultDomain changed from abstract to non-abstract in version 6, so we need to handle that differently
	/**
	 * Parse an owner(s) string and set the players as owner of the WorldGuard region (set by UUID or name depending on implementation).
	 * @param region The WorldGuard region to set the owners of
	 * @param regionAccessSet  The owner(s) to set
	 */
	public abstract void setOwners(ProtectedRegion region, RegionAccessSet regionAccessSet);

	/**
	 * Parse a member(s) string and set the players as member of the WorldGuard region (set by UUID or name depending on implementation).
	 * @param region The WorldGuard region to set the members of
	 * @param regionAccessSet  The member(s) to set
	 */
	public abstract void setMembers(ProtectedRegion region, RegionAccessSet regionAccessSet);

	/**
	 * Check if a player is a member of the WorldGuard region.
	 * @param region The region to check
	 * @param player The player to check
	 * @return true if the player is a member of the region, otherwise false
	 */
	public abstract boolean containsMember(ProtectedRegion region, UUID player);

	/**
	 * Check if a player is an owner of the WorldGuard region.
	 * @param region The region to check
	 * @param player The player to check
	 * @return true if the player is an owner of the region, otherwise false
	 */
	public abstract boolean containsOwner(ProtectedRegion region, UUID player);

	/**
	 * Get the members of a region.
	 * @param region to get the members of
	 * @return RegionAccessSet with all members (by uuid and name) and groups of the given region
	 */
	public abstract RegionAccessSet getMembers(ProtectedRegion region);

	/**
	 * Get the owners of a region.
	 * @param region to get the owners of
	 * @return RegionAccessSet with all owners (by uuid and name) and groups of the given region
	 */
	public abstract RegionAccessSet getOwners(ProtectedRegion region);

	// New flag system was introcuded in version 6.1.3, requiring different flag parsing
	/**
	 * Get a flag from the name of a flag.
	 * @param flagName The name of the flag to get
	 * @return The specific flag type for the given name
	 */
	public abstract Flag<?> fuzzyMatchFlag(String flagName);

	/**
	 * Convert string input to a region group flag value.
	 * @param flag  The flag to parse the input for
	 * @param input The input
	 * @param <V> Flag type
	 * @return The RegionGroup denoted by the input
	 * @throws InvalidFlagFormat When the input for the flag is incorrect
	 */
	public abstract <V> V parseFlagInput(Flag<V> flag, String input) throws InvalidFlagFormat;

	/**
	 * Convert string input to a region group flag value.
	 * @param flag  The flag to parse the input for
	 * @param input The input
	 * @return The RegionGroup denoted by the input
	 * @throws InvalidFlagFormat When the input for the flag is incorrect
	 */
	public abstract RegionGroup parseFlagGroupInput(RegionGroupFlag flag, String input) throws InvalidFlagFormat;
}
