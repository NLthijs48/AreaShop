package me.wiefferink.areashop.commands;

import me.wiefferink.areashop.regions.RegionGroup;
import me.wiefferink.areashop.tools.Utils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GroupinfoCommand extends CommandAreaShop {

	@Override
	public String getCommandStart() {
		return "areashop groupinfo";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.groupinfo")) {
			return "help-groupinfo";
		}
		return null;
	}


	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.groupinfo")) {
			plugin.message(sender, "groupinfo-noPermission");
			return;
		}
		if(args.length < 2 || args[1] == null) {
			plugin.message(sender, "groupinfo-help");
			return;
		}
		RegionGroup group = plugin.getFileManager().getGroup(args[1]);
		if(group == null) {
			plugin.message(sender, "groupinfo-noGroup", args[1]);
			return;
		}
		Set<String> members = group.getMembers();
		if(members.isEmpty()) {
			plugin.message(sender, "groupinfo-noMembers", group.getName());
		} else {
			plugin.message(sender, "groupinfo-members", group.getName(), Utils.createCommaSeparatedList(members));
		}
	}

	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		List<String> result = new ArrayList<>();
		if(toComplete == 2) {
			result = plugin.getFileManager().getGroupNames();
		}
		return result;
	}

}










