package com.carrot123.until_eternity.compat.bettercombat;

import com.carrot123.until_eternity.tarot.TarotSingleCardPresence;

import net.bettercombat.api.client.AttackRangeExtensions;
import shiroroku.tarotcards.Registry.ItemRegistry;

public final class TarotStarBetterCombatCompat {

    private static boolean registered;

    private TarotStarBetterCombatCompat() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        AttackRangeExtensions.register(
                context -> {
                    var player =
                            context.player();

                    if (player == null
                            || !TarotSingleCardPresence.has(
                                    player,
                                    ItemRegistry.the_star.get()
                            )) {
                        return neutral();
                    }

                    return new AttackRangeExtensions.Modifier(
                            1.5D,
                            AttackRangeExtensions.Operation.MULTIPLY
                    );
                }
        );
    }

    private static AttackRangeExtensions.Modifier neutral() {
        return new AttackRangeExtensions.Modifier(
                1.0D,
                AttackRangeExtensions.Operation.MULTIPLY
        );
    }
}