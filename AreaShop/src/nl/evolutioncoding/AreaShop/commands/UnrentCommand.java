package nl.evolutioncoding.AreaShop.commands;

import java.util.HashMap;

import nl.evolutioncoding.AreaShop.AreaShop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnrentCommand extends CommandAreaShop {

	public UnrentCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop unrent";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.unrent")) {
			return plugin.getLanguageManager().getLang("help-unrent");
		} else if(target.hasPermission("areashop.unrentown")) {
			return plugin.getLanguageManager().getLang("help-unrentOwn");
		}
		return null;
	}
	
	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if(args.length <= 1 || args[1] == null) {
			plugin.message(sender, "unrent-help");
			return;
		}		
		HashMap<String,String> rent = plugin.getFileManager().getRent(args[1]);
		if(rent == null) {
			plugin.message(sender, "unrent-notRegistered");
			return;
		}		
		if(rent.get(AreaShop.keyPlayerUUID) == null) {
			plugin.message(sender, "unrent-notRented");
			return;
		}		
		if(sender.hasPermission("areashop.unrent")) {
			plugin.message(sender, "unrent-other", plugin.toName(rent.get(AreaShop.keyPlayerUUID)));
			plugin.getFileManager().unRent(args[1], true);
		} else {
			if(sender.hasPermission("areashop.unrentown") && sender instanceof Player) {
				if(rent.get(AreaShop.keyPlayerUUID).equals(((Player)sender).getUniqueId().toString())) {
					plugin.message(sender, "unrent-unrented");
					plugin.getFileManager().unRent(args[1], true);
				} else {
					plugin.message(sender, "unrent-noPermissionOther");
				}
			} else {
				plugin.message(sender, "unrent-noPermission");
			}
		}			
	}
}








