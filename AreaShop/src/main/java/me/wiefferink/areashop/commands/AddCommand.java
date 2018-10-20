package me.wiefferink.areashop.commands;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.interfaces.WorldEditSelection;
import me.wiefferink.areashop.managers.FileManager;
import me.wiefferink.areashop.regions.BuyRegion;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.regions.RentRegion;
import me.wiefferink.areashop.tools.Utils;
import me.wiefferink.bukkitdo.Do;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

public class AddCommand extends CommandAreaShop {

	@Override
	public String getCommandStart() {
		return "areashop add";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.createrent")
				|| target.hasPermission("areashop.createrent.member")
				|| target.hasPermission("areashop.createrent.owner")

				|| target.hasPermission("areashop.createbuy")
				|| target.hasPermission("areashop.createbuy.member")
				|| target.hasPermission("areashop.createbuy.owner")) {
			return "help-add";
		}
		return null;
	}

	@Override
	public void execute(final CommandSender sender, final String[] args) {
		if(!sender.hasPermission("areashop.createrent")
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
		Map<String, ProtectedRegion> regions = new HashMap<>();
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
			WorldEditSelection selection = plugin.getWorldEditHandler().getPlayerSelection(player);
			if(selection == null) {
				plugin.message(player, "cmd-noSelection");
				return;
			}
			world = selection.getWorld();
			regions = Utils.getWorldEditRegionsInSelection(selection).stream().collect(Collectors.toMap(ProtectedRegion::getId, region -> region));
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
			ProtectedRegion region = plugin.getRegionManager(world).getRegion(args[2]);
			if(region == null) {
				plugin.message(sender, "cmd-noRegion", args[2]);
				return;
			}
			regions.put(args[2], region);
		}
		final boolean isRent = "rent".equals(args[1].toLowerCase());
		final Player finalPlayer = player;
		AreaShop.debug("Starting add task with " + regions.size() + " regions");

		TreeSet<GeneralRegion> regionsSuccess = new TreeSet<>();
		TreeSet<GeneralRegion> regionsAlready = new TreeSet<>();
		TreeSet<GeneralRegion> regionsAlreadyOtherWorld = new TreeSet<>();
		TreeSet<String> namesBlacklisted = new TreeSet<>();
		TreeSet<String> namesNoPermission = new TreeSet<>();
		Do.forAll(
			plugin.getConfig().getInt("adding.regionsPerTick"),
			regions.entrySet(),
			regionEntry -> {
				String regionName = regionEntry.getKey();
				ProtectedRegion region = regionEntry.getValue();
				// Determine if the player is an owner or member of the region
				boolean isMember = finalPlayer != null && plugin.getWorldGuardHandler().containsMember(region, finalPlayer.getUniqueId());
				boolean isOwner = finalPlayer != null && plugin.getWorldGuardHandler().containsMember(region, finalPlayer.getUniqueId());
				String type;
				if(isRent) {
					type = "rent";
				} else {
					type = "buy";
				}
				FileManager.AddResult result = plugin.getFileManager().checkRegionAdd(sender, region, world, isRent ? GeneralRegion.RegionType.RENT : GeneralRegion.RegionType.BUY);
				if(result == FileManager.AddResult.ALREADYADDED) {
					regionsAlready.add(plugin.getFileManager().getRegion(regionName));
				} else if(result == FileManager.AddResult.ALREADYADDEDOTHERWORLD) {
					regionsAlreadyOtherWorld.add(plugin.getFileManager().getRegion(regionName));
				} else if(result == FileManager.AddResult.BLACKLISTED) {
					namesBlacklisted.add(regionName);
				} else if(result == FileManager.AddResult.NOPERMISSION) {
					namesNoPermission.add(regionName);
				} else {
					// Check if the player should be landlord
					boolean landlord = (!sender.hasPermission("areashop.create" + type)
							&& ((sender.hasPermission("areashop.create" + type + ".owner") && isOwner)
							|| (sender.hasPermission("areashop.create" + type + ".member") && isMember)));
					List<UUID> existing = new ArrayList<>();
					existing.addAll(plugin.getWorldGuardHandler().getOwners(region).asUniqueIdList());
					existing.addAll(plugin.getWorldGuardHandler().getMembers(region).asUniqueIdList());
					if(isRent) {
						RentRegion rent = new RentRegion(regionName, world);
						// Set landlord
						if(landlord) {
							rent.setLandlord(finalPlayer.getUniqueId(), finalPlayer.getName());
						}
						// Run commands
						rent.runEventCommands(GeneralRegion.RegionEvent.CREATED, true);
						plugin.getFileManager().addRent(rent);
						rent.handleSchematicEvent(GeneralRegion.RegionEvent.CREATED);

						rent.update();

						// Run commands
						rent.runEventCommands(GeneralRegion.RegionEvent.CREATED, false);

						// Add existing owners/members if any
						if(!landlord && !existing.isEmpty()) {
							// TODO also execute rent events to notify other plugins?

							// Run commands
							rent.runEventCommands(GeneralRegion.RegionEvent.RENTED, true);

							// Add values to the rent and send it to FileManager
							rent.setRentedUntil(Calendar.getInstance().getTimeInMillis() + rent.getDuration());
							rent.setRenter(existing.remove(0));
							rent.updateLastActiveTime();

							// Add others as friends
							for(UUID friend : existing) {
								rent.getFriendsFeature().addFriend(friend, null);
							}

							// Fire schematic event and updated times extended
							rent.handleSchematicEvent(GeneralRegion.RegionEvent.RENTED);

							// Notify about updates
							rent.update();

							rent.runEventCommands(GeneralRegion.RegionEvent.RENTED, false);
						}

						regionsSuccess.add(rent);
					} else {
						BuyRegion buy = new BuyRegion(regionName, world);
						// Set landlord
						if(landlord) {
							buy.setLandlord(finalPlayer.getUniqueId(), finalPlayer.getName());
						}
						// Run commands
						buy.runEventCommands(GeneralRegion.RegionEvent.CREATED, true);

						plugin.getFileManager().addBuy(buy);
						buy.handleSchematicEvent(GeneralRegion.RegionEvent.CREATED);

						buy.update();

						// Run commands
						buy.runEventCommands(GeneralRegion.RegionEvent.CREATED, false);

						// Add existing owners/members if any
						if(!landlord && !existing.isEmpty()) {
							// TODO also execute buy events to notify for other plugins?

							// Run commands
							buy.runEventCommands(GeneralRegion.RegionEvent.BOUGHT, true);

							// Set the owner
							buy.setBuyer(existing.remove(0));
							buy.updateLastActiveTime();
							// Add others as friends
							for(UUID friend : existing) {
								buy.getFriendsFeature().addFriend(friend, null);
							}

							// Notify about updates
							buy.update();

							// Update everything
							buy.handleSchematicEvent(GeneralRegion.RegionEvent.BOUGHT);

							// Run commands
							buy.runEventCommands(GeneralRegion.RegionEvent.BOUGHT, false);
						}

						regionsSuccess.add(buy);
					}
				}
			},
			() -> {
				if(!regionsSuccess.isEmpty()) {
					plugin.message(sender, "add-success", args[1], Utils.combinedMessage(regionsSuccess, "region"));
				}
				if(!regionsAlready.isEmpty()) {
					plugin.message(sender, "add-failed", Utils.combinedMessage(regionsAlready, "region"));
				}
				if(!regionsAlreadyOtherWorld.isEmpty()) {
					plugin.message(sender, "add-failedOtherWorld", Utils.combinedMessage(regionsAlreadyOtherWorld, "region"));
				}
				if(!namesBlacklisted.isEmpty()) {
					plugin.message(sender, "add-blacklisted", Utils.createCommaSeparatedList(namesBlacklisted));
				}
				if(!namesNoPermission.isEmpty()) {
					plugin.message(sender, "add-noPermissionRegions", Utils.createCommaSeparatedList(namesNoPermission));
					plugin.message(sender, "add-noPermissionOwnerMember");
				}
			}
		);
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
					for(ProtectedRegion region : plugin.getRegionManager(player.getWorld()).getRegions().values()) {
						result.add(region.getId());
					}
				}
			}
		}
		return result;
	}

}










