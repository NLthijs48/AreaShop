package me.wiefferink.areashop.commands;

import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SetlandlordCommand extends CommandAreaShop {

	@Override
	public String getCommandStart() {
		return "areashop setlandlord";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.setlandlord")) {
			return "help-setlandlord";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.setlandlord")) {
			plugin.message(sender, "setlandlord-noPermission");
			return;
		}
		if(args.length < 2) {
			plugin.message(sender, "setlandlord-help");
			return;
		}
		@SuppressWarnings("deprecation")
		OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
		GeneralRegion region;
		if(args.length < 3) {
			if(sender instanceof Player) {
				// get the region by location
				List<GeneralRegion> regions = Utils.getImportantRegions(((Player)sender).getLocation());
				if(regions.isEmpty()) {
					plugin.message(sender, "cmd-noRegionsAtLocation");
					return;
				} else if(regions.size() > 1) {
					plugin.message(sender, "cmd-moreRegionsAtLocation");
					return;
				} else {
					region = regions.get(0);
				}
			} else {
				plugin.message(sender, "cmd-automaticRegionOnlyByPlayer");
				return;
			}
		} else {
			region = plugin.getFileManager().getRegion(args[2]);
		}
		if(region == null) {
			plugin.message(sender, "cmd-notRegistered", args[2]);
			return;
		}
		String playerName = player.getName();
		if(playerName == null || playerName.isEmpty()) {
			playerName = args[1];
		}
		region.setLandlord(player.getUniqueId(), playerName);
		region.update();
		plugin.message(sender, "setlandlord-success", playerName, region);
	}

	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		ArrayList<String> result = new ArrayList<>();
		if(toComplete == 2) {
			for(Player player : Utils.getOnlinePlayers()) {
				result.add(player.getName());
			}
		} else if(toComplete == 3) {
			result.addAll(plugin.getFileManager().getRegionNames());
		}
		return result;
	}

}
