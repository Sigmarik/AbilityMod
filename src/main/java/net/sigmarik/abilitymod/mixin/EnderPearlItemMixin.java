package net.sigmarik.abilitymod.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.sigmarik.abilitymod.AbilityMod;
import net.sigmarik.abilitymod.util.ServerState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderPearlItem.class)
public abstract class EnderPearlItemMixin extends Item {

    public EnderPearlItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "use", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/ItemCooldownManager;set(Lnet/minecraft/item/Item;I)V",
            shift = At.Shift.AFTER
    ))
    private void traiedSetDelay(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (ServerState.hasTrait(user, AbilityMod.TRAIT_EASY_PEARLS)) {
            user.getItemCooldownManager().set(Items.ENDER_PEARL, 0);
        }
    }
}
