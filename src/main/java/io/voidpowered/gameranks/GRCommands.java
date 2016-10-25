package io.voidpowered.gameranks;

import java.util.IllegalFormatException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import io.voidpowered.gameranks.api.Rank;
import io.voidpowered.gameranks.config.Language;
import io.voidpowered.gameranks.manager.RankManager;
import io.voidpowered.gameranks.manager.VaultManager;
import net.milkbowl.vault.economy.Economy;

public final class GRCommands implements CommandExecutor {

	private GameRanks plugin;
	private Logger logger;
	
	public GRCommands(GameRanks plugin) {
		this.plugin = plugin;
		this.logger = plugin.getLogger();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		switch(cmd.getName().toLowerCase()) {
			case "ranks":
				return handleRanksCommand(sender, cmd, args);
			case "rank": 
				return handleRankCommand(sender, cmd, args);
			case "rankup":
				return handleRankUpCommand(sender, cmd, args);
			case "rankdown":
				return handleRankDownCommand(sender, cmd, args);
			case "gameranks":
				return handleGameRanksCommand(sender, cmd, args);
		}
		return false;
	}

	private boolean handleRanksCommand(CommandSender sender, Command cmd, String[] args) {
		Language lang = plugin.lang;
		if(sender.hasPermission("gameranks.commands.ranks")) {
			RankManager rankManager = plugin.rankManager;
			if(args.length > 0 && (args.length > 0) ? plugin.rankManager.rankExists(args[0]) : false) {
				String arg = args[0];
				Rank rank = null;
				for(Rank rankSearch : rankManager.getRanks()) {
					if(rankSearch.getName().equalsIgnoreCase(arg)) {
						rank = rankSearch;
						break;
					}
				}
				if(rank != null) {
					String[] description = rank.getDescription();
					if(description.length > 0) {
						for(String line : description) {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
						}
					} else {
						String noRankDescription = lang.getLanguageString("NoRankDescription");
						if(!noRankDescription.isEmpty()) {
							sender.sendMessage(noRankDescription);
						}
					}
				} else {
					String rankNotFound = lang.getLanguageString("RankNotFound");
					if(!rankNotFound.isEmpty()) {
						sender.sendMessage(rankNotFound);
					}
				}
			} else {
				Rank[] ranks = rankManager.getRanks().toArray(new Rank[0]);
				if(ranks.length > 0) {
					String ranksListLine = lang.getLanguageString("RanksListLine");
					if(!ranksListLine.isEmpty()) {
						Integer index = 1;
						if(args.length > 0) {
							// Attempt to get page user is referencing.
							try {
								index = Integer.parseInt(args[0]); 
							} catch(NumberFormatException ex) {
								// This should ideally have an error message of it's own.
								index = null; 
							}
							// Any attempt to reference a negative number is nullified.
							if(index != null && index < 1){
								index = 1;
							}
						}
						if(index == null) {
							sender.sendMessage(ChatColor.RED + "Error: Invalid page number!");
						} else {
							String ranksListTitle = lang.getLanguageString("RanksListTitle");
							if(!ranksListTitle.isEmpty()) {
								sender.sendMessage(ranksListTitle + ChatColor.DARK_GRAY + " [" + ChatColor.GREEN + index + ChatColor.GRAY + "/" + ChatColor.GREEN + Integer.valueOf(ranks.length / 8 + (ranks.length % 8 > 0 ? 1 : 0)) + ChatColor.DARK_GRAY + "]");
							}
							// Calculate start position
							index = (ranks.length < 9) ? 0 : (index != 1) ? ((index - 1) * 8 + 1) : ((index - 1) * 8);
							for(int i = index; i < ranks.length && i <= index + 8; i++) {
								try {
									sender.sendMessage(String.format(ranksListLine, i + 1, ranks[i].getName()));
								} catch(IllegalFormatException e) {
									logger.log(Level.WARNING, "Error in language file with format of RanksListLine, please correct.", e);
								}
							}
						}
					}
				} else {
					String noRanksSetup = lang.getLanguageString("NoRanksSetup");
					if(!noRanksSetup.isEmpty()) {
						sender.sendMessage(noRanksSetup);
					}
				}
			}
		} else {
			String noPermissionsError = lang.getLanguageString("NoPermissionsError");
			if(!noPermissionsError.isEmpty()) {
				sender.sendMessage(noPermissionsError);
			}
		}
		return true;
	}
	
	private boolean handleRankCommand(CommandSender sender, Command cmd, String[] args) {
		Language lang = plugin.lang;
		if(sender.hasPermission("gameranks.commands.rank")) {
			RankManager rankManager = plugin.rankManager;
			if(args.length > 0) {
				if(sender.hasPermission("gameranks.commands.rank.others")) {
					@SuppressWarnings("deprecation")
					Player player = Bukkit.getPlayer(args[0]);
					if(player != null) {
						Rank rank = rankManager.getUserRank(player);
						if(rank != null) {
							String currentPlayerRank = lang.getLanguageString("CurrentPlayerRank");
							if(!currentPlayerRank.isEmpty()) {
								try {
									sender.sendMessage(String.format(currentPlayerRank, player.getName(), rank.getName()));
								} catch(IllegalFormatException e) {
									logger.log(Level.WARNING, "Error in language file with format of CurrentPlayerRank, please correct.", e);
								}
							}
						} else {
							String playerHasNoRank = lang.getLanguageString("PlayerHasNoRank");
							if(!playerHasNoRank.isEmpty()) {
								try {
									sender.sendMessage(String.format(playerHasNoRank, player.getName()));
								} catch(IllegalFormatException e) {
									logger.log(Level.WARNING, "Error in language file with format of PlayerHasNoRank, please correct.", e);
								}
							}
						}
					} else {
						String playerNotFound = lang.getLanguageString("PlayerNotFound");
						if(!playerNotFound.isEmpty()) {
							sender.sendMessage(playerNotFound);
						}
					}
				} else {
					String noPermissionsError = lang.getLanguageString("NoPermissionsError");
					if(!noPermissionsError.isEmpty()) {
						sender.sendMessage(noPermissionsError);
					}
				}
			} else {
				if(sender instanceof Player) {
					Player player = (Player) sender;
					Rank rank = rankManager.getUserRank(player);
					if(rank != null) {
						String currentUserRank = lang.getLanguageString("CurrentUserRank");
						if(!currentUserRank.isEmpty()) {
							try {
								sender.sendMessage(String.format(currentUserRank, rank.getName()));
							} catch(IllegalFormatException e) {
								logger.log(Level.WARNING, "Error in language file with format of CurrentUserRank, please correct.", e);
							}
						}
					} else {
						String userHasNoRank = lang.getLanguageString("UserHasNoRank");
						if(!userHasNoRank.isEmpty()) {
							sender.sendMessage(userHasNoRank);
						}
					}
				} else {
					String consoleIsNotAPlayer = lang.getLanguageString("ConsoleIsNotAPlayer");
					if(!consoleIsNotAPlayer.isEmpty()) {
						sender.sendMessage(consoleIsNotAPlayer);
					}
				}
			}
		} else {
			String noPermissionsError = lang.getLanguageString("NoPermissionsError");
			if(!noPermissionsError.isEmpty()) {
				sender.sendMessage(noPermissionsError);
			}
		}
		return true;
	}
	
	private boolean handleRankUpCommand(CommandSender sender, Command cmd, String[] args) {
		Language lang = plugin.lang;
		if(sender.hasPermission("gameranks.commands.rankup")) {
			RankManager rankManager = plugin.rankManager;
			VaultManager vaultManager = plugin.vaultManager;
			if(args.length > 0) {
				if(sender.hasPermission("gameranks.commands.rankup.others")) {
					@SuppressWarnings("deprecation")
					Player player = Bukkit.getPlayer(args[0]);
					if(player != null) {
						Rank rank = rankManager.getUserRank(player);
						if(rank != null) {
							Rank newRank = rankManager.getRank(rank.getId() + 1);
							if(newRank == null) {
								String playerMaximumRank = lang.getLanguageString("PlayerMaximumRank");
								if(!playerMaximumRank.isEmpty()) {
									try {
										sender.sendMessage(String.format(playerMaximumRank, player.getName()));
									} catch(IllegalFormatException e) {
										logger.log(Level.WARNING, "Error in language file with format of PlayerMaximumRank, please correct.", e);
									}
								}								
							} else {
								if(sender instanceof Player) {
									Player executor = (Player) sender;
									Economy economy = vaultManager.getEconomy();
									// Check if TARGET has required permissions to rankup to that
									if(((player.hasPermission("gameranks.rankup." + newRank.getName().toLowerCase()) || player.hasPermission("gameranks.rankup.*")) && plugin.usePermissions) || !plugin.usePermissions){
										if(economy.getBalance(executor) >= newRank.getPrice()) {
											if(economy.withdrawPlayer(executor, newRank.getPrice()).transactionSuccess()) {
												rankManager.applyRank(player, newRank);
												rankManager.setUserRank(player, newRank);
												newRank = rankManager.getUserRank(player);
												String playerRankUp = lang.getLanguageString("PlayerRankUp");
												if(!playerRankUp.isEmpty()) {
													try {
														sender.sendMessage(String.format(playerRankUp, player.getName(), newRank.getName()));
													} catch(IllegalFormatException e) {
														logger.log(Level.WARNING, "Error in language file with format of PlayerRankUp, please correct.", e);
													}
												}
											}
										} else {
											String lackOfFunds = lang.getLanguageString("LackOfFunds");
											if(!lackOfFunds.isEmpty()) {
												sender.sendMessage(lackOfFunds);
											}
										}
									} else {
										String noPermissionsError = lang.getLanguageString("NoPermissionsError");
										if(!noPermissionsError.isEmpty()) {
											sender.sendMessage(noPermissionsError);
										}
									}
								} else {
									rankManager.applyRank(player, newRank);
									rankManager.setUserRank(player, newRank);
									newRank = rankManager.getUserRank(player);
									
									String playerRankUp = lang.getLanguageString("PlayerRankUp");
									if(!playerRankUp.isEmpty()) {
										try {
											sender.sendMessage(String.format(playerRankUp, player.getName(), newRank.getName()));
										} catch(IllegalFormatException e) {
											logger.log(Level.WARNING, "Error in language file with format of PlayerRankUp, please correct.", e);
										}
									}
								}
							}
						} else {
							String playerHasNoRank = lang.getLanguageString("PlayerHasNoRank");
							if(!playerHasNoRank.isEmpty()) {
								try {
									sender.sendMessage(String.format(playerHasNoRank, player.getName()));
								} catch(IllegalFormatException e) {
									logger.log(Level.WARNING, "Error in language file with format of PlayerHasNoRank, please correct.", e);
								}
							}
						}
					} else {
						String playerNotFound = lang.getLanguageString("PlayerNotFound");
						if(!playerNotFound.isEmpty()) {
							sender.sendMessage(playerNotFound);
						}
					}
				} else {
					String noPermissionsError = lang.getLanguageString("NoPermissionsError");
					if(!noPermissionsError.isEmpty()) {
						sender.sendMessage(noPermissionsError);
					}
				}
			} else {
				if(sender instanceof Player) {
					Player player = (Player) sender;
					Rank rank = rankManager.getUserRank(player);
					if(rank != null) {
						Rank newRank = rankManager.getRank(rank.getId() + 1);
						if(newRank == null) {
							String userMaximumRank = lang.getLanguageString("UserMaximumRank");
							if(!userMaximumRank.isEmpty()) {
								sender.sendMessage(userMaximumRank);
							}
						} else {
							Economy economy = vaultManager.getEconomy();
							// Check if player has required permissions to rankup to that rank.
							if(((sender.hasPermission("gameranks.rankup." + newRank.getName().toLowerCase()) || sender.hasPermission("gameranks.rankup.*")) && plugin.usePermissions) || !plugin.usePermissions){
								if(economy.getBalance(player) >= newRank.getPrice()) {
									if(economy.withdrawPlayer(player, newRank.getPrice()).transactionSuccess()) {
										rankManager.applyRank(player, newRank);
										rankManager.setUserRank(player, newRank);
										newRank = rankManager.getUserRank(player);
										
										String userRankUp = lang.getLanguageString("UserRankUp");
										if(!userRankUp.isEmpty()) {
											try {
												sender.sendMessage(String.format(userRankUp, newRank.getName()));
											} catch(IllegalFormatException e) {
												logger.log(Level.WARNING, "Error in language file with format of UserRankUp, please correct.", e);
											}
										}
									}
								} else {
									String lackOfFunds = lang.getLanguageString("LackOfFunds");
									if(!lackOfFunds.isEmpty()) {
										sender.sendMessage(lackOfFunds);
									}
									sender.sendMessage(ChatColor.RED + "You need " + economy.format(newRank.getPrice() - economy.getBalance(player)) + " more to rankup!");
								}
							} else {
								String noPermissionsError = lang.getLanguageString("NoPermissionsError");
								if(!noPermissionsError.isEmpty()) {
									sender.sendMessage(noPermissionsError);
								}
							}
						}
					} else {
						String userHasNoRank = lang.getLanguageString("UserHasNoRank");
						if(!userHasNoRank.isEmpty()) {
							try {
								sender.sendMessage(String.format(userHasNoRank, player.getName()));
							} catch(IllegalFormatException e) {
								logger.log(Level.WARNING, "Error in language file with format of UserHasNoRank, please correct.", e);
							}
						}
					}
				} else {
					String consoleIsNotAPlayer = lang.getLanguageString("ConsoleIsNotAPlayer");
					if(!consoleIsNotAPlayer.isEmpty()) {
						sender.sendMessage(consoleIsNotAPlayer);
					}
				}
			}
		} else {
			String noPermissionsError = lang.getLanguageString("NoPermissionsError");
			if(!noPermissionsError.isEmpty()) {
				sender.sendMessage(noPermissionsError);
			}
		}
		return true;
	}
	
	private boolean handleRankDownCommand(CommandSender sender, Command cmd, String[] args) {
		Language lang = plugin.lang;
		if(sender.hasPermission("gameranks.commands.rankdown")) {
			RankManager rankManager = plugin.rankManager;
			VaultManager vaultManager = plugin.vaultManager;
			if(args.length > 0) {
				if(sender.hasPermission("gameranks.commands.rankdown.others")) {
					Player player = Bukkit.getPlayer(args[0]);
					if(player != null) {
						Rank rank = rankManager.getUserRank(player);
						if(rank != null) {
							Rank newRank = rankManager.getRank(rank.getId() - 1);
							if(newRank == null) {
								String playerMinimumRank = lang.getLanguageString("PlayerMinimumRank");
								if(!playerMinimumRank.isEmpty()) {
									try {
										sender.sendMessage(String.format(playerMinimumRank, player.getName()));
									} catch(IllegalFormatException e) {
										logger.log(Level.WARNING, "Error in language file with format of PlayerMinimumRank, please correct.", e);
									}
								}
							} else {
								if(sender instanceof Player) {
									// Check if TARGET has needed permissions to rankdown
									if(((player.hasPermission("gameranks.rankdown." + newRank.getName().toLowerCase()) || player.hasPermission("gameranks.rankdown.*")) && plugin.usePermissions) || !plugin.usePermissions){
										Economy economy = vaultManager.getEconomy();
										if(economy.depositPlayer(player, rank.getRefund()).transactionSuccess()) {
											rankManager.applyRank(player, newRank);
											rankManager.setUserRank(player, newRank);
											newRank = rankManager.getUserRank(player);
											
											String playerRankDown = lang.getLanguageString("PlayerRankDown");
											if(!playerRankDown.isEmpty()) {
												try {
													sender.sendMessage(String.format(playerRankDown, player.getName(), newRank.getName()));
												} catch(IllegalFormatException e) {
													logger.log(Level.WARNING, "Error in language file with format of PlayerRankDown, please correct.", e);
												}
											}
										}
									} else {
										String noPermissionsError = lang.getLanguageString("NoPermissionsError");
										if(!noPermissionsError.isEmpty()) {
											sender.sendMessage(noPermissionsError);
										}
									}
								} else {
										rankManager.applyRank(player, newRank);
										rankManager.setUserRank(player, newRank);
										newRank = rankManager.getUserRank(player);
										
										String playerRankDown = lang.getLanguageString("PlayerRankDown");
										if(!playerRankDown.isEmpty()) {
											try {
												sender.sendMessage(String.format(playerRankDown, player.getName(), newRank.getName()));
											} catch(IllegalFormatException e) {
												logger.log(Level.WARNING, "Error in language file with format of PlayerRankDown, please correct.", e);
											}
										}
									}
								}
						} else {
							String playerHasNoRank = lang.getLanguageString("PlayerHasNoRank");
							if(!playerHasNoRank.isEmpty()) {
								try {
									sender.sendMessage(String.format(playerHasNoRank, player.getName()));
								} catch(IllegalFormatException e) {
									logger.log(Level.WARNING, "Error in language file with format of PlayerHasNoRank, please correct.", e);
								}
							}
						}
					} else {
						String playerNotFound = lang.getLanguageString("PlayerNotFound");
						if(!playerNotFound.isEmpty()) {
							sender.sendMessage(playerNotFound);
						}
					}
				} else {
					String noPermissionsError = lang.getLanguageString("NoPermissionsError");
					if(!noPermissionsError.isEmpty()) {
						sender.sendMessage(noPermissionsError);
					}
				}
			} else {
				if(sender instanceof Player) {
					Player player = (Player) sender;
					Rank rank = rankManager.getUserRank(player);
					if(rank != null) {
						Rank newRank = rankManager.getRank(rank.getId() - 1);
						if(newRank == null) {
							String userMinimumRank = lang.getLanguageString("UserMinimumRank");
							if(!userMinimumRank.isEmpty()) {
								sender.sendMessage(userMinimumRank);
							}
						} else {
							if(((player.hasPermission("gameranks.rankdown." + newRank.getName().toLowerCase()) || player.hasPermission("gameranks.rankdown.*")) && plugin.usePermissions) || !plugin.usePermissions){
								Economy economy = vaultManager.getEconomy();
									if(economy.depositPlayer(player, rank.getRefund()).transactionSuccess()) {
										rankManager.applyRank(player, newRank);
										rankManager.setUserRank(player, newRank);
										newRank = rankManager.getUserRank(player);
										
										String userRankDown = lang.getLanguageString("UserRankDown");
										if(!userRankDown.isEmpty()) {
											try {
												sender.sendMessage(String.format(userRankDown, newRank.getName()));
											} catch(IllegalFormatException e) {
												logger.log(Level.WARNING, "Error in language file with format of UserRankDown, please correct.", e);
											}
										}
									}
							} else {
								String noPermissionsError = lang.getLanguageString("NoPermissionsError");
								if(!noPermissionsError.isEmpty()) {
									sender.sendMessage(noPermissionsError);
								}
							}
						}
					} else {
						String userHasNoRank = lang.getLanguageString("UserHasNoRank");
						if(!userHasNoRank.isEmpty()) {
							try {
								sender.sendMessage(String.format(userHasNoRank, player.getName()));
							} catch(IllegalFormatException e) {
								logger.log(Level.WARNING, "Error in language file with format of UserHasNoRank, please correct.", e);
							}
						}
					}
				} else {
					String consoleIsNotAPlayer = lang.getLanguageString("ConsoleIsNotAPlayer");
					if(!consoleIsNotAPlayer.isEmpty()) {
						sender.sendMessage(consoleIsNotAPlayer);
					}
				}
			}
		} else {
			String noPermissionsError = lang.getLanguageString("NoPermissionsError");
			if(!noPermissionsError.isEmpty()) {
				sender.sendMessage(noPermissionsError);
			}
		}
		return true;
	}
	
	private boolean handleGameRanksCommand(CommandSender sender, Command cmd, String[] args) {
		Language lang = plugin.lang;
		if(sender.hasPermission("gameranks.commands.gameranks")) {
			if(args.length > 0) {
				switch(args[0].toLowerCase()) {
					case "version":
						if(sender.hasPermission("gameranks.commands.gameranks.version")) {
							PluginDescriptionFile pluginDesc = plugin.getDescription();
							String authors = "";
							for(String author : pluginDesc.getAuthors()) {
								authors += author + ", ";
							}
							if(!authors.isEmpty()) {
								authors = authors.substring(0, authors.length() - 2);
							}
							sender.sendMessage(ChatColor.WHITE + (pluginDesc.getName() + "-" + pluginDesc.getVersion() + " by " + authors + "."));
						} else {
							String noPermissionsError = lang.getLanguageString("NoPermissionsError");
							if(!noPermissionsError.isEmpty()) {
								sender.sendMessage(noPermissionsError);
							}
						}
						break;
					case "reload":
						if(sender.hasPermission("gameranks.commands.gameranks.reload")) {
							plugin.reload();
							String reloadMessage = lang.getLanguageString("ReloadMessage");
							if(!reloadMessage.isEmpty()) {
								sender.sendMessage(reloadMessage);
							}
						} else {
							String noPermissionsError = lang.getLanguageString("NoPermissionsError");
							if(!noPermissionsError.isEmpty()) {
								sender.sendMessage(noPermissionsError);
							}
						}
						break;
					case "help": 
						showHelp(sender);
						break;
					default:
						sender.sendMessage(lang.getLanguageString("CommandNotFound"));
						break;
				}
			} else {
				showHelp(sender);
			}
		} else {
			String noPermissionsError = lang.getLanguageString("NoPermissionsError");
			if(!noPermissionsError.isEmpty()) {
				sender.sendMessage(noPermissionsError);
			}
		}
		return true;
	}
	
	private void showHelp(CommandSender sender) {
		Language lang = plugin.lang;
		if(sender.hasPermission("gameranks.commands.gameranks")) {
			String helpHeader = lang.getLanguageString("HelpHeader");
			if(!helpHeader.isEmpty()) {
				sender.sendMessage(helpHeader);
			}
			String helpLine = lang.getLanguageString("HelpLine");
			if(!helpLine.isEmpty()) {
				for(String commandName : plugin.getDescription().getCommands().keySet()) {
					Command cmd = plugin.getCommand(commandName);
					if(cmd != null) {
						try {
							sender.sendMessage(String.format(helpLine, cmd.getName(), cmd.getDescription()));
						} catch(IllegalFormatException e) {
							
						}	
					}
				}
			}
			String helpFooter = lang.getLanguageString("HelpFooter");
			if(!helpFooter.isEmpty()) {
				sender.sendMessage(helpFooter);
			}
		} else {
			String noPermissionsError = lang.getLanguageString("NoPermissionsError");
			if(!noPermissionsError.isEmpty()) {
				sender.sendMessage(noPermissionsError);
			}
		}
	}
	
}
