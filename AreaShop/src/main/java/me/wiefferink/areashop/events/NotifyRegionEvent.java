package me.wiefferink.areashop.events;

public class NotifyRegionEvent<T> extends RegionEvent<T> {
	public NotifyRegionEvent(T region) {
		super(region);
	}
}
