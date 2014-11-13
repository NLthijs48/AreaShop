package nl.evolutioncoding.areashop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class UpdaterentsCommand extends CommandAreaShop {

	public UpdaterentsCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop updaterents";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.updaterents")) {
			return plugin.getLanguageManager().getLang("help-updaterents");
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if(!sender.hasPermission("areashop.updaterents")) {
			plugin.message(sender, "rents-noPermission");
			return;
		}
		plugin.getFileManager().updateRentSignsAndFlags(sender);
	}

	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		ArrayList<String> result = new ArrayList<String>();
		return result;
	}
}
