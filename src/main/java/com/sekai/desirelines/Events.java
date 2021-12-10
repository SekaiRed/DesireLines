package com.sekai.desirelines;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;

public class Events {
    private static Int2ObjectMap<Vector3d> lastEntitiesPos = new Int2ObjectLinkedOpenHashMap<>();

    private static void decay(Entity living) {
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
                        decayBlock(world, living, pos, entry);
                        return;
                    }
                }
            }
        }

    }

    private static boolean canEntityDecay(Entity living) {
        //System.out.println(living + " " + isEntityMoving(living) + " " + !living.isPassenger() + " " + !living.isShiftKeyDown());
        return isEntityMoving(living) && !living.isPassenger() && !living.isShiftKeyDown() && !living.isSpectator();
    }

    private static void decayBlock(World world, Entity living, BlockPos entityLocation, Config.BlockDecayEntry entry) {
        Block block = ForgeRegistries.BLOCKS.getValue(entry.result);
        if(block == null)
            return;

        BlockState state = block.defaultBlockState();
        double newHeight = entityLocation.getY() + state.getCollisionShape(world, entityLocation).max(Direction.Axis.Y);

        //TODO check for all living entities
        if(newHeight > living.getY())
            living.setPos(living.getX(), newHeight, living.getZ());

        world.setBlock(entityLocation, state, 3);
    }

    //TODO easier calculation
    private static boolean isEntityMoving(Entity living) {
        if(lastEntitiesPos.keySet().contains(living.getId())) {
            boolean isMoving = false;

            Vector3d lastPos = lastEntitiesPos.getOrDefault(living.getId(), living.position());

            if (Math.abs(living.getX() - lastPos.x()) > 0.0D || Math.abs(living.getY() - lastPos.y()) > 0.0D || Math.abs(living.getZ() - lastPos.z()) > 0.0D)
                isMoving = true;

            //update position
            lastEntitiesPos.put(living.getId(), living.position());

            return isMoving;
        } else {
            lastEntitiesPos.put(living.getId(), living.position());
            return false;
        }

        //boolean isMoving = false;

        /*if(living instanceof PlayerEntity) {
            System.out.println(" 1) " + living.position().subtract(new Vector3d(living.xOld, living.yOld, living.zOld)));
            System.out.println(" 2) " + living.position().subtract(new Vector3d(living.xo, living.yo, living.zo)));
            System.out.println(" 3) " + living.getDeltaMovement());
        }*/

        /*if (Math.abs(living.getX() - living.xOld) > 0.0D || Math.abs(living.getY() - living.yOld) > 0.0D || Math.abs(living.getZ() - living.zOld) > 0.0D)
            isMoving = true;

        return isMoving;*/
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
                ServerWorld serverWorld = ((ServerWorld) event.world);
                IntIterator iterator = lastEntitiesPos.keySet().iterator();
                int j = 0;
                while(iterator.hasNext()) {
                    j++;
                    int key = iterator.nextInt();
                    //The key doesn't exist anymore, rip, remove nao
                    /*if(!serverWorld.getEntities().allMatch(entity -> {
                        //System.out.println(entity.getId() + " comp " + key + " returns " + (entity.getId() != key));
                        return entity.getId() != key;
                    })) {*/
                    /*if(serverWorld.getEntities().noneMatch(entity -> {
                        System.out.println(entity.getId() + " comp " + key + " returns " + (entity.getId() == key));
                        return entity.getId() == key;
                    })) {
                        System.out.println("removed " + key);
                        iterator.remove();
                    }*/
                    boolean match = false;
                    //System.out.println(serverWorld.getAllEntities().spliterator().estimateSize());
                    int i = 0;
                    for(Entity entity : serverWorld.getAllEntities()) {
                        i++;
                        if(entity.getId() == key)
                            match = true;
                        //System.out.println(entity.getId() + " == " + key + " is " + (entity.getId() == key) + " and match is " + match);
                    }
                    if(!match && i!=0) {
                        //System.out.println("1) removed " + key + " with match at " + match + " " + i + " " + j);
                        iterator.remove();
                    }
                    /*if(!match) {
                        System.out.println("1) removed " + key + " with match at " + match + " " + i + " " + j);
                        //iterator.remove();
                    }
                    if(match) {
                        System.out.println("2) removed " + key + " with match at " + match + " " + i + " " + j);
                        //iterator.remove();
                    }*/
                }
            }
            /*if(event.world
            //getAllEntities()
            IntIterator iterator = lastEntitiesPos.keySet().iterator();
            while(iterator.hasNext()) {
                int key = iterator.nextInt();
            }
            System.out.println("fuck");*/
        }
    }

    /*@SubscribeEvent
    public static void decayMob(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving() != null && !(event.getEntityLiving() instanceof PlayerEntity) && Config.shouldMobDecay) {
            decay(event.getEntityLiving());
        }
    }*/

    /*@SubscribeEvent
    public static void decayPlayer(PlayerEvent event) {
        if (event.getPlayer() != null) {
            decay(event.getPlayer());
        }
    }*/
}
