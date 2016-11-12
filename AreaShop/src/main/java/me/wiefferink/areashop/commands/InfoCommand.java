package me.wiefferink.areashop.commands;

import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.Utils;
import me.wiefferink.areashop.messages.Message;
import me.wiefferink.areashop.regions.BuyRegion;
import me.wiefferink.areashop.regions.GeneralRegion;
import me.wiefferink.areashop.regions.RegionGroup;
import me.wiefferink.areashop.regions.RentRegion;
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
	 * Display a page of a list of regions
	 * @param sender The CommandSender to send the messages to
	 * @param regions The regions to display
	 * @param filterGroup The group to filter the regions by
	 * @param keyHeader The header to print above the page
	 * @param pageInput The page number, if any
	 * @param baseCommand The command to execute for next/previous page
	 */
	private void showSortedPagedList(CommandSender sender, List<? extends GeneralRegion> regions, RegionGroup filterGroup, String keyHeader, String pageInput, String baseCommand) {
		int MAX_ITEMS = 20;
		int ITEMS_PER_PAGE = MAX_ITEMS-2;
		int page = 1;
		if(pageInput != null && Utils.isNumeric(pageInput)) {
			try {
				page = Integer.parseInt(pageInput);
			} catch(NumberFormatException e) {
				plugin.message(sender, "info-wrongPage", pageInput);
				return;
			}
		}
		if(filterGroup != null) {
			for(Iterator<? extends GeneralRegion> it = regions.iterator(); it.hasNext(); ) {
				if(!filterGroup.isMember(it.next())) {
					it.remove();
				}
			}
		}
		if(regions.isEmpty()) {
			plugin.message(sender, "info-noRegions");
		} else {
			long start = Calendar.getInstance().getTimeInMillis();
			// First sort by type, then by name
			Collections.sort(regions, new Comparator<GeneralRegion>() {
				@Override
				public int compare(GeneralRegion one, GeneralRegion two) {
					int typeCompare = getTypeOrder(two).compareTo(getTypeOrder(one));
					if(typeCompare != 0) {
						return typeCompare;
					} else {
						return one.getName().compareTo(two.getName());
					}
				}
			});
			// Header
			Message limitedToGroup = Message.empty();
			if(filterGroup != null) {
				limitedToGroup = Message.fromKey("info-limitedToGroup").replacements(filterGroup.getName());
			}
			plugin.message(sender, keyHeader, limitedToGroup);
			// Page entries
			int totalPages = (int)Math.ceil(regions.size()/(double)ITEMS_PER_PAGE); // Clip page to correct boundaries, not much need to tell the user
			if(regions.size() == ITEMS_PER_PAGE+1) { // 19 total items is mapped to 1 page of 19
				ITEMS_PER_PAGE++;
				totalPages = 1;
			}
			page = Math.max(1, Math.min(totalPages, page));
			int linesPrinted = 1; // header
			for(int i = (page-1)*ITEMS_PER_PAGE; i < page*ITEMS_PER_PAGE && i < regions.size(); i++) {
				String state;
				GeneralRegion region = regions.get(i);
				if(region.getType() == GeneralRegion.RegionType.RENT) {
					if(region.getOwner() == null) {
						state = "Forrent";
					} else {
						state = "Rented";
					}
				} else {
					if(region.getOwner() == null) {
						state = "Forsale";
					} else if(!((BuyRegion)region).isInResellingMode()) {
						state = "Sold";
					} else {
						state = "Reselling";
					}
				}
				plugin.messageNoPrefix(sender, "info-entry"+state, region);
				linesPrinted++;
			}
			Message footer = Message.empty();
			// Previous button
			if(page > 1) {
				footer.append(Message.fromKey("info-pagePrevious").replacements(baseCommand+" "+(page-1)));
			} else {
				footer.append(Message.fromKey("info-pageNoPrevious"));
			}
			// Page status
			if(totalPages > 1) {
				String pageString = ""+page;
				for(int i = pageString.length(); i < (totalPages+"").length(); i++) {
					pageString = "0"+pageString;
				}
				footer.append(Message.fromKey("info-pageStatus").replacements(page, totalPages));
				if(page < totalPages) {
					footer.append(Message.fromKey("info-pageNext").replacements(baseCommand+" "+(page+1)));
				} else {
					footer.append(Message.fromKey("info-pageNoNext"));
				}
				// Fill up space if the page is not full (aligns header nicely)
				for(int i = linesPrinted; i < MAX_ITEMS-1; i++) {
					sender.sendMessage(" ");
				}
				footer.send(sender);
				long end = Calendar.getInstance().getTimeInMillis();
			}
		}
	}

	/**
	 * Get an integer to order by type, usable for Comparators
	 * @param region The region to get the order for
	 * @return An integer for sorting by type
	 */
	private Integer getTypeOrder(GeneralRegion region) {
		if(region.getType() == GeneralRegion.RegionType.RENT) {
			if(region.getOwner() == null) {
				return 1;
			} else {
				return 2;
			}
		} else {
			if(region.getOwner() == null) {
				return 3;
			} else if(!((BuyRegion)region).isInResellingMode()) {
				return 4;
			} else {
				return 5;
			}
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
			Set<String> groupFilters = new HashSet<>(Arrays.asList("all", "rented", "forrent", "sold", "forsale", "reselling"));
			if(groupFilters.contains(args[1].toLowerCase()) && args.length > 2) {
				if(!Utils.isNumeric(args[2])) {
					filterGroup = plugin.getFileManager().getGroup(args[2]);
					if(filterGroup == null) {
						plugin.message(sender, "info-noFiltergroup", args[2]);
						return;
					}
					// Pass page number to args[2] if available
					if(args.length > 3) {
						args[2] = args[3];
					}
				}
			}
			
			// All regions
			if(args[1].equalsIgnoreCase("all")) {
				showSortedPagedList(sender, plugin.getFileManager().getRegions(), filterGroup, "info-allHeader", (args.length > 2 ? args[2] : null), "/areashop info all");
			}
			
			// Rented regions
			else if(args[1].equalsIgnoreCase("rented")) {
				List<RentRegion> regions = plugin.getFileManager().getRents();
				for(Iterator<RentRegion> it = regions.iterator(); it.hasNext(); ) {
					if(!it.next().isRented()) {
						it.remove();
					}
				}
				showSortedPagedList(sender, regions, filterGroup, "info-rentedHeader", (args.length > 2 ? args[2] : null), "/areashop info rented");
			}
			// Forrent regions
			else if(args[1].equalsIgnoreCase("forrent")) {
				List<RentRegion> regions = plugin.getFileManager().getRents();
				for(Iterator<RentRegion> it = regions.iterator(); it.hasNext(); ) {
					if(it.next().isRented()) {
						it.remove();
					}
				}
				showSortedPagedList(sender, regions, filterGroup, "info-forrentHeader", (args.length > 2 ? args[2] : null), "/areashop info forrent");
			}
			// Sold regions
			else if(args[1].equalsIgnoreCase("sold")) {
				List<BuyRegion> regions = plugin.getFileManager().getBuys();
				for(Iterator<BuyRegion> it = regions.iterator(); it.hasNext(); ) {
					if(!it.next().isSold()) {
						it.remove();
					}
				}
				showSortedPagedList(sender, regions, filterGroup, "info-soldHeader", (args.length > 2 ? args[2] : null), "/areashop info sold");
			}
			// Forsale regions
			else if(args[1].equalsIgnoreCase("forsale")) {
				List<BuyRegion> regions = plugin.getFileManager().getBuys();
				for(Iterator<BuyRegion> it = regions.iterator(); it.hasNext(); ) {
					if(it.next().isSold()) {
						it.remove();
					}
				}
				showSortedPagedList(sender, regions, filterGroup, "info-forsaleHeader", (args.length > 2 ? args[2] : null), "/areashop info forsale");
			}
			// Reselling regions
			else if(args[1].equalsIgnoreCase("reselling")) {
				List<BuyRegion> regions = plugin.getFileManager().getBuys();
				for(Iterator<BuyRegion> it = regions.iterator(); it.hasNext(); ) {
					if(!it.next().isInResellingMode()) {
						it.remove();
					}
				}
				showSortedPagedList(sender, regions, filterGroup, "info-resellingHeader", (args.length > 2 ? args[2] : null), "/areashop info reselling");
			}

			// List of regions without a group
			else if(args[1].equalsIgnoreCase("nogroup")) {
				List<GeneralRegion> regions = plugin.getFileManager().getRegions();
				for(RegionGroup group : plugin.getFileManager().getGroups()) {
					regions.removeAll(group.getMemberRegions());
				}
				if(regions.isEmpty()) {
					plugin.message(sender, "info-nogroupNone");
				} else {
					showSortedPagedList(sender, regions, filterGroup, "info-nogroupHeader", (args.length > 2 ? args[2] : null), "/areashop info nogroup");
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
							// Money back
							if(UnrentCommand.canUse(sender, rent)) {
								plugin.messageNoPrefix(sender, "info-regionMoneyBackRentClick", rent);
							} else {
								plugin.messageNoPrefix(sender, "info-regionMoneyBackRent", rent);
							}
							// Friends
							if(!rent.getFriendsFeature().getFriendNames().isEmpty()) {
								String messagePart = "info-friend";
								if(DelfriendCommand.canUse(sender, rent)) {
									messagePart = "info-friendRemove";
								}
								plugin.messageNoPrefix(sender, "info-regionFriends", rent, Utils.combinedMessage(rent.getFriendsFeature().getFriendNames(), messagePart));
							}
						} else {
							plugin.messageNoPrefix(sender, "info-regionCanBeRented", rent);
						}
						if(rent.getLandlordName() != null) {
							plugin.messageNoPrefix(sender, "info-regionLandlord", rent);
						}
						// Maximum extends
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
						// Teleport
						Message tp = Message.fromKey("info-prefix");
						boolean foundSomething = false;
						if(TeleportCommand.canUse(sender, rent)) {
							foundSomething = true;
							tp.append(Message.fromKey("info-regionTeleport").replacements(rent));
						}
						if(SetteleportCommand.canUse(sender, rent)) {
							if(foundSomething) {
								tp.append(", ");
							}
							foundSomething = true;
							tp.append(Message.fromKey("info-setRegionTeleport").replacements(rent));
						}
						if(foundSomething) {
							tp.append(".");
							tp.send(sender);
						}
						// Signs
						List<String> signLocations = new ArrayList<>();
						for(Location location : rent.getSignsFeature().getSignLocations()) {
							signLocations.add(Message.fromKey("info-regionSignLocation").replacements(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ()).getPlain());
						}
						if(!signLocations.isEmpty()) {
							plugin.messageNoPrefix(sender, "info-regionSigns", signLocations.toArray());
						}
						// Groups
						if(sender.hasPermission("areashop.groupinfo") && !rent.getGroupNames().isEmpty()) {
							plugin.messageNoPrefix(sender, "info-regionGroups", Utils.createCommaSeparatedList(rent.getGroupNames()));
						}
						// Restoring
						if(rent.isRestoreEnabled()) {
							plugin.messageNoPrefix(sender, "info-regionRestoringRent", rent);
						}
						// Restrictions
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
							// Money back
							if(SellCommand.canUse(sender, buy)) {
								plugin.messageNoPrefix(sender, "info-regionMoneyBackBuyClick", buy);
							} else {
								plugin.messageNoPrefix(sender, "info-regionMoneyBackBuy", buy);
							}
							// Friends
							if(!buy.getFriendsFeature().getFriendNames().isEmpty()) {
								String messagePart = "info-friend";
								if(DelfriendCommand.canUse(sender, buy)) {
									messagePart = "info-friendRemove";
								}
								plugin.messageNoPrefix(sender, "info-regionFriends", buy, Utils.combinedMessage(buy.getFriendsFeature().getFriendNames(), messagePart));
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
						// Teleport
						Message tp = Message.fromKey("info-prefix");
						boolean foundSomething = false;
						if(TeleportCommand.canUse(sender, buy)) {
							foundSomething = true;
							tp.append(Message.fromKey("info-regionTeleport").replacements(buy));
						}
						if(SetteleportCommand.canUse(sender, buy)) {
							if(foundSomething) {
								tp.append(", ");
							}
							foundSomething = true;
							tp.append(Message.fromKey("info-setRegionTeleport").replacements(buy));
						}
						if(foundSomething) {
							tp.append(".");
							tp.send(sender);
						}
						// Signs
						List<String> signLocations = new ArrayList<>();
						for(Location location : buy.getSignsFeature().getSignLocations()) {
							signLocations.add(Message.fromKey("info-regionSignLocation").replacements(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ()).getPlain());
						}
						if(!signLocations.isEmpty()) {
							plugin.messageNoPrefix(sender, "info-regionSigns", signLocations.toArray());
						}
						// Groups
						if(sender.hasPermission("areashop.groupinfo") && !buy.getGroupNames().isEmpty()) {
							plugin.messageNoPrefix(sender, "info-regionGroups", Utils.createCommaSeparatedList(buy.getGroupNames()));
						}
						// Restoring
						if(buy.isRestoreEnabled()) {
							plugin.messageNoPrefix(sender, "info-regionRestoringBuy", buy);
						}
						// Restrictions
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
			result.addAll(Arrays.asList("all", "rented", "forrent", "sold", "forsale", "player", "region", "nogroup", "reselling"));
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


























