package io.github.michaelfedora.fedorasmarket.cmdexecutors.shop;

import io.github.michaelfedora.fedorasmarket.FedorasMarket;
import io.github.michaelfedora.fedorasmarket.PluginInfo;
import io.github.michaelfedora.fedorasmarket.cmdexecutors.FmExecutor;
import io.github.michaelfedora.fedorasmarket.cmdexecutors.FmExecutorBase;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.*;

/**
 * Created by Michael on 2/29/2016.
 */
public class FmShopExecutor extends FmExecutorBase {

    public static List<String> aliases = Arrays.asList("shop", "sh");

    public static CommandSpec create(Map<List<String>, ? extends CommandCallable> children) {
        return CommandSpec.builder()
                .description(Text.of("Do shop things (lists sub commands)"))
                .permission(PluginInfo.DATA_ROOT + ".shop")
                .executor(new FmShopExecutor())
                .children(children)
                .build();
    }

    @Override
    protected String getName() {
        return "shop";
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {

        FmExecutor.listSubCommandsFunc(src, FedorasMarket.getGrandChildCommands("shop").orElseThrow(makeExceptionSupplier("Can't find subcommands?!")), "shop");

        return CommandResult.success();
    }
}
