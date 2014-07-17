package nl.evolutioncoding.AreaShop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.AreaShop.AreaShop;
import nl.evolutioncoding.AreaShop.regions.RentRegion;

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
		RentRegion rent = plugin.getFileManager().getRent(args[1]);
		if(rent == null) {
			plugin.message(sender, "rentprice-notRegistered", args[1]);
			return;
		} 	
		double price = 0.0;
		try {
			price = Double.parseDouble(args[2]);
		} catch(NumberFormatException e) {
			plugin.message(sender, "rentprice-wrongPrice", args[2]);
			return;
		}	
		rent.setPrice(price);
		rent.save();
		plugin.message(sender, "rentprice-success", rent.getName(), args[2], rent.getDurationString());
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		List<String> result = new ArrayList<String>();
		if(toComplete == 2) {
			result = plugin.getFileManager().getRentNames();		
		}
		return result;
	}

}
