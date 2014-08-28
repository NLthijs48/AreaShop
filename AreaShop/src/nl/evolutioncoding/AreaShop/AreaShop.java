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
import org.bukkit.permissions.Permission;
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
	public static final String regionsFolder = "regions";
	public static final String groupsFile = "groups.yml";
	public static final String defaultFile = "default.yml";	
	public static final String versionFile = "versions";
	
	/* Euro tag for in the config */
	public static final String currencyEuro = "%euro%";
	
	/* Constants for handling file versions */
	public static final String versionFiles = "files";
	public static final int versionFilesCurrent = 2;
	
	/* Keys for replacing parts of flags */
	public static final String tagPlayerName = "%player%";
	public static final String tagPlayerUUID = "%uuid%";
	public static final String tagWorldName = "%world%";
	public static final String tagRegionName = "%region%";
	public static final String tagRegionType = "%type%";
	public static final String tagPrice = "%price%";
	public static final String tagDuration = "%duration%";
	public static final String tagRentedUntil = "%until%";
	public static final String tagRentedUntilShort = "%untilshort%";
	
	public static AreaShop getInstance() {
		return AreaShop.instance;
	}
	
	/**
	 * Called on start or reload of the server
	 */
	public void onEnable(){
		AreaShop.instance = this;
		boolean error = false;
	
		/* Check the config, loads default if errors */
		debug = this.getConfig().getBoolean("debug");
		configOk = true;
		
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
	    error = error & !fileManager.loadFiles();
	    fileManager.checkRents();
		
		/* Save a copy of the default config.yml if one is not present */
		this.saveDefaultConfig();
	    
		if(error) {
			this.getLogger().info("The plugin has not started, fix the errors listed above");
		} else {
			// Register the event listeners
			this.getServer().getPluginManager().registerEvents(new SignChangeListener(this), this);
			this.getServer().getPluginManager().registerEvents(new SignBreakListener(this), this);
			this.getServer().getPluginManager().registerEvents(new SignClickListener(this), this);			
			
	        // Start thread for checking renting
	        int checkDelay = Integer.parseInt(this.config().getString("checkDelay"))*20;
	        new RentCheck(this).runTaskTimer(this, checkDelay, checkDelay);
		    
		    // Bind commands for this plugin
	        commandManager = new CommandManager(this);
			
	        // Enable Metrics if config allows it
			if(this.config().getBoolean("sendStats")) {
				this.startMetrics();
			}
			
			// Register dynamic permission (things declared in config)
			registerDynamicPermissions();
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
	 * Register dynamic permissions controlled by config settings
	 */
	public void registerDynamicPermissions() {
		// Register limit groups of amount of regions a player can have
		for(String group : config().getConfigurationSection("limitGroups").getKeys(false)) {
			if(!"unlimited".equals(group) && !"default".equals(group)) {
				Permission perm = new Permission("areashop.limits." + group);
				Bukkit.getPluginManager().addPermission(perm);
			}
		}	
		Bukkit.getPluginManager().recalculatePermissionDefaults(Bukkit.getPluginManager().getPermission("playerwarps.limits"));
	}
	
	/**
	 * Method to send a message to a CommandSender, using chatprefix if it is a player
	 * @param target The CommandSender you wan't to send the message to (e.g. a player)
	 * @param key The key to get the translation
	 * @param prefix Specify if the message should have a prefix
	 * @param params The parameters to inject into the message string
	 */
	public void configurableMessage(Object target, String key, boolean prefix, Object... params) {
		String langString = this.fixColors(languageManager.getLang(key, params));
		if(langString == null) {
			this.getLogger().info("Something is wrong with the language file, could not find key: " + key);
		} else if(langString.equals("")) {
			// Do nothing, message is disabled
		} else {
			if(target instanceof Player) {
				if(prefix) {
					((Player)target).sendMessage(this.fixColors(chatprefix) + langString);
				} else {
					((Player)target).sendMessage(langString);
				}
			} else if(target instanceof CommandSender) {
				if(!config().getBoolean("useColorsInConsole")) {
					langString = ChatColor.stripColor(langString);
				}
				((CommandSender)target).sendMessage(langString);
			}	
			else if(target instanceof Logger) {
				if(!config().getBoolean("useColorsInConsole")) {
					langString = ChatColor.stripColor(langString);
				}
				((Logger)target).info(langString);
			} else {
				langString = ChatColor.stripColor(langString);
				this.getLogger().info("Could not send message, target is wrong: " + langString);
			}
		}
	}
	public void messageNoPrefix(Object target, String key, Object... params) {
		configurableMessage(target, key, false, params);
	}
	public void message(Object target, String key, Object... params) {
		configurableMessage(target, key, true, params);
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
	
	/**
	 * Start the Metrics stats collection
	 */
	private void startMetrics() {
		try {
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		} catch (IOException e) {
		    AreaShop.debug("Could not start Metrics");
		}
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
		configOk = true;
		chatprefix = this.config().getString("chatPrefix");
		debug = this.getConfig().getBoolean("debug");
		languageManager = new LanguageManager(this);
		fileManager.loadFiles();
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




