package io.github.michaelfedora.fedorasmarket.cmdexecutors;

import io.github.michaelfedora.fedorasmarket.FedorasMarket;
import io.github.michaelfedora.fedorasmarket.database.DatabaseManager;
import io.github.michaelfedora.fedorasmarket.enumtype.PartyType;
import io.github.michaelfedora.fedorasmarket.trade.TradeForm;
import io.github.michaelfedora.fedorasmarket.util.FmUtil;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.economy.Currency;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Created by Michael on 2/25/2016.
 */
public class FmTradeFormSetCurrencyExecutor implements CommandExecutor {

    public CommandResult error(CommandSource src) {

        src.sendMessage(FmUtil.makeMessageError("Bad params, try again!"));

        return CommandResult.empty();
    }

    public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {

        if(!(src instanceof Player)) {
            return FmTradeFormExecutor.errorNotPlayer(src);
        }

        Player player = (Player) src;

        String name;
        {
            Optional<String> opt_name = ctx.<String>getOne("name");
            if (!opt_name.isPresent())
                return error(src);
            name = opt_name.get();
        }

        PartyType partyType;
        {
            Optional<PartyType> opt_partyType = ctx.<PartyType>getOne("party");
            if (!opt_partyType.isPresent())
                return error(src);
            partyType = opt_partyType.get();
        }

        double amount;
        {
            Optional<Double> opt_amount = ctx.<Double>getOne("amount");
            if (!opt_amount.isPresent())
                return error(src);
            amount = opt_amount.get();
        }

        Currency currency;
        {
            Optional<Currency> opt_currency = ctx.<Currency>getOne("currency");
            if (opt_currency.isPresent())
                currency = opt_currency.get();
            else
                currency = FedorasMarket.getEconomyService().getDefaultCurrency();
        }

        try {

            ResultSet resultSet = DatabaseManager.tradeForms.selectWithMore(player.getUniqueId(), name, "LIMIT 1");

            TradeForm tradeForm;
            if(resultSet.next()) {
                tradeForm = ((TradeForm.Data) resultSet.getObject("data")).deserialize();

                switch(partyType) {
                    case OWNER:
                        tradeForm.setOwnerParty(tradeForm.getOwnerParty().setCurrency(currency, BigDecimal.valueOf(amount)));
                        break;
                    case CUSTOMER:
                        tradeForm.setCustomerParty(tradeForm.getCustomerParty().setCurrency(currency, BigDecimal.valueOf(amount)));
                        break;
                }

                DatabaseManager.tradeForms.update(tradeForm.toData(), player.getUniqueId(), name);
            }

        } catch(SQLException e) {
            FedorasMarket.getLogger().error("SQL Error: ", this, e);
            src.sendMessage(FmUtil.makeMessageError("SQL ERROR: See console :c"));
            return CommandResult.empty();
        }


        return CommandResult.success();
    }
}