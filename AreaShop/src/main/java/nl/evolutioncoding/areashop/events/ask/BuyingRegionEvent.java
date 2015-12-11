package nl.evolutioncoding.areashop.events.ask;

import nl.evolutioncoding.areashop.events.CancellableAreaShopEvent;
import nl.evolutioncoding.areashop.regions.BuyRegion;
import org.bukkit.entity.Player;

/**
 * Broadcasted when a player tries to buy a region.
 */
public class BuyingRegionEvent extends CancellableAreaShopEvent {

	private BuyRegion region;
	private Player player;

	/**
	 * Constructor
	 * @param region The region that is about to get bought
	 * @param player The player that tries to buy the region
	 */
	public BuyingRegionEvent(BuyRegion region, Player player) {
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
	public Player getPlayer() {
		return player;
	}
}
