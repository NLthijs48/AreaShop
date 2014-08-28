package nl.evolutioncoding.AreaShop;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import nl.evolutioncoding.AreaShop.commands.AddCommand;
import nl.evolutioncoding.AreaShop.commands.AddsignCommand;
import nl.evolutioncoding.AreaShop.commands.BuyCommand;
import nl.evolutioncoding.AreaShop.commands.CommandAreaShop;
import nl.evolutioncoding.AreaShop.commands.DelCommand;
import nl.evolutioncoding.AreaShop.commands.DelsignCommand;
import nl.evolutioncoding.AreaShop.commands.FindCommand;
import nl.evolutioncoding.AreaShop.commands.GroupaddCommand;
import nl.evolutioncoding.AreaShop.commands.GroupdelCommand;
import nl.evolutioncoding.AreaShop.commands.GroupinfoCommand;
import nl.evolutioncoding.AreaShop.commands.GrouplistCommand;
import nl.evolutioncoding.AreaShop.commands.HelpCommand;
import nl.evolutioncoding.AreaShop.commands.InfoCommand;
import nl.evolutioncoding.AreaShop.commands.ReloadCommand;
import nl.evolutioncoding.AreaShop.commands.RentCommand;
import nl.evolutioncoding.AreaShop.commands.RentdurationCommand;
import nl.evolutioncoding.AreaShop.commands.SchematiceventCommand;
import nl.evolutioncoding.AreaShop.commands.SellCommand;
import nl.evolutioncoding.AreaShop.commands.SetpriceCommand;
import nl.evolutioncoding.AreaShop.commands.SetrestoreCommand;
import nl.evolutioncoding.AreaShop.commands.SetteleportCommand;
import nl.evolutioncoding.AreaShop.commands.TeleportCommand;
import nl.evolutioncoding.AreaShop.commands.UnrentCommand;
import nl.evolutioncoding.AreaShop.commands.UpdatebuysCommand;
import nl.evolutioncoding.AreaShop.commands.UpdaterentsCommand;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class CommandManager implements CommandExecutor, TabCompleter {
	AreaShop plugin;
	ArrayList<CommandAreaShop> commands;
	
	/**
	 * Constructor
	 * @param plugin The AreaShop plugin
	 */
	public CommandManager(AreaShop plugin) {
		this.plugin = plugin;
		commands = new ArrayList<CommandAreaShop>();
		commands.add(new HelpCommand(plugin));
		commands.add(new RentCommand(plugin));
		commands.add(new UnrentCommand(plugin));
		commands.add(new BuyCommand(plugin));
		commands.add(new SellCommand(plugin));
		commands.add(new InfoCommand(plugin));
		commands.add(new TeleportCommand(plugin));
		commands.add(new SetteleportCommand(plugin));
		commands.add(new FindCommand(plugin));
		commands.add(new UpdaterentsCommand(plugin));
		commands.add(new UpdatebuysCommand(plugin));
		commands.add(new SetrestoreCommand(plugin));
		commands.add(new SetpriceCommand(plugin));
		commands.add(new RentdurationCommand(plugin));
		commands.add(new ReloadCommand(plugin));
		commands.add(new GroupaddCommand(plugin));
		commands.add(new GroupdelCommand(plugin));
		commands.add(new GrouplistCommand(plugin));
		commands.add(new GroupinfoCommand(plugin));
		commands.add(new SchematiceventCommand(plugin));
		commands.add(new AddCommand(plugin));
		commands.add(new DelCommand(plugin));
		commands.add(new AddsignCommand(plugin));
		commands.add(new DelsignCommand(plugin));
		
		/* Register commands in bukkit */
		plugin.getCommand("AreaShop").setExecutor(this);	
		plugin.getCommand("AreaShop").setTabCompleter(this);
	}	
	
	/**
	 * Get the list with AreaShop commands
	 * @return The list with AreaShop commands
	 */
	public ArrayList<CommandAreaShop> getCommands() {
		return commands;
	}
	
	/**
	 * Shows the help page for the CommandSender
	 * @param target The CommandSender to show the help to
	 */
	public void showHelp(CommandSender target) {
		/* Add all messages to a list */
		ArrayList<String> messages = new ArrayList<String>();
		messages.add(plugin.config().getString("chatPrefix") + plugin.getLanguageManager().getLang("help-header"));
		messages.add(plugin.config().getString("chatPrefix") + plugin.getLanguageManager().getLang("help-alias"));
		for(CommandAreaShop command : commands) {
			String help = command.getHelp(target);
			if(help != null && help.length() != 0) {
				messages.add(help);
			}
		}
		
		/* Send the messages to the target */
		for(String message : messages) {
			if(!plugin.config().getBoolean("useColorsInConsole") && !(target instanceof Player)) {
				target.sendMessage(ChatColor.stripColor(plugin.fixColors(message)));
			} else {
				target.sendMessage(plugin.fixColors(message));
			}
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		boolean executed = false;		
		for(int i=0; i<commands.size() && !executed; i++) {
			if(commands.get(i).canExecute(command, args)) {
				commands.get(i).execute(sender, command, args);
				executed = true;
			}
		}
		if(!executed && args.length == 0) {
			this.showHelp(sender);
		} else if(!executed && args.length > 0) {
			plugin.message(sender, "cmd-notValid");
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> result = new ArrayList<String>();
		if(!sender.hasPermission("areashop.tabcomplete")) {
			return result;
		}
		int toCompleteNumber = args.length;
		String toCompletePrefix = args[args.length-1].toLowerCase();
		AreaShop.debug("toCompleteNumber=" + toCompleteNumber + ", toCompletePrefix=" + toCompletePrefix + ", length=" + toCompletePrefix.length());
		if(toCompleteNumber == 1) {
			for(CommandAreaShop c : commands) {
				String begin = c.getCommandStart();
				result.add(begin.substring(begin.indexOf(' ') +1));
			}
		} else {
			String[] start = new String[args.length];
			start[0] = command.getName();
			for(int i=1; i<args.length; i++) {
				start[i] = args[i-1];
			}
			for(CommandAreaShop c : commands) {
				if(c.canExecute(command, args)) {
					result = c.getTabCompleteList(toCompleteNumber, start);
				}
			}
		}
		// Filter and sort the results
		if(result.size() > 0) {
			SortedSet<String> set = new TreeSet<String>();
			for(String suggestion : result) {
				if(suggestion.toLowerCase().startsWith(toCompletePrefix)) {
					set.add(suggestion);
				}
			}	
			result.clear();
			result.addAll(set);
		}
		AreaShop.debug("Tabcomplete #" + toCompleteNumber + ", prefix="+ toCompletePrefix + ", result=" + result.toString());
		return result;
	}
}

















