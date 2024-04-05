package lol.hub.fastharvest;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static org.bukkit.Material.*;

public final class Plugin extends JavaPlugin implements Listener {

    private static final Set<Material> crops = Set.of(WHEAT, CARROTS, POTATOES, BEETROOTS, NETHER_WART);
    private static final Set<Material> seeds = Set.of(WHEAT_SEEDS, BEETROOT_SEEDS);

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEvent(PlayerInteractEvent ev) {

        if (ev.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (ev.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Block block = ev.getClickedBlock();
        if (block == null) return;

        if (!crops.contains(block.getType())) {
            // not a crop
            return;
        }

        Ageable data = (Ageable) block.getBlockData();
        if (data.getAge() < data.getMaximumAge()) {
            // not ripe
            return;
        }

        ItemStack tool = ItemStack.empty();
        if (isHoe(ev.getItem())) {
            tool = ev.getItem();
        }

        // drop loot
        for (ItemStack stack : block.getDrops(tool)) {

            // balance drops
            if (seeds.contains(stack.getType())) {

                // drop is a seed

                if (ThreadLocalRandom.current().nextBoolean()) {
                    // drop 1 seed maximum
                    stack.setAmount(1);
                } else {
                    // ignore seed
                    continue;
                }

            } else {

                // drop is not a seed

                // drop 1 less for balancing if possible
                if (!isHoe(ev.getItem()) && stack.getAmount() > 1) {
                    stack.setAmount(stack.getAmount() - 1);
                }

            }

            block.getWorld().dropItemNaturally(block.getLocation().add(0, 0.5, 0), stack);
        }

        // damage tool
        if (tool.getItemMeta() instanceof Damageable damageable) {
            var damageChance = 100 / (tool.getEnchantmentLevel(Enchantment.DURABILITY) + 1);
            if (ThreadLocalRandom.current().nextInt(1, 100 + 1) <= damageChance) {
                damageable.setDamage(damageable.getDamage() + 1);
                tool.setItemMeta(damageable);
            }
        }

        // reset crop age
        data.setAge(0);
        block.setBlockData(data);

        // play sound
        block.getWorld().playSound(block.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 0.33f, 0.8f);

    }

    private boolean isHoe(@Nullable ItemStack stack) {
        if (stack == null) return false;
        return switch (stack.getType()) {
            case WOODEN_HOE,
                 STONE_HOE,
                 GOLDEN_HOE,
                 IRON_HOE,
                 DIAMOND_HOE,
                 NETHERITE_HOE,
                 LEGACY_WOOD_HOE,
                 LEGACY_STONE_HOE,
                 LEGACY_GOLD_HOE,
                 LEGACY_IRON_HOE,
                 LEGACY_DIAMOND_HOE
                    -> true;
            default -> false;
        };
    }

}
