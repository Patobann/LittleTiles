package team.creative.littletiles.common.block;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import team.creative.littletiles.common.api.block.LittleBlock;

public final class LittleBlockRegistry {

    private static final HashMap<Block, LittleBlock> BLOCK_MAP = new HashMap<>();
    private static final HashMap<String, LittleBlock> NAME_MAP = new HashMap<>();

    private LittleBlockRegistry() {}

    public static LittleBlock getLittleBlock(Block block) {
        return BLOCK_MAP.computeIfAbsent(block, LittleMCBlock::new);
    }

    public static LittleBlock getLittleBlock(String name) {
        LittleBlock cached = NAME_MAP.get(name);
        if (cached != null)
            return cached;

        String blockName = extractBlockName(name);
        try {
            ResourceLocation location = new ResourceLocation(blockName);
            if (ForgeRegistries.BLOCKS.containsKey(location)) {
                LittleBlock little = getLittleBlock(ForgeRegistries.BLOCKS.getValue(location));
                NAME_MAP.put(name, little);
                return little;
            }
        } catch (RuntimeException ignored) {}

        LittleBlock missing = new LittleMissingBlock(name);
        NAME_MAP.put(name, missing);
        return missing;
    }

    public static String saveState(BlockState state) {
        String name = state.getBlock().getRegistryName().toString();
        if (state.getValues().isEmpty())
            return name;
        return name + "[" + state.getValues().entrySet().stream()
                .sorted((one, two) -> one.getKey().getName().compareTo(two.getKey().getName()))
                .map(LittleBlockRegistry::saveProperty)
                .collect(Collectors.joining(",")) + "]";
    }

    public static BlockState loadState(String name) {
        String blockName = extractBlockName(name);
        LittleBlock little = getLittleBlock(blockName);
        BlockState state = little.getBlock().defaultBlockState();

        int start = name.indexOf('[');
        if (start < 0 || !name.endsWith("]") || little instanceof LittleMissingBlock)
            return state;

        String properties = name.substring(start + 1, name.length() - 1);
        if (properties.isEmpty())
            return state;
        for (String propertyData : properties.split(",")) {
            String[] pair = propertyData.split("=", 2);
            if (pair.length != 2)
                throw new IllegalArgumentException("Invalid block state property: " + propertyData);
            Property<?> property = state.getBlock().getStateDefinition().getProperty(pair[0]);
            if (property == null)
                throw new IllegalArgumentException("Unknown property '" + pair[0] + "' in " + name);
            Optional<?> value = property.getValue(pair[1]);
            if (!value.isPresent())
                throw new IllegalArgumentException("Invalid value '" + pair[1] + "' in " + name);
            state = setValue(state, property, value.get());
        }
        return state;
    }

    private static String extractBlockName(String stateName) {
        int properties = stateName.indexOf('[');
        String name = properties >= 0 ? stateName.substring(0, properties) : stateName;
        String[] legacy = name.split(":");
        if (legacy.length == 3 && legacy[2].matches("-?\\d+"))
            return legacy[0] + ":" + legacy[1];
        return name;
    }

    private static String saveProperty(Entry<Property<?>, Comparable<?>> entry) {
        return entry.getKey().getName() + "=" + propertyName(entry.getKey(), entry.getValue());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static String propertyName(Property property, Comparable value) {
        return property.getName(value);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static BlockState setValue(BlockState state, Property property, Object value) {
        return state.setValue(property, (Comparable) value);
    }
}
