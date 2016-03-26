package nl.evolutioncoding.areashop;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.milkbowl.vault.economy.Economy;
import nl.evolutioncoding.areashop.Updater.UpdateResult;
import nl.evolutioncoding.areashop.Updater.UpdateType;
import nl.evolutioncoding.areashop.features.DebugFeature;
import nl.evolutioncoding.areashop.features.Feature;
import nl.evolutioncoding.areashop.features.SignDisplayFeature;
import nl.evolutioncoding.areashop.features.WorldGuardRegionFlagsFeature;
import nl.evolutioncoding.areashop.interfaces.AreaShopInterface;
import nl.evolutioncoding.areashop.interfaces.WorldEditInterface;
import nl.evolutioncoding.areashop.interfaces.WorldGuardInterface;
import nl.evolutioncoding.areashop.listeners.PlayerLoginLogoutListener;
import nl.evolutioncoding.areashop.listeners.SignBreakListener;
import nl.evolutioncoding.areashop.listeners.SignChangeListener;
import nl.evolutioncoding.areashop.listeners.SignClickListener;
import nl.evolutioncoding.areashop.managers.CommandManager;
import nl.evolutioncoding.areashop.managers.FileManager;
import nl.evolutioncoding.areashop.managers.LanguageManager;
import nl.evolutioncoding.areashop.managers.SignLinkerManager;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Main class for the AreaShop plugin
 * @author NLThijs48
 */
public final class AreaShop extends JavaPlugin implements AreaShopInterface {
	// General variables
	private static AreaShop instance = null;
	
	private WorldGuardPlugin worldGuard = null;
	private WorldGuardInterface worldGuardInterface = null;
	private WorldEditPlugin worldEdit = null;
	private WorldEditInterface worldEditInterface = null;
	private FileManager fileManager = null;
	private LanguageManager languageManager = null;
	private CommandManager commandManager = null;
	private SignLinkerManager signLinkerManager = null;
	private boolean debug = false;
	private String chatprefix = null;
	private Updater updater = null;
	private boolean updateAvailable = false;
	private boolean ready = false;
	
	// Folders and file names
	public static final String languageFolder = "lang";
	public static final String schematicFolder = "schem";
	public static final String schematicExtension = ".schematic";	
	public static final String regionsFolder = "regions";
	public static final String groupsFile = "groups.yml";
	public static final String defaultFile = "default.yml";	
	public static final String configFile = "config.yml";	
	public static final String versionFile = "versions";
	
	// Euro tag for in the config
	public static final String currencyEuro = "%euro%";
	
	// Constants for handling file versions
	public static final String versionFiles = "files";
	public static final int versionFilesCurrent = 3;
	
	// Keys for replacing parts of flags, commands, strings
	public static final String tagPlayerName = "%player%";
	public static final String tagPlayerUUID = "%uuid%";
	public static final String tagWorldName = "%world%";
	public static final String tagRegionName = "%region%";
	public static final String tagRegionType = "%type%";
	public static final String tagPrice = "%price%";
	public static final String tagRawPrice = "%rawprice%";
	public static final String tagDuration = "%duration%";
	public static final String tagRentedUntil = "%until%";
	public static final String tagRentedUntilShort = "%untilshort%";
	public static final String tagWidth = "%width%"; // x-axis
	public static final String tagHeight = "%height%"; // y-axis
	public static final String tagDepth = "%depth%"; // z-axis
	public static final String tagTimeLeft = "%timeleft%";
	public static final String tagClicker = "%clicker%";
	public static final String tagResellPrice = "%resellprice%";
	public static final String tagRawResellPrice = "%rawresellprice%";
	public static final String tagFriends = "%friends%";
	public static final String tagFriendsUUID = "%friendsuuid%";
	public static final String tagMoneyBackPercentage = "%moneybackpercent%";
	public static final String tagMoneyBackAmount = "%moneyback%";
	public static final String tagRawMoneyBackAmount = "%rawmoneyback%";
	public static final String tagMaxExtends = "%maxextends%";
	public static final String tagExtendsLeft = "%extendsleft%";
	public static final String tagMaxRentTime = "%maxrenttime%";
	public static final String tagMaxInactiveTime = "%inactivetime%";
	public static final String tagLandlord = "%landlord%";
	public static final String tagLandlordUUID = "%landlorduuid%";
	public static final String tagDateTime = "%datetime%";
	public static final String tagDateTimeShort = "%datetimeshort%";
	public static final String tagYear = "%year%";
	public static final String tagMonth = "%month%";
	public static final String tagDay = "%day%";
	public static final String tagHour = "%hour%";
	public static final String tagMinute = "%minute%";
	public static final String tagSecond = "%second%";
	public static final String tagMillisecond = "%millisecond%";
	public static final String tagEpoch = "%epoch%";
	
	public static AreaShop getInstance() {
		return AreaShop.instance;
	}
	
	/**
	 * Called on start or reload of the server
	 */
	public void onEnable() {
		AreaShop.instance = this;
		boolean error = false;
		
		// Check if WorldGuard is present
		String wgVersion = null;
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	    	this.getLogger().severe("WorldGuard plugin is not present or has not loaded correctly");
	    	error = true;
	    } else {
		    worldGuard = (WorldGuardPlugin)plugin;
		    // Get correct WorldGuardInterface (handles things that changed version to version)
	        if(worldGuard.getDescription().getVersion().startsWith("5.")) {
	        	wgVersion = "5";
	        } else {
		        wgVersion = "6";
	        }
	        try {
	            final Class<?> clazz = Class.forName("nl.evolutioncoding.areashop.handlers.WorldGuardHandler" + wgVersion);
	            // Check if we have a NMSHandler class at that location.
	            if (WorldGuardInterface.class.isAssignableFrom(clazz)) { // Make sure it actually implements WorldGuardInterface
	                this.worldGuardInterface = (WorldGuardInterface) clazz.getConstructor(AreaShopInterface.class).newInstance(this); // Set our handler
	            }
	        } catch (final Exception e) {
	            e.printStackTrace();
	            this.getLogger().severe("Could not load the handler for WorldGuard (tried to load " + wgVersion + "), report this problem to the author.");
	            error = true;
	            wgVersion = null;
	        }
	    }
 
		// Check if WorldEdit is present
		String weVersion = null;
		plugin = getServer().getPluginManager().getPlugin("WorldEdit");
	    if (plugin == null || !(plugin instanceof WorldEditPlugin)) {
	    	this.getLogger().severe("WorldEdit plugin is not present or has not loaded correctly");
	    	error = true;
	    } else {
		    worldEdit = (WorldEditPlugin)plugin;
		    // Get correct WorldEditInterface (handles things that changed version to version)
	        if(worldEdit.getDescription().getVersion().startsWith("5.")) {
	        	weVersion = "5";
	        } else {
		        weVersion = "6";
	        }
	        try {
	            final Class<?> clazz = Class.forName("nl.evolutioncoding.areashop.handlers.WorldEditHandler" + weVersion);
	            // Check if we have a NMSHandler class at that location.
	            if (WorldEditInterface.class.isAssignableFrom(clazz)) { // Make sure it actually implements WorldEditInterface
	                this.worldEditInterface = (WorldEditInterface) clazz.getConstructor(AreaShopInterface.class).newInstance(this); // Set our handler
	            }
	        } catch (final Exception e) {
	            e.printStackTrace();
	            this.getLogger().severe("Could not load the handler for WorldEdit (tried to load " + weVersion + "), report this problem to the author.");
	            error = true;
	            weVersion = null;
	        }
	    }

	    // Check if Vault is present
	    if(getServer().getPluginManager().getPlugin("Vault") == null) {
	    	this.getLogger().severe("Vault plugin is not present or has not loaded correctly");
        	error = true;
	    }
        
		// Load all data from files and check versions
	    fileManager = new FileManager(this);
	    error = error || !fileManager.loadFiles();
	    
	    // Print loaded version of WG and WE in debug
	    if(wgVersion != null) {
	    	AreaShop.debug("Loaded WorldGuardHandler" + wgVersion);
	    }
	    if(weVersion != null) {
	    	AreaShop.debug("Loaded WorldEditHandler" + weVersion);
	    }
        
	    // Create a LanguageMananager
	    languageManager = new LanguageManager(this);
	    
		if(error) {
			this.getLogger().severe("The plugin has not started, fix the errors listed above");
		} else {
			// Register the event listeners
			this.getServer().getPluginManager().registerEvents(new SignChangeListener(this), this);
			this.getServer().getPluginManager().registerEvents(new SignBreakListener(this), this);
			this.getServer().getPluginManager().registerEvents(new SignClickListener(this), this);			
			this.getServer().getPluginManager().registerEvents(new PlayerLoginLogoutListener(this), this);

			setupFeatures();
			setupTasks();
	        
		    // Startup the CommandManager (registers itself for the command)
	        commandManager = new CommandManager(this);
	        
	        // Create a signLinkerManager
	        signLinkerManager = new SignLinkerManager(this);
			
	        // Enable Metrics if config allows it
			if(this.getConfig().getBoolean("sendStats")) {
				this.startMetrics();
			}
			
			// Register dynamic permission (things declared in config)
			registerDynamicPermissions();
			
			// Dont initialize the updatechecker if disabled in the config
			if(this.getConfig().getBoolean("checkForUpdates")) {
		        new BukkitRunnable() {
					@Override
					public void run() {
						try {
							updater = new Updater(AreaShop.getInstance(), 76518, null, UpdateType.NO_DOWNLOAD, false);
							AreaShop.debug("Result=" + updater.getResult().toString() + ", Latest=" + updater.getLatestName() + ", Type=" + updater.getLatestType());
							updateAvailable = updater.getResult() == UpdateResult.UPDATE_AVAILABLE;
							if(updateAvailable) {
								AreaShop.getInstance().getLogger().info("Update from AreaShop V" + AreaShop.getInstance().getDescription().getVersion() + " to " + updater.getLatestName() + " available, get the latest version at http://dev.bukkit.org/bukkit-plugins/regionbuyandrent/");
								new BukkitRunnable() {
									@Override
									public void run() {
										for(Player player : Utils.getOnlinePlayers()) {
											if(player.hasPermission("areashop.notifyupdate")) {
												AreaShop.getInstance().message(player, "update-playerNotify", AreaShop.getInstance().getDescription().getVersion(), AreaShop.getInstance().getUpdater().getLatestName());	
											}
										}
									}								
								}.runTask(AreaShop.getInstance());
							}
						} catch(Exception e) {
							AreaShop.debug("Something went wrong with the Updater:");
							AreaShop.debug(e.getMessage());
							updateAvailable = false;
						}
					}
		        }.runTaskAsynchronously(this);
			}
		}
	}
	
	/**
	 *  Called on shutdown or reload of the server 
	 */
	public void onDisable() {
		// Update lastactive time for players that are online now
		for(GeneralRegion region : fileManager.getRegions()) {
			Player player = Bukkit.getPlayer(region.getOwner());
			if(player != null) {
				region.updateLastActiveTime();
			}
		}
		fileManager.saveRequiredFilesAtOnce();		
		Bukkit.getServer().getScheduler().cancelTasks(this);
		
		worldGuard = null;
		worldEdit = null;
		fileManager = null;
		languageManager = null;
		commandManager = null;
		chatprefix = null;
		debug = false;
		ready = false;
		updater = null;
	}

	/**
	 * Instanciate and register all Feature classes
	 */
	public void setupFeatures() {
		Set<Feature> features = new HashSet<>();

		features.add(new DebugFeature(this));
		features.add(new SignDisplayFeature(this));
		features.add(new WorldGuardRegionFlagsFeature(this));

		// Register as listener when necessary
		for(Feature feature : features) {
			if(feature instanceof Listener) {
				this.getServer().getPluginManager().registerEvents((Listener)feature, this);
			}
		}
	}
	
	/**
	 * Indicates if the plugin is ready to be used
	 * @return true if the plugin is ready, false otherwise
	 */
	public boolean isReady() {
		return ready;
	}
	
	/**
	 * Set if the plugin is ready to be used or not (not to be used from another plugin!)
	 * @param ready Indicate if the plugin is ready to be used
	 */
	public void setReady(boolean ready) {
		this.ready = ready;
	}
	
	/**
	 * Set if the plugin should output debug messages (loaded from config normally)
	 * @param debug Indicates if the plugin should output debug messages or not
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	/**
	 * Set the chatprefix to use in the chat (loaded from config normally)
	 * @param chatprefix The string to use in front of chat messages (supports formatting codes like &1)
	 */
	public void setChatprefix(String chatprefix) {
		this.chatprefix = chatprefix;
	}
 
	/**
	 * Function to get the WorldGuard plugin
	 * @return WorldGuardPlugin
	 */
	public WorldGuardPlugin getWorldGuard() {
	    return worldGuard;
	}
	
	/**
	 * Function to get WorldGuardInterface for version dependent things
	 * @return WorldGuardInterface
	 */
	public WorldGuardInterface getWorldGuardHandler() {
		return this.worldGuardInterface;
	}
	
	/**
	 * Function to get the WorldEdit plugin
	 * @return WorldEditPlugin
	 */
	public WorldEditPlugin getWorldEdit() {
	    return worldEdit;
	}
	
	/**
	 * Function to get WorldGuardInterface for version dependent things
	 * @return WorldGuardInterface
	 */
	public WorldEditInterface getWorldEditHandler() {
		return this.worldEditInterface;
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
	
	public SignLinkerManager getSignlinkerManager() {
		return signLinkerManager;
	}
	
	/**
	 * Function to get the Vault plugin
	 * @return Economy
	 */
	public Economy getEconomy() {
		RegisteredServiceProvider<Economy> economy = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economy == null || economy.getProvider() == null) {
        	this.getLogger().severe("There is no economy provider to support Vault, make sure you installed an economy plugin");
        	return null;
        }		
	    return economy.getProvider();
	}	
	
	/**
	 * Get the current chatPrefix
	 * @return The current chatPrefix
	 */
	public String getChatPrefix() {
		return chatprefix;
	}
	
	/**
	 * Method to get the FileManager (loads/save regions and can be used to get regions)
	 * @return The fileManager
	 */
	public FileManager getFileManager() {
		return fileManager;
	}
	
	/**
	 * Get the updater (check the update result)
	 * @return The updater
	 */
	public Updater getUpdater() {
		return updater;
	}
	
	/**
	 * Check if an update for AreaShop is available
	 * @return true if an update is available, otherwise false
	 */
	public boolean updateAvailable() {
		return updateAvailable;
	}
	
	/**
	 * Register dynamic permissions controlled by config settings
	 */
	public void registerDynamicPermissions() {
		// Register limit groups of amount of regions a player can have
		ConfigurationSection section = getConfig().getConfigurationSection("limitGroups");
		if(section == null) {
			return;
		}
		for(String group : section.getKeys(false)) {
			if(!"default".equals(group)) {
				Permission perm = new Permission("areashop.limits." + group);
				try {
					Bukkit.getPluginManager().addPermission(perm);
				} catch(IllegalArgumentException e) {
					this.getLogger().warning("Could not add the following permission to be used as limit: " + perm.getName());
				}
			}
		}	
		Bukkit.getPluginManager().recalculatePermissionDefaults(Bukkit.getPluginManager().getPermission("playerwarps.limits"));
	}
	
	/**
	 * Register all required tasks
	 */
	public void setupTasks() {
        // Rent expiration timer
		long expirationCheck = Utils.millisToTicks(Utils.getDurationFromSecondsOrString("expiration.delay"));
		final AreaShop finalPlugin = this;
		if(expirationCheck > 0) {
	        new BukkitRunnable() {
				@Override
				public void run() {
					if(isReady()) {
						finalPlugin.getFileManager().checkRents();
						AreaShop.debugTask("Checking rent expirations...");
					} else {
						AreaShop.debugTask("Skipped checking rent expirations, plugin not ready");
					}
				}
	        }.runTaskTimer(this, 1, expirationCheck);
        }
	    // Inactive unrenting/selling timer
		long inactiveCheck = Utils.millisToTicks(Utils.getDurationFromMinutesOrString("inactive.delay"));
		if(inactiveCheck > 0) {
			new BukkitRunnable() {
				@Override
				public void run() {
					if(isReady()) {
						finalPlugin.getFileManager().checkForInactiveRegions();
						AreaShop.debugTask("Checking for regions with players that are inactive too long...");
					} else {
						AreaShop.debugTask("Skipped checking for regions of inactive players, plugin not ready");
					}
				}
	        }.runTaskTimer(this, inactiveCheck, inactiveCheck);	     
        }	        
	    // Periodic updating of signs for timeleft tags
		long periodicUpdate = Utils.millisToTicks(Utils.getDurationFromSecondsOrString("signs.delay"));
		if(periodicUpdate > 0) {
			new BukkitRunnable() {
				@Override
				public void run() {
					if(isReady()) {
						finalPlugin.getFileManager().performPeriodicSignUpdate();
						AreaShop.debugTask("Performing periodic sign update...");
					} else {
						AreaShop.debugTask("Skipped performing periodic sign update, plugin not ready");
					}
				}
	        }.runTaskTimer(this, periodicUpdate, periodicUpdate);	     
        }
        // Saving regions and group settings
		long saveFiles = Utils.millisToTicks(Utils.getDurationFromMinutesOrString("saving.delay"));
		if(saveFiles > 0) {
			new BukkitRunnable() {
				@Override
				public void run() {
					if(isReady()) {
						finalPlugin.getFileManager().saveRequiredFiles();
						AreaShop.debugTask("Saving required files...");
					} else {
						AreaShop.debugTask("Skipped saving required files, plugin not ready");
					}
				}
	        }.runTaskTimer(this, saveFiles, saveFiles);	     
        }
        // Sending warnings about rent regions to online players
		long expireWarning = Utils.millisToTicks(Utils.getDurationFromMinutesOrString("expireWarning.delay"));
		if(expireWarning > 0) {
			new BukkitRunnable() {
				@Override
				public void run() {
					if(isReady()) {
						finalPlugin.getFileManager().sendRentExpireWarnings();
						AreaShop.debugTask("Sending rent expire warnings...");
					} else {
						AreaShop.debugTask("Skipped sending rent expire warnings, plugin not ready");
					}
				}
	        }.runTaskTimer(this, expireWarning, expireWarning);	     
        }
        // Update all regions on startup
        if(getConfig().getBoolean("updateRegionsOnStartup")) {
	        new BukkitRunnable() {
				@Override
				public void run() {
					finalPlugin.getFileManager().updateAllRegions();
					AreaShop.debugTask("Updating all regions at startup...");
				}
	        }.runTaskLater(this, 20L);	     
        }
	}
	
	/**
	 * Method to send a message to a CommandSender, using chatprefix if it is a player
	 * @param target The CommandSender you wan't to send the message to (e.g. a player)
	 * @param key The key to get the translation
	 * @param prefix Specify if the message should have a prefix
	 * @param params The parameters to inject into the message string
	 */
	public void configurableMessage(Object target, String key, boolean prefix, Object... params) {
		if(target == null) {
			return;
		}
		String langString = Utils.applyColors(languageManager.getLang(key, params));
		if(langString == null || langString.equals("")) {
			// Do nothing, message is not available or disabled
		} else {
			if(target instanceof Player) {
				if(prefix) {
					((Player)target).sendMessage(Utils.applyColors(chatprefix)+langString);
				} else {
					((Player)target).sendMessage(langString);
				}
			} else if(target instanceof CommandSender) {
				if(!getConfig().getBoolean("useColorsInConsole")) {
					langString = ChatColor.stripColor(langString);
				}
				((CommandSender)target).sendMessage(langString);
			}	
			else if(target instanceof Logger) {
				if(!getConfig().getBoolean("useColorsInConsole")) {
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
	 * Return the config
	 */
	@Override
	public YamlConfiguration getConfig() {
		return fileManager.getConfig();
	}
	
	/**
	 * Start the Metrics stats collection
	 */
	private void startMetrics() {
		try {
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		} catch (Exception e) {
		    AreaShop.debug("Could not start Metrics");
		}
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
	 * Non-static debug to use as implementation of the interface
	 */
	public void debugI(String message) {
		AreaShop.debug(message);
	}

	/**
	 * Print debug message for periodic task
	 * @param message The message to print
	 */
	public static void debugTask(String message) {
		if(AreaShop.getInstance().getConfig().getBoolean("debugTask")) {
			AreaShop.debug(message);
		}
	}

	/**
	 * Reload all files of the plugin and update all regions
	 * confirmationReceiver The CommandSender that should receive confirmation messages, null for nobody
	 */
	public void reload(final CommandSender confirmationReceiver) {
		setReady(false);
		fileManager.saveRequiredFilesAtOnce();
		fileManager.loadFiles();
		languageManager.startup();
		message(confirmationReceiver, "reload-reloading");
		fileManager.checkRents();
		fileManager.updateAllRegions(confirmationReceiver);
	}
	/**
	 * Reload all files of the plugin and update all regions
	 */
	public void reload() {
		reload(null);
	}
	
}




