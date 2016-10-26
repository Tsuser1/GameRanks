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
import io.voidpowered.gameranks.config.GRConfiguration;
import io.voidpowered.gameranks.config.Language;
import io.voidpowered.gameranks.manager.CooldownManager;
import io.voidpowered.gameranks.manager.RankManager;
import io.voidpowered.gameranks.manager.VaultManager;
import io.voidpowered.gameranks.util.CooldownType;
import net.milkbowl.vault.economy.Economy;

public final class GRCommands implements CommandExecutor {

	private GameRanks plugin;
	private Logger logger;
	private CooldownManager cooldown;
	
	public GRCommands(GameRanks plugin, GRConfiguration users) {
		this.plugin = plugin;
		this.logger = plugin.getLogger();
		this.cooldown = new CooldownManager(users);
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
			case "setrank":
				return handleSetRankCommand(sender, cmd, args);
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	private boolean handleSetRankCommand(CommandSender sender, Command cmd, String[] args){
		Language lang = plugin.lang;
		if(sender.hasPermission("gameranks.commands.setrank")){
			RankManager rankManager = plugin.rankManager;
			if(args.length == 2){
				Rank rank = null;
				for(Rank rankSearch : rankManager.getRanks()) {
					if(rankSearch.getName().equalsIgnoreCase(args[0])) {
						rank = rankSearch;
						break;
					}
				}
				if(rank != null){
					Player player = Bukkit.getPlayer(args[1]);
					if(player == null) {
						String playerNotFound = lang.getLanguageString("PlayerNotFound");
						if(!playerNotFound.isEmpty()) {
							sender.sendMessage(playerNotFound);
						}
					} else { // ELSE: Bukkit couldn't find the player, returned null
						rankManager.applyRank(player, rank);
					}
				} else { // ELSE: The rank couldn't be found
					String rankNotFound = lang.getLanguageString("RankNotFound");
					if(!rankNotFound.isEmpty()) {
						sender.sendMessage(rankNotFound);
					}
				}
			} else if(args.length == 1) { // ELSE IF( There is one argument in the command ): Search for specified rank
				if(sender instanceof Player) {
					Rank rank = null;
					for(Rank rankSearch : rankManager.getRanks()) {
						if(rankSearch.getName().equalsIgnoreCase(args[0])) {
							rank = rankSearch;
							break;
						}
					}
					if(rank != null){
						rankManager.applyRank((Player) sender, rank);
					} else { // ELSE: Rank couldn't be found
						String rankNotFound = lang.getLanguageString("RankNotFound");
						if(!rankNotFound.isEmpty()) {
							sender.sendMessage(rankNotFound);
						}
					}
				} else { // ELSE: Sender isn't a player
					String consoleIsNotAPlayer = lang.getLanguageString("ConsoleIsNotAPlayer");
					if(!consoleIsNotAPlayer.isEmpty()) {
						sender.sendMessage(consoleIsNotAPlayer);
					}
				}
			} else { // ELSE: Sender used incorrect arguments
				//TODO: Add proper usage message implementation
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4Error&8: &cUsage: /setrank [player] <rank>"));
			}
		} else { // ELSE: Sender doesn't have permission to set ranks
			String noPermissionsError = lang.getLanguageString("NoPermissionsError");
			if(!noPermissionsError.isEmpty()) {
				sender.sendMessage(noPermissionsError);
			}
		}
		return true;
	}

	private boolean handleRanksCommand(CommandSender sender, Command cmd, String[] args) {
		Language lang = plugin.lang;
		if(sender.hasPermission("gameranks.commands.ranks")) {
			RankManager rankManager = plugin.rankManager;
			if((args.length > 0) ? plugin.rankManager.rankExists(args[0]) : false) {
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
					} else { // ELSE: The rank doesn't have a description
						String noRankDescription = lang.getLanguageString("NoRankDescription");
						if(!noRankDescription.isEmpty()) {
							sender.sendMessage(noRankDescription);
						}
					}
				} else { // ELSE: Rank wasn't found
					String rankNotFound = lang.getLanguageString("RankNotFound");
					if(!rankNotFound.isEmpty()) {
						sender.sendMessage(rankNotFound);
					}
				}
			} else { // ELSE: No rank was specified or was not found
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
						} else { // ELSE: Page number is valid
							String ranksListTitle = lang.getLanguageString("RanksListTitle");
							if(!ranksListTitle.isEmpty()) {
								//TODO: Add configurable page list format.
								sender.sendMessage(ranksListTitle + ChatColor.DARK_GRAY + " <" + ChatColor.GREEN + index + ChatColor.DARK_GRAY + "/" + ChatColor.GREEN + Integer.valueOf(ranks.length / 8 + (ranks.length % 8 > 0 ? 1 : 0)) + ChatColor.DARK_GRAY + ">");
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
				} else { // ELSE: There are no ranks setup to display!
					String noRanksSetup = lang.getLanguageString("NoRanksSetup");
					if(!noRanksSetup.isEmpty()) {
						sender.sendMessage(noRanksSetup);
					}
				}
			}
		} else { // ELSE: Sender doesn't have permission to view all ranks
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
						} else { // ELSE: Target has no rank to report
							String playerHasNoRank = lang.getLanguageString("PlayerHasNoRank");
							if(!playerHasNoRank.isEmpty()) {
								try {
									sender.sendMessage(String.format(playerHasNoRank, player.getName()));
								} catch(IllegalFormatException e) {
									logger.log(Level.WARNING, "Error in language file with format of PlayerHasNoRank, please correct.", e);
								}
							}
						}
					} else { // ELSE: Target couldn't be found
						String playerNotFound = lang.getLanguageString("PlayerNotFound");
						if(!playerNotFound.isEmpty()) {
							sender.sendMessage(playerNotFound);
						}
					}
				} else { // ELSE: Sender doesn't have permission to view others rank
					String noPermissionsError = lang.getLanguageString("NoPermissionsError");
					if(!noPermissionsError.isEmpty()) {
						sender.sendMessage(noPermissionsError);
					}
				}
			} else { // ELSE: There is no arguments, target is sender
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
					} else { // ELSE: Sender has no rank
						String userHasNoRank = lang.getLanguageString("UserHasNoRank");
						if(!userHasNoRank.isEmpty()) {
							sender.sendMessage(userHasNoRank);
						}
					}
				} else { // ELSE: Sender isn't a player
					String consoleIsNotAPlayer = lang.getLanguageString("ConsoleIsNotAPlayer");
					if(!consoleIsNotAPlayer.isEmpty()) {
						sender.sendMessage(consoleIsNotAPlayer);
					}
				}
			}
		} else { // ELSE: Sender doesn't have permission to use rank info
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
							} else { // ELSE: Player is not at maximum rank
								if(sender instanceof Player) {
									Player executor = (Player) sender;
									Economy economy = vaultManager.getEconomy();
									// Check if TARGET has required permissions to rankup to that
									if(((player.hasPermission("gameranks.rankup." + newRank.getName().toLowerCase()) || player.hasPermission("gameranks.rankup.*")) && plugin.usePermissions) || !plugin.usePermissions){
										if(economy.getBalance(executor) >= newRank.getPrice()) {
											if(!cooldown.isCooling(player.getUniqueId(), CooldownType.RANKUP, 3L)){
												if(economy.withdrawPlayer(executor, newRank.getPrice()).transactionSuccess()) {
													rankManager.applyRank(player, newRank);
													rankManager.setUserRank(player, newRank);
													cooldown.setCooldown(executor.getUniqueId(), CooldownType.RANKUP);
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
											} else { // ELSE: Target is still in rankup command cooldown
													String playerInCooldown = lang.getLanguageString("PlayerInCooldown");
													if(!playerInCooldown.isEmpty()) {
														try {
															sender.sendMessage(String.format(playerInCooldown, player.getName()));
														} catch(IllegalFormatException e) {
															logger.log(Level.WARNING, "Error in language file with format of PlayerInCooldown, please correct.", e);
														}
													}
												}
										} else { // ELSE: Sender doesn't have enough money to rankup target
											String lackOfFunds = lang.getLanguageString("LackOfFunds");
											if(!lackOfFunds.isEmpty()) {
												sender.sendMessage(lackOfFunds);
											}
										}
									} else { // ELSE: Target doesn't have the specific permission to rank up that rank.
										String noPermissionsError = lang.getLanguageString("NoPermissionsError");
										if(!noPermissionsError.isEmpty()) {
											sender.sendMessage(noPermissionsError);
										}
									}
								} else { // ELSE: Sender isn't a player *Bypasses economy*
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
						} else { // ELSE: Target has no rank!
							String playerHasNoRank = lang.getLanguageString("PlayerHasNoRank");
							if(!playerHasNoRank.isEmpty()) {
								try {
									sender.sendMessage(String.format(playerHasNoRank, player.getName()));
								} catch(IllegalFormatException e) {
									logger.log(Level.WARNING, "Error in language file with format of PlayerHasNoRank, please correct.", e);
								}
							}
						}
					} else { // ELSE: Target's rank cannot be found
						String playerNotFound = lang.getLanguageString("PlayerNotFound");
						if(!playerNotFound.isEmpty()) {
							sender.sendMessage(playerNotFound);
						}
					}
				} else { // ELSE: Sender doesn't have the permission to rankup other players
					String noPermissionsError = lang.getLanguageString("NoPermissionsError");
					if(!noPermissionsError.isEmpty()) {
						sender.sendMessage(noPermissionsError);
					}
				}
			} else { // ELSE: There is no target, execute on the sender
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
						} else { // ELSE: The is another rank above this one to rankup to
							Economy economy = vaultManager.getEconomy();
							// Check if player has required permissions to rankup to that rank.
							if(((sender.hasPermission("gameranks.rankup." + newRank.getName().toLowerCase()) || sender.hasPermission("gameranks.rankup.*")) && plugin.usePermissions) || !plugin.usePermissions){
								if(economy.getBalance(player) >= newRank.getPrice()) {
									//TODO: Add configuration option for cooldown times
									if(!cooldown.isCooling(player.getUniqueId(), CooldownType.RANKUP, 3L)){
										if(economy.withdrawPlayer(player, newRank.getPrice()).transactionSuccess()) {
											rankManager.applyRank(player, newRank);
											rankManager.setUserRank(player, newRank);
											newRank = rankManager.getUserRank(player);
											cooldown.setCooldown((Player) sender, CooldownType.RANKUP);
											String userRankUp = lang.getLanguageString("UserRankUp");
											if(!userRankUp.isEmpty()) {
												try {
													sender.sendMessage(String.format(userRankUp, newRank.getName()));
												} catch(IllegalFormatException e) {
													logger.log(Level.WARNING, "Error in language file with format of UserRankUp, please correct.", e);
												}
											}
										}
									} else { // ELSE: Player is still in a rankup cooldown
										String playerInCooldown = lang.getLanguageString("PlayerInCooldown");
										if(!playerInCooldown.isEmpty()) {
											try {
												sender.sendMessage(String.format(playerInCooldown, player.getName()));
											} catch(IllegalFormatException e) {
												logger.log(Level.WARNING, "Error in language file with format of PlayerInCooldown, please correct.", e);
											}
										}
									}
								} else { // ELSE: Player doesn't have enough money.
									String lackOfFunds = lang.getLanguageString("LackOfFunds");
									if(!lackOfFunds.isEmpty()) {
										sender.sendMessage(lackOfFunds);
									}
									sender.sendMessage(ChatColor.RED + "You need " + economy.format(newRank.getPrice() - economy.getBalance(player)) + " more to rankup!");
								}
							} else { // ELSE: Player doesn't have the specific permission to rankup to that rank
								String noPermissionsError = lang.getLanguageString("NoPermissionsError");
								if(!noPermissionsError.isEmpty()) {
									sender.sendMessage(noPermissionsError);
								}
							}
						}
					} else { // ELSE: Player doesn't have a rank
						String userHasNoRank = lang.getLanguageString("UserHasNoRank");
						if(!userHasNoRank.isEmpty()) {
							try {
								sender.sendMessage(String.format(userHasNoRank, player.getName()));
							} catch(IllegalFormatException e) {
								logger.log(Level.WARNING, "Error in language file with format of UserHasNoRank, please correct.", e);
							}
						}
					}
				} else { // ELSE: Sender isn't a player
					String consoleIsNotAPlayer = lang.getLanguageString("ConsoleIsNotAPlayer");
					if(!consoleIsNotAPlayer.isEmpty()) {
						sender.sendMessage(consoleIsNotAPlayer);
					}
				}
			}
		} else { // ELSE: Player doesn't have the permissions for the rankup command
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
					@SuppressWarnings("deprecation")
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
										if(!cooldown.isCooling(player.getUniqueId(), CooldownType.RANKDOWN, 3L)){
											Economy economy = vaultManager.getEconomy();
											if(economy.depositPlayer(player, rank.getRefund()).transactionSuccess()) {
												rankManager.applyRank(player, newRank);
												rankManager.setUserRank(player, newRank);
												newRank = rankManager.getUserRank(player);
												cooldown.setCooldown(player, CooldownType.RANKDOWN);
												String playerRankDown = lang.getLanguageString("PlayerRankDown");
												if(!playerRankDown.isEmpty()) {
													try {
														sender.sendMessage(String.format(playerRankDown, player.getName(), newRank.getName()));
													} catch(IllegalFormatException e) {
														logger.log(Level.WARNING, "Error in language file with format of PlayerRankDown, please correct.", e);
													}
												}
											}
										} else { // ELSE: Player is still cooling down from a rankdown
											String playerInCooldown = lang.getLanguageString("PlayerInCooldown");
											if(!playerInCooldown.isEmpty()) {
												try {
													sender.sendMessage(String.format(playerInCooldown, player.getName()));
												} catch(IllegalFormatException e) {
													logger.log(Level.WARNING, "Error in language file with format of PlayerInCooldown, please correct.", e);
												}
											}
										}
									} else { // ELSE: Target player doesn't have the needed permission to rankdown to that rank
										String noPermissionsError = lang.getLanguageString("NoPermissionsError");
										if(!noPermissionsError.isEmpty()) {
											sender.sendMessage(noPermissionsError);
										}
									}
								} else { // ELSE: The executor of the command isn't a player
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
						} else { // ELSE: Target player doesn't have a rank
							String playerHasNoRank = lang.getLanguageString("PlayerHasNoRank");
							if(!playerHasNoRank.isEmpty()) {
								try {
									sender.sendMessage(String.format(playerHasNoRank, player.getName()));
								} catch(IllegalFormatException e) {
									logger.log(Level.WARNING, "Error in language file with format of PlayerHasNoRank, please correct.", e);
								}
							}
						}
					} else { // ELSE: Bukkit couldn't get the player and returned null
						String playerNotFound = lang.getLanguageString("PlayerNotFound");
						if(!playerNotFound.isEmpty()) {
							sender.sendMessage(playerNotFound);
						}
					}
				} else { // ELSE: Player doesn't have permission to rankdown other people.
					String noPermissionsError = lang.getLanguageString("NoPermissionsError");
					if(!noPermissionsError.isEmpty()) {
						sender.sendMessage(noPermissionsError);
					}
				}
			} else { // ELSE: There is no arguments, execute on the sender instead of a target
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
						} else { // ELSE: There is a lower rank, rankdown possible
							if(((player.hasPermission("gameranks.rankdown." + newRank.getName().toLowerCase()) || player.hasPermission("gameranks.rankdown.*")) && plugin.usePermissions) || !plugin.usePermissions){
								Economy economy = vaultManager.getEconomy();
								if(!cooldown.isCooling(player.getUniqueId(), CooldownType.RANKDOWN, 3L)){
									if(economy.depositPlayer(player, rank.getRefund()).transactionSuccess()) {
										rankManager.applyRank(player, newRank);
										rankManager.setUserRank(player, newRank);
										newRank = rankManager.getUserRank(player);
										cooldown.setCooldown(player, CooldownType.RANKDOWN);
										String userRankDown = lang.getLanguageString("UserRankDown");
										if(!userRankDown.isEmpty()) {
											try {
												sender.sendMessage(String.format(userRankDown, newRank.getName()));
											} catch(IllegalFormatException e) {
												logger.log(Level.WARNING, "Error in language file with format of UserRankDown, please correct.", e);
											}
										}
									}
								} else { // ELSE: If player is still cooling down
									String playerInCooldown = lang.getLanguageString("PlayerInCooldown");
									if(!playerInCooldown.isEmpty()) {
										try {
											sender.sendMessage(String.format(playerInCooldown, player.getName()));
										} catch(IllegalFormatException e) {
											logger.log(Level.WARNING, "Error in language file with format of PlayerInCooldown, please correct.", e);
										}
									}
								}
							} else { // ELSE: Player doesn't have permission to rankdown to that rank
								String noPermissionsError = lang.getLanguageString("NoPermissionsError");
								if(!noPermissionsError.isEmpty()) {
									sender.sendMessage(noPermissionsError);
								}
							}
						}
					} else { // ELSE: Player doesn't have a rank
						String userHasNoRank = lang.getLanguageString("UserHasNoRank");
						if(!userHasNoRank.isEmpty()) {
							try {
								sender.sendMessage(String.format(userHasNoRank, player.getName()));
							} catch(IllegalFormatException e) {
								logger.log(Level.WARNING, "Error in language file with format of UserHasNoRank, please correct.", e);
							}
						}
					}
				} else { // ELSE: Player isn't actually a player
					String consoleIsNotAPlayer = lang.getLanguageString("ConsoleIsNotAPlayer");
					if(!consoleIsNotAPlayer.isEmpty()) {
						sender.sendMessage(consoleIsNotAPlayer);
					}
				}
			}
		} else { // ELSE: Player doesn't have the permissions to command rankdown
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
			} else { // ELSE: The isn't enough arguments in the command
				showHelp(sender);
			}
		} else { // ELSE: Player doesn't have permissions to the command gameranks
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
		} else { // ELSE: Player doesn't have permissions to the command gameranks. How did they get here?
			String noPermissionsError = lang.getLanguageString("NoPermissionsError");
			if(!noPermissionsError.isEmpty()) {
				sender.sendMessage(noPermissionsError);
			}
		}
	}
	
}
