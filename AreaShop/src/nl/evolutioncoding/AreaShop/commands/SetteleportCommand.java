package nl.evolutioncoding.AreaShop.commands;

import nl.evolutioncoding.AreaShop.AreaShop;
import nl.evolutioncoding.AreaShop.regions.BuyRegion;
import nl.evolutioncoding.AreaShop.regions.GeneralRegion;
import nl.evolutioncoding.AreaShop.regions.RentRegion;

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
		if(args.length <= 1 || args[1] == null) {
			plugin.message(sender, "setteleport-help");
			return;
		}
		Player player = (Player) sender;
		boolean owner = false;
		GeneralRegion region = plugin.getFileManager().getRegion(args[1]);
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
			plugin.message(player, "setteleport-reset", args[1]);
			return;
		}
		if(!wgRegion.contains(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()) && !player.hasPermission(" areashop.setteleportoutsideregion")) {
			plugin.message(player, "setteleport-notInside", args[1]);
			return;
		}
		region.setTeleport(player.getLocation());
		plugin.message(player, "setteleport-success", args[1]);
	}

}
