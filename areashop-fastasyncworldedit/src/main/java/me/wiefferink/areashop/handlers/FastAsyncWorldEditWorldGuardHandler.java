package me.wiefferink.areashop.handlers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.wiefferink.areashop.interfaces.AreaShopInterface;
import me.wiefferink.areashop.interfaces.RegionAccessSet;
import me.wiefferink.areashop.interfaces.WorldGuardInterface;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class FastAsyncWorldEditWorldGuardHandler extends WorldGuardInterface {

	public FastAsyncWorldEditWorldGuardHandler(AreaShopInterface pluginInterface) {
		super(pluginInterface);
	}

	@Override
	public RegionManager getRegionManager(World world) {
		return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
	}

	@Override
	public Set<ProtectedRegion> getApplicableRegionsSet(Location location) {
		Set<ProtectedRegion> result = new HashSet<>();
		BlockVector3 vector = BlockVector3.at(location.getX(), location.getY(), location.getZ());
		for(ProtectedRegion region : getRegionManager(location.getWorld()).getRegions().values()) {
			if(region.contains(vector)) {
				result.add(region);
			}
		}
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

		for(UUID uuid : regionAccessSet.getPlayerUniqueIds()) {
			owners.addPlayer(uuid);
		}

		for(String group : regionAccessSet.getGroupNames()) {
			owners.addGroup(group);
		}

		return owners;
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
	public boolean containsMember(ProtectedRegion region, UUID player) {
		return region.getMembers().contains(player);
	}

	@Override
	public boolean containsOwner(ProtectedRegion region, UUID player) {
		return region.getOwners().contains(player);
	}

	@Override
	public RegionAccessSet getMembers(ProtectedRegion region) {
		RegionAccessSet result = new RegionAccessSet();
		result.getGroupNames().addAll(region.getMembers().getGroups());
		result.getPlayerNames().addAll(region.getMembers().getPlayers());
		result.getPlayerUniqueIds().addAll(region.getMembers().getUniqueIds());
		return result;
	}

	@Override
	public RegionAccessSet getOwners(ProtectedRegion region) {
		RegionAccessSet result = new RegionAccessSet();
		result.getGroupNames().addAll(region.getOwners().getGroups());
		result.getPlayerNames().addAll(region.getOwners().getPlayers());
		result.getPlayerUniqueIds().addAll(region.getOwners().getUniqueIds());
		return result;
	}

	@Override
	public Flag<?> fuzzyMatchFlag(String flagName) {
		return Flags.fuzzyMatchFlag(WorldGuard.getInstance().getFlagRegistry(), flagName);
	}

	@Override
	public <V> V parseFlagInput(Flag<V> flag, String input) throws InvalidFlagFormat {
		return flag.parseInput(FlagContext.create().setInput(input).build());
	}

	@Override
	public RegionGroup parseFlagGroupInput(RegionGroupFlag flag, String input) throws InvalidFlagFormat {
		return flag.parseInput(FlagContext.create().setInput(input).build());
	}

	@Override
	public Vector getMinimumPoint(ProtectedRegion region) {
		BlockVector3 min = region.getMinimumPoint();
		return new Vector(min.getX(), min.getY(), min.getZ());
	}

	@Override
	public Vector getMaximumPoint(ProtectedRegion region) {
		BlockVector3 min = region.getMaximumPoint();
		return new Vector(min.getX(), min.getY(), min.getZ());
	}

	@Override
	public List<Vector> getRegionPoints(ProtectedRegion region) {
		List<Vector> result = new ArrayList<>();
		for (BlockVector2 point : region.getPoints()) {
			result.add(new Vector(point.getX(), 0,point.getZ()));
		}
		return result;
	}

	@Override
	public ProtectedCuboidRegion createCuboidRegion(String name, Vector corner1, Vector corner2) {
		return new ProtectedCuboidRegion(name, BlockVector3.at(corner1.getBlockX(), corner1.getBlockY(), corner1.getBlockZ()), BlockVector3.at(corner2.getBlockX(), corner2.getBlockY(), corner2.getBlockZ()));
	}
}
