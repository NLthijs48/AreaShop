package nl.evolutioncoding.areashop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.Utils;
import nl.evolutioncoding.areashop.regions.RentRegion;

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
		if(!sender.hasPermission("areashop.unrent") && !sender.hasPermission("areashop.unrentown")) {
			plugin.message(sender, "unrent-noPermission");
			return;
		}
		RentRegion rent = null;
		if(args.length <= 1) {
			if (sender instanceof Player) {
				// get the region by location
				List<RentRegion> regions = Utils.getApplicableRentRegions(((Player) sender).getLocation());
				if (regions.isEmpty()) {
					plugin.message(sender, "cmd-noRegionsAtLocation");
					return;
				} else if (regions.size() > 1) {
					plugin.message(sender, "cmd-moreRegionsAtLocation");
					return;
				} else {
					rent = regions.get(0);
				}
			} else {
				plugin.message(sender, "cmd-automaticRegionOnlyByPlayer");
				return;
			}			
		} else {
			rent = plugin.getFileManager().getRent(args[1]);
		}
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
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		ArrayList<String> result = new ArrayList<String>();
		if(toComplete == 2) {
			for(RentRegion region : plugin.getFileManager().getRents()) {
				if(region.isRented()) {
					result.add(region.getName());
				}
			}
		}
		return result;
	}
}








