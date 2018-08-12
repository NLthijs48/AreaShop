# Features

This package contains classes that represent a certain feature and implement it for all regions.

* `RegionFeature` represents a single feature, for each implementing class one instance is created without an assigned region, to listen for events etc, and an instance per region is created on-demand to handle region specific things.
* More features will be moved from `GeneralRegion` to these kind of classes, to keep all code for a feature together and cleanup `GeneralRegion`.