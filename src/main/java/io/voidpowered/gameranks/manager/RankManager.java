package io.voidpowered.gameranks.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import io.voidpowered.gameranks.api.Rank;
import io.voidpowered.gameranks.config.GRConfiguration;
import io.voidpowered.gameranks.event.PlayerRankEvent;
import io.voidpowered.gameranks.util.GameRanksException;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

public final class RankManager {

	private GRConfiguration users, ranks;
	private List<Rank> rankList;
	
	private VaultManager vaultManager;
	
	private Permission permissions;
	private boolean supportsPerms;
	private boolean supportsGroups;
	
	private Chat chat;
	private boolean supportsChat;
	
	private Rank defaultRank;
	
	private Logger logger;
	
	public RankManager(VaultManager vaultManager, GRConfiguration users, GRConfiguration ranks) {
		this.vaultManager = vaultManager;
		this.users = users;
		this.ranks = ranks;
		this.rankList = new ArrayList<Rank>();
		this.logger = Bukkit.getPluginManager().getPlugin("GameRanks").getLogger();
		setup();
		load();
	}
	
	private void setup() {
		permissions = vaultManager.getPermissions();
		if(permissions != null) {
			supportsPerms = permissions.isEnabled();
			supportsGroups = permissions.hasGroupSupport();
		}
		if(!supportsPerms) {
			logger.info("Permissions aren't enabled.");
		}
		if(!supportsGroups) {
			logger.info("Your permissions plugin doesn't support groups.");
		}
		try {
			chat = vaultManager.getChat();
			supportsChat = chat.isEnabled();
			if(!supportsChat) {
				logger.info("Chat modifications aren't enabled.");
			}
		} catch(GameRanksException e) {
			logger.info("Chat modifications aren't enabled.");
		}
	}
	
	public Collection<Rank> getRanks() {
		return Collections.unmodifiableCollection(rankList);
	}
	
	public void load() {
		FileConfiguration config = ranks.getConfig();
		ConfigurationSection section = config.getConfigurationSection("ranks");
		if(section != null) {
			Set<String> ranks = section.getKeys(false);
			if(!(ranks == null || ranks.isEmpty())) {
				int id = 0;
				for(String rankName : ranks) {
					loadRank(id, rankName);
					id++;
				}
			}
		} else {
			logger.info("Please setup the ranks for this plugin, follow the guide on the plugin post for guidance.");
		}
		loadDefaultRank();
	}
	
	public void clear() {
		rankList.clear();
	}
	
	public void loadDefaultRank() {
		defaultRank = getRank(ranks.getConfig().getString("default"));
		if(rankList.isEmpty()) {
			defaultRank = null;
		} else if(defaultRank == null) {
			defaultRank = getRank(0);
		}
	}
	
	public Rank getDefaultRank() {
		if(defaultRank == null) {
			loadDefaultRank();
		}
		return defaultRank;
	}
	
	private void loadRank(int id, String rankName) {
		String rankPath = "ranks." + rankName;
		FileConfiguration config = ranks.getConfig();
		List<String> errors = new ArrayList<>();
		if(config.contains(rankPath)) {
			List<String> permissions = new ArrayList<>();
			if(config.isSet(rankPath + ".permissions")) {
				if(config.isList(rankPath + ".permissions")) {
					permissions.addAll(config.getStringList(rankPath + ".permissions"));
				} else if(config.isString(rankPath + ".permissions")) {
					permissions.add(config.getString(rankPath + ".permissions"));
				} else {
					errors.add("Invalid permissions type.");
				}
			}
			List<String> description = new ArrayList<>();
			if(config.isSet(rankPath + ".description")) {
				if(config.isList(rankPath + ".description")) {
					description.addAll(config.getStringList(rankPath + ".description"));
				} else if(config.isString(rankPath + ".description")) {
					description.add(config.getString(rankPath + ".description"));
				} else {
					errors.add("Invalid description.");
				}
			} else {
				errors.add("No description specified.");
			}
			double price = 0D;
			if(config.isSet(rankPath + ".price")) {
				if(config.isDouble(rankPath + ".price") || config.isInt(rankPath + ".price")) {
					price = config.getDouble(rankPath + ".price");
				} else {
					errors.add("Invalid price.");
				}
			} else {
				errors.add("No price specified, using zero as default.");
			}
			double refund = 0D;
			if(config.isSet(rankPath + ".refund")) {
				if(config.isDouble(rankPath + ".refund") || config.isInt(rankPath + ".refund")) {
					refund = config.getDouble(rankPath + ".refund");
				} else {
					errors.add("Invalid refund.");
				}
			} else {
				errors.add("No refund specified, using zero as default.");
			}
			String prefix = null;
			if(config.isSet(rankPath + ".prefix")) {
				if(config.isString(rankPath + ".prefix")) {
					prefix = config.getString(rankPath + ".prefix");
				} else {
					errors.add("Invalid prefix.");
				}
			}
			String suffix = null;
			if(config.isSet(rankPath + ".suffix")) {
				if(config.isString(rankPath + ".suffix")) {
					suffix = config.getString(rankPath + ".suffix");
				} else {
					errors.add("Invalid suffix.");
				}
			}
			String group = null;
			if(config.isSet(rankPath + ".group")) {
				if(config.isString(rankPath + ".group")) {
					group = config.getString(rankPath + ".group");
				} else {
					errors.add("Invalid group.");
				}
			}
			Rank rank = new Rank(id, rankName);
			rank.setPermissions(permissions.toArray(new String[0]));
			rank.setDescription(description.toArray(new String[0]));
			rank.setPrice(price);
			rank.setRefund(refund);
			if(prefix != null) {
				rank.setPrefix(prefix);
			}
			if(suffix != null) {
				rank.setSuffix(suffix);
			}
			if(group != null) {
				rank.setGroup(group);
			}
			rankList.add(rank);
		} else {
			errors.add("Couldn't find rank in configuration file.");
		}
		if(errors.size() > 0) {
			logger.log(Level.WARNING, "There is " + errors.size() + " error(s) with rank: [{id:\"" + id + "\",name:\"" + rankName + "\"}]");
			int errorNum = 1;
			for(String error : errors) {
				logger.log(Level.WARNING, "\r" + errorNum + " - " + error);
				errorNum++;
			}
		}
	}

	public Rank getUserRank(OfflinePlayer player) {
		if(player == null) {
			return null;
		}
		return getUserRank(player.getUniqueId());
	}
	
	public Rank getUserRank(UUID uuid) {
		if(uuid == null) {
			return null;
		}
		FileConfiguration config = users.getConfig();
		String rankPointer = "users." + uuid.toString() + ".rank";
		if(config.contains(rankPointer)) {
			if(config.isString(rankPointer)) {
				String rankName = config.getString(rankPointer);
				Rank rank = getRank(rankName);
				if(rank == null) {
					return null;
				} else {
					return rank;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public Rank getRank(String rankName) {
		if(rankName == null || rankName.isEmpty()) {
			return null;
		}
		// case sensitive search
		for(Rank rank: rankList) {
			if(rank.getName().equals(rankName)) {
				return rank;
			}
		}
		// executes non-case sensitive search after so it doesn't order incorrectly
		for(Rank rank: rankList) {
			if(rank.getName().equalsIgnoreCase(rankName)) {
				return rank;
			}
		}
		return null;
	}
	
	public Rank getRank(int rankId) {
		for(Rank rank : rankList) {
			if(rank.getId() == rankId) {
				return rank;
			}
		}
		return null;
	}
	
	public void setUserRank(OfflinePlayer player, Rank rank) {
		if(player == null) {
			return;
		}
		setUserRank(player.getUniqueId(), rank);
	}
	
	public void setUserRank(UUID uuid, Rank rank) {
		if(uuid == null) {
			return;
		}
		PlayerRankEvent event = new PlayerRankEvent(Bukkit.getOfflinePlayer(uuid), getUserRank(uuid), rank);
		Bukkit.getPluginManager().callEvent(event);
		if(!event.isCancelled()) {
			rank = event.getRank();
			if(rank == null) {
				return;
			}
			users.getConfig().set("users." + uuid.toString() + ".rank", rank.getName());
			users.saveConfig();
		}
	}
	
	public void addPermissions(Player player) {
		if(supportsPerms && player != null) {
			Rank playerRank = getUserRank(player);
			if(playerRank == null) {
				return;
			}
			for(int i = 0; i <= playerRank.getId(); i++) {
				Rank rank = getRank(i);
				if(rank != null && rank.getPermissions().length > 0) {
					addPermissions(player, rank);
				}
			}
		}
	}
	
	public void removePermissions(Player player) {
		if(supportsPerms && player != null) {
			Rank playerRank = getUserRank(player);
			if(playerRank == null) {
				return;
			}
			for(int i = 0; i <= playerRank.getId(); i++) {
				Rank rank = getRank(i);
				if(rank != null && rank.getPermissions().length > 0) {
					removePermissions(player, rank);
				}
			}
		}
	}
	
	public void addPermissions(Player player, Rank rank) {
		if(supportsPerms && player != null && rank != null) {
			if(rank != null && rank.getPermissions().length > 0) {
				for(String permission : rank.getPermissions()) {
					permissions.playerAdd(player, permission);
				}
			}
		}
	}

	public void removePermissions(Player player, Rank rank) {
		if(supportsPerms && player != null && rank != null) {
			if(rank != null && rank.getPermissions().length > 0) {
				for(String permission : rank.getPermissions()) {
					permissions.playerRemove(player, permission);
				}
			}
		}
	}
	
	public void addGroup(Player player) {
		if(supportsGroups && player != null) {
			addGroup(player, getUserRank(player));
		}
	}
	
	public void removeGroup(Player player) {
		if(supportsGroups && player != null) {
			removeGroup(player, getUserRank(player));
		}
	}
	
	public void setPrefix(Player player) {
		if(supportsChat && player != null) {
			setPrefix(player, getUserRank(player));
		}
	}
	
	public void setPrefix(Player player, Rank rank) {
		if(supportsChat && player != null && rank != null) {
			if(rank != null && rank.getPrefix() != null && !rank.getPrefix().isEmpty()) {
				chat.setPlayerPrefix(null, player, rank.getPrefix());
			}
		}
	}
	
	public void removePrefix(Player player) {
		if(supportsChat && player != null) {
			chat.setPlayerPrefix(null, player, null);
		}
	}
	
	public void removeSuffix(Player player) {
		if(supportsChat && player != null) {
			chat.setPlayerSuffix(null, player, null);
		}
	}
	
	public void setSuffix(Player player, Rank rank) {
		if(supportsChat && player != null && rank != null) {
			if(rank != null && rank.getSuffix() != null && !rank.getSuffix().isEmpty()) {
				chat.setPlayerSuffix(null, player, rank.getSuffix());
			}
		}
	}
	
	public void addGroup(Player player, Rank rank) {
		if(supportsGroups && player != null && rank != null) {
			if(rank != null && rank.getGroup() == null) {
				permissions.playerAddGroup(player, rank.getGroup());
			}
		}
	}
	
	public void removeGroup(Player player, Rank rank) {
		if(supportsGroups && player != null) {
			if(rank != null && rank.getGroup() == null) {
				permissions.playerRemoveGroup(player, rank.getGroup());
			}
		}
	}

	public void applyRank(Player player, Rank rank) {
		removePermissions(player);
		removeGroup(player);
		addPermissions(player, rank);
		addGroup(player, rank);
		setPrefix(player, rank);
		setSuffix(player, rank);
	}
	
	/**
	 * Check if a rank is defined in the ranks configuration.
	 * @param rank to be checked for existence
	 * @return if rank exists
	 */
	public boolean rankExists(String rank){
		for(Rank rankSearch : getRanks()) {
			if(rankSearch.getName().equalsIgnoreCase(rank)) {
				return true;
			}
		}
		return false;
	}
}
