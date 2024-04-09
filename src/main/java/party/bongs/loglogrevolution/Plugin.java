package party.bongs.loglogrevolution;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.bukkit.Material.*;

public final class Plugin extends JavaPlugin implements Listener {

    private static final int limit = 420;

    private static final Set<Vector> neighbors = Set.of(
            new Vector( 0, 0, -1), // north
            new Vector( 1, 0,  0), // east
            new Vector( 0, 0,  1), // south
            new Vector(-1, 0,  0), // west
            new Vector( 1, 0, -1), // north-east
            new Vector(-1, 0, -1), // north-west
            new Vector( 1, 0,  1), // south-east
            new Vector(-1, 0,  1)  // south-west
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

    private static final Map<Material, Set<Material>> associated = ImmutableMap.<Material, Set<Material>>builder()
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

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerBlockBreakEvent(BlockBreakEvent ev) {

        // only logs
        if (!logs.contains(ev.getBlock().getType())) return;

        // only axes
        if (!axes.contains(ev.getPlayer().getInventory().getItemInMainHand().getType())) return;

        Set<Block> breakies = new HashSet<>();

        // trunk
        breakies.addAll(bfs(ev.getBlock(), mat1 -> mat1.equals(ev.getBlock().getType())));

        // leaves
        breakies.addAll(bfs(ev.getBlock(), mat -> associated
                .getOrDefault(ev.getBlock().getType(), Collections.emptySet())
                .contains(mat)));

        for (Block block : breakies) {
            block.breakNaturally();
            // TODO: drop all loot in front of player
        }
    }

    private Set<Block> neighbors(Block center, Predicate<Material> filter) {
        return neighbors.stream()
                // grab all neighbors of the center block
                .map(v -> center.getRelative(v.getBlockX(), v.getBlockY(), v.getBlockZ()))
                // keep them if they match the material filter
                .filter(block -> filter.test(block.getType()))
                .collect(Collectors.toUnmodifiableSet());
    }

    private Set<Block> bfs(Block start, Predicate<Material> filter) {
        Set<Block> blocks = new HashSet<>();

        // TODO

        if (blocks.size() >= limit) return blocks;

        return blocks;
    }
}
