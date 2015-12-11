package nl.evolutioncoding.areashop.events.ask;

import nl.evolutioncoding.areashop.events.CancellableAreaShopEvent;
import nl.evolutioncoding.areashop.regions.RentRegion;
import org.bukkit.entity.Player;

/**
 * Broadcasted when a player tries to rent a region
 */
public class RentingRegionEvent extends CancellableAreaShopEvent {

	private RentRegion region;
	private Player player;
	private boolean extending;

	/**
	 * Constructor
	 * @param region    The region that is about to be rented
	 * @param player    The player that tries to rent the region
	 * @param extending true if the player is extending the rental of the region, otherwise false
	 */
	public RentingRegionEvent(RentRegion region, Player player, boolean extending) {
		this.region = region;
		this.player = player;
		this.extending = extending;
	}

	/**
	 * Get the region that is about to be rented
	 * @return the region that is about to be rented
	 */
	public RentRegion getRegion() {
		return region;
	}

	/**
	 * Get the player that is trying to rent the region
	 * @return The player that is trying to rent the region
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Check if the player is extending the region or renting it for the first time
	 * @return true if the player tries to extend the region, false if he tries to rent it the first time
	 */
	public boolean isExtending() {
		return extending;
	}
}
