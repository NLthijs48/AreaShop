package me.wiefferink.areashop.commands;

import me.wiefferink.areashop.regions.RentRegion;
import me.wiefferink.areashop.tools.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RentCommand extends CommandAreaShop {

	@Override
	public String getCommandStart() {
		return "areashop rent";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.rent")) {
			return "help-rent";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.rent")) {
			plugin.message(sender, "rent-noPermission");
			return;
		}
		if(!(sender instanceof Player)) {
			plugin.message(sender, "cmd-onlyByPlayer");
			return;
		}
		Player player = (Player)sender;
		if(args.length > 1 && args[1] != null) {
			RentRegion rent = plugin.getFileManager().getRent(args[1]);
			if(rent == null) {
				plugin.message(sender, "rent-notRentable", args[1]);
			} else {
				rent.rent(player);
			}
		} else {
			// get the region by location
			List<RentRegion> regions = Utils.getImportantRentRegions(((Player)sender).getLocation());
			if(regions.isEmpty()) {
				plugin.message(sender, "cmd-noRegionsAtLocation");
			} else if(regions.size() > 1) {
				plugin.message(sender, "cmd-moreRegionsAtLocation");
			} else {
				regions.get(0).rent(player);
			}
		}
	}

	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		ArrayList<String> result = new ArrayList<>();
		if(toComplete == 2) {
			for(RentRegion region : plugin.getFileManager().getRents()) {
				if(!region.isRented()) {
					result.add(region.getName());
				}
			}
		}
		return result;
	}
}
