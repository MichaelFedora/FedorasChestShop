package io.github.michaelfedora.fedorasmarket.cmdexecutors.quickshop;

import io.github.michaelfedora.fedorasmarket.FedorasMarket;
import io.github.michaelfedora.fedorasmarket.PluginInfo;
import io.github.michaelfedora.fedorasmarket.cmdexecutors.FmExecutorBase;
import io.github.michaelfedora.fedorasmarket.cmdexecutors.FmHelpExecutor;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.List;

/**
 * Created by Michael on 3/13/2016.
 */
public class FmQuickShopHelpExecutor extends FmExecutorBase {

    public static final List<String> aliases = FmHelpExecutor.aliases;

    public static CommandSpec create() {
        return CommandSpec.builder()
                .arguments(GenericArguments.optional(GenericArguments.string(Text.of("cmd"))))
                .description(Text.of(FmHelpExecutor.desc))
                .extendedDescription(Text.of(FmHelpExecutor.exDesc))
                .permission(PluginInfo.DATA_ROOT + ".quickshop.help")
                .executor(new FmQuickShopHelpExecutor())
                .build();
    }

    @Override
    protected String getName() {
        return "quickshop help";
    }

    public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {

        FmHelpExecutor.helpFunc(src, ctx, FedorasMarket.getGrandChildCommands("quickshop").orElseThrow(makeExceptionSupplier("Can't find the subcommands :o")), "quickshop");

        return CommandResult.success();
    }

}