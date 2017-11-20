package me.wiefferink.areashop.commands;

import me.wiefferink.interactivemessenger.processing.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LinksignsCommand extends CommandAreaShop {

	@Override
	public String getCommandStart() {
		return "areashop linksigns";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.linksigns")) {
			return "help-linksigns";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.linksigns")) {
			plugin.message(sender, "linksigns-noPermission");
			return;
		}
		if(!(sender instanceof Player)) {
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
				ConfigurationSection signProfilesSection = plugin.getConfig().getConfigurationSection("signProfiles");
				if(signProfilesSection != null) {
					Set<String> profiles = signProfilesSection.getKeys(false);
					if(!profiles.contains(profile)) {
						ArrayList<String> message = new ArrayList<>();
						for(String p : profiles) {
							if(!message.isEmpty()) {
								message.add(", ");
							}
							message.addAll(Message.fromKey("addsign-profile").replacements(p).get());
						}
						plugin.message(sender, "addsign-wrongProfile", Message.fromList(message));
						return;
					}
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










