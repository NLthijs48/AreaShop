package nl.evolutioncoding.areashop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.regions.GeneralRegion;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetrestoreCommand extends CommandAreaShop {

	public SetrestoreCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop setrestore";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.setrestore")) {
			return plugin.getLanguageManager().getLang("help-setrestore");
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
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
		String value = null;
		if(args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false") || args[2].equalsIgnoreCase("general")) {
			value = args[2].toLowerCase();
		} else {
			plugin.message(sender, "setrestore-invalidSetting", args[2]);
			return;
		}
		region.setRestoreSetting(value);
		if(args.length > 3) {
			region.setRestoreProfile(args[3]);
			plugin.message(sender, "setrestore-successProfile", region.getName(), value, args[3]);
		} else {
			plugin.message(sender, "setrestore-success", region.getName(), value);
		}
		region.saveRequired();
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		List<String> result = new ArrayList<String>();
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
