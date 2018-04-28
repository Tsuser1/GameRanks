package io.voidpowered.gameranks.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class GRConfiguration {

	private Plugin plugin;
	private File configFile;
	private FileConfiguration config;
	private String configName;
	private String defaultConfigName;
	
	/**
	 * Define GRConfiguration
	 * @param plugin This plugin
	 * @param configName Name of configuration
	 */
	public GRConfiguration(Plugin plugin, String configName) {
		this(plugin, configName, configName);
	}
	
	/**
	 * Define GRConfiguration and default configuration
	 * @param plugin This plugin
	 * @param configName Name of configuration
	 * @param defaultConfigName Name of default configuration
	 */
	public GRConfiguration(Plugin plugin, String configName, String defaultConfigName) {
		this.plugin = plugin;
		this.configName = configName;
		this.defaultConfigName = defaultConfigName;
	}
	
	/**
	 * Save the configuration to disk.
	 */
	public void saveConfig() {
		if(config == null || configFile == null) {
			return;
		}
		try {
			getConfig().save(configFile);
		} catch(IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configName, e);
		}
	}
	
	/**
	 * Get changes from disk.
	 */
	public void reloadConfig() {
		if(configFile == null) {
			configFile = new File(plugin.getDataFolder(), configName);
		}
		config = YamlConfiguration.loadConfiguration(configFile);
		try {
			Reader defConfigStream = new InputStreamReader(plugin.getResource(defaultConfigName), "UTF8");
			if(defConfigStream != null) {
				YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
				config.setDefaults(defConfig);
			}
		} catch(IOException ex) {
			plugin.getLogger().log(Level.SEVERE, "Could not write defaults.", ex);
		}
	}
	
	public FileConfiguration getConfig() {
		if(config == null) {
			reloadConfig();
		}
		return config;
	}
	
	/**
	 * Save the default configuration file.
	 */
	public void saveDefaultConfig() {
		if(configFile == null) {
			configFile = new File(plugin.getDataFolder(), configName);
		}
		if(!configFile.exists()) {
			configFile.getParentFile().mkdirs();
			copy(plugin.getResource(defaultConfigName), configFile);
		}
	}
	
	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while((len = in.read(buf)) > 0){
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
