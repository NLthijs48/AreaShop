# How to release a version of AreaShop
Below the steps to follow for releasing a version of AreaShop are listed.

## Pull the latest translations from Transifex
**Setup:**
1. Clone the [InteractiveMessengerTransifex](https://github.com/NLthijs48/InteractiveMessengerTransifex) repository
2. Run `mvn package`
	- The `target\InteractiveMessengerTransifex.jar` file should now be present

**Pull translations:**
1. Run `target/InteractiveMessengerTransifex.jar` using Java with the following arguments:
	- **`download`**
	- **<transifex api key>**, get from Transifex
	- **`areashop`**, project name
	- **`enyml`**, name of the file to download, this is the name AreaShop uses on Transifex
	- **`./AreaShop/src/main/resources/lang`**, path to the `lang` directory in your clone of the AreaShop repository
	- **`70`**, meaning only languages translated for at least 70% will get downloaded
	- **`true`**, reformat the files to be suitable for using in the plugin
	- **`./langHeader.yml`**, header to add to language file, this file is in the AreaShop repository
	- **`<version>`**, release version to put in the header
2. Commit the changed files in the AreaShop repository
3. Push the commit to Github

## Update the version number
1. In `./AreaShop/pom.xml` update the `<version>` tag with the new version (following semantic versioning)
2. In `./changelog.md` add notes about the features/fixes of this new version
3. Commit the updated files
4. Push the commit to Github
5. Jenkins will build a new version and make it available on Maven

## Uploading to distribution platforms
The order is important, uploading to Github will trigger the update notification, but points people to Spigot
1. Post a [resource update on Spigot](https://www.spigotmc.org/resources/areashop.2991/add-version)
	- Use the changelog in the update message
	- Indicate major features in the update title
	- Update the supported Spigot versions if necessary
2. Upload a [new file on Bukkit](https://dev.bukkit.org/projects/regionbuyandrent/files/upload)
	- Use the changelog in the update message
	- The file needs to be approved before it will be available for everyone
3. Add a [new release on Github](https://github.com/NLthijs48/AreaShop/releases/new)
	- Enter a tag like `v2.6.0`
	- Enter a title like `AreaShop 2.6.0`
	- Add the changelog
	- Add the .jar file as `AreaShop.jar`

