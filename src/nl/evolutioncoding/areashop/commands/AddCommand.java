package nl.evolutioncoding.areashop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.Utils;
import nl.evolutioncoding.areashop.regions.BuyRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import nl.evolutioncoding.areashop.regions.RentRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion.RegionEvent;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class AddCommand extends CommandAreaShop {

	public AddCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop add";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.createrent") || target.hasPermission("areashop.createbuy")) {
			return plugin.getLanguageManager().getLang("help-add");
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if(!sender.hasPermission("areashop.createrent") && !sender.hasPermission("areashop.createbuy")) {
			plugin.message(sender, "add-noPermission");
			return;
		}
		
		if(args.length < 2 || args[1] == null || (!"rent".equals(args[1].toLowerCase()) && !"buy".equals(args[1].toLowerCase()))) {
			plugin.message(sender, "add-help");
			return;
		}	
		boolean isRent = "rent".equals(args[1].toLowerCase());
		if((isRent && !sender.hasPermission("areashop.createrent")) || (!isRent && !sender.hasPermission("areashop.createbuy"))) {
			plugin.message(sender, "add-noPermission");
			return;
		}
		List<ProtectedRegion> regions = new ArrayList<ProtectedRegion>();
		World world = null;
		if(args.length == 2) {
			if(!(sender instanceof Player)) {
				plugin.message(sender, "cmd-weOnlyByPlayer");
				return;
			}
			Player player = (Player)sender;			
			Selection selection = plugin.getWorldEdit().getSelection(player);
			if(selection == null) {
				plugin.message(player, "cmd-noSelection");
				return;
			}
			world = selection.getWorld();
			regions = plugin.getFileManager().getWERegionsInSelection(selection);
			if(regions.size() == 0) {
				plugin.message(player, "cmd-noWERegionsFound");
				return;
			}
		} else {
			if(sender instanceof Player) {
				if(args.length == 4) {
					world = Bukkit.getWorld(args[3]);
					if(world == null) {
						plugin.message(sender, "add-incorrectWorld", args[3]);
						return;
					}
				} else {
					world = ((Player)sender).getWorld();
				}
			} else {
				if(args.length < 4) {
					plugin.message(sender, "add-specifyWorld");
					return;
				} else {
					world = Bukkit.getWorld(args[3]);
					if(world == null) {
						plugin.message(sender, "add-incorrectWorld", args[3]);
						return;
					}
				}
			}
			ProtectedRegion region = plugin.getWorldGuard().getRegionManager(world).getRegion(args[2]);
			if(region == null) {
				plugin.message(sender, "add-noRegion", args[2]);
				return;
			}
			regions.add(region);
		}
			
		ArrayList<String> namesSuccess = new ArrayList<String>();
		ArrayList<String> namesFailed = new ArrayList<String>();
		for(ProtectedRegion region : regions) {
			GeneralRegion asRegion = plugin.getFileManager().getRegion(region.getId());
			if(asRegion != null) {
				namesFailed.add(region.getId());
			} else {
				namesSuccess.add(region.getId());
				if(isRent) {
					RentRegion rent = new RentRegion(plugin, region.getId(), world);
					// Run commands
					rent.runEventCommands(RegionEvent.CREATED, true);						
					plugin.getFileManager().addRent(rent);
					rent.handleSchematicEvent(RegionEvent.CREATED);
					// Set the flags for the region
					rent.updateRegionFlags();
					// Run commands
					rent.runEventCommands(RegionEvent.CREATED, false);
					rent.saveRequired();
				} else {
					BuyRegion buy = new BuyRegion(plugin, region.getId(), world);
					// Run commands
					buy.runEventCommands(RegionEvent.CREATED, true);
					
					plugin.getFileManager().addBuy(buy);
					buy.handleSchematicEvent(RegionEvent.CREATED);
					// Set the flags for the region
					buy.updateRegionFlags();						
					// Run commands
					buy.runEventCommands(RegionEvent.CREATED, false);
					buy.saveRequired();
				}
			}
		}
		if(namesSuccess.size() != 0) {
			plugin.message(sender, "add-success", args[1], Utils.createCommaSeparatedList(namesSuccess));
		}
		if(namesFailed.size() != 0) {
			plugin.message(sender, "add-failed", Utils.createCommaSeparatedList(namesFailed));
		}	
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		List<String> result = new ArrayList<String>();
		if(toComplete == 2) {
			if(sender.hasPermission("areashop.createrent")) {
				result.add("rent");
			}
			if(sender.hasPermission("areashop.createbuy")) {
				result.add("buy");
			}
		} else if(toComplete == 3) {
			if(sender instanceof Player) {
				Player player = (Player)sender;
				if(sender.hasPermission("areashop.createrent") || sender.hasPermission("areashop.createbuy")) {
					for(ProtectedRegion region : plugin.getWorldGuard().getRegionManager(player.getWorld()).getRegions().values()) {
						result.add(region.getId());
					}
				}
			}
		}
		return result;
	}

}










