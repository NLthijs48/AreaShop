package me.wiefferink.areashop.events.ask;

import me.wiefferink.areashop.events.CancellableRegionEvent;
import me.wiefferink.areashop.regions.RentRegion;

/**
 * Broadcasted when a region is about to be unrented.
 */
public class UnrentingRegionEvent extends CancellableRegionEvent<RentRegion> {

	/**
	 * Constructor.
	 * @param region The region that is about to be unrented
	 */
	public UnrentingRegionEvent(RentRegion region) {
		super(region);
	}
}
