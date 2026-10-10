package dev.raftocean.creature;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** Original passive island animal: native land pathfinding, wheat temptation and breeding. */
public final class IslandGrazer extends Animal {
    public IslandGrazer(EntityType<? extends IslandGrazer> type,Level level) { super(type,level); }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(1,new PanicGoal(this,1.4));
        goalSelector.addGoal(2,new BreedGoal(this,1));
        goalSelector.addGoal(3,new TemptGoal(this,1,i->i.is(Items.WHEAT),false));
        goalSelector.addGoal(4,new FollowParentGoal(this,1.1));
        goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,1));
        goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,6));
        goalSelector.addGoal(7,new RandomLookAroundGoal(this));
    }
    @Override public boolean isFood(ItemStack stack) { return stack.is(Items.WHEAT); }
    @Override public IslandGrazer getBreedOffspring(ServerLevel level,AgeableMob partner) { return new IslandGrazer(CreatureContent.GRAZER.get(),level); }
}
