package nl.evolutioncoding.areashop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.Utils;
import nl.evolutioncoding.areashop.regions.BuyRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SellCommand extends CommandAreaShop {

	public SellCommand(AreaShop plugin) {
		super(plugin);
	}	
	
	@Override
	public String getCommandStart() {
		return "areashop sell";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.sell")) {
			return plugin.getLanguageManager().getLang("help-sell");
		} else if(target.hasPermission("areashop.sellown")) {
			return plugin.getLanguageManager().getLang("help-sellOwn");
		}
		return null;
	}
	
	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if(!sender.hasPermission("areashop.sell") && !sender.hasPermission("areashop.sellown")) {
			plugin.message(sender, "sell-noPermission");
			return;
		}
		BuyRegion buy = null;
		if(args.length <= 1) {
			if (sender instanceof Player) {
				// get the region by location
				List<BuyRegion> regions = plugin.getFileManager().getApplicableBuyRegions(((Player) sender).getLocation());
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
		}
		if(buy == null) {
			plugin.message(sender, "sell-notRegistered");
			return;
		}
		if(!buy.isSold()) {
			plugin.message(sender, "sell-notBought");
			return;
		}
		//check if it has childrens
		List<GeneralRegion> activechilds = Utils.getActiveRegions(Utils.getParentChilds(buy));
        if(!activechilds.isEmpty()){
            plugin.message(sender, "require-childsFail", buy.getName(), StringUtils.join(Utils.getRegionsNames(activechilds),", "));
            return;
        }
        
		if(sender.hasPermission("areashop.sell")) {
			plugin.message(sender, "sell-sold", buy.getPlayerName());
			buy.sell(true);
		} else {
			if(sender.hasPermission("areashop.sellown") && sender instanceof Player) {
				if(buy.getBuyer().equals(((Player)sender).getUniqueId())) {
					plugin.message(sender, "sell-soldYours");
					buy.sell(true);
				} else {
					plugin.message(sender, "sell-noPermissionOther");
				}
			} else {
				plugin.message(sender, "sell-noPermission");
			}									
		}		
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		ArrayList<String> result = new ArrayList<String>();
		if(toComplete == 2) {
			for(BuyRegion region : plugin.getFileManager().getBuys()) {
				if(region.isSold()) {
					result.add(region.getName());
				}
			}
		}
		return result;
	}
}
















