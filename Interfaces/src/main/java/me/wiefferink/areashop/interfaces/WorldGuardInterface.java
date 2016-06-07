package me.wiefferink.areashop.interfaces;

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
	 * Parse an owner(s) string and set the players as owner of the WorldGuard region (set by UUID or name depending on implementation) 
	 * @param region The WorldGuard region to set the owners of
	 * @param input The owner(s) string to parse and set
	 */
	public abstract void setOwners(ProtectedRegion region, String input);
	
	/**
	 * Parse a member(s) string and set the players as member of the WorldGuard region (set by UUID or name depending on implementation) 
	 * @param region The WorldGuard region to set the members of
	 * @param input The member(s) string to parse and set
	 */
	public abstract void setMembers(ProtectedRegion region, String input);

	/**
	 * Get a set of ProtectedRegion's that are present on a certain location
	 * @param location The location to check
	 * @return A set containing all regions present at that location
	 */
	public abstract Set<ProtectedRegion> getApplicableRegionsSet(Location location);

	/**
	 * Check if a player is a member of the WorldGuard region
	 * @param region The region to check
	 * @param player The player to check
	 * @return true if the player is a member of the region, otherwise false
	 */
	public abstract boolean containsMember(ProtectedRegion region, UUID player);
	
	/**
	 * Check if a player is an owner of the WorldGuard region
	 * @param region The region to check
	 * @param player The player to check
	 * @return true if the player is an owner of the region, otherwise false
	 */
	public abstract boolean containsOwner(ProtectedRegion region, UUID player);
}