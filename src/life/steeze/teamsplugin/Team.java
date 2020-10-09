package life.steeze.teamsplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Team {
    private String name;
    private ArrayList<UUID> members = new ArrayList<>();
    private UUID leader;
    public Team(String n, String uuid){
        name = n;
        leader = UUID.fromString(uuid);
        if(FileManager.getTeams().contains(n)) {
            for (String member : FileManager.getTeams().getStringList(n + ".members")) {
                members.add(UUID.fromString(member));
            }
        }
        if(!members.contains(leader)){
            members.add(leader);
        }
    }

    public String getName(){
        return name;
    }
    public ArrayList<UUID> getMembers(){
        return members;
    }
    public UUID getLeader(){
        return leader;
    }
    public void setLeader(UUID id){
        leader = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equals(name, team.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
