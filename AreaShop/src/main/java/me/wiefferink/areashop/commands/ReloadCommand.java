package me.wiefferink.areashop.commands;

import org.bukkit.command.CommandSender;

public class ReloadCommand extends CommandAreaShop {

	@Override
	public String getCommandStart() {
		return "areashop reload";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.reload")) {
			return "help-reload";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(sender.hasPermission("areashop.reload")) {
			// Reload the configuration files and update all region flags/signs
			plugin.reload(sender);
		} else {
			plugin.message(sender, "reload-noPermission");
		}
	}
}
