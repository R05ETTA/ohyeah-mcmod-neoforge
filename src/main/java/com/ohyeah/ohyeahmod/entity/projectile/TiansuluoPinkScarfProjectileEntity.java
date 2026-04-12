package com.ohyeah.ohyeahmod.entity.projectile;

import com.ohyeah.ohyeahmod.registry.ModEntityTypes;
import com.ohyeah.ohyeahmod.registry.ModItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class TiansuluoPinkScarfProjectileEntity extends ThrowableItemProjectile {
    private float damage = 1.0F;

    public TiansuluoPinkScarfProjectileEntity(EntityType<? extends TiansuluoPinkScarfProjectileEntity> type, Level level) {
        super(type, level);
    }

    public TiansuluoPinkScarfProjectileEntity(Level level, LivingEntity owner) {
        super(ModEntityTypes.TIANSULUO_PINK_SCARF_PROJECTILE.get(), owner, level);
    }

    public TiansuluoPinkScarfProjectileEntity(Level level, double x, double y, double z) {
        super(ModEntityTypes.TIANSULUO_PINK_SCARF_PROJECTILE.get(), x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.TIANSULUO_PINK_SCARF_EGG.get();
    }

    public void setDamage(float damage) {
        this.damage = Math.max(0.0F, damage);
    }

    @Override
    public void handleEntityEvent(byte status) {
        if (status == 3) {
            ItemStack stack = this.getItem();
            ItemParticleOption particle = new ItemParticleOption(ParticleTypes.ITEM, stack.isEmpty() ? new ItemStack(this.getDefaultItem()) : stack);
            for (int i = 0; i < 8; i++) {
                this.level().addParticle(particle, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        entity.hurt(this.damageSources().thrown(this, this.getOwner()), this.damage);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }
}
