package arghorror;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * The Architect: an invisible stalker that exists in the world.
 * It never attacks. It watches. It stops ~10 blocks away and holds.
 * If the player looks directly at it, it freezes (Weeping Angel rule).
 * If the player looks away, it steps closer.
 * Despawns at dawn. Extinguishes torches near it via PresenceSystem.
 */
public class TheArchitectEntity extends PathfinderMob {

    private static final EntityDataAccessor<Boolean> FROZEN =
        SynchedEntityData.defineId(TheArchitectEntity.class, EntityDataSerializers.BOOLEAN);

    // How long the entity has been watching a specific player (ticks)
    private int watchTicks = 0;
    private UUID targetPlayerUUID = null;

    public TheArchitectEntity(EntityType<? extends TheArchitectEntity> type, Level level) {
        super(type, level);
        this.setInvisible(true);
        this.setNoAi(false);
        this.setSilent(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 2000.0)
            .add(Attributes.MOVEMENT_SPEED, 0.28)
            .add(Attributes.FOLLOW_RANGE, 64.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FROZEN, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new WatchAndStalkGoal(this));
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        // Completely immune to all damage
        return false;
    }

    @Override
    public boolean isInvisible() {
        return true;
    }

    public int getWatchTicks() { return watchTicks; }
    public UUID getTargetPlayerUUID() { return targetPlayerUUID; }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;

        ServerLevel level = (ServerLevel) this.level();
        long dayTime = level.getDayTime() % 24000;

        // Despawn at dawn (6000 = roughly 6am)
        if (dayTime >= 6000 && dayTime <= 12000 && !level.dimensionType().hasFixedTime()) {
            this.discard();
            return;
        }

        // Find nearest player
        Player nearest = level.getNearestPlayer(this, 64);
        if (nearest == null) return;
        targetPlayerUUID = nearest.getUUID();

        // Weeping Angel: freeze if player is looking at us
        boolean playerLooking = isPlayerLookingAt((ServerPlayer) nearest);
        this.entityData.set(FROZEN, playerLooking);
        this.setNoAi(playerLooking);

        // Count watch ticks for chapter progression
        if (distanceTo(nearest) < 20) {
            watchTicks++;
            // After 3 seconds of watching, trigger presence events
            if (watchTicks % 60 == 0) {
                PresenceSystem.onArchitectWatch((ServerPlayer) nearest, this.blockPosition());
            }
            // After 60 seconds of watching, notify StoryManager
            if (watchTicks == 1200) {
                StoryManager.onArchitectWatched60s((ServerPlayer) nearest);
            }
        }
    }

    private boolean isPlayerLookingAt(ServerPlayer player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 toEntity = this.position().subtract(eyePos).normalize();
        double dot = lookVec.dot(toEntity);
        double dist = this.distanceTo(player);
        // Within 40 degree cone and 30 blocks
        return dot > 0.766 && dist < 30;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("WatchTicks", watchTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        watchTicks = tag.getInt("WatchTicks");
    }

    // -------------------------------------------------------------------------
    // Inner Goal: Stalk the nearest player, stop ~10 blocks away
    // -------------------------------------------------------------------------
    static class WatchAndStalkGoal extends Goal {
        private final TheArchitectEntity entity;
        private Player target;
        private int stuckTicks = 0;

        WatchAndStalkGoal(TheArchitectEntity entity) {
            this.entity = entity;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            target = entity.level().getNearestPlayer(entity, 64);
            return target != null;
        }

        @Override
        public void tick() {
            if (target == null || entity.entityData.get(FROZEN)) return;
            entity.getLookControl().setLookAt(target);

            double dist = entity.distanceTo(target);
            if (dist > 12) {
                entity.getNavigation().moveTo(target, 1.0);
                stuckTicks = 0;
            } else if (dist < 8) {
                // Back away slightly if too close
                Vec3 away = entity.position().subtract(target.position()).normalize().scale(3);
                entity.getNavigation().moveTo(
                    entity.getX() + away.x, entity.getY(), entity.getZ() + away.z, 0.8);
            } else {
                entity.getNavigation().stop();
            }
        }
    }
}
