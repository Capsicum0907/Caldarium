package io.github.capsicum0907.caldarium.fabric;

import io.github.capsicum0907.caldarium.MachineMenu;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
//? if >=1.17
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
//? if >=1.20.5 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
//?} else
//import net.minecraft.network.FriendlyByteBuf;

public final class Menus {
    private Menus() {
    }

    //? if <1.17
    /*@com.github.bsideup.jabel.Desugar*/
    public record Opening(boolean burns, int slots, boolean pours) {
        //? if >=1.20.5 {
        static final StreamCodec<RegistryFriendlyByteBuf, Opening> CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, Opening::burns,
                ByteBufCodecs.VAR_INT, Opening::slots,
                ByteBufCodecs.BOOL, Opening::pours,
                Opening::new);
        //?}
    }

    public static MenuType<MachineMenu> type() {
        //? if >=1.20.5 {
        return new ExtendedScreenHandlerType<>(
                (id, inventory, opening) -> new MachineMenu(id, inventory, opening.burns(), opening.slots(),
                        opening.pours()),
                Opening.CODEC);
        //?} else if >=1.17 {
        /*return new ExtendedScreenHandlerType<>(MachineMenu::new);
        *///?} else
        /*return new net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType<>(MachineMenu::new);*/
    }

    public static void open(Player player, MenuProvider provider, boolean burns, int slots, boolean pours) {
        Opening opening = new Opening(burns, slots, pours);
        //? if >=1.20.5 {
        player.openMenu(new ExtendedScreenHandlerFactory<Opening>() {
            @Override
            public Opening getScreenOpeningData(ServerPlayer opener) {
                return opening;
            }
        //?} else {
        /*player.openMenu(new ExtendedScreenHandlerFactory() {
            @Override
            public void writeScreenOpeningData(ServerPlayer opener, FriendlyByteBuf buffer) {
                buffer.writeBoolean(opening.burns());
                buffer.writeByte(opening.slots());
                buffer.writeBoolean(opening.pours());
            }
        *///?}

            @Override
            public Component getDisplayName() {
                return provider.getDisplayName();
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player user) {
                return provider.createMenu(id, inventory, user);
            }
        });
    }
}
