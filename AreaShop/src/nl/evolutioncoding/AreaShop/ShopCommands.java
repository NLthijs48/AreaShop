package nl.evolutioncoding.AreaShop;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommands implements CommandExecutor {
	AreaShop plugin;
	
	/**
	 * Constructor
	 * @param plugin The AreaShop plugin
	 */
	public ShopCommands(AreaShop plugin) {
		this.plugin = plugin;
	}	
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if(command.getName().equalsIgnoreCase("AreaShop")) {
			ShopManager shopManager = plugin.getShopManager();
			
			/* Commands with 1 argument or more */
			if(args.length > 0 && args[0] != null) {
				/* Help command */
				if(args[0].equalsIgnoreCase("help")) {
					plugin.showHelp(sender);
				}
				
				/* Renting command */
				else if(args[0].equalsIgnoreCase("rent")) {
					if (!(sender instanceof Player)) {
						plugin.message(sender, "onlyByPlayer");
						return true;
					}					
					Player player = (Player)sender;
					if(args.length > 1 && args[1] != null) {
						shopManager.rent(player, args[1]);
					} else {
						plugin.message(sender, "rent-help");
					}					
				} 
				
				/* Buying command */
				else if(args[0].equalsIgnoreCase("buy")) {
					if (!(sender instanceof Player)) {
						plugin.message(sender, "onlyByPlayer");
						return true;
					}
					Player player = (Player)sender;
					if(args.length > 1 && args[1] != null) {
						shopManager.buy(player, args[1]);
					} else {
						plugin.message(player, "buy-help");
					}					
				} 
				
				/* Info command */
				else if(args[0].equalsIgnoreCase("info")) {
					if(args.length > 1 && args[1] != null) {
						if(args[1].equalsIgnoreCase("all")) {
							String message = "";
							/* Message for rents */
							Iterator<String> it = shopManager.getRents().keySet().iterator();
							if(it.hasNext()) {
								message = shopManager.getRent(it.next()).get(plugin.keyName);
								while(it.hasNext()) {
									message += ", " + shopManager.getRent(it.next()).get(plugin.keyName);
								}
							}
							if(message.equals("")) {
								plugin.message(sender, "info-all-noRents");
							} else {
								plugin.message(sender, "info-all-rents", message);
							}
							
							/* Message for buys */
							it = shopManager.getBuys().keySet().iterator();
							if(it.hasNext()) {
								message = shopManager.getBuy(it.next()).get(plugin.keyName);
								while(it.hasNext()) {
									message += ", " + shopManager.getBuy(it.next()).get(plugin.keyName);
								}
							}
							if(message.equals("")) {
								plugin.message(sender, "info-all-noBuys");
							} else {
								plugin.message(sender, "info-all-buys", message);
							}
						}
						else if(args[1].equalsIgnoreCase("rented")) {
							String message = "";
							Iterator<String> it = shopManager.getRents().keySet().iterator();
							boolean first = true;
							while(it.hasNext()) {
								String next = shopManager.getRent(it.next()).get(plugin.keyName);
								if(shopManager.getRent(next).get(plugin.keyPlayer) != null) {
									if(!first) {
										message += ", " + next;
									} else {
										first = false;
										message = next;
									}
								}
							}
							if(message.equals("")) {
								plugin.message(sender, "info-noRented");
							} else {
								plugin.message(sender, "info-rented", message);
							}								
						} else if(args[1].equalsIgnoreCase("forrent")) {
							String message = "";
							Iterator<String> it = shopManager.getRents().keySet().iterator();
							boolean first = true;
							while(it.hasNext()) {
								String next = shopManager.getRent(it.next()).get(plugin.keyName);
								if(shopManager.getRent(next).get(plugin.keyPlayer) == null) {
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
							Iterator<String> it = shopManager.getBuys().keySet().iterator();
							boolean first = true;
							while(it.hasNext()) {
								String next = shopManager.getBuy(it.next()).get(plugin.keyName);
								if(shopManager.getBuy(next).get(plugin.keyPlayer) != null) {
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
							Iterator<String> it = shopManager.getBuys().keySet().iterator();
							boolean first = true;
							while(it.hasNext()) {
								String next = shopManager.getBuy(it.next()).get(plugin.keyName);
								if(shopManager.getBuy(next).get(plugin.keyPlayer) == null) {
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
								Iterator<String> it = shopManager.getRents().keySet().iterator();
								boolean first = true;
								while(it.hasNext()) {
									String next = shopManager.getRent(it.next()).get(plugin.keyName);
									if(shopManager.getRent(next).get(plugin.keyPlayer) != null && shopManager.getRent(next).get(plugin.keyPlayer).equalsIgnoreCase(args[2])) {
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
								it = shopManager.getBuys().keySet().iterator();
								first = true;
								while(it.hasNext()) {
									String next = shopManager.getBuy(it.next()).get(plugin.keyName);
									if(shopManager.getBuy(next).get(plugin.keyPlayer) != null && shopManager.getBuy(next).get(plugin.keyPlayer).equalsIgnoreCase(args[2])) {
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
								
								HashMap<String,String> rent = shopManager.getRent(args[2]);
								HashMap<String,String> buy = shopManager.getBuy(args[2]);
								
								if(rent == null) {
									plugin.message(sender, "info-regionRenting", args[2]);
									plugin.message(sender, "info-regionNoRenting", args[2]);
								} else {
									plugin.message(sender, "info-regionRenting", rent.get(plugin.keyName));
									plugin.message(sender, "info-regionSign", rent.get(plugin.keyWorld), rent.get(plugin.keyX), rent.get(plugin.keyY), rent.get(plugin.keyZ));
									plugin.message(sender, "info-regionPriceDuration", plugin.getCurrencyCharacter() + rent.get(plugin.keyPrice), rent.get(plugin.keyDuration));
									if(rent.get(plugin.keyPlayer) == null) {
										plugin.message(sender, "info-regionNotRented");
									} else {
										SimpleDateFormat dateFull = new SimpleDateFormat("dd MMMMMMMMMMMMMMMMM yyyy HH:mm");
										plugin.message(sender, "info-regionRentedBy", rent.get(plugin.keyPlayer), dateFull.format(Long.parseLong(rent.get(plugin.keyRentedUntil))));
									}
								}
								
								if(buy == null) {
									plugin.message(sender, "info-regionBuying", args[2]);
									plugin.message(sender, "info-regionNoBuying", args[2]);
								} else {
									plugin.message(sender, "info-regionBuying", buy.get(plugin.keyName));
									plugin.message(sender, "info-regionSign", buy.get(plugin.keyWorld), buy.get(plugin.keyX), buy.get(plugin.keyY), buy.get(plugin.keyZ));
									plugin.message(sender, "info-regionPrice", plugin.getCurrencyCharacter() + buy.get(plugin.keyPrice));
									if(buy.get(plugin.keyPlayer) == null) {
										plugin.message(sender, "info-regionNotBought");
									} else {
										plugin.message(sender, "info-regionBoughtBy", buy.get(plugin.keyPlayer));
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
				
				/* Unrenting command */
				else if(args[0].equalsIgnoreCase("unrent")) {
					if(args.length > 1 && args[1] != null) {
						HashMap<String,String> rent = shopManager.getRent(args[1]);
						if(rent == null) {
							plugin.message(sender, "unrent-notRegistered");
						} else {
							if(rent.get(plugin.keyPlayer) == null) {
								plugin.message(sender, "unrent-notRented");
							} else {
								if(sender.hasPermission("areashop.unrent")) {
									plugin.message(sender, "unrent-other", rent.get(plugin.keyPlayer));
									shopManager.unRent(args[1]);
								} else {
									if(sender.hasPermission("areashop.unrentown")) {
										if(rent.get(plugin.keyPlayer).equals(sender.getName())) {
											plugin.message(sender, "unrent-unrented");
											shopManager.unRent(args[1]);
										} else {
											plugin.message(sender, "unrent-noPermissionOther");
										}
									} else {
										plugin.message(sender, "unrent-noPermission");
									}
								}
							}
						}
					} else {
						plugin.message(sender, "unrent-help");
					}					
				} 
				
				/* Selling command */
				else if(args[0].equalsIgnoreCase("sell")) {
					if(args.length > 1 && args[1] != null) {
						HashMap<String,String> buy = shopManager.getBuy(args[1]);
						if(buy == null) {
							plugin.message(sender, "sell-notRegistered");
						} else {
							if(buy.get(plugin.keyPlayer) == null) {
								plugin.message(sender, "sell-notBought");
							} else {
								if(sender.hasPermission("areashop.sell")) {
									plugin.message(sender, "sell-sold", buy.get(plugin.keyPlayer));
									shopManager.unBuy(args[1]);
								} else {
									if(sender.hasPermission("areashop.sellown")) {
										if(buy.get(plugin.keyPlayer).equals(sender.getName())) {
											plugin.message(sender, "sell-soldYours");
											shopManager.unBuy(args[1]);
										} else {
											plugin.message(sender, "sell-noPermissionOther");
										}
									} else {
										plugin.message(sender, "sell-noPermission");
									}									
								}
							}
						}
					} else {
						plugin.message(sender, "sell-help");
					}					
				} 
				
				/* UpdateRentSigns command */
				else if(args[0].equalsIgnoreCase("updaterentsigns")) {
					
					if(sender.hasPermission("areashop.updaterentsigns")) {
						boolean result = plugin.getShopManager().updateRentSigns();
						if(result) {
							plugin.message(sender, "rentsigns-updated");
						} else {
							plugin.message(sender, "rentsigns-notUpdated");
						}

					} else {
						plugin.message(sender, "rentsigns-noPermission");
					}
				}
				
				/* UpdateBuySigns command */
				else if(args[0].equalsIgnoreCase("updatebuysigns")) {
					
					if(sender.hasPermission("areashop.updatebuysigns")) {
						boolean result = plugin.getShopManager().updateBuySigns();
						if(result) {
							plugin.message(sender, "buysigns-updated");
						} else {
							plugin.message(sender, "buysigns-notUpdated");
						}							
					} else {
						plugin.message(sender, "buysigns-noPermission");
					}
				}
				
				/* updaterentregions command */
				else if(args[0].equalsIgnoreCase("updaterentregions")) {
					
					if(sender.hasPermission("areashop.updaterentregions")) {
						plugin.getShopManager().updateRentRegions();
						plugin.message(sender, "rentregions-updated");				
					} else {
						plugin.message(sender, "rentregions-noPermission");
					}
				}
				
				/* updatebuyregions command */
				else if(args[0].equalsIgnoreCase("updatebuyregions")) {
					
					if(sender.hasPermission("areashop.updatebuyregions")) {
						plugin.getShopManager().updateBuyRegions();
						plugin.message(sender, "buyregions-updated");					
					} else {
						plugin.message(sender, "buyregions-noPermission");
					}
				}
				
				/* reload command */
				else if(args[0].equalsIgnoreCase("reload")) {
					
					if(sender.hasPermission("areashop.reload")) {
						plugin.reload();	
						plugin.message(sender, "reload-reloaded");
					} else {
						plugin.message(sender, "reload-noPermission");
					}
				}
				
				/* Not a valid command */
				else {
					plugin.message(sender, "cmd-notValid");
				}
				
			} else {
				plugin.showHelp(sender);
			}		
			return true;
		}
		return false;
	}

}
