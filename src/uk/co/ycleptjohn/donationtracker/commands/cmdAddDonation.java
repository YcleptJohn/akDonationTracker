package uk.co.ycleptjohn.donationtracker.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import uk.co.ycleptjohn.donationtracker.configuration.Config;
import uk.co.ycleptjohn.donationtracker.configuration.ConfigFile;
import uk.co.ycleptjohn.donationtracker.configuration.ConfigHandler;
import uk.co.ycleptjohn.donationtracker.configuration.NameFetcher;

public class cmdAddDonation implements CommandExecutor {
	private ConfigHandler cf;
	private ConfigFile settingsCfg;
	private ConfigFile dataCfg;
	
	@Override
	public boolean onCommand(CommandSender cmdP, Command cmd, String cmdLbl, String[] args) {
		cf = new ConfigHandler();
		cf.regenerateAllMissingConfigs();
		settingsCfg = cf.get(Config.SETTINGS);
		dataCfg = cf.get(Config.DATA);
		settingsCfg.reload();
		dataCfg.reload();
		
		if(!((cmdP instanceof Player && cmdP.hasPermission("arise.donation.add")) || !(cmdP instanceof Player))) {
			cmdP.sendMessage(ezColour("&cYou don't have permission to perform this action"));
		} else {
			if(args == null || args.length < 2 || args.length > 2) {
				cmdP.sendMessage(ezColour("&cInvalid format: 2 arguments required. (uuid:amount)"));
				System.out.println("#$# DonationTracker error:");
				System.out.println("Command: adddonation");
				System.out.println("args given: " + Arrays.deepToString(args));
				System.out.println("Error: invalid argument length");
			} else {
				DEBUG("Accepted args");
				UUID uuid = null;
				double amount = 0;
				try {
					amount = Double.valueOf(args[1]);
					uuid = UUID.fromString(args[0]);
				} catch (NullPointerException e) {
					cmdP.sendMessage(ezColour("&cInvalid donation amount entered"));
					System.out.println("#$# DonationTracker error:");
					System.out.println("Command: adddonation");
					System.out.println("args given: " + Arrays.deepToString(args));
					System.out.println("Error: invalid donation amount");
					return false;
				} catch (IllegalArgumentException e1) {
					cmdP.sendMessage(ezColour("&cInvalid UUID given"));
					System.out.println("#$# DonationTracker error:");
					System.out.println("Command: adddonation");
					System.out.println("args given: " + Arrays.deepToString(args));
					System.out.println("Error: invalid uuid(or format) given");
					return false;
				}
				DEBUG("Post-input tokenized: ");
				DEBUG("  -  uuid: " + uuid);
				DEBUG("  -  amount: " + amount);
				double currentAmount = 0;
				if(dataCfg.contains("users." + uuid)) {
					currentAmount = dataCfg.getDouble("users." + uuid);
				}
				double newTotal = currentAmount + amount;
				DEBUG("User started with: $" + currentAmount);
				DEBUG("They donated: $" + amount);
				DEBUG("They now have: $" + newTotal);
				dataCfg.set("users." + uuid, newTotal);
				try {
					dataCfg.save(dataCfg.getFile());
				} catch (IOException e) {
					System.out.println("ERROR saving config after new donation amount");
					e.printStackTrace();
				}

				if(!settingsCfg.getString("messages.donation-broadcast").equals("false")) {
					String msg = settingsCfg.getString("messages.donation-broadcast");
					NameFetcher nf = new NameFetcher(Arrays.asList(uuid));
					Map<UUID, String> nameResult = null;
					try {
						nameResult = nf.call();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					String username = "";
					try {
						username = nameResult.get(uuid);
						msg = msg.replace("{username}", username);
					} catch(NullPointerException e) {
						msg = msg.replace("{username}", "&4ERR: Rate limit 429&f");
					}
					msg = msg.replace("{amount}", "" + amount);
					msg = msg.replace("{total}", "" + newTotal);
					Bukkit.broadcastMessage(ezColour(msg));
				}
				updateRank(uuid);
			}
		}


		return true;
	}

	public String ezColour(String text) {
		return ChatColor.translateAlternateColorCodes('&', text);
	}

	public void DEBUG(String info) {
		System.out.println("DonationTracker VERBOSE DEBUG: " + info);
	}

	public void updateRank(UUID uuid) {
		if(dataCfg.contains("users." + uuid)) {
			
			double usersTotal = dataCfg.getDouble("users." + uuid);
			DEBUG("UpdateRank section");
			DEBUG("Users total: " + usersTotal);
			Set<String> ranksSet = settingsCfg.getConfigurationSection("ranks").getKeys(false);
			List<String> ranks = new ArrayList<String>(ranksSet);
			List<String> surpassedRanks = new ArrayList<String>();
			String correctRank = "$";
			boolean rankFound = false;
			for(int i = 0; i<ranks.size(); i++) {
				double boundary = settingsCfg.getDouble("ranks." + ranks.get(i) + ".cost");
				DEBUG(ranks.get(i).toString());
				DEBUG("Bound: " + boundary);
				if(usersTotal >= boundary) {
					if(!rankFound) {
						DEBUG("rank found");
						correctRank = ranks.get(i);
						DEBUG("Correct rank: " + correctRank);
						rankFound = true;
					} else {
						surpassedRanks.add(ranks.get(i));
						DEBUG("Added surpassed rank: " + ranks.get(i));
					}
				}
			}
			
			String rankIdentityPerm = "group." + correctRank;
			DEBUG("Rank-perm: " + rankIdentityPerm);
			Player pl = Bukkit.getOfflinePlayer(uuid).getPlayer();
			if(!(pl == null) && !pl.hasPermission(rankIdentityPerm)) {
				ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
				String pexCmd = "pex user " + uuid + " group add " + correctRank;
				Bukkit.dispatchCommand(console, pexCmd);
				for(String rank : surpassedRanks) {
					pexCmd = "pex user " + uuid + " group remove " + rank;
					Bukkit.dispatchCommand(console, pexCmd);
					DEBUG("Removed a surpassed rank from user: " + rank);
				}
				
				String msg = settingsCfg.getString("messages.rankup-reached");
				String rankNameFormat = settingsCfg.getString("ranks." + correctRank + ".format");
				DEBUG("msg: " + msg);
				DEBUG("format: " + rankNameFormat);
				msg = msg.replace("{format}", rankNameFormat);
				Bukkit.broadcastMessage(ezColour(msg));
			} else {
				String msg = settingsCfg.getString("messages.non-rankup");
				if(!msg.equals("false")) {
					Bukkit.broadcastMessage(ezColour(msg));
				}
			}

		} else {
			System.out.println("User not found when updating ranks: " + uuid);
			System.out.println("#$# DonationTracker error:");
			System.out.println("Command: adddonation");
			System.out.println("uuid given: " + uuid);
			System.out.println("Error: invalid uuid(or format) given");
			System.out.println("NOTE: CAN BE CAUSED BY A USER WHO HAS NEVER LOGGED IN HERE!");
		}
	}

}
