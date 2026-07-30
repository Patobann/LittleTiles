package team.creative.littletiles;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.RegistryObject;
import team.creative.creativecore.common.network.CreativeNetwork;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.handler.GuiContainerHandler;
import team.creative.creativecore.common.gui.handler.GuiContainerHandler.GuiHandlerPlayer;
import team.creative.littletiles.common.block.BlockTiles;
import team.creative.littletiles.common.block.LittleBlockRegistry;
import team.creative.littletiles.common.block.TETiles;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.LittleToolConfigGui;
import team.creative.littletiles.common.item.ItemLittleChisel;
import team.creative.littletiles.common.item.ItemLittleHammer;
import team.creative.littletiles.common.item.ItemLittleBag;
import team.creative.littletiles.common.item.LittleToolActionPacket;
import team.creative.littletiles.common.item.LittleToolConfigPacket;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.tile.LittleCollection;
import team.creative.littletiles.common.tile.LittleElement;
import team.creative.littletiles.common.tile.LittleTile;

@Mod(LittleTiles.MODID)
public class LittleTiles {

    public static final String MODID = "littletiles";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static final CreativeNetwork NETWORK = new CreativeNetwork("1", LOGGER, new ResourceLocation(MODID, "main"));

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MODID);

    public static final RegistryObject<Block> TILES_BLOCK = BLOCKS.register("tiles", BlockTiles::new);
    public static final RegistryObject<Item> CHISEL_ITEM = ITEMS.register("chisel",
            () -> new ItemLittleChisel(new Item.Properties().stacksTo(1).tab(ItemGroup.TAB_TOOLS)));
    public static final RegistryObject<Item> HAMMER_ITEM = ITEMS.register("hammer",
            () -> new ItemLittleHammer(new Item.Properties().stacksTo(1).tab(ItemGroup.TAB_TOOLS)));
    public static final RegistryObject<Item> CONTAINER_ITEM = ITEMS.register("container",
            () -> new ItemLittleBag(new Item.Properties().stacksTo(1).tab(ItemGroup.TAB_TOOLS)));
    public static final RegistryObject<Item> TILES_ITEM = ITEMS.register("tiles",
            () -> new BlockItem(TILES_BLOCK.get(), new Item.Properties().tab(ItemGroup.TAB_BUILDING_BLOCKS)));
    public static final RegistryObject<TileEntityType<TETiles>> TILES_TE_TYPE = TILE_ENTITIES.register("tiles",
            () -> TileEntityType.Builder.of(TETiles::new, TILES_BLOCK.get()).build(null));

    public LittleTiles() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        TILE_ENTITIES.register(modBus);
        NETWORK.registerType(LittleToolActionPacket.class);
        NETWORK.registerType(LittleToolConfigPacket.class);
        GuiContainerHandler.registerGuiHandler("littletiles_tool", new GuiHandlerPlayer() {
            @Override
            public GuiLayer create(PlayerEntity player) {
                return new LittleToolConfigGui(player);
            }
        });
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::gatherData);

        runCoreCodecChecks();
        LOGGER.info("LittleTiles 1.16.5 port bootstrap loaded");
    }

    private void runCoreCodecChecks() {
        BlockState state = Blocks.OAK_STAIRS.defaultBlockState().setValue(HorizontalBlock.FACING, Direction.EAST);
        String serialized = LittleBlockRegistry.saveState(state);
        if (LittleBlockRegistry.loadState(serialized) != state)
            throw new IllegalStateException("Little block-state codec failed for " + serialized);

        LittleElement element = new LittleElement(state, 0x80402010);
        LittleElement loaded = new LittleElement(element.save(new CompoundNBT()));
        if (!element.equals(loaded))
            throw new IllegalStateException("Little element NBT codec failed for " + serialized);

        LittleTile tile = new LittleTile(element, Arrays.asList(new LittleBox(0, 0, 0, 8, 16, 16), new LittleBox(8, 0, 0, 16, 16, 16)));
        LittleTile loadedTile = new LittleTile(tile.save(new CompoundNBT()));
        if (!tile.equals(loadedTile))
            throw new IllegalStateException("Little tile multi-box NBT codec failed");
        loadedTile.combine();
        if (loadedTile.size() != 1 || loadedTile.getVolume() != 4096)
            throw new IllegalStateException("Little tile box combination failed");
        if (!loadedTile.equals(new LittleTile(loadedTile.save(new CompoundNBT()))))
            throw new IllegalStateException("Little tile single-box NBT codec failed");

        LittleCollection collection = new LittleCollection();
        collection.add(new LittleTile(element, new LittleBox(0, 0, 0, 8, 16, 16)));
        collection.add(new LittleTile(element, new LittleBox(8, 0, 0, 16, 16, 16)));
        LittleCollection loadedCollection = new LittleCollection();
        loadedCollection.load(collection.save());
        int iteratedBoxes = 0;
        for (Object ignored : loadedCollection.boxes())
            iteratedBoxes++;
        if (loadedCollection.size() != 2 || loadedCollection.boxCount() != 2 || iteratedBoxes != 2)
            throw new IllegalStateException("Little collection NBT/box iteration failed");
        if (!loadedCollection.combineTiles() || loadedCollection.size() != 1 || loadedCollection.boxCount() != 1 || loadedCollection.getVolume() != 4096)
            throw new IllegalStateException("Little collection combination failed");
        if (BlockTiles.countFullBlocks(LittleGrid.overallDefault(), loadedCollection).get(state) != 1)
            throw new IllegalStateException("Little material block-equivalent accounting failed");

        LOGGER.info("Little block-state/tile codecs loaded: {}", serialized);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(this::verifyBlockEntityCodec);
    }

    private void gatherData(GatherDataEvent event) {
        verifyBlockEntityCodec();
    }

    private void verifyBlockEntityCodec() {
            LittleElement element = new LittleElement(Blocks.STONE.defaultBlockState(), -1);
            TETiles original = new TETiles();
            original.getTiles().add(new LittleTile(element, new LittleBox(0, 0, 0, 8, 16, 16)));
            original.convertTo(LittleGrid.get(32));

            CompoundNBT saved = original.save(new CompoundNBT());
            TETiles loaded = new TETiles();
            loaded.load(TILES_BLOCK.get().defaultBlockState(), saved);
            if (loaded.getGrid() != LittleGrid.get(32) || loaded.getTiles().size() != 1 || loaded.getTiles().getVolume() != 16384)
                throw new IllegalStateException("LittleTiles block entity NBT codec failed");
            VoxelShape shape = BlockTiles.createShape(loaded.getGrid(), loaded.getTiles());
            if (shape.isEmpty() || shape.bounds().minX != 0 || shape.bounds().maxX != 0.5D || shape.bounds().maxY != 1 || shape.bounds().maxZ != 1)
                throw new IllegalStateException("LittleTiles voxel shape generation failed");
            ItemStack bag = new ItemStack(CONTAINER_ITEM.get());
            if (!ItemLittleBag.add(bag, Blocks.STONE.defaultBlockState(), 0.5D)
                    || !ItemLittleBag.add(bag, Blocks.STONE.defaultBlockState(), 0.5D)
                    || bag.getTag().getList("inv", 10).size() != 1
                    || bag.getTag().getList("inv", 10).getCompound(0).getDouble("volume") != 1.0D
                    || !ItemLittleBag.take(bag, Blocks.STONE.defaultBlockState(), 0.25D)
                    || bag.getTag().getList("inv", 10).getCompound(0).getDouble("volume") != 0.75D)
                throw new IllegalStateException("Little ingredient bag volume accounting failed");
            LOGGER.info("LittleTiles block entity registered and NBT-verified");
    }
}
