package nl.evolutioncoding.areashop.commands;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.regions.BuyRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import nl.evolutioncoding.areashop.regions.RentRegion;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class MeCommand extends CommandAreaShop {

	public MeCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop me";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.me")) {
			return plugin.getLanguageManager().getLang("help-me");
		}
		return null;
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.me")) {
			plugin.message(sender, "me-noPermission");
			return;
		}		
		if(!(sender instanceof Player)) {
			plugin.message(sender, "me-notAPlayer");
			return;
		}
		Player player = (Player)sender;
		// Get the regions owned by the player
		Set<RentRegion> rentRegions = new HashSet<>();
		for(RentRegion region : plugin.getFileManager().getRents()) {
			if(region.isOwner(player)) {
				rentRegions.add(region);
			}
		}
		Set<BuyRegion> buyRegions = new HashSet<>();
		for(BuyRegion region : plugin.getFileManager().getBuys()) {
			if(region.isOwner(player)) {
				buyRegions.add(region);
			}
		}
		// Send messages
		if(rentRegions.isEmpty()) {
			plugin.message(player, "me-noRentRegions");
		} else {
			plugin.message(player, "me-rentRegions");
			for(RentRegion region : rentRegions) {
				plugin.messageNoPrefix(player, "me-rentLine", region);
			}
		}
		if(buyRegions.isEmpty()) {
			plugin.message(player, "me-noBuyRegions");
		} else {
			plugin.message(player, "me-buyRegions");
			for(BuyRegion region : buyRegions) {
				plugin.messageNoPrefix(player, "me-buyLine", region);
			}
		}
		Set<GeneralRegion> friendRegions = new HashSet<>();
		for(GeneralRegion region : plugin.getFileManager().getRegions()) {
			if(region.getFriends() != null && region.getFriends().contains(player.getUniqueId())) {
				friendRegions.add(region);
			}
		}
		if(friendRegions.isEmpty()) {
			plugin.message(player, "me-noFriendRegions");
		} else {
			plugin.message(player, "me-friendRegions");
			for(GeneralRegion region : friendRegions) {
				plugin.messageNoPrefix(player, "me-friendLine", region);
			}
		}
		
	}
}


























