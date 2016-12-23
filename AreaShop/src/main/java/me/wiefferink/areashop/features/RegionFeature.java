package me.wiefferink.areashop.features;

import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.regions.GeneralRegion;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class RegionFeature implements Listener {
	static final AreaShop plugin = AreaShop.getInstance();

	public YamlConfiguration config = plugin.getConfig();
	GeneralRegion region;

	/**
	 * Start listening to events
	 */
	public void listen() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	/**
	 * Destroy the feature and deregister everything
	 */
	public void shutdownFeature() {
		HandlerList.unregisterAll(this);
		shutdown();
	}

	/**
	 * Dummy method a RegionFeature implementation can override
	 */
	public void shutdown() {
	}

}
