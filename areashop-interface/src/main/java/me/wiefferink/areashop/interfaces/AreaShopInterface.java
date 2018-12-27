package me.wiefferink.areashop.interfaces;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.logging.Logger;

public interface AreaShopInterface {
	void debugI(Object... message);

	YamlConfiguration getConfig();

	WorldGuardPlugin getWorldGuard();

	WorldEditPlugin getWorldEdit();

	Logger getLogger();
}
