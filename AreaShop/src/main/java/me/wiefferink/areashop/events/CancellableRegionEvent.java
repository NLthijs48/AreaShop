package me.wiefferink.areashop.events;

public class CancellableRegionEvent<T> extends RegionEvent<T> {

	private boolean cancelled;
	private String reason;

	public CancellableRegionEvent(T region) {
		super(region);
	}

	/**
	 * Cancel the event from happening.
	 * @param reason The reason of cancelling, used for display to the user, should end with a dot
	 */
	public void cancel(String reason) {
		this.cancelled = true;
		this.reason = reason;
	}

	/**
	 * Let the event continue, possible overwriting a cancel() call from another plugin.
	 */
	public void allow() {
		this.cancelled = false;
		this.reason = null;
	}

	/**
	 * Check if the event has been cancelled.
	 * @return true if the event has been cancelled, otherwise false
	 */
	public boolean isCancelled() {
		return cancelled;
	}

	/**
	 * Get the reason why this event is cancelled.
	 * @return null if there is no reason or the event is not cancelled, otherwise a string
	 */
	public String getReason() {
		return reason;
	}

}
