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
