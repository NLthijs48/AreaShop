package nl.evolutioncoding.areashop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class HelpCommand extends CommandAreaShop {

	public HelpCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop help";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.help")) {
			return plugin.getLanguageManager().getLang("help-help");
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		plugin.getCommandManager().showHelp(sender);
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		ArrayList<String> result = new ArrayList<String>();
		return result;
	}

}
