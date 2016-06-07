package me.wiefferink.areashop.messages;

import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.regions.GeneralRegion;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {

	public static final String VARIABLESTART = "%";
	public static final String VARIABLEEND = "%";
	public static final String LANGUAGEVARIABLE = "lang:";
	public static final String CHATLANGUAGEVARIABLE = "prefix";
	public static final int REPLACEMENTLIMIT = 50;

	private List<String> message;
	private Object[] replacements;
	private String key = null;

	/**
	 * Internal use only
	 */
	private Message() {
		message = new ArrayList<>();
	}

	/**
	 * Empty message object
	 * @return this
	 */
	public static Message none() {
		return new Message();
	}

	/**
	 * Construct a message from a language key
	 * @param key The key of the message to use
	 * @return this
	 */
	public static Message fromKey(String key) {
		return new Message().setMessageFromKey(key);
	}

	/**
	 * Construct a message from a string
	 * @param message The message to use
	 * @return this
	 */
	public static Message fromString(String message) {
		return new Message().setMessage(message);
	}

	/**
	 * Construct a message from a string list
	 * @param message The message to use
	 * @return this
	 */
	public static Message fromList(List<String> message) {
		return new Message().setMessage(message);
	}

	/**
	 * Get the message with all replacements done
	 * @return Message as a list
	 */
	public List<String> get() {
		executeReplacements();
		return message;
	}

	private List<String> get(Limit limit) {
		executeReplacements(limit);
		return message;
	}

	/**
	 * Get a plain string for the message (for example for using in the console)
	 * @return The message as simple string
	 */
	public String getPlain() {
		executeReplacements();
		return FancyMessageFormat.convertToConsole(message);
	}

	/**
	 * Add the default prefix to the message
	 * @param doIt true if the prefix should be added, otherwise false
	 * @return this
	 */
	public Message prefix(boolean doIt) {
		if(doIt) {
			message.add(0, VARIABLESTART+LANGUAGEVARIABLE+CHATLANGUAGEVARIABLE+VARIABLEEND);
		}
		return this;
	}

	public Message prefix() {
		return prefix(true);
	}

	/**
	 * Set the replacements to apply to the message
	 * @param replacements The replacements to apply
	 *                     - GeneralRegion: All region replacements are applied
	 *                     - Message: Message is inserted
	 *                     - other: index tag is replaced, like %0%
	 * @return this
	 */
	public Message replacements(Object... replacements) {
		this.replacements = replacements;
		return this;
	}

	/**
	 * Append lines to the message
	 * @param lines The lines to append
	 * @return this
	 */
	public Message append(List<String> lines) {
		message.addAll(lines);
		return this;
	}

	/**
	 * Append a message to this message
	 * @param message The message to append
	 * @return this
	 */
	public Message append(Message message) {
		return append(message.get());
	}

	/**
	 * Append lines to the message
	 * @param line The line to append
	 * @return this
	 */
	public Message append(String line) {
		message.add(line);
		return this;
	}

	/**
	 * Send the message to a target
	 * @param target The target to send the message to (Player, CommandSender, Logger)
	 * @return this
	 */
	public Message send(Object target) {
		if(message == null || message.size() == 0 || (message.size() == 1 && message.get(0).length() == 0) || target == null) {
			return this;
		}
		executeReplacements();
		if(target instanceof Player) {
			if(AreaShop.getInstance().getConfig().getBoolean("useFancyMessages")) {
				FancyMessageSender.sendJSON((Player)target, FancyMessageFormat.convertToJSON(message));
			} else {
				((Player)target).sendMessage(FancyMessageFormat.convertToConsole(message));
			}
		} else {
			String plainMessage = FancyMessageFormat.convertToConsole(message);
			if(!AreaShop.getInstance().getConfig().getBoolean("useColorsInConsole")) {
				plainMessage = ChatColor.stripColor(plainMessage);
			}
			if(target instanceof CommandSender) {
				((CommandSender)target).sendMessage(plainMessage);
			} else if(target instanceof Logger) {
				((Logger)target).info(plainMessage);
			} else {
				AreaShop.getInstance().getLogger().warning("Could not send message, target is wrong: "+plainMessage);
			}
		}
		return this;
	}


	// INTERNAL METHODS

	/**
	 * Set the internal message
	 * @param message The message to set
	 * @return this
	 */
	private Message setMessage(List<String> message) {
		this.message = message;
		if(this.message == null) {
			this.message = new ArrayList<>();
		}
		return this;
	}

	/**
	 * Set the internal message with a key
	 * @param key The message key to get the message for
	 * @return this
	 */
	private Message setMessageFromKey(String key) {
		this.key = key;
		return this.setMessage(AreaShop.getInstance().getLanguageManager().getRawMessage(key));
	}

	/**
	 * Set the internal message with a string
	 * @param message The message to set
	 * @return this
	 */
	private Message setMessage(String message) {
		List<String> list = new ArrayList<>();
		list.add(message);
		return this.setMessage(list);
	}

	/**
	 * Apply all replacements to the message
	 */
	private void executeReplacements() {
		executeReplacements(new Limit(REPLACEMENTLIMIT, this));
	}

	private void executeReplacements(Limit limit) {
		// Replace variables until they are all gone, or when the limit is reached
		Pattern variable = Pattern.compile(Pattern.quote(VARIABLESTART)+"[a-zA-Z]+"+Pattern.quote(VARIABLEEND));

		boolean shouldReplace = true;
		while(shouldReplace) {
			List<String> original = new ArrayList<>(message);

			limit.left--;
			if(limit.reached()) {
				if(!limit.notified) {
					limit.notified = true;
					AreaShop.getInstance().getLogger().warning("Reached replacement limit, probably has replacements loops, problematic message: "+limit.message.toString());
				}
				break;
			}

			replaceArgumentVariables(limit);
			replaceLanguageVariables(limit);

			shouldReplace = !message.equals(original);
		}
	}

	/**
	 * Replace argument variables in a message
	 * The arguments to apply as replacements:
	 * - If it is a GeneralRegion the replacements of the region will be applied
	 * - Else the parameter will replace its number surrounded with VARIABLESTART and VARIABLEEND
	 */
	private void replaceArgumentVariables(Limit limit) {
		if(message == null || message.size() == 0 || replacements == null) {
			return;
		}
		boolean result = false;
		for(int i = 0; i < message.size(); i++) {
			int number = 0;
			for(Object param : replacements) {
				if(param != null) {
					if(param instanceof GeneralRegion) {
						message.set(i, ((GeneralRegion)param).applyAllReplacements(message.get(i)));
					} else if(param instanceof Message) {
						Pattern variables = Pattern.compile(Pattern.quote(VARIABLESTART)+number+Pattern.quote(VARIABLEEND));
						Matcher matches = variables.matcher(message.get(i));
						if(matches.find()) {
							FancyMessageFormat.insertMessage(message, ((Message)param).get(limit), i, matches.start(), matches.end());
						}
						number++;
					} else {
						message.set(i, message.get(i).replace(VARIABLESTART+number+VARIABLEEND, param.toString()));
						number++;
					}
				}
			}
		}
	}

	/**
	 * Replace all language variables in a message
	 */
	private void replaceLanguageVariables(Limit limit) {
		if(message == null || message.size() == 0) {
			return;
		}

		Pattern variables = Pattern.compile(
				Pattern.quote(VARIABLESTART)+
						Pattern.quote(LANGUAGEVARIABLE)+"[a-zA-Z-]+"+        // Language key
						"(\\|(.*?\\|)+)?"+                                    // Optional message arguments
						Pattern.quote(VARIABLEEND)
		);

		for(int i = 0; i < message.size(); i++) {
			Matcher matches = variables.matcher(message.get(i));
			if(matches.find()) {
				String variable = matches.group();
				String key;
				Object[] arguments = null;
				if(variable.contains("|")) {
					key = variable.substring(6, variable.indexOf("|"));
					arguments = variable.substring(variable.indexOf("|")+1, variable.length()-1).split("\\|");
				} else {
					key = variable.substring(6, variable.length()-1);
				}
				Message insert = Message.fromKey(key);
				if(arguments != null) {
					insert.replacements(arguments);
				}

				// Insert message
				int startDiff = message.size()-i;
				FancyMessageFormat.insertMessage(message, insert.get(limit), i, matches.start(), matches.end());
				// Skip to end of insert, language tags already replaced
				i = message.size()-startDiff;
			}
		}
	}

	@Override
	public String toString() {
		return "Message(key:"+key+", message:"+message+")";
	}


	/**
	 * Class to store a limit
	 */
	private class Limit {
		public int left = 0;
		public boolean notified = false;
		public Message message;

		/**
		 * Set the initial limit
		 * @param count The limit to use
		 */
		public Limit(int count, Message message) {
			left = count;
			this.message = message;
		}

		/**
		 * Check if the limit is reached
		 * @return true if the limit is reached, otherwise false
		 */
		public boolean reached() {
			return left <= 0;
		}
	}
}
