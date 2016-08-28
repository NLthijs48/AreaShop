package me.wiefferink.areashop.features;

import me.wiefferink.areashop.AreaShop;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class Feature implements Listener {
	public AreaShop plugin = AreaShop.getInstance();
	public YamlConfiguration config = plugin.getConfig();

	/**
	 * Start listening to events
	 */
	public void listen() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	/**
	 * Stop listening to events
	 */
	public void stopListen() {
		HandlerList.unregisterAll(this);
	}

}
