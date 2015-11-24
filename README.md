![AreaShop logo](https://cloud.githubusercontent.com/assets/6951068/9471294/f016d8ee-4b4f-11e5-9bda-d61b1c423ebb.png)<br/>
**Usage and configuration:**
[►Download (releases)](https://github.com/NLthijs48/AreaShop/releases)&nbsp;&nbsp;
[►Commands and Permissions](https://github.com/NLthijs48/AreaShop/wiki/Commands-and-Permissions)
<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
[►Basic regions setup](https://github.com/NLthijs48/AreaShop/wiki/Basic-regions-setup)&nbsp;&nbsp;
[►Advanced regions setup](https://github.com/NLthijs48/AreaShop/wiki/Advanced-regions-setup)&nbsp;&nbsp;
[►Configuration files](https://github.com/NLthijs48/AreaShop/wiki/The-config-system)<br/>
**Advanced features:**
[►Save/restore region blocks](https://github.com/NLthijs48/AreaShop/wiki/Region-blocks-save-restore)&nbsp;&nbsp;
[►Change the language](https://github.com/NLthijs48/AreaShop/wiki/Language-support)&nbsp;&nbsp;
[►Limitgroups](https://github.com/NLthijs48/AreaShop/wiki/Limitgroups-information-and-examples)<br/>
**Troubleshooting:**
[►Frequently Asked Questions](https://github.com/NLthijs48/AreaShop/wiki/Frequently-Asked-Questions)&nbsp;&nbsp;
[►Common errors](https://github.com/NLthijs48/AreaShop/wiki/Common-errors)<br/>
**Support:**
[►Ask question](https://github.com/NLthijs48/AreaShop/issues/new?body=Indicate%20the%20steps%20you%20did%20while%20encountering%20the%20problem,%20the%20expected%20outcome,%20and%20the%20actual%20outcome.%20Provide%20links%20to%20your%20config.yml,%20default.yml%20and%20groups.yml%20files%20(use%20pastebin.com%20or%20similar).%20Also%20provide%20a%20full%20startup%20log%20of%20your%20server%20(this%20indicates%20the%20plugin%20versions%20used).&labels=Support)&nbsp;&nbsp;
[►Request feature](https://github.com/NLthijs48/AreaShop/issues/new?body=Describe%20the%20feature%20with%20as%20much%20detail%20as%20possible:%20command,%20permissions,%20messages%20in%20chat,%20etc.%20Also%20indicate%20for%20which%20situations%20you%20would%20use%20it%20and%20why%20it%20should%20be%20added.&labels=Feature)&nbsp;&nbsp;
[►Planned features](https://github.com/NLthijs48/AreaShop/labels/Feature)&nbsp;&nbsp;
[►Report bug](https://github.com/NLthijs48/AreaShop/issues/new?body=Indicate%20the%20steps%20you%20did%20while%20encountering%20the%20problem,%20the%20expected%20outcome,%20and%20the%20actual%20outcome.%20Provide%20links%20to%20your%20config.yml,%20default.yml%20and%20groups.yml%20files%20(use%20pastebin.com%20or%20similar).%20Also%20provide%20a%20full%20startup%20log%20of%20your%20server%20(this%20indicates%20the%20plugin%20versions%20used).&labels=Bug)&nbsp;&nbsp;
[►Open bugs](https://github.com/NLthijs48/AreaShop/labels/Bug)<br/>
**Development:**
[►Changelog](https://github.com/NLthijs48/AreaShop/wiki/Changelog)&nbsp;&nbsp;
[►Compiling](https://github.com/NLthijs48/AreaShop/wiki/Compiling-AreaShop)&nbsp;&nbsp;
[►Modules, packages and classes overview](https://github.com/NLthijs48/AreaShop/wiki/Modules,-packages-and-classes-overview)<br/>
**Connections:**
[►AreaShop in Spigot Resources](http://www.spigotmc.org/resources/areashop.2991/)&nbsp;&nbsp;
[►AreaShop on BukkitDev](http://dev.bukkit.org/bukkit-plugins/regionbuyandrent/)&nbsp;&nbsp;
[►Dev builds on Jenkins](http://ci.frostcast.net/job/AreaShop/)

***

This plugin is for giving players the option to rent or buy a region. It could be used to let them rent a jail in your Prison server or maybe a shop in the market of the Survival server. The player interacts with signs, making it easy to use. It also has a lot of commands to check the status of all regions, manage the renting and buying of a region and also features for admins. Also the plugin is capable of saving the region and restoring it later, this is very flexible and can be set general and per region, check the [config](https://github.com/NLthijs48/AreaShop/wiki/The-config-system) for more information.

### All features in a list
* Rent and sell regions to players + Players can resell their bought regions to other players.
* [Signs](https://github.com/NLthijs48/AreaShop/wiki/Basic-regions-setup) for easy interacting and current status.
* Change the [language](https://github.com/NLthijs48/AreaShop/wiki/Language-support) of the plugin or use of of the already provided language files (check [here](https://github.com/NLthijs48/AreaShop/tree/master/AreaShop/src/main/resources/lang) for supported languages, these are already packed with the plugin, just change the language setting in `config.yml`).
* Automatically let the plugin [restore](https://github.com/NLthijs48/AreaShop/wiki/Region-blocks-save-restore) the region with schematics.
* Change which [commands](https://github.com/NLthijs48/AreaShop/wiki/Commands-and-Permissions) players can use with [permissions](https://github.com/NLthijs48/AreaShop/wiki/Commands-and-Permissions).
* Customize the plugin by changing the [config files](https://github.com/NLthijs48/AreaShop/wiki/The-config-system).
* Automatically place the region name on the sign if it is placed inside a region.
* Teleport to rent/buy regions and optionally set the teleport location (teleporting is safe and within the region).
* Adding friends to regions (which also can teleport then).
* Automatic unrent/sell for regions of which the owner is offline for a certain time.
* Warning to players when their rent is about to run out (at login and while they are online).
* [Group system](https://github.com/NLthijs48/AreaShop/wiki/The-config-system) to set options for a couple of regions instead of all of them.
* All heavy tasks are spread over time (each tick a part is executed until done), so the plugin should not cause any lag.
* Rent/buy limits can be different per permission node (player group), world or group of regions (possible situation: Normal players can buy 1 market region in survival + 1 build region in survival and 2 plots in creative, while VIPs have double limits for all those), [check these examples](https://github.com/NLthijs48/AreaShop/wiki/Limitgroups-information-and-examples).
* Supports name changes because of saving player info by UUID, for more details check the FAQ entry: [What happens when a player changes his name?](https://github.com/NLthijs48/AreaShop/wiki/Frequently-Asked-Questions#what-happens-when-a-player-changes-his-name).

You need to have WorldEdit, WorldGuard and Vault installed on your server, WorldGuard is used for creating and managing regions, WorldEdit for the saving and restoring the blocks in regions and Vault is used to let players pay for the regions.

### Tutorial & Feature overview (AreaShop V2.0.1)
**Made by [Koz4Christ](https://www.youtube.com/user/koz4christ)**<br/>
[![Tutorial Video](https://cloud.githubusercontent.com/assets/6951068/9532789/152c33f8-4d0e-11e5-8d1c-9e80c19ceab8.png)](https://www.youtube.com/watch?v=328WrStVkzs)

### Prison cell setup tutorial (AreaShop V2.1.0)
**Made by [PerkulatorTime](https://www.youtube.com/user/PerkulatorTime)**<br/>
[![Tutorial Video](https://cloud.githubusercontent.com/assets/6951068/9532788/147526cc-4d0e-11e5-9672-1274faae280a.png)](https://www.youtube.com/watch?v=OQOsOG-EdNc)

Old video for AreaShop v1.0: [Tutorial by VariationVault](https://www.youtube.com/watch?v=k2HMCxCCOYo)

### Required dependencies
* Java 7 or higher (latest recommended)
* Bukkit/Spigot 1.7.9 or higher (modded servers often include Bukkit/Spigot support, so it should also work on that, but it is not specifically tested for it)
* [WorldGuard](http://dev.bukkit.org/bukkit-plugins/worldguard/): 5.9 or higher (6.0+ recommended)
* [WorldEdit](http://dev.bukkit.org/bukkit-plugins/worldedit/): 5.6.3 or higher (6.0+ recommended)
* [Vault](http://dev.bukkit.org/bukkit-plugins/vault/): 1.4.1 or higher
* An economy plugin supported by Vault (check the [Vault page](http://dev.bukkit.org/bukkit-plugins/vault/) for a list of these)

### Metrics
This plugin utilizes Hidendra's plugin metrics system, which means that the following information is collected and sent to mcstats.org:

*A unique identifier, the server's version of Java, whether the server is in offline or online mode, the plugin's version, the server's version, the OS version/name and architecture, the core count for the CPU, the number of players online, the Metrics version.*

This information will give me an indication how much the plugin is used and encourages me to continue development. Opting out of this service can be done by setting `sendStats` in the config of this plugin to `false`, if you want to disable Metrics for any plugin go to `plugins/Plugin Metrics/config.yml` and change `opt-out` to `true`. Check graphs of the statistics at this page: [mcstats.org/plugin/AreaShop](http://mcstats.org/plugin/AreaShop)

### Do you want to translate AreaShop?
Help translating the plugin into different languages and to keep the translations up to date. Go to [this page](https://github.com/NLthijs48/AreaShop/wiki/Language-support#translating-on-transifex) for more information. Translating goes through the [Transifex](https://www.transifex.com/projects/p/areashop/) website and is very easy, sending files back and forth is not needed, you can translate lines online and whenever you want. Every line you translate will be saved, no need to do it all at once (this also allows for easy collaboration).

[![Donate through PayPal to support development](https://www.paypal.com/en_US/i/btn/btn_donate_LG.gif)](https://www.paypal.com/cgi-bin/webscr?return=http%3A%2F%2Fdev.bukkit.org%2Fbukkit-plugins%2Fregionbuyandrent%2F&cn=Add+special+instructions+to+the+addon+author%28s%29&business=nlthijs48%40gmail.com&bn=PP-DonationsBF%3Abtn_donateCC_LG.gif%3ANonHosted&cancel_return=http%3A%2F%2Fdev.bukkit.org%2Fbukkit-plugins%2Fregionbuyandrent%2F&lc=US&item_name=AreaShop+%28from+Bukkit.org%29&cmd=_donations&rm=1&no_shipping=1&currency_code=USD)
