package io.voidpowered.gameranks.api;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import io.voidpowered.gameranks.manager.RankManager;

public final class GameRanksAPI {

	private RankManager rankManager;
	
	public GameRanksAPI(RankManager rankManager) {
		this.rankManager = rankManager;
	}
	
	/**
	 * Returns all the Rank's registered by GameRanks as UnmodifiableCollection.
	 * @return collection of Ranks
	 */
	public Collection<Rank> getRanks() {
		return rankManager.getRanks();
	}
	
	/**
	 * Returns whether the rank specified is real.
	 * @return boolean of legitimacy
	 */
	public boolean rankExists(String rankName) {
		return rankManager.rankExists(rankName);
	}
	
	/**
	 * Returns the Rank associated with the id parameter.
	 * @param id of Rank
	 * @return Rank associated with id or null
	 */
	public Rank getRank(int id) {
		return rankManager.getRank(id);
	}
	
	/**
	 * Returns the default Rank that new players are given when they join.
	 * @return default Rank or null
	 */
	public Rank getDefaultRank() {
		return rankManager.getDefaultRank();
	}
	
	/**
	 * Searches for Rank and returns when string is matched.
	 * The search is case sensitive.
	 * @param rankName to search
	 * @return Rank found or null
	 */
	public Rank getRank(String rankName) {
		return rankManager.getRank(rankName);
	}
	
	/**
	 * Returns found Rank or null if user doesn't have a rank.
	 * @param player for search
	 * @return player's Rank found or null
	 */
	public Rank getUserRank(OfflinePlayer player) {
		return rankManager.getUserRank(player);
	}
	
	/**
	 * Returns found Rank or null if user doesn't have a rank.
	 * @param uuid for search
	 * @return player's Rank found or null
	 */
	public Rank getUserRank(UUID uuid) {
		return rankManager.getUserRank(uuid);
	}
	
	/**
	 * Calls PlayerRankEvent when player rank is changed to Rank parameter.
	 * If the call is successful and the event is not cancelled, the event's rank
	 * is used to decide what the player's new rank will be.
	 * @param player to gather UUID
	 * @param rank of player 
	 */
	public void setUserRank(OfflinePlayer player, Rank rank) {
		rankManager.setUserRank(player, rank);
	}
	
	/**
	 * Calls PlayerRankEvent when player rank is changed to Rank parameter.
	 * If the call is successful and the event is not cancelled, the event's rank
	 * is used to decide what the player's new rank will be.
	 * @param uuid used to store in registry
	 * @param rank of player 
	 */
	public void setUserRank(UUID uuid, Rank rank) {
		rankManager.setUserRank(uuid, rank);
	}
	
	/**
	 * Uses the Rank to apply to the player the permissions, group, prefixes and suffixes.
	 * It removes the players current permissions from their current rank and puts all the permissions from the rank specified instead.
	 * This should be used before the player rank has been changed, otherwise their permissions will not apply until after they login again.
	 * @param player for application
	 * @param rank to apply to player
	 */
	public void applyRank(Player player, Rank rank) {
		rankManager.applyRank(player, rank);
	}
	
}
