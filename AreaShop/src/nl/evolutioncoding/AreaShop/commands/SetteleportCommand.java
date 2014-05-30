package nl.evolutioncoding.AreaShop.commands;

import nl.evolutioncoding.AreaShop.AreaShop;

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
		owner = plugin.getFileManager().getRent(args[1]) != null && player.getUniqueId().toString().equals(plugin.getFileManager().getRent(args[1]).get(AreaShop.keyPlayerUUID));
		owner = owner || plugin.getFileManager().getBuy(args[1]) != null && player.getUniqueId().toString().equals(plugin.getFileManager().getBuy(args[1]).get(AreaShop.keyPlayerUUID));
		if(!player.hasPermission("areashop.setteleport")) {
			plugin.message(player, "setteleport-noPermission");
			return;
		} else if(!owner && !player.hasPermission("areashop.setteleportall")) {
			plugin.message(player, "setteleport-noPermissionOther");
			return;
		}
		if(plugin.getFileManager().getRent(args[1]) == null && plugin.getFileManager().getBuy(args[1]) == null) {
			plugin.message(player, "setteleport-noRentOrBuy", args[1]);
			return;
		}
		ProtectedRegion region = plugin.getWorldGuard().getRegionManager(player.getWorld()).getRegion(args[1]);
		if(!region.contains(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ())) {
			plugin.message(player, "setteleport-notInside", args[1]);
			return;
		}
		plugin.getFileManager().setTeleport(args[1], player.getLocation(), plugin.getFileManager().getRent(args[1]) != null);
		plugin.message(player, "setteleport-success", args[1]);
	}

}
