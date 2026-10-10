package dev.raftocean.survival;

import dev.raftocean.entity.RaftEntity;
import dev.raftocean.entity.RaftPiece;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;

/** Bounded cosmetic snapshots expose activity without broadcasting private inventories. */
public final class MachineAnimationState {
    public record Visual(int progress, boolean working, int rawWater, int cleanWater, boolean ready, int crop) {
        public static final Visual EMPTY=new Visual(0,false,0,0,false,0);
    }
    private final RaftEntity raft;
    private final Map<BlockPos,Visual> visuals=new HashMap<>();
    public MachineAnimationState(RaftEntity raft) {this.raft=raft;}
    public Visual at(BlockPos pos) {return visuals.getOrDefault(pos,Visual.EMPTY);}
    public void sync() {
        if(raft.level().isClientSide()){return;}
        StringBuilder snapshot=new StringBuilder();
        for(var entry:raft.pieces().entrySet()) {
            if(!entry.getValue().kind().station()){continue;}
            if(!entry.getValue().kind().machine()) {
                BlockPos pos=entry.getKey();if(!snapshot.isEmpty()){snapshot.append(';');}
                snapshot.append(pos.getX()).append(',').append(pos.getY()).append(',').append(pos.getZ())
                        .append(",0,").append(raft.stationInUse(pos)?1:0).append(",0,0,0,0");
                continue;
            }
            var inventory=raft.storage(entry.getKey());var kind=entry.getValue().kind();
            int output=kind==RaftPiece.Kind.COOKING_POT?4:2;
            boolean ready=!inventory.getItem(output).isEmpty();
            int progress=inventory.duration==0?0:Math.clamp((int)((long)inventory.progress*1000/inventory.duration),0,1000);
            if(kind==RaftPiece.Kind.FUEL_TANK) {progress=inventory.getItem(0).is(Automation.BIOFUEL.get())?inventory.getItem(0).getCount()*1000/64:0;}
            int crop=0;
            if(kind==RaftPiece.Kind.PLANTER) {
                var seed=inventory.getItem(0).isEmpty()?inventory.getItem(2):inventory.getItem(0);
                crop=seed.is(Items.WHEAT_SEEDS) || seed.is(Items.WHEAT)?1:seed.is(Items.BEETROOT_SEEDS) || seed.is(Items.BEETROOT)?2:seed.is(Items.CARROT)?3:seed.is(Items.POTATO)?4:
                        seed.is(Items.SUGAR_CANE)?5:seed.is(Items.KELP)?6:0;
            }
            BlockPos pos=entry.getKey();
            if(!snapshot.isEmpty()){snapshot.append(';');}
            snapshot.append(pos.getX()).append(',').append(pos.getY()).append(',').append(pos.getZ()).append(',')
                    .append(progress).append(',').append(inventory.status.equals("working")?1:0).append(',')
                    .append(inventory.rawWater).append(',').append(inventory.cleanWater).append(',').append(ready?1:0).append(',').append(crop);
        }
        raft.publishMachines(snapshot.toString());
    }
    public void accept(String encoded) {
        visuals.clear();if(encoded.length()>24000 || encoded.isEmpty()){return;}
        for(String entry:encoded.split(";")) {
            String[] fields=entry.split(",");if(fields.length!=9){continue;}
            try {
                BlockPos pos=new BlockPos(Integer.parseInt(fields[0]),Integer.parseInt(fields[1]),Integer.parseInt(fields[2]));
                visuals.put(pos,new Visual(Math.clamp(Integer.parseInt(fields[3]),0,1000),fields[4].equals("1"),
                        Math.clamp(Integer.parseInt(fields[5]),0,4),Math.clamp(Integer.parseInt(fields[6]),0,64),fields[7].equals("1"),
                        Math.clamp(Integer.parseInt(fields[8]),0,6)));
            } catch(NumberFormatException ignored) { }
            if(visuals.size()>=512){break;}
        }
    }
}
