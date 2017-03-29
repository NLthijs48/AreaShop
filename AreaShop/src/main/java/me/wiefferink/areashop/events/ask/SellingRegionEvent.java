package me.wiefferink.areashop.events.ask;

import me.wiefferink.areashop.events.CancellableRegionEvent;
import me.wiefferink.areashop.regions.BuyRegion;

/**
 * Broadcasted when a region is about to get sold.
 */
public class SellingRegionEvent extends CancellableRegionEvent<BuyRegion> {

	/**
	 * Constructor.
	 * @param region The region that is about to get sold
	 */
	public SellingRegionEvent(BuyRegion region) {
		super(region);
	}
}
