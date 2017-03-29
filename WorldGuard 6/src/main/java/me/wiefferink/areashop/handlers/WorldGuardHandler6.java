package me.wiefferink.areashop.handlers;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.wiefferink.areashop.interfaces.AreaShopInterface;
import me.wiefferink.areashop.interfaces.WorldGuardInterface;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WorldGuardHandler6 extends WorldGuardInterface {

	public WorldGuardHandler6(AreaShopInterface pluginInterface) {
		super(pluginInterface);
	}

	@Override
	public void setOwners(ProtectedRegion region, String input) {
		// Split the string and parse all values
		String[] names = input.split(", ");
		DefaultDomain owners = region.getOwners();
		owners.removeAll();
		for(String owner : names) {
			if(owner != null && !owner.isEmpty()) {
				// Check for groups
				if(owner.startsWith("g:")) {
					if(owner.length() > 2) {
						owners.addGroup(owner.substring(2));
					}
				} else if(owner.startsWith("n:")) {
					if(owner.length() > 2) {
						owners.addPlayer(owner.substring(2));
					}
				} else {
					UUID uuid;
					try {
						uuid = UUID.fromString(owner);
					} catch(IllegalArgumentException e) {
						// Don't like this but cannot access main plugin class from this module...
						System.out.println("[AreaShop] Tried using '" + owner + "' as uuid for a region owner, is your flagProfiles section correct?");
						uuid = null;
					}
					if(uuid != null) {
						owners.addPlayer(uuid);
					}
				}
			}
		}
		region.setOwners(owners);
		//System.out.println("  Flag " + flagName + " set: " + owners.toUserFriendlyString());		
	}

	@Override
	public void setMembers(ProtectedRegion region, String input) {
		// Split the string and parse all values
		String[] names = input.split(", ");
		DefaultDomain members = region.getMembers();
		members.removeAll();
		for(String member : names) {
			if(member != null && !member.isEmpty()) {
				// Check for groups
				if(member.startsWith("g:")) {
					if(member.length() > 2) {
						members.addGroup(member.substring(2));
					}
				} else if(member.startsWith("n:")) {
					if(member.length() > 2) {
						members.addPlayer(member.substring(2));
					}
				} else {
					UUID uuid;
					try {
						uuid = UUID.fromString(member);
					} catch(IllegalArgumentException e) {
						// Don't like this but cannot access main plugin class from this module...
						System.out.println("[AreaShop] Tried using '" + member + "' as uuid for a region member, is your flagProfiles section correct?");
						uuid = null;
					}
					if(uuid != null) {
						members.addPlayer(uuid);
					}
				}
			}
		}
		region.setMembers(members);
		//System.out.println("  Flag " + flagName + " set: " + members.toUserFriendlyString());
	}

	@Override
	public Set<ProtectedRegion> getApplicableRegionsSet(Location location) {
		Set<ProtectedRegion> result = new HashSet<>();
		Vector vector = new Vector(location.getX(), location.getY(), location.getZ());
		for(ProtectedRegion region : pluginInterface.getWorldGuard().getRegionManager(location.getWorld()).getRegions().values()) {
			if(region.contains(vector)) {
				result.add(region);
			}
		}
		return result;
	}

	@Override
	public boolean containsMember(ProtectedRegion region, UUID player) {
		return region.getMembers().contains(player);
	}

	@Override
	public boolean containsOwner(ProtectedRegion region, UUID player) {
		return region.getOwners().contains(player);
	}

	@Override
	public Flag<?> fuzzyMatchFlag(String flagName) {
		return DefaultFlag.fuzzyMatchFlag(flagName);
	}

	@Override
	public <V> V parseFlagInput(Flag<V> flag, String input) throws InvalidFlagFormat {
		return flag.parseInput(WorldGuardPlugin.inst(), null, input);
	}

	@Override
	public RegionGroup parseFlagGroupInput(RegionGroupFlag flag, String input) throws InvalidFlagFormat {
		return flag.parseInput(WorldGuardPlugin.inst(), null, input);
	}
}