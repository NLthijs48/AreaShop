package me.wiefferink.areashop.events.ask;

import me.wiefferink.areashop.events.CancellableRegionEvent;
import me.wiefferink.areashop.regions.RentRegion;
import org.bukkit.OfflinePlayer;

/**
 * Broadcasted when a player tries to rent a region.
 */
public class RentingRegionEvent extends CancellableRegionEvent<RentRegion> {

	private final OfflinePlayer player;
	private final boolean extending;

	/**
	 * Constructor.
	 * @param region    The region that is about to be rented
	 * @param player    The player that tries to rent the region
	 * @param extending true if the player is extending the rental of the region, otherwise false
	 */
	public RentingRegionEvent(RentRegion region, OfflinePlayer player, boolean extending) {
		super(region);
		this.player = player;
		this.extending = extending;
	}

	/**
	 * Get the player that is trying to rent the region.
	 * @return The player that is trying to rent the region
	 */
	public OfflinePlayer getPlayer() {
		return player;
	}

	/**
	 * Check if the player is extending the region or renting it for the first time.
	 * @return true if the player tries to extend the region, false if he tries to rent it the first time
	 */
	public boolean isExtending() {
		return extending;
	}
}
