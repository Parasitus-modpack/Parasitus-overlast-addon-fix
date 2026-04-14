package com.overlast.handlers;

import com.dhanantry.scapeandrunparasites.entity.ai.misc.EntityPInfected;
import com.dhanantry.scapeandrunparasites.entity.monster.infected.*;
import com.dhanantry.scapeandrunparasites.init.SRPPotions;
import com.overlast.OverLast;
import com.overlast.gui.RenderHUD;
import com.overlast.lib.ModMobEffects;
import com.overlast.util.Broadcasts;
import com.overlast.util.client.KeyBinds;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockStone;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@EventBusSubscriber(modid = OverLast.MOD_ID)
public class EventHandlerServer {

    private static final int FORECAST_MESSAGE_COUNT = 10;
    private static final int DAILY_MESSAGE_COUNT = 58;
    private static final int DAY_START_WINDOW_TICKS = 1200;
    private static final int SEASON_LENGTH_DAYS = 10;
    private static final int SPRING_OUTRO_COUNT = 9;
    private static final int SUMMER_OUTRO_COUNT = 9;
    private static final int FALL_OUTRO_COUNT = 10;
    private static final int WINTER_OUTRO_COUNT = 9;
    private static final Random RANDOM = new Random();
    private static int updateTimer = 0;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if (KeyBinds.KEY_SWITCH.isPressed()) {
            if (RenderHUD.switchhud) {
                RenderHUD.switchhud = false;
            } else {
                RenderHUD.switchhud = true;
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerMining(BlockEvent.HarvestDropsEvent e) {
        if (e.getHarvester() != null && !(e.getHarvester().getActivePotionEffect(ModMobEffects.FORTUNATE) == null)) {
            if (e.isSilkTouching())
                return;
            Block origBlock = e.getState().getBlock();
            if (!e.getHarvester().getHeldItemMainhand().canHarvestBlock(e.getState()))
                return;
            if (origBlock instanceof BlockStone)
                return;
            if (!(origBlock instanceof BlockOre))
                return;
            if(origBlock == Blocks.DIAMOND_ORE || origBlock == Blocks.COAL_ORE || origBlock == Blocks.LAPIS_ORE || origBlock == Blocks.EMERALD_ORE || origBlock == Blocks.QUARTZ_ORE || origBlock == Blocks.QUARTZ_ORE)
                e.getDrops().get(0).grow(1);
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote) {
            return;
        }
        if (event.world.provider.getDimension() != 0 || event.world.getMinecraftServer() == null) {
            return;
        }

        SeasonCalendarData calendar = SeasonCalendarData.get(event.world);
        calendar.updateFromWorld(event.world);

        long dayTime = Math.floorMod(event.world.getWorldTime(), 24000L);
        if (dayTime >= DAY_START_WINDOW_TICKS || event.world.playerEntities.isEmpty() || calendar.hasBroadcastForCurrentDay()) {
            return;
        }

        calendar.markBroadcastSent();
        broadcastDailyMessage(event.world.getMinecraftServer());
    }


    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onEntityUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!event.getEntity().world.isRemote) {
            EntityUpdate(event.getEntity());

            if (event.getEntity() instanceof EntityAnimal) {
                EntityAnimal annimals = (EntityAnimal) event.getEntity();
                if (!(annimals.getActivePotionEffect(ModMobEffects.PARASITESPURIFY) == null)) {
                    annimals.removePotionEffect(SRPPotions.COTH_E);
                    annimals.removePotionEffect(SRPPotions.FEAR_E);
                    annimals.removePotionEffect(SRPPotions.BLEED_E);
                    annimals.removePotionEffect(SRPPotions.CORRO_E);
                    annimals.removePotionEffect(SRPPotions.VIRA_E);
                }
            }
            if (event.getEntity() instanceof EntityVillager) {
                EntityVillager villager = (EntityVillager) event.getEntity();
                if (!(villager.getActivePotionEffect(ModMobEffects.PARASITESPURIFY) == null)) {
                    villager.removePotionEffect(SRPPotions.COTH_E);
                    villager.removePotionEffect(SRPPotions.FEAR_E);
                    villager.removePotionEffect(SRPPotions.BLEED_E);
                    villager.removePotionEffect(SRPPotions.CORRO_E);
                }
            }
        }
    }

    public static void EntityUpdate(Entity entity) {
        if (updateTimer < 20) {
            updateTimer++;
        } else {
            updateTimer = 0;
            if (entity instanceof EntityPInfected) {
                double dx = entity.posX;
                double dy = entity.posY;
                double dz = entity.posZ;
                if (!(((EntityPInfected) entity).getActivePotionEffect(ModMobEffects.PARASITESPURIFY) == null)
                        && ((EntityPInfected) entity).getActivePotionEffect(ModMobEffects.PARASITESPURIFY).getDuration() <= 40) {
                    EntityMob infAnnimals = (EntityMob) entity;
                    if (infAnnimals instanceof EntityDorpa) {
                        infAnnimals.setDead();
                        infAnnimals.world.createExplosion(infAnnimals, dx, dy + 0.2F, dz, 0, true);
                        EntitySpider entityageable = new EntitySpider(infAnnimals.world);
                        entityageable.setLocationAndAngles(dx, dy + 0.2F, dz, 0.0F, 0.0F);
                        infAnnimals.world.spawnEntity(entityageable);
                    }
                    if (infAnnimals instanceof EntityInfBear) {
                        infAnnimals.setDead();
                        infAnnimals.world.createExplosion(infAnnimals, dx, dy + 0.2F, dz, 0, true);
                        EntityAnimal entityageable = new EntityPolarBear(infAnnimals.world);
                        entityageable.setGrowingAge(-24000);
                        entityageable.setLocationAndAngles(dx, dy + 0.2F, dz, 0.0F, 0.0F);
                        infAnnimals.world.spawnEntity(entityageable);
                    }
                    if (infAnnimals instanceof EntityInfCow) {
                        infAnnimals.setDead();
                        infAnnimals.world.createExplosion(infAnnimals, dx, dy + 0.2F, dz, 0, true);
                        EntityAnimal entityageable = new EntityCow(infAnnimals.world);
                        entityageable.setGrowingAge(-24000);
                        entityageable.setLocationAndAngles(dx, dy + 0.2F, dz, 0.0F, 0.0F);
                        infAnnimals.world.spawnEntity(entityageable);
                    }
                    if (infAnnimals instanceof EntityInfEnderman) {
                        infAnnimals.setDead();
                        infAnnimals.world.createExplosion(infAnnimals, dx, dy + 0.2F, dz, 0, true);
                        EntityEnderman entityageable = new EntityEnderman(infAnnimals.world);
                        entityageable.setLocationAndAngles(dx, dy + 0.2F, dz, 0.0F, 0.0F);
                        infAnnimals.world.spawnEntity(entityageable);
                    }
                    if (infAnnimals instanceof EntityInfHorse) {
                        infAnnimals.setDead();
                        infAnnimals.world.createExplosion(infAnnimals, dx, dy + 0.2F, dz, 0, true);
                        EntityAnimal entityageable = new EntityHorse(infAnnimals.world);
                        entityageable.setGrowingAge(-24000);
                        entityageable.setLocationAndAngles(dx, dy + 0.2F, dz, 0.0F, 0.0F);
                        infAnnimals.world.spawnEntity(entityageable);
                    }
                    if (infAnnimals instanceof EntityInfHuman) {
                        infAnnimals.setDead();
                        infAnnimals.world.createExplosion(infAnnimals, dx, dy + 0.2F, dz, 0, true);
                        EntityZombie entityageable = new EntityZombie(infAnnimals.world);
                        entityageable.setLocationAndAngles(dx, dy + 0.2F, dz, 0.0F, 0.0F);
                        infAnnimals.world.spawnEntity(entityageable);
                    }
                    if (infAnnimals instanceof EntityInfPig) {
                        infAnnimals.setDead();
                        infAnnimals.world.createExplosion(infAnnimals, dx, dy + 0.2F, dz, 0, true);
                        EntityAnimal entityageable = new EntityPig(infAnnimals.world);
                        entityageable.setGrowingAge(-24000);
                        entityageable.setLocationAndAngles(dx, dy + 0.2F, dz, 0.0F, 0.0F);
                        infAnnimals.world.spawnEntity(entityageable);
                    }
                    if (infAnnimals instanceof EntityInfSheep) {
                        infAnnimals.setDead();
                        infAnnimals.world.createExplosion(infAnnimals, dx, dy + 0.2F, dz, 0, true);
                        EntityAnimal entityageable = new EntitySheep(infAnnimals.world);
                        entityageable.setGrowingAge(-24000);
                        entityageable.setLocationAndAngles(dx, dy + 0.2F, dz, 0.0F, 0.0F);
                        infAnnimals.world.spawnEntity(entityageable);
                    }
                    if (infAnnimals instanceof EntityInfVillager) {
                        infAnnimals.setDead();
                        infAnnimals.world.createExplosion(infAnnimals, dx, dy + 0.2F, dz, 0, true);
                        EntityVillager entityageable = new EntityVillager(infAnnimals.world);
                        entityageable.setGrowingAge(-24000);
                        entityageable.setLocationAndAngles(dx, dy + 0.2F, dz, 0.0F, 0.0F);
                        infAnnimals.world.spawnEntity(entityageable);
                    }
                    if (infAnnimals instanceof EntityInfWolf) {
                        infAnnimals.setDead();
                        infAnnimals.world.createExplosion(infAnnimals, dx, dy + 0.2F, dz, 0, true);
                        EntityAnimal entityageable = new EntityWolf(infAnnimals.world);
                        entityageable.setGrowingAge(-24000);
                        entityageable.setLocationAndAngles(dx, dy + 0.2F, dz, 0.0F, 0.0F);
                        infAnnimals.world.spawnEntity(entityageable);
                    }
                }
            }
            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;
                if (!(player.getActivePotionEffect(ModMobEffects.PARASITESPURIFY) == null)) {
                    player.removePotionEffect(SRPPotions.COTH_E);
                    player.removePotionEffect(SRPPotions.FEAR_E);
                    player.removePotionEffect(SRPPotions.BLEED_E);
                    player.removePotionEffect(SRPPotions.CORRO_E);
                    player.removePotionEffect(SRPPotions.VIRA_E);
                }
            }
        }
    }

    private static void broadcastDailyMessage(net.minecraft.server.MinecraftServer server) {
        int messageIndex = RANDOM.nextInt(DAILY_MESSAGE_COUNT);
        int forecastIndex = RANDOM.nextInt(FORECAST_MESSAGE_COUNT);
        Broadcasts.sendDailyTransmission(
                server,
                createForecastMessage(server.getWorld(0), forecastIndex),
                getWeatherMessage(server),
                new TextComponentTranslation("message.seasons.daily" + messageIndex),
                getSeasonalOutroMessage(server.getWorld(0)));
    }

    private static TextComponentTranslation getWeatherMessage(MinecraftServer server) {
        if (server.getWorld(0).isThundering()) {
            return new TextComponentTranslation("message.weather.isThunder",
                    Integer.valueOf(0),
                    Integer.valueOf(Math.max(0, server.getWorld(0).getWorldInfo().getThunderTime() / 20)));
        }
        if (server.getWorld(0).isRaining()) {
            return new TextComponentTranslation("message.weather.noThunder",
                    Integer.valueOf(0),
                    Integer.valueOf(Math.max(0, server.getWorld(0).getWorldInfo().getRainTime() / 20)));
        }
        return new TextComponentTranslation("message.weather.sunny");
    }

    public static TextComponentTranslation createSeasonDayMessage(net.minecraft.world.World world, String key) {
        long elapsedDays = getElapsedDays(world);
        return new TextComponentTranslation(key, new TextComponentTranslation(getSeasonTranslationKey(elapsedDays)),
                Integer.valueOf(getDayInSeason(elapsedDays)));
    }

    private static TextComponentTranslation createForecastMessage(net.minecraft.world.World world, int forecastIndex) {
        long elapsedDays = getElapsedDays(world);
        TextComponentTranslation season = new TextComponentTranslation(getSeasonTranslationKey(elapsedDays));
        Integer dayInSeason = Integer.valueOf(getDayInSeason(elapsedDays));
        if (forecastIndex == 3) {
            return new TextComponentTranslation("message.seasons.forecast3", dayInSeason, season);
        }
        return new TextComponentTranslation("message.seasons.forecast" + forecastIndex, season, dayInSeason);
    }

    public static String getSeasonTranslationKey(long elapsedDays) {
        int seasonIndex = getSeasonIndex(elapsedDays);
        switch (seasonIndex) {
            case 0:
                return "message.seasons.spring";
            case 1:
                return "message.seasons.summer";
            case 2:
                return "message.seasons.fall";
            case 3:
            default:
                return "message.seasons.winter";
        }
    }

    public static int getDayInSeason(long elapsedDays) {
        return (int) ((Math.max(1L, elapsedDays) - 1L) % SEASON_LENGTH_DAYS) + 1;
    }

    private static int getSeasonIndex(long elapsedDays) {
        return (int) (((Math.max(1L, elapsedDays) - 1L) / SEASON_LENGTH_DAYS) % 4L);
    }

    public static TextComponentTranslation getSeasonalOutroMessage(net.minecraft.world.World world) {
        long elapsedDays = getElapsedDays(world);
        switch (getSeasonIndex(elapsedDays)) {
            case 0:
                return new TextComponentTranslation("message.seasons.dailySpring" + RANDOM.nextInt(SPRING_OUTRO_COUNT));
            case 1:
                return new TextComponentTranslation("message.seasons.dailySummer" + RANDOM.nextInt(SUMMER_OUTRO_COUNT));
            case 2:
                return new TextComponentTranslation("message.seasons.dailyFall" + RANDOM.nextInt(FALL_OUTRO_COUNT));
            case 3:
            default:
                return new TextComponentTranslation("message.seasons.dailyWinter" + RANDOM.nextInt(WINTER_OUTRO_COUNT));
        }
    }

    private static long getElapsedDays(net.minecraft.world.World world) {
        SeasonCalendarData calendar = SeasonCalendarData.get(world);
        calendar.updateFromWorld(world);
        return calendar.getElapsedDays();
    }
}
