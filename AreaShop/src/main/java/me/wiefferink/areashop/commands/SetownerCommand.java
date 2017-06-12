package me.wiefferink.areashop.commands;

import me.wiefferink.areashop.regions.BuyRegion;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.regions.RentRegion;
import me.wiefferink.areashop.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class SetownerCommand extends CommandAreaShop {

	@Override
	public String getCommandStart() {
		return "areashop setowner";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.setownerrent") || target.hasPermission("areashop.setownerbuy")) {
			return "help-setowner";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.setownerrent") && !sender.hasPermission("areashop.setownerbuy")) {
			plugin.message(sender, "setowner-noPermission");
			return;
		}
		GeneralRegion region;
		if(args.length < 2) {
			plugin.message(sender, "setowner-help");
			return;
		}
		if(args.length == 2) {
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
			plugin.message(sender, "setowner-notRegistered");
			return;
		}

		if(region instanceof RentRegion && !sender.hasPermission("areashop.setownerrent")) {
			plugin.message(sender, "setowner-noPermissionRent", region);
			return;
		}
		if(region instanceof BuyRegion && !sender.hasPermission("areashop.setownerbuy")) {
			plugin.message(sender, "setowner-noPermissionBuy", region);
			return;
		}

		UUID uuid = null;
		@SuppressWarnings("deprecation")
		OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
		if(player != null) {
			uuid = player.getUniqueId();
		}
		if(uuid == null) {
			plugin.message(sender, "setowner-noPlayer", args[1], region);
			return;
		}

		if(region instanceof RentRegion) {
			RentRegion rent = (RentRegion)region;
			if(rent.isRenter(uuid)) {
				// extend
				rent.setRentedUntil(rent.getRentedUntil() + rent.getDuration());
				rent.setRenter(uuid);
				plugin.message(sender, "setowner-succesRentExtend", region);
			} else {
				// change
				if(!rent.isRented()) {
					rent.setRentedUntil(Calendar.getInstance().getTimeInMillis() + rent.getDuration());
				}
				rent.setRenter(uuid);
				plugin.message(sender, "setowner-succesRent", region);
			}
		}
		if(region instanceof BuyRegion) {
			BuyRegion buy = (BuyRegion)region;
			buy.setBuyer(uuid);
			plugin.message(sender, "setowner-succesBuy", region);
		}
		region.getFriendsFeature().deleteFriend(region.getOwner(), null);
		region.update();
		region.saveRequired();
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








