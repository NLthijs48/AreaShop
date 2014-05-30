package nl.evolutioncoding.AreaShop.commands;

import nl.evolutioncoding.AreaShop.AreaShop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RentCommand extends CommandAreaShop {

	public RentCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop rent";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.rent")) {
			return plugin.getLanguageManager().getLang("help-rent");
		}
		return null;
	}
	
	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if (!(sender instanceof Player)) {
			plugin.message(sender, "onlyByPlayer");
			return;
		}					
		Player player = (Player)sender;
		if(args.length > 1 && args[1] != null) {
			plugin.getFileManager().rent(player, args[1]);
		} else {
			plugin.message(sender, "rent-help");
		}	
	}
}
