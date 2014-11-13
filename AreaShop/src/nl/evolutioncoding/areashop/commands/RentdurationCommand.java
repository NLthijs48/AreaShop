package nl.evolutioncoding.areashop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import nl.evolutioncoding.areashop.regions.RentRegion;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RentdurationCommand extends CommandAreaShop {

	public RentdurationCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop rentduration";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.rentduration")) {
			return plugin.getLanguageManager().getLang("help-rentduration");
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if(!sender.hasPermission("areashop.rentduration")) {
			plugin.message(sender, "rentduration-noPermission");
			return;
		}
		if(args.length < 3 || args[1] == null || args[2] == null) {
			plugin.message(sender, "rentduration-help");
			return;
		}
		RentRegion rent = null;
		if(args.length <= 3) {
			if(sender instanceof Player) {
				// get the region by location
				List<GeneralRegion> regions = plugin.getFileManager().getApplicalbeASRegions(((Player)sender).getLocation());
				if(regions.size() != 1) {
					plugin.message(sender, "rentduration-help");
					return;
				} else {
					if(regions.get(0).isRentRegion()) {
						rent = (RentRegion)regions.get(0);
					}
				}				
			} else {
				plugin.message(sender, "rentduration-help");
				return;
			}			
		} else {
			rent = plugin.getFileManager().getRent(args[3]);
		}
		if(rent == null) {
			plugin.message(sender, "rentduration-notRegistered", args[3]);
			return;
		}
		try {
			Integer.parseInt(args[1]);
		} catch(NumberFormatException e) {
			plugin.message(sender, "rentduration-wrongAmount", args[1]);
			return;
		}
		if(!plugin.checkTimeFormat(args[1] + " " + args[2])) {
			plugin.message(sender, "rentduration-wrongFormat", args[1]+" "+args[2]);
			return;
		}					
		rent.setDuration(args[1]+" "+args[2]);
		rent.updateRegionFlags();
		rent.updateSigns();
		rent.saveRequired();
		plugin.message(sender, "rentduration-success", rent.getName(), rent.getDurationString());
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		List<String> result = new ArrayList<String>();
		if(toComplete == 2) {
			result = plugin.getFileManager().getRentNames();
		} else if(toComplete == 4) {
			result.addAll(plugin.getConfig().getStringList("minutes"));
			result.addAll(plugin.getConfig().getStringList("hours"));
			result.addAll(plugin.getConfig().getStringList("days"));
			result.addAll(plugin.getConfig().getStringList("months"));
			result.addAll(plugin.getConfig().getStringList("years"));
		}
		return result;
	}

}
