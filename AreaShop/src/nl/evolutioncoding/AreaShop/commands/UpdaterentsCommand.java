package nl.evolutioncoding.AreaShop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.AreaShop.AreaShop;

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
		boolean result = plugin.getFileManager().updateRentSigns();
		plugin.getFileManager().updateRentRegions();
		if(result) {
			plugin.message(sender, "rents-updated");
		} else {
			plugin.message(sender, "rents-notUpdated");
		}
	}

	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		ArrayList<String> result = new ArrayList<String>();
		return result;
	}
}
