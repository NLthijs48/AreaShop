#AreaShop

Check the documentation at http://dev.bukkit.org/bukkit-plugins/regionbuyandrent/

Every time I make a change to the code that is somewhat finished and possibly tested I will push the code to GitHub, this way you can follow the commit log to see the progress.

# Compiling yourself
Below is some information about how to compile AreaShop, as a normal user you would not need this. You can simply download a stable and tested release here from the [releases page](https://github.com/NLthijs48/AreaShop/releases), the [BukkitDev](http://dev.bukkit.org/bukkit-plugins/regionbuyandrent/) platform or the [Spigot](http://www.spigotmc.org/resources/areashop.2991/) resources section. If you however want to compile a development build yourself (code will be pushed to GitHub after adding something, but an official release is only made after some longer time).

#### Cloning the repository
You can clone the AreaShop repository here from GitHub to your local computer, if you have the GitHub Windows client installed then you can simply click the "Clone in Desktop" button on the bottom right corner fo the page. Otherwise you can use the "git clone" command. If you don't know how to use git or have no interest in using it then you can also simply click "Download ZIP" to download the project.

#### Creating a project
Now you can create a project with your IDE, I use Eclipse myself, so that is what I mainly explain it for (but the same things are possible with other IDEs). If you cloned the git repository then you can create a project based on that, otherwise you can create a new empty Java project and add the files manually.

#### Adding dependencies
First you need to make sure that you use the correct Java version, AreaShop is currently compiled with Java 7, but a higher version is also fine (but make sure that your Minecraft server also has that higher Java version). To compile the current master branch you will need to add the correct dependencies. I'm compiling with the lowest possible versions to make sure that everyone can use AreaShop, but compiling with higher versions is possible if you also have those versions on your server. The versions found below are recommended:
- Bukkit 1.7.9 R0.2 (later versions should work, but might require some small changes in the code)
- [WorldGuard](http://dev.bukkit.org/bukkit-plugins/worldguard/files/) 6.0
- [WorldEdit](http://dev.bukkit.org/bukkit-plugins/worldedit/files/) 6.0
- [Vault](http://dev.bukkit.org/bukkit-plugins/vault/files/) 1.4.1 (later versions should work)

#### Exporting
Now your project should have no errors left (warnings can be ignored usually), if you have any then you will have to fix them or send me a message. In Eclipse you can rightclick your project, then select "Export", a window should pop up. Now select "JAR file" that is located in the "Java" folder and click "Next". Then select the "src" and "lang" folder inside you project, and make sure that "config.yml", "default.yml" and "plugin.yml" are also selected. Also select an output path as export destination, and then click "Finish". Now you should have a .jar file ready to use in your server.

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
