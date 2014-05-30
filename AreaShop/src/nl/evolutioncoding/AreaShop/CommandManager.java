package nl.evolutioncoding.AreaShop;

import java.util.ArrayList;

import nl.evolutioncoding.AreaShop.commands.BuyCommand;
import nl.evolutioncoding.AreaShop.commands.BuypriceCommand;
import nl.evolutioncoding.AreaShop.commands.BuyrestoreCommand;
import nl.evolutioncoding.AreaShop.commands.CommandAreaShop;
import nl.evolutioncoding.AreaShop.commands.HelpCommand;
import nl.evolutioncoding.AreaShop.commands.InfoCommand;
import nl.evolutioncoding.AreaShop.commands.ReloadCommand;
import nl.evolutioncoding.AreaShop.commands.RentCommand;
import nl.evolutioncoding.AreaShop.commands.RentdurationCommand;
import nl.evolutioncoding.AreaShop.commands.RentpriceCommand;
import nl.evolutioncoding.AreaShop.commands.RentrestoreCommand;
import nl.evolutioncoding.AreaShop.commands.SellCommand;
import nl.evolutioncoding.AreaShop.commands.SetteleportCommand;
import nl.evolutioncoding.AreaShop.commands.TeleportCommand;
import nl.evolutioncoding.AreaShop.commands.UnrentCommand;
import nl.evolutioncoding.AreaShop.commands.UpdatebuysCommand;
import nl.evolutioncoding.AreaShop.commands.UpdaterentsCommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandManager implements CommandExecutor {
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
		commands.add(new UpdaterentsCommand(plugin));
		commands.add(new UpdatebuysCommand(plugin));
		commands.add(new RentrestoreCommand(plugin));
		commands.add(new BuyrestoreCommand(plugin));
		commands.add(new RentpriceCommand(plugin));
		commands.add(new BuypriceCommand(plugin));
		commands.add(new RentdurationCommand(plugin));
		commands.add(new ReloadCommand(plugin));
		
		/* Register commands in bukkit */
		plugin.getCommand("AreaShop").setExecutor(this);	
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
			target.sendMessage(plugin.fixColors(message));
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
}

















