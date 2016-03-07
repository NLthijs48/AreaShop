package nl.evolutioncoding.areashop.commands;

import nl.evolutioncoding.areashop.AreaShop;
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
			return "help-help";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		plugin.getCommandManager().showHelp(sender);
	}
}
