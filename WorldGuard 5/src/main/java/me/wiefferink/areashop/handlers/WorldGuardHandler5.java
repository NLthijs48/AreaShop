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
import me.wiefferink.areashop.interfaces.RegionAccessSet;
import me.wiefferink.areashop.interfaces.WorldGuardInterface;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WorldGuardHandler5 extends WorldGuardInterface {

	public WorldGuardHandler5(AreaShopInterface pluginInterface) {
		super(pluginInterface);
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
	public void setOwners(ProtectedRegion region, RegionAccessSet regionAccessSet) {
		DefaultDomain defaultDomain = buildDomain(regionAccessSet);
		if(!region.getOwners().toUserFriendlyString().equals(defaultDomain.toUserFriendlyString())) {
			region.setOwners(defaultDomain);
		}
	}

	@Override
	public void setMembers(ProtectedRegion region, RegionAccessSet regionAccessSet) {
		DefaultDomain defaultDomain = buildDomain(regionAccessSet);
		if(!region.getMembers().toUserFriendlyString().equals(defaultDomain.toUserFriendlyString())) {
			region.setMembers(defaultDomain);
		}
	}

	@Override
	public RegionAccessSet getMembers(ProtectedRegion region) {
		RegionAccessSet result = new RegionAccessSet();
		result.getGroupNames().addAll(region.getMembers().getGroups());
		result.getPlayerNames().addAll(region.getMembers().getPlayers());
		return result;
	}

	@Override
	public RegionAccessSet getOwners(ProtectedRegion region) {
		RegionAccessSet result = new RegionAccessSet();
		result.getGroupNames().addAll(region.getOwners().getGroups());
		result.getPlayerNames().addAll(region.getOwners().getPlayers());
		return result;
	}

	/**
	 * Build a DefaultDomain from a RegionAccessSet.
	 * @param regionAccessSet RegionAccessSet to read
	 * @return DefaultDomain containing the entities from the RegionAccessSet
	 */
	private DefaultDomain buildDomain(RegionAccessSet regionAccessSet) {
		DefaultDomain owners = new DefaultDomain();

		for(String playerName : regionAccessSet.getPlayerNames()) {
			owners.addPlayer(playerName);
		}

		// Add by name since UUIDs were not yet supported
		for(UUID uuid : regionAccessSet.getPlayerUniqueIds()) {
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
			if(offlinePlayer != null && offlinePlayer.getName() != null) {
				owners.addPlayer(offlinePlayer.getName());
			}
		}

		for(String group : regionAccessSet.getGroupNames()) {
			owners.addGroup(group);
		}

		return owners;
	}

	@Override
	public boolean containsMember(ProtectedRegion region, UUID player) {
		if(player == null) {
			return false;
		} else {
			String name = Bukkit.getOfflinePlayer(player).getName();
			return name != null && region.getMembers().contains(name);
		}
	}

	@Override
	public boolean containsOwner(ProtectedRegion region, UUID player) {
		if(player == null) {
			return false;
		} else {
			String name = Bukkit.getOfflinePlayer(player).getName();
			return name != null && region.getOwners().contains(name);
		}
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
