package nl.evolutioncoding.AreaShop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.AreaShop.AreaShop;

import org.bukkit.command.Command;
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
	public void execute(CommandSender sender, Command command, String[] args) {
		if(sender.hasPermission("areashop.reload")) {
			plugin.reload();	
			plugin.message(sender, "reload-reloaded");
		} else {
			plugin.message(sender, "reload-noPermission");
		}
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		ArrayList<String> result = new ArrayList<String>();
		return result;
	}

}
