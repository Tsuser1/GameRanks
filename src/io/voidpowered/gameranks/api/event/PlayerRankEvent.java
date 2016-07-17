package io.voidpowered.gameranks.api.event;

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
	
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}


	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}


	public OfflinePlayer getPlayer() {
		return player;
	}

	public Rank getPreviousRank() {
		return previousRank;
	}


	public Rank getRank() {
		return rank;
	}


	public void setRank(Rank newRank) {
		this.rank = newRank;
	}
	
}
