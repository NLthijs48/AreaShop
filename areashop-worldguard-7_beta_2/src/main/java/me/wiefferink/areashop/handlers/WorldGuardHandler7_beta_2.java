package me.wiefferink.areashop.handlers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
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
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class WorldGuardHandler7_beta_2 extends WorldGuardHandler6_1_3 {

	public WorldGuardHandler7_beta_2(AreaShopInterface pluginInterface) {
		super(pluginInterface);
	}

	@Override
	public RegionManager getRegionManager(World world) {
		return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
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
