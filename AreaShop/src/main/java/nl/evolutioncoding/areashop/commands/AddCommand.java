package nl.evolutioncoding.areashop.commands;

import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.Utils;
import nl.evolutioncoding.areashop.managers.FileManager.AddResult;
import nl.evolutioncoding.areashop.regions.BuyRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion.RegionEvent;
import nl.evolutioncoding.areashop.regions.GeneralRegion.RegionType;
import nl.evolutioncoding.areashop.regions.RentRegion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

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
			return "help-add";
		}
		return null;
	}

	@Override
	public void execute(final CommandSender sender, final String[] args) {
		if(		   !sender.hasPermission("areashop.createrent") 
				&& !sender.hasPermission("areashop.createrent.member")
				&& !sender.hasPermission("areashop.createrent.owner")
				
				&& !sender.hasPermission("areashop.createbuy")
				&& !sender.hasPermission("areashop.createbuy.member")
				&& !sender.hasPermission("areashop.createbuy.owner")) {
			plugin.message(sender, "add-noPermission");
			return;
		}
		
		if(args.length < 2 || args[1] == null || (!"rent".equals(args[1].toLowerCase()) && !"buy".equals(args[1].toLowerCase()))) {
			plugin.message(sender, "add-help");
			return;
		}
		List<ProtectedRegion> regions = new ArrayList<>();
		World world;
		Player player = null;
		if(sender instanceof Player) {
			player = (Player)sender;
		}
		if(args.length == 2) {
			if(player == null) {
				plugin.message(sender, "cmd-weOnlyByPlayer");
				return;
			}		
			Selection selection = plugin.getWorldEdit().getSelection(player);
			if(selection == null) {
				plugin.message(player, "cmd-noSelection");
				return;
			}
			world = selection.getWorld();
			regions = Utils.getWERegionsInSelection(selection);
			if(regions.size() == 0) {
				plugin.message(player, "cmd-noWERegionsFound");
				return;
			}
		} else {
			if(player != null) {
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
		final boolean isRent = "rent".equals(args[1].toLowerCase());
		final List<ProtectedRegion> finalRegions = regions;
		final Player finalPlayer = player;
		final World finalWorld = world;
		AreaShop.debug("Starting add task with " + regions.size() + " regions");
        new BukkitRunnable() {
			private int current = 0;
			private ArrayList<String> namesSuccess = new ArrayList<>();
			private ArrayList<String> namesAlready = new ArrayList<>();
			private ArrayList<String> namesBlacklisted = new ArrayList<>();
			private ArrayList<String> namesNoPermission = new ArrayList<>();
			private ArrayList<String> namesExternal = new ArrayList<>();
			
			@Override
			public void run() {
				for(int i=0; i<plugin.getConfig().getInt("adding.regionsPerTick"); i++) {
					if(current < finalRegions.size()) {
						ProtectedRegion region = finalRegions.get(current);
						// Determine if the player is an owner or member of the region
						boolean isMember = finalPlayer != null && plugin.getWorldGuardHandler().containsMember(region, finalPlayer.getUniqueId());
						boolean isOwner = finalPlayer != null && plugin.getWorldGuardHandler().containsMember(region, finalPlayer.getUniqueId());
						String type;
						if(isRent) {
							type = "rent";				
						} else {
							type = "buy";
						}
						AddResult result = plugin.getFileManager().checkRegionAdd(sender, region, isRent ? RegionType.RENT : RegionType.BUY);
						if(result == AddResult.ALREADYADDED) {
							namesAlready.add(region.getId());
						} else if(result == AddResult.BLACKLISTED) {
							namesBlacklisted.add(region.getId());
						} else if(result == AddResult.NOPERMISSION) {
							namesNoPermission.add(region.getId());
						} else {
							// Check if the player should be landlord
							boolean landlord = (!sender.hasPermission("areashop.create" + type)
								&& ((sender.hasPermission("areashop.create" + type + ".owner") && isOwner)
									|| (sender.hasPermission("areashop.create" + type + ".member") && isMember)));
							if(isRent) {
								RentRegion rent = new RentRegion(plugin, region.getId(), finalWorld);
								// Set landlord
								if(landlord) {
									rent.setLandlord(finalPlayer.getUniqueId(), finalPlayer.getName());
								}
								// Run commands
								rent.runEventCommands(RegionEvent.CREATED, true);						
								plugin.getFileManager().addRent(rent);
								rent.handleSchematicEvent(RegionEvent.CREATED);

								rent.update();

								// Run commands
								rent.runEventCommands(RegionEvent.CREATED, false);
								namesSuccess.add(region.getId());
							} else {
								BuyRegion buy = new BuyRegion(plugin, region.getId(), finalWorld);
								// Set landlord
								if(landlord) {
									buy.setLandlord(finalPlayer.getUniqueId(), finalPlayer.getName());
								}
								// Run commands
								buy.runEventCommands(RegionEvent.CREATED, true);
								
								plugin.getFileManager().addBuy(buy);
								buy.handleSchematicEvent(RegionEvent.CREATED);

								buy.update();

								// Run commands
								buy.runEventCommands(RegionEvent.CREATED, false);
								namesSuccess.add(region.getId());
							}
						}
						current++;
					}
				}
				if(current >= finalRegions.size()) {
					if(!namesSuccess.isEmpty()) {
						plugin.message(sender, "add-success", args[1], Utils.createCommaSeparatedList(namesSuccess));
					}
					if(!namesAlready.isEmpty()) {
						plugin.message(sender, "add-failed", Utils.createCommaSeparatedList(namesAlready));
					}
					if(!namesBlacklisted.isEmpty()) {
						plugin.message(sender, "add-blacklisted", Utils.createCommaSeparatedList(namesBlacklisted));
					}
					if(!namesNoPermission.isEmpty()) {
						plugin.message(sender, "add-noPermissionRegions", Utils.createCommaSeparatedList(namesNoPermission));
						plugin.message(sender, "add-noPermissionOwnerMember");
					}
					this.cancel();
				}
			}
        }.runTaskTimer(plugin, 1, 1);
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		List<String> result = new ArrayList<>();
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










