package net.sigmarik.abilitymod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.sigmarik.abilitymod.util.ServerState;
import net.sigmarik.abilitymod.util.Traits;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderPearlEntity.class)
public abstract class EnderPearlEntityMixin extends ThrownItemEntity {
    @Shadow protected abstract void onCollision(HitResult hitResult);

    public EnderPearlEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "onCollision", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z")
    )
    private boolean traitedDamage(Entity entity, DamageSource damageSource, float amount) {
        if (ServerState.hasTrait((PlayerEntity) entity, Traits.EASY_PEARLS)) {
            return false;
        } else return entity.damage(damageSource, amount);
    }

    @Inject(method="tick", at=@At(value="INVOKE", target="Lnet/minecraft/entity/projectile/thrown/ThrownItemEntity;tick()V"), cancellable = true)
    private void traitedHitScan(CallbackInfo ci) {
        if (!(getOwner() instanceof ServerPlayerEntity &&
                ServerState.hasTrait((PlayerEntity)getOwner(), Traits.EASY_PEARLS))) {
            return;
        }

        BlockHitResult hitResult = longRangeRaycast(world, (PlayerEntity)getOwner(), RaycastContext.FluidHandling.NONE, 256);
        if (hitResult.getType() == HitResult.Type.MISS) this.kill();
        else {
            this.setPos(hitResult.getPos().getX(), hitResult.getPos().getY(), hitResult.getPos().getZ());
            this.onCollision(hitResult);
        }

        ci.cancel();
    }

    private static BlockHitResult longRangeRaycast(World world, PlayerEntity player,
                                                   RaycastContext.FluidHandling fluidHandling, double distance) {
        float f = player.getPitch();
        float g = player.getYaw();
        Vec3d vec3d = player.getEyePos();
        float h = MathHelper.cos(-g * ((float)Math.PI / 180) - (float)Math.PI);
        float i = MathHelper.sin(-g * ((float)Math.PI / 180) - (float)Math.PI);
        float j = -MathHelper.cos(-f * ((float)Math.PI / 180));
        float k = MathHelper.sin(-f * ((float)Math.PI / 180));
        float l = i * j;
        float n = h * j;
        Vec3d vec3d2 = vec3d.add((double)l * distance, (double) k * distance, (double)n * distance);
        return world.raycast(new RaycastContext(vec3d, vec3d2, RaycastContext.ShapeType.OUTLINE, fluidHandling, player));
    }
}
