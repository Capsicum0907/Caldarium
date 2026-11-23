package io.github.capsicum0907.caldarium.fabric;

import java.util.List;
import java.util.Optional;

import io.github.capsicum0907.caldarium.Tooltips;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ReadingItem extends BlockItem {
    public ReadingItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        List<Tooltips.Row> rows = Tooltips.of(getBlock());
        return rows.isEmpty() ? Optional.empty() : Optional.of(new Tooltips.Readings(rows));
    }
}
