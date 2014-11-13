package nl.evolutioncoding.areashop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;

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
		plugin.getFileManager().updateBuySignsAndFlags(sender);						
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		ArrayList<String> result = new ArrayList<String>();
		return result;
	}
}
