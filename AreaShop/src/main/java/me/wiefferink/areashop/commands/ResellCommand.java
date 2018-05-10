package me.wiefferink.areashop.commands;

import me.wiefferink.areashop.regions.BuyRegion;
import me.wiefferink.areashop.tools.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ResellCommand extends CommandAreaShop {

	@Override
	public String getCommandStart() {
		return "areashop resell";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.resellall") || target.hasPermission("areashop.resell")) {
			return "help-resell";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.resell") && !sender.hasPermission("areashop.resellall")) {
			plugin.message(sender, "resell-noPermissionOther");
			return;
		}

		if(args.length <= 1) {
			plugin.message(sender, "resell-help");
			return;
		}
		double price;
		try {
			price = Double.parseDouble(args[1]);
		} catch(NumberFormatException e) {
			plugin.message(sender, "resell-wrongPrice", args[1]);
			return;
		}

		if(price < 0) {
			plugin.message(sender, "resell-wrongPrice", args[1]);
			return;
		}

		BuyRegion buy;
		if(args.length <= 2) {
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
			buy = plugin.getFileManager().getBuy(args[2]);
			if(buy == null) {
				plugin.message(sender, "resell-notRegistered", args[2]);
				return;
			}
		}
		if(buy == null) {
			plugin.message(sender, "cmd-noRegionsAtLocation");
			return;
		}
		if(!buy.isSold()) {
			plugin.message(sender, "resell-notBought", buy);
			return;
		}
		if(sender.hasPermission("areashop.resellall")) {
			buy.enableReselling(price);
			buy.update();
			plugin.message(sender, "resell-success", buy);
		} else if(sender.hasPermission("areashop.resell") && sender instanceof Player) {
			if(!buy.isOwner((Player)sender)) {
				plugin.message(sender, "resell-noPermissionOther", buy);
				return;
			}

			if(buy.getBooleanSetting("buy.resellDisabled")) {
				plugin.message(sender, "resell-disabled", buy);
				return;
			}

			buy.enableReselling(price);
			buy.update();
			plugin.message(sender, "resell-success", buy);
		} else {
			plugin.message(sender, "resell-noPermission", buy);
		}
	}

	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		ArrayList<String> result = new ArrayList<>();
		if(toComplete == 3) {
			for(BuyRegion region : plugin.getFileManager().getBuys()) {
				if(region.isSold() && !region.isInResellingMode()) {
					result.add(region.getName());
				}
			}
		}
		return result;
	}
}
















