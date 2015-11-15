package nl.evolutioncoding.areashop.commands;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LinksignsCommand extends CommandAreaShop {

	public LinksignsCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop linksigns";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.linksigns")) {
			return plugin.getLanguageManager().getLang("help-linksigns");
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.linksigns")) {
			plugin.message(sender, "linksigns-noPermission");
			return;
		}		
		if (!(sender instanceof Player)) {
			plugin.message(sender, "cmd-onlyByPlayer");
			return;
		}
		
		Player player = (Player)sender;
		if(plugin.getSignlinkerManager().isInSignLinkMode(player)) {
			plugin.getSignlinkerManager().exitSignLinkMode(player);
		} else {
			// Get the profile
			String profile = null;
			if(args.length > 1) {
				profile = args[1];
				Set<String> profiles = plugin.getConfig().getConfigurationSection("signProfiles").getKeys(false);
				if(!profiles.contains(profile)) {
					plugin.message(sender, "addsign-wrongProfile", Utils.createCommaSeparatedList(profiles));
					return;
				}
			}
			plugin.getSignlinkerManager().enterSignLinkMode(player, profile);
		}
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		List<String> result = new ArrayList<>();
		if(toComplete == 2) {
			result.addAll(plugin.getFileManager().getRegionNames());
		} else if(toComplete == 3) {
			result.addAll(plugin.getConfig().getStringList("signProfiles"));
		}
		return result;
	}

}










