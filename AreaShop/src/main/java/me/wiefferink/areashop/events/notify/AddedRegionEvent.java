package me.wiefferink.areashop.events.notify;

import me.wiefferink.areashop.events.NotifyRegionEvent;
import me.wiefferink.areashop.regions.GeneralRegion;

/**
 * Broadcasted when a region has been added to AreaShop.
 */
public class AddedRegionEvent extends NotifyRegionEvent<GeneralRegion> {

	/**
	 * Constructor.
	 * @param region The region that has been added
	 */
	public AddedRegionEvent(GeneralRegion region) {
		super(region);
	}

}
