## AreaShop NEXT
[**Planned features**](https://github.com/NLthijs48/AreaShop/milestone/5) 

## AreaShop 2.6.0
**Features:**
* Add 1.13 FastAsyncWorldEdit support (note: FastAsyncWorldEdit does seem to work correctly for 1.13.0, but not yet on 1.13.2)
* Add support for the latest WorldEdit/WorldGuard 7 versions (which have refactored the vector classes)
* Improve adding back signs after they have been removed by players or schematic restore
* Improve performance of the BlockPhysicsEvent (allow signs to get removed to prevent loops, while not dropping an item)
* Updated all translations, pulled from Transifex

**Fixed:**
* Fixed `/as setduration` tab completion
* Fixed 1.13 Material handling, especially dealing with signs and the checks by the safe teleport function
* `/as add`: properly set the landlord when the player is an owner of the region (it was only working for members)

**Technical:**
* Change Maven dependencies to use `.jar` files in the `dependencies/` folder to ensure the build keeps working even though maven repositories have disappeared

## AreaShop 2.5.0
**Features:**
* Minecraft 1.13 and 1.13.1 support.
* WorldGuard 7 support.
* WorldEdit 7 support.
* Update all translations (pulled from Transifex).
* Add import for RegionForSale regions:
    * `/as import RegionForSale`
    * Imports all regions from RegionForSale
    * Converts almost all settings to their AreaShop equivalent
    * Handles parent regions
* Add setting to automatically extends a rent when the time is up: `rent.autoExtend` in `default.yml`.
* Added options to disable players selling/reselling regions: `buy.sellDisabled`, `buy.resellDisabled`.
* Add setting to disable cross-world teleports for the `/as tp` command: `teleportCrossWorld` in the `general` section of `default.yml`.
* Add setting to disable cross-world teleports for the `/as find` command: `findCrossWorld` in the `general` section of `default.yml`.
* Allow `price` and `moneyBack` values to be calculated dynamically:
  * All region variables can be used: `price: "5*%volume%"` would be `$5` per cubic meter (block in the region).
  * Full JavaScript expressions and code can be used, the last statement will be the resulting price.
* Add `%volume%` variable for the number of blocks in a region, which supports polygon regions correctly.
* `/as stack` now allows the usage of `#` to choose the position of the number in the region name.
* Add customizable metric suffixes, now you can decide when to shorten a number and how, see `metricSymbols` in `config.yml`.
* Add support for automatically adding all AreaShop regions of a certain world to a RegionGroup

**Technical:**
* BREAKING: `BuyingRegionEvent`, `RentingRegionEvent` and `ResellingRegionEvent` now use `OfflinePlayer` instead of `Player`, because renting/buying/reselling is now also possible while the player is offline.
* Improved file loading mechanics
* Improve sign update performance, AreaShop will never load chunks anymore, and will simply update signs when players load chunks.
* Moved the messaging library to its own project: [InteractiveMessenger](https://github.com/NLthijs48/InteractiveMessenger)
  * Added a lot of automated tests to ensure all functionality keeps working
  * Fixed bugs with formatting codes, line endings and hover messages
  * All user input that is inserted into messages is now escaped properly (using variables does not break the message anyore)
* Add [bStats.org](https://bstats.org/plugin/bukkit/AreaShop) analytics. Since mcstats.org is really unstable and offline half of the time I added bStats (which tracks the same information, plus the number of regions and their state), MCStats tracking will be removed in a future version.
* Groups are never removed from `groups.yml` anymore, previously they were removed when they had zero regions, which wipe settings.
* Github update checker has been added as replacement for the BukkitDev updater (it will never download anything, it just notifies about updates).
* Only update WorldGuard region settings if they are different, this should prevent unnecessary saving by WorldGuard.
* When new code is pushed to Github the translations on Transifex get updated automatically, so that translations can begin immediately.

**Fixed:**
* Security issues caused by saving/restoring entities in schematics
* Prevent missing `schematicProfile` causing an exception.
* Setting `schematicProfile` in a region file.
* Players could resell their region with a negative price.
* Using `landlordName` without `landlord` (UUID) setting works correctly now.
* Using `/as addfriend` with a player that is online for the first time.
* Removing the English time modifiers from the config.yml file would cause all tasks to be broken, because their schedule time is defined with strings like `1 minute`. The English modifiers from the config.yml inside the .jar file are always added now.
* `/as setowner` for regions that are not yet rented has been fixed.
* Restoring polygon regions will now only touch blocks that are actually inside the region.

***

## AreaShop V2.4.0
* Change the `config.yml` and `default.yml` files to be easier to understand:
  * The profiles previously listed in the `config.yml` are now directly assigned to the profile settings in `default.yml`
  * When you give a profile setting in `default.yml` a string it will still look up the profile in `config.yml`, which ensures your old config still works.
* Add permissions `areashop.teleportavailable` and `areashop.teleportavailablesign`, with which you can give players permission to teleport to all forsale/forrent regions (thanks jewome62 for the [pull request](https://github.com/NLthijs48/AreaShop/pull/241)!).
* Support for stacking up and down with `/as stack`.
* Fix clicking signs while confirmations are enabled trying to execute commands as players by default (causing that the player does not get a confirmation message).
* Fix a line break problem in the message parser and fix line breaks on 1.7 servers.
* Fix some color parsing issues in the message parser (it would not detect a color if you directly used the section sign).
* Add tab completion for `/as me`.
* Fix a bug that caused `/as setowner` extending the rent, even though the owner is different as the previous one.
* Rename the events AreaShop emits for integration with other plugins (**this breaks existing plugins that use these events!**).
* Fix WorldGuard version detection.

***

## AreaShop V2.3.2
* Added Russian translation (86% complete, so if you know Russian please [help translating](https://github.com/NLthijs48/AreaShop/wiki/Language-support))
* Fixed server crash bug caused by the new messages system (#205)
* Improve stability of the messages system (all reflection removed, using /tellraw in the background now)
* Fix Vault API repo link, thanks Androkai (#206)

***

## AreaShop V2.3.1
* Added German, French and Norwegian translations, thanks to all the translators!
* Added automatic conversion for language files downloaded from Transifex (this means you can download the latest version from Transifex and AreaShop will correctly read it).

***

## AreaShop V2.3.0
**Additions:**
- Add hover/click support for all messages of AreaShop:
  - If a region name is in a message then you can click the region name to see all information about the region.
  - Player names can be clicked to see which regions they own.
  - For long lists like `/as info all` AreaShop will use pages, with buttons you can click to go to the next/previous page.
  - A lot of buttons on the region info page, to rent/unrent, remove friends or teleport to the region
  - All these messages are defined in the messages file (EN.yml), so you can also edit and [use these yourself](https://github.com/NLthijs48/AreaShop/wiki/Language-support#message-format-specifications).
  - Got any suggestions for adding more buttons like these? Please let me know.
  - Can be turned off by `useFancyMessages` in `config.yml` (no idea why you would want to do that though).
- Improvements to inserting messages into other messages, for example passing along variables: `%lang:action|My action text|%`.
- `/as help` now respects the `areashop.help` permission.
- Add an `ALL` state for flagProfiles to set flags for all states (like `priority`).
- By default players need to confirm selling/unrenting when using signs, to prevent accidentely losing your region and possibly the contents of it (you can also easily turn it on for renting/buying in the `signProfiles` section in `config.yml`).
- Add compatibility for the upcoming WorldGuard version (setting flags of regions changed).

**Technical:**
- Add redunded money to the `UnrentedRegionEvent` and `SoldRegionEvent`.
- Add events for adding/removing friends: `AddFriendEvent` and `DeleteFriendEvent`, both can be cancelled.
- Move all code to the `me.wiefferink` package, since I intent on keeping this domain, and will not have `nl.evolutioncoding` for long anymore.
- Add an issue template to the repository so that creating Github issues has placeholder text and directions on what information to provide.
- Cleanup the config file by making all timing settings hidden.
- Improve the Maven setup by adding options to move plugin output to a directory, produce a jar with sources and produce Javadoc
- Add a [build server](http://wiefferink.me:8080/job/AreaShop/) where you can find the latest **in development** AreaShop version.
- Setup a Javadoc site: [https://wiefferink.me/AreaShop/javadocs/](https://wiefferink.me/AreaShop/javadocs/), automatically updated by the Jenkins build server.

**Fixes:**
- Fix problems with detecting most important region when placing a sign (problem showed up when using child/parent system of WorldGuard)

***

## AreaShop V2.2.2
**Additions:**
* Add full 1.9 compatibility (only stats reporting and some tab completions were broken, all other functionality was functional already).
* Add feature that lets players rent a region at least to the maximum rental time (before they could not extend before the region had `max-duration` time left). Can be turned off in the `default.yml` file with `extendToFullWhenAboveMaxRentTime`.
* Add event system:
  - 'ask' type events that can be cancelled, broadcasted before the action happens.
  - 'notify' type events that cannot be cancelled, but only let you know that it has happened.
  - Events for renting (with extending flag), unrenting, buying, selling, reselling, adding and removing.
  - General 'RegionUpdateEvent' to listen to as display service (update signs, update regions flags, etc).
  - Can be used to write addons for AreaShop.
  - Is currently only used for updating signs and regions, but other parts of AreaShop will also be using it soon.
* Add `areashop.linksigns` permissions to the `areashop.*` permissions group.
* Add priority, interact and build flag to the default `config.yml` file, helps to reduce support for issues with this.
* Add `middle` and number options to the `teleportLocationY` setting in `default.yml`.
* Add variable support for all language messages that can support it, which means where a region is known.
* Add Czech language, translated by Fractival.

**Fixes:**
* Fix last active time of players sometimes not updating correctly, influences inactive unrent/sell feature.
* Fix message variables for `/as info player <player>`.
* Fix the `n:<name>` option for member/owner region flags (the `n:` was not stripped away before adding them).

***

## AreaShop V2.2.1
* Added a setting to deny extending rental regions when the player is above his limits (if the player cannot rent the region back when he unrents it then it he is considered to be above his limits). Setting `allowRegionExtendsWhenAboveLimits` can be found in `config.yml` and is by default set at `false`, this modifies the behaviour of AreaShop compared to the previous version, but this is probably something that users already expect to happen.
* Fixed a bug with `/as tp` sometimes teleporting to the region instead of a the sign when a player has certain permissions.
* Moved all documentation to GitHub, as you probably have already seen.
* Added permission `areashop.buyresell` and `areashop.buynormal` to be able to prevent players from buying regions that are in reselling mode, or regions that are not in reselling mode.
* Fixed `/as find` without balance argument using a maximum of zero instead of the balance of the player + formatting of the price will now use the proper currency formatting instead of a plain number.
* Fixed `/as info` trying to filter by group for a couple of sub commands when it should not.
* Fixed `/as info region <region>` always showing a line specifying the landlord, even when there is not one (it would show `<UNKNOWN>`).
* Added Vault jar file to the repository to make compiling easier, only cloning + running with Maven is enough now.
* Improved handling of failed file loading for all config and region files, better messages in console for when it happens.
* Remove `lastActive` line from region files when unrented/sold, nothing serious, just cleanup.
* Fixed sign click actions still executing even when the Bukkit event is cancelled.
* Breaking signs that are connected to regions will not longer disconnect it, but instead make it float.
* Made removing regions from AreaShop less destructible for region flags, only greeting and farewell are removed to avoid confusion (this helps when you accidentally add wrong regions).
* Fixed support for the seconds modifiers when using `/as setduration`.
* Added support for weeks modifiers for specifying time durations.
* Cleaned old Dutch time modifiers from `config.yml`, **Warning: This will break existing signs with these modifiers if not re-added in the config**. Regions using incorrect time specifiers will be listed in the console/log at startup.
* Unrenting/selling a region that has a landlord will now get the money from the landlord instead of introducing new money. If the landlord does not have enough money then the user will get nothing back.
* Change the default sign tags to `[asrent]` and `[asbuy]`, this prevents clashes with for example Essentials. This will not impact already linked signs, only creating new signs and the tags can still be easily changed in the config.

***

## AreaShop V2.2.0
* Change to a module based Maven project to support dependencies with multiple versions:
	* 1 .jar file for AreaShop, no different files for certain dependencies anymore.
	* Support for WorldGuard 5 and WorldGuard 6.
	* Support for WorldEdit 5 and WorldEdit 6.
	* Setup to easily add support for more version if breaking changes in dependencies happen.
* Add support for the new schematic save/restore classes of WorldEdit (supports entities).
* Added the `/as stack` command to easily create and register a lot of regions, syntax:
	* /as stack <amount> <gap> <name> <rent|buy> [group]
        * `<amount>` Number of regions that will be created
        * `<gap>` Blocks distance between the regions
        * `<name>` Name of the regions (number will be behind it)
        * `<rent|buy>` The type of the region
        * `[group]` A group to add the created regions to
        * The regions will be created in the direction you are facing
	* Don't be afraid to create 100+ regions with this command, it will do a certain number per tick, and therefore not lag/crash the server.
	* Setting `stackRegionNumberLength` to config.yml to indicate how long the numbers appended to the base region name should at least be (generate names like this: `region-001`, `region-014`, `region-4567`).
* Make region loading non-descructive:
  Regions will never be removed again by AreaShop itself. If the world or WorldGuard region is missing that is required for the functions of the region then it will simply say so in the console. This will make sure that regions are not removed when you have trouble loading your world or your WorldGuard regions.
* Make the update checker async, also when it cannot reach BukkitDev to check for updates it does not slow down the start of the server.
* Fix `/as setowner` not saving the region file, and therefore losing changes when restarting the server.
* Change Vault integration to support custom (not build into Vault) economies. This also enables support to change economies while the server is running.
* Fix a bug with teleporting to regions, and add a message when the teleport changes from sign to region or the other way around (this happens when the user does not have permission for one of them, but has for the other).
* Remove the `enableSchematics` setting from the config file, this setting is already in default.yml and only caused confusion.
* Added landlord functionality:
	* Set the landlord of a region with `/as setlandlord <player>`.
	* Set the landlord in default.yml or groups by UUID or name.
	* All revenue of a region (buy/rental) will go to the landlord.
	* Permissions `areashop.createrent.owner`, `areashop.createrent.member`, `areashop.createbuy.owner` and `areashop.createbuy.member` to enable users to add existing WorldGuard regions to AreaShop if they are already owner/member (they are automatically assigned as landlord when they add regions).
	* Permissions `areashop.destroybuy.landlord` and `areashop.destroyrent.landlord` to give landlords the permission to remove their region from AreaShop.
	* Permission `areashop.setprice.landlord` to let landlords change the price of their region.
	* Permission `areashop.setduration.landlord` to let landlords change the duration of their region.
	* Permission `areashop.landlord.*` to give all landlord permissions to a player (all permissions above).
* Added arguments `default` and `reset` to `/as setprice` and `/as setduration` to remove the setting from the region file (then it inherits it again from the groups or default.yml).
* Support Metric prefixes (https://en.wikipedia.org/wiki/Metric_prefix) to display large numbers nicely. This means you can display numbers like `1000000` as `1.00G` instead. Has options to indicate from which number prefixes should be used.
* Added a decimalMark setting to config.yml to change the character used to indicate the fractional part of numbers (change from dot to comma for example).
* Changed all time period settings in config files to a readable format like the rental times. Old values will still be read correctly, and should work exactly like in the old version.
* Make sure regions are not unnecessary saved directly after loading them at server startup.
* Fixed an issue with `/as addfriend` denying you to add players that just joined for the first time.
* Fix a problem with depositing money to players that never joined the server (or for which there is no player.dat file anymore).
* Add date and time variables for all config settings (can be used to save schematics as backup, creating a new one each time instead of overwriting).
* Add fallback system for owners and landlords, if the UUID has no correct name within the Bukkit API it will use the cached name. This makes sure that these people can still be paid through Vault.

***

## AreaShop V2.1.5
* Changed all translations to have a period at the end of sentences.
* Added a Chinese (Taiwan) language file
* Added a blacklist option in the config to block certain regions from being added to AreaShop (you can provide a list of region names with regular expressions) by default only `_global_` is on the list. Regular expressions make it possible to for example block all regions with a certain pattern.
* Added a setting `findTeleportToSign` to change if `/as find` should teleport you to the first sign of the region or to the `/as settp` spot (with fallback to the middle of the region).
* Fixed a small bug that would cause you to get teleported to the fallback spot instead of the `/as settp` one when you try to teleport to a sign but don't have permission for it (AreaShop changes your request to a non-sign teleport if you do have permission for that in that case)
* Added an extra option to `/as info` to list all regions that do not have a group, use `/as info nogroup`.
* Added name change detection on login of the player (checks if the players name matches the saved name for the regions that are owned by the players, works because getting regions happens with the UUID) and if it changed it will update the region flags and signs.
* Changed that now all region flags and signs will be updated when starting the server, that way users don't think that something does not work simply because the region is not updated. This can be turned off by setting `updateRegionsOnStartup` to false in the config (updating the region happens slowly so it should not cause any noticeable lag).
* Removed the `/as updaterents` and `/as updatebuys` commands, became redundant because `/as reload` also updates all regions now so that can be used instead.
* Fixed translation of color codes for greeting/farewell messages for WorldGuard 6.0+ users (this problem was caused by an unintended breaking change by WorldGuard, check [this ticket](http://dev.bukkit.org/bukkit-plugins/regionbuyandrent/tickets/26-color-issue/) for details).

***

## AreaShop V2.1.4
* Fixed a bug with regions that are in reselling mode, they will now correctly check if you have enough money to pay the resell price instead of checking if you have enough money for the normal price.
* Added a line to the `/as info region` output for regions that are in resell mode which displays the resell price.
* Changed `/as find` to first search all matching regions (rent/buy, maxprice and group) and after that select a random region from that list.

***

## AreaShop V2.1.3
* Splitted into 2 versions now, 1 for WorldGuard 5.9 and 1 for WorldGuard 6.x+ Use the correct version that matches the WorldGuard version you have installed.

***

## AreaShop V2.1.2
* Fixed a bug with the `/as settp` command not accepting arguments, it would always try to find regions at your location instead of first checking if you give it a region name.
* Fixed a bug with inactive player unrent/sell settings. If the `inactiveTimeUntilUnrent` or `inactiveTimeUntilSell` settings would be set to a value above 35000 it would cause an integer overflow, resulting in a 50% chance that all regions will be unrentend/sold (and if that does not happen it will still sell/unrent them too early).
* Fixed a bug with periodic sign updating crashing when a region has 0 signs.
* Add an update checker, will check BukkitDev for the latest file and print a message in console if an update has been found. Also notifies OPs + players with the `areashop.notifyupdate` permission. It will never download the latest version, because those might require a backup or manual changes to run properly.
* Fixed some possible bugs with getting the list of players that are added as friend to a region.
* --Restored compatibility with WorldGuard 5.9 (WorldGuard 6.0+ is still recommended because that one properly supports UUID's, so 5.9 would cause problems with name changing) and WorldEdit 5.6.3 (could probably already be used with previous AreaShop versions)-- Broken in this version, fixed in V2.1.3 by releasing 2 jar files.
* The French language file has been corrected at some points.
* The German language file has been completed and corrected at some points
* The Polish language file has been improved slightly, but is not 100% complete yet (can you help translating? Check [this page](http://dev.bukkit.org/bukkit-plugins/regionbuyandrent/pages/schematics-languages/#w-option-2))

***


## AreaShop V2.1.1
* Fixed a bug with setting the `owners` flag through the flagProfiles section. (previously it would split the list behind owners into different entries using ` ` as separator, but that should be `, ` this has been fixed now)
* Fixed a bug that cause the "conversion to a new version of the file format starts, could take some time" message to always appear after reload/restart of the server. It would not actually do anything to the files, but would print the message.
* Fixed the unnecessary saving of the groups.yml file, if loaded from disk it would automatically mark the file as changed, causing it to save at the next iteration of the save task.
* Fixed a bug that caused the region flags and signs to update for each member that is added (this is unnecessary when loading the groups from disk, commands still update signs etc properly). Now it will update after all the members of a group are added, and this update is spread over multiple ticks like the `/as updaterents` command
* Updated the French language file to the latest version that is made at [Transifex](https://www.transifex.com/projects/p/areashop/), it has been fully updated by Yaelknown.

***

## AreaShop V2.1.0
#### Features added/changed
* Changed the `/as info region` command to show more relevant information:
![New /as info layout](https://camo.githubusercontent.com/955823568bd47790d56f50ed0c24af8f65640356/687474703a2f2f6465762e62756b6b69742e6f72672f6d656469612f696d616765732f37392f3534372f53637265656e73686f745f312e706e67)

	These are almost all things that could show there, some lines will not show up when based on config options (extending, in advance renting, restoring), some don't show up if empty (friends, signs) and some don't show up based on permissions (groups, teleport location, restoring).
* Adding friends to your region is now possible with `/as addfriend <player> [region]` (delete with `/as delfriend <player> [region]`), by default these players will also be added as member of the region, this can however be changed in the `flagProfiles` section of config.yml. By default friends can also teleport to the regions they are added to, deny permission `areashop.teleportfriend` to stop this.
* Automatic unrenting for players that are inactive for a certain period of time (`inactiveTimeUntilUnrent` and `inactiveTimeUntilSell` in default.yml, so this can also be changed for groups of regions or individual ones) OPs are excluded by default, those regions will not be unrented/sold.
* Added a argument to `/as find` to specify a group, it will only search for regions in that group if specified. Could be useful to create command signs and let players automatically get a region that is at a certain place.
* Added a command `/as me`, which lists all your rent and buy regions, plus regions you are added as friend.
* Added a `/as setowner <player> [region]` command to change the owner of a region without triggering events for commands, schematics or money transactions (using this command on a rent region with the player that already rents it will extend the region by 1 time period).
* Added a `/as resell <price> [region]` command to let players put their bought region into resell mode. If they do that the sign will indicate you can buy the region (shows [Resale] at the top), but the owner can still do anything like normal. When another player buys the region then the money will get transferred to the original owner and the new player will be owner (then the sign shows [Sold] again). If a player changed his mind and does not want to resell his region anymore he can use `/as resell`.
* Added a tag `%timeleft%` for signs to show the time that a rent has left instead of the end date, AreaShop will update signs that have this every minute by default (check the values in the `signs` section at the bottom of the config to change this)
* All files (config.yml, default.yml, groups.yml, <region>.yml and <lang>.yml) are now loaded in UTF-8 mode, so now they support all fancy characters like € or ê.
* Limitgroups for limiting how much regions a player can buy/rent have changed to be more flexible. Now a `worlds` and `groups` section can be added to a group to let the limits only count for regions in these worlds and groups. This allows for different limits for regions in different world (let players have max 2 plots in creative, while limiting the number of shops in survival to 1) the groups allow you to limit certain type of regions that are in the same world.
* The limitgroup `unlimited` that was normally in the config by default has been removed and now the permission `areashop.limitbypass` allows you to ignore the limits (default to OPs)
* The `areashop.setteleportoutsideregion` permission now works correctly
* Added a setting to specify the maximum time a rent region can be rented in advance: `maxRentTime` in default.yml. This prevents players from renting a region a year in advance, instead they have to extend their rent every now and then.
* Added `areashop.teleportsignall` permission to teleport to signs of region you do not own (default for OPs)
* If a language string contains tags other then `%0%`, `%1%`, `%2%` etc, then all tags for regions can be used (check the config.yml file on the [Config System](http://dev.bukkit.org/bukkit-plugins/regionbuyandrent/pages/the-config-system/) page to see all available tags)
* All places where tags can be used, now also language strings can be inserted. Use `%lang:<key>%`, where <key> is the part before the colon in the language file. All tags that are available in the original string will be available in the inserted language string.
* The default commands that are executed by clicking on signs now use the full `/areashop` instead of `/as` to prevent collisions with commands of other plugins.
* The number of digits behind the decimal point of prices can be changed with the `fractionalNumbers` setting in config.yml, the setting `hideEmptyFractionalPart` determines if the `.0` part of a number should be hidden or not. Default settings produce numbers like this: `$10`, `$5,50`, `$3.45`.
* Fixed a bug with with money withdrawal: If a player is in world A and buys a region in world B the plugin now correctly withdraws money from world B.
* Added a setting to force a user to be in the correct world before he can buy/rent a region: `restrictedToWorld` in default.yml
* Added a setting to force a user to be inside the region before he can buy/rent it: `restrictedToRegion` in default.yml
* Added a warning system for players to indicate their rent is running out, `expirationWarningProfiles` section in the config. Different times can be specifed when a message should be sent and a custom list of commands can be executed (all region tags can be used)
* Added a settting `warningOnLoginTime` to default.yml, when a rent region has less then this time left when the owner of it logs in it will send a warning message to the owner.
* The command `/as rentduration` is renamed to `/as setduration` to be consistent with the other commands
* The flagprofiles changed, owners and members now take a list of UUID's, when setting to a region only these will be set (others are cleared). It is now possible to add groups to owners/members by using `g:<group>` in the list, this does the same as `/region addmember <region> g:<group>`. It is now possible to specify the group a flag is about, use `g:<members|non_members|owners|non_owners|all>`, can for example be used to only let members of a region walk into it.
* If a schematic is wrongly sized compared to the region size AreaShop will print a warning in the console (but it will still restore it)

#### Performance improvements
* Operations that change/check multiple regions have been changed to do this spread over multiple ticks (now it does a certain number of regions per second instead of all at once, prevents lag spikes for if you have a lot of regions). At the bottom of the config is a section `Limits and Timings` where the speed of these tasks can be changed. The following actions have been changed to this new system (NEW indicates that this is added for a new feature of this update):
	* Checking expiration of rent regions
	* Updating of regions when using `/as updaterents` or `/as updatebuys`
	* Checking of regions for players that are inactive for too long (NEW)
	* Periodic sign updates of regions for when it has tags like `%timeleft%` (NEW)
	* Saving region files, now regions only get saved every 10 minutes now by default. Only regions that actually changed will be saved, when shutting down the server it will also safe the regions to prevent data loss. This task also handles saving the groups.yml file (also only saved when necessary) and saving of WorldGuard regions (uses the saveChanged() method of WorldGuard to only save regions that require it).
	* Checking for when to send warning about rent expiration to players (NEW)

***

## AreaShop V2.0.2
* Adds compatibility with WorldGuard 6, but does not work with WorldGuard 5 anymore
* Fixes a bug with upgrading from a lower version. With V2.0.0 and V2.0.1 it could happen that the plugin does not update correctly from a lower version, the config.yml would not be moved to the `#old` folder preventing AreaShop to generate a new config. Then the plugin would load the old config and deny users to rent/buy regions because the limits are set incorrectly.
* Fixes the incompatibility of UTF-8 characters with the language files, now characters like é, ê, etc. will show up correctly in-game.
* Adds a fully updated French language file to the plugin, thanks BlaZingHope for translating, check [the ticket](http://dev.bukkit.org/bukkit-plugins/regionbuyandrent/tickets/5-language-add-a-language/).

***

## AreaShop V2.0.1
* Moves setting to determine y location of teleport position to default.yml/groups/region files
* Adds setting to specify if teleporting should be inside the region or not (default.yml/groups/region files)
* Adds a setting to the config.yml to specify the maximum number of tries the safe teleport function can do (default 50000)
* The teleport function now functions properly with locations close to build limit or below 0 (no more falling from the sky).

***

## AreaShop V2.0.0
#### WARNING
Before updating to this version of the plugin make a backup of all worlds that contain regions registered in this plugin!
After updating from a previous version the old config.yml will be placed in the folder `#old` because the config changed 
a lot, you can copy the old settings to the new config, just put them in the right position. In the `#old` folder are also
the necessary files to revert back to version 1.X of AreaShop.

#### Feature changes
Note: sections in the config ending with `Profile` can be assigned to region with the inheritance system. You can create new profiles by copying an existing one and changing the values.
Parts you don't need can usually be left out.
* Adds a permission to set the teleport location outside the WorldGuard region: areashop.setteleportoutsideregion (given to OPs by default)
* If you remove a rent or buy region all the flags of the WorldGuard region will be reset
* Adds extra checks for teleportation, now also check blocks around you so now a 3x3x4 shape will be checked for danger
* Adds a setting in the config to specify what the default y coordinate should be for teleporting, setting `teleportLocationY`, values: bottom, middle, top
* You can specify in the config if AreaShop messages in the console should be in color or not
* Adds support for different limits for players by using permissions, declare limit groups in the section `limitGroups` and then give players the permission
 `areashop.limits.<group>`, replacing <group> with the name of the group (if a player has multiple groups then highest limit will be used, by default they have group `default` and OPs have `unlimited`)
* Adds full customization of signs, in the section `signProfiles` all lines of a sign can be changed and also the commands to execute when a players right/left clicks a sign (multiple commands can be used, it is a list). Tags are provided to insert region all region data (check config)
* Adds support for multiple signs for one region, for every sign you can set the profile (profiles specified in the config), if not set the profile will be inherited from the region/groups/default.yml
 signs can be added by `as addsign` or placing a sign with the first line `[as]`, second line the region (optional if sign is inside region) and third line the profile (optional)
* Adds support to execute commands at certain events (created, deleted, rented, extended, unrented, bought, sold) check config section `eventCommandProfiles`
* Flag settings are now by profile (check `flagProfiles`), so regions can have different flags set (for example a different greeting)
* Adds a setting to limit the maximum number of times a rent can be extended (check default.yml/groups/region files)

#### Command changes
* Adds tab completion for all commands if you are OP or have the permission areashop.tabcomplete
* Adds color and format support to greeting and farewell messages (use the normal bukkit formatting codes)
* Adds a command to find an available rent/buy with an optional maximumprice (with no maxprice your balance will be used as max), /as find <buy|rent> [maxprice]
* Adds extra optional parameter `sign` to `as tp`, with that parameter you will teleport to the (first) sign instead of the region
* With a lot of player command you don't have to specify a region anymore, just stand in the region and the plugin will detect it
* Merges commands `as rentrestore` and `as buyrestore` into `as setrestore`
* Merges commands `as buyprice` and `as rentprice` into `as setprice` 
* Adds `as groupadd` command to add regions to groups. Rents and buys can be mixed, region can be specified or taken from your WorldEdit selection (adds them all), group will be created if it has no members yet
* Adds `as groupdel` command to delete regions from groups, region can be specified or taken from your WorldEdit selection (removes them all), if a groups is empty it will be removed
* Adds `as grouplist` command that lists all groups registered
* Adds `as groupinfo` command that displays members of the specified group
* Adds `as add` command to add a region or all regions inside your WorldEdit selection (no signs will be added)
* Adds `as del` command to delete a region from AreaShop or all inside your WorldEdit selection (signs will be removed)
* Adds `as addsign` command to add the sign you are looking at (max 100 blocks away) to the specified region (with optionally a profile)
* Adds `as delsign` command to delete the sign you are looking at from the region it belongs to
* Adds `as schemevent` command to manually trigger a schematic event

#### The new config system
The config system has drastically changed and an inheritance system has been added to support global, group and individual settings.
Now the config only contains global settings and profiles, these profiles can be assigned with the inheritance system.
A file called `default.yml` will appear, this file contains all settings that can be changed globally, in a group or in a individual region.
When you create a group with the build-in commands a file `groups.yml` will appear, this contains the groups and members of the groups.
Each region has its own file inside the folder `/regions` located inside the AreaShop folder, this file contains the individual settings.
When checking a setting, for example `price`, the plugin first checks if it has been set in the region file, then it checks 
all groups assigned to the region for the setting (the one with highest priority will apply, or otherwise the first one with the setting).
If it still did not find the setting it gets it from `default.yml`. All settings in `default.yml` can be used in group files and region files,
just add them to the file, to for example set the price for a group with buy regions you add the setting `price` inside the section `buy`.

***

## AreaShop V1.3.0
* Adds support for name changing, all files will be converted to UUID's on startup. It will create files `rents.old` and `buys.old` as backup for if the conversion goes wrong, you can delete these after you are sure everything works
* Adds version system for all files, this is for converting to next version and is just a `behind the scenes` thing, a `versions` file will appear in the datafolder of the plugin
* Adds a command to teleport to regions, normal players can only teleport to their bought/rented regions (can be changed with permissions) ops can teleport to all rent/buy regions. Teleportation is safe, the plugin tries to find a safe spot within the region.
* Adds a command to set the teleport location, normal players can only set it for their bought/rented regions (can be changed with permissions) ops can change it for all regions. If no location is set the teleport command will try to teleport to the bottom middle of the region (and then checking a cube of increasing size around that while it is not safe).
* Fixes problem with Multiverse-Core, if Multiverse would load after AreaShop then all rents/buys in other worlds then `world`, `world_nether` and `world_the_end` will deregister. Now AreaShop softdepends on Multiverse-Core so if Multiverse is on the server it should load before AreaShop.
* Fixes problem with the flags sections in the config, it was not possible to reset flag with the previous way of handling the flags. If you put no value behind a flag or `none` it will now reset.
* Fixes bug with percentage of money back after buying/renting not working, only 100% and 0% were possible before, now it works normally.
* Refactored the commands system, adding commands and maintenance should be easier now. Also tab completion would be possible now, leave a comment if you would like to have that.

***

## AreaShop V1.2.1
* Fixes bugs with deleting regions and worlds (plugin would not start correctly)
* Adds support for automatically using the region you place the sign in when you leave the second line empty (looks at priority and parent/child relations)

***

## AreaShop V1.2.0
* Fixed a bug with accessing the Vault api to early (plugin does not load in rare cases)
* Added option to set `moneyCharacterAfter`, instead of $10 you can create something like `10 gold` now.
* Added statistics, using Metrics, can be disabled in the config (check http://mcstats.org/|http://mcstats.org/)
* Added commands to change the price of buy and rent regions. `/as rentprice` and `/as buyprice`
* Added command to change the duration of a rent region. `/as rentduration`
* Added config option to change the timeformat that is used on the signs and in the chat
* Added config options to change the identifiers used for rent regions (minutes, hours, days, months, years) This also fixes that `M` did not work as month, but as minute.

***

## AreaShop V1.1.2
* Small bugfix for restoring, the `rented` restore event was also firing when you extended your rent, possibly removing all your items within the region.
* The included language file for Dutch (NL.yml) should work now.

***

## AreaShop V1.1
* This release adds a very configurable saving and restoring of regions. You can let the plugin save and restore the region from/to a .schematic file on certain events. Commands are added to change the restoring profile for a rent/buy and the state (true, false, general) which are respectively enabled, disabled and `as general config option`. Check the [[http://dev.bukkit.org/bukkit-plugins/regionbuyandrent/pages/default-config/|Config page]] for which options you are missing or let the plugin generate a new one for you. Also some commands are merged to make it simpler.

***

## AreaShop V1.0
* First public release of the plugin, contains language support, commands, sign interaction and a config.
