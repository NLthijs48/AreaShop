package me.wiefferink.areashop.regions;

import me.wiefferink.areashop.AreaShop;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

// TODO consider switching to saving lowercase regions
public class RegionGroup {

	private final AreaShop plugin;
	private final String name;
	private final Set<String> regions;
	private Set<String> autoRegions;
	private boolean autoDirty;
	private final Set<String> worlds;

	/**
	 * Constructor, used when creating new groups or restoring them from groups.yml at server boot.
	 * @param plugin The AreaShop plugin
	 * @param name   Name of the group, has to be unique
	 */
	public RegionGroup(AreaShop plugin, String name) {
		this.plugin = plugin;
		this.name = name;
		this.autoDirty = true;
		setSetting("name", name);

		// Load regions and worlds
		regions = new HashSet<>(getSettings().getStringList("regions"));
		worlds = new HashSet<>(getSettings().getStringList("worlds"));
	}

	/**
	 * Get automatically added regions.
	 * @return Set of regions automatically added by the configuration
	 */
	public Set<String> getAutoRegions() {
		if(autoDirty) {
			autoRegions = new HashSet<>();
			for(GeneralRegion region : plugin.getFileManager().getRegions()) {
				if(worlds.contains(region.getWorldName())) {
					autoRegions.add(region.getName());
				}
			}
			autoDirty = false;
		}
		return autoRegions;
	}

	/**
	 * Mark that automatically added regions should be regenerated.
	 */
	public void autoDirty() {
		autoDirty = true;
	}

	/**
	 * Adds a world from which all regions should be added to the group.
	 * @param world World from which all regions should be added
	 * @return true if the region was not already added, otherwise false
	 */
	public boolean addWorld(String world) {
		if(worlds.add(world)) {
			setSetting("regionsFromWorlds", new ArrayList<>(worlds));
			saveRequired();
			autoDirty();
			return true;
		}
		return false;
	}

	/**
	 * Remove a member from the group.
	 * @param world World to remove
	 * @return true if the region was in the group before, otherwise false
	 */
	public boolean removeWorld(String world) {
		if(worlds.remove(world)) {
			setSetting("regionsFromWorlds", new ArrayList<>(worlds));
			saveRequired();
			autoDirty();
			return true;
		}
		return false;
	}

	/**
	 * Get all worlds from which regions are added automatically.
	 * @return A list with the names of all worlds (immutable)
	 */
	public Set<String> getWorlds() {
		return new HashSet<>(worlds);
	}

	/**
	 * Adds a member to a group.
	 * @param region The region to add to the group (GeneralRegion or a subclass of it)
	 * @return true if the region was not already added, otherwise false
	 */
	public boolean addMember(GeneralRegion region) {
		if(regions.add(region.getName())) {
			setSetting("regions", new ArrayList<>(regions));
			saveRequired();
			return true;
		}
		return false;
	}

	/**
	 * Remove a member from the group.
	 * @param region The region to remove
	 * @return true if the region was in the group before, otherwise false
	 */
	public boolean removeMember(GeneralRegion region) {
		if(regions.remove(region.getName())) {
			setSetting("regions", new ArrayList<>(regions));
			saveRequired();
			return true;
		}
		return false;
	}

	/**
	 * Get all members of the group.
	 * @return A list with the names of all members of the group (immutable)
	 */
	public Set<String> getMembers() {
		HashSet<String> result = new HashSet<>(regions);
		result.addAll(getAutoRegions());
		return result;
	}

	/**
	 * Get all manually added members of the group.
	 * @return A list with the names of all members of the group (immutable)
	 */
	public Set<String> getManualMembers() {
		return new HashSet<>(regions);
	}

	/**
	 * Get all members of the group as GeneralRegions.
	 * @return A Set with all group members
	 */
	public Set<GeneralRegion> getMemberRegions() {
		Set<GeneralRegion> result = new HashSet<>();
		for(String playerName : getMembers()) {
			result.add(plugin.getFileManager().getRegion(playerName));
		}
		return result;
	}

	/**
	 * Get the name of the group.
	 * @return The name of the group
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the lowercase name of the group (used for getting the config etc).
	 * @return The name of the group in lowercase
	 */
	public String getLowerCaseName() {
		return getName().toLowerCase();
	}

	/**
	 * Check if a region is member of the group.
	 * @param region Region to check
	 * @return true if the region is in the group, otherwise false
	 */
	public boolean isMember(GeneralRegion region) {
		return getMembers().contains(region.getName());
	}

	/**
	 * Get the priority of the group (higher overwrites).
	 * @return The priority of the group
	 */
	public int getPriority() {
		return getSettings().getInt("priority");
	}

	/**
	 * Get the configurationsection with the settings of this group.
	 * @return The ConfigurationSection with the settings of the group
	 */
	public ConfigurationSection getSettings() {
		ConfigurationSection result =  plugin.getFileManager().getGroupSettings(name);
		if(result != null) {
			return result;
		} else {
			return new YamlConfiguration();
		}
	}

	/**
	 * Set a setting of this group.
	 * @param path    The path to set
	 * @param setting The value to set
	 */
	public void setSetting(String path, Object setting) {
		plugin.getFileManager().setGroupSetting(this, path, setting);
	}

	/**
	 * Indicates this file needs to be saved, will actually get saved later by a task.
	 */
	public void saveRequired() {
		plugin.getFileManager().saveGroupsIsRequired();
	}

	/**
	 * Save the groups to disk now, normally saveRequired() is preferred because of performance.
	 */
	public void saveNow() {
		plugin.getFileManager().saveGroupsNow();
	}
}
