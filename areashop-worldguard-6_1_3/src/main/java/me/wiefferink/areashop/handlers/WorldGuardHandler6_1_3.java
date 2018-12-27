package me.wiefferink.areashop.handlers;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import me.wiefferink.areashop.interfaces.AreaShopInterface;

public class WorldGuardHandler6_1_3 extends WorldGuardHandler6 {

	public WorldGuardHandler6_1_3(AreaShopInterface pluginInterface) {
		super(pluginInterface);
	}

	@Override
	public Flag<?> fuzzyMatchFlag(String flagName) {
		return DefaultFlag.fuzzyMatchFlag(WorldGuardPlugin.inst().getFlagRegistry(), flagName);
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