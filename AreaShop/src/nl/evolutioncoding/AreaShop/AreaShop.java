package nl.evolutioncoding.AreaShop;

import java.util.ArrayList;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

/**
 * Main class for the AreaShop plugin
 * @author NLThijs48
 */
public final class AreaShop extends JavaPlugin {
	/* General variables */
	private WorldGuardPlugin worldGuard = null;
	private Economy economy = null;
	private ShopManager shopManager = null;
	private LanguageManager languageManager = null;
	private boolean configOk = false;
	private boolean debug = false;
	private String chatprefix = null;
	
	/* Folder where the language files will be stored */
	public final String languageFolder = "lang";
	
	/* Euro tag for in the config */
	public final String currencyEuro = "%euro%";
	
	/* Keys for adding things to the hashmap */
	public final String keyWorld = "world";
	public final String keyX = "x";
	public final String keyY = "y";
	public final String keyZ = "z";
	public final String keyDuration = "duration";
	public final String keyPrice = "price";
	public final String keyPlayer = "player";
	public final String keyRentedUntil = "rented";
	public final String keyName = "name";
	
	/* Keys for replacing parts of flags */
	public final String tagPlayerName = "%player%";
	public final String tagRegionName = "%region%";
	public final String tagPrice = "%price%";
	public final String tagDuration = "%duration%";
	public final String tagRentedUntil = "%until%";
	
	
	
	/**
	 * Called on start or reload of the server
	 */
	public void onEnable(){
		boolean error = false;
		
		/* Save a copy of the default config.yml if one is not present */
		this.saveDefaultConfig();
	
		/* Check the config, loads default if errors */
		configOk = this.checkConfig();

		/* Check if WorldGuard is present */
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	    	this.getLogger().info("Error: WorldGuard plugin is not present or has not loaded correctly");
	    	error = true;
	    } else {
		    worldGuard = (WorldGuardPlugin)plugin;
	    }

	    /* Check if Vault is present */
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider == null) {
        	this.getLogger().info("Error: Vault plugin is not present or has not loaded correctly");
        	error = true;
        } else {
            economy = economyProvider.getProvider();
        }
        
	    /* Create a LanguageMananager */
	    languageManager = new LanguageManager(this);
	    
	    /* Save the chatPrefix */
	    chatprefix = this.config().getString("chatPrefix");

		/* Load all data from files */
	    shopManager = new ShopManager(this);
	    error = error & !shopManager.loadRents();
	    shopManager.checkRents();
	    error = error & !shopManager.loadBuys();
	    
		if(error) {
			this.getLogger().info("The plugin has not started, fix the errors listed above");
		} else {
			/* Register the event listeners */
			this.getServer().getPluginManager().registerEvents(new SignChangeListener(this), this);
			this.getServer().getPluginManager().registerEvents(new SignBreakListener(this), this);
			this.getServer().getPluginManager().registerEvents(new RightClickListener(this), this);
			
	        /* Start thread for checking renting */
	        int checkDelay = Integer.parseInt(this.config().getString("checkDelay"))*20;
	        new RentCheck(this).runTaskTimer(this, checkDelay, checkDelay);
		    
		    /* Bind commands for this plugin */
			getCommand("AreaShop").setExecutor(new ShopCommands(this));			
		}
	}
	
	/**
	 *  Called on shutdown or reload of the server 
	 */
	public void onDisable() {
		
		/* set variables to null to prevent memory leaks */
		worldGuard = null;
		economy = null;
		shopManager = null;
		languageManager = null;
		configOk = false;
		debug = false;
	}
 
	/**
	 * Function to get the WorldGuard plugin
	 * @return WorldGuardPlugin
	 */
	public WorldGuardPlugin getWorldGuard() {
	    return worldGuard;
	}
	
	/**
	 * Function to get the WorldGuard plugin
	 * @return WorldGuardPlugin
	 */
	public LanguageManager getLanguageManager() {
	    return languageManager;
	}
	
	/**
	 * Function to get the Vault plugin
	 * @return Economy
	 */
	public Economy getEconomy() {
	    return economy;
	}		
	
	/**
	 * Method to get the ShopManager
	 * @return The shopManager
	 */
	public ShopManager getShopManager() {
		return shopManager;
	}
	
	/**
	 * Method to send a message to a CommandSender, using chatprefix if it is a player
	 * @param target The CommandSender you wan't to send the message to (e.g. a player)
	 * @param key The key to get the translation
	 * @param params The parameters to inject into the message string
	 */
	public void message(Object target, String key, Object... params) {
		String langString = this.fixColors(languageManager.getLang(key, params));
		if(langString == null) {
			this.getLogger().info("Something is wrong with the language file, could not find key: " + key);
		} else {
			if(target instanceof Player) {
				((Player)target).sendMessage(this.fixColors(chatprefix) + langString);
			} else if(target instanceof CommandSender) {
				((CommandSender)target).sendMessage(langString);
			}	
			else if(target instanceof Logger) {
				((Logger)target).info(langString);
			} else {
				this.getLogger().info("Could not send message, target is wrong: " + langString);
			}
		}
	}
	
	/**
	 * Convert color and formatting codes to bukkit values
	 * @param input Start string with color and formatting codes in it
	 * @return String with the color and formatting codes in the bukkit format
	 */
	public String fixColors(String input) {
		String result = null;
		if(input != null) {
			result = input.replaceAll("(&([a-f0-9]))", "\u00A7$2");
			result = result.replaceAll("&k", ChatColor.MAGIC.toString());
			result = result.replaceAll("&l", ChatColor.BOLD.toString());
			result = result.replaceAll("&m", ChatColor.STRIKETHROUGH.toString());
			result = result.replaceAll("&n", ChatColor.UNDERLINE.toString());
			result = result.replaceAll("&o", ChatColor.ITALIC.toString());
			result = result.replaceAll("&r", ChatColor.RESET.toString());	
			result = result.replaceAll("€", "\u20AC");
		}		
		return result;		
	}
	
	/**
	 * Get the currency character, fixes problems with euro character acting weird
	 * @return Currency character
	 */
	public String getCurrencyCharacter() {
		String result = this.config().getString("moneyCharacter");
		result = result.replace(currencyEuro, "\u20ac");
		return result;
	}
	
	
	/**
	 * Function for quitting the plugin, NOT USED ATM
	 */
	public void quit() {
		this.getLogger().info("Plugin will be stopped");
		Bukkit.getPluginManager().disablePlugin(this);
	}
	
	
	/**
	 * Return the config configured by the user or the default
	 */
	public Configuration config() {
		if(configOk) {
			return this.getConfig();
		} else {
			return this.getConfig().getDefaults();
		}		
	}
	
	/**
	 * Shows the help page for the player
	 * @param player The player to show the help to
	 */
	public void showHelp(CommandSender target) {
		/* Set up the list of messages to be sent */
		ArrayList<String> messages = new ArrayList<String>();
		messages.add(this.config().getString("chatPrefix") + languageManager.getLang("help-header"));
		messages.add(this.config().getString("chatPrefix") + languageManager.getLang("help-alias"));
		if(target.hasPermission("areashop.help")) {
			messages.add(languageManager.getLang("help-help"));
		}
		if(target.hasPermission("areashop.info")) {
			messages.add(languageManager.getLang("help-info"));
		}
		if(target.hasPermission("areashop.rent")) {
			messages.add(languageManager.getLang("help-rent"));
		}
		if(target.hasPermission("areashop.buy")) {
			messages.add(languageManager.getLang("help-buy"));
		}
		if(target.hasPermission("areashop.unrent")) {
			messages.add(languageManager.getLang("help-unrent"));
		} else if(target.hasPermission("areashop.unrentown")) {
			messages.add(languageManager.getLang("help-unrentOwn"));
		}
		if(target.hasPermission("areashop.sell")) {
			messages.add(languageManager.getLang("help-sell"));
		} else if(target.hasPermission("areashop.sellown")) {
			messages.add(languageManager.getLang("help-sellOwn"));
		}
		if(target.hasPermission("areashop.updaterentsigns")) {
			messages.add(languageManager.getLang("help-updaterentsigns"));
		}
		if(target.hasPermission("areashop.updatebuysigns")) {
			messages.add(languageManager.getLang("help-updatebuysigns"));
		}
		if(target.hasPermission("areashop.updaterentregions")) {
			messages.add(languageManager.getLang("help-updaterentregions"));
		}
		if(target.hasPermission("areashop.updatebuyregions")) {
			messages.add(languageManager.getLang("help-updatebuyregions"));
		}
		if(target.hasPermission("areashop.reload")) {
			messages.add(languageManager.getLang("help-reload"));
		}
		
		/* Send them all */
		for(int i=0; i<messages.size(); i++) {
			target.sendMessage(this.fixColors(messages.get(i)));
		}
	}
	
	
	
	/**
	 * Checks the config for errors, loads default config if they occur
	 */
	public boolean checkConfig() {
		int error = 0;		
		debug = this.getConfig().getString("debug").equalsIgnoreCase("true");
		
		/* GENERAL */
		String chatPrefix = this.getConfig().getString("chatPrefix");
		if (chatPrefix.length() == 0) {
			this.getLogger().info("Config-Error: chatPrefix has length zero");
			error++;
		}
		String moneyCharacter = this.getCurrencyCharacter();
		if (moneyCharacter.length() > 14) {
			this.getLogger().info("Config-Error: moneyCharacter is longer than 14 characters");
			error++;
		}
		String maximumTotal = this.getConfig().getString("maximumTotal");
		try {
			int maximumTotalInt = Integer.parseInt(maximumTotal);
			if(maximumTotalInt < -1) {
				this.getLogger().info("Config-Error: maximumTotal must be -1 or higher");
				error++;
			}
		} catch (NumberFormatException e) {
			this.getLogger().info("Config-Error: maximumTotal is not a valid number");
			error++;
		}
		
		/* RENTING */
		String rentSign = this.getConfig().getString("rentSign");
		if (rentSign.length() > 15) {
			this.getLogger().info("Config-Error: rentSign is too long, maximum length is 15 characters");
			error++;
		}		
		String signRentable = this.getConfig().getString("signRentable");
		if (signRentable.length() > 15) {
			this.getLogger().info("Config-Error: signRentable is too long, maximum length is 15 characters");
			error++;
		}		
		String signRented = this.getConfig().getString("signRented");
		if (signRented.length() > 15) {
			this.getLogger().info("Config-Error: signRented is too long, maximum length is 15 characters");
			error++;
		}
		String maximumRents = this.getConfig().getString("maximumRents");
		try {
			int maximumRentsInt = Integer.parseInt(maximumRents);
			if(maximumRentsInt < -1) {
				this.getLogger().info("Config-Error: maximumRents must be -1 or higher");
				error++;
			}
		} catch (NumberFormatException e) {
			this.getLogger().info("Config-Error: maximumRents is not a valid number");
			error++;
		}
		String rentMoneyBack = this.getConfig().getString("rentMoneyBack");
		try {
			int rentMoneyBackInt = Integer.parseInt(rentMoneyBack);
			if(rentMoneyBackInt < 0 || rentMoneyBackInt > 100) {
				this.getLogger().info("Config-Error: rentMoneyBack must be between 0 and 100");
				error++;
			}
		} catch (NumberFormatException e) {
			this.getLogger().info("Config-Error: rentMoneyBack is not a valid number");
			error++;
		}		
		String checkDelay = this.getConfig().getString("checkDelay");
		try {
			int checkDelayInt = Integer.parseInt(checkDelay);
			if(checkDelayInt < 1) {
				this.getLogger().info("Config-Error: checkDelay can't be below 1");
				error++;
			}
		} catch (NumberFormatException e) {
			this.getLogger().info("Config-Error: checkDelay is not a valid number");
			error++;
		}
		
		/* BUYING */
		String buySign = this.getConfig().getString("buySign");
		if (buySign.length() > 15) {
			this.getLogger().info("Config-Error: buySign is too long, maximum length is 15 characters");
			error++;
		}		
		String signBuyable = this.getConfig().getString("signBuyable");
		if (signBuyable.length() > 15) {
			this.getLogger().info("Config-Error: signBuyable is too long, maximum length is 15 characters");
			error++;
		}		
		String signBuyed = this.getConfig().getString("signBuyed");
		if (signBuyed.length() > 15) {
			this.getLogger().info("Config-Error: signBuyed is too long, maximum length is 15 characters");
			error++;
		}
		String maximumBuys = this.getConfig().getString("maximumBuys");
		try {
			int maximumBuysInt = Integer.parseInt(maximumBuys);
			if(maximumBuysInt < -1) {
				this.getLogger().info("Config-Error: maximumBuys must be -1 or higher");
				error++;
			}
		} catch (NumberFormatException e) {
			this.getLogger().info("Config-Error: maximumBuys is not a valid number");
			error++;
		}
		String buyMoneyBack = this.getConfig().getString("buyMoneyBack");
		try {
			int buyMoneyBackInt = Integer.parseInt(buyMoneyBack);
			if(buyMoneyBackInt < 0 || buyMoneyBackInt > 100) {
				this.getLogger().info("Config-Error: buyMoneyBack must be between 0 and 100");
				error++;
			}
		} catch (NumberFormatException e) {
			this.getLogger().info("Config-Error: buyMoneyBack is not a valid number");
			error++;
		}		
		
		/* Load default config if errors have occurred */
		if (error > 0) {
			this.getLogger().info("The plugin has " + error + " error(s) in the config, default config will be used");
		}
		
		/* return true if no errors, false if there are errors */
		return (error == 0);
	}
	
	/**
	 * Sends an debug message to the console
	 * @param message The message that should be printed to the console
	 */
	public void debug(String message) {
		if(this.debug) {
			this.getLogger().info("Debug: " + message);
		}
	}
	
	/**
	 * Reload the config of the plugin
	 */
	public void reload() {
		this.saveDefaultConfig();
		this.reloadConfig();
		configOk = this.checkConfig();
		chatprefix = this.config().getString("chatPrefix");
		languageManager = new LanguageManager(this);
		shopManager.checkRents();
	}
	
}




