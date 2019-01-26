package me.wiefferink.areashop.features;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.events.notify.UpdateRegionEvent;
import me.wiefferink.areashop.interfaces.RegionAccessSet;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.interactivemessenger.processing.Message;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class WorldGuardRegionFlagsFeature extends RegionFeature {


	@EventHandler
	public void regionUpdate(UpdateRegionEvent event) {
		updateRegionFlags(event.getRegion());
	}

	/**
	 * Set the region flags/options to the values of a ConfigurationSection.
	 * @param region The region to update the flags for
	 * @return true if the flags have been set correctly, otherwise false
	 */
	private boolean updateRegionFlags(GeneralRegion region) {
		boolean result = true;

		// Get section defining the region flag profile
		ConfigurationSection flagProfileSection = region.getConfigurationSectionSetting("general.flagProfile", "flagProfiles");
		if(flagProfileSection == null) {
			return false;
		}

		// Region flags for all states
		ConfigurationSection allFlags = flagProfileSection.getConfigurationSection("ALL");
		if(allFlags != null) {
			result = updateRegionFlags(region, allFlags);
		}

		// Region flags for the current state
		ConfigurationSection stateFlags = flagProfileSection.getConfigurationSection(region.getState().getValue());

		// If in reselling mode, fallback to 'resale' section if 'resell' is not found (legacy configuration problem: https://github.com/NLthijs48/AreaShop/issues/303)
		if(stateFlags == null && region.getState() == GeneralRegion.RegionState.RESELL) {
			stateFlags = flagProfileSection.getConfigurationSection("resale");
		}
		if(stateFlags != null) {
			result &= updateRegionFlags(region, stateFlags);
		}

		return result;
	}

	/**
	 * Set the region flags/options to the values of a ConfigurationSection.
	 * @param region The region to update the flags for
	 * @param flags  The flags to apply
	 * @return true if the flags have been set correctly, otherwise false
	 */
	private boolean updateRegionFlags(GeneralRegion region, ConfigurationSection flags) {
		boolean result = true;

		Set<String> flagNames = flags.getKeys(false);
		WorldGuardPlugin worldGuard = plugin.getWorldGuard();

		// Get the region
		ProtectedRegion worldguardRegion = region.getRegion();
		if(worldguardRegion == null) {
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
				plugin.getWorldGuardHandler().setMembers(worldguardRegion, parseAccessSet(value));
				//AreaShop.debug("  Flag " + flagName + " set: " + members.toUserFriendlyString());
			} else if(flagName.equalsIgnoreCase("owners")) {
				plugin.getWorldGuardHandler().setOwners(worldguardRegion, parseAccessSet(value));
				//AreaShop.debug("  Flag " + flagName + " set: " + owners.toUserFriendlyString());
			} else if(flagName.equalsIgnoreCase("priority")) {
				try {
					int priority = Integer.parseInt(value);
					if(worldguardRegion.getPriority() != priority) {
						worldguardRegion.setPriority(priority);
					}
					//AreaShop.debug("  Flag " + flagName + " set: " + value);
				} catch(NumberFormatException e) {
					AreaShop.warn("The value of flag " + flagName + " is not a number");
					result = false;
				}
			} else if(flagName.equalsIgnoreCase("parent")) {
				if(region.getWorld() == null || plugin.getRegionManager(region.getWorld()) == null) {
					continue;
				}
				ProtectedRegion parentRegion = plugin.getRegionManager(region.getWorld()).getRegion(value);
				if(parentRegion != null) {
					if(!parentRegion.equals(worldguardRegion.getParent())) {
						try {
							worldguardRegion.setParent(parentRegion);
							//AreaShop.debug("  Flag " + flagName + " set: " + value);
						} catch(ProtectedRegion.CircularInheritanceException e) {
							AreaShop.warn("The parent set in the config is not correct (circular inheritance)");
						}
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
					AreaShop.warn("Found wrong flag in flagProfiles section: " + flagName + ", check if that is the correct WorldGuard flag");
					continue;
				}
				RegionGroupFlag groupFlag = foundFlag.getRegionGroupFlag();
				if(value == null || value.isEmpty()) {
					if(worldguardRegion.getFlag(foundFlag) != null) {
						worldguardRegion.setFlag(foundFlag, null);
					}
					if(groupFlag != null && worldguardRegion.getFlag(groupFlag) != null) {
						worldguardRegion.setFlag(groupFlag, null);
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
										AreaShop.warn("Found wrong group value for flag " + flagName);
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
							setFlag(worldguardRegion, foundFlag, flagSetting);
							//AreaShop.debug("  Flag " + flagName + " set: " + flagSetting);
						} catch(InvalidFlagFormat e) {
							AreaShop.warn("Found wrong value for flag " + flagName);
						}
					}
					if(groupValue != null) {
						if(groupValue == groupFlag.getDefault()) {
							worldguardRegion.setFlag(groupFlag, null);
							//AreaShop.debug("    Group of flag " + flagName + " set to default: " + groupValue);
						} else {
							worldguardRegion.setFlag(groupFlag, groupValue);
							//AreaShop.debug("    Group of flag " + flagName + " set: " + groupValue);
						}
					}
				}
			}
		}
		// Indicate that the regions needs to be saved
		if(worldGuard.getDescription().getVersion().startsWith("5.")) {
			plugin.getFileManager().saveIsRequiredForRegionWorld(region.getWorldName());
		}
		return result;
	}

	/**
	 * Build an RegionAccessSet from an input that specifies player names, player uuids and groups.
	 * @param input Input string defining the access set
	 * @return RegionAccessSet containing the entities parsed from the input
	 */
	public RegionAccessSet parseAccessSet(String input) {
		RegionAccessSet result = new RegionAccessSet();

		String[] inputParts = input.split(", ");
		for(String access : inputParts) {
			if(access != null && !access.isEmpty()) {
				// Check for groups
				if(access.startsWith("g:")) {
					if(access.length() > 2) {
						result.getGroupNames().add(access.substring(2));
					}
				} else if(access.startsWith("n:")) {
					if(access.length() > 2) {
						result.getPlayerNames().add(access.substring(2));
					}
				} else {
					try {
						result.getPlayerUniqueIds().add(UUID.fromString(access));
					} catch(IllegalArgumentException e) {
						AreaShop.warn("Tried using '" + access + "' as uuid for a region member/owner, is your flagProfiles section correct?");
					}
				}
			}
		}

		return result;
	}

	/**
	 * Set a WorldGuard region flag.
	 * @param region The WorldGuard region to set
	 * @param flag   The flag to set
	 * @param value  The value to set the flag to
	 * @param <V>    They type of flag to set
	 * @throws InvalidFlagFormat When the value of the flag is wrong
	 */
	private <V> void setFlag(ProtectedRegion region, Flag<V> flag, String value) throws InvalidFlagFormat {
		V current = region.getFlag(flag);
		V next = plugin.getWorldGuardHandler().parseFlagInput(flag, value);

		if(!Objects.equals(current, next)) {
			region.setFlag(flag, next);
		}
	}

	/**
	 * Translate the color codes you put in greeting/farewell messages to the weird color codes of WorldGuard.
	 * @param message The message where the color codes should be translated (this message has bukkit color codes)
	 * @return The string with the WorldGuard color codes
	 */
	private String translateBukkitToWorldGuardColors(String message) {
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
