package nl.evolutioncoding.areashop.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.Utils;
import nl.evolutioncoding.areashop.regions.BuyRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
import nl.evolutioncoding.areashop.regions.RegionGroup;
import nl.evolutioncoding.areashop.regions.RentRegion;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
			return plugin.getLanguageManager().getLang("help-info");
		}
		return null;
	}
	
	@Override
	public void execute(CommandSender sender, Command command, String[] args) {
		if(!sender.hasPermission("areashop.info")) {
			plugin.message(sender, "info-noPermission");
			return;
		}
		if(args.length > 1 && args[1] != null) {
			/* List of all regions */
			if(args[1].equalsIgnoreCase("all")) {
				String message = "";
				/* Message for rents */
				Iterator<RentRegion> itRent = plugin.getFileManager().getRents().iterator();
				if(itRent.hasNext()) {
					message = itRent.next().getName();
					while(itRent.hasNext()) {
						message += ", " + itRent.next().getName();
					}
				}
				if(message.equals("")) {
					plugin.message(sender, "info-all-noRents");
				} else {
					plugin.message(sender, "info-all-rents", message);
				}
				
				/* Message for buys */
				message = "";
				Iterator<BuyRegion> itBuy = plugin.getFileManager().getBuys().iterator();
				if(itBuy.hasNext()) {
					message = itBuy.next().getName();
					while(itBuy.hasNext()) {
						message += ", " + itBuy.next().getName();
					}
				}
				if(message.equals("")) {
					plugin.message(sender, "info-all-noBuys");
				} else {
					plugin.message(sender, "info-all-buys", message);
				}
			}
			/* List of rented regions */
			else if(args[1].equalsIgnoreCase("rented")) {
				String message = "";
				Iterator<RentRegion> it = plugin.getFileManager().getRents().iterator();
				boolean first = true;
				while(it.hasNext()) {
					RentRegion next = it.next();
					if(next.isRented()) {
						if(!first) {
							message += ", " + next.getName();
						} else {
							first = false;
							message += next.getName();
						}
					}
				}
				if(message.equals("")) {
					plugin.message(sender, "info-noRented");
				} else {
					plugin.message(sender, "info-rented", message);
				}								
			} 
			/* List of unrented regions */
			else if(args[1].equalsIgnoreCase("forrent")) {
				String message = "";
				Iterator<RentRegion> it = plugin.getFileManager().getRents().iterator();
				boolean first = true;
				while(it.hasNext()) {
					RentRegion next = it.next();
					if(!next.isRented()) {
						if(!first) {
							message += ", " + next.getName();
						} else {
							first = false;
							message = next.getName();
						}
					}
				}
				if(message.equals("")) {
					plugin.message(sender, "info-noUnrented");
				} else {
					plugin.message(sender, "info-unrented", message);
				}							
			} else if(args[1].equalsIgnoreCase("sold")) {
				String message = "";
				Iterator<BuyRegion> it = plugin.getFileManager().getBuys().iterator();
				boolean first = true;
				while(it.hasNext()) {
					BuyRegion next = it.next();
					if(next.isSold()) {
						if(!first) {
							message += ", ";
						} else {
							first = false;
						}
						message += next.getName();
					}
				}
				if(message.equals("")) {
					plugin.message(sender, "info-noSold");
				} else {
					plugin.message(sender, "info-sold", message);
				}							
			} else if(args[1].equalsIgnoreCase("forsale")) {
				String message = "";
				Iterator<BuyRegion> it = plugin.getFileManager().getBuys().iterator();
				boolean first = true;
				while(it.hasNext()) {
					BuyRegion next =it.next();
					if(!next.isSold()) {
						if(!first) {
							message += ", " + next.getName();
						} else {
							first = false;
							message = next.getName();
						}
					}
				}
				if(message.equals("")) {
					plugin.message(sender, "info-noForsale");
				} else {
					plugin.message(sender, "info-forsale", message);
				}							
			} else if(args[1].equalsIgnoreCase("player")) {
				if(args.length > 2 && args[2] != null) {
					String message = "";
					Iterator<RentRegion> itRent = plugin.getFileManager().getRents().iterator();
					boolean first = true;
					while(itRent.hasNext()) {
						RentRegion next = itRent.next();
						if(next.isRented() && next.getPlayerName().equalsIgnoreCase(args[2])) {
							if(!first) {
								message += ", " + next.getName();
							} else {
								first = false;
								message = next.getName();
							}
						}
					}
					if(message.equals("")) {
						plugin.message(sender, "info-playerNoRents", args[2]);
					} else {
						plugin.message(sender, "info-playerRents", args[2], message);
					}		
					
					message = "";
					Iterator<BuyRegion> itBuy = plugin.getFileManager().getBuys().iterator();
					first = true;
					while(itBuy.hasNext()) {
						BuyRegion next = itBuy.next();
						if(next.isSold() && next.getPlayerName().equalsIgnoreCase(args[2])) {
							if(!first) {
								message += ", ";
							} else {
								first = false;
							}
							message += next.getName();
						}
					}
					if(message.equals("")) {
						plugin.message(sender, "info-playerNoBuys", args[2]);
					} else {
						plugin.message(sender, "info-playerBuys", args[2], message);
					}	
				} else {
					plugin.message(sender, "info-playerHelp");
				}
			} else if(args[1].equalsIgnoreCase("region")) {
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
						if(rent.getLandlord() != null) {
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
									plugin.messageNoPrefix(sender, "info-regionNoTeleport", rent, plugin.getLanguageManager().getLang("info-regionTeleportHint", rent));
								} else {
									plugin.messageNoPrefix(sender, "info-regionNoTeleport", rent, "");
								}
							} else {
								plugin.messageNoPrefix(sender, "info-regionTeleportAt", rent, teleport.getWorld().getName(), teleport.getBlockX(), teleport.getBlockY(), teleport.getBlockZ(), (int)teleport.getPitch(), (int)teleport.getYaw());
							}
						}
						List<String> signLocations = new ArrayList<String>();
						for(Location location : rent.getSignLocations()) {
							signLocations.add(plugin.getLanguageManager().getLang("info-regionSignLocation", location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ()));
						}
						if(!signLocations.isEmpty()) {
							plugin.messageNoPrefix(sender, "info-regionSigns", Utils.createCommaSeparatedList(signLocations));
						}
						if(sender.hasPermission("areashop.groupinfo") && !rent.getGroupNames().isEmpty()) {
							plugin.messageNoPrefix(sender, "info-regionGroups", Utils.createCommaSeparatedList(rent.getGroupNames()));
						}
						if(rent.isRestoreEnabled()) {
							if(sender.hasPermission("areashop.setrestore")) {
								plugin.messageNoPrefix(sender, "info-regionRestoringRent", rent, plugin.getLanguageManager().getLang("info-regionRestoringProfile", rent.getRestoreProfile()));
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
									plugin.messageNoPrefix(sender, "info-regionNoTeleport", buy, plugin.getLanguageManager().getLang("info-regionTeleportHint", buy));
								} else {
									plugin.messageNoPrefix(sender, "info-regionNoTeleport", buy, "");
								}
							} else {
								plugin.messageNoPrefix(sender, "info-regionTeleportAt", buy, teleport.getWorld().getName(), teleport.getBlockX(), teleport.getBlockY(), teleport.getBlockZ(), (int)teleport.getPitch(), (int)teleport.getYaw());
							}
						}
						List<String> signLocations = new ArrayList<String>();
						for(Location location : buy.getSignLocations()) {
							signLocations.add(plugin.getLanguageManager().getLang("info-regionSignLocation", location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ()));
						}
						if(!signLocations.isEmpty()) {
							plugin.messageNoPrefix(sender, "info-regionSigns", Utils.createCommaSeparatedList(signLocations));
						}
						if(sender.hasPermission("areashop.groupinfo") && !buy.getGroupNames().isEmpty()) {
							plugin.messageNoPrefix(sender, "info-regionGroups", Utils.createCommaSeparatedList(buy.getGroupNames()));
						}
						if(buy.isRestoreEnabled()) {
							if(sender.hasPermission("areashop.setrestore")) {
								plugin.messageNoPrefix(sender, "info-regionRestoringBuy", buy, plugin.getLanguageManager().getLang("info-regionRestoringProfile", buy.getRestoreProfile()));
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
				
			/* List of regions without a group */
			else if(args[1].equalsIgnoreCase("nogroup")) {
				String message = "";
				/* Message for rents */
				List<String> rents = plugin.getFileManager().getRentNames();
				// Remove regions that have a group
				for(RegionGroup group : plugin.getFileManager().getGroups()) {
					rents.removeAll(group.getMembers());
				}
				// Create the list message
				Iterator<String> itRent = rents.iterator();
				if(itRent.hasNext()) {
					message = itRent.next();
					while(itRent.hasNext()) {
						message += ", " + itRent.next();
					}
				}
				if(message.equals("")) {
					plugin.message(sender, "info-nogroupNoRents");
				} else {
					plugin.message(sender, "info-nogroupRents", message);
				}
				
				/* Message for buys */
				message = "";
				// Remove regions that have a group
				List<String> buys = plugin.getFileManager().getBuyNames();
				for(RegionGroup group : plugin.getFileManager().getGroups()) {
					buys.removeAll(group.getMembers());
				}
				// Create the list message
				Iterator<String> itBuy = buys.iterator();
				if(itBuy.hasNext()) {
					message = itBuy.next();
					while(itBuy.hasNext()) {
						message += ", " + itBuy.next();
					}
				}
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
		List<String> result = new ArrayList<String>();
		if(toComplete == 2) {
			result.addAll(Arrays.asList("all", "rented", "forrent", "sold", "forsale", "player", "region", "nogroup"));
		} else if(toComplete == 3) {
			if(start[2].equalsIgnoreCase("player")) {
				for(Player player : Bukkit.getOnlinePlayers()) {
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


























