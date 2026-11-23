package io.github.capsicum0907.caldarium;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
//? if >=1.19.4
import net.minecraft.core.registries.Registries;

public final class Damage {
    //? if <1.19.4 {
    /*private static final DamageSource SOL = new Unworn("caldarium.sol");
    private static final DamageSource SPOLIARIUM = new Unworn("caldarium.spoliarium");

    private static final class Unworn extends DamageSource {
        private Unworn(String id) {
            super(id);
        }

        @Override
        public float getFoodExhaustion() {
            return 0.0F;
        }
    }
    *///?}

    private Damage() {
    }

    public static void sunburn(ServerLevel level, LivingEntity living, float amount) {
        //? if >=1.19.4 {
        living.hurt(new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(CaldariumRegistry.SOL_DAMAGE)), amount);
        //?} else {
        /*living.invulnerableTime = 0;
        living.hurt(SOL, amount);
        *///?}
    }

    public static void spoil(ServerLevel level, LivingEntity living) {
        //? if >=1.19.4 {
        living.hurt(new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(CaldariumRegistry.SPOLIARIUM_DAMAGE)), Float.MAX_VALUE);
        //?} else
        /*living.hurt(SPOLIARIUM, Float.MAX_VALUE);*/
    }

    public static boolean spoiled(DamageSource source) {
        //? if >=1.19.4 {
        return source.is(CaldariumRegistry.SPOLIARIUM_DAMAGE);
        //?} else
        /*return source == SPOLIARIUM;*/
    }

    public static DamageSource fire(ServerLevel level) {
        //? if >=1.19.4 {
        return level.damageSources().inFire();
        //?} else
        /*return DamageSource.IN_FIRE;*/
    }

    public static DamageSource blast(ServerLevel level, Player breaker) {
        //? if >=1.19.4 {
        return level.damageSources().explosion(breaker, breaker);
        //?} else
        /*return DamageSource.explosion(breaker);*/
    }
}
