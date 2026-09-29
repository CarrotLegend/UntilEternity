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
                    texture("END_book_GUI.png"),
                    texture("END_butten_GUI.png"),

                    256,
                    192,
                    128,
                    96,

                    26,
                    35,
                    204,
                    162,

                    0x49315E,
                    0x29202D,
                    0x6B467E,

                    true,

                    false,

                    12,

                    List.of(
                            page("miner_log", 1),
                            page("miner_log", 2),
                            minerPage(3, "body.1", "body.2", "emphasis.1", "body.3"),
                            page("miner_log", 4),
                            page(
                                    "miner_log",
                                    5
                            ),
                            minerPage(6, "body.1", "body.2", "emphasis.1", "body.3", "emphasis.2")
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
                texture("END_paper_GUI.png"),

                null,

                256,
                192,
                128,
                96,

                80,
                26,
                96,
                166,

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

    private static BookPageData minerPage(int page, String... parts) {
        String root = "gui.until_eternity.lore_book.miner_log.page." + page;
        String[] keys = new String[parts.length];
        for (int index = 0; index < parts.length; index++) {
            keys[index] = root + "." + parts[index];
        }
        return BookPageData.text(root + ".title", keys);
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
