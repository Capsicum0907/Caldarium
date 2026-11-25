plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.21.1-neoforge"

stonecutter parameters {
    constants.match(node.metadata.project.substringAfterLast('-'), "fabric", "forge", "neoforge")

    replacements.string(node.metadata.project.endsWith("-forge")) {
        replace("import net.neoforged.neoforge.energy.", "import net.minecraftforge.energy.")
        replace("import net.neoforged.neoforge.items.", "import net.minecraftforge.items.")
        replace("import net.neoforged.neoforge.fluids.", "import net.minecraftforge.fluids.")
        replace("import net.neoforged.api.distmarker.", "import net.minecraftforge.api.distmarker.")
        replace("import net.neoforged.neoforge.client.event.", "import net.minecraftforge.client.event.")
        replace("import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;", "import net.minecraftforge.client.extensions.common.IClientItemExtensions;")
        replace("import net.neoforged.neoforge.common.ModConfigSpec;", "import net.minecraftforge.common.ForgeConfigSpec;")
        replace("ModConfigSpec", "ForgeConfigSpec")
    }
    replacements.string(node.metadata.project.endsWith("-forge") && eval(node.metadata.version, "<1.19")) {
        replace("import net.minecraftforge.common.capabilities.ForgeCapabilities;", "import net.minecraftforge.energy.CapabilityEnergy;")
        replace("ForgeCapabilities.ENERGY", "CapabilityEnergy.ENERGY")
        replace("ForgeCapabilities.ITEM_HANDLER", "net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY")
        replace("ForgeCapabilities.FLUID_HANDLER", "net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY")
        replace("RenderHighlightEvent.Block", "DrawSelectionEvent.HighlightBlock")
        replace("intoSun(event.getLevel(), event.getEntity(),", "intoSun(event.getWorld(), event.getPlayer(),")
        replace("event.getLevel() instanceof net.minecraft.world.level.Level level", "event.getWorld() instanceof net.minecraft.world.level.Level level")
    }
    replacements.string(eval(node.metadata.version, "<1.17")) {
        replace("List.of(", "com.google.common.collect.ImmutableList.of(")
        replace("List.copyOf(", "com.google.common.collect.ImmutableList.copyOf(")
        replace("Map.entry(", "new java.util.AbstractMap.SimpleImmutableEntry<>(")
        replace("Mth.TWO_PI", "((float) (Math.PI * 2.0))")
        replace("Mth.PI", "((float) Math.PI)")
        replace("Shapes.create(", "Shapes.box(")
        replace(".getEyePosition()", ".getEyePosition(1.0F)")
        replace("addRenderableWidget(", "addButton(")
        replace("Block.UPDATE_ALL", "io.github.capsicum0907.caldarium.Caldarium.UPDATE_ALL")
        replace("VertexFormat.Mode.QUADS", "org.lwjgl.opengl.GL11.GL_QUADS")
        replace(".setShaderState(RenderStateShard.RENDERTYPE_LIGHTNING_SHADER)", ".setShadeModelState(RenderStateShard.SMOOTH_SHADE)")
        replace("LightTexture.FULL_BRIGHT", "LightTexture.pack(15, 15)")
    }
    replacements.string(node.metadata.project.endsWith("-forge") && eval(node.metadata.version, "<1.17")) {
        replace("import net.minecraftforge.common.extensions.IForgeMenuType;", "import net.minecraftforge.common.extensions.IForgeContainerType;")
        replace("IForgeMenuType.create(", "IForgeContainerType.create(")
        replace("net.minecraftforge.network.NetworkHooks.", "net.minecraftforge.fml.network.NetworkHooks.")
    }
    replacements.string(eval(node.metadata.version, "<1.19")) {
        replace("Component.translatable(", "new net.minecraft.network.chat.TranslatableComponent(")
    }
    replacements.string(eval(node.metadata.version, "<1.19.3")) {
        replace("import net.minecraft.core.registries.Registries;", "import net.minecraft.core.Registry;")
        replace("(Registries.BLOCK,", "(Registry.BLOCK_REGISTRY,")
        replace("(Registries.ITEM,", "(Registry.ITEM_REGISTRY,")
        replace("(Registries.BLOCK_ENTITY_TYPE,", "(Registry.BLOCK_ENTITY_TYPE_REGISTRY,")
        replace("(Registries.MENU,", "(Registry.MENU_REGISTRY,")
        replace("BuiltInRegistries.BLOCK", "net.minecraft.core.Registry.BLOCK")
        replace("import org.joml.Matrix4f;", "import com.mojang.math.Matrix4f;")
    }
    replacements.string(node.metadata.project.endsWith("-fabric")) {
        replace("import net.neoforged.neoforge.energy.", "import io.github.capsicum0907.caldarium.fabric.energy.")
        replace("import net.neoforged.neoforge.items.", "import io.github.capsicum0907.caldarium.fabric.items.")
        replace("import net.neoforged.neoforge.fluids.", "import io.github.capsicum0907.caldarium.fabric.fluids.")
        replace("import net.neoforged.neoforge.common.ModConfigSpec;", "import io.github.capsicum0907.caldarium.fabric.config.ModConfigSpec;")
    }
    replacements.string(node.metadata.project.endsWith("-fabric") && eval(node.metadata.version, "<1.19")) {
        replace("ContainerItemContext.withConstant(", "ContainerItemContext.withInitial(")
    }
    replacements.string(eval(node.metadata.version, "<1.20.5")) {
        replace(".serializeNBT(registries)", ".serializeNBT()")
        replace(".deserializeNBT(registries, ", ".deserializeNBT(")
        replace(".writeToNBT(registries, ", ".writeToNBT(")
        replace(".readFromNBT(registries, ", ".readFromNBT(")
    }
}

tasks.named<Wrapper>("wrapper") {
    distributionType = Wrapper.DistributionType.BIN
}
