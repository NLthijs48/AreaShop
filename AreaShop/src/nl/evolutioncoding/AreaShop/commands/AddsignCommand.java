package nl.evolutioncoding.AreaShop.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import nl.evolutioncoding.AreaShop.AreaShop;
import nl.evolutioncoding.AreaShop.Utils;
import nl.evolutioncoding.AreaShop.regions.GeneralRegion;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.Sign;
import org.bukkit.util.BlockIterator;

import com.sk89q.worldedit.bukkit.selections.CuboidSelection;

public class AddsignCommand extends CommandAreaShop {

	public AddsignCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop addsign";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.addsign")) {
			return plugin.getLanguageManager().getLang("help-addsign");
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
		if(!player.hasPermission("areashop.addsign")) {
			plugin.message(sender, "addsign-noPermission");
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
			plugin.message(sender, "addsign-noSign");
			return;
		}

		GeneralRegion region = null;
		if(args.length > 1) {
			// Get region by argument
			region = plugin.getFileManager().getRegion(args[1]);
			if(region == null) {
				plugin.message(sender, "addsign-noRegion", args[1]);
				return;
			}			
		} else {
			// Get region by sign position
			List<GeneralRegion> regions = plugin.getFileManager().getASRegionsInSelection(new CuboidSelection(block.getWorld(), block.getLocation(), block.getLocation()));
			if(regions.isEmpty()) {
				plugin.message(sender, "addsign-noRegions");
				return;
			} else if(regions.size() > 1) {
				plugin.message(sender, "addsign-couldNotDetect", regions.get(0).getName(), regions.get(1).getName());
				return;
			}
			region = regions.get(0);
		}
		Sign sign = (Sign)block.getState().getData();
		String profile = null;
		if(args.length > 2) {
			profile = args[2];
			Set<String> profiles = plugin.config().getConfigurationSection("signProfiles").getKeys(false);
			if(!profiles.contains(profile)) {
				plugin.message(sender, "addsign-wrongProfile", Utils.createCommaSeparatedList(profiles));
				return;
			}
		}
		GeneralRegion signRegion = plugin.getFileManager().getRegionBySignLocation(block.getLocation());
		if(signRegion != null) {
			plugin.message(sender, "addsign-alreadyRegistered", signRegion.getName());
			return;
		}
		
		region.addSign(block.getLocation(), block.getType(), sign.getFacing(), profile);
		if(profile == null) {
			plugin.message(sender, "addsign-success", region.getName());
		} else {
			plugin.message(sender, "addsign-successProfile", region.getName(), profile);
		}
		region.updateSigns();
		region.save();
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		List<String> result = new ArrayList<String>();
		if(toComplete == 2) {
			result.addAll(plugin.getFileManager().getRegionNames());
		} else if(toComplete == 3) {
			result.addAll(plugin.config().getStringList("signProfiles"));
		}
		return result;
	}

}










