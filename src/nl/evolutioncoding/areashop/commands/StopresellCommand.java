package nl.evolutioncoding.areashop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.regions.BuyRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// TODO ALL
public class StopresellCommand extends CommandAreaShop {

	public StopresellCommand(AreaShop plugin) {
		super(plugin);
	}	
	
	@Override
	public String getCommandStart() {
		return "areashop stopresell";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.stopresell")) {
			return plugin.getLanguageManager().getLang("help-stopResell");
		} else if(target.hasPermission("areashop.stopresellall")) {
			plugin.getLanguageManager().getLang("help-stopResellAll");
		}
		return null;
	}
	
	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		BuyRegion buy = null;
		if(args.length <= 1) {
			if(sender instanceof Player) {
				// get the region by location
				List<GeneralRegion> regions = plugin.getFileManager().getApplicalbeASRegions(((Player)sender).getLocation());
				if(regions.size() != 1) {
					plugin.message(sender, "stopResell-help");
					return;
				} else {
					if(regions.get(0).isBuyRegion()) {
						buy = (BuyRegion)regions.get(0);
					}
				}				
			} else {
				plugin.message(sender, "stopResell-help");
				return;
			}			
		} else {
			buy = plugin.getFileManager().getBuy(args[1]);
			if(buy == null) {
				plugin.message(sender, "stopResell-notRegistered", args[1]);
				return;
			}
		}
		if(buy == null) {
			plugin.message(sender, "stopResell-noRegionFound");
			return;
		}
		if(!buy.isInResellingMode()) {
			plugin.message(sender, "stopResell-notResell", buy);
			return;
		}
		if(sender.hasPermission("areashop.stopresellall")) {
			buy.disableReselling();
			buy.saveRequired();
			plugin.message(sender, "stopResell-success", buy);
			buy.updateSigns();
			buy.updateRegionFlags();
		} else if(sender.hasPermission("areashop.stopresell") && sender instanceof Player) {
			if(buy.isOwner((Player)sender)) {
				buy.disableReselling();
				buy.saveRequired();
				plugin.message(sender, "stopResell-success", buy);
				buy.updateSigns();
				buy.updateRegionFlags();
			} else {
				plugin.message(sender, "stopResell-noPermissionOther");
			}
		} else {
			plugin.message(sender, "stopResell-noPermission");
		}	
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		ArrayList<String> result = new ArrayList<String>();
		if(toComplete == 2) {
			for(BuyRegion region : plugin.getFileManager().getBuys()) {
				if(region.isSold() && region.isInResellingMode()) {
					result.add(region.getName());
				}
			}
		}
		return result;
	}
}
















