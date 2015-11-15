package nl.evolutioncoding.areashop.commands;

import nl.evolutioncoding.areashop.AreaShop;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends CommandAreaShop {

	public ReloadCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop reload";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.reload")) {
			return plugin.getLanguageManager().getLang("help-reload");
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
