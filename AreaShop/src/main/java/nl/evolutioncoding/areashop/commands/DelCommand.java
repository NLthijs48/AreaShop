package nl.evolutioncoding.areashop.commands;

import com.sk89q.worldedit.bukkit.selections.Selection;
import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.Utils;
import nl.evolutioncoding.areashop.events.notify.RemovedRegionEvent;
import nl.evolutioncoding.areashop.regions.BuyRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import nl.evolutioncoding.areashop.regions.RentRegion;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class DelCommand extends CommandAreaShop {

	public DelCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop del";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.destroyrent") || target.hasPermission("areashop.destroybuy")) {
			return "help-del";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(		   !sender.hasPermission("areashop.destroybuy")
				&& !sender.hasPermission("areashop.destroybuy.landlord")
			
				&& !sender.hasPermission("areashop.destroyrent")
				&& !sender.hasPermission("areashop.destroyrent.landlord")) {
			plugin.message(sender, "del-noPermission");
			return;
		}
		if(args.length < 2) {
			// Only players can have a selection
			if(!(sender instanceof Player)) {
				plugin.message(sender, "cmd-weOnlyByPlayer");
				return;
			}
			Player player = (Player)sender;
			Selection selection = plugin.getWorldEdit().getSelection(player);
			if(selection == null) {
				plugin.message(player, "cmd-noSelection");
				return;
			}			
			List<GeneralRegion> regions = Utils.getASRegionsInSelection(selection);
			if(regions == null || regions.size() == 0) {
				plugin.message(player, "cmd-noRegionsFound");
				return;
			}
			// Start removing the regions that he has permission for
			ArrayList<String> namesSuccess = new ArrayList<>();
			TreeSet<GeneralRegion> namesFailed = new TreeSet<>();
			for(GeneralRegion region : regions) {
				boolean isLandlord = region.isLandlord(((Player)sender).getUniqueId());
				if(region.isRentRegion()) {
					if(!sender.hasPermission("areashop.destroyrent") && !(isLandlord && sender.hasPermission("areashop.destroyrent.landlord"))) {
						namesFailed.add(region);
					} else {
						plugin.getFileManager().removeRent((RentRegion)region, true);
						namesSuccess.add(region.getName());
					}					
				} else if(region.isBuyRegion()) {
					if(!sender.hasPermission("areashop.destroybuy") && !(isLandlord && sender.hasPermission("areashop.destroybuy.landlord"))) {
						namesFailed.add(region);
					} else {
						plugin.getFileManager().removeBuy((BuyRegion)region, true);
						namesSuccess.add(region.getName());
					}
				}
				Bukkit.getPluginManager().callEvent(new RemovedRegionEvent(region));
			}
			// send messages
			if(namesSuccess.size() != 0) {
				plugin.message(sender, "del-success", Utils.createCommaSeparatedList(namesSuccess));
			}
			if(namesFailed.size() != 0) {
				plugin.message(sender, "del-failed", Utils.regionListMessage(namesFailed));
			}
		} else {
			GeneralRegion region = plugin.getFileManager().getRegion(args[1]);
			if(region == null) {
				plugin.message(sender, "del-noRegion", args[1]);
				return;
			}
			boolean isLandlord = sender instanceof Player && region.isLandlord(((Player)sender).getUniqueId());
			if(region.isRentRegion()) {
				// Remove the rent if the player has permission
				if(sender.hasPermission("areashop.destroyrent") || (isLandlord && sender.hasPermission("areashop.destroyrent.landlord"))) {
					plugin.getFileManager().removeRent((RentRegion)region, true);
					plugin.message(sender, "destroy-successRent", region);
				} else {
					plugin.message(sender, "destroy-noPermissionRent", region);
				}
			} else if(region.isBuyRegion()) {
				// Remove the buy if the player has permission
				if(sender.hasPermission("areashop.destroybuy") || (isLandlord && sender.hasPermission("areashop.destroybuy.landlord"))) {
					plugin.getFileManager().removeBuy((BuyRegion)region, true);
					plugin.message(sender, "destroy-successBuy", region);
				} else {
					plugin.message(sender, "destroy-noPermissionBuy", region);
				}
			}
			Bukkit.getPluginManager().callEvent(new RemovedRegionEvent(region));
		}
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		List<String> result = new ArrayList<>();
		if(toComplete == 2) {
			result = plugin.getFileManager().getRegionNames();
		}
		return result;
	}

}










