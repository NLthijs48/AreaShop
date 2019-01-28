package me.wiefferink.areashop.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RegionEvent<T> extends Event {
	protected T region;
	private static final HandlerList handlers = new HandlerList();

	public RegionEvent(T region) {
		this.region = region;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	// Required by Bukkit/Spigot
	public static HandlerList getHandlerList() {
		return handlers;
	}

	/**
	 * Get the region of this event.
	 * @return The region the event is about
	 */
	public T getRegion() {
		return region;
	}
}
