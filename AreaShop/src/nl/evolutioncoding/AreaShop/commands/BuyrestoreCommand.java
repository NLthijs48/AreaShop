package nl.evolutioncoding.AreaShop.commands;

import java.util.HashMap;

import nl.evolutioncoding.AreaShop.AreaShop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class BuyrestoreCommand extends CommandAreaShop {

	public BuyrestoreCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop buyrestore";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.buyrestore")) {
			return plugin.getLanguageManager().getLang("help-buyrestore");
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if(sender.hasPermission("areashop.buyrestore")) {
			plugin.message(sender, "buyrestore-noPermission");
			return;
		}
		if(args.length <= 2 || args[1] == null || args[2] == null) {
			plugin.message(sender, "buyrestore-help");
			return;
		}
		HashMap<String,String> buy = plugin.getFileManager().getBuy(args[1]);
		if(buy == null) {
			plugin.message(sender, "buyrestore-notRegistered", args[1]);
		} else {
			String value = null;
			if(args[2].equalsIgnoreCase("true")) {
				buy.put(AreaShop.keyRestore, "true");
				value = "true";
			} else if(args[2].equalsIgnoreCase("false")) {
				buy.put(AreaShop.keyRestore, "false");
				value = "false";
			} else if(args[2].equalsIgnoreCase("general")) {
				buy.put(AreaShop.keyRestore, "general");
				value = "general";
			} else {
				plugin.message(sender, "buyrestore-invalidSetting", args[2]);
			}
			if(value != null) {
				if(args.length > 3) {
					buy.put(AreaShop.keySchemProfile, args[3]);
					plugin.message(sender, "buyrestore-successProfile", buy.get(AreaShop.keyName), value, args[3]);
				} else {
					plugin.message(sender, "buyrestore-success", buy.get(AreaShop.keyName), value);
				}
				plugin.getFileManager().saveBuys();
			}
		}
	}
}
