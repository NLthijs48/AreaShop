package nl.evolutioncoding.AreaShop.commands;

import java.util.HashMap;

import nl.evolutioncoding.AreaShop.AreaShop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class RentpriceCommand extends CommandAreaShop {

	public RentpriceCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop rentprice";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.rentprice")) {
			return plugin.getLanguageManager().getLang("help-rentprice");
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if(!sender.hasPermission("areashop.rentprice")) {
			plugin.message(sender, "rentprice-noPermission");
			return;
		}		
		if(args.length < 3 || args[1] == null || args[2] == null) {
			plugin.message(sender, "rentprice-help");
			return;
		}		
		HashMap<String,String> rent = plugin.getFileManager().getRent(args[1]);
		if(rent == null) {
			plugin.message(sender, "rentprice-notRegistered", args[1]);
			return;
		} 		
		try {
			Double.parseDouble(args[2]);
		} catch(NumberFormatException e) {
			plugin.message(sender, "rentprice-wrongPrice", args[2]);
			return;
		}		
		rent.put(AreaShop.keyPrice, args[2]);
		plugin.getFileManager().saveRents();
		plugin.getFileManager().updateRentSign(args[1]);
		plugin.getFileManager().updateRentRegion(args[1]);
		plugin.message(sender, "rentprice-success", rent.get(AreaShop.keyName), args[2], rent.get(AreaShop.keyDuration));
	}

}
