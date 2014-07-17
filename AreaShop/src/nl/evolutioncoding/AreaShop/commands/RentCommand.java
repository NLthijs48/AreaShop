package nl.evolutioncoding.AreaShop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.AreaShop.AreaShop;
import nl.evolutioncoding.AreaShop.regions.RentRegion;

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
			RentRegion rent = plugin.getFileManager().getRent(args[1]);
			if(rent == null) {
				plugin.message(sender, "rent-notRentable");
			} else {
				rent.rent(player);
			}
		} else {
			plugin.message(sender, "rent-help");
		}	
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		ArrayList<String> result = new ArrayList<String>();
		if(toComplete == 2) {
			for(RentRegion region : plugin.getFileManager().getRents()) {
				if(!region.isRented()) {
					result.add(region.getName());
				}
			}
		}
		return result;
	}
}
