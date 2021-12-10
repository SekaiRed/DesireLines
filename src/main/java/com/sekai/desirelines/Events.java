package com.sekai.desirelines;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.stream.Stream;

public class Events {
    private static final Int2ObjectMap<Vector3d> lastEntitiesPos = new Int2ObjectLinkedOpenHashMap<>();

    private static void decay(LivingEntity living) {
        World world = living.level;

        if (!world.isClientSide && canEntityDecay(living)) {
            BlockPos pos = new BlockPos(living.getX(), living.getY() - 0.01D, living.getZ());
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            double random = Math.random();
            double factor = 1D;

            //System.out.println(block);

            if(!(living instanceof PlayerEntity))
                factor = Config.mobFactor;

            for(Config.BlockDecayEntry entry : Config.blockDecayEntries) {
                if(block.equals(ForgeRegistries.BLOCKS.getValue(entry.source))) {
                    if(random < (entry.getChance() * factor)) {
                        decayBlock(living, state, pos, world, entry);
                        return;
                    }
                }
            }
        }

    }

    private static boolean canEntityDecay(LivingEntity living) {
        //System.out.println(living + " " + isEntityMoving(living) + " " + !living.isPassenger() + " " + !living.isShiftKeyDown());
        return isEntityMoving(living) && !living.isPassenger() && !living.isShiftKeyDown() && !living.isSpectator() && living.isOnGround();
    }

    private static void decayBlock(LivingEntity living, BlockState originalState, BlockPos pos, World world, Config.BlockDecayEntry entry) {
        Block block = ForgeRegistries.BLOCKS.getValue(entry.result);
        if(block == null)
            return;

        BlockState newState = block.defaultBlockState();

        VoxelShape shape = newState.getCollisionShape(world, pos).move((double)pos.getX(), (double)pos.getY() - 0.001D, (double)pos.getZ());

        for(Entity entity : world.getEntities((Entity)null, shape.bounds())) {
            double d0 = VoxelShapes.collide(Direction.Axis.Y, entity.getBoundingBox().move(0.0D, 1.0D, 0.0D), Stream.of(shape), -1.0D);
            entity.teleportTo(entity.getX(), entity.getY() + 1.0D + d0, entity.getZ());
        }

        world.setBlockAndUpdate(pos, block.defaultBlockState());

        /*newState.getCollisionShape(world, pos).bounds()

        if(block.defaultBlockState().getCollisionShape(world, pos).max(Direction.Axis.Y) == originalState.getCollisionShape(world, pos).max(Direction.Axis.Y)) {
            world.setBlockAndUpdate(pos, block.defaultBlockState());
        } else {
            world.setBlockAndUpdate(pos, Block.pushEntitiesUp(originalState, block.defaultBlockState(), world, pos));
        }*/

        /*System.out.println(originalState.getCollisionShape(world, pos) + " " + block.defaultBlockState().getCollisionShape(world, pos));

        world.setBlockAndUpdate(pos, Block.pushEntitiesUp(originalState, block.defaultBlockState(), world, pos));*/

        /*BlockState state = block.defaultBlockState();
        double newHeight = pos.getY() + state.getCollisionShape(world, pos).max(Direction.Axis.Y);

        //TODO check for all living entities
        if(newHeight > living.getY())
            living.setPos(living.getX(), newHeight, living.getZ());

        world.setBlock(pos, state, 3);*/
    }

    //TODO easier calculation
    private static boolean isEntityMoving(LivingEntity living) {
        if(lastEntitiesPos.keySet().contains(living.getId())) {
            Vector3d lastPos = lastEntitiesPos.getOrDefault(living.getId(), living.position());

            //update position
            lastEntitiesPos.put(living.getId(), living.position());

            return Math.abs(living.getX() - lastPos.x()) > 0.0D || Math.abs(living.getY() - lastPos.y()) > 0.0D || Math.abs(living.getZ() - lastPos.z()) > 0.0D;
        } else {
            lastEntitiesPos.put(living.getId(), living.position());
            return false;
        }
    }

    private static double getEntityMoveSpeed(LivingEntity living) {
        if(lastEntitiesPos.keySet().contains(living.getId())) {
            Vector3d lastPos = lastEntitiesPos.getOrDefault(living.getId(), living.position());

            //update position
            lastEntitiesPos.put(living.getId(), living.position());

            return living.position().subtract(lastPos).length();
        } else {
            lastEntitiesPos.put(living.getId(), living.position());
            return 0D;
        }
    }

    @SubscribeEvent
    public static void decayMob(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving() != null && !event.getEntityLiving().level.isClientSide()) {
            if(event.getEntityLiving() instanceof PlayerEntity) {
                decay(event.getEntityLiving());
            } else if(Config.shouldMobDecay) {
                decay(event.getEntityLiving());
            }
        }
    }

    @SubscribeEvent
    public static void checkForErasedMobs(TickEvent.WorldTickEvent event) {
        if(event.side.isServer() && event.phase.equals(TickEvent.Phase.END)) {
            if(event.world instanceof ServerWorld) {
                //long time = System.nanoTime();
                ServerWorld serverWorld = ((ServerWorld) event.world);
                IntIterator iterator = lastEntitiesPos.keySet().iterator();
                boolean fakeServer = false;
                while(iterator.hasNext()) {
                    int key = iterator.nextInt();
                    boolean match = false;
                    int i = 0;
                    for(Entity entity : serverWorld.getAllEntities()) {
                        i++;
                        if(entity.getId() == key)
                            match = true;
                    }
                    if(!match && i!=0) {
                        iterator.remove();
                    }
                    if(i == 0)
                        fakeServer = true;
                }
                /*if(!fakeServer)
                    System.out.println(System.nanoTime() - time);*/
            }
        }
    }
}
