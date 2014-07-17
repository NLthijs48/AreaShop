package nl.evolutioncoding.AreaShop.commands;

import nl.evolutioncoding.AreaShop.AreaShop;
import nl.evolutioncoding.AreaShop.regions.BuyRegion;

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
			plugin.getLanguageManager().getLang("help-sellOwn");
		}
		return null;
	}
	
	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if(args.length <= 1 || args[1] == null) {
			plugin.message(sender, "sell-help");
			return;
		}
		BuyRegion buy = plugin.getFileManager().getBuy(args[1]);
		if(buy == null) {
			plugin.message(sender, "sell-notRegistered");
			return;
		}
		if(!buy.isSold()) {
			plugin.message(sender, "sell-notBought");
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
}
















