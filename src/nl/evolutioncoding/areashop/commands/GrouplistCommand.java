package nl.evolutioncoding.areashop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.Utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class GrouplistCommand extends CommandAreaShop {

	public GrouplistCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop grouplist";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.grouplist")) {
			return plugin.getLanguageManager().getLang("help-grouplist");
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if(!sender.hasPermission("areashop.grouplist")) {
			plugin.message(sender, "grouplist-noPermission");
			return;
		}
		List<String> groups = plugin.getFileManager().getGroupNames();
		if(groups.size() == 0) {
			plugin.message(sender, "grouplist-noGroups");
		} else {
			plugin.message(sender, "grouplist-success", Utils.createCommaSeparatedList(groups));
		}
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		return new ArrayList<String>();
	}

}










