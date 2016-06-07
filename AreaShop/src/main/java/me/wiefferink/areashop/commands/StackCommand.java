package me.wiefferink.areashop.commands;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.Utils;
import me.wiefferink.areashop.messages.Message;
import me.wiefferink.areashop.regions.BuyRegion;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.regions.RegionGroup;
import me.wiefferink.areashop.regions.RentRegion;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class StackCommand extends CommandAreaShop {

	public StackCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop stack";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.stack")) {
			return "help-stack";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		// Check permission
		if(!sender.hasPermission("areashop.stack")) {
			plugin.message(sender, "stack-noPermission");
			return;
		}
		// Only from ingame
		if (!(sender instanceof Player)) {
			plugin.message(sender, "cmd-onlyByPlayer");
			return;
		}
		final Player player = (Player)sender;
		// Specify enough arguments
		if(args.length < 5) {
			plugin.message(sender, "stack-help");
			return;
		}
		// Check amount
		int tempAmount = -1;
		try {
			tempAmount = Integer.parseInt(args[1]);
		} catch(NumberFormatException e) {
			// Incorrect number
		}
		if(tempAmount <= 0) {
			plugin.message(player, "stack-wrongAmount", args[1]);
			return;
		}
		// Check gap
		int gap;
		try {
			gap = Integer.parseInt(args[2]);
		} catch(NumberFormatException e) {
			plugin.message(player, "stack-wrongGap", args[2]);
			return;
		}
		// Check region type
		if(!"rent".equalsIgnoreCase(args[4]) && !"buy".equalsIgnoreCase(args[4])) {
			plugin.message(sender, "stack-help");
			return;
		}
		// Get WorldEdit selection
		final Selection selection = plugin.getWorldEdit().getSelection(player);
		if(selection == null) {
			plugin.message(player, "stack-noSelection");
			return;
		}		
		// Get or create group
		RegionGroup group = null;
		if(args.length > 5) {
			group = plugin.getFileManager().getGroup(args[5]);
			if(group == null) {
				group = new RegionGroup(plugin, args[5]);
				plugin.getFileManager().addGroup(group);
			}
		}
		// Get facing of the player (must be clearly one of the four directions to make sure it is no mistake)
		BlockFace facing = Utils.yawToFacing(player.getLocation().getYaw());
		if(!(facing == BlockFace.NORTH || facing == BlockFace.EAST || facing == BlockFace.SOUTH || facing == BlockFace.WEST)) {
			plugin.message(player, "stack-unclearDirection", facing.toString().toLowerCase().replace('_', '-'));
			return;
		}
		final Location shift = new Location(selection.getWorld(), 0, 0, 0);
		if(facing == BlockFace.SOUTH) {
			shift.setZ(-selection.getLength() - gap);
		} else if(facing == BlockFace.WEST) {
			shift.setX(selection.getWidth() + gap);
		} else if(facing == BlockFace.NORTH) {
			shift.setZ(selection.getLength() + gap);
		} else if(facing == BlockFace.EAST) {
			shift.setX(-selection.getWidth() - gap);
		}
		AreaShop.debug("  calculated shift vector: " + shift + ", with facing=" + facing);
		// Create regions and add them to AreaShop
		final String namePrefix = args[3];
		final int regionsPerTick = plugin.getConfig().getInt("adding.regionsPerTick");
		final boolean rentRegions = "rent".equalsIgnoreCase(args[4]);
		final int amount = tempAmount;
		final RegionGroup finalGroup = group;
		String type;
		if(rentRegions) {
			type = "rent";
		} else {
			type = "buy";
		}
		Message groupsMessage = Message.none();
		if(group != null) {
			groupsMessage = Message.fromKey("stack-addToGroup").replacements(group.getName());
		}		
		plugin.message(player, "stack-accepted", amount, type, gap, namePrefix, groupsMessage);
		plugin.message(player, "stack-addStart", amount, regionsPerTick*20);
		new BukkitRunnable() {
			private int current = 0;
			private RegionManager manager = AreaShop.getInstance().getWorldGuard().getRegionManager(selection.getWorld());
			private int counter = 1;
			@Override
			public void run() {
				for(int i=0; i<regionsPerTick; i++) {
					if(current < amount) {
						// Create the region name
						String counterName = counter+"";
						int minimumLength = plugin.getConfig().getInt("stackRegionNumberLength");
						while(counterName.length() < minimumLength) {
							counterName = "0"+counterName;
						}
						String regionName = namePrefix + counterName;
						while(manager.getRegion(regionName) != null || AreaShop.getInstance().getFileManager().getRegion(regionName) != null) {
							counter++;
							counterName = counter+"";
							minimumLength = plugin.getConfig().getInt("stackRegionNumberLength");
							while(counterName.length() < minimumLength) {
								counterName = "0"+counterName;
							}
							regionName = namePrefix + counterName;
						}
						// Add the region to WorldGuard (at startposition shifted by the number of this region times the blocks it should shift)
						ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionName, 
								new BlockVector(
										selection.getMinimumPoint().getBlockX() + shift.getBlockX()*current, 
										selection.getMinimumPoint().getBlockY() + shift.getBlockY()*current, 
										selection.getMinimumPoint().getBlockZ() + shift.getBlockZ()*current
								), 
								new BlockVector(
										selection.getMaximumPoint().getBlockX() + shift.getBlockX()*current, 
										selection.getMaximumPoint().getBlockY() + shift.getBlockY()*current, 
										selection.getMaximumPoint().getBlockZ() + shift.getBlockZ()*current
								)
						);
						manager.addRegion(region);
						// Add the region to AreaShop
						if(rentRegions) {
							RentRegion rent = new RentRegion(plugin, region.getId(), selection.getWorld());
							if(finalGroup != null) {
								finalGroup.addMember(rent);
							}
							rent.runEventCommands(GeneralRegion.RegionEvent.CREATED, true);
							plugin.getFileManager().addRent(rent);
							rent.handleSchematicEvent(GeneralRegion.RegionEvent.CREATED);
							rent.runEventCommands(GeneralRegion.RegionEvent.CREATED, false);
							rent.update();
						} else {
							BuyRegion buy = new BuyRegion(plugin, region.getId(), selection.getWorld());
							if(finalGroup != null) {
								finalGroup.addMember(buy);
							}
							buy.runEventCommands(GeneralRegion.RegionEvent.CREATED, true);
							plugin.getFileManager().addBuy(buy);
							buy.handleSchematicEvent(GeneralRegion.RegionEvent.CREATED);
							buy.runEventCommands(GeneralRegion.RegionEvent.CREATED, false);
							buy.update();
						}
						current++;
					} 
				}
				if(current >= amount) {
					if(player.isOnline()) {
						plugin.message(player, "stack-addComplete");
					}
					this.cancel();
				}
			}
		}.runTaskTimer(plugin, 1, 1);
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		List<String> result = new ArrayList<>();
		if(toComplete == 5) {
			result.add("rent");
			result.add("buy");
		} else if(toComplete == 6) {
			result.addAll(plugin.getFileManager().getGroupNames());
		}
		return result;
	}

}










