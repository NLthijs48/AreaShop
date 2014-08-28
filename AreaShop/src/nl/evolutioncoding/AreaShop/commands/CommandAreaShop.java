package nl.evolutioncoding.AreaShop.commands;

import java.util.List;

import nl.evolutioncoding.AreaShop.AreaShop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Abstract class for generalising command classes
 */
public abstract class CommandAreaShop {
	AreaShop plugin;
	
	/**
	 * Save the instance of the main plugin class
	 * @param plugin The main plugin
	 */
	public CommandAreaShop(AreaShop plugin) {
		this.plugin = plugin;
	}
	
	public boolean canExecute(Command command, String[] args) {
		String commandString = command.getName();
		for(String argument : args) {
			commandString += " " + argument;
		}
		if(commandString.length() > getCommandStart().length()) {
			return commandString.toLowerCase().startsWith(getCommandStart().toLowerCase() + " ");
		}
		return commandString.toLowerCase().startsWith(getCommandStart().toLowerCase());
	}
	
	/**
	 * Get a list of string to complete a command with (raw list, not matching ones not filtered out)
	 * @param toComplete The number of the argument that has to be completed
	 * @param start The already given start of the command
	 * @return A collection with all the possibilities for argument to complete
	 */
	public abstract List<String> getTabCompleteList(int toComplete, String[] start);
	
	/**
	 * Get the argument that comes after the base command that this command reacts to
	 * @return
	 */
	public abstract String getCommandStart();
	
	/**
	 * Returns the correct help string for the reciever
	 * @param target The CommandSender that the help message is for
	 * @return The help message according to the permissions of the reciever
	 */
	public abstract String getHelp(CommandSender target);
	
	/**
	 * Execute a (sub)command if the conditions are met
	 * @param sender The commandSender that executed the command
	 * @param commandAreaShop The command that has been executed
	 * @param args The arguments that are given
	 */
	public abstract void execute(CommandSender sender, Command command, String[] args);	
	
	
	
}
