package nl.evolutioncoding.areashop.commands;

import java.util.ArrayList;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.regions.GeneralRegion;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PermCommand extends CommandAreaShop {

    public PermCommand(AreaShop plugin) {
        super(plugin);
    }
    
    @Override
    public String getCommandStart() {
        return "areashop setperm";
    }
    
    @Override
    public String getHelp(CommandSender target) {
        if(target.hasPermission("areashop.setperm")) {
            return plugin.getLanguageManager().getLang("help-setperm");
        }
        return null;
    }

    @Override
    public void execute(CommandSender sender, Command command, String[] args) {
        if(!sender.hasPermission("areashop.setperm")) {
            plugin.message(sender, "setperm-noPermission");
            return;
        }       
        if(args.length < 2 || args[1] == null) {
            plugin.message(sender, "setperm-help");
            return;
        }       
        GeneralRegion region = null;
        //      0      1   2
        //as setperm perm area
        if(args.length < 3) {
            if (sender instanceof Player) {
                // get the region by location
                List<GeneralRegion> regions = plugin.getFileManager().getAllApplicableRegions(((Player) sender).getLocation());
                if (regions.isEmpty()) {
                    plugin.message(sender, "cmd-noRegio nsAtLocation");
                    return;
                } else if (regions.size() > 1) {
                    plugin.message(sender, "cmd-moreRegionsAtLocation");
                    return;
                } else {
                    region = regions.get(0);
                }
            } else {
                plugin.message(sender, "cmd-automaticRegionOnlyByPlayer");
                return;
            }       
        } else {
            region = plugin.getFileManager().getRegion(args[2]);
        }       
        if(region == null) {
            plugin.message(sender, "setperm-notRegistered", args[2]);
            return;
        }
        String perm = null;//args[1];
        if(args.length>1)
            perm = args[1];         
        
            region.setPermission(perm);
            plugin.message(sender, "setperm-successSet", region.getName(), perm.toString());
       
        region.updateSigns();
        region.updateRegionFlags();
        region.saveRequired();
    }
    
    @Override
    public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
        List<String> result = new ArrayList<String>();
        if(toComplete == 3) {
            result = plugin.getFileManager().getRegionNames();      
        }
        return result;
    }

}