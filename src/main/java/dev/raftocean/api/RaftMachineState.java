package dev.raftocean.api;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Server-owned inventory and bounded named properties, persisted with its owning raft. */
public final class RaftMachineState extends SimpleContainer {
    private final Map<String,Integer> integers=new LinkedHashMap<>();
    private final Map<String,String> strings=new LinkedHashMap<>();
    public RaftMachineState(int slots) {
        super(validSlots(slots));
    }
    private static int validSlots(int slots) {
        if(slots<9 || slots>54 || slots%9!=0)throw new IllegalArgumentException("Machine slots must be 9,18,27,36,45 or54");
        return slots;
    }
    private static void key(String key) {
        if(key==null || !key.matches("[A-Za-z0-9_./:-]{1,64}"))throw new IllegalArgumentException("Invalid machine property key");
    }
    public int getInt(String key) { return integers.getOrDefault(key,0); }
    public String getString(String key) { return strings.getOrDefault(key,""); }
    public boolean hasInt(String key) { return integers.containsKey(key); }
    public boolean hasString(String key) { return strings.containsKey(key); }
    public Map<String,Integer> integerProperties() { return Map.copyOf(integers); }
    public Map<String,String> stringProperties() { return Map.copyOf(strings); }
    public void removeInt(String key) { key(key);if(integers.remove(key)!=null)setChanged(); }
    public void removeString(String key) { key(key);if(strings.remove(key)!=null)setChanged(); }
    public void setInt(String key,int value) {
        key(key);if(!integers.containsKey(key) && integers.size()>=32)throw new IllegalStateException("Too many integer properties");
        integers.put(key,value);setChanged();
    }
    public void setString(String key,String value) {
        key(key);if(value==null || value.length()>256)throw new IllegalArgumentException("Machine strings are limited to256 characters");
        if(!strings.containsKey(key) && strings.size()>=32)throw new IllegalStateException("Too many string properties");
        strings.put(key,value);setChanged();
    }
    /** Bind from a custom menu subclass using addDataSlots; native short packet limits apply. */
    public ContainerData data(String... keys) {
        var names=keys.clone();for(var name:names)key(name);
        return new ContainerData() {
            public int get(int index) { return getInt(names[index]); }
            public void set(int index,int value) { setInt(names[index],value); }
            public int getCount() { return names.length; }
        };
    }
    public void save(ValueOutput output) {
        ContainerHelper.saveAllItems(output,getItems());
        var numbers=output.childrenList("Integers");integers.forEach((key,value)->{var entry=numbers.addChild();entry.putString("Key",key);entry.putInt("Value",value);});
        var text=output.childrenList("Strings");strings.forEach((key,value)->{var entry=text.addChild();entry.putString("Key",key);entry.putString("Value",value);});
    }
    public void load(ValueInput input) {
        clearContent();integers.clear();strings.clear();ContainerHelper.loadAllItems(input,getItems());
        for(var entry:input.childrenListOrEmpty("Integers"))setInt(entry.getStringOr("Key","invalid"),entry.getIntOr("Value",0));
        for(var entry:input.childrenListOrEmpty("Strings"))setString(entry.getStringOr("Key","invalid"),entry.getStringOr("Value",""));
    }
}
