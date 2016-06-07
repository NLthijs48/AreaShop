package me.wiefferink.areashop.events.ask;

import me.wiefferink.areashop.events.CancellableAreaShopEvent;
import me.wiefferink.areashop.regions.BuyRegion;
import org.bukkit.entity.Player;

/**
 * Broadcasted when a player tries to resell a region
 */
public class ResellingRegionEvent extends CancellableAreaShopEvent {

	private BuyRegion region;
	private Player player;

	/**
	 * Contructor
	 * @param region The region that the player is trying to resell
	 * @param player The player that is trying to buy this region from the current owner
	 */
	public ResellingRegionEvent(BuyRegion region, Player player) {
		this.region = region;
		this.player = player;
	}

	/**
	 * Get the region that is about to be bought
	 * @return the region that is about to be bought
	 */
	public BuyRegion getRegion() {
		return region;
	}

	/**
	 * Get the player that is trying to buy the region
	 * @return The player that is trying to buy the region
	 */
	public Player getBuyer() {
		return player;
	}
}
