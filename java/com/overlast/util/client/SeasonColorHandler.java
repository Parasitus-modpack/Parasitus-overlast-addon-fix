package com.overlast.util.client;

import com.overlast.config.OverConfig;
import com.overlast.handlers.EventHandlerServer;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SeasonColorHandler {

    private static final int SEASON_SPRING = 0;
    private static final int SEASON_SUMMER = 1;
    private static final int SEASON_FALL = 2;
    private static final int SEASON_WINTER = 3;

    @SubscribeEvent
    public void onBlockColors(ColorHandlerEvent.Block event) {
        event.getBlockColors().registerBlockColorHandler(createGrassColorHandler(),
                Blocks.GRASS,
                Blocks.TALLGRASS,
                Blocks.DOUBLE_PLANT);

        event.getBlockColors().registerBlockColorHandler(createFoliageColorHandler(),
                Blocks.LEAVES,
                Blocks.LEAVES2,
                Blocks.VINE);
    }

    private IBlockColor createGrassColorHandler() {
        return (state, world, pos, tintIndex) -> {
            if (world == null || pos == null || !OverConfig.SEASONS.enableSeasons) {
                return ColorizerGrass.getGrassColor(0.5D, 1.0D);
            }

            int baseColor = BiomeColorHelper.getGrassColorAtPos(world, pos);
            if (state.getBlock() == Blocks.DOUBLE_PLANT && state.getValue(BlockDoublePlant.VARIANT) != BlockDoublePlant.EnumPlantType.GRASS
                    && state.getValue(BlockDoublePlant.VARIANT) != BlockDoublePlant.EnumPlantType.FERN) {
                return baseColor;
            }
            return tintForSeason(baseColor, getSeasonIndex(world), true);
        };
    }

    private IBlockColor createFoliageColorHandler() {
        return (state, world, pos, tintIndex) -> {
            if (world == null || pos == null || !OverConfig.SEASONS.enableSeasons) {
                return ColorizerFoliage.getFoliageColorBasic();
            }

            int baseColor = BiomeColorHelper.getFoliageColorAtPos(world, pos);
            return tintForSeason(baseColor, getSeasonIndex(world), false);
        };
    }

    private int getSeasonIndex(IBlockAccess world) {
        return world instanceof World ? EventHandlerServer.getSeasonIndex((World) world) : SEASON_SPRING;
    }

    private int tintForSeason(int baseColor, int season, boolean grass) {
        switch (season) {
            case SEASON_SPRING:
                return blend(baseColor, grass ? 0x285F1E : 0x2F6F28, 0.45F);
            case SEASON_SUMMER:
                return blend(baseColor, grass ? 0x4E8B2D : 0x4A7B24, 0.20F);
            case SEASON_FALL:
                return blend(baseColor, grass ? 0x9B5C10 : 0xB85E18, 0.60F);
            case SEASON_WINTER:
                return blend(baseColor, grass ? 0x6E7E72 : 0x7B8791, 0.45F);
            default:
                return baseColor;
        }
    }

    private int blend(int baseColor, int tintColor, float tintStrength) {
        int baseR = (baseColor >> 16) & 0xFF;
        int baseG = (baseColor >> 8) & 0xFF;
        int baseB = baseColor & 0xFF;

        int tintR = (tintColor >> 16) & 0xFF;
        int tintG = (tintColor >> 8) & 0xFF;
        int tintB = tintColor & 0xFF;

        int outR = Math.round(baseR * (1.0F - tintStrength) + tintR * tintStrength);
        int outG = Math.round(baseG * (1.0F - tintStrength) + tintG * tintStrength);
        int outB = Math.round(baseB * (1.0F - tintStrength) + tintB * tintStrength);

        return (outR << 16) | (outG << 8) | outB;
    }
}
