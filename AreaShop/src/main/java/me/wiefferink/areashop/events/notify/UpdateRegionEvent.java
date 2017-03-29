package me.wiefferink.areashop.events.notify;

import me.wiefferink.areashop.events.NotifyRegionEvent;
import me.wiefferink.areashop.regions.GeneralRegion;

/**
 * Broadcasted when the data of a region changes.
 * Should be used for updating displays that use region data.
 */
public class UpdateRegionEvent extends NotifyRegionEvent<GeneralRegion> {

	/**
	 * Contructor.
	 * @param region The region that has been updated
	 */
	public UpdateRegionEvent(GeneralRegion region) {
		super(region);
	}
}
