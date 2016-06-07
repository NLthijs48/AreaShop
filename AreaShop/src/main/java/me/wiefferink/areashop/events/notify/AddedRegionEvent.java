package me.wiefferink.areashop.events.notify;

import me.wiefferink.areashop.events.NotifyAreaShopEvent;
import me.wiefferink.areashop.regions.GeneralRegion;

/**
 * Broadcasted when a region has been added to AreaShop
 */
public class AddedRegionEvent extends NotifyAreaShopEvent {

	private GeneralRegion region;

	/**
	 * Constructor
	 * @param region The region that has been added
	 */
	public AddedRegionEvent(GeneralRegion region) {
		this.region = region;
	}

	/**
	 * Get the region that has been added
	 * @return the region that has been added
	 */
	public GeneralRegion getRegion() {
		return region;
	}
}
