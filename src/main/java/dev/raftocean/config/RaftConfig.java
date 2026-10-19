package dev.raftocean.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server gameplay settings synchronize to clients; registrations never depend on settings. */
public final class RaftConfig {
    public enum Mode { WORLD, PEACEFUL, EASY, NORMAL, HARD }
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();
    public static final ModConfigSpec.BooleanValue OUTPOST_TRADING=bool("trading.enabled",true,"Allow island barter counters. Disabling closes trade access without removing generated islands.");
    public static final ModConfigSpec.IntValue OUTPOST_RESTOCK=integer("trading.restockSeconds",1200,60,86400,"World gameplay seconds between outpost stock refreshes, applied on the next opening. Stock is shared and saved per counter.");
    public static final ModConfigSpec.BooleanValue RECYCLING=bool("trading.recyclingEnabled",true,"Process surplus materials into salvage bales; disabling preserves inventory and progress.");
    public static final ModConfigSpec.IntValue COMPACTOR_SECONDS=integer("trading.compactorSeconds",180,30,1800,"Powered seconds per forty-point salvage bale; consumes one native battery charge per running second, pauses without power or output space.");
    public static final ModConfigSpec.BooleanValue AUTOMATION = bool("automation.enabled",true,"Run connected raft water, fuel and electrical networks.");
    public static final ModConfigSpec.IntValue BIOFUEL_ENGINE_TICKS = integer("automation.biofuelEngineTicks",6000,20,200000,"Engine burn ticks per biofuel canister, before engineFuelMultiplier.");
    public static final ModConfigSpec.IntValue GENERATOR_ENERGY = integer("automation.energyPerBiofuel",2000,20,20000,"Energy generated per biofuel canister; stored energy capacity is 2000.");
    public static final ModConfigSpec.IntValue BATTERY_CHARGE_RATE = integer("automation.batteryChargePerSecond",20,1,200,"Total energy charged per second across both charger sockets. One fuel uses energyPerBiofuel.");
    public static final ModConfigSpec.IntValue PURIFIER_ENERGY = integer("automation.purifierEnergy",8,1,2000,"Energy per one-second electric purifier processing step.");
    public static final ModConfigSpec.IntValue ELECTRIC_PURIFY_TICKS = integer("automation.electricPurifyTicks",100,20,12000,"Electric purifier ticks per cup of fresh water.");
    public static final ModConfigSpec.IntValue SPRINKLER_RADIUS = integer("automation.sprinklerRadius",2,1,6,"Horizontal irrigation radius in raft cells; irrigates planters on the same deck.");
    public static final ModConfigSpec.IntValue SPRINKLER_ENERGY = integer("automation.sprinklerEnergy",4,1,2000,"Energy per delivered cup of irrigation water.");
    public static final ModConfigSpec.EnumValue<Mode> MODE = B.comment("WORLD follows Minecraft difficulty; other values override mod creature behavior. Minecraft Peaceful always prevents hostile behavior.").translation("raft_ocean.config.gameMode").defineEnum("gameMode",Mode.WORLD);
    public static final ModConfigSpec.IntValue FOUNDATION_LIMIT = integer("building.foundationLimit",1024,9,1024,"Maximum foundations placed per raft; lowering this preserves existing builds. Native save/network ceiling is 1024.");
    public static final ModConfigSpec.BooleanValue REINFORCEMENT = bool("building.foundationReinforcement",true,"Allow metal braces to armor foundations against shark damage. Turning this off preserves existing armor but disables protection.");
    public static final ModConfigSpec.BooleanValue QUICK_HARVEST = bool("building.quickMachineHarvest",true,"Sneak-use machines with an empty hand to collect only completed outputs. Excess remains in the machine.");
    public static final ModConfigSpec.BooleanValue QUICK_STASH = bool("building.quickStash",true,"Sneak-click chests/barrels with an empty hand to store matching backpack items. Hotbar and equipment are preserved.");
    public static final ModConfigSpec.BooleanValue PEACEFUL_THIRST = bool("survival.thirstInPeaceful",true,"Allow thirst drain in Peaceful. Dehydration damage is always disabled there; false restores hydration.");
    public static final ModConfigSpec.BooleanValue THIRST = bool("survival.thirstEnabled", true, "Enable hydration drain and dehydration damage.");
    public static final ModConfigSpec.IntValue THIRST_PERIOD = integer("survival.thirstPeriodTicks",600,100,12000,"Ticks per thirst point; sprinting drains twice as fast.");
    public static final ModConfigSpec.BooleanValue JUICING=bool("survival.juicingEnabled",true,"Run four-food battery juicers. Disabling preserves queued ingredients, progress and ready drinks.");
    public static final ModConfigSpec.DoubleValue JUICER_SPEED=decimal("survival.juicerSpeed",1,.1,4,"Juicer speed multiplied by processingSpeed. One portable battery charge is spent per running second.");
    public static final ModConfigSpec.IntValue JUICE_HYDRATION=integer("survival.juiceHydration",12,1,20,"Hydration restored per prepared juice. Glasses are reusable; optional drink bonuses follow preparedMealBonuses and mealBonusTicks.");
    public static final ModConfigSpec.BooleanValue MANUAL_COOKING = bool("survival.manualCooking",true,"Cooking pots prepare one batch per Cook button press. False restores automatic pot processing. Existing ingredients and bowl recipes are retained.");
    public static final ModConfigSpec.DoubleValue PROCESSING = decimal("survival.processingSpeed",1,.1,4,"Machine processing speed multiplier; fuel still burns in real ticks.");
    public static final ModConfigSpec.DoubleValue CURRENT = decimal("navigation.currentMultiplier",1,0,3,"Multiply saved raft current; zero disables natural drift.");
    public static final ModConfigSpec.DoubleValue WIND = decimal("navigation.windMultiplier",1,0,2,"Wind strength multiplier.");
    public static final ModConfigSpec.BooleanValue ENGINE_LOAD_LIMITS = bool("navigation.engineLoadLimits",true,"Engine capacity bands: full through capacity, half through 110%, then stall; six fueled enabled engines bypass load. False restores legacy additive engine scaling.");
    public static final ModConfigSpec.IntValue ENGINE_FOUNDATIONS = integer("navigation.engineFoundationCapacity",100,1,1024,"Foundation load per engine at full thrust; additional engines recover power on larger rafts.");
    public static final ModConfigSpec.DoubleValue MAX_SPEED = decimal("navigation.maxSpeed",.16,.02,.20,"Maximum requested horizontal blocks per tick.");
    public static final ModConfigSpec.DoubleValue TURN_SPEED = decimal("navigation.turnSpeed",.75,.1,1,"Maximum hull degrees per tick.");
    public static final ModConfigSpec.BooleanValue DEBRIS = bool("salvage.enabled",true,"Spawn natural floating salvage.");
    public static final ModConfigSpec.IntValue DEBRIS_INTERVAL = integer("salvage.spawnIntervalTicks",100,20,12000,"Time between salvage spawn batches.");
    public static final ModConfigSpec.IntValue DEBRIS_ATTEMPTS = integer("salvage.spawnAttempts",4,1,16,"Attempts per occupied raft and batch.");
    public static final ModConfigSpec.IntValue DEBRIS_CAP = integer("salvage.perRaftCap",24,1,128,"Maximum incoming salvage per raft.");
    public static final ModConfigSpec.IntValue DEBRIS_LEVEL_CAP = integer("salvage.levelCap",128,1,512,"Maximum naturally spawned loaded salvage per dimension.");
    public static final ModConfigSpec.IntValue DEBRIS_LIFETIME = integer("salvage.lifetimeTicks",6000,600,24000,"Salvage lifetime, including elapsed time unloaded.");
    public static final ModConfigSpec.BooleanValue CREATURES = bool("creatures.naturalSpawning",true,"Spawn creatures around occupied rafts on loaded terrain/water.");
    public static final ModConfigSpec.BooleanValue SHARKS = bool("creatures.sharks",true,"Allow natural sharks and shark attacks.");
    public static final ModConfigSpec.BooleanValue BIRDS = bool("creatures.birds",true,"Allow natural crop pests and crop theft.");
    public static final ModConfigSpec.BooleanValue FAUNA = bool("creatures.friendlyFauna",true,"Spawn friendly grazers on suitable nearby land.");
    public static final ModConfigSpec.IntValue CREATURE_INTERVAL = integer("creatures.spawnIntervalTicks",600,100,12000,"Creature spawn/replacement interval.");
    public static final ModConfigSpec.IntValue CREATURE_CAP = integer("creatures.levelCap",32,1,128,"Loaded mod creature cap per dimension; manual summons bypass it.");
    public static final ModConfigSpec.DoubleValue AGGRESSION = decimal("creatures.sharkAggression",1,0,4,"Bite frequency multiplier; zero disables player and raft attacks.");
    public static final ModConfigSpec.DoubleValue DIFFICULTY = decimal("creatures.damageMultiplier",1,0,4,"Hostile damage multiplier, in addition to vanilla difficulty.");
    public static final ModConfigSpec.BooleanValue RAFT_DAMAGE = bool("creatures.raftDamage",true,"Allow sharks to damage exposed foundations.");
    public static final ModConfigSpec.IntValue BITE_DAMAGE = integer("creatures.foundationBiteDamage",10,1,100,"Foundation damage per bite; each foundation has 100 health.");
    public static final ModConfigSpec.IntValue BITE_INTERVAL = integer("creatures.sharkBiteIntervalTicks",600,100,12000,"Base delay between raft attacks, divided by aggression.");
    public static final ModConfigSpec.IntValue RAFT_ATTACK_DELAY = integer("creatures.sharkRaftAttackDelaySeconds",600,0,3600,"Minimum loaded gameplay seconds before each shark's first raft attack and between raft attacks, including repelled or interrupted attacks. Not shortened by difficulty or aggression. Zero disables this additional delay; sharkBiteIntervalTicks still applies. Swimming-player attacks are unaffected.");
    public static final ModConfigSpec.IntValue BITE_BURST = integer("creatures.foundationBitesPerAttack",10,1,20,"Maximum bites in one foundation attack; stops when the target breaks.");
    public static final ModConfigSpec.IntValue REPEL_HITS = integer("creatures.sharkRepelHits",3,1,10,"Successful player hits during a foundation attack required to repel the shark; crew hits count together.");
    public static final ModConfigSpec.BooleanValue SHARED_RESEARCH = bool("multiplayer.sharedResearch",true,"Crew share studied samples and learned recipes; guests do not. Learned recipes remain known after leaving.");
    public static final ModConfigSpec.BooleanValue CREW_PERMISSIONS = bool("multiplayer.crewPermissions",true,"Owned rafts restrict building, containers and controls to captain/crew. Public join boards as guest; captain uses /raftcrew role.");
    public static final ModConfigSpec.BooleanValue CREW_RESCUE = bool("multiplayer.playerRescue",true,"Lethal damage with a living online teammate starts a rescue window. Void and kill commands always kill normally.");
    public static final ModConfigSpec.IntValue RESCUE_SECONDS = integer("multiplayer.rescueWindowSeconds",60,10,600,"Loaded seconds before an incapacitated player dies. Sneak empty-hand right-click a teammate and stay within three blocks for three seconds to revive.");
    public static final ModConfigSpec.DoubleValue RESCUE_HEALTH = decimal("multiplayer.reviveHealth",6,1,20,"Health restored after a successful rescue.");
    public static final ModConfigSpec.BooleanValue SHARED_STARTER = bool("multiplayer.sharedStarterRaft",false,"Ocean World: new players share one saved starter raft and default respawn. Existing homes are retained; use /raftcrew join to join another nearby raft.");
    public static final ModConfigSpec.IntValue FOUNDATION_WINDUP = integer("creatures.foundationBiteWindupTicks",40,10,200,"Windup duration for each foundation bite; animation scales to this duration.");
    public static final ModConfigSpec.IntValue FOUNDATION_PAUSE = integer("creatures.foundationBitePauseTicks",12,12,200,"Recovery pause between foundation bites; minimum preserves the jaw-close animation.");
    public static final ModConfigSpec.IntValue RETREAT_DURATION = integer("creatures.sharkRetreatTicks",55,20,400,"Swimming retreat duration after attacks or cooperative repelling.");
    public static final ModConfigSpec.IntValue REPEL_COOLDOWN = integer("creatures.sharkRepelCooldownTicks",120,20,12000,"Minimum delay before another attack after successful cooperative repelling.");
    public static final ModConfigSpec.IntValue PEACEFUL_SHARKS = integer("creatures.peacefulSharksPerRaft",1,0,8,"Natural passive sharks per occupied raft in Peaceful; zero prevents new sharks.");
    public static final ModConfigSpec.IntValue EASY_SHARKS = integer("creatures.easySharksPerRaft",1,0,8,"Natural sharks per occupied raft in Easy.");
    public static final ModConfigSpec.IntValue NORMAL_SHARKS = integer("creatures.normalSharksPerRaft",1,0,8,"Natural sharks per occupied raft in Normal.");
    public static final ModConfigSpec.IntValue HARD_SHARKS = integer("creatures.hardSharksPerRaft",2,0,8,"Natural sharks per occupied raft in Hard; also bounded by the dimension cap.");
    public static final ModConfigSpec.BooleanValue STARTER_SUPPLIES = bool("multiplayer.giveStarterSupplies",true,"Grant starter items once on first Ocean World entry, including co-op; not on join commands, reconnect or death.");
    public static final ModConfigSpec.BooleanValue STARTER_HAMMER = bool("multiplayer.starterHammer",true,"Include a building hammer when first-entry starter supplies are enabled.");
    public static final ModConfigSpec.IntValue STARTER_FOUNDATIONS = integer("multiplayer.starterFoundations",8,0,64,"Foundations per new player when starter supplies are enabled.");
    public static final ModConfigSpec.IntValue STARTER_PLANKS = integer("multiplayer.starterPlanks",16,0,128,"Oak planks per new player when starter supplies are enabled.");
    public static final ModConfigSpec.IntValue STARTER_FOOD = integer("multiplayer.starterBread",8,0,64,"Bread per new player when starter supplies are enabled.");
    public static final ModConfigSpec.IntValue BIRD_INTERVAL = integer("creatures.birdTheftIntervalTicks",1200,200,12000,"Delay between crop theft attempts.");
    public static final ModConfigSpec.IntValue BIRD_THEFT_DELAY = integer("creatures.birdCropTheftDelaySeconds",300,0,3600,"Minimum loaded gameplay seconds before each bird's first crop theft and between thefts, including interrupted approaches. Zero disables this additional delay; birdTheftIntervalTicks still applies.");
    public static final ModConfigSpec.IntValue BAIT_DURATION = integer("creatures.baitDurationTicks",1200,200,12000,"Duration of a placed chum float.");
    public static final ModConfigSpec.IntValue BAIT_CAP = integer("creatures.baitCap",16,1,64,"Maximum loaded chum floats placed with the bait item per dimension.");
    public static final ModConfigSpec.BooleanValue RESEARCH = bool("progression.researchRequired",true,"Require learned blueprints for tagged mod crafting outputs; existing equipment remains usable.");
    public static final ModConfigSpec.IntValue PIECE_LIMIT = integer("building.pieceLimit",512,1,512,"Limit new structural pieces per raft; existing saves remain intact. Technical ceiling is 512.");
    public static final ModConfigSpec.IntValue DRINK_RESTORE = integer("survival.drinkHydration",6,1,20,"Hydration restored by one fresh-water cup.");
    public static final ModConfigSpec.DoubleValue SPRINT_THIRST = decimal("survival.sprintThirstMultiplier",2,1,4,"Hydration drain multiplier while sprinting.");
    public static final ModConfigSpec.DoubleValue PURIFIER_SPEED = decimal("survival.purifierSpeed",1,.1,4,"Purifier speed multiplier, combined with processingSpeed.");
    public static final ModConfigSpec.DoubleValue RAIN_SPEED = decimal("survival.rainCollectorSpeed",1,.1,4,"Rain collector speed multiplier; still requires exposed rain.");
    public static final ModConfigSpec.DoubleValue GRILL_SPEED = decimal("survival.grillSpeed",1,.1,4,"Grill speed multiplier, combined with processingSpeed.");
    public static final ModConfigSpec.DoubleValue SMELTER_SPEED = decimal("survival.smelterSpeed",1,.1,4,"Smelter speed multiplier, combined with processingSpeed.");
    public static final ModConfigSpec.DoubleValue POT_SPEED = decimal("survival.cookingPotSpeed",1,.1,4,"Cooking pot speed multiplier, combined with processingSpeed.");
    public static final ModConfigSpec.DoubleValue CROP_SPEED = decimal("survival.cropGrowthSpeed",1,.1,4,"Small planter growth multiplier, combined with processingSpeed.");
    public static final ModConfigSpec.DoubleValue TREE_SPEED = decimal("survival.treeGrowthSpeed",1,.1,4,"Tree planter growth multiplier, combined with processingSpeed.");
    public static final ModConfigSpec.DoubleValue MACHINE_FUEL = decimal("survival.machineFuelMultiplier",1,.1,4,"Fuel duration multiplier for newly consumed survival-machine fuel; existing burning fuel is preserved.");
    public static final ModConfigSpec.IntValue PLANT_LIGHT = integer("survival.planterMinimumLight",9,0,15,"Minimum light for both planters; zero removes brightness requirement.");
    public static final ModConfigSpec.BooleanValue PLANT_SKY = bool("survival.plantersRequireOpenSky",true,"Require both planters to be uncovered by raft pieces; brightness is checked separately.");
    public static final ModConfigSpec.DoubleValue ENGINE_FUEL = decimal("navigation.engineFuelMultiplier",1,.1,4,"Engine fuel duration multiplier for newly consumed fuel.");
    public static final ModConfigSpec.DoubleValue ENGINE_THRUST = decimal("navigation.engineThrustMultiplier",1,0,3,"Engine thrust multiplier (base 1.5 m/s); zero stops engines without consuming fuel. Speed cap still applies.");
    public static final ModConfigSpec.DoubleValue SAIL_THRUST = decimal("navigation.sailThrustMultiplier",1,0,3,"Sail thrust multiplier (base 1.0 m/s at unit aligned wind); speed cap and wind alignment still apply.");
    public static final ModConfigSpec.DoubleValue PADDLE_THRUST = decimal("navigation.paddleStrengthMultiplier",1,0,3,"Paddle impulse multiplier (base peak 1.8 m/s); zero disables strokes. Speed cap still applies.");
    public static final ModConfigSpec.BooleanValue MEAL_BONUSES = bool("survival.preparedMealBonuses",true,"Prepared bowl meals grant temporary level-one bonuses; false leaves their ordinary nutrition unchanged.");
    public static final ModConfigSpec.IntValue MEAL_DURATION = integer("survival.mealBonusTicks",2400,200,12000,"Duration of prepared meal bonuses; repeat meals refresh rather than stack effect strength.");
    public static final ModConfigSpec.BooleanValue ENERGY_ITEMS = bool("compatibility.energyItems",true,"Expose raft batteries through NeoForge energy capabilities and charge compatible single modded energy items in the raft charger. One charge unit equals one FE.");
    public static final ModConfigSpec SPEC = B.build();
    private static final ModConfigSpec.Builder C = new ModConfigSpec.Builder();
    public static final ModConfigSpec.BooleanValue NAV_HUD = C.translation("raft_ocean.config.navigationHud").define("navigationHud",true);
    public static final ModConfigSpec.BooleanValue INTERACTION_HINTS = C.translation("raft_ocean.config.interactionHints").define("interactionHints",true);
    public static final ModConfigSpec.BooleanValue THIRST_HUD = C.translation("raft_ocean.config.thirstHud").define("thirstHud",true);
    public static final ModConfigSpec.IntValue EFFECT_DISTANCE = C.comment("Maximum distance in blocks for cosmetic machine bubbles, smoke, sparks and rain drops; zero disables them. Water, crops and food remain visible.").translation("raft_ocean.config.machineEffectDistance").defineInRange("machineEffectDistance",32,0,128);
    public static final ModConfigSpec CLIENT_SPEC = C.build();
    private static ModConfigSpec.BooleanValue bool(String name,boolean value,String help) { return B.comment(help).translation("raft_ocean.config."+name).define(name,value); }
    private static ModConfigSpec.IntValue integer(String name,int value,int min,int max,String help) { return B.comment(help).translation("raft_ocean.config."+name).defineInRange(name,value,min,max); }
    private static ModConfigSpec.DoubleValue decimal(String name,double value,double min,double max,String help) { return B.comment(help).translation("raft_ocean.config."+name).defineInRange(name,value,min,max); }
    public static boolean enabled(ModConfigSpec.BooleanValue value) { return value.get(); }
    public static double difficulty(net.minecraft.world.Difficulty difficulty) {
        difficulty = effectiveDifficulty(difficulty);
        return difficulty == net.minecraft.world.Difficulty.PEACEFUL ? 0 : DIFFICULTY.get() * (difficulty == net.minecraft.world.Difficulty.EASY ? .65 : difficulty == net.minecraft.world.Difficulty.HARD ? 1.4 : 1);
    }
    public static net.minecraft.world.Difficulty effectiveDifficulty(net.minecraft.world.Difficulty world) {
        if(world==net.minecraft.world.Difficulty.PEACEFUL){return world;}
        return switch (MODE.get()) {
            case WORLD -> world;
            case PEACEFUL -> net.minecraft.world.Difficulty.PEACEFUL;
            case EASY -> net.minecraft.world.Difficulty.EASY;
            case NORMAL -> net.minecraft.world.Difficulty.NORMAL;
            case HARD -> net.minecraft.world.Difficulty.HARD;
        };
    }
    public static int sharkCount(net.minecraft.world.Difficulty world) {
        return switch(effectiveDifficulty(world)) {
            case PEACEFUL -> PEACEFUL_SHARKS.get();
            case EASY -> EASY_SHARKS.get();
            case NORMAL -> NORMAL_SHARKS.get();
            case HARD -> HARD_SHARKS.get();
        };
    }
    public static double attackFrequency(net.minecraft.world.Difficulty world) {
        return switch (effectiveDifficulty(world)) {
            case PEACEFUL -> 0;
            case EASY -> .5;
            case NORMAL -> 1;
            case HARD -> 1.5;
        };
    }
    private RaftConfig() { }
}
