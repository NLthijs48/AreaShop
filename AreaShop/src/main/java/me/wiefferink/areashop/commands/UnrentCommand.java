package me.wiefferink.areashop.commands;

import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.regions.RentRegion;
import me.wiefferink.areashop.tools.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class UnrentCommand extends CommandAreaShop {

	@Override
	public String getCommandStart() {
		return "areashop unrent";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.unrent") || target.hasPermission("areashop.unrentown")) {
			return "help-unrent";
		}
		return null;
	}

	/**
	 * Check if a person can unrent the region.
	 * @param person The person to check
	 * @param region The region to check for
	 * @return true if the person can unrent it, otherwise false
	 */
	public static boolean canUse(CommandSender person, GeneralRegion region) {
		if(person.hasPermission("areashop.unrent")) {
			return true;
		}
		if(person instanceof Player) {
			Player player = (Player)person;
			return region.isOwner(player) && person.hasPermission("areashop.unrentown");
		}
		return false;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.unrent") && !sender.hasPermission("areashop.unrentown")) {
			plugin.message(sender, "unrent-noPermission");
			return;
		}
		RentRegion rent;
		if(args.length <= 1) {
			if(sender instanceof Player) {
				// get the region by location
				List<RentRegion> regions = Utils.getImportantRentRegions(((Player)sender).getLocation());
				if(regions.isEmpty()) {
					plugin.message(sender, "cmd-noRegionsAtLocation");
					return;
				} else if(regions.size() > 1) {
					plugin.message(sender, "cmd-moreRegionsAtLocation");
					return;
				} else {
					rent = regions.get(0);
				}
			} else {
				plugin.message(sender, "cmd-automaticRegionOnlyByPlayer");
				return;
			}
		} else {
			rent = plugin.getFileManager().getRent(args[1]);
		}
		if(rent == null) {
			plugin.message(sender, "unrent-notRegistered");
			return;
		}
		if(!rent.isRented()) {
			plugin.message(sender, "unrent-notRented", rent);
			return;
		}
		rent.unRent(true, sender);
	}

	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		ArrayList<String> result = new ArrayList<>();
		if(toComplete == 2) {
			for(RentRegion region : plugin.getFileManager().getRents()) {
				if(region.isRented()) {
					result.add(region.getName());
				}
			}
		}
		return result;
	}
}








