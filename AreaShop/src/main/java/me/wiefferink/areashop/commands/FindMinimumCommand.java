package me.wiefferink.areashop.commands;

import me.wiefferink.areashop.regions.BuyRegion;
import me.wiefferink.areashop.regions.RegionGroup;
import me.wiefferink.areashop.regions.RentRegion;
import me.wiefferink.areashop.tools.Utils;
import me.wiefferink.interactivemessenger.processing.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FindMinimumCommand extends CommandAreaShop {

	@Override
	public String getCommandStart() {
		return "areashop findminimum";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.findminimum")) {
			return "help-findmin";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.findminimum")) {
			plugin.message(sender, "find-noPermission");
			return;
		}
		if(!(sender instanceof Player)) {
			plugin.message(sender, "cmd-onlyByPlayer");
			return;
		}
		if(args.length <= 1 || args[1] == null || (!args[1].equalsIgnoreCase("buy") && !args[1].equalsIgnoreCase("rent"))) {
			plugin.message(sender, "findminimum-help");
			return;
		}
		Player player = (Player)sender;
		double balance = 0.0;
		if(plugin.getEconomy() != null) {
			balance = plugin.getEconomy().getBalance(player);
		}
		double minPrice = 0;
		RegionGroup group = null;
		// Parse optional price argument
    try {
			minPrice = Double.parseDouble(args[2]);
		} catch(NumberFormatException e) {
			plugin.message(sender, "find-wrongMinPrice", args[2]);
			return;
		}
    
		// Parse optional group argument
		if(args.length >= 4) {
			group = plugin.getFileManager().getGroup(args[3]);
			if(group == null) {
				plugin.message(sender, "find-wrongGroup", args[3]);
				return;
			}
		}

		// Find buy regions
		if(args[1].equalsIgnoreCase("buy")) {
			List<BuyRegion> regions = plugin.getFileManager().getBuys();
			List<BuyRegion> results = new ArrayList<>();
			for(BuyRegion region : regions) {
				if(!region.isSold()
						&& (region.getPrice() >= minPrice)
						&& (group == null || group.isMember(region))
						&& (region.getBooleanSetting("general.findCrossWorld") || player.getWorld().equals(region.getWorld()))) {
					results.add(region);
				}
			}
			if(!results.isEmpty()) {
				// Draw a random one
				BuyRegion region = results.get(new Random().nextInt(results.size()));
				Message onlyInGroup = Message.empty();
				if(group != null) {
					onlyInGroup = Message.fromKey("find-onlyInGroup").replacements(args[3]);
				}

				// Teleport
				plugin.message(player, "find-successMin", "buy", Utils.formatCurrency(minPrice), onlyInGroup, region);
				region.getTeleportFeature().teleportPlayer(player, region.getBooleanSetting("general.findTeleportToSign"), false);
			} else {
				Message onlyInGroup = Message.empty();
				if(group != null) {
					onlyInGroup = Message.fromKey("find-onlyInGroup").replacements(args[3]);
				}
				plugin.message(player, "find-noneFoundMin", "buy", Utils.formatCurrency(minPrice), onlyInGroup);
			}
		}

		// Find rental regions
		else {
			List<RentRegion> regions = plugin.getFileManager().getRents();
			List<RentRegion> results = new ArrayList<>();
			for(RentRegion region : regions) {
				if(!region.isRented()
						&& (region.getPrice() >= minPrice)
						&& (group == null || group.isMember(region))
						&& (region.getBooleanSetting("general.findCrossWorld") || player.getWorld().equals(region.getWorld()))) {
					results.add(region);
				}
			}
			if(!results.isEmpty()) {
				// Draw a random one
				RentRegion region = results.get(new Random().nextInt(results.size()));
				Message onlyInGroup = Message.empty();
				if(group != null) {
					onlyInGroup = Message.fromKey("find-onlyInGroup").replacements(args[3]);
				}

				// Teleport
				plugin.message(player, "find-successMin", "rent", Utils.formatCurrency(minPrice), onlyInGroup, region);
				region.getTeleportFeature().teleportPlayer(player, region.getBooleanSetting("general.findTeleportToSign"), false);
			} else {
				Message onlyInGroup = Message.empty();
				if(group != null) {
					onlyInGroup = Message.fromKey("find-onlyInGroup").replacements(args[3]);
				}
				plugin.message(player, "find-noneFoundMin", "rent", Utils.formatCurrency(minPrice), onlyInGroup);
			}
		}

	}

	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		ArrayList<String> result = new ArrayList<>();
		if(toComplete == 2) {
			result.add("buy");
			result.add("rent");
		}
		return result;
	}

}
