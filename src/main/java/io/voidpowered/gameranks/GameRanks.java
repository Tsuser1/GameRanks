package io.voidpowered.gameranks;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import io.voidpowered.gameranks.api.GameRanksAPI;
import io.voidpowered.gameranks.config.GRConfiguration;
import io.voidpowered.gameranks.config.Language;
import io.voidpowered.gameranks.manager.RankManager;
import io.voidpowered.gameranks.manager.VaultManager;
import io.voidpowered.gameranks.util.GameRanksException;

public final class GameRanks extends JavaPlugin {

	private static GameRanksAPI API;
	
	protected VaultManager vaultManager;
	protected RankManager rankManager;
	protected Language lang;
	protected boolean usePermissions;
	
	private GRConfiguration config, users, ranks;
	private GRCommands commands;
	
	private Logger logger;

	@Override
	public void onEnable() {
		logger = getLogger();
		saveDefaultConfigs();
		try {
			try {
				vaultManager = new VaultManager();
				vaultManager.setupEconomy();
				if(!vaultManager.getEconomy().isEnabled()) {
					throw new GameRanksException("Economy plugin is disabled, unable to be used.");
				}
				vaultManager.setupPermissions();
			} catch(GameRanksException e) {
				logger.log(Level.SEVERE, e.getLocalizedMessage());
				logger.severe("Plugin has disabled itself.");
				setEnabled(false);
				return;
			}
			rankManager = new RankManager(vaultManager, users, ranks);
			API = new GameRanksAPI(rankManager);
			Bukkit.getPluginManager().registerEvents(new Listener() {
	
				@EventHandler
				public void onJoin(PlayerJoinEvent e) {
					addPlayer(e.getPlayer());
				}
				
				@EventHandler
				public void onLeave(PlayerQuitEvent e) {
					removePlayer(e.getPlayer());
				}
	
			}, this);
			// to help with on reload in-case players are already online
			for(Player player : Bukkit.getOnlinePlayers()) {
				addPlayer(player);
			}
			// setup essential configuration
			loadLanguage();
			loadPermissions();
			// setup commands
			commands = new GRCommands(this);
			getCommand("ranks").setExecutor(commands);
			getCommand("rank").setExecutor(commands);
			getCommand("rankup").setExecutor(commands);
			getCommand("rankdown").setExecutor(commands);
			getCommand("gameranks").setExecutor(commands);
			logger.info("GameRanks has enabled succesfully.");
		} catch(GameRanksException | NoClassDefFoundError e) {
			setEnabled(false);
			if(e instanceof NoClassDefFoundError) {
				logger.log(Level.SEVERE, "Please install a supported version of Vault, plugin has been disabled.");
			} else {
				logger.log(Level.SEVERE, "GameRanks failed to enable, plugin has been disabled.", e);
			}
		}
	}
	
	/**
	 * Save default configuration values to disk. This is an initial setup function.
	 */
	private void saveDefaultConfigs() {
		(config = new GRConfiguration(this, "config.yml")).saveDefaultConfig();
		(users = new GRConfiguration(this, "users.yml")).saveDefaultConfig();
		(ranks = new GRConfiguration(this, "ranks.yml")).saveDefaultConfig();
	}
	
	private void loadPermissions() {
		FileConfiguration config = this.config.getConfig();
		boolean perms;
		if(config.isSet("rankPermissions")) {
			if(config.isBoolean("rankPermissions")) {
				perms = config.getBoolean("usePermissions");
			} else {
				config.set("usePermissions", perms = false);
				this.config.saveConfig();
			}
		}  else {
			config.set("usePermissions", perms = false);
			this.config.saveConfig();
		}
		usePermissions = perms;
	}
	
	/**
	 * Load language configuration.
	 */
	private void loadLanguage() {
		FileConfiguration config = this.config.getConfig();
		String langName;
		if(config.isSet("lang")) {
			if(config.isString("lang")) {
				langName = config.getString("lang");
			} else {
				config.set("lang", langName = "en_GB");
				this.config.saveConfig();
			}
		} else {
			config.set("lang", langName = "en_GB");
			this.config.saveConfig();
		}
		lang = new Language(this, langName);
		lang.saveDefaultConfig();
	}
	
	@Override
	public void onDisable() {
		// remove any during a reload
		for(Player player : Bukkit.getOnlinePlayers()) {
			removePlayer(player);
		}
	}
	
	/**
	 * Setup ranks for a player (usually the first time they join).
	 * @param player The player to setup values for
	 */
	private void addPlayer(Player player) {
		if(rankManager != null) {
			boolean playerHasRank = false;
			if(rankManager.getUserRank(player) == null) {
				if(rankManager.getDefaultRank() == null) {
					logger.warning("No default rank has been configured, so new players wont be able to use ranks.");
				} else {
					rankManager.setUserRank(player, rankManager.getDefaultRank());
					playerHasRank = true;
				}
			} else {
				playerHasRank = true;
			}
			if(playerHasRank) {
				// cannot use apply because it wouldn't remove all but update them
				rankManager.addPermissions(player);
				rankManager.addGroup(player);
				rankManager.setPrefix(player);
				rankManager.setSuffix(player);
			}
		}
	}
	
	/**
	 * Delete a player from the database.
	 * @param player The player to be removed
	 */
	private void removePlayer(Player player) {
		if(rankManager != null) {
			if(rankManager.getUserRank(player) != null) {
				// cannot use apply because it wouldn't remove all but update them
				rankManager.removePermissions(player);
				rankManager.removeGroup(player);
				rankManager.removePrefix(player);
				rankManager.removeSuffix(player);
			}
		}
	}
	
	/**
	 * Reloads all configuration, recreating any missing configuration files.
	 * Reallocates all player permissions and additional features associated with their ranks.
	 */
	public void reload() {
		saveDefaultConfigs();
		rankManager.clear();
		rankManager.load();
		loadLanguage();
		for(Player player : Bukkit.getOnlinePlayers()) {
			removePlayer(player);
			addPlayer(player);
		}
	}
	
	/**
	 * Provides an interface for developers.
	 * @return GameRanksAPI instance
	 */
	public static GameRanksAPI getLibrary() {
		return API;
	}
	
}
