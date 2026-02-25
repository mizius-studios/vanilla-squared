package blob.vanillasquared.util.combat;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Set;

public final class HitThroughConfig {

    private static final Set<Block> PASS_THROUGH_BLOCKS = Set.of(
            Blocks.SHORT_GRASS,
            Blocks.TALL_GRASS,
            Blocks.SHORT_DRY_GRASS,
            Blocks.TALL_DRY_GRASS,
            Blocks.FERN,
            Blocks.LARGE_FERN,
            Blocks.BUSH,
            Blocks.DEAD_BUSH,
            Blocks.DANDELION,
            Blocks.TORCHFLOWER,
            Blocks.POPPY,
            Blocks.BLUE_ORCHID,
            Blocks.ALLIUM,
            Blocks.AZURE_BLUET,
            Blocks.RED_TULIP,
            Blocks.ORANGE_TULIP,
            Blocks.WHITE_TULIP,
            Blocks.PINK_TULIP,
            Blocks.OXEYE_DAISY,
            Blocks.CORNFLOWER,
            Blocks.WITHER_ROSE,
            Blocks.LILY_OF_THE_VALLEY,
            Blocks.SUNFLOWER,
            Blocks.ROSE_BUSH,
            Blocks.PEONY,
            Blocks.PINK_PETALS,
            Blocks.WILDFLOWERS
    );

    private static final Map<Item, Set<Block>> HIT_THROUGH_BLOCKS_BY_ITEM = Map.of(
            Items.WOODEN_SWORD, PASS_THROUGH_BLOCKS,
            Items.STONE_SWORD, PASS_THROUGH_BLOCKS,
            Items.COPPER_SWORD, PASS_THROUGH_BLOCKS,
            Items.IRON_SWORD, PASS_THROUGH_BLOCKS,
            Items.GOLDEN_SWORD, PASS_THROUGH_BLOCKS,
            Items.DIAMOND_SWORD, PASS_THROUGH_BLOCKS,
            Items.NETHERITE_SWORD, PASS_THROUGH_BLOCKS
    );

    private HitThroughConfig() {
    }

    public static boolean canHitThrough(ItemStack stack) {
        return HIT_THROUGH_BLOCKS_BY_ITEM.containsKey(stack.getItem());
    }

    public static boolean isPassThroughBlock(ItemStack stack, BlockState state) {
        Set<Block> passThroughBlocks = HIT_THROUGH_BLOCKS_BY_ITEM.get(stack.getItem());
        return passThroughBlocks != null && passThroughBlocks.contains(state.getBlock());
    }
}
