package nl.evolutioncoding.areashop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import nl.evolutioncoding.areashop.regions.RentRegion;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetdurationCommand extends CommandAreaShop {

	public SetdurationCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop setduration";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.setduration")) {
			return plugin.getLanguageManager().getLang("help-setduration");
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if(!sender.hasPermission("areashop.setduration")) {
			plugin.message(sender, "setduration-noPermission");
			return;
		}
		if(args.length < 3 || args[1] == null || args[2] == null) {
			plugin.message(sender, "setduration-help");
			return;
		}
		RentRegion rent = null;
		if(args.length <= 3) {
			if(sender instanceof Player) {
				// get the region by location
				List<GeneralRegion> regions = plugin.getFileManager().getApplicalbeASRegions(((Player)sender).getLocation());
				if(regions.size() != 1) {
					plugin.message(sender, "setduration-help");
					return;
				} else {
					if(regions.get(0).isRentRegion()) {
						rent = (RentRegion)regions.get(0);
					}
				}				
			} else {
				plugin.message(sender, "setduration-help");
				return;
			}			
		} else {
			rent = plugin.getFileManager().getRent(args[3]);
		}
		if(rent == null) {
			plugin.message(sender, "setduration-notRegistered", args[3]);
			return;
		}
		try {
			Integer.parseInt(args[1]);
		} catch(NumberFormatException e) {
			plugin.message(sender, "setduration-wrongAmount", args[1]);
			return;
		}
		if(!plugin.checkTimeFormat(args[1] + " " + args[2])) {
			plugin.message(sender, "setduration-wrongFormat", args[1]+" "+args[2]);
			return;
		}					
		rent.setDuration(args[1]+" "+args[2]);
		rent.updateRegionFlags();
		rent.updateSigns();
		rent.saveRequired();
		plugin.message(sender, "setduration-success", rent.getName(), rent.getDurationString());
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
