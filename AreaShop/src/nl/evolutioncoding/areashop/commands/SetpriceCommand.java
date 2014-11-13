package nl.evolutioncoding.areashop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.regions.BuyRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import nl.evolutioncoding.areashop.regions.RentRegion;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetpriceCommand extends CommandAreaShop {

	public SetpriceCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop setprice";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.setprice")) {
			return plugin.getLanguageManager().getLang("help-setprice");
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if(!sender.hasPermission("areashop.setprice")) {
			plugin.message(sender, "setprice-noPermission");
			return;
		}		
		if(args.length < 2 || args[1] == null) {
			plugin.message(sender, "setprice-help");
			return;
		}		
		GeneralRegion region = null;
		if(args.length < 3) {
			if(sender instanceof Player) {
				// get the region by location
				List<GeneralRegion> regions = plugin.getFileManager().getApplicalbeASRegions(((Player)sender).getLocation());
				if(regions.size() != 1) {
					plugin.message(sender, "setprice-help");
					return;
				} else {
					region = regions.get(0);
				}				
			} else {
				plugin.message(sender, "setprice-help");
				return;
			}			
		} else {
			region = plugin.getFileManager().getRegion(args[2]);
		}		
		if(region == null) {
			plugin.message(sender, "setprice-notRegistered", args[2]);
			return;
		}
		double price = 0.0;
		try {
			price = Double.parseDouble(args[1]);
		} catch(NumberFormatException e) {
			plugin.message(sender, "setprice-wrongPrice", args[1]);
			return;
		}	
		if(region.isRentRegion()) {
			((RentRegion)region).setPrice(price);
			plugin.message(sender, "setprice-successRent", region.getName(), ((RentRegion)region).getFormattedPrice(), ((RentRegion)region).getDurationString());
		} else if(region.isBuyRegion()) {
			((BuyRegion)region).setPrice(price);
			plugin.message(sender, "setprice-successBuy", region.getName(), ((BuyRegion)region).getFormattedPrice());
		}
		region.updateSigns();
		region.updateRegionFlags();
		region.saveRequired();
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		List<String> result = new ArrayList<String>();
		if(toComplete == 2) {
			result = plugin.getFileManager().getRegionNames();		
		}
		return result;
	}

}
