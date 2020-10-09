package life.steeze.teamsplugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.awt.*;
import java.util.Objects;


public class Claim {
    public Point startPoint;
    public Point endPoint;
    public String world;
    public String owner;
    public BoundingBox claim;
    public Location home;
    public Claim(Point start, Point end, String w, String team){

        startPoint = start;
        endPoint = end;
        world = w;
        owner = team;
        claim = new BoundingBox(startPoint.x, 0, startPoint.y, endPoint.x, 255, endPoint.y);
        if(FileManager.getClaims().get(team + ".home") != null){
            home = FileManager.getClaims().getLocation(team + ".home");
        }
    }
    public String getOwner(){
        return owner;
    }
    public BoundingBox getBounds(){
        return claim;
    }
    public void setHome(Location l){
        this.home = l;
    }
    public void tpHome(Player p){
        if(home != null){

        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Claim claim = (Claim) o;
        return owner.equals(claim.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner);
    }
}
