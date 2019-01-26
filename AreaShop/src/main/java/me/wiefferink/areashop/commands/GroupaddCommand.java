package me.wiefferink.areashop.commands;

import me.wiefferink.areashop.interfaces.WorldEditSelection;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.regions.RegionGroup;
import me.wiefferink.areashop.tools.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class GroupaddCommand extends CommandAreaShop {

	@Override
	public String getCommandStart() {
		return "areashop groupadd";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.groupadd")) {
			return "help-groupadd";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.groupadd")) {
			plugin.message(sender, "groupadd-noPermission");
			return;
		}
		if(args.length < 2 || args[1] == null) {
			plugin.message(sender, "groupadd-help");
			return;
		}
		RegionGroup group = plugin.getFileManager().getGroup(args[1]);
		if(group == null) {
			group = new RegionGroup(plugin, args[1]);
			plugin.getFileManager().addGroup(group);
		}
		if(args.length == 2) {
			if(!(sender instanceof Player)) {
				plugin.message(sender, "cmd-weOnlyByPlayer");
				return;
			}
			Player player = (Player)sender;
			WorldEditSelection selection = plugin.getWorldEditHandler().getPlayerSelection(player);
			if(selection == null) {
				plugin.message(player, "cmd-noSelection");
				return;
			}
			List<GeneralRegion> regions = Utils.getRegionsInSelection(selection);
			if(regions.isEmpty()) {
				plugin.message(player, "cmd-noRegionsFound");
				return;
			}
			TreeSet<GeneralRegion> regionsSuccess = new TreeSet<>();
			TreeSet<GeneralRegion> regionsFailed = new TreeSet<>();
			for(GeneralRegion region : regions) {
				if(group.addMember(region)) {
					regionsSuccess.add(region);
				} else {
					regionsFailed.add(region);
				}
			}
			if(!regionsSuccess.isEmpty()) {
				plugin.message(player, "groupadd-weSuccess", group.getName(), Utils.combinedMessage(regionsSuccess, "region"));
			}
			if(!regionsFailed.isEmpty()) {
				plugin.message(player, "groupadd-weFailed", group.getName(), Utils.combinedMessage(regionsFailed, "region"));
			}
			// Update all regions, this does it in a task, updating them without lag
			plugin.getFileManager().updateRegions(new ArrayList<>(regionsSuccess), player);
		} else {
			GeneralRegion region = plugin.getFileManager().getRegion(args[2]);
			if(region == null) {
				plugin.message(sender, "cmd-notRegistered", args[2]);
				return;
			}
			if(group.addMember(region)) {
				region.update();
				plugin.message(sender, "groupadd-success", group.getName(), group.getMembers().size(), region);
			} else {
				plugin.message(sender, "groupadd-failed", group.getName(), region);
			}
		}
	}

	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		List<String> result = new ArrayList<>();
		if(toComplete == 2) {
			result = plugin.getFileManager().getGroupNames();
		} else if(toComplete == 3) {
			result = plugin.getFileManager().getRegionNames();
		}
		return result;
	}

}










