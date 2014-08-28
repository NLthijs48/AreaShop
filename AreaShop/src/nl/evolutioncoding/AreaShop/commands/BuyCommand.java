package nl.evolutioncoding.AreaShop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.AreaShop.AreaShop;
import nl.evolutioncoding.AreaShop.regions.BuyRegion;
import nl.evolutioncoding.AreaShop.regions.GeneralRegion;

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
			plugin.message(sender, "cmd-onlyByPlayer");
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
			// get the region by location
			List<GeneralRegion> regions = plugin.getFileManager().getApplicalbeASRegions(player.getLocation());
			if(regions.size() != 1) {
				plugin.message(sender, "buy-help");
			} else {
				if(!regions.get(0).isRentRegion()) {
					plugin.message(sender, "buy-notBuyable");
				} else {
					((BuyRegion)regions.get(0)).buy(player);
				}
			}
		}
	}

	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		ArrayList<String> result = new ArrayList<String>();
		if(toComplete == 2) {
			for(BuyRegion region : plugin.getFileManager().getBuys()) {
				if(!region.isSold()) {
					result.add(region.getName());
				}
			}
		}
		return result;
	}
}
