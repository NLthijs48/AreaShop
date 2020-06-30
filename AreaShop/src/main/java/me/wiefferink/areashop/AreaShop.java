package me.wiefferink.areashop;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import me.wiefferink.areashop.interfaces.AreaShopInterface;
import me.wiefferink.areashop.interfaces.BukkitInterface;
import me.wiefferink.areashop.interfaces.WorldEditInterface;
import me.wiefferink.areashop.interfaces.WorldGuardInterface;
import me.wiefferink.areashop.listeners.PlayerLoginLogoutListener;
import me.wiefferink.areashop.managers.CommandManager;
import me.wiefferink.areashop.managers.FeatureManager;
import me.wiefferink.areashop.managers.FileManager;
import me.wiefferink.areashop.managers.Manager;
import me.wiefferink.areashop.managers.SignLinkerManager;
//import me.wiefferink.areashop.tools.Analytics;
//import me.wiefferink.areashop.tools.GithubUpdateCheck;
import me.wiefferink.areashop.tools.Utils;
import me.wiefferink.bukkitdo.Do;
import me.wiefferink.interactivemessenger.processing.Message;
import me.wiefferink.interactivemessenger.source.LanguageManager;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main class for the AreaShop plugin.
 * Contains methods to get parts of the plugins functionality and definitions for constants.
 */
public final class AreaShop extends JavaPlugin implements AreaShopInterface {
	// Statically available instance
	private static AreaShop instance = null;

	// General variables
	private WorldGuardPlugin worldGuard = null;
	private WorldGuardInterface worldGuardInterface = null;
	private WorldEditPlugin worldEdit = null;
	private WorldEditInterface worldEditInterface = null;
	private BukkitInterface bukkitInterface = null;
	private FileManager fileManager = null;
	private LanguageManager languageManager = null;
	private CommandManager commandManager = null;
	private SignLinkerManager signLinkerManager = null;
	private FeatureManager featureManager = null;
	private Set<Manager> managers = null;
	private boolean debug = false;
	private List<String> chatprefix = null;
	private boolean ready = false;
	// private GithubUpdateCheck githubUpdateCheck = null;

	// Folders and file names
	public static final String languageFolder = "lang";
	public static final String schematicFolder = "schem";
	public static final String regionsFolder = "regions";
	public static final String groupsFile = "groups.yml";
	public static final String defaultFile = "default.yml";
	public static final String configFile = "config.yml";
	public static final String configFileHidden = "hiddenConfig.yml";
	public static final String versionFile = "versions";

	// Euro tag for in the config
	public static final String currencyEuro = "%euro%";

	// Constants for handling file versions
	public static final String versionFiles = "files";
	public static final int versionFilesCurrent = 3;

	// Keys for replacing parts of flags, commands, strings
	public static final String tagPlayerName = "player";
	public static final String tagPlayerUUID = "uuid";
	public static final String tagWorldName = "world";
	public static final String tagRegionName = "region";
	public static final String tagRegionType = "type";
	public static final String tagPrice = "price";
	public static final String tagRawPrice = "rawprice";
	public static final String tagDuration = "duration";
	public static final String tagRentedUntil = "until";
	public static final String tagRentedUntilShort = "untilshort";
	public static final String tagWidth = "width"; // x-axis
	public static final String tagHeight = "height"; // y-axis
	public static final String tagDepth = "depth"; // z-axis
	public static final String tagVolume = "volume"; // Number of blocks in the region (accounting for polygon regions)
	public static final String tagTimeLeft = "timeleft";
	public static final String tagClicker = "clicker";
	public static final String tagResellPrice = "resellprice";
	public static final String tagRawResellPrice = "rawresellprice";
	public static final String tagFriends = "friends";
	public static final String tagFriendsUUID = "friendsuuid";
	public static final String tagMoneyBackPercentage = "moneybackpercent";
	public static final String tagMoneyBackAmount = "moneyback";
	public static final String tagRawMoneyBackAmount = "rawmoneyback";
	public static final String tagTimesExtended = "timesExtended";
	public static final String tagMaxExtends = "maxextends";
	public static final String tagExtendsLeft = "extendsleft";
	public static final String tagMaxRentTime = "maxrenttime";
	public static final String tagMaxInactiveTime = "inactivetime";
	public static final String tagLandlord = "landlord";
	public static final String tagLandlordUUID = "landlorduuid";
	public static final String tagDateTime = "datetime";
	public static final String tagDateTimeShort = "datetimeshort";
	public static final String tagYear = "year";
	public static final String tagMonth = "month";
	public static final String tagDay = "day";
	public static final String tagHour = "hour";
	public static final String tagMinute = "minute";
	public static final String tagSecond = "second";
	public static final String tagMillisecond = "millisecond";
	public static final String tagEpoch = "epoch";
	public static final String tagTeleportX = "tpx";
	public static final String tagTeleportY = "tpy";
	public static final String tagTeleportZ = "tpz";
	public static final String tagTeleportBlockX = "tpblockx";
	public static final String tagTeleportBlockY = "tpblocky";
	public static final String tagTeleportBlockZ = "tpblockz";
	public static final String tagTeleportPitch = "tppitch";
	public static final String tagTeleportYaw = "tpyaw";
	public static final String tagTeleportPitchRound = "tppitchround";
	public static final String tagTeleportYawRound = "tpyawround";
	public static final String tagTeleportWorld = "tpworld";

	public static AreaShop getInstance() {
		return AreaShop.instance;
	}

	/**
	 * Called on start or reload of the server.
	 */
	@Override
	public void onEnable() {
		AreaShop.instance = this;
		Do.init(this);
		managers = new HashSet<>();
		boolean error = false;

		// Find WorldEdit integration version to load
		String weVersion = null;
		String rawWeVersion = null;
		String weBeta = null;
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldEdit");
		if(!(plugin instanceof WorldEditPlugin) || !plugin.isEnabled()) {
			error("WorldEdit plugin is not present or has not loaded correctly");
			error = true;
		} else {
			worldEdit = (WorldEditPlugin)plugin;
			rawWeVersion = worldEdit.getDescription().getVersion();

			// Find beta version
			Pattern pattern = Pattern.compile("beta-?\\d+");
			Matcher matcher = pattern.matcher(rawWeVersion);
			if (matcher.find()) {
				weBeta = matcher.group();
			}

			// Get correct WorldEditInterface (handles things that changed version to version)
			if(worldEdit.getDescription().getVersion().startsWith("5.")) {
				weVersion = "5";
			} else if(worldEdit.getDescription().getVersion().startsWith("6.")) {
				weVersion = "6";
			} else if (worldEdit.getDescription().getVersion().startsWith("7.0.0") && "beta-01".equalsIgnoreCase(weBeta)) {
				weVersion = "7_beta_1";
			} else if (worldEdit.getDescription().getVersion().startsWith("7.0.0") && "beta-04".equalsIgnoreCase(weBeta)) {
				// beta-02 and beta-03 also have the new vector system already
				weVersion = "7_beta_4";
			} else if (worldEdit.getDescription().getVersion().startsWith("7.2.0")) {
				weVersion = "7_2_0_beta";
			}

			weVersion = "WorldEditHandler" + weVersion;
		}

		// Find WorldGuard integration version to load
		String wgVersion = null;
		String rawWgVersion = null;
		int major = 0;
		int minor = 0;
		int fixes = 0;
		Integer build = null;
		plugin = getServer().getPluginManager().getPlugin("WorldGuard");
		if(!(plugin instanceof WorldGuardPlugin) || !plugin.isEnabled()) {
			error("WorldGuard plugin is not present or has not loaded correctly");
			error = true;
		} else {
			worldGuard = (WorldGuardPlugin)plugin;
			// Get correct WorldGuardInterface (handles things that changed version to version)
			try {
				rawWgVersion = worldGuard.getDescription().getVersion();
				if(rawWgVersion.contains("-SNAPSHOT;")) {
					String buildNumber = rawWgVersion.substring(rawWgVersion.indexOf("-SNAPSHOT;") + 10);
					if(buildNumber.contains("-")) {
						buildNumber = buildNumber.substring(0, buildNumber.indexOf('-'));
						if (Utils.isNumeric(buildNumber)) {
							build = Integer.parseInt(buildNumber);
						} else {
							warn("Could not correctly parse the build of WorldGuard, raw version: " + rawWgVersion + ", buildNumber: " + buildNumber);
						}
					}
				}
				// Clear stuff from the version string that is not a number
				String[] versionParts = rawWgVersion.split("\\.");
				for(int i = 0; i < versionParts.length; i++) {
					Pattern pattern = Pattern.compile("^\\d+");
					Matcher matcher = pattern.matcher(versionParts[i]);
					if(matcher.find()) {
						versionParts[i] = matcher.group();
					}
				}
				// Find major, minor and fix numbers
				try {
					if(versionParts.length > 0) {
						major = Integer.parseInt(versionParts[0]);
					}
					if(versionParts.length > 1) {
						minor = Integer.parseInt(versionParts[1]);
					}
					if(versionParts.length > 2) {
						fixes = Integer.parseInt(versionParts[2]);
					}
				} catch(NumberFormatException e) {
					warn("Something went wrong while parsing WorldGuard version number: " + rawWgVersion);
				}

				// Determine correct implementation to use
				if(rawWgVersion.startsWith("5.")) {
					wgVersion = "5";
				} else if(major == 6 && minor == 1 && fixes < 3) {
					wgVersion = "6";
				} else if(major == 6) {
					if(build != null && build == 1672) {
						error = true;
						error("Build 1672 of WorldGuard is broken, update to a later build or a stable version!");
					} else if(build != null && build < 1672) {
						wgVersion = "6";
					} else {
						wgVersion = "6_1_3";
					}
				} else if (worldGuard.getDescription().getVersion().startsWith("7.0.0") && "beta-01".equalsIgnoreCase(weBeta)) {
					// When using WorldEdit beta-01, we need to use the WorldGuard variant with the old vector system
					wgVersion = "7_beta_1";
				} else if (worldGuard.getDescription().getVersion().startsWith("7.0.0") && "beta-02".equalsIgnoreCase(weBeta)) {
					// Even though the WorldGuard file is called beta-02, the reported version is still beta-01!
					wgVersion = "7_beta_2";
				} else {
					wgVersion = "7_0_4_beta1";
				}
			} catch(Exception e) { // If version detection fails, at least try to load the latest version
				warn("Parsing the WorldGuard version failed, assuming version 7_beta_2:", rawWgVersion);
				wgVersion = "7_beta_2";
			}

			wgVersion = "WorldGuardHandler" + wgVersion;
		}

		// Check if FastAsyncWorldEdit is installed
		boolean fawe;
		try {
			Class.forName("com.boydti.fawe.Fawe" );
			fawe = true;
		} catch (ClassNotFoundException ignore) {
			fawe = false;
		}

		if (fawe) {
			boolean useNewIntegration = true;
			List<String> standardIntegrationVersions = Arrays.asList("1.7", "1.8", "1.9", "1.10", "1.11", "1.12");
			for(String standardIntegrationVersion : standardIntegrationVersions) {
				String version = Bukkit.getBukkitVersion();
				// Detects '1.8', '1.8.3', '1.8-pre1' style versions
				if(version.equals(standardIntegrationVersion)
						|| version.startsWith(standardIntegrationVersion + ".")
						|| version.startsWith(standardIntegrationVersion + "-")) {
					useNewIntegration = false;
					break;
				}
			}

			if (useNewIntegration) {
				weVersion = "FastAsyncWorldEditHandler";
				wgVersion = "FastAsyncWorldEditWorldGuardHandler";
			}
		}

		// Load WorldEdit
		try {
			Bukkit.getLogger().log(Level.INFO, "Version Debug: " + weVersion); //WorldEditHandler7_beta_1
			Bukkit.getLogger().log(Level.INFO, "Beta Debug: " + rawWgVersion); //WorldEditHandler7_beta_1
			Class<?> clazz = Class.forName("me.wiefferink.areashop.handlers." + weVersion);
			// Check if we have a NMSHandler class at that location.
			if(WorldEditInterface.class.isAssignableFrom(clazz)) { // Make sure it actually implements WorldEditInterface
				worldEditInterface = (WorldEditInterface)clazz.getConstructor(AreaShopInterface.class).newInstance(this); // Set our handler
			}
		} catch(Exception e) {
			error("Could not load the handler for WorldEdit (tried to load " + weVersion + "), report this problem to the author: " + ExceptionUtils.getStackTrace(e));
			error = true;
			weVersion = null;
		}

		// Load WorldGuard
		try {
			Bukkit.getLogger().log(Level.INFO, "Version Debug: " + wgVersion); //WorldGuardHandler7_beta_1
			Bukkit.getLogger().log(Level.INFO, "Raw Version Debug: " + rawWgVersion); //7.0.4-beta1
			Class<?> clazz = Class.forName("me.wiefferink.areashop.handlers." + wgVersion);
			// Check if we have a NMSHandler class at that location.
			if(WorldGuardInterface.class.isAssignableFrom(clazz)) { // Make sure it actually implements WorldGuardInterface
				worldGuardInterface = (WorldGuardInterface)clazz.getConstructor(AreaShopInterface.class).newInstance(this); // Set our handler
			}
		} catch(Exception e) {
			error("Could not load the handler for WorldGuard (tried to load " + wgVersion + "), report this problem to the author:" + ExceptionUtils.getStackTrace(e));
			error = true;
			wgVersion = null;
		}

		// Load Bukkit implementation
		String bukkitHandler;
		try {
			Class.forName("org.bukkit.block.data.type.WallSign");
			bukkitHandler = "1_13";
		} catch (ClassNotFoundException e) {
			bukkitHandler = "1_12";
		}

		try {
			Class<?> clazz = Class.forName("me.wiefferink.areashop.handlers.BukkitHandler" + bukkitHandler);
			bukkitInterface = (BukkitInterface)clazz.getConstructor(AreaShopInterface.class).newInstance(this);
		} catch (Exception e) {
			error("Could not load the Bukkit handler (used for sign updates), tried to load:", bukkitHandler + ", report this problem to the author:", ExceptionUtils.getStackTrace(e));
			error = true;
			bukkitHandler = null;
		}

		// Check if Vault is present
		if(getServer().getPluginManager().getPlugin("Vault") == null) {
			error("Vault plugin is not present or has not loaded correctly");
			error = true;
		}

		// Load all data from files and check versions
		fileManager = new FileManager();
		managers.add(fileManager);
		boolean loadFilesResult = fileManager.loadFiles(false);
		error = error || !loadFilesResult;

		// Print loaded version of WG and WE in debug
		if(wgVersion != null) {
			AreaShop.debug("Loaded ", wgVersion, "(raw version:" + rawWgVersion + ", major:" + major + ", minor:" + minor + ", fixes:" + fixes + ", build:" + build + ", fawe:" + fawe + ")");
		}
		if(weVersion != null) {
			AreaShop.debug("Loaded ", weVersion, "(raw version:" + rawWeVersion + ", beta:" + weBeta + ")");
		}
		if(bukkitHandler != null) {
			AreaShop.debug("Loaded BukkitHandler", bukkitHandler);
		}

		setupLanguageManager();

		if(error) {
			error("The plugin has not started, fix the errors listed above");
		} else {
			featureManager = new FeatureManager();
			managers.add(featureManager);

			// Register the event listeners
			getServer().getPluginManager().registerEvents(new PlayerLoginLogoutListener(this), this);

			setupTasks();

			// Startup the CommandManager (registers itself for the command)
			commandManager = new CommandManager();
			managers.add(commandManager);

			// Create a signLinkerManager
			signLinkerManager = new SignLinkerManager();
			managers.add(signLinkerManager);

			// Enable Metrics if config allows it
			// if(getConfig().getBoolean("sendStats")) {
			//	 Analytics.start();
			// }

			// Register dynamic permission (things declared in config)
			registerDynamicPermissions();

			// Don't initialize the updatechecker if disabled in the config
			//			if(getConfig().getBoolean("checkForUpdates")) {
			//				githubUpdateCheck = new GithubUpdateCheck(
			//						AreaShop.getInstance(),
			//						"NLThijs48",
			//						"AreaShop"
			//				).withVersionComparator((latestVersion, currentVersion) ->
			//						!cleanVersion(latestVersion).equals(cleanVersion(currentVersion))
			//				).checkUpdate(result -> {
			//					AreaShop.debug("Update check result:", result);
			//					if(!result.hasUpdate()) {
			//						return;
			//					}
			//
			//					AreaShop.info("Update from AreaShop V" + cleanVersion(result.getCurrentVersion()) + " to AreaShop V" + cleanVersion(result.getLatestVersion()) + " available, get the latest version at https://www.spigotmc.org/resources/areashop.2991/");
			//					for(Player player : Utils.getOnlinePlayers()) {
			//						notifyUpdate(player);
			//					}
			//				});
			//			}
		}
	}

	//	/**
	//	 * Notify a player about an update if he wants notifications about it and an update is available.
	//	 * @param sender CommandSender to notify
	//	 */
	//	public void notifyUpdate(CommandSender sender) {
	//		if(githubUpdateCheck != null && githubUpdateCheck.hasUpdate() && sender.hasPermission("areashop.notifyupdate")) {
	//			AreaShop.getInstance().message(sender, "update-playerNotify", cleanVersion(githubUpdateCheck.getCurrentVersion()), cleanVersion(githubUpdateCheck.getLatestVersion()));
	//		}
	//	}

	/**
	 * Cleanup a version number.
	 * @param version Version to clean
	 * @return Cleaned up version (removed prefixes and suffixes)
	 */
	private String cleanVersion(String version) {
		version = version.toLowerCase();

		// Strip 'v' as used on Github tags
		if(version.startsWith("v")) {
			version = version.substring(1);
		}

		// Strip build number as used by Jenkins
		if(version.contains("#")) {
			version = version.substring(0, version.indexOf("#"));
		}

		return version;
	}

	/**
	 * Called on shutdown or reload of the server.
	 */
	@Override
	public void onDisable() {

		Bukkit.getServer().getScheduler().cancelTasks(this);

		// Cleanup managers
		for(Manager manager : managers) {
			manager.shutdown();
		}
		managers = null;
		fileManager = null;
		languageManager = null;
		commandManager = null;
		signLinkerManager = null;
		featureManager = null;

		// Cleanup plugins
		worldGuard = null;
		worldGuardInterface = null;
		worldEdit = null;
		worldEditInterface = null;

		// Cleanup other stuff
		chatprefix = null;
		debug = false;
		ready = false;

		HandlerList.unregisterAll(this);
	}

	/**
	 * Indicates if the plugin is ready to be used.
	 * @return true if the plugin is ready, false otherwise
	 */
	public boolean isReady() {
		return ready;
	}

	/**
	 * Set if the plugin is ready to be used or not (not to be used from another plugin!).
	 * @param ready Indicate if the plugin is ready to be used
	 */
	public void setReady(boolean ready) {
		this.ready = ready;
	}

	/**
	 * Set if the plugin should output debug messages (loaded from config normally).
	 * @param debug Indicates if the plugin should output debug messages or not
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * Setup a new LanguageManager.
	 */
	private void setupLanguageManager() {
		languageManager = new LanguageManager(
				this,
				languageFolder,
				getConfig().getString("language"),
				"EN",
				chatprefix
		);
	}

	/**
	 * Set the chatprefix to use in the chat (loaded from config normally).
	 * @param chatprefix The string to use in front of chat messages (supports formatting codes)
	 */
	public void setChatprefix(List<String> chatprefix) {
		this.chatprefix = chatprefix;
	}

	/**
	 * Function to get the WorldGuard plugin.
	 * @return WorldGuardPlugin
	 */
	@Override
	public WorldGuardPlugin getWorldGuard() {
		return worldGuard;
	}

	/**
	 * Function to get WorldGuardInterface for version dependent things.
	 * @return WorldGuardInterface
	 */
	public WorldGuardInterface getWorldGuardHandler() {
		return this.worldGuardInterface;
	}

	/**
	 * Get the RegionManager.
	 * @param world World to get the RegionManager for
	 * @return RegionManager for the given world, if there is one, otherwise null
	 */
	public RegionManager getRegionManager(World world) {
		return this.worldGuardInterface.getRegionManager(world);
	}

	/**
	 * Function to get the WorldEdit plugin.
	 * @return WorldEditPlugin
	 */
	@Override
	public WorldEditPlugin getWorldEdit() {
		return worldEdit;
	}

	/**
	 * Function to get WorldGuardInterface for version dependent things.
	 * @return WorldGuardInterface
	 */
	public WorldEditInterface getWorldEditHandler() {
		return this.worldEditInterface;
	}

	/**
	 * Function to get the LanguageManager.
	 * @return the LanguageManager
	 */
	public LanguageManager getLanguageManager() {
		return languageManager;
	}

	/**
	 * Get the BukkitHandler, for sign interactions.
	 * @return BukkitHandler
	 */
	public BukkitInterface getBukkitHandler() {
		return this.bukkitInterface;
	}

	/**
	 * Function to get the CommandManager.
	 * @return the CommandManager
	 */
	public CommandManager getCommandManager() {
		return commandManager;
	}

	/**
	 * Get the SignLinkerManager.
	 * Handles sign linking mode.
	 * @return The SignLinkerManager
	 */
	public SignLinkerManager getSignlinkerManager() {
		return signLinkerManager;
	}

	/**
	 * Get the FeatureManager.
	 * Manages region specific features.
	 * @return The FeatureManager
	 */
	public FeatureManager getFeatureManager() {
		return featureManager;
	}

	/**
	 * Function to get the Vault plugin.
	 * @return Economy
	 */
	public Economy getEconomy() {
		RegisteredServiceProvider<Economy> economy = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if(economy == null || economy.getProvider() == null) {
			error("There is no economy provider to support Vault, make sure you installed an economy plugin");
			return null;
		}
		return economy.getProvider();
	}

	/**
	 * Get the Vault permissions provider.
	 * @return Vault permissions provider
	 */
	public net.milkbowl.vault.permission.Permission getPermissionProvider() {
		RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider == null || permissionProvider.getProvider() == null) {
			return null;
		}
		return permissionProvider.getProvider();
	}

	/**
	 * Check for a permission of a (possibly offline) player.
	 * @param offlinePlayer OfflinePlayer to check
	 * @param permission Permission to check
	 * @return true if the player has the permission, false if the player does not have permission or, is offline and there is not Vault-compatible permission plugin
	 */
	public boolean hasPermission(OfflinePlayer offlinePlayer, String permission) {
		// Online, return through Bukkit
		if(offlinePlayer.getPlayer() != null) {
			return offlinePlayer.getPlayer().hasPermission(permission);
		}

		// Resolve while offline if possible
		net.milkbowl.vault.permission.Permission permissionProvider = getPermissionProvider();
		if(permissionProvider != null) {
			// TODO: Should we provide a world here?
			return permissionProvider.playerHas(null, offlinePlayer, permission);
		}

		// Player offline and no offline permission provider available, safely say that there is no permission
		return false;
	}

	/**
	 * Method to get the FileManager (loads/save regions and can be used to get regions).
	 * @return The fileManager
	 */
	public FileManager getFileManager() {
		return fileManager;
	}

	/**
	 * Register dynamic permissions controlled by config settings.
	 */
	private void registerDynamicPermissions() {
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
					warn("Could not add the following permission to be used as limit: " + perm.getName());
				}
			}
		}
		Bukkit.getPluginManager().recalculatePermissionDefaults(Bukkit.getPluginManager().getPermission("playerwarps.limits"));
	}

	/**
	 * Register all required tasks.
	 */
	private void setupTasks() {
		// Rent expiration timer
		long expirationCheck = Utils.millisToTicks(Utils.getDurationFromSecondsOrString("expiration.delay"));
		final AreaShop finalPlugin = this;
		if(expirationCheck > 0) {
			Do.syncTimer(expirationCheck, () -> {
				if(isReady()) {
					finalPlugin.getFileManager().checkRents();
					AreaShop.debugTask("Checking rent expirations...");
				} else {
					AreaShop.debugTask("Skipped checking rent expirations, plugin not ready");
				}
			});
		}

		// Inactive unrenting/selling timer
		long inactiveCheck = Utils.millisToTicks(Utils.getDurationFromMinutesOrString("inactive.delay"));
		if(inactiveCheck > 0) {
			Do.syncTimer(inactiveCheck, () -> {
				if(isReady()) {
					finalPlugin.getFileManager().checkForInactiveRegions();
					AreaShop.debugTask("Checking for regions with players that are inactive too long...");
				} else {
					AreaShop.debugTask("Skipped checking for regions of inactive players, plugin not ready");
				}
			});
		}

		// Periodic updating of signs for timeleft tags
		long periodicUpdate = Utils.millisToTicks(Utils.getDurationFromSecondsOrString("signs.delay"));
		if(periodicUpdate > 0) {
			Do.syncTimer(periodicUpdate, () -> {
				if(isReady()) {
					finalPlugin.getFileManager().performPeriodicSignUpdate();
					AreaShop.debugTask("Performing periodic sign update...");
				} else {
					AreaShop.debugTask("Skipped performing periodic sign update, plugin not ready");
				}
			});
		}

		// Saving regions and group settings
		long saveFiles = Utils.millisToTicks(Utils.getDurationFromMinutesOrString("saving.delay"));
		if(saveFiles > 0) {
			Do.syncTimer(saveFiles, () -> {
				if(isReady()) {
					finalPlugin.getFileManager().saveRequiredFiles();
					AreaShop.debugTask("Saving required files...");
				} else {
					AreaShop.debugTask("Skipped saving required files, plugin not ready");
				}
			});
		}

		// Sending warnings about rent regions to online players
		long expireWarning = Utils.millisToTicks(Utils.getDurationFromMinutesOrString("expireWarning.delay"));
		if(expireWarning > 0) {
			Do.syncTimer(expireWarning, () -> {
				if(isReady()) {
					finalPlugin.getFileManager().sendRentExpireWarnings();
					AreaShop.debugTask("Sending rent expire warnings...");
				} else {
					AreaShop.debugTask("Skipped sending rent expire warnings, plugin not ready");
				}
			});
		}

		// Update all regions on startup
		if(getConfig().getBoolean("updateRegionsOnStartup")) {
			Do.syncLater(20, () -> {
				finalPlugin.getFileManager().updateAllRegions();
				AreaShop.debugTask("Updating all regions at startup...");
			});
		}
	}

	/**
	 * Send a message to a target without a prefix.
	 * @param target       The target to send the message to
	 * @param key          The key of the language string
	 * @param replacements The replacements to insert in the message
	 */
	public void messageNoPrefix(Object target, String key, Object... replacements) {
		Message.fromKey(key).replacements(replacements).send(target);
	}

	/**
	 * Send a message to a target, prefixed by the default chat prefix.
	 * @param target       The target to send the message to
	 * @param key          The key of the language string
	 * @param replacements The replacements to insert in the message
	 */
	public void message(Object target, String key, Object... replacements) {
		Message.fromKey(key).prefix().replacements(replacements).send(target);
	}


	/**
	 * Return the config.
	 */
	@Override
	public YamlConfiguration getConfig() {
		return fileManager.getConfig();
	}

	/**
	 * Sends an debug message to the console.
	 * @param message The message that should be printed to the console
	 */
	public static void debug(Object... message) {
		if(AreaShop.getInstance().debug) {
			info("Debug: " + StringUtils.join(message, " "));
		}
	}

	/**
	 * Non-static debug to use as implementation of the interface.
	 * @param message Object parts of the message that should be logged, toString() will be used
	 */
	@Override
	public void debugI(Object... message) {
		AreaShop.debug(StringUtils.join(message, " "));
	}

	/**
	 * Print an information message to the console.
	 * @param message The message to print
	 */
	public static void info(Object... message) {
		AreaShop.getInstance().getLogger().info(StringUtils.join(message, " "));
	}

	/**
	 * Print a warning to the console.
	 * @param message The message to print
	 */
	public static void warn(Object... message) {
		AreaShop.getInstance().getLogger().warning(StringUtils.join(message, " "));
	}

	/**
	 * Print an error to the console.
	 * @param message The message to print
	 */
	public static void error(Object... message) {
		AreaShop.getInstance().getLogger().severe(StringUtils.join(message, " "));
	}

	/**
	 * Print debug message for periodic task.
	 * @param message The message to print
	 */
	public static void debugTask(Object... message) {
		if(AreaShop.getInstance().getConfig().getBoolean("debugTask")) {
			AreaShop.debug(StringUtils.join(message, " "));
		}
	}

	/**
	 * Reload all files of the plugin and update all regions.
	 * @param confirmationReceiver The CommandSender which should be notified when complete, null for nobody
	 */
	public void reload(final CommandSender confirmationReceiver) {
		setReady(false);
		fileManager.saveRequiredFilesAtOnce();
		fileManager.loadFiles(true);
		setupLanguageManager();
		message(confirmationReceiver, "reload-reloading");
		fileManager.checkRents();
		fileManager.updateAllRegions(confirmationReceiver);
	}

	/**
	 * Reload all files of the plugin and update all regions.
	 */
	public void reload() {
		reload(null);
	}

}




