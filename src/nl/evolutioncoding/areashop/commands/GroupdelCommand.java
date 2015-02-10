package nl.evolutioncoding.areashop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.Utils;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import nl.evolutioncoding.areashop.regions.RegionGroup;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.selections.Selection;

public class GroupdelCommand extends CommandAreaShop {

	public GroupdelCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop groupdel";
	}
	
	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.groupdel")) {
			return plugin.getLanguageManager().getLang("help-groupdel");
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if(!sender.hasPermission("areashop.groupdel")) {
			plugin.message(sender, "groupdel-noPermission");
			return;
		}		
		if(args.length < 2 || args[1] == null) {
			plugin.message(sender, "groupdel-help");
			return;
		}	
		RegionGroup group = plugin.getFileManager().getGroup(args[1]);
		if(group == null) {
			group = new RegionGroup(plugin, args[1]);
			plugin.getFileManager().addGroup(group);
			group.saveRequired();
		}
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
			List<GeneralRegion> regions = plugin.getFileManager().getASRegionsInSelection(selection);
			if(regions.size() == 0) {
				plugin.message(player, "cmd-noRegionsFound");
				return;
			}			
			ArrayList<String> namesSuccess = new ArrayList<String>();
			ArrayList<String> namesFailed = new ArrayList<String>();
			ArrayList<GeneralRegion> toUpdate = new ArrayList<GeneralRegion>();
			for(GeneralRegion region : regions) {
				if(group.addMember(region)) {
					namesSuccess.add(region.getName());
					toUpdate.add(region);
				} else {
					namesFailed.add(region.getName());
				}
			}
			if(namesSuccess.size() != 0) {
				plugin.message(player, "groupdel-weSuccess", group.getName(), Utils.createCommaSeparatedList(namesSuccess));
			}
			if(namesFailed.size() != 0) {
				plugin.message(player, "groupdel-weFailed", group.getName(), Utils.createCommaSeparatedList(namesFailed));
			}
			// Update all regions, this does it in a task, updating them without lag
			plugin.getFileManager().updateRegions(toUpdate, player);
			group.saveRequired();
		} else {
			GeneralRegion region = plugin.getFileManager().getRegion(args[2]);
			if(region == null) {
				plugin.message(sender, "groupdel-noRegion", args[2]);
				return;
			}	
			if(group.removeMember(region)) {
				region.updateRegionFlags();
				region.updateSigns();
				plugin.message(sender, "groupdel-success", region.getName(), group.getName(), group.getMembers().size());
			} else {
				plugin.message(sender, "groupdel-failed", region.getName(), group.getName());
			}
			group.saveRequired();
		}
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		List<String> result = new ArrayList<String>();
		if(toComplete == 2) {
			result = plugin.getFileManager().getGroupNames();		
		} else if(toComplete == 3) {
			result = plugin.getFileManager().getRegionNames();
		}
		return result;
	}

}










