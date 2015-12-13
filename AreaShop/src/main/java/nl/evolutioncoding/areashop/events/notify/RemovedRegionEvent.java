package nl.evolutioncoding.areashop.events.notify;

import nl.evolutioncoding.areashop.events.NotifyAreaShopEvent;
import nl.evolutioncoding.areashop.regions.GeneralRegion;

/**
 * Broadcasted when a region has been removed from AreaShop
 */
public class RemovedRegionEvent extends NotifyAreaShopEvent {

	private GeneralRegion region;

	/**
	 * Constructor
	 * @param region The region that has been removed
	 */
	public RemovedRegionEvent(GeneralRegion region) {
		this.region = region;
	}

	/**
	 * Get the region that has been removed
	 * @return the region that has been removed
	 */
	public GeneralRegion getRegion() {
		return region;
	}
}
