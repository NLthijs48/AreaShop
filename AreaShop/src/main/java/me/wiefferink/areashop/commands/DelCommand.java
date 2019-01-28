package me.wiefferink.areashop.commands;

import me.wiefferink.areashop.events.ask.DeletingRegionEvent;
import me.wiefferink.areashop.interfaces.WorldEditSelection;
import me.wiefferink.areashop.regions.BuyRegion;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.regions.RentRegion;
import me.wiefferink.areashop.tools.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class DelCommand extends CommandAreaShop {

	@Override
	public String getCommandStart() {
		return "areashop del";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.destroyrent") || target.hasPermission("areashop.destroybuy") || target.hasPermission("areashop.destroyrent.landlord") || target.hasPermission("areashop.destroybuy.landlord")) {
			return "help-del";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.destroybuy")
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
			WorldEditSelection selection = plugin.getWorldEditHandler().getPlayerSelection(player);
			if(selection == null) {
				plugin.message(player, "cmd-noSelection");
				return;
			}
			List<GeneralRegion> regions = Utils.getRegionsInSelection(selection);
			if(regions == null || regions.isEmpty()) {
				plugin.message(player, "cmd-noRegionsFound");
				return;
			}
			// Start removing the regions that he has permission for
			ArrayList<String> namesSuccess = new ArrayList<>();
			TreeSet<GeneralRegion> regionsFailed = new TreeSet<>();
			TreeSet<GeneralRegion> regionsCancelled = new TreeSet<>();
			for(GeneralRegion region : regions) {
				boolean isLandlord = region.isLandlord(((Player)sender).getUniqueId());
				if(region instanceof RentRegion) {
					if(!sender.hasPermission("areashop.destroyrent") && !(isLandlord && sender.hasPermission("areashop.destroyrent.landlord"))) {
						regionsFailed.add(region);
						continue;
					}
				} else if(region instanceof BuyRegion) {
					if(!sender.hasPermission("areashop.destroybuy") && !(isLandlord && sender.hasPermission("areashop.destroybuy.landlord"))) {
						regionsFailed.add(region);
						continue;
					}
				}

				DeletingRegionEvent event = plugin.getFileManager().deleteRegion(region, true);
				if (event.isCancelled()) {
					regionsCancelled.add(region);
				} else {
					namesSuccess.add(region.getName());
				}
			}

			// Send messages
			if(!namesSuccess.isEmpty()) {
				plugin.message(sender, "del-success", Utils.createCommaSeparatedList(namesSuccess));
			}
			if(!regionsFailed.isEmpty()) {
				plugin.message(sender, "del-failed", Utils.combinedMessage(regionsFailed, "region"));
			}
			if(!regionsCancelled.isEmpty()) {
				plugin.message(sender, "del-cancelled", Utils.combinedMessage(regionsCancelled, "region"));
			}
		} else {
			GeneralRegion region = plugin.getFileManager().getRegion(args[1]);
			if(region == null) {
				plugin.message(sender, "cmd-notRegistered", args[1]);
				return;
			}
			boolean isLandlord = sender instanceof Player && region.isLandlord(((Player)sender).getUniqueId());
			if(region instanceof RentRegion) {
				// Remove the rent if the player has permission
				if(sender.hasPermission("areashop.destroyrent") || (isLandlord && sender.hasPermission("areashop.destroyrent.landlord"))) {
					DeletingRegionEvent event = plugin.getFileManager().deleteRegion(region, true);
					if (event.isCancelled()) {
						plugin.message(sender, "general-cancelled", event.getReason());
					} else {
						plugin.message(sender, "destroy-successRent", region);
					}
				} else {
					plugin.message(sender, "destroy-noPermissionRent", region);
				}
			} else if(region instanceof BuyRegion) {
				// Remove the buy if the player has permission
				if(sender.hasPermission("areashop.destroybuy") || (isLandlord && sender.hasPermission("areashop.destroybuy.landlord"))) {
					DeletingRegionEvent event = plugin.getFileManager().deleteRegion(region, true);
					if (event.isCancelled()) {
						plugin.message(sender, "general-cancelled", event.getReason());
					} else {
						plugin.message(sender, "destroy-successBuy", region);
					}
				} else {
					plugin.message(sender, "destroy-noPermissionBuy", region);
				}
			}
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










