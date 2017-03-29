package me.wiefferink.areashop.commands;

import me.wiefferink.areashop.regions.BuyRegion;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.regions.RentRegion;
import me.wiefferink.areashop.tools.Utils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SchematiceventCommand extends CommandAreaShop {

	@Override
	public String getCommandStart() {
		return "areashop schemevent";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.schematicevents")) {
			return "help-schemevent";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.schematicevents")) {
			plugin.message(sender, "schemevent-noPermission");
			return;
		}

		if(args.length < 3 || args[1] == null || args[2] == null) {
			plugin.message(sender, "schemevent-help");
			return;
		}
		GeneralRegion region = plugin.getFileManager().getRegion(args[1]);
		if(region == null) {
			plugin.message(sender, "cmd-notRegistered", args[1]);
			return;
		}
		if(region.getRegion() == null) {
			plugin.message(sender, "general-noRegion", region);
			return;
		}
		GeneralRegion.RegionEvent event = null;
		boolean exception = false;
		try {
			event = GeneralRegion.RegionEvent.valueOf(args[2].toUpperCase());
		} catch(IllegalArgumentException e) {
			exception = true;
		}
		// Check for a totally wrong event or a non matching event
		if(exception) {
			ArrayList<String> values = new ArrayList<>();
			for(GeneralRegion.RegionEvent value : GeneralRegion.RegionEvent.values()) {
				values.add(value.getValue().toLowerCase());
			}
			plugin.message(sender, "schemevent-wrongEvent", args[2], Utils.createCommaSeparatedList(values), region);
			return;
		}
		region.handleSchematicEvent(event);
		region.update();
		plugin.message(sender, "schemevent-success", args[2], region);
	}

	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		ArrayList<String> result = new ArrayList<>();
		if(toComplete == 2) {
			result.addAll(plugin.getFileManager().getRegionNames());
		} else if(toComplete == 3) {
			GeneralRegion region = plugin.getFileManager().getRegion(start[2]);
			if(region != null) {
				if(region instanceof RentRegion) {
					result.addAll(Arrays.asList("created", "deleted", "rented", "unrented"));
				} else if(region instanceof BuyRegion) {
					result.addAll(Arrays.asList("created", "deleted", "bought", "sold"));
				}
			}
		}
		return result;
	}
}
