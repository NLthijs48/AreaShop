package nl.evolutioncoding.areashop.commands;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.Utils;
import nl.evolutioncoding.areashop.regions.BuyRegion;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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
		if(target.hasPermission("areashop.stopresellall")) {
			return plugin.getLanguageManager().getLang("help-stopResellAll");
		} else if(target.hasPermission("areashop.stopresell")) {
			return plugin.getLanguageManager().getLang("help-stopResell");
		}
		return null;
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.stopresell") && !sender.hasPermission("areashop.stopresellall")) {
			plugin.message(sender, "stopresell-noPermissionOther");
			return;
		}

		BuyRegion buy;
		if(args.length <= 1) {
			if (sender instanceof Player) {
				// get the region by location
				List<BuyRegion> regions = Utils.getApplicableBuyRegions(((Player) sender).getLocation());
				if (regions.isEmpty()) {
					plugin.message(sender, "cmd-noRegionsAtLocation");
					return;
				} else if (regions.size() > 1) {
					plugin.message(sender, "cmd-moreRegionsAtLocation");
					return;
				} else {
					buy = regions.get(0);
				}
			} else {
				plugin.message(sender, "cmd-automaticRegionOnlyByPlayer");
				return;
			}		
		} else {
			buy = plugin.getFileManager().getBuy(args[1]);
			if(buy == null) {
				plugin.message(sender, "stopresell-notRegistered", args[1]);
				return;
			}
		}
		if(buy == null) {
			plugin.message(sender, "stopresell-noRegionFound");
			return;
		}
		if(!buy.isInResellingMode()) {
			plugin.message(sender, "stopresell-notResell", buy);
			return;
		}
		if(sender.hasPermission("areashop.stopresellall")) {
			buy.disableReselling();
			plugin.message(sender, "stopresell-success", buy);
			buy.updateSigns();
			buy.updateRegionFlags();
		} else if(sender.hasPermission("areashop.stopresell") && sender instanceof Player) {
			if(buy.isOwner((Player)sender)) {
				buy.disableReselling();
				plugin.message(sender, "stopresell-success", buy);
				buy.updateSigns();
				buy.updateRegionFlags();
			} else {
				plugin.message(sender, "stopresell-noPermissionOther");
			}
		} else {
			plugin.message(sender, "stopresell-noPermission");
		}	
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		ArrayList<String> result = new ArrayList<>();
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
















