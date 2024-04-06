package party.bongs.loglogrevolution;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.bukkit.Material.*;

public final class Plugin extends JavaPlugin implements Listener {

    private static final Set<Material> logs = Set.of(OAK_LOG, ACACIA_LOG, BIRCH_LOG, CHERRY_LOG, DARK_OAK_LOG,
            JUNGLE_LOG, MANGROVE_LOG, SPRUCE_LOG, CRIMSON_STEM, WARPED_STEM, MANGROVE_ROOTS);

    private static final Set<Material> leaves = Set.of(OAK_LEAVES, ACACIA_LEAVES, BIRCH_LEAVES, CHERRY_LEAVES,
            DARK_OAK_LEAVES, JUNGLE_LEAVES, MANGROVE_LEAVES, SPRUCE_LEAVES, CRIMSON_HYPHAE, CRIMSON_NYLIUM,
            WARPED_HYPHAE, WARPED_NYLIUM);

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerBlockBreakEvent(BlockBreakEvent ev) {

        if (!isAxe(ev.getPlayer().getActiveItem())) return;

        Set<Block> breakyLot = ConcurrentHashMap.newKeySet();

        Block breaky = ev.getBlock();
        breakyListyAddy(breakyLot, breaky);

        for (Block block : breakyLot) {
            block.breakNaturally();
        }
    }

    private void breakyListyAddy(Set<Block> breakies, Block breaky) {

        // if block is already in our list, do nothing
        if (breakies.contains(breaky)) return;

        // if nor log or leaf, do nothing
        if (!logs.contains(breaky.getType()) && !leaves.contains(breaky.getType())) return;

        // put block in our list
        breakies.add(breaky);

        // for all neighbours
        for (BlockFace face : BlockFace.values()) {
            // call recursive
            breakyListyAddy(breakies, breaky.getRelative(face));
        }
    }

    private boolean isAxe(@Nullable ItemStack stack) {
        if (stack == null) return false;
        return switch (stack.getType()) {
            case WOODEN_AXE,
                 STONE_AXE,
                 GOLDEN_AXE,
                 IRON_AXE,
                 DIAMOND_AXE,
                 NETHERITE_AXE,
                 LEGACY_WOOD_AXE,
                 LEGACY_STONE_AXE,
                 LEGACY_GOLD_AXE,
                 LEGACY_IRON_AXE,
                 LEGACY_DIAMOND_AXE
                    -> true;
            default -> false;
        };
    }

}
