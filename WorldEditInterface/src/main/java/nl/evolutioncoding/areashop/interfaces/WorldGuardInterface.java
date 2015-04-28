package nl.evolutioncoding.areashop.interfaces;

import java.util.Set;

import org.bukkit.Location;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public abstract class WorldGuardInterface {
	protected AreaShopInterface pluginInterface;
	
	public WorldGuardInterface(AreaShopInterface pluginInterface) {
		this.pluginInterface = pluginInterface;
	}
	
	// Players set by UUID or name depending on implementation
	public abstract void setOwners(ProtectedRegion region, String input);
	// Players set by UUID or name depending on implementation
	public abstract void setMembers(ProtectedRegion region, String input);
	// Looping through the ApplicableRegionSet from WorldGuard is different per implementation
	public abstract Set<ProtectedRegion> getApplicableRegionsSet(Location location);
}