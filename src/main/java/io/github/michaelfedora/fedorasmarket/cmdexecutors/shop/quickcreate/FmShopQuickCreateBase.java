package io.github.michaelfedora.fedorasmarket.cmdexecutors.shop.quickcreate;

import io.github.michaelfedora.fedorasmarket.FedorasMarket;
import io.github.michaelfedora.fedorasmarket.cmdexecutors.FmExecutorBase;
import io.github.michaelfedora.fedorasmarket.data.shopreference.ShopReferenceData;
import io.github.michaelfedora.fedorasmarket.data.shopreference.ShopReferenceDataManipulatorBuilder;
import io.github.michaelfedora.fedorasmarket.database.DatabaseManager;
import io.github.michaelfedora.fedorasmarket.enumtype.GoodType;
import io.github.michaelfedora.fedorasmarket.shop.*;
import io.github.michaelfedora.fedorasmarket.trade.TradeForm;
import io.github.michaelfedora.fedorasmarket.util.FmUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.Tuple;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Michael on 3/13/2016.
 */
public abstract class FmShopQuickCreateBase extends FmExecutorBase {

    public static Map<UUID, Tuple<String, TradeForm>> to_apply = new HashMap<>();
    public static Set<UUID> as_server = new HashSet<>();

    public void OnInteractSecondary(InteractBlockEvent.Secondary event, Player player) {

        event.setCancelled(true);

        UUID playerId = player.getUniqueId();
        String name = to_apply.get(playerId).getFirst();
        TradeForm tradeForm = to_apply.get(playerId).getSecond();
        to_apply.remove(playerId);
        boolean isServerOwned = as_server.contains(playerId);
        as_server.remove(playerId);


        BlockSnapshot blockSnapshot = event.getTargetBlock();
        if(blockSnapshot.getState().getType() != BlockTypes.WALL_SIGN) {
            error(player, "Bad block :c . I need a wall-sign!");
            return;
        }

        Sign sign;
        {
            Optional<Sign> opt_sign = FmUtil.getSignFromBlockSnapshot(blockSnapshot);
            if(!opt_sign.isPresent()) {
                error(player, "Bad block :c . I need a sign! (but should've already been checked?)");
                return;
            }
            sign = opt_sign.get();
        }

        ShopData shopData = new ShopData(tradeForm, ShopModifier.NONE, sign.getLocation(), (isServerOwned) ? Optional.empty() : Optional.of(playerId));
        try(Connection conn = DatabaseManager.getConnection()) {

            Shop shop = new Shop(sign, shopData);
            shop.initialize(conn);

        } catch(SQLException e) {
            throwSafeException("SQL Error", e, player);
            return;
        }
    }

}
