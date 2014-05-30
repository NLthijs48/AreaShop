package nl.evolutioncoding.AreaShop.commands;

import java.util.HashMap;

import nl.evolutioncoding.AreaShop.AreaShop;

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
		HashMap<String,String> rent = plugin.getFileManager().getRent(args[1]);
		if(rent == null) {
			plugin.message(sender, "rentrestore-notRegistered", args[1]);
			return;
		}
		String value = null;
		if(args[2].equalsIgnoreCase("true")) {
			value = "true";
		} else if(args[2].equalsIgnoreCase("false")) {
			value = "false";
		} else if(args[2].equalsIgnoreCase("general")) {
			value = "general";
		} else {
			plugin.message(sender, "rentrestore-invalidSetting", args[2]);
			return;
		}
		rent.put(AreaShop.keyRestore, value);
		if(args.length > 3) {
			rent.put(AreaShop.keySchemProfile, args[3]);
			plugin.message(sender, "rentrestore-successProfile", rent.get(AreaShop.keyName), value, args[3]);
		} else {
			plugin.message(sender, "rentrestore-success", rent.get(AreaShop.keyName), value);
		}
		plugin.getFileManager().saveRents();
	}
}
