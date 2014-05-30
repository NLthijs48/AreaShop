package nl.evolutioncoding.AreaShop.commands;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;

import nl.evolutioncoding.AreaShop.AreaShop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

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
				Iterator<String> it = plugin.getFileManager().getRents().keySet().iterator();
				if(it.hasNext()) {
					message = plugin.getFileManager().getRent(it.next()).get(AreaShop.keyName);
					while(it.hasNext()) {
						message += ", " + plugin.getFileManager().getRent(it.next()).get(AreaShop.keyName);
					}
				}
				if(message.equals("")) {
					plugin.message(sender, "info-all-noRents");
				} else {
					plugin.message(sender, "info-all-rents", message);
				}
				
				/* Message for buys */
				it = plugin.getFileManager().getBuys().keySet().iterator();
				if(it.hasNext()) {
					message = plugin.getFileManager().getBuy(it.next()).get(AreaShop.keyName);
					while(it.hasNext()) {
						message += ", " + plugin.getFileManager().getBuy(it.next()).get(AreaShop.keyName);
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
				Iterator<String> it = plugin.getFileManager().getRents().keySet().iterator();
				boolean first = true;
				while(it.hasNext()) {
					String next = plugin.getFileManager().getRent(it.next()).get(AreaShop.keyName);
					if(plugin.getFileManager().getRent(next).get(AreaShop.keyPlayerUUID) != null) {
						if(!first) {
							message += ", " + next;
						} else {
							first = false;
							message += next;
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
				Iterator<String> it = plugin.getFileManager().getRents().keySet().iterator();
				boolean first = true;
				while(it.hasNext()) {
					String next = plugin.getFileManager().getRent(it.next()).get(AreaShop.keyName);
					if(plugin.getFileManager().getRent(next).get(AreaShop.keyPlayerUUID) == null) {
						if(!first) {
							message += ", " + next;
						} else {
							first = false;
							message = next;
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
				Iterator<String> it = plugin.getFileManager().getBuys().keySet().iterator();
				boolean first = true;
				while(it.hasNext()) {
					String next = plugin.getFileManager().getBuy(it.next()).get(AreaShop.keyName);
					if(plugin.getFileManager().getBuy(next).get(AreaShop.keyPlayerUUID) != null) {
						if(!first) {
							message += ", ";
						} else {
							first = false;
						}
						message += next;
					}
				}
				if(message.equals("")) {
					plugin.message(sender, "info-noSold");
				} else {
					plugin.message(sender, "info-sold", message);
				}							
			} else if(args[1].equalsIgnoreCase("forsale")) {
				String message = "";
				Iterator<String> it = plugin.getFileManager().getBuys().keySet().iterator();
				boolean first = true;
				while(it.hasNext()) {
					String next = plugin.getFileManager().getBuy(it.next()).get(AreaShop.keyName);
					if(plugin.getFileManager().getBuy(next).get(AreaShop.keyPlayerUUID) == null) {
						if(!first) {
							message += ", " + next;
						} else {
							first = false;
							message = next;
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
					Iterator<String> it = plugin.getFileManager().getRents().keySet().iterator();
					boolean first = true;
					while(it.hasNext()) {
						String next = plugin.getFileManager().getRent(it.next()).get(AreaShop.keyName);
						if(plugin.getFileManager().getRent(next).get(AreaShop.keyPlayerUUID) != null && plugin.toName(plugin.getFileManager().getRent(next).get(AreaShop.keyPlayerUUID)).equalsIgnoreCase(args[2])) {
							if(!first) {
								message += ", " + next;
							} else {
								first = false;
								message = next;
							}
						}
					}
					if(message.equals("")) {
						plugin.message(sender, "info-playerNoRents", args[2]);
					} else {
						plugin.message(sender, "info-playerRents", args[2], message);
					}		
					
					message = "";
					it = plugin.getFileManager().getBuys().keySet().iterator();
					first = true;
					while(it.hasNext()) {
						String next = plugin.getFileManager().getBuy(it.next()).get(AreaShop.keyName);
						if(plugin.getFileManager().getBuy(next).get(AreaShop.keyPlayerUUID) != null && plugin.toName(plugin.getFileManager().getBuy(next).get(AreaShop.keyPlayerUUID)).equalsIgnoreCase(args[2])) {
							if(!first) {
								message += ", ";
							} else {
								first = false;
							}
							message += next;
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
				if(args.length > 2 && args[2] != null) {
					
					HashMap<String,String> rent = plugin.getFileManager().getRent(args[2]);
					HashMap<String,String> buy = plugin.getFileManager().getBuy(args[2]);
					
					if(rent == null) {
						plugin.message(sender, "info-regionRenting", args[2]);
						plugin.message(sender, "info-regionNoRenting", args[2]);
					} else {
						plugin.message(sender, "info-regionRenting", rent.get(AreaShop.keyName));
						plugin.message(sender, "info-regionSign", rent.get(AreaShop.keyWorld), rent.get(AreaShop.keyX), rent.get(AreaShop.keyY), rent.get(AreaShop.keyZ));
						plugin.message(sender, "info-regionPriceDuration", plugin.formatCurrency(rent.get(AreaShop.keyPrice)), rent.get(AreaShop.keyDuration));
						if(rent.get(AreaShop.keyPlayerUUID) == null) {
							plugin.message(sender, "info-regionNotRented");
						} else {
							SimpleDateFormat dateFull = new SimpleDateFormat("dd MMMMMMMMMMMMMMMMM yyyy HH:mm");
							plugin.message(sender, "info-regionRentedBy", plugin.toName(rent.get(AreaShop.keyPlayerUUID)), dateFull.format(Long.parseLong(rent.get(AreaShop.keyRentedUntil))));
						}
						if(sender.hasPermission("areashop.rentrestore")) {
							plugin.message(sender, "info-regionRestore", rent.get(AreaShop.keyRestore));
							plugin.message(sender, "info-regionRestoreProfile", rent.get(AreaShop.keySchemProfile));
						}
						if(sender.hasPermission("areashop.teleport")) {
							if(rent.get(AreaShop.keyTPX) == null) {
								plugin.message(sender, "info-regionNoTP");
							} else {
								plugin.message(sender, "info-regionTPLocation", rent.get(AreaShop.keyWorld), rent.get(AreaShop.keyTPX), rent.get(AreaShop.keyTPY), rent.get(AreaShop.keyTPZ), rent.get(AreaShop.keyTPPitch), rent.get(AreaShop.keyTPYaw));
							}
						}
					}
					
					if(buy == null) {
						plugin.message(sender, "info-regionBuying", args[2]);
						plugin.message(sender, "info-regionNoBuying", args[2]);
					} else {
						plugin.message(sender, "info-regionBuying", buy.get(AreaShop.keyName));
						plugin.message(sender, "info-regionSign", buy.get(AreaShop.keyWorld), buy.get(AreaShop.keyX), buy.get(AreaShop.keyY), buy.get(AreaShop.keyZ));
						plugin.message(sender, "info-regionPrice", plugin.formatCurrency(buy.get(AreaShop.keyPrice)));
						if(buy.get(AreaShop.keyPlayerUUID) == null) {
							plugin.message(sender, "info-regionNotBought");
						} else {
							plugin.message(sender, "info-regionBoughtBy", plugin.toName(buy.get(AreaShop.keyPlayerUUID)));
						}
						if(sender.hasPermission("areashop.buyrestore")) {
							plugin.message(sender, "info-regionRestore", buy.get(AreaShop.keyRestore));
							plugin.message(sender, "info-regionRestoreProfile", buy.get(AreaShop.keySchemProfile));
						}
						if(sender.hasPermission("areashop.teleport")) {
							if(buy.get(AreaShop.keyTPX) == null) {
								plugin.message(sender, "info-regionNoTP");
							} else {
								plugin.message(sender, "info-regionTPLocation", buy.get(AreaShop.keyWorld), buy.get(AreaShop.keyTPX), buy.get(AreaShop.keyTPY), buy.get(AreaShop.keyTPZ), buy.get(AreaShop.keyTPPitch), buy.get(AreaShop.keyTPYaw));
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
}
