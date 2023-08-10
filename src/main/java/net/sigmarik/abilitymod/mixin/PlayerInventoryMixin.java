package net.sigmarik.abilitymod.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Nameable;
import net.minecraft.util.collection.DefaultedList;
import net.sigmarik.abilitymod.AbilityMod;
import net.sigmarik.abilitymod.util.PropSets;
import net.sigmarik.abilitymod.util.ServerState;
import net.sigmarik.abilitymod.util.Traits;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin implements Inventory, Nameable {
    @Shadow @Final public PlayerEntity player;

    @Shadow public abstract void offerOrDrop(ItemStack stack);

    @Shadow protected abstract int addStack(int slot, ItemStack stack);

    @Shadow public abstract ItemStack removeStack(int slot);

    @Shadow @Final public DefaultedList<ItemStack> main;

    @Shadow public abstract int getSlotWithStack(ItemStack stack);

    @Shadow public abstract ItemStack dropSelectedItem(boolean entireStack);

    private int getWeight(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (ServerState.hasTrait(player, Traits.DISPOSABLE_STONE) &&
                PropSets.DISPOSABLE_ITEMS.contains(stack.getItem())) return 2;
        if (ServerState.hasTrait(player, Traits.TRICKY_MENDING) &&
                EnchantmentHelper.getLevel(Enchantments.MENDING, stack) > 0) return 1;
        return 10;
    }

    private int findMinWeight() {
        int cell = -1;
        int minWeight = 9999;

        for (int id = 0; id < main.size(); ++id) {
            int weight = getWeight(main.get(id));
            if (weight < minWeight) {
                cell = id;
                minWeight = weight;
            }
        }

        return cell;
    }

    @Inject(method = "addStack(Lnet/minecraft/item/ItemStack;)I", at = @At("RETURN"), cancellable = true)
    private void traitedAddStack(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() != stack.getCount()) return;

        int cell = findMinWeight();

        if (cell == -1) return;

        cell = getSlotWithStack(main.get(cell));

        if (getWeight(getStack(cell)) >= getWeight(stack)) return;

        player.dropItem(main.get(cell), true);
        removeStack(cell);

        cir.setReturnValue(addStack(cell, stack));
    }

    // @Inject(method = "addPickBlock", at = @At("HEAD"))
    // private void traitedPickBlock(ItemStack stack, CallbackInfo ci) {
    //     AbilityMod.LOGGER.info("Item has been scrolled");

    //     if (!ServerState.hasTrait(player, Traits.SHAKY_HANDS)) return;

    //     if (player.getWorld().getRandom().nextInt(100) < 100) {
    //         dropSelectedItem(true);
    //     }
    // }
}
