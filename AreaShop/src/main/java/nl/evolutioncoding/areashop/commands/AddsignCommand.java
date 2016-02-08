package nl.evolutioncoding.areashop.commands;

import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.Utils;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.Sign;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.addsign")) {
			plugin.message(sender, "addsign-noPermission");
			return;
		}		
		if (!(sender instanceof Player)) {
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
		if(block == null || !(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST)) {
			plugin.message(sender, "addsign-noSign");
			return;
		}

		GeneralRegion region;
		if(args.length > 1) {
			// Get region by argument
			region = plugin.getFileManager().getRegion(args[1]);
			if(region == null) {
				plugin.message(sender, "addsign-noRegion", args[1]);
				return;
			}			
		} else {
			// Get region by sign position
			List<GeneralRegion> regions = Utils.getASRegionsInSelection(new CuboidSelection(block.getWorld(), block.getLocation(), block.getLocation()));
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
			Set<String> profiles = plugin.getConfig().getConfigurationSection("signProfiles").getKeys(false);
			if(!profiles.contains(profile)) {
				plugin.message(sender, "addsign-wrongProfile", Utils.createCommaSeparatedList(profiles), region);
				return;
			}
		}
		GeneralRegion signRegion = plugin.getFileManager().getRegionBySignLocation(block.getLocation());
		if(signRegion != null) {
			plugin.message(sender, "addsign-alreadyRegistered", signRegion);
			return;
		}
		
		region.addSign(block.getLocation(), block.getType(), sign.getFacing(), profile);
		if(profile == null) {
			plugin.message(sender, "addsign-success", region);
		} else {
			plugin.message(sender, "addsign-successProfile", profile, region);
		}
		region.update();
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		List<String> result = new ArrayList<>();
		if(toComplete == 2) {
			result.addAll(plugin.getFileManager().getRegionNames());
		} else if(toComplete == 3) {
			result.addAll(plugin.getConfig().getStringList("signProfiles"));
		}
		return result;
	}

}










