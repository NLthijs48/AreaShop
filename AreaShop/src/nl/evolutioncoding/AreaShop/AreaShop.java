package nl.evolutioncoding.AreaShop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
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

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

/**
 * Main class for the AreaShop plugin
 * @author NLThijs48
 */
public final class AreaShop extends JavaPlugin {
	/* General variables */
	private static AreaShop instance = null;
	
	private WorldGuardPlugin worldGuard = null;
	private WorldEditPlugin worldEdit = null;
	private Economy economy = null;
	private FileManager fileManager = null;
	private LanguageManager languageManager = null;
	private CommandManager commandManager = null;
	private boolean configOk = false;
	private boolean debug = false;
	private String chatprefix = null;
	
	/* Folders and file names */
	public static final String languageFolder = "lang";
	public static final String schematicFolder = "schem";
	public static final String schematicExtension = ".schematic";	
	public static final String rentsFile = "rents";
	public static final String buysFile = "buys";
	public static final String versionFile = "versions";
	
	/* Constants for handling file versions */
	public static final String versionRentKey = "rents";
	public static final int versionRentCurrent = 1;
	public static final String versionBuyKey = "buys";
	public static final int versionBuyCurrent = 1;
	
	/* Euro tag for in the config */
	public static final String currencyEuro = "%euro%";
	
	/* Keys for adding things to the hashmap */
	public static final String keyWorld = "world";
	public static final String keyX = "x";
	public static final String keyY = "y";
	public static final String keyZ = "z";
	public static final String keyTPX = "tpx";
	public static final String keyTPY = "tpy";
	public static final String keyTPZ = "tpz";
	public static final String keyTPPitch = "tppitch";
	public static final String keyTPYaw = "tpyaw";
	public static final String keyDuration = "duration";
	public static final String keyPrice = "price";
	public static final String oldKeyPlayer = "player";
	public static final String keyPlayerUUID = "playeruuid";
	public static final String keyRentedUntil = "rented";
	public static final String keyName = "name";
	public static final String keyRestore = "restore";
	public static final String keySchemProfile = "profile";
	
	/* Keys for replacing parts of flags */
	public static final String tagPlayerName = "%player%";
	public static final String tagRegionName = "%region%";
	public static final String tagPrice = "%price%";
	public static final String tagDuration = "%duration%";
	public static final String tagRentedUntil = "%until%";
	
	/* Enum for schematic event types */
	// TODO delete
	public enum RegionEventType {		
		CREATED("Created"),
		DELETED("Deleted"),
		BOUGHT("Bought"),
		SOLD("Sold");
		
		private final String value;
		private RegionEventType(String value) {
			this.value = value;
		}
		public String getValue() {
			return value;
		}
	} 
	
	/* Enum for region types */
	public enum RegionType {
		SELL("sell"),
		RENT("rent");
		
		private final String value;
		private RegionType(String value) {
			this.value = value;
		}
		public String getValue() {
			return value;
		}
	}	
	
	public static AreaShop getInstance() {
		return AreaShop.instance;
	}
	
	/**
	 * Called on start or reload of the server
	 */
	public void onEnable(){
		AreaShop.instance = this;
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
	    
		/* Check if WorldEdit is present */
		plugin = getServer().getPluginManager().getPlugin("WorldEdit");
	    if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
	    	this.getLogger().info("Error: WorldEdit plugin is not present or has not loaded correctly");
	    	error = true;
	    } else {
		    worldEdit = (WorldEditPlugin)plugin;
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

		/* Load all data from files and check versions */
	    fileManager = new FileManager(this);
	    error = error & !fileManager.loadRents();
	    fileManager.checkRents();
	    error = error & !fileManager.loadBuys();
	    
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
	        commandManager = new CommandManager(this);
			
			if(this.config().getBoolean("sendStats")) {
				this.startMetrics();
			}
			
		}
	}
	
	/**
	 *  Called on shutdown or reload of the server 
	 */
	public void onDisable() {
		
		/* set variables to null to prevent memory leaks */
		worldGuard = null;
		economy = null;
		fileManager = null;
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
	 * Function to get the WorldEdit plugin
	 * @return WorldEditPlugin
	 */
	public WorldEditPlugin getWorldEdit() {
	    return worldEdit;
	}
	
	/**
	 * Function to get the LanguageManager
	 * @return the LanguageManager
	 */
	public LanguageManager getLanguageManager() {
	    return languageManager;
	}
	
	/**
	 * Function to get the CommandManager
	 * @return the CommandManager
	 */
	public CommandManager getCommandManager() {
		return commandManager;
	}
	
	/**
	 * Function to get the Vault plugin
	 * @return Economy
	 */
	public Economy getEconomy() {
	    return economy;
	}		
	
	/**
	 * Method to get the FileManager
	 * @return The fileManager
	 */
	public FileManager getFileManager() {
		return fileManager;
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
		} else if(langString.equals("")) {
			// Do nothing, message is disabled
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
	 * Format the currency amount with the characters before and after
	 * @return Currency character format string
	 * @param amount Amount of money to format
	 */
	public String formatCurrency(String amount) {
		String before = this.config().getString("moneyCharacter");
		before = before.replace(currencyEuro, "\u20ac");
		String after = this.config().getString("moneyCharacterAfter");
		after = after.replace(currencyEuro, "\u20ac");
		return before + amount + after;
	}
	public String formatCurrency(double amount) {
		return this.formatCurrency("" + amount);
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
	
	private void startMetrics() {
		try {
		    Metrics metrics = new Metrics(this);
		    // Number of rents rented/not rented
		    /*Graph rentGraph = metrics.createGraph("Rent regions");
		    rentGraph.addPlotter(new Metrics.Plotter("For rent") {
	            @Override
	            public int getValue() {
	    		    int result = 0;
	    		    for(String rent : fileManager.getRents().keySet()) {
	    		    	if(fileManager.getRent(rent).get(keyPlayer) == null) {
	    		    		result++;
	    		    	}
	    		    }
                    return result;
	            }
		    });
		    rentGraph.addPlotter(new Metrics.Plotter("Rented") {
	            @Override
	            public int getValue() {
	    		    int result = 0;
	    		    for(String rent : fileManager.getRents().keySet()) {
	    		    	if(fileManager.getRent(rent).get(keyPlayer) != null) {
	    		    		result++;
	    		    	}
	    		    }
                    return result;
	            }
		    });
		    // Number of buys bought/not bought
		    Graph buyGraph = metrics.createGraph("Buy regions");
		    buyGraph.addPlotter(new Metrics.Plotter("For sale") {
	            @Override
	            public int getValue() {
	    		    int result = 0;
	    		    for(String buy : fileManager.getBuys().keySet()) {
	    		    	if(fileManager.getBuy(buy).get(keyPlayer) == null) {
	    		    		result++;
	    		    	}
	    		    }
                    return result;
	            }
		    });
		    buyGraph.addPlotter(new Metrics.Plotter("Sold") {
	            @Override
	            public int getValue() {
	    		    int result = 0;
	    		    for(String buy : fileManager.getBuys().keySet()) {
	    		    	if(fileManager.getBuy(buy).get(keyPlayer) != null) {
	    		    		result++;
	    		    	}
	    		    }
                    return result;
	            }
		    });
		    */
		    metrics.start();
		} catch (IOException e) {
		    AreaShop.debug("Could not start Metrics");
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
	 * Checks if the string is a correct time period
	 * @param time String that has to be checked
	 * @return true if format is correct, false if not
	 */
	public boolean checkTimeFormat(String time) {
		/* Check if the string is not empty and check the length */
		if(time == null || time.length() <= 1 || time.indexOf(' ') == -1 || time.indexOf(' ') >= (time.length()-1)) {
			return false;
		}
		
		/* Check if the suffix is one of these values */
		String suffix = time.substring(time.indexOf(' ')+1, time.length());
		ArrayList<String> identifiers = new ArrayList<String>();
		identifiers.addAll(this.config().getStringList("minutes"));
		identifiers.addAll(this.config().getStringList("hours"));
		identifiers.addAll(this.config().getStringList("days"));
		identifiers.addAll(this.config().getStringList("months"));
		identifiers.addAll(this.config().getStringList("years"));
		if(!identifiers.contains(suffix)) {
			return false;
		}
		
		/* check if the part before the space is a number */
		String prefix = time.substring(0, (time.indexOf(' ')));
		return prefix.matches("\\d+");
	}
	
	/**
	 * Sends an debug message to the console
	 * @param message The message that should be printed to the console
	 */
	public static void debug(String message) {
		if(AreaShop.getInstance().debug) {
			AreaShop.getInstance().getLogger().info("Debug: " + message);
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
		fileManager.checkRents();
	}
	
	/**
	 * Conversion to name by uuid
	 * @param uuid The uuid in string format
	 * @return the name of the player
	 */
	public String toName(String uuid) {
		if(uuid == null) {
			return null;
		} else {
			return this.toName(UUID.fromString(uuid));			
		}
	}
	/**
	 * Conversion to name by uuid object
	 * @param uuid The uuid in string format
	 * @return the name of the player
	 */
	public String toName(UUID uuid) {
		if(uuid == null) {
			return null;
		} else {
			return Bukkit.getOfflinePlayer(uuid).getName();			
		}
	}
	
	/**
	 * Conversion from name to uuid
	 * @param name The name of the player
	 * @return The uuid of the player
	 */
	@SuppressWarnings("deprecation") // Fake deprecation by Bukkit to inform developers, method will stay
	public String toUUID(String name) {
		if(name == null) {
			return null;
		} else {
			return Bukkit.getOfflinePlayer(name).getUniqueId().toString();
		}
		
	}
	
}




