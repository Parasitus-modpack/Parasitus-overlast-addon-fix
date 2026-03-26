package com.overlast.command;

import com.overlast.handlers.EventHandlerServer;
import com.overlast.util.Broadcasts;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class CommandOverLast extends CommandBase {

    private static final String TEST_SUBCOMMAND = "broadcasttest";
    private static final String INVASION_TEST_SUBCOMMAND = "invasiontest";
    private static final String MUTE_SUBCOMMAND = "mute";
    private static final String UNMUTE_SUBCOMMAND = "unmute";
    private static final String STATUS_SUBCOMMAND = "mutestatus";
    private static final String MUTE_UNTIL_NEXT_INVASION_SUBCOMMAND = "muteuntilnextinvasion";

    @Override
    public String getName() {
        return "overlast";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "command.overlast.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 1) {
            throw new WrongUsageException(getUsage(sender));
        }

        String subcommand = args[0].toLowerCase();
        if (TEST_SUBCOMMAND.equals(subcommand)) {
            executeBroadcastTest(server, sender);
            return;
        }

        if (INVASION_TEST_SUBCOMMAND.equals(subcommand)) {
            executeInvasionTest(server, sender);
            return;
        }

        if (MUTE_SUBCOMMAND.equals(subcommand)) {
            EntityPlayer player = getPlayerSender(sender);
            Broadcasts.setMuted(player, true);
            sender.sendMessage(new TextComponentTranslation("command.overlast.mute.enabled"));
            return;
        }

        if (UNMUTE_SUBCOMMAND.equals(subcommand)) {
            EntityPlayer player = getPlayerSender(sender);
            Broadcasts.setMuted(player, false);
            sender.sendMessage(new TextComponentTranslation("command.overlast.mute.disabled"));
            return;
        }

        if (STATUS_SUBCOMMAND.equals(subcommand)) {
            EntityPlayer player = getPlayerSender(sender);
            sender.sendMessage(createMuteStatusMessage(player));
            return;
        }

        if (MUTE_UNTIL_NEXT_INVASION_SUBCOMMAND.equals(subcommand)) {
            EntityPlayer player = getPlayerSender(sender);
            Broadcasts.setDailyMutedUntilInvasion(player, true);
            sender.sendMessage(new TextComponentTranslation("command.overlast.mute_until_next_invasion.enabled"));
            return;
        }

        throw new WrongUsageException(getUsage(sender));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
            @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            Set<String> subcommands = new LinkedHashSet<String>();
            subcommands.add(MUTE_SUBCOMMAND);
            subcommands.add(UNMUTE_SUBCOMMAND);
            subcommands.add(STATUS_SUBCOMMAND);
            subcommands.add(MUTE_UNTIL_NEXT_INVASION_SUBCOMMAND);
            if (sender.canUseCommand(2, getName())) {
                subcommands.add(TEST_SUBCOMMAND);
                subcommands.add(INVASION_TEST_SUBCOMMAND);
            }
            return getListOfStringsMatchingLastWord(args, subcommands);
        }
        return Collections.emptyList();
    }

    private void executeBroadcastTest(MinecraftServer server, ICommandSender sender) throws CommandException {
        if (!sender.canUseCommand(2, getName())) {
            throw new CommandException("commands.generic.permission");
        }
        if (server.getPlayerList().getCurrentPlayerCount() <= 0) {
            throw new CommandException("command.overlast.broadcasttest.no_players");
        }

        Broadcasts.sendTransmission(
                server,
                new TextComponentTranslation("broadcast.overlast.test.intro"),
                new TextComponentTranslation("broadcast.overlast.test.weather"),
                new TextComponentTranslation("broadcast.overlast.test.body"),
            EventHandlerServer.getSeasonalOutroMessage(server.getWorld(0)));
    }

    private void executeInvasionTest(MinecraftServer server, ICommandSender sender) throws CommandException {
        if (!sender.canUseCommand(2, getName())) {
            throw new CommandException("commands.generic.permission");
        }
        if (server.getPlayerList().getCurrentPlayerCount() <= 0) {
            throw new CommandException("command.overlast.broadcasttest.no_players");
        }

        Broadcasts.sendInvasionTransmission(
                server,
                new TextComponentTranslation("broadcast.overlast.invasion.intro"),
                new TextComponentTranslation("broadcast.overlast.invasion.weather"),
                new TextComponentTranslation("broadcast.overlast.invasion.body"),
            EventHandlerServer.getSeasonalOutroMessage(server.getWorld(0)));
    }

    private EntityPlayer getPlayerSender(ICommandSender sender) throws CommandException {
        Entity senderEntity = sender.getCommandSenderEntity();
        if (!(senderEntity instanceof EntityPlayer)) {
            throw new CommandException("command.overlast.player_only");
        }
        return (EntityPlayer) senderEntity;
    }

    private TextComponentString createMuteStatusMessage(EntityPlayer player) {
        TextComponentString line = new TextComponentString("");
        line.appendSibling(styled("Radio Status", TextFormatting.GOLD, true));
        line.appendSibling(styled(" - ", TextFormatting.DARK_GRAY, false));
        if (Broadcasts.isMuted(player)) {
            line.appendSibling(styled("Muted", TextFormatting.RED, true));
        } else if (Broadcasts.isDailyMutedUntilInvasion(player)) {
            line.appendSibling(styled("Muted Until Next Invasion", TextFormatting.YELLOW, true));
        } else {
            line.appendSibling(styled("Active", TextFormatting.GREEN, true));
        }
        return line;
    }

    private TextComponentString styled(String text, TextFormatting color, boolean bold) {
        return (TextComponentString) new TextComponentString(text)
                .setStyle(new Style().setColor(color).setBold(Boolean.valueOf(bold)));
    }
}
