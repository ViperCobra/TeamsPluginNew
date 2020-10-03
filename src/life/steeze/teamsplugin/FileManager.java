package life.steeze.teamsplugin;

import com.sun.org.apache.xerces.internal.xs.StringList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class FileManager {
    public static File teamsfile;
    public static FileConfiguration teamsdata;
    public static File claimsfile;
    public static FileConfiguration claimsdata;

    public static ArrayList<Claim> claims = new ArrayList<>();
    public static ArrayList<Claim> getClaimsList(){
        return claims;
    }

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
            Point end = new Point(claimsdata.getInt(claim + ".x1"), claimsdata.getInt(claim + ".z1"));
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
        try {
            teamsdata.save(teamsfile);
        } catch(IOException e){
            System.out.println("Couldn't save");
        }
    }

    public static void addValue(String team, UUID value){
        ArrayList<String> list = (ArrayList<String>) FileManager.getTeams().getStringList(team);
        list.add(value.toString());
        FileManager.getTeams().set(team, list);
        save();
    }
    public static void removeValue(String team, UUID value){
        ArrayList<String> list = (ArrayList<String>) FileManager.getTeams().getStringList(team);
        list.remove(value.toString());
        FileManager.getTeams().set(team, list);
        save();
    }

    public static void reload(){
        teamsdata = YamlConfiguration.loadConfiguration(teamsfile);
    }
    public static String getTeamOf(Player p){
        String s = "none";
        for(String key : teamsdata.getKeys(false)){
            if(teamsdata.getStringList(key).contains(p.getUniqueId().toString())){
                s = key;
            }
        }

        System.out.println(p.getDisplayName() + " is on the Team " + s);
        return s;
    }
    public static String getTeamOf(UUID e){
        String s = "none";
        for(String key : teamsdata.getKeys(false)){
            if(teamsdata.getStringList(key).contains(e.toString())){
                s = key;
            }
        }

        System.out.println(e.toString() + " is on the Team " + s);
        return s;
    }


}
