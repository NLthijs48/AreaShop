package me.wiefferink.areashop.events.ask;

import me.wiefferink.areashop.events.CancellableAreaShopEvent;
import me.wiefferink.areashop.regions.RentRegion;

/**
 * Broadcasted when a region is about to be unrented
 */
public class UnrentingRegionEvent extends CancellableAreaShopEvent {

	private RentRegion region;

	/**
	 * Constructor
	 * @param region The region that is about to be unrented
	 */
	public UnrentingRegionEvent(RentRegion region) {
		this.region = region;
	}

	/**
	 * Get the region that is about to be rented
	 * @return the region that is about to be rented
	 */
	public RentRegion getRegion() {
		return region;
	}
}
