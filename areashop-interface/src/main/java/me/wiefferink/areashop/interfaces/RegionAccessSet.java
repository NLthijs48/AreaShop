package me.wiefferink.areashop.interfaces;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RegionAccessSet {
	private final Set<String> playerNames;
	private final Set<UUID> playerUniqueIds;
	private final Set<String> groupNames;

	/**
	 * Constructor, creates an empty set.
	 */
	public RegionAccessSet() {
		playerNames = new HashSet<>();
		playerUniqueIds = new HashSet<>();
		groupNames = new HashSet<>();
	}

	/**
	 * Get the players that have been added by name.
	 * @return Set with players that have been added by name
	 */
	public Set<String> getPlayerNames() {
		return playerNames;
	}

	/**
	 * Get the players that have been added by uuid.
	 * @return Set with players that have been added by uuid
	 */
	public Set<UUID> getPlayerUniqueIds() {
		return playerUniqueIds;
	}

	/**
	 * Get the groups.
	 * @return Set with groups added to this RegionAccessSet.
	 */
	public Set<String> getGroupNames() {
		return groupNames;
	}

	/**
	 * Get this access set as a list of player UUIDs.
	 * @return List of player UUIDs, first players already added by UUID, then players added by name, groups are not in the list
	 */
	public List<UUID> asUniqueIdList() {
		List<UUID> result = new ArrayList<>();
		result.addAll(playerUniqueIds);
		for(String playerName : playerNames) {
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
			if(offlinePlayer != null && offlinePlayer.getUniqueId() != null) {
				result.add(offlinePlayer.getUniqueId());
			}
		}
		return result;
	}
}
