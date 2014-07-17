package nl.evolutioncoding.AreaShop.commands;

import nl.evolutioncoding.AreaShop.AreaShop;
import nl.evolutioncoding.AreaShop.regions.GeneralRegion;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportCommand extends CommandAreaShop {

	public TeleportCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop tp";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.teleportall")) {
			return plugin.getLanguageManager().getLang("help-teleportAll");
		} else if(target.hasPermission("areashop.teleport")) {
			return plugin.getLanguageManager().getLang("help-teleport");
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if (!(sender instanceof Player)) {
			plugin.message(sender, "onlyByPlayer");
			return;
		}		
		if(args.length <= 1 || args[1] == null) {
			plugin.message(sender, "teleport-help");
			return;
		}
		Player player = (Player)sender;
		GeneralRegion region = plugin.getFileManager().getRegion(args[1]);
		if(region == null) {
			plugin.message(player, "teleport-noRentOrBuy", args[1]);
			return;
		}
		region.teleportPlayer(player);
	}

}
