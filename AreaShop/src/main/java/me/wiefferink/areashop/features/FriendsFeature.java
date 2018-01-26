package me.wiefferink.areashop.features;

import me.wiefferink.areashop.events.askandnotify.AddedFriendEvent;
import me.wiefferink.areashop.events.askandnotify.DeletedFriendEvent;
import me.wiefferink.areashop.regions.GeneralRegion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class FriendsFeature extends RegionFeature {

	public FriendsFeature(GeneralRegion region) {
		setRegion(region);
	}

	/**
	 * Add a friend to the region.
	 * @param player The UUID of the player to add
	 * @param by     The CommandSender that is adding the friend, or null
	 * @return true if the friend has been added, false if adding a friend was cancelled by another plugin
	 */
	public boolean addFriend(UUID player, CommandSender by) {
		// Fire and check event
		AddedFriendEvent event = new AddedFriendEvent(getRegion(), Bukkit.getOfflinePlayer(player), by);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) {
			plugin.message(by, "general-cancelled", event.getReason(), this);
			return false;
		}

		Set<String> friends = new HashSet<>(getRegion().getConfig().getStringList("general.friends"));
		friends.add(player.toString());
		List<String> list = new ArrayList<>(friends);
		getRegion().setSetting("general.friends", list);
		return true;
	}

	/**
	 * Delete a friend from the region.
	 * @param player The UUID of the player to delete
	 * @param by     The CommandSender that is adding the friend, or null
	 * @return true if the friend has been added, false if adding a friend was cancelled by another plugin
	 */
	public boolean deleteFriend(UUID player, CommandSender by) {
		// Fire and check event
		DeletedFriendEvent event = new DeletedFriendEvent(getRegion(), Bukkit.getOfflinePlayer(player), by);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) {
			plugin.message(by, "general-cancelled", event.getReason(), this);
			return false;
		}

		Set<String> friends = new HashSet<>(getRegion().getConfig().getStringList("general.friends"));
		friends.remove(player.toString());
		List<String> list = new ArrayList<>(friends);
		if(list.isEmpty()) {
			getRegion().setSetting("general.friends", null);
		} else {
			getRegion().setSetting("general.friends", list);
		}
		return true;
	}

	/**
	 * Get the list of friends added to this region.
	 * @return Friends added to this region
	 */
	public Set<UUID> getFriends() {
		HashSet<UUID> result = new HashSet<>();
		for(String friend : getRegion().getConfig().getStringList("general.friends")) {
			try {
				UUID id = UUID.fromString(friend);
				result.add(id);
			} catch(IllegalArgumentException e) {
				// Don't add it
			}
		}
		return result;
	}

	/**
	 * Get the list of friends added to this region.
	 * @return Friends added to this region
	 */
	public Set<String> getFriendNames() {
		HashSet<String> result = new HashSet<>();
		for(UUID friend : getFriends()) {
			OfflinePlayer player = Bukkit.getOfflinePlayer(friend);
			if(player != null && player.getName() != null) {
				result.add(player.getName());
			}
		}
		return result;
	}

	/**
	 * Remove all friends that are added to this region.
	 */
	public void clearFriends() {
		getRegion().setSetting("general.friends", null);
	}

}
