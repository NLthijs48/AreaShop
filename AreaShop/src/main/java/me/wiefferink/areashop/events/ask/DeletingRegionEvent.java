package me.wiefferink.areashop.events.ask;

import me.wiefferink.areashop.events.CancellableRegionEvent;
import me.wiefferink.areashop.regions.GeneralRegion;

/**
 * Broadcasted when a region has been added to AreaShop.
 */
public class DeletingRegionEvent extends CancellableRegionEvent<GeneralRegion> {

	/**
	 * Constructor.
	 * @param region The region that has been added
	 */
	public DeletingRegionEvent(GeneralRegion region) {
		super(region);
	}

}
