package com.overlast.handlers;

import com.overlast.OverLast;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

public class SeasonCalendarData extends WorldSavedData {

    private static final String DATA_NAME = OverLast.MOD_ID + "_season_calendar";
    private static final String TAG_INITIALIZED = "Initialized";
    private static final String TAG_ELAPSED_DAYS = "ElapsedDays";
    private static final String TAG_LAST_OBSERVED_WORLD_DAY = "LastObservedWorldDay";
    private static final String TAG_LAST_BROADCAST_DAY = "LastBroadcastDay";

    private boolean initialized;
    private long elapsedDays = 1L;
    private long lastObservedWorldDay;
    private long lastBroadcastDay;

    public SeasonCalendarData() {
        super(DATA_NAME);
    }

    public SeasonCalendarData(String name) {
        super(name);
    }

    public static SeasonCalendarData get(World world) {
        World overworld = world.getMinecraftServer() != null ? world.getMinecraftServer().getWorld(0) : world;
        MapStorage storage = overworld.getPerWorldStorage();
        SeasonCalendarData data = (SeasonCalendarData) storage.getOrLoadData(SeasonCalendarData.class, DATA_NAME);
        if (data == null) {
            data = new SeasonCalendarData();
            storage.setData(DATA_NAME, data);
        }
        return data;
    }

    public boolean updateFromWorld(World world) {
        long observedWorldDay = Math.max(0L, Math.floorDiv(world.getWorldTime(), 24000L));
        if (!this.initialized) {
            this.initialized = true;
            this.elapsedDays = Math.max(1L, observedWorldDay + 1L);
            this.lastObservedWorldDay = observedWorldDay;
            markDirty();
            return false;
        }

        if (observedWorldDay > this.lastObservedWorldDay) {
            this.elapsedDays += observedWorldDay - this.lastObservedWorldDay;
            this.lastObservedWorldDay = observedWorldDay;
            markDirty();
            return true;
        }

        if (observedWorldDay < this.lastObservedWorldDay) {
            this.lastObservedWorldDay = observedWorldDay;
            markDirty();
        }

        return false;
    }

    public long getElapsedDays() {
        return this.elapsedDays;
    }

    public boolean hasBroadcastForCurrentDay() {
        return this.lastBroadcastDay >= this.elapsedDays;
    }

    public void markBroadcastSent() {
        if (this.lastBroadcastDay != this.elapsedDays) {
            this.lastBroadcastDay = this.elapsedDays;
            markDirty();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.initialized = nbt.getBoolean(TAG_INITIALIZED);
        this.elapsedDays = Math.max(1L, nbt.getLong(TAG_ELAPSED_DAYS));
        this.lastObservedWorldDay = nbt.getLong(TAG_LAST_OBSERVED_WORLD_DAY);
        this.lastBroadcastDay = nbt.getLong(TAG_LAST_BROADCAST_DAY);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setBoolean(TAG_INITIALIZED, this.initialized);
        compound.setLong(TAG_ELAPSED_DAYS, this.elapsedDays);
        compound.setLong(TAG_LAST_OBSERVED_WORLD_DAY, this.lastObservedWorldDay);
        compound.setLong(TAG_LAST_BROADCAST_DAY, this.lastBroadcastDay);
        return compound;
    }
}
