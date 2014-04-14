package nl.evolutioncoding.AreaShop;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandManager implements CommandExecutor {
	AreaShop plugin;
	
	/**
	 * Constructor
	 * @param plugin The AreaShop plugin
	 */
	public CommandManager(AreaShop plugin) {
		this.plugin = plugin;
	}	
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if(command.getName().equalsIgnoreCase("AreaShop")) {
			FileManager fileManager = plugin.getFileManager();
			
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
						fileManager.rent(player, args[1]);
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
						fileManager.buy(player, args[1]);
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
							Iterator<String> it = fileManager.getRents().keySet().iterator();
							if(it.hasNext()) {
								message = fileManager.getRent(it.next()).get(plugin.keyName);
								while(it.hasNext()) {
									message += ", " + fileManager.getRent(it.next()).get(plugin.keyName);
								}
							}
							if(message.equals("")) {
								plugin.message(sender, "info-all-noRents");
							} else {
								plugin.message(sender, "info-all-rents", message);
							}
							
							/* Message for buys */
							it = fileManager.getBuys().keySet().iterator();
							if(it.hasNext()) {
								message = fileManager.getBuy(it.next()).get(plugin.keyName);
								while(it.hasNext()) {
									message += ", " + fileManager.getBuy(it.next()).get(plugin.keyName);
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
							Iterator<String> it = fileManager.getRents().keySet().iterator();
							boolean first = true;
							while(it.hasNext()) {
								String next = fileManager.getRent(it.next()).get(plugin.keyName);
								if(fileManager.getRent(next).get(plugin.keyPlayer) != null) {
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
							Iterator<String> it = fileManager.getRents().keySet().iterator();
							boolean first = true;
							while(it.hasNext()) {
								String next = fileManager.getRent(it.next()).get(plugin.keyName);
								if(fileManager.getRent(next).get(plugin.keyPlayer) == null) {
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
							Iterator<String> it = fileManager.getBuys().keySet().iterator();
							boolean first = true;
							while(it.hasNext()) {
								String next = fileManager.getBuy(it.next()).get(plugin.keyName);
								if(fileManager.getBuy(next).get(plugin.keyPlayer) != null) {
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
							Iterator<String> it = fileManager.getBuys().keySet().iterator();
							boolean first = true;
							while(it.hasNext()) {
								String next = fileManager.getBuy(it.next()).get(plugin.keyName);
								if(fileManager.getBuy(next).get(plugin.keyPlayer) == null) {
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
								Iterator<String> it = fileManager.getRents().keySet().iterator();
								boolean first = true;
								while(it.hasNext()) {
									String next = fileManager.getRent(it.next()).get(plugin.keyName);
									if(fileManager.getRent(next).get(plugin.keyPlayer) != null && fileManager.getRent(next).get(plugin.keyPlayer).equalsIgnoreCase(args[2])) {
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
								it = fileManager.getBuys().keySet().iterator();
								first = true;
								while(it.hasNext()) {
									String next = fileManager.getBuy(it.next()).get(plugin.keyName);
									if(fileManager.getBuy(next).get(plugin.keyPlayer) != null && fileManager.getBuy(next).get(plugin.keyPlayer).equalsIgnoreCase(args[2])) {
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
								
								HashMap<String,String> rent = fileManager.getRent(args[2]);
								HashMap<String,String> buy = fileManager.getBuy(args[2]);
								
								if(rent == null) {
									plugin.message(sender, "info-regionRenting", args[2]);
									plugin.message(sender, "info-regionNoRenting", args[2]);
								} else {
									plugin.message(sender, "info-regionRenting", rent.get(plugin.keyName));
									plugin.message(sender, "info-regionSign", rent.get(plugin.keyWorld), rent.get(plugin.keyX), rent.get(plugin.keyY), rent.get(plugin.keyZ));
									plugin.message(sender, "info-regionPriceDuration", plugin.formatCurrency(rent.get(plugin.keyPrice)), rent.get(plugin.keyDuration));
									if(rent.get(plugin.keyPlayer) == null) {
										plugin.message(sender, "info-regionNotRented");
									} else {
										SimpleDateFormat dateFull = new SimpleDateFormat("dd MMMMMMMMMMMMMMMMM yyyy HH:mm");
										plugin.message(sender, "info-regionRentedBy", rent.get(plugin.keyPlayer), dateFull.format(Long.parseLong(rent.get(plugin.keyRentedUntil))));
									}
									if(sender.hasPermission("areashop.rentrestore")) {
										plugin.message(sender, "info-regionRestore", rent.get(plugin.keyRestore));
										plugin.message(sender, "info-regionRestoreProfile", rent.get(plugin.keySchemProfile));
									}
								}
								
								if(buy == null) {
									plugin.message(sender, "info-regionBuying", args[2]);
									plugin.message(sender, "info-regionNoBuying", args[2]);
								} else {
									plugin.message(sender, "info-regionBuying", buy.get(plugin.keyName));
									plugin.message(sender, "info-regionSign", buy.get(plugin.keyWorld), buy.get(plugin.keyX), buy.get(plugin.keyY), buy.get(plugin.keyZ));
									plugin.message(sender, "info-regionPrice", plugin.formatCurrency(buy.get(plugin.keyPrice)));
									if(buy.get(plugin.keyPlayer) == null) {
										plugin.message(sender, "info-regionNotBought");
									} else {
										plugin.message(sender, "info-regionBoughtBy", buy.get(plugin.keyPlayer));
									}
									if(sender.hasPermission("areashop.buyrestore")) {
										plugin.message(sender, "info-regionRestore", buy.get(plugin.keyRestore));
										plugin.message(sender, "info-regionRestoreProfile", buy.get(plugin.keySchemProfile));
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
						HashMap<String,String> rent = fileManager.getRent(args[1]);
						if(rent == null) {
							plugin.message(sender, "unrent-notRegistered");
						} else {
							if(rent.get(plugin.keyPlayer) == null) {
								plugin.message(sender, "unrent-notRented");
							} else {
								if(sender.hasPermission("areashop.unrent")) {
									plugin.message(sender, "unrent-other", rent.get(plugin.keyPlayer));
									fileManager.unRent(args[1], true);
								} else {
									if(sender.hasPermission("areashop.unrentown")) {
										if(rent.get(plugin.keyPlayer).equals(sender.getName())) {
											plugin.message(sender, "unrent-unrented");
											fileManager.unRent(args[1], true);
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
						HashMap<String,String> buy = fileManager.getBuy(args[1]);
						if(buy == null) {
							plugin.message(sender, "sell-notRegistered");
						} else {
							if(buy.get(plugin.keyPlayer) == null) {
								plugin.message(sender, "sell-notBought");
							} else {
								if(sender.hasPermission("areashop.sell")) {
									plugin.message(sender, "sell-sold", buy.get(plugin.keyPlayer));
									fileManager.unBuy(args[1], true);
								} else {
									if(sender.hasPermission("areashop.sellown")) {
										if(buy.get(plugin.keyPlayer).equals(sender.getName())) {
											plugin.message(sender, "sell-soldYours");
											fileManager.unBuy(args[1], true);
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
				
				/* UpdateRents command */
				else if(args[0].equalsIgnoreCase("updaterents")) {
					
					if(sender.hasPermission("areashop.updaterents")) {
						boolean result = plugin.getFileManager().updateRentSigns();
						plugin.getFileManager().updateRentRegions();
						if(result) {
							plugin.message(sender, "rents-updated");
						} else {
							plugin.message(sender, "rents-notUpdated");
						}

					} else {
						plugin.message(sender, "rents-noPermission");
					}
				}
				
				/* UpdateBuys command */
				else if(args[0].equalsIgnoreCase("updatebuys")) {
					
					if(sender.hasPermission("areashop.updatebuys")) {
						boolean result = plugin.getFileManager().updateBuySigns();
						if(result) {
							plugin.message(sender, "buys-updated");
						} else {
							plugin.message(sender, "buys-notUpdated");
						}							
					} else {
						plugin.message(sender, "buys-noPermission");
					}
				}
				
				/* setRentRestore command*/
				else if(args[0].equalsIgnoreCase("rentrestore")) {
					if(sender.hasPermission("areashop.rentrestore")) {
						if(args.length > 2 && args[1] != null && args[2] != null) {
							HashMap<String,String> rent = fileManager.getRent(args[1]);
							if(rent == null) {
								plugin.message(sender, "rentrestore-notRegistered", args[1]);
							} else {
								String value = null;
								if(args[2].equalsIgnoreCase("true")) {
									rent.put(plugin.keyRestore, "true");
									value = "true";
								} else if(args[2].equalsIgnoreCase("false")) {
									rent.put(plugin.keyRestore, "false");
									value = "false";
								} else if(args[2].equalsIgnoreCase("general")) {
									rent.put(plugin.keyRestore, "general");
									value = "general";
								} else {
									plugin.message(sender, "rentrestore-invalidSetting", args[2]);
								}
								if(value != null) {
									if(args.length > 3) {
										rent.put(plugin.keySchemProfile, args[3]);
										plugin.message(sender, "rentrestore-successProfile", rent.get(plugin.keyName), value, args[3]);
									} else {
										plugin.message(sender, "rentrestore-success", rent.get(plugin.keyName), value);
									}
									fileManager.saveRents();
								}
							}
						} else {
							plugin.message(sender, "rentrestore-help");
						}			
					} else {
						plugin.message(sender, "rentrestore-noPermission");
					}
				}
				
				/* setBuyRestore command*/
				else if(args[0].equalsIgnoreCase("buyrestore")) {
					if(sender.hasPermission("areashop.buyrestore")) {
						if(args.length > 2 && args[1] != null && args[2] != null) {
							HashMap<String,String> buy = fileManager.getBuy(args[1]);
							if(buy == null) {
								plugin.message(sender, "buyrestore-notRegistered", args[1]);
							} else {
								String value = null;
								if(args[2].equalsIgnoreCase("true")) {
									buy.put(plugin.keyRestore, "true");
									value = "true";
								} else if(args[2].equalsIgnoreCase("false")) {
									buy.put(plugin.keyRestore, "false");
									value = "false";
								} else if(args[2].equalsIgnoreCase("general")) {
									buy.put(plugin.keyRestore, "general");
									value = "general";
								} else {
									plugin.message(sender, "buyrestore-invalidSetting", args[2]);
								}
								if(value != null) {
									if(args.length > 3) {
										buy.put(plugin.keySchemProfile, args[3]);
										plugin.message(sender, "buyrestore-successProfile", buy.get(plugin.keyName), value, args[3]);
									} else {
										plugin.message(sender, "buyrestore-success", buy.get(plugin.keyName), value);
									}
									fileManager.saveBuys();
								}
							}
						} else {
							plugin.message(sender, "buyrestore-help");
						}			
					} else {
						plugin.message(sender, "buyrestore-noPermission");
					}
				}
				
				/* rentprice command*/
				else if(args[0].equalsIgnoreCase("rentprice")) {
					if(!sender.hasPermission("areashop.rentprice")) {
						plugin.message(sender, "rentprice-noPermission");
						return true;
					}
					
					if(args.length < 3 || args[1] == null || args[2] == null) {
						plugin.message(sender, "rentprice-help");
						return true;
					}
					
					HashMap<String,String> rent = fileManager.getRent(args[1]);
					if(rent == null) {
						plugin.message(sender, "rentprice-notRegistered", args[1]);
						return true;
					} 
					
					try {
						Double.parseDouble(args[2]);
					} catch(NumberFormatException e) {
						plugin.message(sender, "rentprice-wrongPrice", args[2]);
						return true;
					}
					
					rent.put(plugin.keyPrice, args[2]);
					plugin.getFileManager().saveRents();
					plugin.getFileManager().updateRentSign(args[1]);
					plugin.getFileManager().updateRentRegion(args[1]);
					plugin.message(sender, "rentprice-success", rent.get(plugin.keyName), args[2], rent.get(plugin.keyDuration));
				}
				
				/* buyprice command*/
				else if(args[0].equalsIgnoreCase("buyprice")) {
					if(!sender.hasPermission("areashop.buyprice")) {
						plugin.message(sender, "buyprice-noPermission");
						return true;
					}
					
					if(args.length < 3 || args[1] == null || args[2] == null) {
						plugin.message(sender, "buyprice-help");
						return true;
					}
					
					HashMap<String,String> buy = fileManager.getBuy(args[1]);
					if(buy == null) {
						plugin.message(sender, "buyprice-notRegistered", args[1]);
						return true;
					} 

					try {
						Double.parseDouble(args[2]);
					} catch(NumberFormatException e) {
						plugin.message(sender, "buyprice-wrongPrice", args[2]);
						return true;
					}
					
					buy.put(plugin.keyPrice, args[2]);
					plugin.getFileManager().saveBuys();
					plugin.getFileManager().updateBuySign(args[1]);
					plugin.getFileManager().updateBuyRegion(args[1]);
					plugin.message(sender, "buyprice-success", buy.get(plugin.keyName), args[2]);
				}
				
				/* rentduration command*/
				else if(args[0].equalsIgnoreCase("rentduration")) {
					if(!sender.hasPermission("areashop.rentduration")) {
						plugin.message(sender, "rentduration-noPermission");
						return true;
					}
					
					if(args.length < 4 || args[1] == null || args[2] == null || args[3] == null) {
						plugin.message(sender, "rentduration-help");
						return true;
					}
					
					HashMap<String,String> rent = fileManager.getRent(args[1]);
					if(rent == null) {
						plugin.message(sender, "rentduration-notRegistered", args[1]);
						return true;
					} 

					try {
						Integer.parseInt(args[2]);
					} catch(NumberFormatException e) {
						plugin.message(sender, "rentduration-wrongAmount", args[2]);
						return true;
					}
					
					if(!plugin.checkTimeFormat(args[2] + " " + args[3])) {
						plugin.message(sender, "rentduration-wrongFormat", args[2]+" "+args[3]);
						return true;
					}					
					
					rent.put(plugin.keyDuration, args[2]+" "+args[3]);
					plugin.getFileManager().saveRents();
					plugin.getFileManager().updateRentSign(args[1]);
					plugin.getFileManager().updateRentRegion(args[1]);
					plugin.message(sender, "rentduration-success", rent.get(plugin.keyName), args[2]+" "+args[3]);
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
