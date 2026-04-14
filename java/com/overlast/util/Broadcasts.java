package com.overlast.util;

import java.util.List;
import java.util.Objects;

import com.overlast.config.OverConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public final class Broadcasts {

    private static final String PLAYER_PERSISTED_TAG = "PlayerPersisted";
    private static final String MUTED_BROADCASTS_TAG = "overlastMutedBroadcasts";
    private static final String DAILY_MUTED_UNTIL_INVASION_TAG = "overlastDailyMutedUntilInvasion";

    private Broadcasts() {
    }

    public static void sendTransmission(MinecraftServer server, ITextComponent intro, ITextComponent weather,
            ITextComponent main, ITextComponent outro) {
        List<EntityPlayerMP> players = server.getPlayerList().getPlayers();
        for (EntityPlayerMP player : players) {
            if (isMuted(player)) {
                continue;
            }
            player.sendMessage(createHeader());
            if (intro != null) {
                player.sendMessage(createLine(intro, getIntroColor(), false));
            }
            if (weather != null) {
                player.sendMessage(createLine(weather, getWeatherColor(), true));
            }
            if (main != null) {
                player.sendMessage(createLine(main, getMainColor(), false));
            }
            if (outro != null) {
                player.sendMessage(createLine(outro, getOutroColor(), false, false));
            }
        }
    }

    public static void sendDailyTransmission(MinecraftServer server, ITextComponent intro, ITextComponent weather,
            ITextComponent main, ITextComponent outro) {
        List<EntityPlayerMP> players = server.getPlayerList().getPlayers();
        for (EntityPlayerMP player : players) {
            if (isMuted(player) || isDailyMutedUntilInvasion(player)) {
                continue;
            }
            player.sendMessage(createHeader());
            if (intro != null) {
                player.sendMessage(createLine(intro, getIntroColor(), false));
            }
            if (weather != null) {
                player.sendMessage(createLine(weather, getWeatherColor(), true));
            }
            if (main != null) {
                player.sendMessage(createLine(main, getMainColor(), false));
            }
            if (outro != null) {
                player.sendMessage(createLine(outro, getOutroColor(), false, false));
            }
        }
    }

    public static void sendInvasionTransmission(MinecraftServer server, ITextComponent intro, ITextComponent weather,
            ITextComponent main, ITextComponent outro) {
        List<EntityPlayerMP> players = server.getPlayerList().getPlayers();
        for (EntityPlayerMP player : players) {
            if (isMuted(player)) {
                continue;
            }
            if (isDailyMutedUntilInvasion(player)) {
                setDailyMutedUntilInvasion(player, false);
            }
            player.sendMessage(createHeader());
            if (intro != null) {
                player.sendMessage(createLine(intro, getIntroColor(), false));
            }
            if (weather != null) {
                player.sendMessage(createLine(weather, getWeatherColor(), true));
            }
            if (main != null) {
                player.sendMessage(createLine(main, getMainColor(), false));
            }
            if (outro != null) {
                player.sendMessage(createLine(outro, getOutroColor(), false, false));
            }
        }
    }

    public static boolean isMuted(EntityPlayer player) {
        NBTTagCompound persistedData = getPersistedData(player, false);
        return persistedData != null && persistedData.getBoolean(MUTED_BROADCASTS_TAG);
    }

    public static void setMuted(EntityPlayer player, boolean muted) {
        Objects.requireNonNull(getPersistedData(player, true)).setBoolean(MUTED_BROADCASTS_TAG, muted);
    }

    public static boolean isDailyMutedUntilInvasion(EntityPlayer player) {
        NBTTagCompound persistedData = getPersistedData(player, false);
        return persistedData != null && persistedData.getBoolean(DAILY_MUTED_UNTIL_INVASION_TAG);
    }

    public static void setDailyMutedUntilInvasion(EntityPlayer player, boolean muted) {
        Objects.requireNonNull(getPersistedData(player, true)).setBoolean(DAILY_MUTED_UNTIL_INVASION_TAG, muted);
    }

    public static ITextComponent createCommandFooter(ITextComponent text, String command, ITextComponent hoverText) {
        return text.createCopy().setStyle(new Style()
                .setColor(TextFormatting.YELLOW)
                .setItalic(true)
                .setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.createCopy())));
    }

    private static ITextComponent createHeader() {
        TextComponentString header = new TextComponentString("");
        header.appendSibling(styled("========== ", getHeaderFrameColor(), false));
        header.appendSibling(styled("[Incoming Transmission]", getHeaderTitleColor(), true));
        header.appendSibling(styled(" ==========", getHeaderFrameColor(), false));
        return header;
    }

    private static ITextComponent createLine(ITextComponent text, TextFormatting color, boolean italic) {
        return createLine(text, color, italic, true);
    }

    private static ITextComponent createLine(ITextComponent text, TextFormatting color, boolean italic, boolean showPrefix) {
        TextComponentString line = new TextComponentString("");
        if (showPrefix) {
            line.appendSibling(styled("> ", getLinePrefixColor(), false));
        }
        line.appendSibling(text.createCopy().setStyle(new Style().setColor(color).setBold(false).setItalic(italic)));
        return line;
    }

    private static TextFormatting getHeaderFrameColor() {
        return resolveColor(OverConfig.BROADCAST.headerFrameColor, TextFormatting.GOLD);
    }

    private static TextFormatting getHeaderTitleColor() {
        return resolveColor(OverConfig.BROADCAST.headerTitleColor, TextFormatting.GOLD);
    }

    private static TextFormatting getLinePrefixColor() {
        return resolveColor(OverConfig.BROADCAST.linePrefixColor, TextFormatting.DARK_GRAY);
    }

    private static TextFormatting getIntroColor() {
        return resolveColor(OverConfig.BROADCAST.introColor, TextFormatting.BLUE);
    }

    private static TextFormatting getWeatherColor() {
        return resolveColor(OverConfig.BROADCAST.weatherColor, TextFormatting.GRAY);
    }

    private static TextFormatting getMainColor() {
        return resolveColor(OverConfig.BROADCAST.mainColor, TextFormatting.YELLOW);
    }

    private static TextFormatting getOutroColor() {
        return resolveColor(OverConfig.BROADCAST.outroColor, TextFormatting.YELLOW);
    }

    private static TextFormatting resolveColor(String configuredColor, TextFormatting fallback) {
        if (configuredColor == null || configuredColor.trim().isEmpty()) {
            return fallback;
        }

        String normalized = normalizeColorName(configuredColor);
        for (TextFormatting formatting : TextFormatting.values()) {
            if (!formatting.isColor()) {
                continue;
            }
            if (normalizeColorName(formatting.getFriendlyName()).equals(normalized)
                    || normalizeColorName(formatting.name()).equals(normalized)) {
                return formatting;
            }
        }

        return fallback;
    }

    private static String normalizeColorName(String value) {
        return value == null ? "" : value.trim().toLowerCase().replace("_", "").replace("-", "").replace(" ", "");
    }

    private static TextComponentString styled(String text, TextFormatting color, boolean bold) {
        return (TextComponentString) new TextComponentString(text)
                .setStyle(new Style().setColor(color).setBold(bold).setItalic(false));
    }

    private static NBTTagCompound getPersistedData(EntityPlayer player, boolean create) {
        NBTTagCompound entityData = player.getEntityData();
        if (!entityData.hasKey(PLAYER_PERSISTED_TAG)) {
            if (!create) {
                return null;
            }
            entityData.setTag(PLAYER_PERSISTED_TAG, new NBTTagCompound());
        }
        return entityData.getCompoundTag(PLAYER_PERSISTED_TAG);
    }
}
