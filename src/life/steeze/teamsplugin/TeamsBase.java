package life.steeze.teamsplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;


public class TeamsBase extends JavaPlugin implements Listener {


    HashMap<Player, String> invites = new HashMap<>();

    private static TeamsBase inst;



    public void console(String x) {
        getServer().getConsoleSender().sendMessage(x);
    }

    public void sendInvite(Player p, String team, String inviter) {
        p.sendMessage("You have been invited by " + inviter + " to join the team: " + team);
        p.sendMessage("Do you accept? /accept (30 seconds to accept)");
        invites.put(p, team);
        new BukkitRunnable() {

            @Override
            public void run() {
                invites.remove(p);
            }
        }.runTaskLaterAsynchronously(TeamsBase.inst, 20 * 30);

    }



    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        inst = this;
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        FileManager.getTeamsData();
        console("TeamsPlugin loading...");
        FileManager.get().options().copyDefaults(true);
    }

    @Override
    public void onDisable() {
        console("TeamsPlugin unloading...");
        FileManager.save();
        inst = null;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && e instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) e).getDamager() instanceof Player) {
            if (FileManager.getTeamOf(e.getEntity().getUniqueId()).equals(FileManager.getTeamOf(((EntityDamageByEntityEvent) e).getDamager().getUniqueId()))) {
                if (FileManager.getTeamOf(e.getEntity().getUniqueId()).equals("none")) {
                    e.setCancelled(false);
                } else {
                    e.setCancelled(true);
                }
            }

        }


    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("accept")) {
            Player p = (Player) sender;
            if (invites.containsKey(p)) {
                FileManager.addValue(invites.get(p), p.getUniqueId());
                p.sendMessage(ChatColor.GOLD + "Successfully joined " + ChatColor.RED + invites.get(p));
            } else {
                p.sendMessage("NO INVITATION TO ACCEPT BUBBY");
            }
        }
        if (command.getName().equals("team")) {
            Player p = (Player) sender;
            if (args.length == 0) {
                p.sendMessage("Teams plugin by ViperCobra. /team <create [name] -OR- [player to invite]>");
            } else if (args.length == 1) {
                if (!args[0].equalsIgnoreCase("create")) {
                    if (args[0].equals("leave")) {
                        if (!FileManager.getTeamOf(p).equals("none")) {
                            if (FileManager.get().getStringList(FileManager.getTeamOf(p)).size() == 1) {
                                p.sendMessage(ChatColor.RED + "You are the only person on your team. Disbanding...");
                                FileManager.get().set(FileManager.getTeamOf(p), null);
                            }
                            FileManager.removeValue(FileManager.getTeamOf(p), p.getUniqueId());
                            p.sendMessage(ChatColor.RED + "Successfully left your team");
                        } else {
                            p.sendMessage("YOU'RE NOT ON A TEAM!");
                        }
                    } else if (!FileManager.getTeamOf(p).equals("none")) {
                        Player t = Bukkit.getPlayer(args[0]);
                        if (t == null) {
                            p.sendMessage("PLAYER OFFLINE OR DOESN'T EXIST SMART MAN");
                        } else {
                            if(!FileManager.getTeamOf(t).equals("none")){
                                p.sendMessage(ChatColor.RED + "PLAYER IS ON A TEAM ALREADY BRO GET IT TOGETHER");
                            } else
                            if(!p.getUniqueId().equals(t.getUniqueId())){
                                sendInvite(t, FileManager.getTeamOf(p), p.getDisplayName());
                                p.sendMessage(ChatColor.RED + "INVITATION SENT! CONGRATS!!");
                            } else{
                                p.sendMessage(ChatColor.RED + "YOU CAN'T INVITE YOURSELF");
                            }

                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "YOU NEED TO BE ON A TEAM BUBFORD");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "SPECIFY A TEAM NAME BUBFORD!");
                }
            } else if (args.length == 2) {
                if (args[0].equals("create")) {
                    if (FileManager.getTeamOf(p).equals("none")) {
                        boolean an = true;
                        for (char c : args[1].toCharArray()) {

                            if (Character.isDigit(c) || Character.isAlphabetic(c)) {} else {
                                an = false;
                            }

                        }
                        if (!an) {
                            p.sendMessage(ChatColor.RED + "INVALID TEAM NAME SORRY BUD");
                        } else {
                            if (FileManager.get().contains(args[1])) {
                                p.sendMessage(ChatColor.RED + "TEAM ALREADY EXISTS BUD");
                            } else {
                                p.sendMessage(ChatColor.RED + "Creating team " + ChatColor.YELLOW + args[1]);
                                FileManager.get().createSection(args[1]);
                                FileManager.addValue(args[1], p.getUniqueId());
                                Bukkit.broadcastMessage(ChatColor.RED + "Team " + ChatColor.YELLOW + args[1] + ChatColor.RED + " has been created!");
                            }
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "YOU'RE ON A TEAM ALREADY");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "WHAT ARE YOU TRYING TO SAY?? [INVALID COMMAND]");
                }
            } else {
                p.sendMessage(ChatColor.RED + "TOO MANY WORDS");
            }
        }
        return false;
    }

    @EventHandler
    public void chatFormat(AsyncPlayerChatEvent event) {
        if(getConfig().get("format-chat").equals(true)) {
            Player p = event.getPlayer();
            if (!FileManager.getTeamOf(p).equals("none")) {
                event.setFormat(ChatColor.DARK_GRAY + "[" + ChatColor.RED + FileManager.getTeamOf(p) + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY + p.getDisplayName() + ": " + ChatColor.WHITE + event.getMessage());
            } else {
                event.setFormat(ChatColor.GRAY + p.getDisplayName() + ": " + ChatColor.WHITE + event.getMessage());
            }
        }
    }
}
