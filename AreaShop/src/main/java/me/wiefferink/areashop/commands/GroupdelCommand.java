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

public class GroupdelCommand extends CommandAreaShop {

	@Override
	public String getCommandStart() {
		return "areashop groupdel";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.groupdel")) {
			return "help-groupdel";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.groupdel")) {
			plugin.message(sender, "groupdel-noPermission");
			return;
		}
		if(args.length < 2 || args[1] == null) {
			plugin.message(sender, "groupdel-help");
			return;
		}
		RegionGroup group = plugin.getFileManager().getGroup(args[1]);
		if(group == null) {
			plugin.message(sender, "groupdel-wrongGroup", args[1]);
			return;
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
				if(group.removeMember(region)) {
					regionsSuccess.add(region);
				} else {
					regionsFailed.add(region);
				}
			}
			if(!regionsSuccess.isEmpty()) {
				plugin.message(player, "groupdel-weSuccess", group.getName(), Utils.combinedMessage(regionsSuccess, "region"));
			}
			if(!regionsFailed.isEmpty()) {
				plugin.message(player, "groupdel-weFailed", group.getName(), Utils.combinedMessage(regionsFailed, "region"));
			}
			// Update all regions, this does it in a task, updating them without lag
			plugin.getFileManager().updateRegions(new ArrayList<>(regionsSuccess), player);
			group.saveRequired();
		} else {
			GeneralRegion region = plugin.getFileManager().getRegion(args[2]);
			if(region == null) {
				plugin.message(sender, "cmd-notRegistered", args[2]);
				return;
			}
			if(group.removeMember(region)) {
				region.update();
				plugin.message(sender, "groupdel-success", group.getName(), group.getMembers().size(), region);
			} else {
				plugin.message(sender, "groupdel-failed", group.getName(), region);
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










