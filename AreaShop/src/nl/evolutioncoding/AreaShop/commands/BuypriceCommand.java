package nl.evolutioncoding.AreaShop.commands;

import java.util.HashMap;

import nl.evolutioncoding.AreaShop.AreaShop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class BuypriceCommand extends CommandAreaShop {

	public BuypriceCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop buyprice";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.buyprice")) {
			return plugin.getLanguageManager().getLang("help-buyprice");
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if(!sender.hasPermission("areashop.buyprice")) {
			plugin.message(sender, "buyprice-noPermission");
			return;
		}
		
		if(args.length < 3 || args[1] == null || args[2] == null) {
			plugin.message(sender, "buyprice-help");
			return;
		}
		
		HashMap<String,String> buy = plugin.getFileManager().getBuy(args[1]);
		if(buy == null) {
			plugin.message(sender, "buyprice-notRegistered", args[1]);
			return;
		} 

		try {
			Double.parseDouble(args[2]);
		} catch(NumberFormatException e) {
			plugin.message(sender, "buyprice-wrongPrice", args[2]);
			return;
		}
		
		buy.put(AreaShop.keyPrice, args[2]);
		plugin.getFileManager().saveBuys();
		plugin.getFileManager().updateBuySign(args[1]);
		plugin.getFileManager().updateBuyRegion(args[1]);
		plugin.message(sender, "buyprice-success", buy.get(AreaShop.keyName), args[2]);
	}

}
