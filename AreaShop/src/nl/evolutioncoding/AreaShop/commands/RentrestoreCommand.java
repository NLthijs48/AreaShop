package nl.evolutioncoding.AreaShop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.AreaShop.AreaShop;
import nl.evolutioncoding.AreaShop.regions.RentRegion;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class RentrestoreCommand extends CommandAreaShop {

	public RentrestoreCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop rentrestore";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.rentrestore")) {
			return plugin.getLanguageManager().getLang("help-rentrestore");
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if(!sender.hasPermission("areashop.rentrestore")) {
			plugin.message(sender, "rentrestore-noPermission");
			return;
		}
		if(args.length <= 2 || args[1] == null || args[2] == null) {
			plugin.message(sender, "rentrestore-help");
			return;
		}
		RentRegion rent = plugin.getFileManager().getRent(args[1]);
		if(rent == null) {
			plugin.message(sender, "rentrestore-notRegistered", args[1]);
			return;
		}
		String value = null;
		if(args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false") || args[2].equalsIgnoreCase("general")) {
			value = args[2].toLowerCase();
		} else {
			plugin.message(sender, "rentrestore-invalidSetting", args[2]);
			return;
		}
		rent.setRestoreSetting(value);
		if(args.length > 3) {
			rent.setRestoreProfile(args[3]);
			plugin.message(sender, "rentrestore-successProfile", rent.getName(), value, args[3]);
		} else {
			plugin.message(sender, "rentrestore-success", rent.getName(), value);
		}
		plugin.getFileManager().saveRents();
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		List<String> result = new ArrayList<String>();
		if(toComplete == 2) {
			result = plugin.getFileManager().getRentNames();
		} else if(toComplete == 3) {
			result.add("true");
			result.add("false");
			result.add("general");
		} else if(toComplete == 4) {
			result.addAll(plugin.config().getConfigurationSection("rentSchematicProfiles").getKeys(false));
		}
		return result;
	}
}
