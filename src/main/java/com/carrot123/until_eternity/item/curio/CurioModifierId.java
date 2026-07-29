package com.carrot123.until_eternity.item.curio;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

public final class CurioModifierId {
    private CurioModifierId() {
    }

    public static UUID create(
            UUID slotUuid,
            String modifierKey
    ) {
        Objects.requireNonNull(slotUuid, "slotUuid");
        Objects.requireNonNull(modifierKey, "modifierKey");
        String source = slotUuid + "/until_eternity/" + modifierKey;
        return UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8));
    }
}
