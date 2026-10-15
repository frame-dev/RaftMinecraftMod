package dev.raftocean.api;

/** Power in FE, water in cups and biofuel in whole canisters. */
public record RaftUtilityCost(int power,int freshWater,int biofuel) {
    public static final RaftUtilityCost NONE=new RaftUtilityCost(0,0,0);
    public RaftUtilityCost {
        if(power<0 || freshWater<0 || biofuel<0)throw new IllegalArgumentException("Utility costs cannot be negative");
    }
    public int amount(RaftUtility utility) {
        return switch(utility) { case POWER->power;case FRESH_WATER->freshWater;case BIOFUEL->biofuel; };
    }
}
