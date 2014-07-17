package nl.evolutioncoding.AreaShop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.AreaShop.AreaShop;
import nl.evolutioncoding.AreaShop.regions.RentRegion;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

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
		if(args.length < 4 || args[1] == null || args[2] == null || args[3] == null) {
			plugin.message(sender, "rentduration-help");
			return;
		}
		RentRegion rent = plugin.getFileManager().getRent(args[1]);
		if(rent == null) {
			plugin.message(sender, "rentduration-notRegistered", args[1]);
			return;
		}
		try {
			Integer.parseInt(args[2]);
		} catch(NumberFormatException e) {
			plugin.message(sender, "rentduration-wrongAmount", args[2]);
			return;
		}
		if(!plugin.checkTimeFormat(args[2] + " " + args[3])) {
			plugin.message(sender, "rentduration-wrongFormat", args[2]+" "+args[3]);
			return;
		}					
		rent.setDuration(args[2]+" "+args[3]);
		rent.save();
		plugin.message(sender, "rentduration-success", rent.getName(), rent.getDurationString());
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		List<String> result = new ArrayList<String>();
		if(toComplete == 2) {
			result = plugin.getFileManager().getRentNames();
		} else if(toComplete == 4) {
			result.addAll(plugin.config().getStringList("minutes"));
			result.addAll(plugin.config().getStringList("hours"));
			result.addAll(plugin.config().getStringList("days"));
			result.addAll(plugin.config().getStringList("months"));
			result.addAll(plugin.config().getStringList("years"));
		}
		return result;
	}

}
