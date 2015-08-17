#AreaShop

Check the documentation at http://dev.bukkit.org/bukkit-plugins/regionbuyandrent/

Every time I make a change to the code that is somewhat finished and possibly tested I will push the code to GitHub, this way you can follow the commit log to see the progress.

# Compiling AreaShop
[Check this wiki page](https://github.com/NLthijs48/AreaShop/wiki/Compiling-AreaShop)

#### Questions
If you have any questions about how to use AreaShop or how to change the code of AreaShop then feel free to message me. You can reach me through [BukkitDev](http://dev.bukkit.org/profiles/NLThijs48/) or [Spigot](http://www.spigotmc.org/members/nlthijs48.15658/).

# Development tips
Below you can find a basic overview of the structure of AreaShop, this can help you understand the plugin and make it easier to change something.

#### Description of packages and classes
The following packages and classes can be found inside the AreaShop plugin:

**nl.evolutioncoding.areashop** (general classes):
- **AreaShop.java**: Base class of the plugin, handles the startup and shutdown sequence, registers tasks, has useful methods to get dependancies or send messages and also contains a lot of constants.
- **Metrics.java**: Class from MCStats, used to send some anonymous statistics to see how many people use the plugin. Check the results [here](http://mcstats.org/plugin/AreaShop).
- **Updater.java**: Updater class to notify players about new updates, also contains code to download/install updates automatically but that is a feature of the library that is unused.
- **Utils.java**: Contains a few methods that might be useful, currently the conversion from Location to config string and the other way around, creating a comma separated list and getting a Facing from a yaw.

**nl.evolutioncoding.areashop.commands** (command classes):
- **CommandAreaShop.java**: Base abstract class for a command.
- **All others**: A command from AreaShop, extends the CommandAreaShop class and implements the required methods (command start, execution, help entry, tabcomplete).

**nl.evolutioncoding.areashop.exceptions** (exceptions):
- **RegionCreateException.java**: An exception that is thrown if a region cannot be successfully created (nonexisten world or WorldGuard region).

**nl.evolutioncoding.areashop.listeners** (listeners):
- **PlayerLoginListener.java**: Notifies admins about updates, displays messages about rental regions expiring, handles name changes.
- **SignBreakListener.java**: Handles breaking AreaShop signs, blocks it when the user does not have permission, removes the sign if they do have permission.
- **SignChangeListener.java**: Handles placing AreaShop signs, checks if the user has permission and after that creates and adds a region to AreaShop (also triggers a bunch of events that happen when a region is added).
- **SignClickListener.java**: Handles clicking a sign, simply gives the event to the region that the sign is attached to.

**nl.evolutioncoding.areashop.managers** (managers):
- **CommandManager.java**: Entry point for all Bukkit commands, handles commands and tab completions. All command classes are registered here and will be used when a command is received for them.
- **FileManager.java**: Handles all File IO, loads all config files (config.yml, default.yml, groups.yml), all region files and the versions file (used to determine if a file upgrade is required). Also saves all registered region in memory, and has a bunch of methods to get them (get all, get rentals, get buys, get name lists, etc). Manages region groups, these can also be asked for here. Used to add/remove regions, update them (signs and region flags), and has methods to save files (be sure you use the "saveGroupsIsRequired()" function instead of "saveGroupsNow()", files will be saved by a task later or at shutdown, also applies for region saving). Has methods to get a list of regions (AreaShop regions and WorldEdit regions) at a certain location, or those that intersect a WorldEdit selection.
- **LanguageManager.java**: Handles loading and saving of language files. Has methods to get messages from the currently loaded and backup language file (only the selected file and the English one are loaded in memory). If you want to send a message use the "message()" method on AreaShop, that one also handles the sending itself.
- **SignLinkerManager.java**: Handles everyting that has to do with the "/as linksigns" command, acts upon certain events.

**nl.evolutioncoding.areashop.regions** (regions and groups):
- **GeneralRegion.java**: Contains all general methods that a buy and rental region share, also has a couple of enums. Has methods to ask for all properties of a region, and methods to change these properties. Most properties will first be taken from the region file itself, after that from the groups.yml file (if the region is assigned to any groups) and at last from default.yml. If you change a property it will automatically mark the region as "saveRequired", which will make sure that the region is saved to disk the next time the scheduled task comes around or at shutdown. Has commands to teleport players to the region, run events on the region or update the region (signs and region flags). This class should never be contructed, always use BuyRegion or RentRegion. Use "isRentRegion()" and "isBuyRegion()" to determine what an object of this class actually is. 
- **BuyRegion.java**: Class for buy regions, inherits everything from GeneralRegion. Adds a price to the region and adds an owner. Has methods to buy/sell the region.
- **RentRegion.java**: Class for rental regions, inherits everything from GeneralRegion. Adds a price, duration and owner to the region. Has methods to rent/unrent the region (for extending simply rent again).
- **RegionGroup.java**: A regiongroup which can be used to apply certain settings to with the config system. Has a list of regions that belong to it. Has methods to get settings, but these are normally not used (getting settings in a region already takes it from the region, a group or from default.yml).

#### TODO
- Guide for saving custom data for the AreaShop regions
- Starting guide, which methods to use to get regions etc.
- -
