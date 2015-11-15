package nl.evolutioncoding.areashop.commands;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.Utils;
import nl.evolutioncoding.areashop.regions.BuyRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import nl.evolutioncoding.areashop.regions.RentRegion;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.setteleport") && !sender.hasPermission("areashop.setteleportall")) {
			plugin.message(sender, "setteleport-noPermission");
			return;
		}
		if (!(sender instanceof Player)) {
			plugin.message(sender, "onlyByPlayer");
			return;
		}
		Player player = (Player)sender;
		GeneralRegion region;
		if(args.length < 2) {
			// get the region by location
			List<GeneralRegion> regions = Utils.getAllApplicableRegions(((Player)sender).getLocation());
			if(regions.isEmpty()) {
				plugin.message(sender, "cmd-noRegionsAtLocation");
				return;
			} else if(regions.size() > 1) {
				plugin.message(sender, "cmd-moreRegionsAtLocation");
				return;
			} else {
				region = regions.get(0);
			}
		} else {
			region = plugin.getFileManager().getRegion(args[1]);
		}

		boolean owner;
		
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
		if(!player.hasPermission("areashop.setteleportoutsideregion") && (wgRegion == null || !wgRegion.contains(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()))) {
			plugin.message(player, "setteleport-notInside", region.getName());
			return;
		}
		region.setTeleport(player.getLocation());
		plugin.message(player, "setteleport-success", region.getName());
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		ArrayList<String> result = new ArrayList<>();
		if(toComplete == 2) {
			result.addAll(plugin.getFileManager().getRegionNames());
		} else if(toComplete == 3) {
			result.add("reset");
		}
		return result;
	}

}
