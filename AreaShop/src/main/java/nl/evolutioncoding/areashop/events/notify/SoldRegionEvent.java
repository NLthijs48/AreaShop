package nl.evolutioncoding.areashop.events.notify;

import nl.evolutioncoding.areashop.events.NotifyAreaShopEvent;
import nl.evolutioncoding.areashop.regions.BuyRegion;

import java.util.UUID;

public class SoldRegionEvent extends NotifyAreaShopEvent {

	private BuyRegion region;
	private UUID oldBuyer;

	public SoldRegionEvent(BuyRegion region, UUID oldBuyer) {
		this.region = region;
		this.oldBuyer = oldBuyer;
	}

	/**
	 * Get the region that has been sold
	 * @return the region that has been sold
	 */
	public BuyRegion getRegion() {
		return region;
	}

	/**
	 * Get the player that the region is sold for
	 * @return The UUID of the player that the region is sold for
	 */
	public UUID getOldBuyer() {
		return oldBuyer;
	}
}
