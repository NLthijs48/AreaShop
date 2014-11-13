package nl.evolutioncoding.areashop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.regions.BuyRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import nl.evolutioncoding.areashop.regions.RentRegion;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class SetteleportCommand extends CommandAreaShop {

	public SetteleportCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop settp";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.teleportall")) {
			return plugin.getLanguageManager().getLang("help-setteleportAll");
		} else if(target.hasPermission("areashop.teleport")) {
			return plugin.getLanguageManager().getLang("help-setteleport");
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if (!(sender instanceof Player)) {
			plugin.message(sender, "onlyByPlayer");
			return;
		}
		Player player = (Player) sender;
		GeneralRegion region = null;
		if(args.length <= 1) {
			// get the region by location
			List<GeneralRegion> regions = plugin.getFileManager().getApplicalbeASRegions(((Player)sender).getLocation());
			if(regions.size() != 1) {
				plugin.message(sender, "setteleport-help");
				return;
			} else {
				region = regions.get(0);
			}							
		} else {
			region = plugin.getFileManager().getRegion(args[1]);
		}
		
		boolean owner = false;
		
		if(region == null) {
			plugin.message(player, "setteleport-noRentOrBuy", args[1]);
			return;
		}
		if(region.isRentRegion()) {
			owner = player.getUniqueId().equals(((RentRegion)region).getRenter());
		} else {
			owner = player.getUniqueId().equals(((BuyRegion)region).getBuyer());
		}
		if(!player.hasPermission("areashop.setteleport")) {
			plugin.message(player, "setteleport-noPermission");
			return;
		} else if(!owner && !player.hasPermission("areashop.setteleportall")) {
			plugin.message(player, "setteleport-noPermissionOther");
			return;
		}

		ProtectedRegion wgRegion = region.getRegion();
		if(args.length > 2 && args[2] != null && (args[2].equalsIgnoreCase("reset") || args[2].equalsIgnoreCase("yes") || args[2].equalsIgnoreCase("true"))) {
			region.setTeleport(null);
			plugin.message(player, "setteleport-reset", region.getName());
			return;
		}
		if(!wgRegion.contains(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()) && !player.hasPermission("areashop.setteleportoutsideregion")) {
			plugin.message(player, "setteleport-notInside", region.getName());
			return;
		}
		region.setTeleport(player.getLocation());
		plugin.message(player, "setteleport-success", region.getName());
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		ArrayList<String> result = new ArrayList<String>();
		if(toComplete == 2) {
			result.addAll(plugin.getFileManager().getRegionNames());
		} else if(toComplete == 3) {
			result.add("reset");
		}
		return result;
	}

}
