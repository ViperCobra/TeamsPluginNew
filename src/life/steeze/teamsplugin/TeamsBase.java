package life.steeze.teamsplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.awt.*;
import java.util.*;
import java.util.List;


public class TeamsBase extends JavaPlugin implements Listener {


    HashMap<Player, String> invites = new HashMap<>();

    public HashMap<UUID, Point> pos1s = new HashMap<>();
    public HashMap<UUID, Point> pos2s = new HashMap<>();

    public boolean formatChat = false;

    private int minWidth = getConfig().getInt("minimumClaimWidth");
    private int maxDistance = getConfig().getInt("maximumClaimCornerDistance");

    private String chatFormat = getConfig().getString("formatted-chat");
    private String noTeamFormat = getConfig().getString("formatted-chat-no-team-found-for-player");

    public static TeamsBase inst;

    public String pointString(Point p) {
        return p.x + ", " + p.y;
    }


    public void console(String x) {
        getServer().getConsoleSender().sendMessage(x);
    }

    public void sendInvite(Player p, String team, String inviter) {
        p.sendMessage(ChatColor.YELLOW + "You have been invited by " +ChatColor.WHITE+ inviter +ChatColor.YELLOW+ " to join the team: " +ChatColor.WHITE+ team);
        p.sendMessage(ChatColor.YELLOW + "Do you accept? /accept (30 seconds to accept)");
        invites.put(p, team);
        new BukkitRunnable() {

            @Override
            public void run() {
                invites.remove(p);
            }
        }.runTaskLaterAsynchronously(inst, 20 * 30);

    }


    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        inst = this;
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        FileManager.getTeamsData();
        console("TeamsPlugin loading...");
        FileManager.getTeams().options().copyDefaults(true);

        FileManager.loadClaims();
        console("Claims loading");
        FileManager.getClaims().options().copyDefaults(true);

        if(getConfig().getBoolean("format-chat")){
            formatChat = true;
        }

    }

    @Override
    public void onDisable() {
        console("TeamsPlugin unloading...");
        FileManager.save();
        FileManager.saveClaims();
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
        if (command.getName().equals("pos1")) {
            Player p = (Player) sender;
            Point point = new Point(p.getLocation().getBlockX(), p.getLocation().getBlockZ());
            pos1s.put(p.getUniqueId(), point);
            p.sendMessage(ChatColor.YELLOW + "Claim start position set as " + ChatColor.WHITE + pointString(point));
        }

        if (command.getName().equals("pos2")) {
            Player p = (Player) sender;
            Point point = new Point(p.getLocation().getBlockX(), p.getLocation().getBlockZ());
            pos2s.put(p.getUniqueId(), point);
            p.sendMessage(ChatColor.YELLOW + "Claim end position set as " + ChatColor.WHITE + pointString(point));
        }

        if (command.getName().equals("accept")) {
            Player p = (Player) sender;
            if (invites.containsKey(p)) {
                if (FileManager.getRealTeam(invites.get(p)) == null) {
                    p.sendMessage(ChatColor.RED + "That team no longer exists!");
                } else {
                    FileManager.getRealTeam(invites.get(p)).getMembers().add(p.getUniqueId());
                    p.sendMessage(ChatColor.GOLD + "Successfully joined " + ChatColor.RED + invites.get(p));
                    FileManager.saveTeams();
                }
            } else {
                p.sendMessage(ChatColor.RED + "You have no invitation.");
            }
        }

        if (command.getName().equals("team")) {
            Player p = (Player) sender;
            String team = FileManager.getTeamOf(p);
            if (args.length == 0) {
                p.sendMessage(ChatColor.YELLOW + "-----=== Teams Help ===-----");
                p.sendMessage(ChatColor.YELLOW + "/team create <name> " + ChatColor.WHITE + "- Create a team");
                p.sendMessage(ChatColor.YELLOW + "/team <player> " + ChatColor.WHITE + "- Invite a player");
                p.sendMessage(ChatColor.YELLOW + "/team leave " + ChatColor.WHITE + "- Leave/disband your team");
                p.sendMessage(ChatColor.YELLOW + "/pos1, /pos2 " + ChatColor.WHITE + "- Define your claim");
                p.sendMessage(ChatColor.YELLOW + "/claim " + ChatColor.WHITE + "- Make your claim");
                p.sendMessage(ChatColor.YELLOW + "/claim info " + ChatColor.WHITE + "- View your claim");
                p.sendMessage(ChatColor.YELLOW + "/claim delete " + ChatColor.WHITE + "- Delete your claim");
            } else if (args.length == 1) {
                if (!args[0].equalsIgnoreCase("create")) {
                    if (args[0].equals("leave")) {
                        if (!team.equals("none")) {
                            Team team1 = FileManager.getRealTeam(p);
                            if (team1.getMembers().size() == 1) {
                                p.sendMessage(ChatColor.DARK_RED + "You are the only person on your team. Disbanding...");

                                //DELETING TEAM FROM RAM
                                FileManager.getTeamsList().remove(team1);

                                //DELETING TEAM AND CLAIM FROM FILE
                                FileManager.getTeams().set(team + ".members", null);
                                FileManager.getTeams().set(team, null);
                                if (FileManager.getClaims().contains(team)) {
                                    for (Claim c : FileManager.getClaimsList()) {
                                        if (c.getOwner().equals(team)) {
                                            FileManager.removeClaimFromFile(c);
                                            FileManager.getClaimsList().remove(c);
                                            break;
                                        }
                                    }
                                }

                                FileManager.save();


                            } else {


                                FileManager.getRealTeam(p).getMembers().remove(p.getUniqueId());
                                if(team1.getLeader().equals(p.getUniqueId())){
                                    team1.setLeader(team1.getMembers().get(0));
                                    Bukkit.getPlayer(team1.getMembers().get(0)).sendMessage(ChatColor.YELLOW + "You are now the leader of " + ChatColor.WHITE + team);
                                }
                                p.sendMessage(ChatColor.YELLOW + "Successfully left your team");
                                FileManager.saveTeams();
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "You must be on a team!");
                        }
                    } else if (args[0].equals("sethome")) {
                        if (!team.equals("none")) {
                            boolean found = false;
                            Claim claim = null;
                            for (Claim c : FileManager.getClaimsList()) {
                                if (c.getOwner().equals(team)) {
                                    claim = c;
                                    found = true;
                                    break;
                                }
                            }
                            if (found) {
                                if (claim.getBounds().contains(p.getLocation().toVector())) {
                                    claim.setHome(p.getLocation());
                                    FileManager.addClaimHome(claim, p.getLocation());
                                    p.sendMessage(ChatColor.YELLOW + "Team home set.");
                                } else {
                                    p.sendMessage(ChatColor.RED + "You must be inside your claim to set a home.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "No claim found. Make a claim to set a home.");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "You're not on a team");
                        }
                    } else if (args[0].equals("home")) {
                        if (!team.equals("none")) {
                            boolean found = false;
                            Claim claim = null;
                            for (Claim c : FileManager.getClaimsList()) {
                                if (c.getOwner().equals(team)) {
                                    claim = c;
                                    found = true;
                                    break;
                                }
                            }
                            if (found) {
                                if (claim.home != null) {
                                    p.sendMessage(ChatColor.YELLOW + "Teleporting...");
                                    p.teleport(claim.home);
                                } else {
                                    p.sendMessage(ChatColor.RED + "No home found");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "No claim found");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "No team found");
                        }

                    } else if (!team.equals("none")) {
                        Player t = Bukkit.getPlayer(args[0]);
                        if (t == null) {
                            p.sendMessage(ChatColor.RED + "Player offline or doesn't exist");
                        } else {
                            if (FileManager.getRealTeam(t) != null) {
                                p.sendMessage(ChatColor.RED + "That player is already on a team.");
                            } else if (!p.getUniqueId().equals(t.getUniqueId())) {
                                sendInvite(t, team, p.getDisplayName());
                                p.sendMessage(ChatColor.YELLOW + "Invitation sent!");
                            } else {
                                p.sendMessage(ChatColor.RED + "You can't invite yourself!");
                            }

                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "You must be on a team");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "Specify a team name");
                }
            } else if (args.length == 2) {
                if (args[0].equals("create")) {
                    if (FileManager.getRealTeam(p) == null) {
                        boolean an = true;
                        for (char c : args[1].toCharArray()) {

                            if (Character.isDigit(c) || Character.isAlphabetic(c)) {
                            } else {
                                an = false;
                            }

                        }
                        if (!an) {
                            p.sendMessage(ChatColor.RED + "Invalid team name");
                        } else {
                            if (FileManager.getRealTeam(args[1]) != null) {
                                p.sendMessage(ChatColor.RED + "Team already exists");
                            } else {
                                Team newTeam = new Team(args[1], p.getUniqueId().toString());
                                FileManager.getTeamsList().add(newTeam);
                                p.sendMessage(ChatColor.YELLOW + "Creating team " + ChatColor.WHITE + args[1]);
                                FileManager.saveTeams();
                                Bukkit.broadcastMessage(ChatColor.YELLOW + "Team " + ChatColor.WHITE + args[1] + ChatColor.YELLOW + " has been created!");
                            }

                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "You are already on a team");
                    }
                } else if(args[0].equals("kick")){
                    if(!team.equals("none")) {
                        Team team1 = FileManager.getRealTeam(team);
                        if(team1.getLeader().equals(p.getUniqueId())) {
                            Player target = Bukkit.getPlayerExact(args[1]);
                            if (target != null) {
                                if(team1.getMembers().contains(target.getUniqueId())){
                                    if(!p.getUniqueId().equals(target.getUniqueId())){
                                        team1.getMembers().remove(target.getUniqueId());
                                        for(UUID id : team1.getMembers()){
                                            Bukkit.getPlayer(id).sendMessage(target.getDisplayName() + ChatColor.YELLOW + " has been kicked from the team!");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "You can't kick yourself. Use " + ChatColor.WHITE + "/team leave");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "That player is not on your team.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "That player does not exist. Ensure you wrote the exact name of the player.");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "You must be the team leader.");
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "You aren't on a team");
                    }
                }

                else {
                    p.sendMessage(ChatColor.RED + "Invalid command arguments");
                }
            } else {
                p.sendMessage(ChatColor.RED + "Too many arguments");
            }
        }

        if (command.getName().equals("claim")) {
            Player p = (Player) sender;

            if (args.length == 0) {
                String team = FileManager.getTeamOf(p);
                UUID id = p.getUniqueId();
                if (!team.equals("none")) {
                    if (!FileManager.getClaims().contains(team)) {
                        if (pos1s.containsKey(id) && pos2s.containsKey(id)) {
                            if (!(pos1s.get(id).distance(pos2s.get(id)) > maxDistance)) {
                                boolean claimOverlapsExisting = false;
                                Point start = pos1s.get(id);
                                Point end = pos2s.get(id);
                                if (start.x > end.x) {
                                    start.x += 1;
                                } else {
                                    end.x += 1;
                                }
                                if (start.y > end.y) {
                                    start.y += 1;
                                } else {
                                    end.y += 1;
                                }

                                Claim newClaim = new Claim(start, end, p.getWorld().getName(), team);
                                BoundingBox b = newClaim.getBounds();
                                    if(b.getWidthX() > minWidth && b.getWidthZ() > minWidth){
                                for (Claim c : FileManager.getClaimsList()) {
                                    if (c.getBounds().overlaps(b)) {
                                        claimOverlapsExisting = true;
                                        break;
                                    }
                                }
                                if (!claimOverlapsExisting) {
                                    FileManager.getClaimsList().add(newClaim);
                                    FileManager.addClaimToFile(newClaim);

                                    p.sendMessage(ChatColor.YELLOW + "Land claimed successfully! You should probably mark it...");
                                } else {
                                    p.sendMessage(ChatColor.YELLOW + "Land is already claimed. Sorry...");
                                }
                            } else {
                                        p.sendMessage(ChatColor.RED + "Your claim must be at least " + minWidth + " blocks wide.");
                                    }
                            } else {
                                p.sendMessage(ChatColor.RED + "Your selection is too large, ensure your 2 positions are less than " + maxDistance + " blocks apart.");
                            }
                        } else {

                            p.sendMessage(ChatColor.RED + "You need to make a selection. Do so with /pos1 and /pos2");
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "You already have a claim.");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "You need to be on a team");
                }

            } else if (args.length == 1 && args[0].equals("delete")) {
                String team = FileManager.getTeamOf(p);
                if (!team.equals("none")) {
                    boolean found = false;
                    for (Claim c : FileManager.getClaimsList()) {
                        if (c.getOwner().equals(team)) {
                            found = true;
                            if (c.getBounds().contains(p.getLocation().toVector())) {
                                FileManager.getClaimsList().remove(c);
                                FileManager.removeClaimFromFile(c);
                                p.sendMessage(ChatColor.YELLOW + "Claim successfully deleted");

                            } else {
                                p.sendMessage(ChatColor.RED + "You need to be inside your claim to delete it.");

                            }
                            break;
                        }
                    }
                    if (!found) {
                        p.sendMessage(ChatColor.RED + "Does your team have a claim?");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "You need to be on a team");
                }
            } else if (args.length == 1 && args[0].equals("info")) {
                String team = FileManager.getTeamOf(p);
                if (!team.equals("none")) {
                    boolean found = false;
                    for (Claim c : FileManager.getClaimsList()) {
                        if (c.getOwner().equals(team)) {
                            found = true;
                            if (found) {
                                p.sendMessage(ChatColor.YELLOW + "Claim info for team " + ChatColor.GRAY + team + ChatColor.YELLOW + ":");
                                p.sendMessage(ChatColor.YELLOW + "World: " + ChatColor.RED + c.world);
                                p.sendMessage(ChatColor.YELLOW + "Starting Point: " + ChatColor.RED + pointString(c.startPoint));
                                p.sendMessage(ChatColor.YELLOW + "Ending Point: " + ChatColor.RED + pointString(c.endPoint));

                                if (c.getBounds().contains(p.getLocation().toVector())) {

                                    double height = p.getWorld().getHighestBlockYAt(c.getBounds().getCenter().toLocation(p.getWorld())) + 25.0;
                                    double minX = c.getBounds().getMinX();
                                    double maxX = c.getBounds().getMaxX();
                                    double minZ = c.getBounds().getMinZ();
                                    double maxZ = c.getBounds().getMaxZ();
                                    for (double i = minX; i < maxX; i += 1d / 3d) {
                                        for (double j = 62; j < height; j += 4.0) {
                                            p.spawnParticle(Particle.DRIP_LAVA, i, j, minZ, 1);
                                            p.spawnParticle(Particle.DRIPPING_OBSIDIAN_TEAR, i, j + 2.0, minZ, 1);
                                        }
                                    }
                                    for (double i = minX; i < maxX; i += 1d / 3d) {
                                        for (double j = 62; j < height; j += 4.0) {
                                            p.spawnParticle(Particle.DRIP_LAVA, i, j, maxZ, 1);
                                            p.spawnParticle(Particle.DRIPPING_OBSIDIAN_TEAR, i, j + 2.0, maxZ, 1);
                                        }
                                    }
                                    for (double i = minZ; i < maxZ; i += 1d / 3d) {
                                        for (double j = 62; j < height; j += 4.0) {
                                            p.spawnParticle(Particle.DRIP_LAVA, minX, j, i, 1);
                                            p.spawnParticle(Particle.DRIPPING_OBSIDIAN_TEAR, minX, j + 2.0, i, 1);
                                        }
                                    }
                                    for (double i = minZ; i < maxZ; i += 1d / 3d) {
                                        for (double j = 62; j < height; j += 4.0) {
                                            p.spawnParticle(Particle.DRIP_LAVA, maxX, j, i, 1);
                                            p.spawnParticle(Particle.DRIPPING_OBSIDIAN_TEAR, maxX, j + 2.0, i, 1);
                                        }
                                    }
                                } else {
                                    p.sendMessage(ChatColor.GOLD + "Tip: " + ChatColor.RED + "Stand within claim to see bounds.");
                                }

                                break;
                            }
                        }
                    }

                    if (!found) {
                        p.sendMessage(ChatColor.RED + "Does your team have a claim?");
                    } else {

                    }
                } else {
                    p.sendMessage(ChatColor.RED + "You must be on a team");
                }
            } else {
                p.sendMessage(ChatColor.RED + "Invalid command");
            }
        }
        return false;
    }

    @EventHandler
    public void chatFormat(AsyncPlayerChatEvent event) {
        if (formatChat) {
            Player p = event.getPlayer();
            String team = FileManager.getTeamOf(p);
            String message = event.getMessage();
            if (!team.equals("none")) {
                chatFormat = chatFormat.replaceAll("\\{name}",p.getName());
                chatFormat = chatFormat.replaceAll("\\{team}", team);
                event.setFormat(chatFormat.replaceAll("\\{message}", message));
            } else {
                noTeamFormat = noTeamFormat.replaceAll("\\{name}", p.getName());
                event.setFormat(noTeamFormat.replaceAll("\\{message}", message));
            }
        }
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent e) {
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Player p = e.getPlayer();
            Vector v = e.getClickedBlock().getLocation().toVector();
            if (e.getClickedBlock().getBlockData().getMaterial().isInteractable()) {
                for (Claim c : FileManager.getClaimsList()) {
                    if (c.getBounds().contains(v) && !c.getOwner().equals(FileManager.getTeamOf(p))) {
                        p.sendMessage(ChatColor.YELLOW + "Land is claimed.");
                        e.setCancelled(true);
                        break;
                    } else {
                        e.setCancelled(false);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Vector v = e.getBlock().getLocation().toVector();
        for (Claim c : FileManager.getClaimsList()) {
            if (c.getBounds().contains(v) && !c.getOwner().equals(FileManager.getTeamOf(p))) {
                p.sendMessage(ChatColor.YELLOW + "Land is claimed.");
                e.setCancelled(true);
                break;
            } else {
                e.setCancelled(false);
            }
        }

    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        Vector v = e.getBlock().getLocation().toVector();
        for (Claim c : FileManager.getClaimsList()) {
            if (c.getBounds().contains(v) && !c.getOwner().equals(FileManager.getTeamOf(p))) {

                p.sendMessage(ChatColor.YELLOW + "Land is claimed.");
                e.setCancelled(true);
                break;
            } else {
                e.setCancelled(false);
            }
        }
    }

}
