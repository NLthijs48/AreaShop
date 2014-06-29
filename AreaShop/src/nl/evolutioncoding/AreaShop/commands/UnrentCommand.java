package nl.evolutioncoding.AreaShop.commands;

import nl.evolutioncoding.AreaShop.AreaShop;
import nl.evolutioncoding.AreaShop.regions.RentRegion;

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
		RentRegion rent = plugin.getFileManager().getRent(args[1]);
		if(rent == null) {
			plugin.message(sender, "unrent-notRegistered");
			return;
		}		
		if(!rent.isRented()) {
			plugin.message(sender, "unrent-notRented");
			return;
		}		
		if(sender.hasPermission("areashop.unrent")) {
			plugin.message(sender, "unrent-other", rent.getPlayerName());
			rent.unRent(true);
		} else {
			if(sender.hasPermission("areashop.unrentown") && sender instanceof Player) {
				if(rent.getRenter().equals(((Player)sender).getUniqueId())) {
					plugin.message(sender, "unrent-unrented");
					rent.unRent(true);
				} else {
					plugin.message(sender, "unrent-noPermissionOther");
				}
			} else {
				plugin.message(sender, "unrent-noPermission");
			}
		}			
	}
}








