package me.wiefferink.areashop.events;

public class NotifyRegionEvent<T> extends NotifyEvent {
	protected T region;

	public NotifyRegionEvent(T region) {
		this.region = region;
	}

	/**
	 * Get the region of this event
	 * @return The region the event is about
	 */
	public T getRegion() {
		return region;
	}
}
