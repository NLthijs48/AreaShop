package me.wiefferink.areashop.events.notify;

import me.wiefferink.areashop.events.NotifyRegionEvent;
import me.wiefferink.areashop.regions.BuyRegion;

/**
 * Broadcasted when a region has been bought.
 */
public class BoughtRegionEvent extends NotifyRegionEvent<BuyRegion> {

	/**
	 * Constructor.
	 * @param region The region that has been bought
	 */
	public BoughtRegionEvent(BuyRegion region) {
		super(region);
	}

}
