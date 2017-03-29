package me.wiefferink.areashop.commands;

import me.wiefferink.areashop.regions.BuyRegion;
import me.wiefferink.areashop.tools.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class StopresellCommand extends CommandAreaShop {

	@Override
	public String getCommandStart() {
		return "areashop stopresell";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.stopresellall") || target.hasPermission("areashop.stopresell")) {
			return "help-stopResell";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.stopresell") && !sender.hasPermission("areashop.stopresellall")) {
			plugin.message(sender, "stopresell-noPermissionOther");
			return;
		}

		BuyRegion buy;
		if(args.length <= 1) {
			if(sender instanceof Player) {
				// get the region by location
				List<BuyRegion> regions = Utils.getImportantBuyRegions(((Player)sender).getLocation());
				if(regions.isEmpty()) {
					plugin.message(sender, "cmd-noRegionsAtLocation");
					return;
				} else if(regions.size() > 1) {
					plugin.message(sender, "cmd-moreRegionsAtLocation");
					return;
				} else {
					buy = regions.get(0);
				}
			} else {
				plugin.message(sender, "cmd-automaticRegionOnlyByPlayer");
				return;
			}
		} else {
			buy = plugin.getFileManager().getBuy(args[1]);
			if(buy == null) {
				plugin.message(sender, "stopresell-notRegistered", args[1]);
				return;
			}
		}
		if(buy == null) {
			plugin.message(sender, "cmd-noRegionsAtLocation");
			return;
		}
		if(!buy.isInResellingMode()) {
			plugin.message(sender, "stopresell-notResell", buy);
			return;
		}
		if(sender.hasPermission("areashop.stopresellall")) {
			buy.disableReselling();
			buy.update();
			plugin.message(sender, "stopresell-success", buy);
		} else if(sender.hasPermission("areashop.stopresell") && sender instanceof Player) {
			if(buy.isOwner((Player)sender)) {
				buy.disableReselling();
				buy.update();
				plugin.message(sender, "stopresell-success", buy);
			} else {
				plugin.message(sender, "stopresell-noPermissionOther", buy);
			}
		} else {
			plugin.message(sender, "stopresell-noPermission", buy);
		}
	}

	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		ArrayList<String> result = new ArrayList<>();
		if(toComplete == 2) {
			for(BuyRegion region : plugin.getFileManager().getBuys()) {
				if(region.isSold() && region.isInResellingMode()) {
					result.add(region.getName());
				}
			}
		}
		return result;
	}
}
















