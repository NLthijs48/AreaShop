package nl.evolutioncoding.AreaShop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.base.Charsets;

public class LanguageManager {
	private AreaShop plugin = null;
	private String languages[] = {"EN", "NL", "FR"};
	private HashMap<String, String> currentLanguage, defaultLanguage;
	
	/**
	 * Constructor
	 * @param plugin The AreaShop plugin
	 */
	public LanguageManager(AreaShop plugin) {
		this.plugin = plugin;
		
		/* Save default language files if not present */
		this.saveDefaults();

		/* Open current language file */
		this.loadLanguage();
	}
	
	/**
	 * Saves the default language files if not already present
	 */
	public void saveDefaults() {
		/* Create the language folder if it not exists */
		File langFolder;
		langFolder = new File(plugin.getDataFolder() + File.separator + AreaShop.languageFolder);
		if(!langFolder.exists()) {
			langFolder.mkdirs();
		}
		
		/* Create the language files, overwrites if a file already exists */
		/* Overriding is necessary because otherwise with an update the new lang */
		/* files would not be used, when translating your own use another */
		/* file name as the default */
		File langFile;
		for(int i=0; i<languages.length; i++) {
			langFile = new File(plugin.getDataFolder() + File.separator + AreaShop.languageFolder + File.separator + languages[i] + ".yml");
			InputStream input = null;
			OutputStream output = null;
			try {
				input = plugin.getResource(AreaShop.languageFolder + "/" + languages[i] + ".yml");
				output = new FileOutputStream(langFile);
		 
				int read = 0;
				byte[] bytes = new byte[1024];		 
				while ((read = input.read(bytes)) != -1) {
					output.write(bytes, 0, read);
				} 
				input.close();
				output.close();
			} catch(IOException e) {
				try {
					input.close();
					output.close();
				} catch (IOException e1) {} catch (NullPointerException e2) {}
				
				plugin.getLogger().info("Something went wrong saving a default language file: " + langFile.getPath());
			}
		}
		
	}
	
	/**
	 * Loads the current language file specified in the config
	 */
	public void loadLanguage() {
		Map<String, Object> map;
		Set<String> set;
		YamlConfiguration ymlFile;
		
		/* Save the current language file to the HashMap */
		currentLanguage = new HashMap<String, String>();		
		File file = new File(plugin.getDataFolder() + File.separator + AreaShop.languageFolder + File.separator + plugin.config().getString("language") + ".yml");
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8);
		} catch (FileNotFoundException e1) {}
		if(reader != null) {
			ymlFile = YamlConfiguration.loadConfiguration(reader);
			map = ymlFile.getValues(true);
			set = map.keySet();
			try {
				for(String key : set) {
					currentLanguage.put(key, (String)map.get(key));
				}
			} catch(ClassCastException e) {}
		}
		
		/* Save the default strings to the HashMap */
		defaultLanguage = new HashMap<String, String>();
		File standard = new File(plugin.getDataFolder() + File.separator + AreaShop.languageFolder + "/" + languages[0]+ ".yml");
		InputStreamReader reader2 = null;
		try {
			reader2 = new InputStreamReader(new FileInputStream(standard), Charsets.UTF_8);
		} catch (FileNotFoundException e1) {}
		if(reader2 != null) {
	        ymlFile = YamlConfiguration.loadConfiguration(reader2);   
	        map = ymlFile.getValues(true);
			set = map.keySet();
			try {
				for(String key : set) {
					defaultLanguage.put(key, (String)map.get(key));
				}
			} catch(ClassCastException e) {}
		}
	}
	
	/**
	 * Function to get the string in the language that has been set
	 * @param key Key to the language string
	 * @param params The replacements for the %1% tags
	 * @return String The language string specified with the key
	 */
	public String getLang(String key, Object... params) {
		String result = null;
		
		/* Get the language string */
		if(currentLanguage.containsKey(key)) {
			result = currentLanguage.get(key);
		} else {
			result = defaultLanguage.get(key);
		}
		
		if(result == null) {
			plugin.getLogger().info("Wrong key for getting translation: " + key);
		} else {
			/* Replace all tags,  e.g. %0% */
		    for (int i=0; i<params.length; i++) {
		    	if(params[i] != null) {
		    		result = result.replace("%" + i + "%", params[i].toString());
		    	}
		    }
		}
	    
		return result;
	}
}








































