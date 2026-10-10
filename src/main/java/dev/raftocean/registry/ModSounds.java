package dev.raftocean.registry;

import dev.raftocean.RaftOcean;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Original short effects, emitted only by successful server actions. */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, RaftOcean.MOD_ID);
    public static final DeferredHolder<SoundEvent, SoundEvent> CUP_FILL = sound("cup_fill");
    public static final DeferredHolder<SoundEvent, SoundEvent> CUP_DRINK = sound("cup_drink");
    public static final DeferredHolder<SoundEvent, SoundEvent> HOOK_CAST = sound("hook_cast");
    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(RaftOcean.MOD_ID, name)));
    }
    private ModSounds() { }
}
