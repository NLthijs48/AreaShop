package me.wiefferink.areashop.events;

public class CancellableRegionEvent<T> extends CancellableEvent {
	protected T region;

	public CancellableRegionEvent(T region) {
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
