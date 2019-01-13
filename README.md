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
[►Request a feature, report a bug or ask support](https://github.com/NLthijs48/AreaShop/issues/new/choose)&nbsp;&nbsp;
[►Open issues](https://github.com/NLthijs48/AreaShop/issues)<br/>
**Development:**
[►Changelog](https://github.com/NLthijs48/AreaShop/blob/master/changelog.md)&nbsp;&nbsp;
[►Compiling](https://github.com/NLthijs48/AreaShop/wiki/Compiling-AreaShop)&nbsp;&nbsp;
[►Modules/classes overview](https://github.com/NLthijs48/AreaShop/wiki/Modules,-packages-and-classes-overview)
[►Javadocs](https://wiefferink.me/AreaShop/javadocs/)
[►Development builds](http://jenkins.wiefferink.me/job/AreaShop)<br/>
**Connections:**
[►AreaShop in Spigot Resources](http://www.spigotmc.org/resources/areashop.2991/)&nbsp;&nbsp;
[►AreaShop on BukkitDev](http://dev.bukkit.org/bukkit-plugins/regionbuyandrent/)

***

AreaShop allows you selling and renting regions to players. It could be used to let them rent a jail in your prison server, a shop in the market of the survival server or a plot on a creative server. The player interacts with signs, making it easy to use. It also has a lot of commands to check the status of all regions, manage the renting and buying of a region and also features for admins. A lot of messages send to the player can be clicked, for immediately performing actions (buying the region, selling the region, etc.) or getting more information (for example clicking region name for region information). To setup the renting and selling of the regions exactly as you want AreaShop has a lot of options to custimize it to your liking.

### All features in a list
* Rent and sell regions to players, and players can resell their bought regions to other players.
* [Signs](https://github.com/NLthijs48/AreaShop/wiki/Basic-regions-setup) for easy interaction (rent/buy/unrent/sell/information) and current status (layout and actions customizable, multiple signs can be added).
* ![rented-sign](https://cloud.githubusercontent.com/assets/6951068/21939029/3844896a-d9be-11e6-8492-7a23ec71fce2.png)
* Messages that can be clicked for more information and actions ([language](https://github.com/NLthijs48/AreaShop/wiki/Language-support) can be changed, as well as click/hover actions).
* ![region-information-message](https://cloud.githubusercontent.com/assets/6951068/21939161/bff2fe3c-d9be-11e6-802f-4a0bce073c64.png)
* Change the  of the plugin or use of of the already provided language files (check [here](https://github.com/NLthijs48/AreaShop/tree/master/AreaShop/src/main/resources/lang))
* Automatically restore the region to its original state when sold: [restore](https://github.com/NLthijs48/AreaShop/wiki/Region-blocks-save-restore) the region with schematics.
* Change which [commands](https://github.com/NLthijs48/AreaShop/wiki/Commands-and-Permissions) players can use with [permissions](https://github.com/NLthijs48/AreaShop/wiki/Commands-and-Permissions).
* Customize the plugin by changing the [config files](https://github.com/NLthijs48/AreaShop/wiki/The-config-system).
* Use any WorldGuard flags on regions: disable building in the region, deny entry to others, etc.
* Teleport to regions (while making sure the location is safe for the player) and changing the teleport location.
* Adding friends to regions (which also can teleport to it).
* Automatic unrent/sell for regions of which the owner is offline for a certain time.
* Warning to players when their rent is about to run out (at login and while they are online).
* [Group system](https://github.com/NLthijs48/AreaShop/wiki/The-config-system) to set options for a couple of regions instead of all of them (all settings in `default.yml` can also be used for groups and individual regions).
* High performance: All heavy tasks are spread over time (each tick a part is executed until done), so the plugin should not cause any lag.
* Limit number of regions a player can have: limits can be different per permission node (player group), world or group of regions (possible situation: Normal players can buy 1 market region in survival + 1 build region in survival and 2 plots in creative, while VIPs have double limits for all those), [check these examples](https://github.com/NLthijs48/AreaShop/wiki/Limitgroups-information-and-examples).
* Supports name changes because of saving player info by UUID ([more details](https://github.com/NLthijs48/AreaShop/wiki/Frequently-Asked-Questions#what-happens-when-a-player-changes-his-name)).

### Preview
For a preview join 'mc.go-craft.com' and go to the Survival server, the shops around the spawn use AreaShop.

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

![Transifex-translation-progress](https://transifex.com/projects/p/areashop/resource/enyml/chart/image_png)

[![Discord-support-chat](https://discordapp.com/api/v6/guilds/342592311122853893/widget.png?style=banner3)](https://discord.gg/84kGMCQ)

[![Donate through PayPal to support development](https://www.paypal.com/en_US/i/btn/btn_donate_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=VEECV88NG3L5C)
