package me.wiefferink.areashop.managers;

import me.wiefferink.areashop.AreaShop;

public abstract class Manager {

	final AreaShop plugin = AreaShop.getInstance();

	/**
	 * Called at shutdown of the plugin.
	 */
	public void shutdown() {
		// To override by extending classes
	}

}
