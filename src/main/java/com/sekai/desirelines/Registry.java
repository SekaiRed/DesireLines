package com.sekai.desirelines;

import com.sekai.desirelines.blocks.ModGrassBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.particles.ParticleType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class Registry {
    public static final String MINECRAFT = "minecraft";

    //Replacements
    public static final DeferredRegister<Item> REP_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MINECRAFT);
    public static final DeferredRegister<Block> REP_BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MINECRAFT);

    public static final DeferredRegister<Item> MOD_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DesireLines.MODID);
    public static final DeferredRegister<Block> MOD_BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, DesireLines.MODID);

    public static void init()
    {
        REP_ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        REP_BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());

        MOD_ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        MOD_BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<Block> MOD_GRASS_BLOCK = REP_BLOCKS.register("grass_block", () -> new ModGrassBlock(AbstractBlock.Properties.of(Material.GRASS).randomTicks().strength(0.6F).sound(SoundType.GRASS)));
    public static final RegistryObject<Item> MOD_GRASS_BLOCK_ITEM = REP_ITEMS.register("grass_block", () -> new BlockItem(MOD_GRASS_BLOCK.get(), new Item.Properties()));

    /*//Blocks
    public static final RegistryObject<Block> AMBIENCE_BLOCK = MOD_BLOCKS.register("ambience_block", AmbienceBlock::new);

    //Block Items
    public static final RegistryObject<Item> AMBIENCE_BLOCK_ITEM = MOD_ITEMS.register("ambience_block", () -> new BlockItemBase(AMBIENCE_BLOCK.get()));*/
}
