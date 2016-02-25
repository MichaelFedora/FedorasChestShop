package io.github.michaelfedora.fedorasmarket.cmdexecutors;

import com.google.common.collect.Lists;
import io.github.michaelfedora.fedorasmarket.FedorasMarket;
import io.github.michaelfedora.fedorasmarket.PluginInfo;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.service.pagination.PaginationBuilder;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashMap;
import java.util.List;

/**
 * Inspired by Polis (though heavily edited)
 */
public class FmHelpExecutor implements CommandExecutor {
    public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
        HashMap<List<String>,CommandSpec> fmSubCommands = FedorasMarket.getSubCommands();
        List<Text> helpList = Lists.newArrayList();

        for(List<String> aliases : fmSubCommands.keySet()) {
            CommandSpec commandSpec = fmSubCommands.get(aliases);
            Text commandHelp = Text.builder()
                    .append(Text.builder()
                            .append(Text.of(TextColors.AQUA, aliases, ": "))
                            .append(Text.of(TextColors.BLUE,
                                    commandSpec.getUsage(src), "\n"))
                            .append(Text.of(TextColors.GRAY, "    ",
                                    commandSpec.getShortDescription(src).get(), "\n"))
                            .append(Text.of(TextColors.AQUA, "Perm: ",
                                    (commandSpec.testPermission(src)) ? TextColors.GREEN : TextColors.RED,
                                    commandSpec.toString().substring(commandSpec.toString().lastIndexOf("permission") + 11,
                                            commandSpec.toString().indexOf("argumentParser") - 2)))
                            .build())
                    .build();
            helpList.add(commandHelp);
        }

        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationBuilder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GOLD, PluginInfo.NAME + " Help")).paddingString("=").contents(helpList);
        paginationBuilder.sendTo(src);
        return CommandResult.success();
    }
}
