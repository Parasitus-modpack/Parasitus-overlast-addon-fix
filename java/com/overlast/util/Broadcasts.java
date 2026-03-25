package com.overlast.util;

import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public final class Broadcasts {

    private Broadcasts() {
    }

    public static void sendNews(MinecraftServer server, ITextComponent title, ITextComponent body, ITextComponent footer) {
        List<EntityPlayerMP> players = server.getPlayerList().getPlayers();
        for (EntityPlayerMP player : players) {
            player.sendMessage(createHeader());
            player.sendMessage(createBody(title, body));
            if (footer != null) {
                player.sendMessage(footer.createCopy());
            }
        }
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
        header.appendSibling(styled("[ OVERLAST NEWS ]", TextFormatting.GOLD, true, false));
        header.appendSibling(styled(" ==========", TextFormatting.DARK_GRAY, false, false));
        return header;
    }

    private static ITextComponent createBody(ITextComponent title, ITextComponent body) {
        TextComponentString line = new TextComponentString("");
        line.appendSibling(styled("> ", TextFormatting.RED, true, false));
        line.appendSibling(title.createCopy().setStyle(new Style().setColor(TextFormatting.RED).setBold(true)));
        line.appendSibling(styled("  ", TextFormatting.WHITE, false, false));
        line.appendSibling(body.createCopy().setStyle(new Style().setColor(TextFormatting.GRAY)));
        return line;
    }

    private static TextComponentString styled(String text, TextFormatting color, boolean bold, boolean italic) {
        return (TextComponentString) new TextComponentString(text)
                .setStyle(new Style().setColor(color).setBold(bold).setItalic(italic));
    }
}
