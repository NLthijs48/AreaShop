package nl.evolutioncoding.AreaShop;

import org.bukkit.scheduler.BukkitRunnable;

public class RentCheck extends BukkitRunnable {
	AreaShop plugin;
	
	/**
	 * Constructor
	 * @param plugin The AreaShop plugin
	 */
	public RentCheck(AreaShop plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void run() {
		plugin.getFileManager().checkRents();		
	}

}
