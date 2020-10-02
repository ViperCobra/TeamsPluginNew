package life.steeze.teamsplugin;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class FileManager {
    public static File teamsfile;
    public static FileConfiguration teamsdata;

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

    public static FileConfiguration get(){
        return teamsdata;
    }

    public static void save(){
        try {
            teamsdata.save(teamsfile);
        } catch(IOException e){
            System.out.println("Couldn't save");
        }
    }

    public static void addValue(String team, UUID value){
        ArrayList<String> list = (ArrayList<String>) FileManager.get().getStringList(team);
        list.add(value.toString());
        FileManager.get().set(team, list);
        save();
    }
    public static void removeValue(String team, UUID value){
        ArrayList<String> list = (ArrayList<String>) FileManager.get().getStringList(team);
        list.remove(value.toString());
        FileManager.get().set(team, list);
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
