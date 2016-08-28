package me.wiefferink.areashop.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CancellableEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private String reason;

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	/**
	 * Cancel the event from happening
	 * @param reason The reason of cancelling, used for display to the user, should end with a dot
	 */
	public void cancel(String reason) {
		this.cancelled = true;
		this.reason = reason;
	}

	/**
	 * Let the event continue, possible overwriting a cancel() call from another plugin
	 */
	public void allow() {
		this.cancelled = false;
		this.reason = null;
	}

	/**
	 * Check if the event has been cancelled
	 * @return true if the event has been cancelled, otherwise false
	 */
	public boolean isCancelled() {
		return cancelled;
	}

	/**
	 * Get the reason why this event is cancelled
	 * @return null if there is no reason or the event is not cancelled, otherwise a string
	 */
	public String getReason() {
		return reason;
	}
}
