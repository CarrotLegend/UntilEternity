package com.carrot123.until_eternity.client.screen.book;

import com.carrot123.until_eternity.item.lore.LoreBookItem;
import com.carrot123.until_eternity.until_eternity;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

final class LoreBookDefinitions {

    private static final String GUI_ROOT =
            "textures/gui/book/";

    private static final String ILLUSTRATION_ROOT =
            GUI_ROOT + "illustrations/";

    static final BookDefinition MINER_LOG =
            new BookDefinition(
                    "item.until_eternity.miner_log",
                    texture("miner_log.png"),
                    texture("miner_log_buttons.png"),

                    256,
                    192,

                    38,
                    17,
                    180,
                    166,

                    0x49315E,
                    0x29202D,
                    0x6B467E,

                    true,

                    true,

                    12,

                    List.of(
                            illustrated(
                                    "miner_log",
                                    1,
                                    "end_cave"
                            ),
                            illustrated(
                                    "miner_log",
                                    2,
                                    "strange_vein"
                            ),
                            illustrated(
                                    "miner_log",
                                    3,
                                    "obsidian_pillars"
                            ),
                            illustrated(
                                    "miner_log",
                                    4,
                                    "enderman_notes"
                            ),
                            page(
                                    "miner_log",
                                    5
                            ),
                            page(
                                    "miner_log",
                                    6
                            )
                    )
            );

    static final BookDefinition DAMP_DIARY =
            new BookDefinition(
                    "item.until_eternity.damp_diary",
                    texture("damp_diary.png"),
                    texture("damp_diary_buttons.png"),

                    256,
                    192,

                    50,
                    17,
                    166,
                    166,

                    0x30483E,
                    0x202E29,
                    0x496759,

                    true,

                    true,

                    12,

                    List.of(
                            illustrated(
                                    "damp_diary",
                                    1,
                                    "wet_corridor"
                            ),
                            page(
                                    "damp_diary",
                                    2
                            ),
                            illustrated(
                                    "damp_diary",
                                    3,
                                    "broken_brazier"
                            ),
                            illustrated(
                                    "damp_diary",
                                    4,
                                    "crest_fragment"
                            ),
                            illustrated(
                                    "damp_diary",
                                    5,
                                    "flooded_map"
                            ),
                            page(
                                    "damp_diary",
                                    6
                            )
                    )
            );

    private LoreBookDefinitions() {
    }

    static BookDefinition tornPaper(
            LoreBookItem.Type type
    ) {

        int number = switch (type) {
            case TORN_PAPER_1 -> 1;
            case TORN_PAPER_2 -> 2;
            case TORN_PAPER_3 -> 3;
            case TORN_PAPER_4 -> 4;
            case TORN_PAPER_5 -> 5;

            default -> throw new IllegalArgumentException(
                    "Not a torn paper type: " + type
            );
        };

        String id =
                "torn_paper_" + number;

        return new BookDefinition(
                "item.until_eternity." + id,
                texture(id + ".png"),

                null,

                192,
                192,

                28,
                23,
                136,
                150,

                0x60452F,
                0x382A21,
                0x60452F,

                false,
                false,

                0,

                List.of(
                        page(id, 1)
                )
        );
    }

    private static BookPageData page(
            String book,
            int page
    ) {

        String root =
                "gui.until_eternity.lore_book."
                        + book
                        + ".page."
                        + page;

        return BookPageData.text(
                root + ".title",
                root + ".body.1",
                root + ".body.2"
        );
    }

    private static BookPageData illustrated(
            String book,
            int page,
            String image
    ) {

        String root =
                "gui.until_eternity.lore_book."
                        + book
                        + ".page."
                        + page;

        return BookPageData.illustrated(
                root + ".title",
                illustration(image),
                root + ".body.1",
                root + ".body.2"
        );
    }

    private static ResourceLocation texture(
            String name
    ) {

        return new ResourceLocation(
                until_eternity.MODID,
                GUI_ROOT + name
        );
    }

    private static ResourceLocation illustration(
            String name
    ) {

        return new ResourceLocation(
                until_eternity.MODID,
                ILLUSTRATION_ROOT
                        + name
                        + ".png"
        );
    }
}