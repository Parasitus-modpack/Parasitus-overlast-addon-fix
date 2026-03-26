package com.overlast.command;

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
import net.minecraft.util.text.TextComponentTranslation;

public class CommandOverLast extends CommandBase {

    private static final String TEST_SUBCOMMAND = "broadcasttest";
    private static final String MUTE_SUBCOMMAND = "mute";
    private static final String UNMUTE_SUBCOMMAND = "unmute";
    private static final String STATUS_SUBCOMMAND = "mutestatus";

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
            sender.sendMessage(new TextComponentTranslation(
                    Broadcasts.isMuted(player) ? "command.overlast.mute.status.on" : "command.overlast.mute.status.off"));
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
            if (sender.canUseCommand(2, getName())) {
                subcommands.add(TEST_SUBCOMMAND);
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
                new TextComponentTranslation("broadcast.overlast.test.outro"));
    }

    private EntityPlayer getPlayerSender(ICommandSender sender) throws CommandException {
        Entity senderEntity = sender.getCommandSenderEntity();
        if (!(senderEntity instanceof EntityPlayer)) {
            throw new CommandException("command.overlast.player_only");
        }
        return (EntityPlayer) senderEntity;
    }
}
