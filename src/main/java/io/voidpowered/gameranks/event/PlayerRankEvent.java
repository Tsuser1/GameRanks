package io.voidpowered.gameranks.event;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import io.voidpowered.gameranks.api.Rank;

public final class PlayerRankEvent extends Event implements Cancellable {

	private static final HandlerList handlers;
	
	static {
		handlers = new HandlerList();
	}
	
	private final OfflinePlayer player;
	private final Rank previousRank;
	private Rank rank;
	
	private boolean cancelled;
	
	public PlayerRankEvent(OfflinePlayer player, Rank previousRank, Rank rank) {
		this.player = player;
		this.previousRank = previousRank;
		this.rank = rank;
	}
	
	/**
	 * Returns handlers
	 */
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	/**
	 * Indicates if the event was cancelled.
	 */
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	/**
	 * Sets if the event has been cancelled.
	 */
	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	/**
	 * Get offline player for the event
	 * @return OfflinePlayer player
	 */
	public OfflinePlayer getPlayer() {
		return player;
	}

	/**
	 * Get the previous rank for the player of the event
	 * @return Previous rank
	 */
	public Rank getPreviousRank() {
		return previousRank;
	}

	/**
	 * Get the rank for the player of the event
	 * @return Rank
	 */
	public Rank getRank() {
		return rank;
	}

	/**
	 * Manually set the rank for the player of the event
	 * @param newRank New rank to use
	 */
	public void setRank(Rank newRank) {
		this.rank = newRank;
	}
	
}
