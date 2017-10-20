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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MeCommand extends CommandAreaShop {

	@Override
	public String getCommandStart() {
		return "areashop me";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.me")) {
			return "help-me";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.me")) {
			plugin.message(sender, "me-noPermission");
			return;
		}
		OfflinePlayer player = null;
		if(!(sender instanceof Player)) {
			if(args.length <= 1) {
				plugin.message(sender, "me-notAPlayer");
				return;
			}
		} else {
			player = (OfflinePlayer)sender;
		}
		if(args.length > 1) {
			player = Bukkit.getOfflinePlayer(args[1]);
			if(player == null) {
				plugin.message(sender, "me-noPlayer", args[1]);
				return;
			}
		}
		if(player == null) {
			return;
		}

		// Get the regions owned by the player
		Set<RentRegion> rentRegions = new HashSet<>();
		for(RentRegion region : plugin.getFileManager().getRents()) {
			if(region.isOwner(player)) {
				rentRegions.add(region);
			}
		}
		Set<BuyRegion> buyRegions = new HashSet<>();
		for(BuyRegion region : plugin.getFileManager().getBuys()) {
			if(region.isOwner(player)) {
				buyRegions.add(region);
			}
		}
		// Get the regions the player is added as friend
		Set<GeneralRegion> friendRegions = new HashSet<>();
		for(GeneralRegion region : plugin.getFileManager().getRegions()) {
			if(region.getFriendsFeature().getFriends().contains(player.getUniqueId())) {
				friendRegions.add(region);
			}
		}

		// Send messages
		boolean foundSome = !rentRegions.isEmpty() || !buyRegions.isEmpty() || !friendRegions.isEmpty();
		if(foundSome) {
			plugin.message(sender, "me-header", player.getName());
		}
		if(!rentRegions.isEmpty()) {
			for(RentRegion region : rentRegions) {
				plugin.messageNoPrefix(sender, "me-rentLine", region);
			}
		}
		if(!buyRegions.isEmpty()) {
			for(BuyRegion region : buyRegions) {
				plugin.messageNoPrefix(sender, "me-buyLine", region);
			}
		}
		if(!friendRegions.isEmpty()) {
			for(GeneralRegion region : friendRegions) {
				plugin.messageNoPrefix(sender, "me-friendLine", region);
			}
		}

		if(!foundSome) {
			plugin.message(sender, "me-nothing", player.getName());
		} else {
			plugin.messageNoPrefix(sender, "me-clickHint");
		}
	}

	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		ArrayList<String> result = new ArrayList<>();
		if(toComplete == 2) {
			for(Player player : Utils.getOnlinePlayers()) {
				result.add(player.getName());
			}
		}
		return result;
	}
}


























