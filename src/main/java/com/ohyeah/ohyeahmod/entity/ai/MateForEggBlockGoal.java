package com.ohyeah.ohyeahmod.entity.ai;

import com.ohyeah.ohyeahmod.entity.common.EggLayingSpecies;
import com.ohyeah.ohyeahmod.sound.bridge.SoundParticipant;
import com.ohyeah.ohyeahmod.sound.bridge.SpeciesSoundFacade;
import com.ohyeah.ohyeahmod.sound.definition.SoundCue;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.animal.Animal;

public class MateForEggBlockGoal<T extends Animal & EggLayingSpecies & SoundParticipant> extends BreedGoal {
    private final T tiansuluo;
    private final String carriedMessageKey;

    public MateForEggBlockGoal(T tiansuluo, double speed, String carriedMessageKey) {
        super(tiansuluo, speed);
        this.tiansuluo = tiansuluo;
        this.carriedMessageKey = carriedMessageKey;
    }

    @Override
    public boolean canUse() {
        return !this.tiansuluo.hasCarriedEggBlock() && super.canUse();
    }

    @Override
    protected void breed() {
        ServerPlayer player = this.animal.getLoveCause();
        if (player == null && this.partner.getLoveCause() != null) {
            player = this.partner.getLoveCause();
        }
        this.tiansuluo.setHasCarriedEggBlock(true);
        this.tiansuluo.setEggBlockAttractedPlayer(player);
        if (player != null) {
            player.displayClientMessage(Component.translatable(this.carriedMessageKey), true);
        }
        SpeciesSoundFacade.playCue(this.tiansuluo, SoundCue.BREED_SUCCESS, 1.0F, 1.0F);
        this.animal.setAge(6000);
        this.partner.setAge(6000);
        this.animal.resetLove();
        this.partner.resetLove();
        this.animal.level().broadcastEntityEvent(this.animal, (byte) 18);
        if (this.animal.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT)) {
            this.animal.level().addFreshEntity(new ExperienceOrb(this.animal.level(), this.animal.getX(), this.animal.getY(), this.animal.getZ(), this.animal.getRandom().nextInt(7) + 1));
        }
    }
}
