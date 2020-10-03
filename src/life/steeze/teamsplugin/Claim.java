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
    public String world;
    public String owner;
    public Claim(Point start, Point end, String w, String team){
        startPoint = start;
        endPoint = end;
        world = w;
        owner = team;
        BoundingBox claim = new BoundingBox(startPoint.x, 0, startPoint.y, endPoint.x, 255, endPoint.y);
    }


}
