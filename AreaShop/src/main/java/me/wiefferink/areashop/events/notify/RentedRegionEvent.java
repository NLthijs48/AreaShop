package me.wiefferink.areashop.events.notify;

import me.wiefferink.areashop.events.NotifyAreaShopEvent;
import me.wiefferink.areashop.regions.RentRegion;

/**
 * Broadcasted when a region has been rented
 */
public class RentedRegionEvent extends NotifyAreaShopEvent {

	private RentRegion region;
	private boolean extended;

	/**
	 * Constructor
	 * @param region   The region that has been rented
	 * @param extended true if the region has been extended, false if this is the first time buying the region
	 */
	public RentedRegionEvent(RentRegion region, boolean extended) {
		this.region = region;
		this.extended = extended;
	}

	/**
	 * Get the region that has been rented
	 * @return the region that has been rented
	 */
	public RentRegion getRegion() {
		return region;
	}

	/**
	 * Check if the region was extended or rented for the first time
	 * @return true if the region was extended, false when rented for the first time
	 */
	public boolean hasExtended() {
		return extended;
	}
}
