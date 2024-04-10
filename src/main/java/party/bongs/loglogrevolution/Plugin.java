package party.bongs.loglogrevolution;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.bukkit.Material.*;
import static org.bukkit.block.BlockFace.*;

public final class Plugin extends JavaPlugin implements Listener {

    private static final int leaveLimit = 420;

    private static final Set<BlockFace> neighborFaces = Set.of(
            NORTH,
            EAST,
            SOUTH,
            WEST,
            UP,
            DOWN
    );

    private static final Set<Material> logs = Set.of(
            OAK_LOG,
            SPRUCE_LOG,
            BIRCH_LOG,
            JUNGLE_LOG,
            ACACIA_LOG,
            DARK_OAK_LOG,
            MANGROVE_LOG,
            MANGROVE_ROOTS,
            MUDDY_MANGROVE_ROOTS,
            CHERRY_LOG,
            CRIMSON_STEM,
            WARPED_STEM
    );

    private static final Set<Material> axes = Set.of(
            WOODEN_AXE,
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
    );

    private static final Map<Material, Set<Material>> connected = ImmutableMap
            .<Material, Set<Material>>builder()
            .put(OAK_LOG, Set.of(OAK_LEAVES))
            .put(SPRUCE_LOG, Set.of(SPRUCE_LEAVES))
            .put(BIRCH_LOG, Set.of(BIRCH_LEAVES))
            .put(JUNGLE_LOG, Set.of(JUNGLE_LEAVES))
            .put(ACACIA_LOG, Set.of(ACACIA_LEAVES))
            .put(DARK_OAK_LOG, Set.of(DARK_OAK_LEAVES))
            .put(MANGROVE_LOG, Set.of(MANGROVE_LEAVES, MANGROVE_ROOTS, MUDDY_MANGROVE_ROOTS))
            .put(MANGROVE_ROOTS, Set.of(MANGROVE_LEAVES, MANGROVE_LOG, MUDDY_MANGROVE_ROOTS))
            .put(MUDDY_MANGROVE_ROOTS, Set.of(MANGROVE_LEAVES, MANGROVE_LOG, MANGROVE_ROOTS))
            .put(CHERRY_LOG, Set.of(CHERRY_LEAVES))
            .put(CRIMSON_STEM, Set.of(NETHER_WART_BLOCK, SHROOMLIGHT))
            .put(WARPED_STEM, Set.of(WARPED_WART_BLOCK, SHROOMLIGHT))
            .build();

    private static Set<Block> neighbors(Block center, Predicate<Material> filter) {
        return neighborFaces.stream()

                // grab all neighbors of the center block
                .map(center::getRelative)

                // keep them if they match the material filter
                .filter(block -> filter
                        .test(block.getType()))
                .collect(Collectors.toUnmodifiableSet());
    }

    private static void searchConnected(
            Block start,
            List<Block> found,
            Predicate<Material> filter,
            int limit
    ) {
        for (Block block : neighbors(start, filter)) {

            // limit reached, stop searching
            if (found.size() >= limit) return;

            // already visited, skip this block
            if (found.contains(block)) continue;

            // wrong material, skip this block
            if (!filter.test(block.getType())) continue;

            // check neighbors
            found.add(block);
            searchConnected(block, found, filter, limit);
        }
    }

    private static void damageTool(ItemStack tool) {
        if (tool.getItemMeta() instanceof Damageable damageable) {
            var damageChance = 100 / (tool.getEnchantmentLevel(Enchantment.DURABILITY) + 1);
            if (ThreadLocalRandom.current().nextInt(1, 100 + 1) <= damageChance) {
                damageable.setDamage(damageable.getDamage() + 1);
                tool.setItemMeta(damageable);
            }
        }
    }

    private static int getDura(ItemStack tool) {
        var damage = ((Damageable) tool.getItemMeta()).getDamage();
        return tool.getType().getMaxDurability() - damage;
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerBlockBreakEvent(BlockBreakEvent ev) {

        // only axes
        var tool = ev.getPlayer().getInventory().getItemInMainHand();
        if (!axes.contains(tool.getType())) return;

        var logMat = ev.getBlock().getType();

        // only logs
        if (!logs.contains(logMat)) return;

        // trunk
        List<Block> trunk = new ArrayList<>();
        searchConnected(ev.getBlock(), trunk, mat -> mat.equals(logMat), getDura(tool));

        // leaves
        List<Block> breakies = new ArrayList<>(trunk);
        for (Block block : trunk) {
            searchConnected(block, breakies, mat -> connected
                    .getOrDefault(logMat, Collections.emptySet())
                    .contains(mat), leaveLimit);
        }

        for (Block block : breakies) {

            // damage tool for each log
            if (logs.contains(block.getType())) {
                if (getDura(tool) <= 1) {
                    return;
                }
                damageTool(tool);
            }

            block.breakNaturally();
        }
    }
}
