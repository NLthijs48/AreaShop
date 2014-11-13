package nl.evolutioncoding.areashop.commands;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import nl.evolutioncoding.areashop.AreaShop;
import nl.evolutioncoding.areashop.Utils;
import nl.evolutioncoding.areashop.regions.BuyRegion;
import nl.evolutioncoding.areashop.regions.GeneralRegion;
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
						if(sender instanceof Player) {
							// get the region by location
							List<GeneralRegion> regions = plugin.getFileManager().getApplicalbeASRegions(((Player)sender).getLocation());
							if(regions.size() != 1) {
								plugin.message(sender, "info-regionHelp");
								return;
							} else {
								if(regions.get(0).isRentRegion()) {
									rent = (RentRegion)regions.get(0);
								} else if(regions.get(0).isBuyRegion()) {
									buy = (BuyRegion)regions.get(0);
								}
							}				
						} else {
							plugin.message(sender, "info-regionHelp");
							return;
						}			
					}
					
					if(rent == null && buy == null) {
						plugin.message(sender, "info-regionNotExisting", args[2]);
						return;
					}
					if(rent != null) {
						plugin.message(sender, "info-regionRenting", rent.getName());
						List<String> signLocations = new ArrayList<String>();
						for(Location location : rent.getSignLocations()) {
							signLocations.add(plugin.getLanguageManager().getLang("info-regionSignLocation", location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ()));
						}
						if(signLocations.isEmpty()) {
							plugin.message(sender, "info-regionNoSign");
						} else {
							plugin.message(sender, "info-regionSign", Utils.createCommaSeparatedList(signLocations));
						}
						plugin.message(sender, "info-regionPriceDuration", rent.getFormattedPrice(), rent.getDurationString());
						if(!rent.isRented()) {
							plugin.message(sender, "info-regionNotRented");
						} else {
							SimpleDateFormat dateFull = new SimpleDateFormat(plugin.getConfig().getString("timeFormatChat"));
							plugin.message(sender, "info-regionRentedBy", rent.getPlayerName(), dateFull.format(rent.getRentedUntil()));
						}
						if(sender.hasPermission("areashop.rentrestore")) {
							plugin.message(sender, "info-regionRestore", rent.isRestoreEnabled());
							plugin.message(sender, "info-regionRestoreProfile", rent.getRestoreProfile());
						}
						if(sender.hasPermission("areashop.teleport")) {
							if(rent.getTeleportLocation() == null) {
								plugin.message(sender, "info-regionNoTP");
							} else {
								plugin.message(sender, "info-regionTPLocation", rent.getTeleportLocation().getWorld().getName(), rent.getTeleportLocation().getBlockX(), rent.getTeleportLocation().getBlockY(), rent.getTeleportLocation().getBlockZ(), rent.getTeleportLocation().getPitch(), rent.getTeleportLocation().getYaw());
							}
						}
					}
					
					if(buy != null) {
						plugin.message(sender, "info-regionBuying", buy.getName());
						List<String> signLocations = new ArrayList<String>();
						for(Location location : buy.getSignLocations()) {
							signLocations.add(plugin.getLanguageManager().getLang("info-regionSignLocation", location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ()));
						}
						if(signLocations.isEmpty()) {
							plugin.message(sender, "info-regionNoSign");
						} else {
							plugin.message(sender, "info-regionSign", Utils.createCommaSeparatedList(signLocations));
						}
						plugin.message(sender, "info-regionPrice", buy.getFormattedPrice());
						if(!buy.isSold()) {
							plugin.message(sender, "info-regionNotBought");
						} else {
							plugin.message(sender, "info-regionBoughtBy", buy.getPlayerName());
						}
						if(sender.hasPermission("areashop.buyrestore")) {
							plugin.message(sender, "info-regionRestore", buy.isRestoreEnabled());
							plugin.message(sender, "info-regionRestoreProfile", buy.getRestoreProfile());
						}
						if(sender.hasPermission("areashop.teleport")) {
							if(buy.getTeleportLocation() == null) {
								plugin.message(sender, "info-regionNoTP");
							} else {
								plugin.message(sender, "info-regionTPLocation", buy.getTeleportLocation().getWorld().toString(), buy.getTeleportLocation().getBlockX(), buy.getTeleportLocation().getBlockY(), buy.getTeleportLocation().getBlockZ(), buy.getTeleportLocation().getPitch(), buy.getTeleportLocation().getYaw());
							}
						}
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
	public List<String> getTabCompleteList(int toComplete, String[] start) {
		List<String> result = new ArrayList<String>();
		if(toComplete == 2) {
			result.addAll(Arrays.asList("all", "rented", "forrent", "sold", "forsale", "player", "region"));
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


























