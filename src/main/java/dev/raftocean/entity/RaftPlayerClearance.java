package dev.raftocean.entity;

import java.util.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Small supported corrections for raft overlap; never seats players or changes world blocks. */
public final class RaftPlayerClearance {
    private static final double LIMIT=1.5,EPS=.001;
    private RaftPlayerClearance() { }
    public static Optional<Vec3> position(RaftEntity raft,Player player,Vec3 target) {
        var cells=raft.getCollisionBoxes();
        AABB original=player.getBoundingBox().move(target.subtract(player.position()));
        if(cells.stream().noneMatch(box->box.intersects(original.deflate(1e-6)))){return Optional.of(target);}
        // Correct only tiny vertical penetration of a supporting surface; never lift into a station or roof.
        double lift=cells.stream().filter(box->RaftContact.isSupported(original,box) && box.maxY>=original.minY)
                .mapToDouble(box->box.maxY+RaftContact.CLEARANCE-original.minY).max().orElse(0);
        double rise=lift>0 && lift<=.101?lift:0;
        AABB body=original.move(0,rise,0);
        Vec3 base=target.add(0,rise,0);
        var overlaps=cells.stream().filter(box->box.intersects(body.deflate(1e-6))).toList();
        if(overlaps.isEmpty()){return safe(raft,player,cells,body)?Optional.of(base):Optional.empty();}
        var nearby=cells.stream().filter(box->box.intersects(body.inflate(LIMIT,0,LIMIT))).toList();
        Set<AABB> initial=new HashSet<>(overlaps);
        Set<Double> xs=new HashSet<>(),zs=new HashSet<>();xs.add(0.0);zs.add(0.0);
        double minX=Double.POSITIVE_INFINITY,minZ=minX,maxX=-minX,maxZ=-minX;
        for(AABB box:overlaps){
            minX=Math.min(minX,box.minX);maxX=Math.max(maxX,box.maxX);
            minZ=Math.min(minZ,box.minZ);maxZ=Math.max(maxZ,box.maxZ);
        }
        for(AABB box:nearby){
            add(xs,box.minX-body.maxX-EPS);add(xs,box.maxX-body.minX+EPS);
            add(zs,box.minZ-body.maxZ-EPS);add(zs,box.maxZ-body.minZ+EPS);
        }
        var xValues=closest(xs);var zValues=closest(zs);
        add(xValues,minX-body.maxX-EPS);add(xValues,maxX-body.minX+EPS);
        add(zValues,minZ-body.maxZ-EPS);add(zValues,maxZ-body.minZ+EPS);
        // Prefer a small supported step to a larger sideways shove. Never step
        // farther than a normal player can, and check full headroom first.
        Optional<Vec3> raised=Optional.empty();
        double stepRise=Double.POSITIVE_INFINITY;
        for(AABB box:overlaps){
            double height=box.maxY+RaftContact.CLEARANCE-body.minY;
            if(height>0 && height<=.6 && height<stepRise && safe(raft,player,cells,body.move(0,height,0))
                    && RaftPlayerSupport.externalMovementFree(raft,player,body,new Vec3(0,height,0))){
                stepRise=height;raised=Optional.of(base.add(0,height,0));
            }
        }
        var offsets=new ArrayList<Vec3>();
        for(double x:xValues){for(double z:zValues){if(x*x+z*z<=LIMIT*LIMIT && x*x+z*z>1e-12){offsets.add(new Vec3(x,0,z));}}}
        offsets.sort(Comparator.comparingDouble(Vec3::lengthSqr).thenComparingDouble(v->v.x).thenComparingDouble(v->v.z));
        for(Vec3 offset:offsets){
            if(!safe(raft,player,cells,body.move(offset))){continue;}
            if(nearby.stream().anyMatch(box->!initial.contains(box) && sweeps(body,box,offset))
                    || !RaftPlayerSupport.externalMovementFree(raft,player,body,offset)){continue;}
            boolean clear=true;
            int steps=Math.max(1,(int)Math.ceil(offset.length()/.05));
            for(int sample=1;sample<=steps;sample++){
                double t=(double)sample/steps;
                AABB step=body.move(offset.scale(t)).deflate(1e-6);
                if(nearby.stream().anyMatch(box->!initial.contains(box) && box.intersects(step))
                        || !supported(raft,cells,step)
                        || !RaftPlayerSupport.externalCollisionFree(raft,player,step)){clear=false;break;}
            }
            if(clear){return stepRise<=offset.length()?raised:Optional.of(base.add(offset));}
        }
        return raised;
    }
    /** Continuous slab intersection: a thin obstacle cannot hide between samples. */
    public static boolean sweeps(AABB body,AABB obstacle,Vec3 move){
        double enter=0,exit=1;
        double[] low={body.minX,body.minY,body.minZ},high={body.maxX,body.maxY,body.maxZ};
        double[] a={obstacle.minX,obstacle.minY,obstacle.minZ},b={obstacle.maxX,obstacle.maxY,obstacle.maxZ};
        double[] velocity={move.x,move.y,move.z};
        for(int axis=0;axis<3;axis++){
            if(Math.abs(velocity[axis])<1e-12){if(high[axis]<=a[axis]+1e-6 || low[axis]>=b[axis]-1e-6){return false;}continue;}
            double t0=(a[axis]+1e-6-high[axis])/velocity[axis],t1=(b[axis]-1e-6-low[axis])/velocity[axis];
            enter=Math.max(enter,Math.min(t0,t1));exit=Math.min(exit,Math.max(t0,t1));
            if(enter>=exit){return false;}
        }
        return enter<exit && exit>0 && enter<1;
    }
    private static void add(Collection<Double> values,double offset){if(Math.abs(offset)<=LIMIT){values.add(offset);}}
    private static List<Double> closest(Set<Double> values){return new ArrayList<>(values.stream().sorted(Comparator.<Double>comparingDouble(Math::abs).thenComparingDouble(Double::doubleValue)).limit(24).toList());}
    private static boolean safe(RaftEntity raft,Player player,List<AABB> cells,AABB body){
        return cells.stream().noneMatch(box->box.intersects(body.deflate(1e-6)))
                && supported(raft,cells,body)
                && player.level().getWorldBorder().isWithinBounds(body)
                && player.level().hasChunkAt(net.minecraft.core.BlockPos.containing(body.minX,body.minY,body.minZ))
                && player.level().hasChunkAt(net.minecraft.core.BlockPos.containing(body.maxX,body.minY,body.maxZ))
                && RaftPlayerSupport.externalCollisionFree(raft,player,body);
    }
    private static boolean supported(RaftEntity raft,List<AABB> cells,AABB body){
        return cells.stream().anyMatch(box->RaftContact.isSupported(body,box)
                && body.getCenter().x>=box.minX-EPS && body.getCenter().x<=box.maxX+EPS
                && body.getCenter().z>=box.minZ-EPS && body.getCenter().z<=box.maxZ+EPS) || raft.isOnLadder(body);
    }
}
