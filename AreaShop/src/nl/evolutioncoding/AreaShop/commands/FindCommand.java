package nl.evolutioncoding.areashop.commands;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.regions.BuyRegion;
import nl.evolutioncoding.areashop.regions.RegionGroup;
import nl.evolutioncoding.areashop.regions.RentRegion;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FindCommand extends CommandAreaShop {

	public FindCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop find";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.find")) {
			return plugin.getLanguageManager().getLang("help-find");
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if(!sender.hasPermission("areashop.find")) {
			plugin.message(sender, "find-noPermission");
			return;
		}	
		if (!(sender instanceof Player)) {
			plugin.message(sender, "cmd-onlyByPlayer");
			return;
		}		
		if(args.length <= 1 || args[1] == null || (!args[1].equalsIgnoreCase("buy") && !args[1].equalsIgnoreCase("rent"))) {
			plugin.message(sender, "find-help");
			return;
		}
		Player player = (Player)sender;
		double balance = plugin.getEconomy().getBalance(player);
		double maxPrice = 0;
		boolean maxPriceSet = false;
		RegionGroup group = null;
		// Parse optional price argument
		if(args.length >= 3) {
			try {
				maxPrice = Double.parseDouble(args[2]);
				maxPriceSet = true;
			} catch(NumberFormatException e) {
				plugin.message(sender, "find-wrongMaxPrice", args[2]);
				return;
			}
		}	
		// Parse optional group argument
		if(args.length >= 4) {
			group = plugin.getFileManager().getGroup(args[3]);
			if(group == null) {
				plugin.message(sender, "find-wrongGroup", args[3]);
				return;
			}
		}
		if(args[1].equalsIgnoreCase("buy")) {
			List<BuyRegion> regions = plugin.getFileManager().getBuys();
			for(BuyRegion region : regions) {
				if(!region.isSold() 
						&& ((region.getPrice() <= balance && !maxPriceSet) || (region.getPrice() <= maxPrice && maxPriceSet)) 
						&& (group == null || group.isMember(region))) {
					BigDecimal bigDecimal = new BigDecimal(balance);
				    bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_UP);
				    balance = bigDecimal.doubleValue();
				    String onlyInGroup = "";
					if(group != null) {
						onlyInGroup = plugin.getLanguageManager().getLang("find-onlyInGroup", args[3]);
					}
					if(maxPriceSet) {
						plugin.message(player, "find-successMax", "buy", region.getName(), maxPrice, onlyInGroup);
					} else {
						plugin.message(player, "find-success", "buy", region.getName(), balance, onlyInGroup);
					}
					region.teleportPlayer(player, true);					
					return;
				}
			}		
			BigDecimal bigDecimal = new BigDecimal(balance);
		    bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_UP);
		    balance = bigDecimal.doubleValue();
		    String onlyInGroup = "";
			if(group != null) {
				onlyInGroup = plugin.getLanguageManager().getLang("find-onlyInGroup", args[3]);
			}
			if(maxPriceSet) {
				plugin.message(player, "find-noneFound", "buy", maxPrice, onlyInGroup);
			} else {
				plugin.message(player, "find-noneFoundMax", "buy", balance, onlyInGroup);
			}

		} else {
			List<RentRegion> regions = plugin.getFileManager().getRents();
			for(RentRegion region : regions) {
				if(!region.isRented() 
						&& ((region.getPrice() <= balance && !maxPriceSet) || (region.getPrice() <= maxPrice && maxPriceSet))
						&& (group == null || group.isMember(region))) {
					BigDecimal bigDecimal = new BigDecimal(balance);
				    bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_UP);
				    balance = bigDecimal.doubleValue();
				    String onlyInGroup = "";
					if(group != null) {
						onlyInGroup = plugin.getLanguageManager().getLang("find-onlyInGroup", args[3]);
					}
					if(maxPriceSet) {
						plugin.message(player, "find-successMax", "rent", region.getName(), maxPrice, onlyInGroup);
					} else {
						plugin.message(player, "find-success", "rent", region.getName(), balance, onlyInGroup);
					}
					region.teleportPlayer(player, true);					
					return;
				}
			}	
			BigDecimal bigDecimal = new BigDecimal(balance);
		    bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_UP);
		    balance = bigDecimal.doubleValue();
		    String onlyInGroup = "";
			if(group != null) {
				onlyInGroup = plugin.getLanguageManager().getLang("find-onlyInGroup", args[3]);
			}
			if(maxPriceSet) {
				plugin.message(player, "find-noneFound", "rent", maxPrice, onlyInGroup);
			} else {
				plugin.message(player, "find-noneFoundMax", "rent", balance, onlyInGroup);
			}
		}

	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		ArrayList<String> result = new ArrayList<String>();
		if(toComplete == 2) {
			result.add("buy");
			result.add("rent");
		}
		return result;
	}

}



























