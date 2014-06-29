package nl.evolutioncoding.AreaShop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.AreaShop.AreaShop;
import nl.evolutioncoding.AreaShop.regions.BuyRegion;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuyCommand extends CommandAreaShop {

	public BuyCommand(AreaShop plugin) {
		super(plugin);
	}	
	
	@Override
	public String getCommandStart() {
		return "areashop buy";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.buy")) {
			return plugin.getLanguageManager().getLang("help-buy");
		}
		return null;
	}
	
	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if (!(sender instanceof Player)) {
			plugin.message(sender, "onlyByPlayer");
			return;
		}
		Player player = (Player)sender;
		if(args.length > 1 && args[1] != null) {
			BuyRegion region = plugin.getFileManager().getBuy(args[1]);
			if(region == null) {
				plugin.message(player, "buy-notBuyable");
			} else {
				region.buy(player);
			}
		} else {
			plugin.message(player, "buy-help");
		}
	}

	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		ArrayList<String> result = new ArrayList<String>();
		if(toComplete == 2) {
			for(BuyRegion region : plugin.getFileManager().getBuys().values()) {
				if(!region.isSold()) {
					result.add(region.getName());
				}
			}
		}
		return result;
	}
}
