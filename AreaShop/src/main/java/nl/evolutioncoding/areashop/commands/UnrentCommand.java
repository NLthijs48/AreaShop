package nl.evolutioncoding.areashop.commands;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.Utils;
import nl.evolutioncoding.areashop.regions.RentRegion;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.unrent") && !sender.hasPermission("areashop.unrentown")) {
			plugin.message(sender, "unrent-noPermission");
			return;
		}
		RentRegion rent;
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
			plugin.message(sender, "unrent-notRented", rent);
			return;
		}
		rent.unRent(true, sender);
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		ArrayList<String> result = new ArrayList<>();
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








