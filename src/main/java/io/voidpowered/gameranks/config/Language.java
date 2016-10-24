package io.voidpowered.gameranks.config;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public final class Language extends GRConfiguration {

	private Plugin plugin;
	
	public Language(Plugin plugin, String lang) {
		super(plugin, lang + ".yml", "en_GB.yml");
		this.plugin = plugin;
	}
	
	private String getString(String str) {
		FileConfiguration config = getConfig();
		if(config.isSet(str)) {
			if(config.isString(str)) {
				return config.getString(str);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public String getLanguageString(String str) {
		String string = null;
		if((string = getString(str)) == null) {
			string = getDefaultString(str);
		}
		if(string == null) {
			return null;
		}
		return ChatColor.translateAlternateColorCodes('&', string);
	}
	
	private String getDefaultString(String str) {
		try {
			Reader defConfigStream = new InputStreamReader(plugin.getResource("en_GB.yml"), "UTF8");
			if(defConfigStream != null) {
				YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
				if(defConfig.isSet(str)) {
					if(defConfig.isString(str)) {
						return defConfig.getString(str);
					} else {
						return null;
					}
				} else {
					return null;
				}
			}
		} catch(IOException ex) {
			plugin.getLogger().log(Level.SEVERE, "Could not write defaults.", ex);
		}
		return null;
	}
	
}
