package life.steeze.teamsplugin;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;

import java.awt.*;

/*
Haven't done anything with this class yet going to use it to make a land claiming system.
 */
public class Claim {
    public Point startPoint;
    public Point endPoint;
    public World world;
    public String owner;
    BoundingBox claim = new BoundingBox(startPoint.x, 0, startPoint.y, endPoint.x, 255, endPoint.y);
    public Claim(Point start, Point end, World w, String team){
        startPoint = start;
        endPoint = end;
        world = w;
        owner = team;
    }
    public boolean hasBlock(Block b){
        if (claim.contains(b.getX(),b.getY(),b.getZ())) {
            return true;
        } else {
            return false;
        }
    }


}
