package com.carrot123.until_eternity.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.carrot123.until_eternity.combat.AbsoluteDamageMath;
import com.carrot123.until_eternity.effect.VoidCorrosionDamageLogic;
import org.junit.jupiter.api.Test;

class ArmorRendCompositionTest {
    @Test
    void amplifiesOnceBeforeTrueKnifeSplitAndComposesWithCorrosion() {
        float armorRended = ArmorRendDamageLogic.apply(10.0F, 6, 5);
        float amplified = VoidCorrosionDamageLogic.amplifyIncomingDamage(armorRended);
        AbsoluteDamageMath.Split split = AbsoluteDamageMath.split(amplified, 100.0F, true);

        assertEquals(30.0F, amplified, 1.0E-6F);
        assertEquals(15.0F, split.directDamage(), 1.0E-6F);
        assertEquals(15.0F, split.eventDamage(), 1.0E-6F);
    }
}
