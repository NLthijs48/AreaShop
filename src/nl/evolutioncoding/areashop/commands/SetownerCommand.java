package nl.evolutioncoding.areashop.commands;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.regions.BuyRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import nl.evolutioncoding.areashop.regions.RentRegion;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetownerCommand extends CommandAreaShop {

	public SetownerCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop setowner";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.setownerrent") || target.hasPermission("areashop.setownerbuy")) {
			return plugin.getLanguageManager().getLang("help-setowner");
		}
		return null;
	}
	
	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		GeneralRegion region;
		if(args.length < 2) {
			plugin.message(sender, "setowner-help");
			return;
		}
		if(!sender.hasPermission("areashop.setownerrent") && !sender.hasPermission("areashop.setownerbuy")) {
			plugin.message(sender, "setowner-noPermission");
			return;
		}
		
		if(args.length == 2) {
			if(sender instanceof Player) {
				// get the region by location
				List<GeneralRegion> regions = plugin.getFileManager().getApplicalbeASRegions(((Player)sender).getLocation());
				if(regions.size() != 1) {
					plugin.message(sender, "setowner-help");
					return;
				} else {
					region = regions.get(0);
				}				
			} else {
				plugin.message(sender, "setowner-help");
				return;
			}			
		} else {
			region = plugin.getFileManager().getRegion(args[2]);
		}
		if(region == null) {
			plugin.message(sender, "setowner-notRegistered");
			return;
		}
		
		if(region.isRentRegion() && !sender.hasPermission("areashop.setownerrent")) {
			plugin.message(sender, "setowner-noPermissionRent");
			return;
		}
		if(region.isBuyRegion() && !sender.hasPermission("areashop.setownerbuy")) {
			plugin.message(sender, "setowner-noPermissionBuy");
			return;
		}
		
		UUID uuid = null;
		@SuppressWarnings("deprecation")
		OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
		if(player != null) {
			uuid = player.getUniqueId();
		}
		if(uuid == null) {
			plugin.message(sender, "setowner-noPlayer", args[1]);
			return;
		}		
		
		if(region.isRentRegion()) {
			RentRegion rent = (RentRegion)region;
			if(rent.isRenter(uuid)) {
				// extend
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(rent.getRentedUntil() + rent.getDuration());
				rent.setRentedUntil(calendar.getTimeInMillis());
				rent.setRenter(uuid);
				plugin.message(sender, "setowner-succesRentExtend", region);
			} else {
				// change
				Calendar calendar = Calendar.getInstance();
				long current = calendar.getTimeInMillis();
				if(rent.isRented()) {
					current = rent.getRentedUntil();
				}
				calendar.setTimeInMillis(current + rent.getDuration());
				rent.setRentedUntil(calendar.getTimeInMillis());
				rent.setRenter(uuid);
				plugin.message(sender, "setowner-succesRent", region);
			}
			rent.updateRegionFlags();
			rent.updateSigns();
		}
		if(region.isBuyRegion()) {
			BuyRegion buy = (BuyRegion)region;
			buy.setBuyer(uuid);
			plugin.message(sender, "setowner-succesBuy", region);
			buy.updateRegionFlags();
			buy.updateSigns();
		}		
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		ArrayList<String> result = new ArrayList<String>();
		if(toComplete == 2) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				result.add(player.getName());
			}
		} else if(toComplete == 3) {
			for(RentRegion region : plugin.getFileManager().getRents()) {
				if(region.isRented()) {
					result.add(region.getName());
				}
			}
		}
		return result;
	}
}








