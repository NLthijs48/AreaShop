package nl.evolutioncoding.areashop.commands;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.Utils;
import nl.evolutioncoding.areashop.messages.Message;
import nl.evolutioncoding.areashop.regions.BuyRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import nl.evolutioncoding.areashop.regions.RegionGroup;
import nl.evolutioncoding.areashop.regions.RentRegion;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class InfoCommand extends CommandAreaShop {

	public InfoCommand(AreaShop plugin) {
		super(plugin);
	}
	
	@Override
	public String getCommandStart() {
		return "areashop info";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("areashop.info")) {
			return "help-info";
		}
		return null;
	}
	
	/**
	 * Filter group of regions, join their names and printout the correct message
	 * @param sender The CommandSender to receive the message
	 * @param regions The regions
	 * @param filterGroup The group to filter to the regions with
	 * @param keySomeFound The key of the message to display when some regions are found
	 * @param keyNoneFound The key of the message to display when no regions are found
	 */
	private void displayMessage(CommandSender sender, Set<? extends GeneralRegion> regions, RegionGroup filterGroup, String keySomeFound, String keyNoneFound) {
		if(filterGroup != null) {
			Iterator<? extends GeneralRegion> it = regions.iterator();
			while(it.hasNext()) {
				GeneralRegion region = it.next();
				if(!filterGroup.isMember(region)) {
					it.remove();
				}
			}
		}
		if(regions.isEmpty()) {
			plugin.message(sender, keyNoneFound);
		} else {
			plugin.message(sender, keySomeFound, StringUtils.join(regions.iterator(), ", "));
		}
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("areashop.info")) {
			plugin.message(sender, "info-noPermission");
			return;
		}
		if(args.length > 1 && args[1] != null) {
			// Get filter group (only used by some commands)
			RegionGroup filterGroup = null;
			Set<String> groupFilters = new HashSet<>(Arrays.asList("all", "rented", "forrent", "sold", "forsale"));
			if(groupFilters.contains(args[0].toLowerCase())) {
				filterGroup = plugin.getFileManager().getGroup(args[2]);
				if(filterGroup == null) {
					plugin.message(sender, "info-noFiltergroup", args[2]);
					return;
				}
			}
			
			// All regions
			if(args[1].equalsIgnoreCase("all")) {
				Set<GeneralRegion> regions = new TreeSet<GeneralRegion>(plugin.getFileManager().getRents());
				displayMessage(sender, regions, filterGroup, "info-all-rents", "info-all-noRents");

				regions = new TreeSet<GeneralRegion>(plugin.getFileManager().getBuys());
				displayMessage(sender, regions, filterGroup, "info-all-buys", "info-all-noBuys");
			}
			
			// Rented regions
			else if(args[1].equalsIgnoreCase("rented")) {
				Set<GeneralRegion> regions = new TreeSet<>();
				for(RentRegion region : plugin.getFileManager().getRents()) {
					if(region.isRented()) {
						regions.add(region);
					}
				}
				displayMessage(sender, regions, filterGroup, "info-rented", "info-noRented");							
			} 
			// Forrent regions
			else if(args[1].equalsIgnoreCase("forrent")) {
				Set<GeneralRegion> regions = new TreeSet<>();
				for(RentRegion region : plugin.getFileManager().getRents()) {
					if(!region.isRented()) {
						regions.add(region);
					}
				}
				displayMessage(sender, regions, filterGroup, "info-unrented", "info-noUnrented");	
			} 
			// Sold regions
			else if(args[1].equalsIgnoreCase("sold")) {
				Set<GeneralRegion> regions = new TreeSet<>();
				for(BuyRegion region : plugin.getFileManager().getBuys()) {
					if(region.isSold()) {
						regions.add(region);
					}
				}
				displayMessage(sender, regions, filterGroup, "info-sold", "info-noSold");							
			} 
			// Forsale regions
			else if(args[1].equalsIgnoreCase("forsale")) {
				Set<GeneralRegion> regions = new TreeSet<>();
				for(BuyRegion region : plugin.getFileManager().getBuys()) {
					if(!region.isSold()) {
						regions.add(region);
					}
				}
				displayMessage(sender, regions, filterGroup, "info-forsale", "info-noForsale");								
			} 
			// Player regions
			else if(args[1].equalsIgnoreCase("player")) {
				if(args.length > 2 && args[2] != null) {
					// Rents
					Set<GeneralRegion> regions = new TreeSet<>();
					for(RentRegion region : plugin.getFileManager().getRents()) {
						if(region.isRented() && region.getPlayerName().equalsIgnoreCase(args[2])) {
							regions.add(region);
						}
					}
					if(regions.isEmpty()) {
						plugin.message(sender, "info-playerNoRents", args[2]);
					} else {
						plugin.message(sender, "info-playerRents", args[2], StringUtils.join(regions.iterator(), ", "));
					}
					// Buys
					regions = new TreeSet<>();
					for(BuyRegion region : plugin.getFileManager().getBuys()) {
						if(region.isSold() && region.getPlayerName().equalsIgnoreCase(args[2])) {
							regions.add(region);
						}
					}
					if(regions.isEmpty()) {
						plugin.message(sender, "info-playerNoBuys", args[2]);
					} else {
						plugin.message(sender, "info-playerBuys", args[2], StringUtils.join(regions.iterator(), ", "));
					}
				} else {
					plugin.message(sender, "info-playerHelp");
				}
			} 
			// Region info
			else if(args[1].equalsIgnoreCase("region")) {
				if(args.length > 1) {
					RentRegion rent = null;
					BuyRegion buy = null;
					if(args.length > 2) {
						rent = plugin.getFileManager().getRent(args[2]);
						buy = plugin.getFileManager().getBuy(args[2]);
					} else {
						if (sender instanceof Player) {
							// get the region by location
							List<GeneralRegion> regions = Utils.getAllApplicableRegions(((Player) sender).getLocation());
							if (regions.isEmpty()) {
								plugin.message(sender, "cmd-noRegionsAtLocation");
								return;
							} else if (regions.size() > 1) {
								plugin.message(sender, "cmd-moreRegionsAtLocation");
								return;
							} else {
								if(regions.get(0).isRentRegion()) {
									rent = (RentRegion)regions.get(0);
								} else if(regions.get(0).isBuyRegion()) {
									buy = (BuyRegion)regions.get(0);
								}
							}
						} else {
							plugin.message(sender, "cmd-automaticRegionOnlyByPlayer");
							return;
						}
					}
					
					if(rent == null && buy == null) {
						plugin.message(sender, "info-regionNotExisting", args[2]);
						return;
					}					
					
					if(rent != null) {
						plugin.message(sender, "info-regionHeaderRent", rent);
						if(rent.isRented()) {
							plugin.messageNoPrefix(sender, "info-regionRented", rent);
							plugin.messageNoPrefix(sender, "info-regionExtending", rent);
							plugin.messageNoPrefix(sender, "info-regionMoneyBackRent", rent);
							if(!rent.getFriendNames().isEmpty()) {
								plugin.messageNoPrefix(sender, "info-regionFriends", rent);
							}
						} else {
							plugin.messageNoPrefix(sender, "info-regionCanBeRented", rent);
						}
						if(rent.getLandlordName() != null) {
							plugin.messageNoPrefix(sender, "info-regionLandlord", rent);
						}
						if(rent.getMaxExtends() != -1) {
							if(rent.getMaxExtends() == 0) {
								plugin.messageNoPrefix(sender, "info-regionNoExtending", rent);
							} else if(rent.isRented()) {
								plugin.messageNoPrefix(sender, "info-regionExtendsLeft", rent);
							} else {
								plugin.messageNoPrefix(sender, "info-regionMaxExtends", rent);
							}
						}
						if(rent.getMaxRentTime() != -1) {
							plugin.messageNoPrefix(sender, "info-regionMaxRentTime", rent);
						}
						if(rent.getInactiveTimeUntilUnrent() != -1) {
							plugin.messageNoPrefix(sender, "info-regionInactiveUnrent", rent);			
						}
						if(sender.hasPermission("areashop.teleport") || sender.hasPermission("areashop.teleportall")) {
							Location teleport = rent.getTeleportLocation();
							if(teleport == null) {
								if(rent.isRented()) {
									plugin.messageNoPrefix(sender, "info-regionNoTeleport", rent, Message.fromKey("info-regionTeleportHint").replacements(rent));
								} else {
									plugin.messageNoPrefix(sender, "info-regionNoTeleport", rent, "");
								}
							} else {
								plugin.messageNoPrefix(sender, "info-regionTeleportAt", rent, teleport.getWorld().getName(), teleport.getBlockX(), teleport.getBlockY(), teleport.getBlockZ(), (int)teleport.getPitch(), (int)teleport.getYaw());
							}
						}
						List<String> signLocations = new ArrayList<>();
						for(Location location : rent.getSignLocations()) {
							signLocations.add(Message.fromKey("info-regionSignLocation").replacements(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ()).getPlain());
						}
						if(!signLocations.isEmpty()) {
							plugin.messageNoPrefix(sender, "info-regionSigns", signLocations.toArray());
						}
						if(sender.hasPermission("areashop.groupinfo") && !rent.getGroupNames().isEmpty()) {
							plugin.messageNoPrefix(sender, "info-regionGroups", Utils.createCommaSeparatedList(rent.getGroupNames()));
						}
						if(rent.isRestoreEnabled()) {
							if(sender.hasPermission("areashop.setrestore")) {
								plugin.messageNoPrefix(sender, "info-regionRestoringRent", rent, Message.fromKey("info-regionRestoringProfile").replacements(rent.getRestoreProfile()));
							} else {
								plugin.messageNoPrefix(sender, "info-regionRestoringRent", rent, "");
							}
						}
						if(!rent.isRented()) {
							if(rent.restrictedToRegion()) {
								plugin.messageNoPrefix(sender, "info-regionRestrictedRegionRent", rent);
							} else if(rent.restrictedToWorld()) {
								plugin.messageNoPrefix(sender, "info-regionRestrictedWorldRent", rent);
							}
						}
						plugin.messageNoPrefix(sender, "info-regionFooterRent", rent);
					} else if(buy != null) {
						plugin.message(sender, "info-regionHeaderBuy", buy);
						if(buy.isSold()) {
							if(buy.isInResellingMode()) {
								plugin.messageNoPrefix(sender, "info-regionReselling", buy);
								plugin.messageNoPrefix(sender, "info-regionReselPrice", buy);
							} else {
								plugin.messageNoPrefix(sender, "info-regionBought", buy);
							}
							plugin.messageNoPrefix(sender, "info-regionMoneyBackBuy", buy);
							if(!buy.getFriendNames().isEmpty()) {
								plugin.messageNoPrefix(sender, "info-regionFriends", buy);
							}
						} else {
							plugin.messageNoPrefix(sender, "info-regionCanBeBought", buy);
						}
						if(buy.getLandlord() != null) {
							plugin.messageNoPrefix(sender, "info-regionLandlord", buy);
						}
						if(buy.getInactiveTimeUntilSell() != -1) {
							plugin.messageNoPrefix(sender, "info-regionInactiveSell", buy);			
						}
						if(sender.hasPermission("areashop.teleport") || sender.hasPermission("areashop.teleportall")) {
							Location teleport = buy.getTeleportLocation();
							if(teleport == null) {
								if(buy.isSold()) {
									plugin.messageNoPrefix(sender, "info-regionNoTeleport", buy, Message.fromKey("info-regionTeleportHint").replacements(buy));
								} else {
									plugin.messageNoPrefix(sender, "info-regionNoTeleport", buy, "");
								}
							} else {
								plugin.messageNoPrefix(sender, "info-regionTeleportAt", buy, teleport.getWorld().getName(), teleport.getBlockX(), teleport.getBlockY(), teleport.getBlockZ(), (int)teleport.getPitch(), (int)teleport.getYaw());
							}
						}
						List<String> signLocations = new ArrayList<>();
						for(Location location : buy.getSignLocations()) {
							signLocations.add(Message.fromKey("info-regionSignLocation").replacements(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ()).getPlain());
						}
						if(!signLocations.isEmpty()) {
							plugin.messageNoPrefix(sender, "info-regionSigns", signLocations.toArray());
						}
						if(sender.hasPermission("areashop.groupinfo") && !buy.getGroupNames().isEmpty()) {
							plugin.messageNoPrefix(sender, "info-regionGroups", Utils.createCommaSeparatedList(buy.getGroupNames()));
						}
						if(buy.isRestoreEnabled()) {
							if(sender.hasPermission("areashop.setrestore")) {
								plugin.messageNoPrefix(sender, "info-regionRestoringBuy", buy, Message.fromKey("info-regionRestoringProfile").replacements(buy.getRestoreProfile()));
							} else {
								plugin.messageNoPrefix(sender, "info-regionRestoringBuy", buy, "");
							}
						}
						if(!buy.isSold()) {
							if(buy.restrictedToRegion()) {
								plugin.messageNoPrefix(sender, "info-regionRestrictedRegionBuy", buy);
							} else if(buy.restrictedToWorld()) {
								plugin.messageNoPrefix(sender, "info-regionRestrictedWorldBuy", buy);
							}
						}
						plugin.messageNoPrefix(sender, "info-regionFooterBuy", buy);
					}
				} else {
					plugin.message(sender, "info-regionHelp");
				}
			}

			// List of regions without a group
			else if(args[1].equalsIgnoreCase("nogroup")) {
				// Rental regions
				Set<String> rents = new TreeSet<>(plugin.getFileManager().getRentNames());
				for(RegionGroup group : plugin.getFileManager().getGroups()) {
					rents.removeAll(group.getMembers());
				}
				String message = StringUtils.join(rents, ", ");
				if(message.equals("")) {
					plugin.message(sender, "info-nogroupNoRents");
				} else {
					plugin.message(sender, "info-nogroupRents", message);
				}
				
				// Buy regions
				Set<String> buys = new TreeSet<>(plugin.getFileManager().getBuyNames());
				for(RegionGroup group : plugin.getFileManager().getGroups()) {
					buys.removeAll(group.getMembers());
				}
				message = StringUtils.join(buys, ", ");
				if(message.equals("")) {
					plugin.message(sender, "info-nogroupNoBuys");
				} else {
					plugin.message(sender, "info-nogroupBuys", message);
				}
			} else {
				plugin.message(sender, "info-help");
			}
		} else {
			plugin.message(sender, "info-help");
		}	
	}
	
	
	/**
	 * Create a comma-space separated list from a collection of strings
	 * @param list The collection with strings
	 * @return A comma-space separated list of the strings in the collection
	 */
	public String createCommaString(Collection<String> list) {
		String result = "";
		boolean first = true;
		for(String part : list) {
			if(first) {
				result += part;
				first = false;
			} else {
				result += ", " + part;
			}
		}		
		return result;
	}
	
	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		List<String> result = new ArrayList<>();
		if(toComplete == 2) {
			result.addAll(Arrays.asList("all", "rented", "forrent", "sold", "forsale", "player", "region", "nogroup"));
		} else if(toComplete == 3) {
			if(start[2].equalsIgnoreCase("player")) {
				for(Player player : Utils.getOnlinePlayers()) {
					result.add(player.getName());
				}
			} else if(start[2].equalsIgnoreCase("region")) {
				result.addAll(plugin.getFileManager().getBuyNames());
				result.addAll(plugin.getFileManager().getRentNames());
			}
		}
		return result;
	}
}


























