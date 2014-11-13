package nl.evolutioncoding.areashop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import nl.evolutioncoding.areashop.regions.RentRegion;

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
			plugin.message(sender, "cmd-onlyByPlayer");
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
			// get the region by location
			List<GeneralRegion> regions = plugin.getFileManager().getApplicalbeASRegions(player.getLocation());
			if(regions.size() != 1) {
				plugin.message(sender, "rent-help");
			} else {
				if(!regions.get(0).isRentRegion()) {
					plugin.message(sender, "rent-notRentable");
				} else {
					((RentRegion)regions.get(0)).rent(player);
				}
				plugin.saveConfig();
			}
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
