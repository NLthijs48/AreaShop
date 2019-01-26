package me.wiefferink.areashop.managers;

import me.wiefferink.areashop.commands.AddCommand;
import me.wiefferink.areashop.commands.AddfriendCommand;
import me.wiefferink.areashop.commands.AddsignCommand;
import me.wiefferink.areashop.commands.BuyCommand;
import me.wiefferink.areashop.commands.CommandAreaShop;
import me.wiefferink.areashop.commands.DelCommand;
import me.wiefferink.areashop.commands.DelfriendCommand;
import me.wiefferink.areashop.commands.DelsignCommand;
import me.wiefferink.areashop.commands.FindCommand;
import me.wiefferink.areashop.commands.GroupaddCommand;
import me.wiefferink.areashop.commands.GroupdelCommand;
import me.wiefferink.areashop.commands.GroupinfoCommand;
import me.wiefferink.areashop.commands.GrouplistCommand;
import me.wiefferink.areashop.commands.HelpCommand;
import me.wiefferink.areashop.commands.ImportCommand;
import me.wiefferink.areashop.commands.InfoCommand;
import me.wiefferink.areashop.commands.LinksignsCommand;
import me.wiefferink.areashop.commands.MeCommand;
import me.wiefferink.areashop.commands.MessageCommand;
import me.wiefferink.areashop.commands.ReloadCommand;
import me.wiefferink.areashop.commands.RentCommand;
import me.wiefferink.areashop.commands.ResellCommand;
import me.wiefferink.areashop.commands.SchematiceventCommand;
import me.wiefferink.areashop.commands.SellCommand;
import me.wiefferink.areashop.commands.SetdurationCommand;
import me.wiefferink.areashop.commands.SetlandlordCommand;
import me.wiefferink.areashop.commands.SetownerCommand;
import me.wiefferink.areashop.commands.SetpriceCommand;
import me.wiefferink.areashop.commands.SetrestoreCommand;
import me.wiefferink.areashop.commands.SetteleportCommand;
import me.wiefferink.areashop.commands.StackCommand;
import me.wiefferink.areashop.commands.StopresellCommand;
import me.wiefferink.areashop.commands.TeleportCommand;
import me.wiefferink.areashop.commands.UnrentCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class CommandManager extends Manager implements CommandExecutor, TabCompleter {
	private final ArrayList<CommandAreaShop> commands;

	/**
	 * Constructor.
	 */
	public CommandManager() {
		commands = new ArrayList<>();
		commands.add(new HelpCommand());
		commands.add(new RentCommand());
		commands.add(new UnrentCommand());
		commands.add(new BuyCommand());
		commands.add(new SellCommand());
		commands.add(new MeCommand());
		commands.add(new InfoCommand());
		commands.add(new TeleportCommand());
		commands.add(new SetteleportCommand());
		commands.add(new AddfriendCommand());
		commands.add(new DelfriendCommand());
		commands.add(new FindCommand());
		commands.add(new ResellCommand());
		commands.add(new StopresellCommand());
		commands.add(new SetrestoreCommand());
		commands.add(new SetpriceCommand());
		commands.add(new SetownerCommand());
		commands.add(new SetdurationCommand());
		commands.add(new ReloadCommand());
		commands.add(new GroupaddCommand());
		commands.add(new GroupdelCommand());
		commands.add(new GrouplistCommand());
		commands.add(new GroupinfoCommand());
		commands.add(new SchematiceventCommand());
		commands.add(new AddCommand());
		commands.add(new DelCommand());
		commands.add(new AddsignCommand());
		commands.add(new DelsignCommand());
		commands.add(new LinksignsCommand());
		commands.add(new StackCommand());
		commands.add(new SetlandlordCommand());
		commands.add(new MessageCommand());
		commands.add(new ImportCommand());

		// Register commands in bukkit
		plugin.getCommand("AreaShop").setExecutor(this);
		plugin.getCommand("AreaShop").setTabCompleter(this);
	}

	/**
	 * Get the list with AreaShop commands.
	 * @return The list with AreaShop commands
	 */
	public List<CommandAreaShop> getCommands() {
		return commands;
	}

	/**
	 * Shows the help page for the CommandSender.
	 * @param target The CommandSender to show the help to
	 */
	public void showHelp(CommandSender target) {
		if(!target.hasPermission("areashop.help")) {
			plugin.message(target, "help-noPermission");
			return;
		}
		// Add all messages to a list
		ArrayList<String> messages = new ArrayList<>();
		plugin.message(target, "help-header");
		plugin.message(target, "help-alias");
		for(CommandAreaShop command : commands) {
			String help = command.getHelp(target);
			if(help != null && !help.isEmpty()) {
				messages.add(help);
			}
		}
		// Send the messages to the target
		for(String message : messages) {
			plugin.messageNoPrefix(target, message);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if(!plugin.isReady()) {
			plugin.message(sender, "general-notReady");
			return true;
		}

		// Redirect `/as info player <player>` to `/as me <player>`
		if(args.length == 3 && "info".equals(args[0]) && "player".equals(args[1])) {
			args[0] = "me";
			args[1] = args[2];
		}

		// Execute command
		boolean executed = false;
		for(int i = 0; i < commands.size() && !executed; i++) {
			if(commands.get(i).canExecute(command, args)) {
				commands.get(i).execute(sender, args);
				executed = true;
			}
		}

		// Show help
		if (!executed) {
			if (args.length == 0) {
				this.showHelp(sender);
			} else {
				// Indicate that the '/as updaterents' and '/as updatebuys' commands are removed
				if("updaterents".equalsIgnoreCase(args[0]) || "updatebuys".equalsIgnoreCase(args[0])) {
					plugin.message(sender, "reload-updateCommandChanged");
				} else {
					plugin.message(sender, "cmd-notValid");
				}
			}
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> result = new ArrayList<>();
		if(!sender.hasPermission("areashop.tabcomplete")) {
			return result;
		}
		int toCompleteNumber = args.length;
		String toCompletePrefix = args[args.length - 1].toLowerCase();
		//AreaShop.debug("toCompleteNumber=" + toCompleteNumber + ", toCompletePrefix=" + toCompletePrefix + ", length=" + toCompletePrefix.length());
		if(toCompleteNumber == 1) {
			for(CommandAreaShop c : commands) {
				String begin = c.getCommandStart();
				result.add(begin.substring(begin.indexOf(' ') + 1));
			}
		} else {
			String[] start = new String[args.length];
			start[0] = command.getName();
			System.arraycopy(args, 0, start, 1, args.length - 1);
			for(CommandAreaShop c : commands) {
				if(c.canExecute(command, args)) {
					result = c.getTabCompleteList(toCompleteNumber, start, sender);
				}
			}
		}
		// Filter and sort the results
		if(!result.isEmpty()) {
			SortedSet<String> set = new TreeSet<>();
			for(String suggestion : result) {
				if(suggestion.toLowerCase().startsWith(toCompletePrefix)) {
					set.add(suggestion);
				}
			}
			result.clear();
			result.addAll(set);
		}
		//AreaShop.debug("Tabcomplete #" + toCompleteNumber + ", prefix="+ toCompletePrefix + ", result=" + result.toString());
		return result;
	}
}

















