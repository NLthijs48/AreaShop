package me.wiefferink.areashop.commands;

import me.wiefferink.areashop.tools.Utils;
import me.wiefferink.interactivemessenger.processing.Message;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MessageCommand extends CommandAreaShop {

	@Override
	public String getCommandStart() {
		return "areashop message";
	}

	@Override
	public String getHelp(CommandSender target) {
		// Internal command, no need to show in the help list
		return null;
	}

	@Override
	public void execute(final CommandSender sender, final String[] args) {
		if(!sender.hasPermission("areashop.message")) {
			plugin.message(sender, "message-noPermission");
			return;
		}

		if(args.length < 3) {
			plugin.message(sender, "message-help");
			return;
		}

		Player player = Bukkit.getPlayer(args[1]);
		if(player == null) {
			plugin.message(sender, "message-notOnline", args[1]);
			return;
		}

		String[] messageArgs = new String[args.length - 2];
		System.arraycopy(args, 2, messageArgs, 0, args.length - 2);
		String message = StringUtils.join(messageArgs, " ");

		Message.fromString(message).send(player);
	}

	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		List<String> result = new ArrayList<>();
		if(toComplete == 2) {
			for(Player player : Utils.getOnlinePlayers()) {
				result.add(player.getName());
			}
		}
		return result;
	}

}










