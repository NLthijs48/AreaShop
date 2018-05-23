package me.wiefferink.areashop.events.notify;

import me.wiefferink.areashop.events.NotifyRegionEvent;
import me.wiefferink.areashop.regions.BuyRegion;

import java.util.UUID;

/**
 * Broadcasted when a region is sold.
 */
public class SoldRegionEvent extends NotifyRegionEvent<BuyRegion> {

	private final UUID oldBuyer;
	private final double refundedMoney;

	/**
	 * Constructor.
	 * @param region        The region that has been sold
	 * @param oldBuyer      The player for which the region has been sold
	 * @param refundedMoney The amount of money that has been refunded
	 */
	public SoldRegionEvent(BuyRegion region, UUID oldBuyer, double refundedMoney) {
		super(region);
		this.oldBuyer = oldBuyer;
		this.refundedMoney = refundedMoney;
	}

	/**
	 * Get the player that the region is sold for.
	 * @return The UUID of the player that the region is sold for
	 */
	public UUID getOldBuyer() {
		return oldBuyer;
	}

	/**
	 * Get the amount that is paid back to the player.
	 * @return The amount of money paid back to the player
	 */
	public double getRefundedMoney() {
		return refundedMoney;
	}
}
