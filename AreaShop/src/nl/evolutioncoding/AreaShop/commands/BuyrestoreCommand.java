package nl.evolutioncoding.AreaShop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.AreaShop.AreaShop;
import nl.evolutioncoding.AreaShop.regions.BuyRegion;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class BuyrestoreCommand extends CommandAreaShop {

	public BuyrestoreCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop buyrestore";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.buyrestore")) {
			return plugin.getLanguageManager().getLang("help-buyrestore");
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if(!sender.hasPermission("areashop.buyrestore")) {
			plugin.message(sender, "buyrestore-noPermission");
			return;
		}
		if(args.length <= 2 || args[1] == null || args[2] == null) {
			plugin.message(sender, "buyrestore-help");
			return;
		}
		BuyRegion buy = plugin.getFileManager().getBuy(args[1]);
		if(buy == null) {
			plugin.message(sender, "buyrestore-notRegistered", args[1]);
		} else {
			String value = null;
			if(args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false") || args[2].equalsIgnoreCase("general")) {
				value = args[2].toLowerCase();
				buy.setRestoreSetting(value);
			} else {
				plugin.message(sender, "buyrestore-invalidSetting", args[2]);
			}
			if(value != null) {
				if(args.length > 3) {
					buy.setRestoreProfile(args[3]);
					plugin.message(sender, "buyrestore-successProfile", buy.getName(), value, args[3]);
				} else {
					plugin.message(sender, "buyrestore-success", buy.getName(), value);
				}
				buy.save();
			}
		}
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		List<String> result = new ArrayList<String>();
		if(toComplete == 2) {
			result = plugin.getFileManager().getBuyNames();
		} else if(toComplete == 3) {
			result.add("true");
			result.add("false");
			result.add("general");
		} else if(toComplete == 4) {
			result.addAll(plugin.config().getConfigurationSection("buySchematicProfiles").getKeys(false));
		}
		return result;
	}
}

















