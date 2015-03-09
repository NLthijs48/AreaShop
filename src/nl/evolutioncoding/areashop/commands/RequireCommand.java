package nl.evolutioncoding.areashop.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.regions.GeneralRegion;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RequireCommand extends CommandAreaShop {

    public RequireCommand(AreaShop plugin) {
        super(plugin);
    }
    
    @Override
    public String getCommandStart() {
        return "areashop require";
    }
    
    @Override
    public String getHelp(CommandSender target) {
        if(target.hasPermission("areashop.require")) {
            return plugin.getLanguageManager().getLang("help-require");
        }
        return null;
    }

    @Override
    public void execute(CommandSender sender, Command command, String[] args) {
        //      0           1           2      3
        //as require [add/clear/list] rshop <shop>
        if(!sender.hasPermission("areashop.require")) {
            plugin.message(sender, "require-noPermission");
            return;
        }   
        List<String> arguments = Arrays.asList("add","clear","list");
        if(args.length < 2 || args[1] == null|| !arguments.contains(args[1])) {
            plugin.message(sender, "require-help");
            return;
        }       
        GeneralRegion region = null;
        //      0     1    2      3
        //as require add rshop <shop>
        if(args[1].equals("add")) {
            if(args.length<3){
                plugin.message(sender, "require-help");
                return;                
            }
            if(args.length < 4){
            if (sender instanceof Player) {
                    // get the region by location
                    List<GeneralRegion> regions = plugin.getFileManager().getAllApplicableRegions(((Player) sender).getLocation());
                    if (regions.isEmpty()) {
                        plugin.message(sender, "cmd-noRegionsAtLocation");
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
                region = plugin.getFileManager().getRegion(args[3]);
            }       
            if(region == null) {
                plugin.message(sender, "require-notRegistered", args[3]);
                return;
            }            
            String rshop = args[2];
          //TODO check if rshop exists?! i keep it as string for now
            if(region.getRequireShops().contains(rshop)) {
                plugin.message(sender, "require-List", region.getName(), StringUtils.join(region.getRequireShops(),", "));
                plugin.message(sender, "require-existsInList", region.getName() , rshop);
                return;
            }            
            region.addRequireShops(rshop);
            plugin.message(sender, "require-successAdd", region.getName(), rshop, StringUtils.join(region.getRequireShops(),", "));
            
        } else if(args[1].equals("list")) {
            //      0     1     2 
            //as require list <rshop> 
            if(args.length<3){
            if (sender instanceof Player) {
                // get the region by location
                    List<GeneralRegion> regions = plugin.getFileManager().getAllApplicableRegions(((Player) sender).getLocation());
                    if (regions.isEmpty()) {
                        plugin.message(sender, "cmd-noRegionsAtLocation");
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
                plugin.message(sender, "require-notRegistered", args[2]);
                return;
            }
            plugin.message(sender, "require-List", region.getName(), StringUtils.join(region.getRequireShops(),", "));
            return;
        } else if(args[1].equals("clear")){
            //      0     1      2 
            //as require clear <rshop> 
            if(args.length<3){
                if (sender instanceof Player) {
                    // get the region by location
                        List<GeneralRegion> regions = plugin.getFileManager().getAllApplicableRegions(((Player) sender).getLocation());
                        if (regions.isEmpty()) {
                            plugin.message(sender, "cmd-noRegionsAtLocation");
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
                    plugin.message(sender, "require-notRegistered", args[3]);
                    return;
                }
                region.clearRequireShops();
                plugin.message(sender, "require-successClear", region.getName()); 
        }
        region.updateSigns();
        region.updateRegionFlags();
        region.saveRequired();
    }
    
    @Override
    public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
        List<String> result = new ArrayList<String>();
        if(toComplete == 1) {
            result = plugin.getFileManager().getRegionNames();      
        }
        return result;
    }

}
