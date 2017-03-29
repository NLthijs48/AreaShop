package me.wiefferink.areashop.commands;

import me.wiefferink.areashop.regions.GeneralRegion;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class SetrestoreCommand extends CommandAreaShop {

	@Override
	public String getCommandStart() {
		return "areashop setrestore";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.setrestore")) {
			return "help-setrestore";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.setrestore")) {
			plugin.message(sender, "setrestore-noPermission");
			return;
		}
		if(args.length <= 2 || args[1] == null || args[2] == null) {
			plugin.message(sender, "setrestore-help");
			return;
		}
		GeneralRegion region = plugin.getFileManager().getRegion(args[1]);
		if(region == null) {
			plugin.message(sender, "setrestore-notRegistered", args[1]);
			return;
		}
		Boolean value = null;
		if(args[2].equalsIgnoreCase("true")) {
			value = true;
		} else if(args[2].equalsIgnoreCase("false")) {
			value = false;
		}
		region.setRestoreSetting(value);
		String valueString = "general";
		if(value != null) {
			valueString = value + "";
		}
		if(args.length > 3) {
			region.setSchematicProfile(args[3]);
			plugin.message(sender, "setrestore-successProfile", valueString, args[3], region);
		} else {
			plugin.message(sender, "setrestore-success", valueString, region);
		}
		region.update();
	}

	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		List<String> result = new ArrayList<>();
		if(toComplete == 2) {
			result = plugin.getFileManager().getRegionNames();
		} else if(toComplete == 3) {
			result.add("true");
			result.add("false");
			result.add("general");
		} else if(toComplete == 4) {
			result.addAll(plugin.getConfig().getConfigurationSection("schematicProfiles").getKeys(false));
		}
		return result;
	}
}
