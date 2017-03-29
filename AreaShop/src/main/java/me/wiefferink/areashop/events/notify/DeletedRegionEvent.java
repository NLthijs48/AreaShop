package me.wiefferink.areashop.events.notify;

import me.wiefferink.areashop.events.NotifyRegionEvent;
import me.wiefferink.areashop.regions.GeneralRegion;

/**
 * Broadcasted when a region has been removed from AreaShop.
 */
public class DeletedRegionEvent extends NotifyRegionEvent<GeneralRegion> {

	/**
	 * Constructor.
	 * @param region The region that has been removed
	 */
	public DeletedRegionEvent(GeneralRegion region) {
		super(region);
	}
}
