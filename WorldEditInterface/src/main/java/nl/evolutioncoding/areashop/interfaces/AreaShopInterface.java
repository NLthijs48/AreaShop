package nl.evolutioncoding.areashop.interfaces;

import java.util.logging.Logger;

import org.bukkit.configuration.file.YamlConfiguration;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public interface AreaShopInterface {
	public void debugI(String message);
	public YamlConfiguration getConfig();
	public WorldGuardPlugin getWorldGuard();
	public WorldEditPlugin getWorldEdit();
	public Logger getLogger();
}
