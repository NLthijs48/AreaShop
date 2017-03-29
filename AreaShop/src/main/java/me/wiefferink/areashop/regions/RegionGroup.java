package me.wiefferink.areashop.regions;

import me.wiefferink.areashop.AreaShop;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegionGroup {

	private AreaShop plugin;
	private String name;

	/**
	 * Constructor, used when creating new groups or restoring them from groups.yml at server boot
	 * @param plugin The AreaShop plugin
	 * @param name   Name of the group, has to be unique
	 */
	public RegionGroup(AreaShop plugin, String name) {
		this.plugin = plugin;
		this.name = name;
		setSetting("name", name);
		// Delete duplicates
		List<String> members = getMembers();
		int previousCount = members.size();
		List<String> newMembers = new ArrayList<>();
		while(!members.isEmpty()) {
			String member = members.remove(0);
			// If the region has been deleted also clean it from the group
			if(plugin.getFileManager().getRegion(member) != null) {
				newMembers.add(member);
			}
			while(members.contains(member)) {
				members.remove(member);
			}
		}
		if(newMembers.size() != previousCount) {
			setSetting("regions", newMembers);
			AreaShop.debug("group save required because of changed member size");
			saveRequired();
		}
		if(getMembers().size() == 0) {
			plugin.getFileManager().removeGroup(this);
		}
	}

	/**
	 * Adds a member to a group.
	 * @param region The region to add to the group (GeneralRegion or a subclass of it)
	 * @return true if the region was not already added, otherwise false
	 */
	public boolean addMember(GeneralRegion region) {
		List<String> members = getMembers();
		if(members.contains(region.getLowerCaseName())) {
			return false;
		} else {
			members.add(region.getName());
			setSetting("regions", members);
			this.saveRequired();
			return true;
		}
	}

	/**
	 * Remove a member from the group.
	 * @param region The region to remove
	 * @return true if the region was in the group before, otherwise false
	 */
	public boolean removeMember(GeneralRegion region) {
		List<String> members = getMembers();
		boolean result = isMember(region);
		members.remove(region.getName());
		if(members.isEmpty()) {
			plugin.getFileManager().removeGroup(this);
		} else {
			setSetting("regions", members);
		}
		if(result) {
			this.saveRequired();
		}
		return result;
	}

	/**
	 * Get all members of the group.
	 * @return A list with the names of all members of the group
	 */
	public List<String> getMembers() {
		if(getSettings() == null || getSettings().getStringList("regions") == null) {
			return new ArrayList<>();
		}
		return getSettings().getStringList("regions");
	}

	/**
	 * Get all members of the group as GeneralRegions.
	 * @return A Set with all group members
	 */
	public Set<GeneralRegion> getMemberRegions() {
		Set<GeneralRegion> result = new HashSet<>();
		for(String name : getMembers()) {
			result.add(plugin.getFileManager().getRegion(name));
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
		return plugin.getFileManager().getGroupSettings(name);
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
