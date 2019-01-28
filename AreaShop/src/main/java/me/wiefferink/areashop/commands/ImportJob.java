package me.wiefferink.areashop.commands;

import com.google.common.base.Charsets;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.events.ask.AddingRegionEvent;
import me.wiefferink.areashop.features.signs.RegionSign;
import me.wiefferink.areashop.features.signs.SignsFeature;
import me.wiefferink.areashop.regions.BuyRegion;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.regions.RegionGroup;
import me.wiefferink.areashop.regions.RentRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ImportJob {

	private final AreaShop plugin;
	private final CommandSender sender;

	/**
	 * Create and execute the import.
	 * @param sender CommandSender that should receive progress updates
	 */
	public ImportJob(CommandSender sender) {
		this.sender = sender;
		this.plugin = AreaShop.getInstance();
		execute();
	}

	/**
	 * Execute the job.
	 */
	private void execute() {
		// Check for RegionForSale data
		File regionForSaleFolder = new File(plugin.getDataFolder().getParentFile().getAbsolutePath(), "RegionForSale");
		if(!regionForSaleFolder.exists()) {
			message("import-noPluginFolder", regionForSaleFolder.getName());
			return;
		}

		File worldsFolder = new File(regionForSaleFolder.getAbsolutePath(), "worlds");
		if(!worldsFolder.exists()) {
			message("import-noWorldsFolder");
			return;
		}

		File[] worldFolders = worldsFolder.listFiles();
		if(worldFolders == null) {
			message("import-noWorldsFolder");
			return;
		}

		// Import data for each world
		message("import-start");

		// Group with settings for all imported regions
		RegionGroup regionForSaleGroup = new RegionGroup(plugin, "RegionForSale");
		plugin.getFileManager().addGroup(regionForSaleGroup);

		// Import /RegionForSale/config.yml settings
		File regionForSaleConfigFile = new File(regionForSaleFolder.getAbsolutePath(), "config.yml");
		YamlConfiguration regionForSaleConfig = loadConfiguration(regionForSaleConfigFile);
		if(regionForSaleConfig == null) {
			messageNoPrefix("import-loadConfigFailed", regionForSaleConfigFile.getAbsolutePath());
			regionForSaleConfig = new YamlConfiguration();
		} else {
			importRegionSettings(regionForSaleConfig, regionForSaleGroup.getSettings(), null, false);
			regionForSaleGroup.setSetting("priority", 0);
		}

		// Import /RegionForSale/general.yml settings
		File regionForSaleGeneralFile = new File(regionForSaleFolder.getAbsolutePath(), "config.yml");
		YamlConfiguration regionForSaleGeneral = loadConfiguration(regionForSaleConfigFile);
		if(regionForSaleGeneral == null) {
			messageNoPrefix("import-loadConfigFailed", regionForSaleGeneralFile.getAbsolutePath());
		} else {
			// Collection interval of RegionForSale maps to rent duration
			String duration = "1 day";
			if(regionForSaleGeneral.isLong("interval.collect_money")) {
				duration = minutesToString(regionForSaleGeneral.getLong("interval.collect_money"));
			}
			regionForSaleGroup.setSetting("rent.duration", duration);

			// Global economy account has an effect close to landlord in AreaShop
			if(regionForSaleGeneral.isString("global_econ_account")) {
				regionForSaleGroup.setSetting("general.landlordName", regionForSaleGeneral.getString("global_econ_account"));
			}
		}
		regionForSaleGroup.saveRequired();

		////////// Handle defaults of RegionForSale

		// Set autoExtend, to keep the same behavior as RegionForSale had
		regionForSaleGroup.setSetting("rent.autoExtend", true);

		// Import regions from each world
		for(File worldFolder : worldFolders) {
			// Skip files
			if(!worldFolder.isDirectory() || worldFolder.isHidden()) {
				continue;
			}

			messageNoPrefix("import-doWorld", worldFolder.getName());

			// Get the Bukkit world
			World world = Bukkit.getWorld(worldFolder.getName());
			if(world == null) {
				messageNoPrefix("import-noBukkitWorld");
				continue;
			}

			// Get the WorldGuard RegionManager
			RegionManager regionManager = plugin.getRegionManager(world);
			if(regionManager == null) {
				messageNoPrefix("import-noRegionManger");
				continue;
			}

			// Load the /worlds/<world>/regions.yml file
			File regionsFile = new File(worldFolder.getAbsolutePath(), "regions.yml");
			YamlConfiguration regions = loadConfiguration(regionsFile);
			if(regions == null) {
				messageNoPrefix("import-loadRegionsFailed", regionsFile.getAbsolutePath());
				continue;
			}

			// Load /worlds/<world>/config.yml file
			File worldConfigFile = new File(worldFolder.getAbsolutePath(), "config.yml");
			YamlConfiguration worldConfig = loadConfiguration(worldConfigFile);
			if(worldConfig == null) {
				messageNoPrefix("import-loadWorldConfigFailed", worldConfigFile.getAbsolutePath());
				// Simply skip importing the settings, since this is not really fatal
				worldConfig = new YamlConfiguration();
			} else {
				// RegionGroup with all world settings
				RegionGroup worldGroup = new RegionGroup(plugin, "RegionForSale-" + worldFolder.getName());
				importRegionSettings(worldConfig, worldGroup.getSettings(), null, false);
				worldGroup.setSetting("priority", 1);
				worldGroup.addWorld(worldFolder.getName());
				plugin.getFileManager().addGroup(regionForSaleGroup);
				worldGroup.saveRequired();
			}

			// Create groups to hold settings of /worlds/<world>/parent-regions.yml
			File parentRegionsFile = new File(worldFolder.getAbsolutePath(), "parent-regions.yml");
			YamlConfiguration parentRegions = loadConfiguration(parentRegionsFile);
			if(parentRegions == null) {
				messageNoPrefix("import-loadParentRegionsFailed", parentRegionsFile.getAbsolutePath());
				// Non-fatal, so just continue
			} else {
				for(String parentRegionName : parentRegions.getKeys(false)) {
					// Get WorldGuard region
					ProtectedRegion worldGuardRegion = regionManager.getRegion(parentRegionName);
					if(worldGuardRegion == null) {
						messageNoPrefix("import-noWorldGuardRegionParent", parentRegionName);
						continue;
					}

					// Get settings section
					ConfigurationSection parentRegionSection = parentRegions.getConfigurationSection(parentRegionName);
					if(parentRegionSection == null) {
						messageNoPrefix("import-improperParentRegion", parentRegionName);
						continue;
					}

					// Skip if it does not have any settings
					if(parentRegionSection.getKeys(false).isEmpty()) {
						continue;
					}

					// Import parent region settings into a RegionGroup
					RegionGroup parentRegionGroup = new RegionGroup(plugin, "RegionForSale-" + worldFolder.getName() + "-" + parentRegionName);
					importRegionSettings(parentRegionSection, parentRegionGroup.getSettings(), null, false);
					parentRegionGroup.setSetting("priority", 2 + parentRegionSection.getLong("info.priority", 0));
					parentRegionGroup.saveRequired();

					// TODO add all regions that are contained in this parent region
					// Utils.getWorldEditRegionsInSelection()
				}
			}

			// Read and import regions
			for(String regionKey : regions.getKeys(false)) {
				GeneralRegion existingRegion = plugin.getFileManager().getRegion(regionKey);
				if(existingRegion != null) {
					if(world.getName().equalsIgnoreCase(existingRegion.getWorldName())) {
						messageNoPrefix("import-alreadyAdded", regionKey);
					} else {
						messageNoPrefix("import-alreadyAddedOtherWorld", regionKey, existingRegion.getWorldName(), world.getName());
					}
					continue;
				}

				ConfigurationSection regionSection = regions.getConfigurationSection(regionKey);
				if(regionSection == null) {
					messageNoPrefix("import-invalidRegionSection", regionKey);
					continue;
				}

				// Get WorldGuard region
				ProtectedRegion worldGuardRegion = regionManager.getRegion(regionKey);
				if(worldGuardRegion == null) {
					messageNoPrefix("import-noWorldGuardRegion", regionKey);
					continue;
				}

				String owner = regionSection.getString("info.owner", null);
				boolean isBought = regionSection.getBoolean("info.is-bought");
				// TODO: should also take into config settings of parent regions
				boolean rentable = regionSection.getBoolean("economic-settings.rentable", worldConfig.getBoolean("economic-settings.rentable", regionForSaleConfig.getBoolean("economic-settings.rentable")));
				boolean buyable = regionSection.getBoolean("economic-settings.buyable", worldConfig.getBoolean("economic-settings.buyable", regionForSaleConfig.getBoolean("economic-settings.buyable")));

				// Can be bought and rented, import as buy
				if(buyable && rentable) {
					messageNoPrefix("import-buyAndRent", regionKey);
				}

				// Cannot be bought or rented, skip
				if(!buyable && !rentable && owner == null) {
					messageNoPrefix("import-noBuyAndNoRent", regionKey);
					continue;
				}

				// Create region
				GeneralRegion region;
				if(rentable || (owner != null && !isBought)) {
					region = new RentRegion(regionKey, world);
				} else {
					region = new BuyRegion(regionKey, world);
				}
				AddingRegionEvent event = plugin.getFileManager().addRegion(region);
				if (event.isCancelled()) {
					messageNoPrefix("general-cancelled", event.getReason());
					continue;
				}

				// Import settings
				importRegionSettings(regionSection, region.getConfig(), region, !buyable && !rentable);
				region.getConfig().set("general.importedFrom", "RegionForSale");

				// Get existing owners and members
				List<UUID> existing = new ArrayList<>();
				if(owner != null) {
					@SuppressWarnings("deprecation")
					OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owner);
					if(offlinePlayer != null) {
						existing.add(offlinePlayer.getUniqueId());
					}
				}
				for(UUID uuid : plugin.getWorldGuardHandler().getOwners(worldGuardRegion).asUniqueIdList()) {
					if(!existing.contains(uuid)) {
						existing.add(uuid);
					}
				}
				for(UUID uuid : plugin.getWorldGuardHandler().getMembers(worldGuardRegion).asUniqueIdList()) {
					if(!existing.contains(uuid)) {
						existing.add(uuid);
					}
				}

				// First owner (or if none, the first member) will be the renter/buyer
				if(!existing.isEmpty()) {
					region.setOwner(existing.remove(0));
				}
				// Add others as friends
				for(UUID friend : existing) {
					region.getFriendsFeature().addFriend(friend, null);
				}

				region.saveRequired();

				messageNoPrefix("import-imported", regionKey);
			}
		}

		// Update all regions
		plugin.getFileManager().updateAllRegions(sender);
		// Write all imported regions and settings to disk
		plugin.getFileManager().saveRequiredFiles();
	}

	/**
	 * Load a YamlConfiguration from disk using UTF-8 encoding.
	 * @param from File to read the configuration from
	 * @return YamlConfiguration if the file exists and got read wihout problems, otherwise null
	 */
	private YamlConfiguration loadConfiguration(File from) {
		try(
				InputStreamReader reader = new InputStreamReader(new FileInputStream(from), Charsets.UTF_8)
		) {
			return YamlConfiguration.loadConfiguration(reader);
		} catch(IOException e) {
			return null;
		}
	}

	/**
	 * Import region specific settings from a RegionForSale source to an AreaShop target ConfigurationSection.
	 * @param from RegionForSale config section that specifies region settings
	 * @param to AreaShop config section that specifies region settings
	 * @param region GeneralRegion to copy settings to, or null if doing generic settings
	 * @param permanent Region cannot be rented or bought, disables some features
	 */
	private void importRegionSettings(ConfigurationSection from, ConfigurationSection to, GeneralRegion region, boolean permanent) {
		// Maximum rental time, TODO check if this is actually the same
		if(from.isLong("permissions.max-rent-time")) {
			to.set("rent.maxRentTime", minutesToString(from.getLong("permissions.max-rent-time")));
		}

		// Region rebuild
		if(from.getBoolean("region-rebuilding.auto-rebuild")) {
			to.set("general.enableRestore", true);
		}

		// Get price settings
		String unit = from.getString("economic-settings.unit-type");
		String rentPrice = from.getString("economic-settings.cost-per-unit.rent");
		String buyPrice = from.getString("economic-settings.cost-per-unit.buy");
		String sellPrice = from.getString("economic-settings.cost-per-unit.selling-price");
		// TODO: There is no easy way to import this, setup eventCommandsProfile?
		// String taxes = from.getString("economic-settings.cost-per-unit.taxes");

		// Determine unit and add that to the price
		String unitSuffix = "";
		if("region".equalsIgnoreCase(unit)) {
			// add nothing
		} else if("m3".equalsIgnoreCase(unit)) {
			unitSuffix = "*%volume%";
		} else { // m2 or nothing (in case not set, we should actually look in parent files to correctly set this...)
			unitSuffix = "*(%volume%/%height%)"; // This is better than width*depth because of polygon regions
		}

		// Apply settings
		if(rentPrice != null) {
			to.set("rent.price", rentPrice + unitSuffix);
		}
		if(buyPrice != null) {
			to.set("buy.price", buyPrice + unitSuffix);
			if(sellPrice != null) {
				try {
					double buyPriceAmount = Double.parseDouble(buyPrice);
					double sellPriceAmount = Double.parseDouble(sellPrice);
					to.set("buy.moneyBack", sellPriceAmount / buyPriceAmount * 100);
				} catch(NumberFormatException e) {
					// There is not always a region here for the message, should probably indicate something though
					message("import-moneyBackFailed", buyPrice, sellPrice);
				}
			}
		}

		// Apply permanent region settings
		if(permanent) {
			to.set("buy.resellDisabled", true);
			to.set("buy.sellDisabled", true);
			to.set("general.countForLimits", false);
		}

		// Set rented until
		if(from.isLong("info.last-withdrawal")
				&& region instanceof RentRegion) {
			RentRegion rentRegion = (RentRegion)region;
			long lastWithdrawal = from.getLong("info.last-withdrawal");
			// Because the rental duration is already imported into the region and its parents this should be correct
			rentRegion.setRentedUntil(lastWithdrawal + rentRegion.getDuration());
		}

		// Import signs (list of strings like "297, 71, -22")
		if(from.isList("info.signs") && region != null) {
			for(String signLocation : from.getStringList("info.signs")) {
				String[] locationParts = signLocation.split(", ");
				if(locationParts.length != 3) {
					message("import-invalidSignLocation", region.getName(), signLocation);
					continue;
				}

				// Parse the location
				Location location;
				try {
					location = new Location(region.getWorld(), Double.parseDouble(locationParts[0]), Double.parseDouble(locationParts[1]), Double.parseDouble(locationParts[2]));
				} catch(NumberFormatException e) {
					message("import-invalidSignLocation", region.getName(), signLocation);
					continue;
				}

				// Check if this location is already added to a region
				RegionSign regionSign = SignsFeature.getSignByLocation(location);
				if(regionSign != null) {
					if(!regionSign.getRegion().equals(region)) {
						message("import-signAlreadyAdded", region.getName(), signLocation, regionSign.getRegion().getName());
					}
					continue;
				}

				// SignType and Facing will be written when the sign is updated later
				region.getSignsFeature().addSign(location, null, null, null);
			}
		}

	}

	private static class TimeUnit {
		public final long minutes;
		public final String identifier;

		TimeUnit(long minutes, String identifier) {
			this.minutes = minutes;
			this.identifier = identifier;
		}
	}

	private static final List<TimeUnit> timeUnitLookup = new ArrayList<TimeUnit>() {
		{
			add(new TimeUnit(60 * 24 * 30 * 12, "year"));
			add(new TimeUnit(60 * 24 * 30, "month"));
			add(new TimeUnit(60 * 24, "day"));
			add(new TimeUnit(60, "hour"));
		}
	};

	/**
	 * Convert minutes to a human-readable string.
	 * @param minutes Value to convert
	 * @return String that represents the same length of time in a readable format, like "1 day", "5 minutes", "3 months"
	 */
	private String minutesToString(long minutes) {
		// If the specified number of minutes can map nicely to a higher unit, use that one
		String resultUnit = "minute";
		long resultValue = minutes;
		for(TimeUnit unit : timeUnitLookup) {
			long result = minutes / unit.minutes;
			if(resultValue * unit.minutes == minutes) {
				resultUnit = unit.identifier;
				resultValue = result;
				break;
			}
		}
		return resultValue + " " + resultUnit + (resultValue == 1 ? "" : "s");
	}

	/**
	 * Send a message to a target without a prefix.
	 * @param key          The key of the language string
	 * @param replacements The replacements to insert in the message
	 */
	public void messageNoPrefix(String key, Object... replacements) {
		plugin.messageNoPrefix(sender, key, replacements);

		if(!(sender instanceof ConsoleCommandSender)) {
			plugin.messageNoPrefix(Bukkit.getConsoleSender(), key, replacements);
		}
	}

	/**
	 * Send a message to a target, prefixed by the default chat prefix.
	 * @param key          The key of the language string
	 * @param replacements The replacements to insert in the message
	 */
	public void message(String key, Object... replacements) {
		plugin.message(sender, key, replacements);

		if(!(sender instanceof ConsoleCommandSender)) {
			plugin.message(Bukkit.getConsoleSender(), key, replacements);
		}
	}
}
