package me.wiefferink.areashop.events.askandnotify;

import me.wiefferink.areashop.events.CancellableAreaShopEvent;
import me.wiefferink.areashop.regions.GeneralRegion;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

/**
 * Broadcasted when a friend is being added to a region
 */
public class AddFriendEvent extends CancellableAreaShopEvent {

	private GeneralRegion region;
	private OfflinePlayer friend;
	private CommandSender by;

	/**
	 * Constructor
	 * @param region The region the friend is getting added to
	 * @param friend The friend that is about to be added
	 * @param by     The CommandSender that is adding the friend, or null if none
	 */
	public AddFriendEvent(GeneralRegion region, OfflinePlayer friend, CommandSender by) {
		this.region = region;
		this.friend = friend;
		this.by = by;
	}

	/**
	 * Get the region where the friend is getting added to
	 * @return the region
	 */
	public GeneralRegion getRegion() {
		return region;
	}

	/**
	 * Get the OfflinePlayer that is getting added as friend
	 * @return The friend that is getting added
	 */
	public OfflinePlayer getFriend() {
		return friend;
	}

	/**
	 * Get the CommandSender that is adding the friend
	 * @return null if none, a CommandSender if done by someone (likely Player or ConsoleCommandSender)
	 */
	public CommandSender getBy() {
		return by;
	}
}
