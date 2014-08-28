package nl.evolutioncoding.AreaShop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.AreaShop.AreaShop;
import nl.evolutioncoding.AreaShop.regions.GeneralRegion;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

public class DelsignCommand extends CommandAreaShop {

	public DelsignCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop delsign";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.delsign")) {
			return plugin.getLanguageManager().getLang("help-delsign");
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
		if(!player.hasPermission("areashop.delsign")) {
			plugin.message(sender, "delsign-noPermission");
			return;
		}
		// Get the sign
		Block block = null;
		BlockIterator blockIterator = new BlockIterator(player, 100);
		while(blockIterator.hasNext() && block == null) {
			Block next = blockIterator.next();
			if(next.getType() != Material.AIR) {
				block = next;
			}
		}
		if(block == null || !(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST)) {
			plugin.message(sender, "delsign-noSign");
			return;
		}
		GeneralRegion region = plugin.getFileManager().getRegionBySignLocation(block.getLocation());
		if(region == null) {
			plugin.message(sender, "delsign-noRegion");
			return;
		}
		plugin.message(sender, "delsign-success", region.getName());
		region.removeSign(block.getLocation());
		region.updateSigns();
		region.save();
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		return new ArrayList<String>();
	}

}










