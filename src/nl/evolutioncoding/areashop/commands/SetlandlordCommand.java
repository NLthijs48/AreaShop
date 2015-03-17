package nl.evolutioncoding.areashop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.regions.GeneralRegion;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetlandlordCommand extends CommandAreaShop {

	public SetlandlordCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop setlandlord";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.setlandlord")) {
			return plugin.getLanguageManager().getLang("help-setlandlord");
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if(!sender.hasPermission("areashop.setlandlord")) {
			plugin.message(sender, "setlandlord-noPermission");
			return;
		}		
		if(args.length < 2) {
			plugin.message(sender, "setlandlord-help");
			return;
		}
		@SuppressWarnings("deprecation")
		OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
		if(player == null || player.getLastPlayed() == 0) {
			plugin.message(sender, "setlandlord-didNotPlayBefore", args[1]); // Using args[1] instead of playername because that could return nothing if not played before
		}		
		GeneralRegion region = null;
		if(args.length < 3) {
			if (sender instanceof Player) {
				// get the region by location
				List<GeneralRegion> regions = plugin.getFileManager().getAllApplicableRegions(((Player) sender).getLocation());
				if (regions.isEmpty()) {
					plugin.message(sender, "cmd-noRegionsAtLocation");
					return;
				} else if (regions.size() > 1) {
					plugin.message(sender, "cmd-moreRegionsAtLocation");
					return;
				} else {
					region = regions.get(0);
				}
			} else {
				plugin.message(sender, "cmd-automaticRegionOnlyByPlayer");
				return;
			}		
		} else {
			region = plugin.getFileManager().getRegion(args[2]);
		}	
		if(region == null) {
			plugin.message(player, "setlandlord-noRegion", args[2]);
			return;
		}		
		region.setLandlord(player.getUniqueId());
		String playerName = player.getName();
		if(playerName.isEmpty()) {
			playerName = args[1];
		}
		plugin.message(sender, "setlandlord-success", playerName, region.getName());
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		ArrayList<String> result = new ArrayList<String>();
		if(toComplete == 2) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				result.add(player.getName());
			}
		} else if(toComplete == 3) {
			result.addAll(plugin.getFileManager().getRegionNames());
		}
		return result;
	}

}
