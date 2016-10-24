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
	
	public Economy getEconomy() {
		if(economy == null) {
			setupEconomy();
		}
		return economy;
	}
	
	public void setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
		if(economyProvider != null) {
			economy = economyProvider.getProvider();
		} else {
			throw new GameRanksException("Missing economy provider, please add a supported economy plugin.");
		}
	}

	public Permission getPermissions() {
		if(permissions == null) {
			setupPermissions();
		}
		return permissions;
	}
	
	public void setupPermissions() {
		RegisteredServiceProvider<Permission> permissionsProvider = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
		if(permissionsProvider != null) {
			permissions = permissionsProvider.getProvider();
		} else {
			throw new GameRanksException("Missing permissions provider, please add a supported permissions plugin.");
		}
	}
	
	public Chat getChat() {
		if(chat == null) {
			setupChat();
		}
		return chat;
	}
	
	public void setupChat() {
		RegisteredServiceProvider<Chat> chatProvider = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
		if(chatProvider != null) {
			chat = chatProvider.getProvider();
		} else {
			throw new GameRanksException("Missing chat provider, please add a supported chat plugin.");
		}
	}
	
}
