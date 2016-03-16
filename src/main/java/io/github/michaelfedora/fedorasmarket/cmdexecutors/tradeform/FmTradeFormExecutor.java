package io.github.michaelfedora.fedorasmarket.cmdexecutors.tradeform;

import io.github.michaelfedora.fedorasmarket.FedorasMarket;
import io.github.michaelfedora.fedorasmarket.PluginInfo;
import io.github.michaelfedora.fedorasmarket.cmdexecutors.FmExecutor;
import io.github.michaelfedora.fedorasmarket.cmdexecutors.FmExecutorBase;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.*;

/**
 * Created by Michael on 2/23/2016.
 */
public class FmTradeFormExecutor extends FmExecutorBase {

    public static final List<String> ALIASES = Arrays.asList("tradeform", "tf");

    public static final String NAME = FmTradeFormExecutor.NAME + ' ' + ALIASES.get(0);
    public static final String PERM = FmExecutor.PERM + '.' + ALIASES.get(0);

    public static CommandSpec create(HashMap<List<String>, CommandSpec> children) {
        return CommandSpec.builder()
                .description(Text.of("Do tradeform things (lists sub commands)"))
                .permission(PERM)
                .children(children)
                .executor(new FmTradeFormExecutor())
                .build();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {

        FmExecutor.listSubCommandsFunc(src, FedorasMarket.getGrandChildCommands(NAME).orElseThrow(makeExceptionSupplier("Can't find subcommands?!")), NAME);

        return CommandResult.success();
    }
}
