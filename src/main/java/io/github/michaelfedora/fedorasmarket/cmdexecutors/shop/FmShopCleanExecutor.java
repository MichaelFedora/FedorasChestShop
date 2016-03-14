package io.github.michaelfedora.fedorasmarket.cmdexecutors.shop;

import io.github.michaelfedora.fedorasmarket.cmdexecutors.FmExecutorBase;
import io.github.michaelfedora.fedorasmarket.data.FmDataKeys;
import io.github.michaelfedora.fedorasmarket.database.BadDataException;
import io.github.michaelfedora.fedorasmarket.database.DatabaseManager;
import io.github.michaelfedora.fedorasmarket.shop.SerializedShopData;
import io.github.michaelfedora.fedorasmarket.shop.Shop;
import io.github.michaelfedora.fedorasmarket.shop.ShopData;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Michael on 3/3/2016.
 */
public class FmShopCleanExecutor extends FmExecutorBase {
    @Override
    protected String getName() {
        return "shop clean";
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {

        if(!(src instanceof Player)) {
            throw sourceNotPlayerException;
        }

        UUID uuid = ((Player) src).getUniqueId();

        boolean failed = false;
        try(Connection conn = DatabaseManager.getConnection()) {

            ResultSet resultSet = DatabaseManager.shopDataDB.select(conn, uuid);

            while(resultSet.next()) {
                //msg(src, "Looking at shop [" + resultSet.getString("name") + "::" + resultSet.getObject("instance") + "]");
                String name = resultSet.getString("name");
                UUID instance = (UUID) resultSet.getObject("instance");
                try {

                    String prefix = "[" + name + "::" + instance + "]" + ": ";
                    ShopData shopData = ((SerializedShopData) resultSet.getObject("data")).deserialize();
                    Optional<TileEntity> opt_te = shopData.location.getTileEntity();
                    Sign sign;
                    Shop shop;
                    if(!opt_te.isPresent()) {
                        failed = true;
                        msg(src, prefix + "Location does not have a tile entity!");
                    } else if(!(opt_te.get() instanceof Sign)) {
                        failed = true;
                        msg(src, prefix + "Tile Entity is not a Sign!");
                    } else if(!((sign = (Sign) opt_te.get()).get(FmDataKeys.SHOP_REFERENCE).isPresent())) {
                        failed = true;
                        msg(src, prefix + "Sign does not support the data key!");
                    } else if(!(sign.get(FmDataKeys.SHOP_REFERENCE).get().instance.equals(instance))) {
                        failed = true;
                        msg(src, prefix + "Instances do not match up!");
                    } else if(!(Shop.fromSign(sign).isPresent())) {
                        failed = true;
                        msg(src, prefix + "Bad data key entry!");
                    } else
                        msg(src, prefix + "Good!");
                } catch (BadDataException e) {
                    failed = true;
                    msg(src, "Bad database entry! Deleting & Refreshing...");
                }

                if(failed) {
                    failed = false;

                    DatabaseManager.shopDataDB.delete(conn, uuid, name, instance);
                    DatabaseManager.shopDataDB.select(conn, uuid);
                }
            }

        } catch (SQLException e) {
            throw makeException("SQL Error", e, src);
        }

        return CommandResult.success();
    }
}