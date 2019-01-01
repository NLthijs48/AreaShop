package me.wiefferink.areashop.handlers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
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
}
