package me.wiefferink.areashop.commands;

import me.wiefferink.areashop.regions.RentRegion;
import me.wiefferink.areashop.tools.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SetdurationCommand extends CommandAreaShop {

	@Override
	public String getCommandStart() {
		return "areashop setduration";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.setduration")) {
			return "help-setduration";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.setduration") && (!sender.hasPermission("areashop.setduration.landlord") && sender instanceof Player)) {
			plugin.message(sender, "setduration-noPermission");
			return;
		}
		if(args.length < 3 || args[1] == null || args[2] == null) {
			plugin.message(sender, "setduration-help");
			return;
		}
		int regionArgument = 3;
		if(args.length >= 2 && ("default".equalsIgnoreCase(args[1]) || "reset".equalsIgnoreCase(args[1]))) {
			regionArgument = 2;
		}
		RentRegion rent;
		if(args.length <= regionArgument) {
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
			rent = plugin.getFileManager().getRent(args[regionArgument]);
		}
		if(rent == null) {
			plugin.message(sender, "setduration-notRegistered", args[regionArgument]);
			return;
		}
		if(!sender.hasPermission("areashop.setduration") && !(sender instanceof Player && rent.isLandlord(((Player)sender).getUniqueId()))) {
			plugin.message(sender, "setduration-noLandlord", rent);
			return;
		}
		if("default".equalsIgnoreCase(args[1]) || "reset".equalsIgnoreCase(args[1])) {
			rent.setDuration(null);
			rent.update();
			plugin.message(sender, "setduration-successRemoved", rent);
			return;
		}
		try {
			Integer.parseInt(args[1]);
		} catch(NumberFormatException e) {
			plugin.message(sender, "setduration-wrongAmount", args[1], rent);
			return;
		}
		if(!Utils.checkTimeFormat(args[1] + " " + args[2])) {
			plugin.message(sender, "setduration-wrongFormat", args[1] + " " + args[2], rent);
			return;
		}
		rent.setDuration(args[1] + " " + args[2]);
		rent.update();
		plugin.message(sender, "setduration-success", rent);
	}

	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		List<String> result = new ArrayList<>();
		if(toComplete == 3) {
			result.addAll(plugin.getConfig().getStringList("minutes"));
			result.addAll(plugin.getConfig().getStringList("hours"));
			result.addAll(plugin.getConfig().getStringList("days"));
			result.addAll(plugin.getConfig().getStringList("months"));
			result.addAll(plugin.getConfig().getStringList("years"));
		} else if(toComplete == 4) {
			result = plugin.getFileManager().getRentNames();
		}
		return result;
	}

}
