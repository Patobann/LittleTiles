package team.creative.littletiles.common.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import team.creative.littletiles.common.block.LittleBlockRegistry;

public class ItemLittleBag extends Item {

    public static final int INVENTORY_SIZE = 24;
    public static final double MAX_VOLUME = 64.0D;

    public ItemLittleBag(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World level, List<ITextComponent> tooltip, ITooltipFlag flag) {
        ListNBT inventory = stack.getOrCreateTag().getList("inv", 10);
        if (inventory.isEmpty()) {
            tooltip.add(new StringTextComponent("Empty"));
            return;
        }
        for (int i = 0; i < inventory.size(); i++) {
            CompoundNBT entry = inventory.getCompound(i);
            tooltip.add(new StringTextComponent(readStateName(entry) + ": " + entry.getDouble("volume")));
        }
    }
    private static String readStateName(CompoundNBT entry) {
        return entry.contains("s", 8) ? entry.getString("s") : entry.getString("block");
    }

    public static boolean canAdd(ItemStack stack, BlockState state, double volume) {
        ListNBT inventory = stack.getOrCreateTag().getList("inv", 10);
        String stateName = LittleBlockRegistry.saveState(state);
        for (int i = 0; i < inventory.size(); i++) {
            CompoundNBT entry = inventory.getCompound(i);
            if (stateName.equals(readStateName(entry)))
                return entry.getDouble("volume") + volume <= MAX_VOLUME;
        }
        return inventory.size() < INVENTORY_SIZE && volume <= MAX_VOLUME;
    }

    public static boolean add(ItemStack stack, BlockState state, double volume) {
        if (!canAdd(stack, state, volume))
            return false;
        CompoundNBT tag = stack.getOrCreateTag();
        ListNBT inventory = tag.getList("inv", 10);
        String stateName = LittleBlockRegistry.saveState(state);
        for (int i = 0; i < inventory.size(); i++) {
            CompoundNBT entry = inventory.getCompound(i);
            if (stateName.equals(readStateName(entry))) {
                entry.putDouble("volume", entry.getDouble("volume") + volume);
                tag.put("inv", inventory);
                return true;
            }
        }
        CompoundNBT entry = new CompoundNBT();
        entry.putString("s", stateName);
        entry.putString("block", state.getBlock().getRegistryName().toString());
        entry.putDouble("volume", volume);
        inventory.add(entry);
        tag.put("inv", inventory);
        return true;
    }
}
