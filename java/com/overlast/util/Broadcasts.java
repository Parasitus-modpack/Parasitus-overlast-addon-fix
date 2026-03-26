package com.overlast.util;

import java.util.List;
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
                player.sendMessage(createLine(intro, TextFormatting.GRAY, false, false));
            }
            if (weather != null) {
                player.sendMessage(createLine(weather, TextFormatting.AQUA, false, false));
            }
            if (main != null) {
                player.sendMessage(createLine(main, TextFormatting.WHITE, false, false));
            }
            if (outro != null) {
                player.sendMessage(createLine(outro, TextFormatting.YELLOW, false, true));
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
                player.sendMessage(createLine(intro, TextFormatting.GRAY, false, false));
            }
            if (weather != null) {
                player.sendMessage(createLine(weather, TextFormatting.AQUA, false, false));
            }
            if (main != null) {
                player.sendMessage(createLine(main, TextFormatting.WHITE, false, false));
            }
            if (outro != null) {
                player.sendMessage(createLine(outro, TextFormatting.YELLOW, false, true));
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
                player.sendMessage(createLine(intro, TextFormatting.GRAY, false, false));
            }
            if (weather != null) {
                player.sendMessage(createLine(weather, TextFormatting.AQUA, false, false));
            }
            if (main != null) {
                player.sendMessage(createLine(main, TextFormatting.WHITE, false, false));
            }
            if (outro != null) {
                player.sendMessage(createLine(outro, TextFormatting.YELLOW, false, true));
            }
        }
    }

    public static boolean isMuted(EntityPlayer player) {
        NBTTagCompound persistedData = getPersistedData(player, false);
        return persistedData != null && persistedData.getBoolean(MUTED_BROADCASTS_TAG);
    }

    public static void setMuted(EntityPlayer player, boolean muted) {
        getPersistedData(player, true).setBoolean(MUTED_BROADCASTS_TAG, muted);
    }

    public static boolean isDailyMutedUntilInvasion(EntityPlayer player) {
        NBTTagCompound persistedData = getPersistedData(player, false);
        return persistedData != null && persistedData.getBoolean(DAILY_MUTED_UNTIL_INVASION_TAG);
    }

    public static void setDailyMutedUntilInvasion(EntityPlayer player, boolean muted) {
        getPersistedData(player, true).setBoolean(DAILY_MUTED_UNTIL_INVASION_TAG, muted);
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
        header.appendSibling(styled("========== ", TextFormatting.DARK_GRAY, false, false));
        header.appendSibling(styled("[Incoming Transmission]", TextFormatting.GOLD, true, false));
        header.appendSibling(styled(" ==========", TextFormatting.DARK_GRAY, false, false));
        return header;
    }

    private static ITextComponent createLine(ITextComponent text, TextFormatting color, boolean bold, boolean italic) {
        TextComponentString line = new TextComponentString("");
        line.appendSibling(styled("> ", TextFormatting.DARK_GRAY, false, false));
        line.appendSibling(text.createCopy().setStyle(new Style().setColor(color).setBold(bold).setItalic(italic)));
        return line;
    }

    private static TextComponentString styled(String text, TextFormatting color, boolean bold, boolean italic) {
        return (TextComponentString) new TextComponentString(text)
                .setStyle(new Style().setColor(color).setBold(bold).setItalic(italic));
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
