package nl.evolutioncoding.AreaShop.commands;

import nl.evolutioncoding.AreaShop.AreaShop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class UpdatebuysCommand extends CommandAreaShop {

	public UpdatebuysCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop updatebuys";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.updatebuys")) {
			return plugin.getLanguageManager().getLang("help-updatebuys");
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if(!sender.hasPermission("areashop.updatebuys")) {
			plugin.message(sender, "buys-noPermission");
			return;
		}
		boolean result = plugin.getFileManager().updateBuySigns();
		if(result) {
			plugin.message(sender, "buys-updated");
		} else {
			plugin.message(sender, "buys-notUpdated");
		}							
	}

}
