package com.sekai.desirelines;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class Events {
    private static void decay(Entity living) {
        World world = living.level;

        if (!world.isClientSide && isEntityMoving(living)) {
            BlockPos pos = new BlockPos(living.getX(), living.getY() - 0.01D, living.getZ());
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            double random = Math.random();

            for(Config.BlockDecayEntry entry : Config.blockDecayEntries) {
                if(block.equals(ForgeRegistries.BLOCKS.getValue(entry.source))) {
                    if(random < entry.getChance()) {
                        decayBlock(world, living, pos, entry);
                        return;
                    }
                }
            }
        }

    }

    private static void decayBlock(World world, Entity living, BlockPos entityLocation, Config.BlockDecayEntry entry) {
        Block block = ForgeRegistries.BLOCKS.getValue(entry.result);
        if(block == null)
            return;

        BlockState state = block.defaultBlockState();
        double newHeight = entityLocation.getY() + state.getCollisionShape(world, entityLocation).max(Direction.Axis.Y);

        if(newHeight > living.getY())
            living.setPos(living.getX(), newHeight, living.getZ());

        world.setBlock(entityLocation, state, 3);
    }

    //TODO easier calculation
    private static boolean isEntityMoving(Entity living) {
        boolean isMoving = false;

        if (Math.abs(living.getX() - living.xOld) > 0.0D || Math.abs(living.getY() - living.yOld) > 0.0D || Math.abs(living.getZ() - living.zOld) > 0.0D)
            isMoving = true;

        return isMoving;
    }

    @SubscribeEvent
    public static void decayMob(LivingEvent event) {
        if (event.getEntityLiving() != null && !(event.getEntityLiving() instanceof PlayerEntity) && Config.shouldMobDecay) {
            decay(event.getEntityLiving());
        }
    }

    @SubscribeEvent
    public static void decayPlayer(PlayerEvent event) {
        if (event.getPlayer() != null) {
            decay(event.getPlayer());
        }
    }
}
