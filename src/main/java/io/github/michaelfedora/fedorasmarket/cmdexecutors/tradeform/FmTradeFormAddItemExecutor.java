package io.github.michaelfedora.fedorasmarket.cmdexecutors.tradeform;

import io.github.michaelfedora.fedorasmarket.PluginInfo;
import io.github.michaelfedora.fedorasmarket.cmdexecutors.FmExecutorBase;
import io.github.michaelfedora.fedorasmarket.database.DatabaseManager;
import io.github.michaelfedora.fedorasmarket.enumtype.PartyType;
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
public class FmTradeFormAddItemExecutor extends FmExecutorBase {

    public static final List<String> aliases = Arrays.asList("additem", "addi");

    public static CommandSpec create() {
        return CommandSpec.builder()
                .description(Text.of("Add an item amount to a trade form"))
                .permission(PluginInfo.DATA_ROOT + ".tradeform.additem")
                .arguments(
                        GenericArguments.string(Text.of("name")),
                        GenericArguments.enumValue(Text.of("party"), PartyType.class),
                        GenericArguments.catalogedElement(Text.of("item"), ItemType.class),
                        GenericArguments.integer(Text.of("amount")))
                .executor(new FmTradeFormAddItemExecutor())
                .build();
    }

    @Override
    protected String getName() {
        return "tradeform additem";
    }

    public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {

        if(!(src instanceof Player)) {
            throw sourceNotPlayerException;
        }

        Player player = (Player) src;

        String name = ctx.<String>getOne("name").orElseThrow(makeParamExceptionSupplier("name"));

        PartyType partyType = ctx.<PartyType>getOne("party").orElseThrow(makeParamExceptionSupplier("party"));

        ItemType itemType = ctx.<ItemType>getOne("item").orElseThrow(makeParamExceptionSupplier("item"));

        int amount = ctx.<Integer>getOne("amount").orElseThrow(makeParamExceptionSupplier("amount"));

        boolean success = false;
        try(Connection conn = DatabaseManager.getConnection()) {

            ResultSet resultSet = DatabaseManager.tradeFormDB.selectWithMore(conn, player.getUniqueId(), name, "LIMIT 1");

            TradeForm tradeForm;
            if(resultSet.next()) {
                tradeForm = ((SerializedTradeForm) resultSet.getObject("data")).safeDeserialize().get();

                int old_amt;
                switch(partyType) {
                    case OWNER:
                        old_amt = tradeForm.getOwnerParty().items.getOrDefault(itemType, 0);
                        tradeForm.setOwnerParty(tradeForm.getOwnerParty().addItem(itemType, amount));
                        success = tradeForm.getOwnerParty().items.containsKey(itemType);
                        if(success)
                            success = (old_amt + amount == tradeForm.getOwnerParty().items.get(itemType));
                        break;
                    case CUSTOMER:
                        old_amt = tradeForm.getCustomerParty().items.getOrDefault(itemType, 0);
                        tradeForm.setCustomerParty(tradeForm.getCustomerParty().addItem(itemType, amount));
                        success = tradeForm.getCustomerParty().items.containsKey(itemType);
                        if(success)
                            success = (old_amt + amount == tradeForm.getCustomerParty().items.get(itemType));
                        break;
                }

                DatabaseManager.tradeFormDB.update(conn, tradeForm.serialize(), player.getUniqueId(), name);
            }

        } catch(SQLException e) {
            throw makeException("SQL Error", e, src);
        }

        if(success)
            msgf(src, Text.of("Added ", amount, " item(s) of type ", itemType.getName(), " to the transaction [", name, "]!"));
        else
            msgf(src, Text.of("Failed to add ", amount, " item(s) of type ", itemType.getName(), " to the transaction [", name, "]!"));

        return CommandResult.success();
    }
}
