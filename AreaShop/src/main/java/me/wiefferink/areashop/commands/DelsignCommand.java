package me.wiefferink.areashop.commands;

import me.wiefferink.areashop.features.signs.RegionSign;
import me.wiefferink.areashop.features.signs.SignsFeature;
import me.wiefferink.areashop.tools.Materials;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.List;

public class DelsignCommand extends CommandAreaShop {

	@Override
	public String getCommandStart() {
		return "areashop delsign";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.delsign")) {
			return "help-delsign";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.delsign")) {
			plugin.message(sender, "delsign-noPermission");
			return;
		}
		if(!(sender instanceof Player)) {
			plugin.message(sender, "cmd-onlyByPlayer");
			return;
		}
		Player player = (Player)sender;

		// Get the sign
		Block block = null;
		BlockIterator blockIterator = new BlockIterator(player, 100);
		while(blockIterator.hasNext() && block == null) {
			Block next = blockIterator.next();
			if(next.getType() != Material.AIR) {
				block = next;
			}
		}
		if(block == null || !Materials.isSign(block.getType())) {
			plugin.message(sender, "delsign-noSign");
			return;
		}
		RegionSign regionSign = SignsFeature.getSignByLocation(block.getLocation());
		if(regionSign == null) {
			plugin.message(sender, "delsign-noRegion");
			return;
		}
		plugin.message(sender, "delsign-success", regionSign.getRegion());
		regionSign.remove();
	}

	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		return new ArrayList<>();
	}

}










