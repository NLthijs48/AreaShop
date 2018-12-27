package me.wiefferink.areashop.handlers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import me.wiefferink.areashop.interfaces.AreaShopInterface;
import org.bukkit.World;

public class WorldGuardHandler7_beta_1 extends WorldGuardHandler6_1_3 {

	public WorldGuardHandler7_beta_1(AreaShopInterface pluginInterface) {
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
}
