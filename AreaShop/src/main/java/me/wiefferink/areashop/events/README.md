# Events

This package contains event classes that allow other plugins to listen to events about AreaShop regions.

* `RegionEvent` class represents a generic event.
* `CancellableRegionEvent` represents an event that has a region and can be cancelled.
* `NotifyRegionEvent` represents events that are to inform only, and cannot be cancelled anymore (an `CancellableRegionEvent` would be sent first).
