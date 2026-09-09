package io.github.capsicum0907.caldarium.client;

import io.github.capsicum0907.caldarium.CaldariumRegistry;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Drawing is a client concern, and this is the only place that knows it exists. */
public final class CaldariumClient {
    private CaldariumClient() {
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(CaldariumRegistry.MACHINE_MENU.get(), MachineScreen::new);
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CaldariumRegistry.SOL_ENTITY.get(), SolRenderer::new);
    }
}
