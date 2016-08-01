package me.wiefferink.areashop.features;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.events.notify.RegionUpdateEvent;
import me.wiefferink.areashop.messages.Message;
import me.wiefferink.areashop.regions.GeneralRegion;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Set;

public class WorldGuardRegionFlagsFeature extends Feature implements Listener {


	@EventHandler
	public void regionUpdate(RegionUpdateEvent event) {
		updateRegionFlags(event.getRegion());
	}

	/**
	 * Set the region flags/options to the values of a ConfigurationSection
	 * @param region The region to update the flags for
	 * @return true if the flags have been set correctly, otherwise false
	 */
	protected boolean updateRegionFlags(GeneralRegion region) {
		boolean result = true;

		// General region flags
		String allPath = "flagProfiles."+region.getStringSetting("general.flagProfile")+".ALL";
		ConfigurationSection generalFlags = plugin.getConfig().getConfigurationSection(allPath);
		if(plugin.getConfig().isSet(allPath) && generalFlags != null) { // Explicitely check if it is set, so don't apply if only in the default config
			result = updateRegionFlags(region, generalFlags);
		}

		// Specific region flags
		String specificPath = "flagProfiles."+region.getStringSetting("general.flagProfile")+"."+region.getState().getValue();
		if(plugin.getConfig().isSet(specificPath)) { // Do no apply default flags if they are removed from the active config
			ConfigurationSection specificFlags = plugin.getConfig().getConfigurationSection(specificPath);
			result = result && specificFlags != null && updateRegionFlags(region, specificFlags);
		}
		return result;
	}
	/**
	 * Set the region flags/options to the values of a ConfigurationSection
	 * @param region The region to update the flags for
	 * @param flags The flags to apply
	 * @return true if the flags have been set correctly, otherwise false
	 */
	protected boolean updateRegionFlags(GeneralRegion region, ConfigurationSection flags) {
		boolean result = true;

		Set<String> flagNames = flags.getKeys(false);
		WorldGuardPlugin worldGuard = plugin.getWorldGuard();

		// Get the region
		ProtectedRegion wRegion = region.getRegion();
		if(wRegion == null) {
			AreaShop.debug("Region '" + region.getName() + "' does not exist, setting flags failed");
			return false;
		}
		// Loop through all flags that are set in the config
		for(String flagName : flagNames) {
			String value = Message.fromString(flags.getString(flagName)).replacements(region).getPlain();
			// In the config normal Bukkit color codes are used, those only need to be translated on 5.X WorldGuard versions
			if(plugin.getWorldGuard().getDescription().getVersion().startsWith("5.")) {
				value = translateBukkitToWorldGuardColors(value);
			}
			if(flagName.equalsIgnoreCase("members")) {
				plugin.getWorldGuardHandler().setMembers(wRegion, value);
				//AreaShop.debug("  Flag " + flagName + " set: " + members.toUserFriendlyString());
			} else if(flagName.equalsIgnoreCase("owners")) {
				plugin.getWorldGuardHandler().setOwners(wRegion, value);
				//AreaShop.debug("  Flag " + flagName + " set: " + owners.toUserFriendlyString());
			} else if(flagName.equalsIgnoreCase("priority")) {
				try {
					int priority = Integer.parseInt(value);
					wRegion.setPriority(priority);
					//AreaShop.debug("  Flag " + flagName + " set: " + value);
				} catch(NumberFormatException e) {
					AreaShop.warn("The value of flag "+flagName+" is not a number");
					result = false;
				}
			} else if(flagName.equalsIgnoreCase("parent")) {
				if(region.getWorld() == null || worldGuard.getRegionManager(region.getWorld()) == null) {
					continue;
				}
				ProtectedRegion parentRegion = worldGuard.getRegionManager(region.getWorld()).getRegion(value);
				if(parentRegion != null) {
					try {
						wRegion.setParent(parentRegion);
						//AreaShop.debug("  Flag " + flagName + " set: " + value);
					} catch(ProtectedRegion.CircularInheritanceException e) {
						AreaShop.warn("The parent set in the config is not correct (circular inheritance)");
					}
				} else {
					AreaShop.warn("The parent set in the config is not correct (region does not exist)");
				}
			} else {
				// Parse all other normal flags (groups are also handled)
				String flagSetting = null;
				com.sk89q.worldguard.protection.flags.RegionGroup groupValue = null;

				Flag<?> foundFlag = plugin.getWorldGuardHandler().fuzzyMatchFlag(flagName);
				if(foundFlag == null) {
					AreaShop.warn("Found wrong flag in flagProfiles section: "+flagName+", check if that is the correct WorldGuard flag");
					continue;
				}
				RegionGroupFlag groupFlag = foundFlag.getRegionGroupFlag();
				if(value == null || value.isEmpty()) {
					wRegion.setFlag(foundFlag, null);
					if(groupFlag != null) {
						wRegion.setFlag(groupFlag, null);
					}
					//AreaShop.debug("  Flag " + flagName + " reset (+ possible group of flag)");
				} else {
					if(groupFlag == null) {
						flagSetting = value;
					} else {
						for(String part : value.split(" ")) {
							if(part.startsWith("g:")) {
								if(part.length() > 2) {
									try {
										groupValue = plugin.getWorldGuardHandler().parseFlagGroupInput(groupFlag, part.substring(2));
									} catch(InvalidFlagFormat e) {
										AreaShop.warn("Found wrong group value for flag "+flagName);
									}
								}
							} else {
								if(flagSetting == null) {
									flagSetting = part;
								} else {
									flagSetting += " " + part;
								}
							}
						}
					}
					if(flagSetting != null) {
						try {
							setFlag(wRegion, foundFlag, flagSetting);
							//AreaShop.debug("  Flag " + flagName + " set: " + flagSetting);
						} catch(InvalidFlagFormat e) {
							AreaShop.warn("Found wrong value for flag "+flagName);
						}
					}
					if(groupValue != null) {
						if(groupValue == groupFlag.getDefault()) {
							wRegion.setFlag(groupFlag, null);
							//AreaShop.debug("    Group of flag " + flagName + " set to default: " + groupValue);
						} else {
							wRegion.setFlag(groupFlag, groupValue);
							//AreaShop.debug("    Group of flag " + flagName + " set: " + groupValue);
						}
					}
				}
			}
		}
		// Indicate that the regions needs to be saved
		plugin.getFileManager().saveIsRequiredForRegionWorld(region.getWorldName());
		return result;
	}

	/**
	 * Set a WorldGuard region flag
	 * @param region The WorldGuard region to set
	 * @param flag   The flag to set
	 * @param value  The value to set the flag to
	 * @param <V> They type of flag to set
	 * @throws InvalidFlagFormat When the value of the flag is wrong
	 */
	private <V> void setFlag(ProtectedRegion region, Flag<V> flag, String value) throws InvalidFlagFormat {
		region.setFlag(flag, plugin.getWorldGuardHandler().parseFlagInput(flag, value));
	}

	/**
	 * Translate the color codes you put in greeting/farewell messages to the weird color codes of WorldGuard
	 * @param message The message where the color codes should be translated (this message has bukkit color codes)
	 * @return The string with the WorldGuard color codes
	 */
	public String translateBukkitToWorldGuardColors(String message) {
		String result = message;
		result = result.replace("&c", "&r");
		result = result.replace("&4", "&R");
		result = result.replace("&e", "&y");
		result = result.replace("&6", "&Y");
		result = result.replace("&a", "&g");
		result = result.replace("&2", "&G");
		result = result.replace("&b", "&c");
		result = result.replace("&3", "&C");
		result = result.replace("&9", "&b");
		result = result.replace("&1", "&B");
		result = result.replace("&d", "&p");
		result = result.replace("&5", "&P");
		result = result.replace("&0", "&0");
		result = result.replace("&8", "&1");
		result = result.replace("&7", "&2");
		result = result.replace("&f", "&w");
		result = result.replace("&r", "&x");
		return result;
	}
}
