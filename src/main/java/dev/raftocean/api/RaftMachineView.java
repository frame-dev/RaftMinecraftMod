package dev.raftocean.api;

/** Immutable presentation snapshot. Inventories and private strings are never broadcast. */
public record RaftMachineView(boolean active,int progress,int duration,int variant) {
    public static final RaftMachineView IDLE=new RaftMachineView(false,0,0,0);
    public RaftMachineView {
        progress=Math.clamp(progress,0,1_000_000);duration=Math.clamp(duration,0,1_000_000);
        variant=Math.clamp(variant,0,4095);
    }
    public float fraction() { return duration>0?Math.clamp((float)progress/duration,0,1):0; }
}
