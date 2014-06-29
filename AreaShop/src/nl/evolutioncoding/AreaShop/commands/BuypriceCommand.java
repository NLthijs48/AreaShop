package nl.evolutioncoding.AreaShop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.AreaShop.AreaShop;
import nl.evolutioncoding.AreaShop.regions.BuyRegion;

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
		
		BuyRegion buy = plugin.getFileManager().getBuy(args[1]);
		if(buy == null) {
			plugin.message(sender, "buyprice-notRegistered", args[1]);
			return;
		} 

		double price = 0.0;
		try {
			price = Double.parseDouble(args[2]);
		} catch(NumberFormatException e) {
			plugin.message(sender, "buyprice-wrongPrice", args[2]);
			return;
		}
		
		buy.setPrice(price);
		buy.save();
		plugin.message(sender, "buyprice-success", buy.getName(), args[2]);
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		List<String> result = new ArrayList<String>();
		if(toComplete == 2) {
			result = plugin.getFileManager().getBuyNames();		
		}
		return result;
	}

}






















