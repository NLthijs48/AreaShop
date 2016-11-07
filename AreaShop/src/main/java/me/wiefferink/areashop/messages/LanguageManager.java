package me.wiefferink.areashop.messages;

import com.google.common.base.Charsets;
import me.wiefferink.areashop.AreaShop;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.*;


public class LanguageManager {
	private AreaShop plugin = null;
	private String languages[] = {"EN", "NL", "FR", "DE", "RU", "SV", "NO"};
	private Map<String, List<String>> currentLanguage, defaultLanguage;

	/**
	 * Constructor
	 * @param plugin The AreaShop plugin
	 */
	public LanguageManager(AreaShop plugin) {
		this.plugin = plugin;
		this.saveDefaults();
		currentLanguage = loadLanguage(plugin.getConfig().getString("language"));
		defaultLanguage = loadLanguage(languages[0]);
	}

	/**
	 * Saves the default language files if not already present
	 */
	public void saveDefaults() {
		// Create the language folder if it not exists
		File langFolder;
		langFolder = new File(plugin.getDataFolder()+File.separator+AreaShop.languageFolder);
		if(!langFolder.exists()) {
			if(!langFolder.mkdirs()) {
				AreaShop.warn("Could not create language directory: "+langFolder.getAbsolutePath());
				return;
			}
		}

		// Create the language files, overwrites if a file already exists
		// Overriding is necessary because otherwise with an update the new lang files would not be used
		File langFile;
		for(String language : languages) {
			langFile = new File(plugin.getDataFolder()+File.separator+AreaShop.languageFolder+File.separator+language+".yml");
			try(
					InputStream input = plugin.getResource(AreaShop.languageFolder+"/"+language+".yml");
					OutputStream output = new FileOutputStream(langFile)
			) {
				if(input == null) {
					AreaShop.warn("Could not save default language to the '"+AreaShop.languageFolder+"' folder: "+language+".yml");
					continue;
				}
				int read;
				byte[] bytes = new byte[1024];
				while((read = input.read(bytes)) != -1) {
					output.write(bytes, 0, read);
				}
				input.close();
				output.close();
			} catch(IOException e) {
				AreaShop.warn("Something went wrong saving a default language file: "+langFile.getPath());
			}
		}

	}

	/**
	 * Loads the specified language
	 * @param key The language to load
	 * @return Map with the messages loaded from the file
	 */
	public Map<String, List<String>> loadLanguage(String key) {
		return loadLanguage(key, true);
	}

	/**
	 * Loads the specified language
	 * @param key     The language to load
	 * @param convert try conversion or not
	 * @return Map with the messages loaded from the file
	 */
	private Map<String, List<String>> loadLanguage(String key, boolean convert) {
		Map<String, List<String>> result = new HashMap<>();

		// Load the strings
		boolean isTransifexFile = false;
		File file = new File(plugin.getDataFolder()+File.separator+AreaShop.languageFolder+File.separator+key+".yml");
		try(
				InputStreamReader reader = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8)
		) {
			YamlConfiguration ymlFile = YamlConfiguration.loadConfiguration(reader);
			// Detect empty language files, happens when the YAML parser prints an exception (it does return an empty YamlConfiguration though)
			if(ymlFile.getKeys(false).isEmpty()) {
				AreaShop.warn("Language file "+key+".yml has zero messages.");
				return result;
			}
			// Detect language files downloaded from Transifex and convert them
			if(ymlFile.getKeys(false).size() == 1) {
				for(String languageKey : ymlFile.getKeys(false)) {
					if(ymlFile.isConfigurationSection(languageKey)) {
						isTransifexFile = convert;
					}
				}
			}
			// Retrieve the messages from the YAML file and create the result
			if(!isTransifexFile) {
				for(String messageKey : ymlFile.getKeys(false)) {
					if(ymlFile.isList(messageKey)) {
						result.put(messageKey, new ArrayList<>(ymlFile.getStringList(messageKey)));
					} else {
						result.put(messageKey, new ArrayList<>(Collections.singletonList(ymlFile.getString(messageKey))));
					}
				}
			}
		} catch(IOException e) {
			AreaShop.warn("Could not load set language file: "+file.getAbsolutePath());
		}

		if(isTransifexFile) {
			if(!Transifex.convertFrom(file)) {
				AreaShop.warn("Failed to convert "+file.getName()+" from the Transifex layout to the AreaShop layout, check the errors above");
			}
			return loadLanguage(key, false);
		}
		return result;
	}


	/**
	 * Get the message for a certain key, without doing any processing
	 * @param key The key of the message to get
	 * @return The message as a list of strings
	 */
	public List<String> getRawMessage(String key) {
		List<String> message;
		if(key.equalsIgnoreCase(Message.CHATLANGUAGEVARIABLE)) {
			message = plugin.getChatPrefix();
		} else if(currentLanguage.containsKey(key)) {
			message = currentLanguage.get(key);
		} else {
			message = defaultLanguage.get(key);
		}
		if(message == null) {
			return new ArrayList<>();
		}
		return new ArrayList<>(message);
	}

}

































