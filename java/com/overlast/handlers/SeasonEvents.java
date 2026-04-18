package com.overlast.handlers;

import java.util.Random;

import com.overlast.OverLast;
import com.overlast.config.OverConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.BlockReed;
import net.minecraft.block.BlockStem;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = OverLast.MOD_ID)
public class SeasonEvents {

    private static final Random RANDOM = new Random();

    private static final int SEASON_SPRING = 0;
    private static final int SEASON_SUMMER = 1;
    private static final int SEASON_WINTER = 3;

    @SubscribeEvent
    public static void onCropGrow(BlockEvent.CropGrowEvent.Pre event) {
        if (!OverConfig.SEASONS.enableSeasons || event.getWorld().isRemote || event.getWorld().provider.getDimension() != 0) {
            return;
        }

        Block block = event.getState().getBlock();
        if (!isSeasonalCrop(block)) {
            return;
        }

        int season = EventHandlerServer.getSeasonIndex(event.getWorld());
        if (season == SEASON_SUMMER) {
            if (RANDOM.nextInt(3) != 0) {
                event.setResult(Event.Result.ALLOW);
            }
            return;
        }

        if (season == SEASON_WINTER && RANDOM.nextInt(4) != 0) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (!OverConfig.SEASONS.enableSeasons || event.phase != TickEvent.Phase.END || event.world.isRemote) {
            return;
        }
        if (event.world.provider.getDimension() != 0 || event.world.playerEntities.isEmpty()) {
            return;
        }
        if (EventHandlerServer.getSeasonIndex(event.world) != SEASON_SPRING || event.world.getTotalWorldTime() % 20L != 0L) {
            return;
        }

        for (int i = 0; i < event.world.playerEntities.size(); i++) {
            BlockPos center = event.world.playerEntities.get(i).getPosition();
            meltAround(event.world, center);
        }
    }

    private static boolean isSeasonalCrop(Block block) {
        return block instanceof BlockCrops
                || block instanceof BlockStem
                || block instanceof BlockNetherWart
                || block instanceof BlockReed
                || block instanceof BlockCactus
                || block instanceof BlockCocoa
                || block instanceof BlockBush;
    }

    private static void meltAround(World world, BlockPos center) {
        for (int i = 0; i < 8; i++) {
            BlockPos pos = center.add(RANDOM.nextInt(33) - 16, RANDOM.nextInt(9) - 4, RANDOM.nextInt(33) - 16);
            Block block = world.getBlockState(pos).getBlock();

            if (block == Blocks.SNOW_LAYER || block == Blocks.SNOW) {
                world.setBlockToAir(pos);
                continue;
            }

            if (block == Blocks.ICE || block == Blocks.FROSTED_ICE || block == Blocks.PACKED_ICE || block instanceof BlockIce) {
                if (world.provider.doesWaterVaporize()) {
                    world.setBlockToAir(pos);
                } else {
                    world.setBlockState(pos, Blocks.WATER.getDefaultState(), 3);
                    world.notifyNeighborsOfStateChange(pos.down(), Blocks.WATER, false);
                }
                continue;
            }

            if (block == Blocks.SNOW_LAYER && world.isAirBlock(pos.up())) {
                world.notifyNeighborsOfStateChange(pos, Blocks.AIR, false);
            }
        }
    }
}
