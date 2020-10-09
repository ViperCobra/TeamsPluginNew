package life.steeze.teamsplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;


public class FileManager {
    public static File teamsfile;
    public static FileConfiguration teamsdata;
    public static File claimsfile;
    public static FileConfiguration claimsdata;

    public static HashSet<Claim> claims = new HashSet<>();
    public static HashSet<Claim> getClaimsList(){
        return claims;
    }

    public static HashSet<Team> teams = new HashSet<>();
    public static HashSet<Team> getTeamsList(){return teams;}

    public static void getTeamsData(){

        teamsfile = new File(Bukkit.getServer().getPluginManager().getPlugin("PvPTeams").getDataFolder(), "teams.yml");

        if(!teamsfile.exists()){
            try {
                teamsfile.createNewFile();
            } catch (IOException e){
                System.out.println("Bad Error--- WARNING WON'T WORK");
            }

        }
        teamsdata = YamlConfiguration.loadConfiguration(teamsfile);

        for(String team : teamsdata.getKeys(false)){
            Team realTeam = new Team(team, teamsdata.getString(team + ".leader"));
            teams.add(realTeam);
        }

    }
    public static void loadClaims(){
        claimsfile = new File(Bukkit.getServer().getPluginManager().getPlugin("PvPTeams").getDataFolder(), "claims.yml");

        if(!claimsfile.exists()){
            try {
                claimsfile.createNewFile();
            } catch (IOException e){
                System.out.println("Bad Error--- WARNING WON'T WORK");
            }

        }
        claimsdata = YamlConfiguration.loadConfiguration(claimsfile);

        for(String claim : claimsdata.getKeys(false)){
            Point start = new Point(claimsdata.getInt(claim + ".x1"), claimsdata.getInt(claim + ".z1"));
            Point end = new Point(claimsdata.getInt(claim + ".x2"), claimsdata.getInt(claim + ".z2"));
            Claim loadedClaim = new Claim(start, end, claimsdata.getString(claim + ".world"), claimsdata.getString(claim + ".team"));
            claims.add(loadedClaim);
        }
    }

    public static FileConfiguration getTeams(){
        return teamsdata;
    }
    public static FileConfiguration getClaims(){
        return claimsdata;
    }

    public static void save(){
        saveClaims();
        saveTeams();
    }
    public static void saveTeams(){

        for(Team t : teams){
            String team = t.getName();

            ArrayList<String> temp = new ArrayList<>();

            for(UUID id : t.getMembers()){
                temp.add(id.toString());
            }
            teamsdata.set(team + ".members", temp);
            teamsdata.set(team + ".leader", t.getLeader().toString());

        }


        try {
            teamsdata.save(teamsfile);
        } catch(IOException e){
            System.out.println("Couldn't save");
        }
    }

    public static void addClaimToFile(Claim claim){
        String i = claim.owner;
        claimsdata.set(i + ".x1", claim.startPoint.x);
        claimsdata.set(i + ".z1", claim.startPoint.y);
        claimsdata.set(i + ".x2", claim.endPoint.x);
        claimsdata.set(i + ".z2", claim.endPoint.y);
        claimsdata.set(i + ".world", claim.world);
        claimsdata.set(i + ".team", claim.owner);
        save();
    }
    public static void addClaimHome(Claim claim, Location l){
        claimsdata.set(claim.owner + ".home", l);
    }
    public static void removeClaimFromFile(Claim claim){
        claimsdata.set(claim.owner, null);
        save();
    }

    public static void saveClaims(){
        try{

            claimsdata.save(claimsfile);
        } catch(IOException e){
            System.out.println("Couldn't save");
        }
    }

    public static void addValue(String team, UUID value){
        ArrayList<String> list = (ArrayList<String>) FileManager.getTeams().getStringList(team + ".members");
        list.add(value.toString());
        FileManager.getTeams().set(team + ".members", list);
        save();
    }
    public static void removeValue(String team, UUID value){
        ArrayList<String> list = (ArrayList<String>) FileManager.getTeams().getStringList(team + ".members");
        list.remove(value.toString());
        FileManager.getTeams().set(team + ".members", list);
        save();
    }

    public static void reload(){
        teamsdata = YamlConfiguration.loadConfiguration(teamsfile);
    }
    public static String getTeamOf(Player p){
        UUID id = p.getUniqueId();
        String s = "none";
        for(Team t : teams){
            if (t.getMembers().contains(id)){
                s = t.getName();
                break;
            }
        }
        return s;
    }
    public static String getTeamOf(UUID e){
        String s = "none";
        for(Team t : teams){
            if (t.getMembers().contains(e)){
                s = t.getName();
                break;
            }
        }
        return s;
    }
    public static Team getRealTeam(Player p){
        Team found = null;
        for(Team t : teams){
            if(t.getMembers().contains(p.getUniqueId())){
                found = t;
                break;
            }
        }
        return found;
    }
    public static Team getRealTeam(UUID p){
        Team found = null;
        for(Team t : teams){
            if(t.getMembers().contains(p)){
                found = t;
                break;
            }
        }
        return found;

    }
    public static Team getRealTeam(String teamName){
        Team found = null;
        for(Team t : teams){
            if(t.getName().equals(teamName)){
                found = t;
                break;
            }
        }
        return found;
    }


}
