package com.overlast.command;

import com.overlast.util.Broadcasts;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class CommandOverLast extends CommandBase {

    private static final String TEST_SUBCOMMAND = "broadcasttest";
    private static final String TEST_COMMAND = "/overlast " + TEST_SUBCOMMAND;

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
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 1 || !TEST_SUBCOMMAND.equalsIgnoreCase(args[0])) {
            throw new WrongUsageException(getUsage(sender));
        }

        if (server.getPlayerList().getCurrentPlayerCount() <= 0) {
            throw new CommandException("command.overlast.broadcasttest.no_players");
        }

        ITextComponent footer = Broadcasts.createCommandFooter(
                new TextComponentTranslation("broadcast.overlast.test.footer"),
                TEST_COMMAND,
                new TextComponentTranslation("broadcast.overlast.test.hover"));
        Broadcasts.sendNews(
                server,
                new TextComponentTranslation("broadcast.overlast.test.title"),
                new TextComponentTranslation("broadcast.overlast.test.body"),
                footer);
        notifyCommandListener(sender, this, "command.overlast.broadcasttest.sent");
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
            @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, Collections.singletonList(TEST_SUBCOMMAND));
        }
        return Collections.emptyList();
    }
}
