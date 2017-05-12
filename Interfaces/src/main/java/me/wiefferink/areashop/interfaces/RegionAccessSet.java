package me.wiefferink.areashop.interfaces;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RegionAccessSet {
	private Set<String> playerNames;
	private Set<UUID> playerUniqueIds;
	private Set<String> groupNames;

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
	 * @return Set with groups added to this RegionAccessSet
	 */
	public Set<String> getGroupNames() {
		return groupNames;
	}
}
