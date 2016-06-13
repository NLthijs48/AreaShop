package me.wiefferink.areashop;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.wiefferink.areashop.messages.Message;
import me.wiefferink.areashop.regions.BuyRegion;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.regions.RentRegion;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Utils {

	// Not used
	private Utils() {}

	private static YamlConfiguration config;
	private static ArrayList<String> identifiers;
	private static ArrayList<String> seconds;
	private static ArrayList<String> minutes;
	private static ArrayList<String> hours;
	private static ArrayList<String> days;
	private static ArrayList<String> weeks;
	private static ArrayList<String> months;
	private static ArrayList<String> years;

	public static void initialize(YamlConfiguration pluginConfig) {
		config = pluginConfig;

		// Setup individual identifiers
		seconds = new ArrayList<>(config.getStringList("seconds"));
		minutes = new ArrayList<>(config.getStringList("minutes"));
		hours = new ArrayList<>(config.getStringList("hours"));
		days = new ArrayList<>(config.getStringList("days"));
		weeks = new ArrayList<>(config.getStringList("weeks"));
		months = new ArrayList<>(config.getStringList("months"));
		years = new ArrayList<>(config.getStringList("years"));
		// Setup all time identifiers
		identifiers = new ArrayList<>();
		identifiers.addAll(seconds);
		identifiers.addAll(minutes);
		identifiers.addAll(hours);
		identifiers.addAll(days);
		identifiers.addAll(weeks);
		identifiers.addAll(months);
		identifiers.addAll(years);
	}

	/**
	 * Create a message with a list of parts
	 * @param replacements The parts to use
	 * @param messagePart The message to use for the parts
	 * @return A Message object containing the parts combined into one message
	 */
	public static Message combinedMessage(Collection<?> replacements, String messagePart) {
		return combinedMessage(replacements, messagePart, ", ");
	}

	/**
	 * Create a message with a list of parts
	 * @param replacements The parts to use
	 * @param messagePart The message to use for the parts
	 * @param combiner     The string to use as combiner
	 * @return A Message object containing the parts combined into one message
	 */
	public static Message combinedMessage(Collection<?> replacements, String messagePart, String combiner) {
		Message result = Message.none();
		boolean first = true;
		for(Object part : replacements) {
			if(first) {
				first = false;
			} else {
				result.append(combiner);
			}
			result.append(Message.fromKey(messagePart).replacements(part));
		}
		return result;
	}

	/**
	 * Gets the online players
	 * Provides backwards compatibility for 1.7- where it returns an array
	 * @return Online players
	 */
	@SuppressWarnings("unchecked")
	public static Collection<? extends Player> getOnlinePlayers() {
		try {
			Method onlinePlayerMethod = Server.class.getMethod("getOnlinePlayers");
			if(onlinePlayerMethod.getReturnType().equals(Collection.class)) {
				return ((Collection<? extends Player>)onlinePlayerMethod.invoke(Bukkit.getServer()));
			} else {
				return Arrays.asList((Player[])onlinePlayerMethod.invoke(Bukkit.getServer()));
			}
		} catch(Exception ex) {
			AreaShop.debug("getOnlinePlayers error: "+ex.getMessage());
		}
		return new HashSet<>();
	}

	/**
	 * Create a map from a location, to save it in the config
	 * @param location The location to transform
	 * @param setPitchYaw true to save the pitch and yaw, otherwise false
	 * @return The map with the location values
	 */
	public static ConfigurationSection locationToConfig(Location location, boolean setPitchYaw) {
		if(location == null) {
			return null;
		}
		ConfigurationSection result = new YamlConfiguration();
		result.set("world", location.getWorld().getName());
		result.set("x", location.getX());
		result.set("y", location.getY());
		result.set("z", location.getZ());
		if(setPitchYaw) {
			result.set("yaw", Float.toString(location.getYaw()));
			result.set("pitch", Float.toString(location.getPitch()));		
		}
		return result;
	}

	/**
	 * Create a map from a location, to save it in the config (without pitch and yaw)
	 * @param location The location to transform
	 * @return The map with the location values
	 */
	public static ConfigurationSection locationToConfig(Location location) {
		return locationToConfig(location, false);
	}
	
	/**
	 * Create a location from a map, reconstruction from the config values
	 * @param config The config section to reconstruct from
	 * @return The location
	 */
	public static Location configToLocation(ConfigurationSection config) {
		if(config == null
				|| !config.isString("world")
				|| !config.isDouble("x")
				|| !config.isDouble("y")
				|| !config.isDouble("z")
				|| Bukkit.getWorld(config.getString("world")) == null) {
			return null;
		}
		Location result = new Location(
				Bukkit.getWorld(config.getString("world")),
				config.getDouble("x"),
				config.getDouble("y"),
				config.getDouble("z"));
		if(config.isString("yaw") && config.isString("pitch")) {
			result.setPitch(Float.parseFloat(config.getString("pitch")));
			result.setYaw(Float.parseFloat(config.getString("yaw")));
		}
		return result;
	}
	
	/**
	 * Create a comma-separated list
	 * @param input Collection of object which should be concatenated with comma's in between
	 * @return Innput object concatenated with comma's in between
	 */
	public static String createCommaSeparatedList(Collection<?> input) {
		String result = "";
		boolean first = true;
		for(Object object : input) {
			if(object != null) {
				if(first) {
					first = false;
					result += object.toString();
				} else {
					result += ", " + object.toString();
				}
			}
		}		
		return result;
	}
	
	/**
	 * Convert milliseconds to ticks
	 * @param milliseconds Milliseconds to convert
	 * @return milliseconds divided by 50 (20 ticks per second)
	 */
	public static long millisToTicks(long milliseconds) {
		return milliseconds/50;
	}

	/**
	 * Convert milliseconds to a human readable format
	 * @param milliseconds The amount of milliseconds to convert
	 * @return A formatted string based on the language file
	 */
	public static String millisToHumanFormat(long milliseconds) {
		long timeLeft = milliseconds+500;
		// To seconds
		timeLeft = timeLeft/1000;
		if(timeLeft <= 0) {
			return Message.fromKey("timeleft-ended").getPlain();
		} else if(timeLeft == 1) {
			return Message.fromKey("timeleft-second").replacements(timeLeft).getPlain();
		} else if(timeLeft <= 120) {
			return Message.fromKey("timeleft-seconds").replacements(timeLeft).getPlain();
		}
		// To minutes
		timeLeft = timeLeft/60;
		if(timeLeft <= 120) {
			return Message.fromKey("timeleft-minutes").replacements(timeLeft).getPlain();
		}
		// To hours
		timeLeft = timeLeft/60;
		if(timeLeft <= 48) {
			return Message.fromKey("timeleft-hours").replacements(timeLeft).getPlain();
		}
		// To days
		timeLeft = timeLeft/24;
		if(timeLeft <= 60) {
			return Message.fromKey("timeleft-days").replacements(timeLeft).getPlain();
		}
		// To months
		timeLeft = timeLeft/30;
		if(timeLeft <= 24) {
			return Message.fromKey("timeleft-months").replacements(timeLeft).getPlain();
		}
		// To years
		timeLeft = timeLeft/12;
		return Message.fromKey("timeleft-years").replacements(timeLeft).getPlain();
	}

	private static final BlockFace[] facings = {BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST};

	/**
	* Get the facing direction based on the yaw
	* @param yaw The horizontal angle that for example the player is looking
	* @return The Block Face of the angle
	*/
	public static BlockFace yawToFacing(float yaw) {
		return facings[Math.round(yaw / 45f) & 0x7];
	}
	
	// ======================================================================
	// Methods to get WorldGuard or AreaShop regions by location or selection
	// ======================================================================
	/**
	 * Get all AreaShop regions intersecting with a WorldEdit selection
	 * @param selection The selection to check
	 * @return A list with all the AreaShop regions intersecting with the selection
	 */
	public static List<GeneralRegion> getASRegionsInSelection(Selection selection) {
		ArrayList<GeneralRegion> result = new ArrayList<>();
		for(ProtectedRegion region : getWERegionsInSelection(selection)) {
			GeneralRegion asRegion = AreaShop.getInstance().getFileManager().getRegion(region.getId());
			if(asRegion != null) {
				result.add(asRegion);
			}
		}
		return result;
	}
	/**
	 * Get all AreaShop regions containing a location
	 * @param location The location to check
	 * @return A list with all the AreaShop regions that contain the location
	 */
	public static List<GeneralRegion> getASRegionsByLocation(Location location) {
		Selection selection = new CuboidSelection(location.getWorld(), location, location);
		return getASRegionsInSelection(selection);
	}
	
	/**
	 * Get all WorldGuard regions intersecting with a WorldEdit selection
	 * @param selection The selection to check
	 * @return A list with all the WorldGuard regions intersecting with the selection
	 */
	public static List<ProtectedRegion> getWERegionsInSelection(Selection selection) {
		// Get all regions inside or intersecting with the WorldEdit selection of the player
		World world = selection.getWorld();
		RegionManager regionManager = AreaShop.getInstance().getWorldGuard().getRegionManager(world);
		ArrayList<ProtectedRegion> result = new ArrayList<>();
		Location selectionMin = selection.getMinimumPoint();
		Location selectionMax = selection.getMaximumPoint();
		for(ProtectedRegion region : regionManager.getRegions().values()) {
			BlockVector regionMin = region.getMinimumPoint();
			BlockVector regionMax = region.getMaximumPoint();			
			if( 
					(      // x part, resolves to true if the selection and region overlap anywhere on the x-axis
						   (regionMin.getBlockX() <= selectionMax.getBlockX() && regionMin.getBlockX() >= selectionMin.getBlockX())
						|| (regionMax.getBlockX() <= selectionMax.getBlockX() && regionMax.getBlockX() >= selectionMin.getBlockX())
						|| (selectionMin.getBlockX() >= regionMin.getBlockX() && selectionMin.getBlockX() <= regionMax.getBlockX())
						|| (selectionMax.getBlockX() >= regionMin.getBlockX() && selectionMax.getBlockX() <= regionMax.getBlockX())
					) && ( // Y part, resolves to true if the selection and region overlap anywhere on the y-axis
					       (regionMin.getBlockY() <= selectionMax.getBlockY() && regionMin.getBlockY() >= selectionMin.getBlockY())
						|| (regionMax.getBlockY() <= selectionMax.getBlockY() && regionMax.getBlockY() >= selectionMin.getBlockY())
						|| (selectionMin.getBlockY() >= regionMin.getBlockY() && selectionMin.getBlockY() <= regionMax.getBlockY())
						|| (selectionMax.getBlockY() >= regionMin.getBlockY() && selectionMax.getBlockY() <= regionMax.getBlockY())
					) && ( // Z part, resolves to true if the selection and region overlap anywhere on the z-axis
				           (regionMin.getBlockZ() <= selectionMax.getBlockZ() && regionMin.getBlockZ() >= selectionMin.getBlockZ())
						|| (regionMax.getBlockZ() <= selectionMax.getBlockZ() && regionMax.getBlockZ() >= selectionMin.getBlockZ())
						|| (selectionMin.getBlockZ() >= regionMin.getBlockZ() && selectionMin.getBlockZ() <= regionMax.getBlockZ())
						|| (selectionMax.getBlockZ() >= regionMin.getBlockZ() && selectionMax.getBlockZ() <= regionMax.getBlockZ())
					)
				) {
				result.add(region);
			}
		}
		return result;
	}
	
	/**
	 * Get a list of regions around a location
	 *  - Returns highest priority, child instead of parent regions
	 * @param location The location to check for regions
	 * @return empty list if no regions found, 1 member if 1 region is a priority, more if regions with the same priority
	 */
	public static List<ProtectedRegion> getApplicableRegions(Location location) {
		List<ProtectedRegion> result = new ArrayList<>();
		Set<ProtectedRegion> regions = AreaShop.getInstance().getWorldGuardHandler().getApplicableRegionsSet(location);
		if(regions != null) {
			boolean first = true;
			for(ProtectedRegion pr : regions) {
				if(first) {
					result.add(pr);
					first = false;
				} else {
					if(pr.getPriority() > result.get(0).getPriority()) {
						result.clear();
						result.add(pr);
					} else if(pr.getParent() != null && pr.getParent().equals(result.get(0))) {
						result.clear();
						result.add(pr);
					} else {
						result.add(pr);
					}
				}
			}
		}
		return result;
	}
	
	// Methods to get the most important AreaShop regions at a certain location
	public static List<RentRegion> getApplicableRentRegions(Location location) {
		List<RentRegion> result = new ArrayList<>();
		for(GeneralRegion region : getApplicableASRegions(location, GeneralRegion.RegionType.RENT)) {
			result.add((RentRegion)region);
		}
		return result;
	}
	public static List<BuyRegion> getApplicableBuyRegions(Location location) {
		List<BuyRegion> result = new ArrayList<>();
		for(GeneralRegion region : getApplicableASRegions(location, GeneralRegion.RegionType.BUY)) {
			result.add((BuyRegion)region);
		}
		return result;
	}
	public static List<GeneralRegion> getAllApplicableRegions(Location location) {
		return getApplicableASRegions(location, null);
	}

	public static List<GeneralRegion> getApplicableASRegions(Location location, GeneralRegion.RegionType type) {
		List<GeneralRegion> result = new ArrayList<>();
		Set<ProtectedRegion> regions = AreaShop.getInstance().getWorldGuardHandler().getApplicableRegionsSet(location);
		if(regions != null) {
			List<GeneralRegion> candidates = new ArrayList<>();
			for(ProtectedRegion pr : regions) {
				GeneralRegion region = AreaShop.getInstance().getFileManager().getRegion(pr.getId());
				if(region != null && (
						(type == GeneralRegion.RegionType.RENT && region.isRentRegion())
								|| (type == GeneralRegion.RegionType.BUY && region.isBuyRegion())
						|| type == null)) {
					candidates.add(region);
				}
			}		
			boolean first = true;
			for(GeneralRegion region : candidates) {
				if(region == null) {
					AreaShop.debug("skipped null region");
					continue;
				}
				if(first) {
					result.add(region);
					first = false;
				} else {
					if(region.getRegion().getPriority() > result.get(0).getRegion().getPriority()) {
						result.clear();
						result.add(region);
					} else if(region.getRegion().getParent() != null && region.getRegion().getParent().equals(result.get(0).getRegion())) {
						result.clear();
						result.add(region);
					} else {
						result.add(region);
					}
				}
			}
		}
		return new ArrayList<>(result);
	}


	/**
	 * Convert color and formatting codes to bukkit values
	 * @param input Start string with color and formatting codes in it
	 * @return String with the color and formatting codes in the bukkit format
	 */
	public static String applyColors(String input) {
		String result = null;
		if(input != null) {
			result = ChatColor.translateAlternateColorCodes('&', input);
		}
		return result;
	}


	/**
	 * Format the currency amount with the characters before and after
	 * @param amount Amount of money to format
	 * @return Currency character format string
	 */
	public static String formatCurrency(double amount) {
		String before = config.getString("moneyCharacter");
		before = before.replace(AreaShop.currencyEuro, "\u20ac");
		String after = config.getString("moneyCharacterAfter");
		after = after.replace(AreaShop.currencyEuro, "\u20ac");
		String result;
		// Check for infinite and NaN
		if(Double.isInfinite(amount)) {
			result = "\u221E"; // Infinite symbol
		} else if(Double.isNaN(amount)) {
			result = "NaN";
		} else {
			// Add metric
			double metricAbove = config.getDouble("metricSuffixesAbove");
			if(metricAbove != -1 && amount >= metricAbove) {
				if(amount >= 1000000000000000000000000.0) {
					amount = amount/1000000000000000000000000.0;
					after = "Y"+after;
				} else if(amount >= 1000000000000000000000.0) {
					amount = amount/1000000000000000000000.0;
					after = "Z"+after;
				} else if(amount >= 1000000000000000000.0) {
					amount = amount/1000000000000000000.0;
					after = "E"+after;
				} else if(amount >= 1000000000000000.0) {
					amount = amount/1000000000000000.0;
					after = "P"+after;
				} else if(amount >= 1000000000000.0) {
					amount = amount/1000000000000.0;
					after = "T"+after;
				} else if(amount >= 1000000000.0) {
					amount = amount/1000000000.0;
					after = "G"+after;
				} else if(amount >= 1000000.0) {
					amount = amount/1000000.0;
					after = "M"+after;
				} else if(amount >= 1000.0) {
					amount = amount/1000.0;
					after = "k"+after;
				}
				BigDecimal bigDecimal = new BigDecimal(amount);
				if(bigDecimal.toString().contains(".")) {
					int frontLength = bigDecimal.toString().substring(0, bigDecimal.toString().indexOf('.')).length();
					bigDecimal = bigDecimal.setScale(config.getInt("fractionalNumbers")+(3-frontLength), RoundingMode.HALF_UP);
				}
				result = bigDecimal.toString();
			} else {
				BigDecimal bigDecimal = new BigDecimal(amount);
				bigDecimal = bigDecimal.setScale(config.getInt("fractionalNumbers"), RoundingMode.HALF_UP);
				amount = bigDecimal.doubleValue();
				result = bigDecimal.toString();
				if(config.getBoolean("hideEmptyFractionalPart") && (amount%1.0) == 0.0 && result.contains(".")) {
					result = result.substring(0, result.indexOf('.'));
				}
			}
		}
		result = result.replace(".", config.getString("decimalMark"));
		return before+result+after;
	}


	/**
	 * Checks if the string is a correct time period
	 * @param time String that has to be checked
	 * @return true if format is correct, false if not
	 */
	public static boolean checkTimeFormat(String time) {
		// Check if the string is not empty and check the length
		if(time == null || time.length() <= 1 || time.indexOf(' ') == -1 || time.indexOf(' ') >= (time.length()-1)) {
			return false;
		}

		// Check if the suffix is one of these values
		String suffix = time.substring(time.indexOf(' ')+1, time.length());
		if(!identifiers.contains(suffix)) {
			return false;
		}

		// check if the part before the space is a number
		String prefix = time.substring(0, (time.indexOf(' ')));
		return prefix.matches("\\d+");
	}

	/**
	 * Methode to tranlate a duration string to a millisecond value
	 * @param duration The duration string
	 * @return The duration in milliseconds translated from the durationstring, or if it is invalid then 0
	 */
	public static long durationStringToLong(String duration) {
		if(duration == null) {
			return 0;
		} else if(duration.equalsIgnoreCase("disabled") || duration.equalsIgnoreCase("unlimited")) {
			return -1;
		} else if(duration.indexOf(' ') == -1) {
			return 0;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(0);

		String durationString = duration.substring(duration.indexOf(' ')+1, duration.length());
		int durationInt = 0;
		try {
			durationInt = Integer.parseInt(duration.substring(0, duration.indexOf(' ')));
		} catch(NumberFormatException exception) {
			// No Number found, add zero
		}

		if(seconds.contains(durationString)) {
			calendar.add(Calendar.SECOND, durationInt);
		} else if(minutes.contains(durationString)) {
			calendar.add(Calendar.MINUTE, durationInt);
		} else if(hours.contains(durationString)) {
			calendar.add(Calendar.HOUR, durationInt);
		} else if(days.contains(durationString)) {
			calendar.add(Calendar.DAY_OF_MONTH, durationInt);
		} else if(weeks.contains(durationString)) {
			calendar.add(Calendar.DAY_OF_MONTH, durationInt*7);
		} else if(months.contains(durationString)) {
			calendar.add(Calendar.MONTH, durationInt);
		} else if(years.contains(durationString)) {
			calendar.add(Calendar.YEAR, durationInt);
		}
		return calendar.getTimeInMillis();
	}

	// LEGACY TIME INPUT CONVERSION

	/**
	 * Get setting from config that could be only a number indicating seconds
	 * or a string indicating a duration string
	 * @param path Path of the setting to read
	 * @return milliseconds that the setting indicates
	 */
	public static long getDurationFromSecondsOrString(String path) {
		if(config.isLong(path) || config.isInt(path)) {
			long setting = config.getLong(path);
			if(setting != -1) {
				setting = setting*1000;
			}
			return setting;
		} else {
			return durationStringToLong(config.getString(path));
		}
	}

	/**
	 * Get setting from config that could be only a number indicating minutes
	 * or a string indicating a duration string
	 * @param path Path of the setting to read
	 * @return milliseconds that the setting indicates
	 */
	public static long getDurationFromMinutesOrString(String path) {
		if(config.isLong(path) || config.isInt(path)) {
			long setting = config.getLong(path);
			if(setting != -1) {
				setting = setting*60*1000;
			}
			return setting;
		} else {
			return durationStringToLong(config.getString(path));
		}
	}

	/**
	 * Parse a time setting that could be minutes or a duration string
	 * @param input The string to parse
	 * @return milliseconds that the string indicates
	 */
	public static long getDurationFromMinutesOrStringInput(String input) {
		long number;
		try {
			number = Long.parseLong(input);
			if(number != -1) {
				number = number*60*1000;
			}
			return number;
		} catch(NumberFormatException e) {
			return durationStringToLong(input);
		}
	}

	/**
	 * Parse a time setting that could be seconds or a duration string
	 * @param input The string to parse
	 * @return seconds that the string indicates
	 */
	public static long getDurationFromSecondsOrStringInput(String input) {
		long number;
		try {
			number = Long.parseLong(input);
			if(number != -1) {
				number = number*1000;
			}
			return number;
		} catch(NumberFormatException e) {
			return durationStringToLong(input);
		}
	}

	// NAME <-> UUID CONVERSION

	/**
	 * Conversion to name by uuid
	 * @param uuid The uuid in string format
	 * @return the name of the player
	 */
	public static String toName(String uuid) {
		String result = "";
		if(uuid != null) {
			try {
				UUID parsed = UUID.fromString(uuid);
				result = toName(parsed);
			} catch(IllegalArgumentException e) {
				// Incorrect UUID
			}
		}
		return result;
	}

	/**
	 * Conversion to name by uuid object
	 * @param uuid The uuid in string format
	 * @return the name of the player
	 */
	public static String toName(UUID uuid) {
		if(uuid == null) {
			return "";
		} else {
			String name = Bukkit.getOfflinePlayer(uuid).getName();
			if(name != null) {
				return name;
			}
			return "";
		}
	}

	/**
	 * Conversion from name to uuid
	 * @param name The name of the player
	 * @return The uuid of the player
	 */
	@SuppressWarnings("deprecation") // Fake deprecation by Bukkit to inform developers, method will stay
	public static String toUUID(String name) {
		if(name == null) {
			return null;
		} else {
			return Bukkit.getOfflinePlayer(name).getUniqueId().toString();
		}

	}

}















