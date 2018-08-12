# Regions

This package contains the `GeneralRegion` class which represents a region in AreaShop. Such a region is linked to a WorldGuard region and has its own configuration file at `/plugins/AreaShop/regions/<name>.yml`.

The `BuyRegion` and `RentRegion` classes represent buy and rental regions (mutually exclusive) and inherit from `GeneralRegion`.

To group regions there is `RegionGroup`, which allows configuring settings for a lot of regions at once.