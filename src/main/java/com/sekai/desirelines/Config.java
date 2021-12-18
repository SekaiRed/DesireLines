package com.sekai.desirelines;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = DesireLines.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    public static final String[] DEFAULT_LIST = {
            "minecraft:grass_block,minecraft:dirt,0.05",
            "minecraft:dirt,minecraft:coarse_dirt,0.10",
            "minecraft:coarse_dirt,minecraft:grass_path,0.05",
            "minecraft:grass_path,minecraft:gravel,0.02",
            "minecraft:stone,minecraft:cobblestone,0.05",
            "minecraft:stone,minecraft:andesite,0.05",
            "minecraft:andesite,minecraft:gravel,0.05",
            "minecraft:cobblestone,minecraft:gravel,0.05",
            "minecraft:stone_bricks,minecraft:cracked_stone_bricks,0.01",
            "minecraft:nether_bricks,minecraft:cracked_nether_bricks,0.05"
    };

    public static final CommonConfig COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;
    static {
        final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    //Common
    public static double playerFactor;
    public static boolean shouldMobDecay;
    public static double mobFactor;
    public static List<BlockDecayEntry> blockDecayEntries;

    public static void bakeConfig() {
        //Common
        playerFactor = COMMON.playerFactor.get();
        shouldMobDecay = COMMON.shouldMobDecay.get();
        mobFactor = COMMON.mobFactor.get();
        blockDecayEntries = new ArrayList<>();
        for (String s : COMMON.blockList.get()) {
            String[] split = s.split(",");

            if (!Util.verifyEntryIntegrity(split))
                continue;

            BlockDecayEntry blockDecayEntry = Util.createBlockDecayEntry(split);
            blockDecayEntries.add(blockDecayEntry);
        }
    }

    public static class CommonConfig {
        public final ForgeConfigSpec.DoubleValue playerFactor;
        public final ForgeConfigSpec.BooleanValue shouldMobDecay;
        public final ForgeConfigSpec.DoubleValue mobFactor;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> blockList;

        public CommonConfig(ForgeConfigSpec.Builder builder) {
            playerFactor = builder
                    .comment("How likely are players to decay terrain?")
                    .translation("keepoffthegrass.config.mobFactor")
                    .defineInRange("mobFactor", 1.0D, 0D, Double.MAX_VALUE);
            shouldMobDecay = builder
                    .comment("Should mobs also decay terrain?")
                    .translation("keepoffthegrass.config.shouldMobDecay")
                    .define("shouldMobDecay", false);
            mobFactor = builder
                    .comment("How likely are mobs to decay terrain?")
                    .translation("keepoffthegrass.config.mobFactor")
                    .defineInRange("mobFactor", 1.0D, 0D, Double.MAX_VALUE);
            blockList = builder
                    .comment("A list of entries, first is the block's full id (minecraft:grass), next the target block id, and third the likelihood of it decaying.")
                    .translation("keepoffthegrass.config.blockList")
                    .defineList("blockList", Arrays.asList(DEFAULT_LIST), it -> it instanceof String);
        }
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent) {
        if (configEvent.getConfig().getSpec() == Config.COMMON_SPEC) {
            bakeConfig();
        }
    }

    public static class BlockDecayEntry {
        BlockStateWithParameters source;
        BlockStateWithParameters result;
        double chance;

        /*public BlockDecayEntry(ResourceLocation source, ResourceLocation result, double chance) {
            this.source = source;
            this.result = result;
            this.chance = chance;
        }*/

        public BlockDecayEntry(BlockStateParser source, BlockStateParser result, double chance) {
            this.source = new BlockStateWithParameters(source);
            this.result = new BlockStateWithParameters(result);
            this.chance = chance;
        }

        public BlockStateWithParameters getSource() {
            return source;
        }

        public BlockStateWithParameters getResult() {
            return result;
        }

        public double getChance() {
            return chance;
        }

        @Override
        public String toString() {
            return "BlockDecayEntry{" +
                    "source=" + source +
                    ", result=" + result +
                    ", chance=" + chance +
                    '}';
        }

        public static class BlockStateWithParameters {
            private final Block block;
            private final Map<Property<?>, Comparable<?>> properties;
            private final BlockState state;

            public BlockStateWithParameters(BlockStateParser parser) {
                block = parser.getState() != null ? parser.getState().getBlock() : null;
                properties = parser.getProperties();
                state = parser.getState();
            }

            public boolean isSame(BlockState state) {
                return isBlock(state) && doPropertiesMatch(state);
            }

            public boolean isBlock(BlockState state) {
                return state.is(this.block);
            }

            public boolean doPropertiesMatch(BlockState state) {
                for(Property<?> property : properties.keySet()) {
                    if (state.getValue(property) != this.state.getValue(property)) {
                        return false;
                    }
                }
                return true;
                /*for(Property<?> property : this.properties) {
                    if (blockstate.getValue(property) != this.state.getValue(property)) {
                        return false;
                    }
                }*/
            }

            public BlockState getBlockState() {
                return state;
            }
        }
    }
}
