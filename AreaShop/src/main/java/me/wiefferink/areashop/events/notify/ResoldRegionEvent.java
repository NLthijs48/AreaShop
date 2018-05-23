package me.wiefferink.areashop.events.notify;

import me.wiefferink.areashop.events.NotifyRegionEvent;
import me.wiefferink.areashop.regions.BuyRegion;

import java.util.UUID;

/**
 * Broadcasted when a region has been resold.
 */
public class ResoldRegionEvent extends NotifyRegionEvent<BuyRegion> {

	private final UUID from;

	/**
	 * Constructor.
	 * @param region The region that has been resold
	 * @param from   The player from who the region has been resold to the current owner
	 */
	public ResoldRegionEvent(BuyRegion region, UUID from) {
		super(region);
		this.from = from;
	}

	/**
	 * Get the player that the region has been bought from.
	 * @return The UUID of the player that the region has been bought from
	 */
	public UUID getFromPlayer() {
		return from;
	}
}
