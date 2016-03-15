package io.github.michaelfedora.fedorasmarket.cmdexecutors.tradeform;

import io.github.michaelfedora.fedorasmarket.PluginInfo;
import io.github.michaelfedora.fedorasmarket.cmdexecutors.FmExecutorBase;
import io.github.michaelfedora.fedorasmarket.database.DatabaseCategory;
import io.github.michaelfedora.fedorasmarket.database.DatabaseManager;
import io.github.michaelfedora.fedorasmarket.database.DatabaseQuery;
import io.github.michaelfedora.fedorasmarket.trade.PartyType;
import io.github.michaelfedora.fedorasmarket.trade.SerializedTradeForm;
import io.github.michaelfedora.fedorasmarket.trade.TradeForm;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.Text;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Michael on 2/25/2016.
 */
public class FmTradeFormRemoveItemExecutor extends FmExecutorBase {

    public static final List<String> aliases = Arrays.asList("removeitem", "remi");

    public static CommandSpec create() {
        return CommandSpec.builder()
                .description(Text.of("Remove an item entry from a trade form"))
                .permission(PluginInfo.DATA_ROOT + ".tradeform.removeitem")
                .arguments(
                        GenericArguments.string(Text.of("name")),
                        GenericArguments.enumValue(Text.of("party"), PartyType.class),
                        GenericArguments.catalogedElement(Text.of("item"), ItemType.class))
                .executor(new FmTradeFormRemoveItemExecutor())
                .build();
    }

    @Override
    protected String getName() {
        return "tradeform removeitem";
    }

    public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {

        if(!(src instanceof Player)) {
            throw makeSourceNotPlayerException();
        }

        Player player = (Player) src;

        String name = ctx.<String>getOne("name").orElseThrow(makeParamExceptionSupplier("name"));

        PartyType partyType = ctx.<PartyType>getOne("party").orElseThrow(makeParamExceptionSupplier("party"));

        ItemType itemType = ctx.<ItemType>getOne("item").orElseThrow(makeParamExceptionSupplier("item"));

        try(Connection conn = DatabaseManager.getConnection()) {

            ResultSet resultSet = DatabaseManager.select(conn, 1, player.getUniqueId(), DatabaseCategory.TRADEFORM, name);

            TradeForm tradeForm;
            if(resultSet.next()) {
                tradeForm = ((SerializedTradeForm) resultSet.getObject(DatabaseQuery.DATA.v)).safeDeserialize().get();

                switch(partyType) {
                    case OWNER:
                        tradeForm.setOwnerParty(tradeForm.getOwnerParty().removeItem(itemType));
                        break;
                    case CUSTOMER:
                        tradeForm.setCustomerParty(tradeForm.getCustomerParty().removeItem(itemType));
                        break;
                }

                DatabaseManager.update(conn, tradeForm.serialize(), player.getUniqueId(), DatabaseCategory.TRADEFORM, name);
            }

        } catch(SQLException e) {
            throw makeException("SQL Error", e, src);
        }

        return CommandResult.success();
    }
}
