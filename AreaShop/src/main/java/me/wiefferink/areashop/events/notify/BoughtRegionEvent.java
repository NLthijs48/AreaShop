package me.wiefferink.areashop.events.notify;

import me.wiefferink.areashop.events.NotifyAreaShopEvent;
import me.wiefferink.areashop.regions.BuyRegion;

/**
 * Broadcasted when a region has been bought
 */
public class BoughtRegionEvent extends NotifyAreaShopEvent {

	private BuyRegion region;

	/**
	 * Constructor
	 * @param region The region that has been bought
	 */
	public BoughtRegionEvent(BuyRegion region) {
		this.region = region;
	}

	/**
	 * Get the region that has been bought
	 * @return the region that has been bought
	 */
	public BuyRegion getRegion() {
		return region;
	}
}
