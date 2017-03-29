package me.wiefferink.areashop.commands;

import me.wiefferink.areashop.regions.BuyRegion;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.tools.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SellCommand extends CommandAreaShop {

	@Override
	public String getCommandStart() {
		return "areashop sell";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.sell") || target.hasPermission("areashop.sellown")) {
			return "help-sell";
		}
		return null;
	}

	/**
	 * Check if a person can sell the region.
	 * @param person The person to check
	 * @param region The region to check for
	 * @return true if the person can sell it, otherwise false
	 */
	public static boolean canUse(CommandSender person, GeneralRegion region) {
		if(person.hasPermission("areashop.sell")) {
			return true;
		}
		if(person instanceof Player) {
			Player player = (Player)person;
			return region.isOwner(player) && person.hasPermission("areashop.sellown");
		}
		return false;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.sell") && !sender.hasPermission("areashop.sellown")) {
			plugin.message(sender, "sell-noPermission");
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
		}
		if(buy == null) {
			plugin.message(sender, "sell-notRegistered");
			return;
		}
		if(!buy.isSold()) {
			plugin.message(sender, "sell-notBought", buy);
			return;
		}
		buy.sell(true, sender);
	}

	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		ArrayList<String> result = new ArrayList<>();
		if(toComplete == 2) {
			for(BuyRegion region : plugin.getFileManager().getBuys()) {
				if(region.isSold()) {
					result.add(region.getName());
				}
			}
		}
		return result;
	}
}
















