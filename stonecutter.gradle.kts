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
    replacements.string(eval(node.metadata.version, "<1.20.5")) {
        replace(".serializeNBT(registries)", ".serializeNBT()")
        replace(".deserializeNBT(registries, ", ".deserializeNBT(")
        replace(".writeToNBT(registries, ", ".writeToNBT(")
        replace(".readFromNBT(registries, ", ".readFromNBT(")
        replace("helper.makeMockPlayer(GameType.SURVIVAL)", "helper.makeMockSurvivalPlayer()")
    }
}

tasks.named<Wrapper>("wrapper") {
    distributionType = Wrapper.DistributionType.BIN
}
