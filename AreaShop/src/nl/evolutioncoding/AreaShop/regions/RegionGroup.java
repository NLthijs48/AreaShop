package nl.evolutioncoding.AreaShop.regions;

import java.util.List;

import nl.evolutioncoding.AreaShop.AreaShop;

import org.bukkit.configuration.ConfigurationSection;

public class RegionGroup {

	private AreaShop plugin;
	private String name;
	
	public RegionGroup(AreaShop plugin, String name) {
		this.plugin = plugin;
		this.name = name;
	}
	
	public void addMember(GeneralRegion region) {
		List<String> members = getMembers();
		members.add(region.getName());
		plugin.getFileManager().getGroupSettings(name).set("regions", members);
	}
	
	public void removeMember(GeneralRegion region) {
		List<String> members = getMembers();
		members.remove(region.getName());
		plugin.getFileManager().getGroupSettings(name).set("regions", members);
	}
	
	public List<String> getMembers() {
		return getSettings().getStringList("regions");
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isMember(GeneralRegion region) {
		return getMembers().contains(region);
	}
	
	public int getPriority() {
		return getSettings().getInt("priority");
	}
	
	public ConfigurationSection getSettings() {
		return plugin.getFileManager().getGroupSettings(name);
	}

	public void save() {
		plugin.getFileManager().saveGroups();
	}
}
