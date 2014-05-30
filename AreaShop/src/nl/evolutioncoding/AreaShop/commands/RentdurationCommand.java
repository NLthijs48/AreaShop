package nl.evolutioncoding.AreaShop.commands;

import java.util.HashMap;

import nl.evolutioncoding.AreaShop.AreaShop;

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
		HashMap<String,String> rent = plugin.getFileManager().getRent(args[1]);
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
		rent.put(AreaShop.keyDuration, args[2]+" "+args[3]);
		plugin.getFileManager().saveRents();
		plugin.getFileManager().updateRentSign(args[1]);
		plugin.getFileManager().updateRentRegion(args[1]);
		plugin.message(sender, "rentduration-success", rent.get(AreaShop.keyName), args[2]+" "+args[3]);
	}

}
