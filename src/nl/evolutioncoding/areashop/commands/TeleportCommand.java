package nl.evolutioncoding.areashop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.regions.GeneralRegion;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportCommand extends CommandAreaShop {

	public TeleportCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop tp";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.teleportall")) {
			return plugin.getLanguageManager().getLang("help-teleportAll");
		} else if(target.hasPermission("areashop.teleport")) {
			return plugin.getLanguageManager().getLang("help-teleport");
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if(!sender.hasPermission("areashop.teleport") && !sender.hasPermission("areashop.teleportall") && !sender.hasPermission("areashop.teleportsign") && !sender.hasPermission("areashop.teleportsignall")) {
			plugin.message(sender, "teleport-noPermission");
			return;
		}
		if (!(sender instanceof Player)) {
			plugin.message(sender, "cmd-onlyByPlayer");
			return;
		}		
		if(args.length <= 1 || args[1] == null) {
			plugin.message(sender, "teleport-help");
			return;
		}
		Player player = (Player)sender;
		GeneralRegion region = plugin.getFileManager().getRegion(args[1]);
		if(region == null) {
			plugin.message(player, "teleport-noRentOrBuy", args[1]);
			return;
		}
		if(args.length >= 3 && (args[2].equalsIgnoreCase("sign") || args[2].equalsIgnoreCase("yes") || args[2].equalsIgnoreCase("true"))) {
			region.teleportPlayer(player, true);
		} else {
			region.teleportPlayer(player, false);
		}

	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		ArrayList<String> result = new ArrayList<String>();
		if(toComplete == 2) {
			result.addAll(plugin.getFileManager().getRegionNames());
		} else if(toComplete == 3) {
			result.add("sign");
		}
		return result;
	}

}
