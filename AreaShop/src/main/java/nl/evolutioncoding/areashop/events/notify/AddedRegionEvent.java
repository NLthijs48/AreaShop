package nl.evolutioncoding.areashop.events.notify;

import nl.evolutioncoding.areashop.events.NotifyAreaShopEvent;
import nl.evolutioncoding.areashop.regions.GeneralRegion;

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
