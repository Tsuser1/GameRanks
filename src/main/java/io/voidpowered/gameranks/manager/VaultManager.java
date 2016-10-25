package io.voidpowered.gameranks.manager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import io.voidpowered.gameranks.util.GameRanksException;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public final class VaultManager {

	private Economy economy;
	private Permission permissions;
	private Chat chat;
	
	/**
	 * Get the economy using vault
	 * @return Economy
	 */
	public Economy getEconomy() {
		if(economy == null) {
			setupEconomy();
		}
		return economy;
	}
	
	/**
	 * Setup the economy using Vault
	 */
	public void setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
		if(economyProvider != null) {
			economy = economyProvider.getProvider();
		} else {
			throw new GameRanksException("Missing economy provider, please add a supported economy plugin.");
		}
	}

	/**
	 * Get permissions using Vault
	 * @return Permissions
	 */
	public Permission getPermissions() {
		if(permissions == null) {
			setupPermissions();
		}
		return permissions;
	}
	
	/**
	 * Setup the permissions using Vault
	 */
	public void setupPermissions() {
		RegisteredServiceProvider<Permission> permissionsProvider = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
		if(permissionsProvider != null) {
			permissions = permissionsProvider.getProvider();
		} else {
			throw new GameRanksException("Missing permissions provider, please add a supported permissions plugin.");
		}
	}
	
	/**
	 * Get the chat using Vault
	 * @return Chat
	 */
	public Chat getChat() {
		if(chat == null) {
			setupChat();
		}
		return chat;
	}
	
	/**
	 * Setup the chat using Vault
	 */
	public void setupChat() {
		RegisteredServiceProvider<Chat> chatProvider = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
		if(chatProvider != null) {
			chat = chatProvider.getProvider();
		} else {
			throw new GameRanksException("Missing chat provider, please add a supported chat plugin.");
		}
	}
	
}
