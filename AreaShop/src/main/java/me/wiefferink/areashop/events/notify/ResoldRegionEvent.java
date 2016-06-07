package me.wiefferink.areashop.events.notify;

import me.wiefferink.areashop.events.NotifyAreaShopEvent;
import me.wiefferink.areashop.regions.BuyRegion;

import java.util.UUID;

/**
 * Broadcasted when a region has been resold
 */
public class ResoldRegionEvent extends NotifyAreaShopEvent {

	private BuyRegion region;
	private UUID from;

	/**
	 * Constructor
	 * @param region The region that has been resold
	 * @param from   The player from who the region has been resold to the current owner
	 */
	public ResoldRegionEvent(BuyRegion region, UUID from) {
		this.region = region;
		this.from = from;
	}

	/**
	 * Get the region that has been bought
	 * @return the region that has been bought
	 */
	public BuyRegion getRegion() {
		return region;
	}

	/**
	 * Get the player that the region has been bought from
	 * @return The UUID of the player that the region has been bought from
	 */
	public UUID getFromPlayer() {
		return from;
	}
}
