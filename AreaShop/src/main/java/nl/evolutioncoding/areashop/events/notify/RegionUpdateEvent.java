package nl.evolutioncoding.areashop.events.notify;

import nl.evolutioncoding.areashop.events.NotifyAreaShopEvent;
import nl.evolutioncoding.areashop.regions.GeneralRegion;

/**
 * Broadcasted when the data of a region changes.
 * Should be used for updating displays that use region data.
 */
public class RegionUpdateEvent extends NotifyAreaShopEvent {

	private GeneralRegion region;

	/**
	 * Contructor
	 * @param region The region that has been updated
	 */
	public RegionUpdateEvent(GeneralRegion region) {
		this.region = region;
	}

	/**
	 * The region that has been updated
	 * @return The GeneralRegion that has been updated
	 */
	public GeneralRegion getRegion() {
		return region;
	}
}
