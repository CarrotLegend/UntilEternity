import argparse
import copy
import json
from pathlib import Path

import rebuild_plains_settlement as nbt


ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "src/main/resources/data/until_eternity"
PLAINS = "plains_settlement"
DESERT = "desert_settlement"
KINDS = (PLAINS, DESERT)
FLOOR_HOLES = {
    "house_3": (1, 0, 0),
    "house_4": (1, 0, 0),
    "pyramid": (0, 0, 0),
}
DESERT_PIECES = [f"house_{index}" for index in range(1, 9)] + [
    "pyramid", "camel_shelter", "hay_pile"]
PLAINS_PIECES = ([f"house_{index}" for index in range(1, 10)] +
                 ["large_field", "stall_1", "stall_2"] +
                 [f"car_{index}" for index in range(1, 4)] +
                 [f"lamp_{index}" for index in range(1, 4)] +
                 ["well", "fence", "sand_pile", "wood_pile"])
PLAINS_DECOR = [f"car_{index}" for index in range(1, 4)] + [
    f"lamp_{index}" for index in range(1, 4)] + ["sand_pile", "wood_pile", "fence"]
ROAD_SPECS = {
    "straight": (11, ("east",), {"north": "building", "south": "building"}),
    "straight_decor": (11, ("east",), {"north": "building", "south": "decor"}),
    "straight_special": (11, ("east",), {"north": "building", "south": "special"}),
    "turn_left": (7, ("north",), {"east": "building", "south": "building"}),
    "turn_right": (7, ("south",), {"east": "building", "north": "building"}),
    "junction_t": (7, ("east", "north"), {"south": "building"}),
    "junction_cross": (7, ("east", "north", "south"), {}),
    "end": (7, (), {"north": "building", "east": "building", "south": "decor"}),
}
ROAD_WEIGHTS = {
    PLAINS: {"straight": 6, "straight_decor": 2, "straight_special": 1,
             "turn_left": 2, "turn_right": 2, "junction_t": 3,
             "junction_cross": 1, "end": 2},
    DESERT: {"straight": 6, "straight_decor": 2, "straight_special": 1,
             "turn_left": 2, "turn_right": 2, "junction_t": 2,
             "junction_cross": 1, "end": 3},
}


def prefix(kind):
    return f"until_eternity:{kind}"


def structures(kind):
    return DATA / "structures" / kind


def pools(kind):
    return DATA / "worldgen/template_pool" / kind


def piece_path(kind, name):
    if kind == PLAINS:
        return structures(kind) / "pieces" / f"{name}.nbt"
    return structures(kind) / f"{name}.nbt"


def jigsaw_blocks(root):
    return [block for block in nbt.blocks(root)
            if nbt.block_name(root, block) == "minecraft:jigsaw"]


def road_socket(kind, slot):
    return (prefix(kind) + "/" + {"building": "building_connector",
                                  "decor": "decor_connector",
                                  "special": "special_connector"}[slot])


def update_plains_pieces():
    for name in PLAINS_DECOR + ["large_field"]:
        path = piece_path(PLAINS, name)
        root_name, root = nbt.read_template(path)
        ports = jigsaw_blocks(root)
        if len(ports) != 1:
            raise ValueError(f"{path}: expected one existing Jigsaw")
        port = ports[0].value["nbt"]
        expected = road_socket(PLAINS, "special" if name == "large_field" else "decor")
        old = nbt.field(port, "name")
        if old not in (prefix(PLAINS) + "/building_connector", expected):
            raise ValueError(f"{path}: unexpected existing connector {old}")
        if old != expected:
            port.value["name"] = nbt.text(expected)
            nbt.write_template(path, root_name, root)


def update_desert_pieces():
    for name in DESERT_PIECES:
        path = piece_path(DESERT, name)
        root_name, root = nbt.read_template(path)
        existing = jigsaw_blocks(root)
        slot = "special" if name == "pyramid" else "decor" if name == "hay_pile" else "building"
        expected = road_socket(DESERT, slot)
        if existing:
            if len(existing) != 1 or nbt.field(existing[0].value["nbt"], "name") != expected:
                raise ValueError(f"{path}: unexpected existing Jigsaw")
            continue
        if name in FLOOR_HOLES:
            hole = FLOOR_HOLES[name]
            old = nbt.at(root, hole)
            if old is None or nbt.block_name(root, old) != "minecraft:air":
                raise ValueError(f"{path}: expected floor gap at {hole}")
            nbt.set_block(root, hole, nbt.state_index(root, "minecraft:sandstone"))
        if name == "house_7":
            side, pos = "south", (3, 1, 8)
        else:
            side, preferred = nbt.choose_entrance(root)
            pos = nbt.open_edge(root, side, preferred)
        old = nbt.at(root, pos)
        if old is not None and "nbt" in old.value:
            raise ValueError(f"{path}: entrance would replace a block entity")
        final_state = nbt.block_state_string(root, old) if old else "minecraft:air"
        nbt.jigsaw(root, pos, side, expected, "minecraft:empty", "minecraft:empty",
                   final_state, only_air=old is None or nbt.block_name(root, old) == "minecraft:air")
        nbt.write_template(path, root_name, root)


def create_desert_center():
    path = structures(DESERT) / "town_centers/center_1.nbt"
    if path.exists():
        root = nbt.read_template(path)[1]
        if len(jigsaw_blocks(root)) != 4:
            raise ValueError(f"{path}: existing center is not the expected four-exit plaza")
        return
    source_name, root = nbt.read_template(piece_path(DESERT, "hay_pile"))
    root = copy.deepcopy(root)
    root.value["size"] = nbt.list_tag(3, [nbt.integer(13), nbt.integer(4), nbt.integer(13)])
    root.value["palette"] = nbt.list_tag(10, [])
    root.value["blocks"] = nbt.list_tag(10, [])
    root.value["entities"] = nbt.list_tag(10, [])
    sandstone = nbt.state_index(root, "minecraft:sandstone")
    smooth = nbt.state_index(root, "minecraft:smooth_sandstone")
    cut = nbt.state_index(root, "minecraft:cut_sandstone")
    chiseled = nbt.state_index(root, "minecraft:chiseled_sandstone")
    water = nbt.state_index(root, "minecraft:water", {"level": "0"})
    for x in range(13):
        for z in range(13):
            path_line = abs(x - 6) <= 1 or abs(z - 6) <= 1
            border = x in (0, 12) or z in (0, 12)
            nbt.set_block(root, (x, 0, z), cut if border else smooth if path_line else sandstone)
    for x in range(5, 8):
        for z in range(5, 8):
            if x == 6 and z == 6:
                nbt.set_block(root, (x, 1, z), water)
            elif x in (5, 7) and z in (5, 7):
                nbt.set_block(root, (x, 1, z), chiseled)
                nbt.set_block(root, (x, 2, z), cut)
            else:
                nbt.set_block(root, (x, 1, z), cut)
    for side, pos in (("north", (6, 1, 0)), ("south", (6, 1, 12)),
                      ("west", (0, 1, 6)), ("east", (12, 1, 6))):
        nbt.jigsaw(root, pos, side, prefix(DESERT) + "/street_out",
                   prefix(DESERT) + "/street_in", prefix(DESERT) + "/streets")
    nbt.write_template(path, source_name, root)


def road_positions(width):
    center = width // 2
    return {"west": (0, 1, 3), "east": (width - 1, 1, 3),
            "north": (center, 1, 0), "south": (center, 1, 6)}


def create_roads(kind):
    source_name, source = nbt.read_template(structures(PLAINS) / "roads/turn_left.nbt")
    for road_name, (width, outputs, side_slots) in ROAD_SPECS.items():
        root = copy.deepcopy(source)
        root.value["size"] = nbt.list_tag(3, [nbt.integer(width), nbt.integer(3), nbt.integer(7)])
        root.value["palette"] = nbt.list_tag(10, [])
        root.value["blocks"] = nbt.list_tag(10, [])
        root.value["entities"] = nbt.list_tag(10, [])
        main_block = nbt.state_index(root, "minecraft:dirt_path" if kind == PLAINS else "minecraft:smooth_sandstone")
        center = width // 2
        positions = road_positions(width)
        extra_decor = None
        if width == 11:
            if road_name == "straight":
                positions["south"] = (8, 1, 6)
                extra_decor = (1, 1, 6)
            else:
                positions["north"] = (8, 1, 0)
                extra_decor = (1, 1, 0)
        for x in range(width):
            for z in range(7):
                road = (x <= center and abs(z - 3) <= 1)
                road |= "east" in outputs and x >= center and abs(z - 3) <= 1
                road |= "north" in outputs and z <= 3 and abs(x - center) <= 1
                road |= "south" in outputs and z >= 3 and abs(x - center) <= 1
                spur = "east" in side_slots and x >= center and z == 3
                spur |= "north" in side_slots and z <= 3 and x == positions["north"][0]
                spur |= "south" in side_slots and z >= 3 and x == positions["south"][0]
                if extra_decor:
                    spur |= x == extra_decor[0] and (
                        z <= 3 if extra_decor[2] == 0 else z >= 3)
                if road or spur:
                    nbt.set_block(root, (x, 0, z), main_block)
        nbt.jigsaw(root, positions["west"], "west", prefix(kind) + "/street_in",
                   "minecraft:empty", "minecraft:empty")
        for side in outputs:
            nbt.jigsaw(root, positions[side], side, prefix(kind) + "/street_out",
                       prefix(kind) + "/street_in", prefix(kind) + "/streets")
        for side, slot in side_slots.items():
            nbt.jigsaw(root, positions[side], side, prefix(kind) + "/" + slot + "_port",
                       road_socket(kind, slot), prefix(kind) + "/" +
                       {"building": "buildings", "decor": "decor", "special": "special_buildings"}[slot])
        if extra_decor:
            side = "north" if extra_decor[2] == 0 else "south"
            nbt.jigsaw(root, extra_decor, side, prefix(kind) + "/decor_port",
                       road_socket(kind, "decor"), prefix(kind) + "/decor")
        nbt.write_template(structures(kind) / "roads" / f"{road_name}.nbt", source_name, root)


def single(kind, location, projection):
    return {"element_type": "minecraft:single_pool_element",
            "location": prefix(kind) + "/" + location,
            "processors": "minecraft:empty", "projection": projection}


def make_pool(kind, entries, fallback="minecraft:empty", projection="rigid"):
    elements = []
    for location, weight in entries:
        element = ({"element_type": "minecraft:empty_pool_element"} if location is None
                   else single(kind, location, projection))
        elements.append({"weight": weight, "element": element})
    return {"fallback": fallback, "elements": elements}


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + "\n", encoding="utf-8")


def write_worldgen(kind):
    center = "pieces/tree" if kind == PLAINS else "town_centers/center_1"
    building_entries = ([(f"pieces/house_{index}", 6) for index in range(1, 10)] +
                        [("pieces/stall_1", 3), ("pieces/stall_2", 3), ("pieces/well", 3)]
                        if kind == PLAINS else
                        [(f"house_{index}", 6) for index in range(1, 9)] + [("camel_shelter", 3)])
    decor_entries = ([(f"pieces/car_{index}", 3) for index in range(1, 4)] +
                     [(f"pieces/lamp_{index}", 3) for index in range(1, 4)] +
                     [("pieces/fence", 4), ("pieces/sand_pile", 2), ("pieces/wood_pile", 2)]
                     if kind == PLAINS else [("hay_pile", 2)])
    special = ("pieces/large_field", 3) if kind == PLAINS else ("pyramid", 1)
    special_empty_weight = 1 if kind == PLAINS else 3
    definitions = {
        "town_centers": make_pool(kind, [(center, 1)]),
        "streets": make_pool(kind, [(f"roads/{road}", weight)
                                    for road, weight in ROAD_WEIGHTS[kind].items()],
                             prefix(kind) + "/terminators", "terrain_matching"),
        "buildings": make_pool(kind, building_entries),
        "decor": make_pool(kind, decor_entries),
        "special_buildings": make_pool(kind, [special, (None, special_empty_weight)]),
        "terminators": make_pool(kind, [("roads/end", 1)], projection="terrain_matching"),
    }
    for name, definition in definitions.items():
        write_json(pools(kind) / f"{name}.json", definition)
    write_json(DATA / "worldgen/structure" / f"{kind}.json", {
        "type": "minecraft:jigsaw", "biomes": f"#until_eternity:has_structure/{kind}",
        "step": "surface_structures", "spawn_overrides": {},
        "terrain_adaptation": "beard_thin", "start_pool": prefix(kind) + "/town_centers",
        "size": 7, "start_height": {"absolute": 0},
        "project_start_to_heightmap": "WORLD_SURFACE_WG",
        "max_distance_from_center": 112, "use_expansion_hack": True})
    write_json(DATA / "worldgen/structure_set" / f"{kind}.json", {
        "structures": [{"structure": prefix(kind), "weight": 1}],
        "placement": {"type": "minecraft:random_spread", "spacing": 34,
                      "separation": 8, "salt": 172839451 if kind == PLAINS else 172839452}})
    if kind == DESERT:
        write_json(DATA / "tags/worldgen/biome/has_structure/desert_settlement.json",
                   {"replace": False, "values": ["minecraft:desert"]})


def verify():
    required_pool_names = {"town_centers", "streets", "buildings", "decor",
                           "special_buildings", "terminators"}
    for kind in KINDS:
        structure = json.loads((DATA / "worldgen/structure" / f"{kind}.json").read_text(encoding="utf-8"))
        structure_set = json.loads((DATA / "worldgen/structure_set" / f"{kind}.json").read_text(encoding="utf-8"))
        tag = json.loads((DATA / "tags/worldgen/biome/has_structure" / f"{kind}.json").read_text(encoding="utf-8"))
        assert structure["start_pool"] == prefix(kind) + "/town_centers"
        assert structure["size"] == 7 and structure["max_distance_from_center"] == 112
        assert structure["start_height"] == {"absolute": 0} and structure["use_expansion_hack"] is True
        assert structure["biomes"] == f"#until_eternity:has_structure/{kind}"
        assert tag["values"] == ["minecraft:plains" if kind == PLAINS else "minecraft:desert"]
        placement = structure_set["placement"]
        assert (placement["spacing"], placement["separation"], placement["salt"]) == (
            34, 8, 172839451 if kind == PLAINS else 172839452)
        assert {path.stem for path in pools(kind).glob("*.json")} == required_pool_names
        loaded = {}
        for path in pools(kind).glob("*.json"):
            definition = json.loads(path.read_text(encoding="utf-8"))
            fallback = definition["fallback"]
            assert fallback == "minecraft:empty" or fallback == prefix(kind) + "/terminators"
            for item in definition["elements"]:
                assert item["weight"] > 0
                element = item["element"]
                if element["element_type"] == "minecraft:empty_pool_element":
                    continue
                location = element["location"]
                assert location.startswith(prefix(kind) + "/"), location
                target = structures(kind) / (location.removeprefix(prefix(kind) + "/") + ".nbt")
                assert target.is_file(), target
                assert element["projection"] == ("terrain_matching" if path.stem in ("streets", "terminators") else "rigid")
            loaded[path.stem] = definition
        street_weights = {
            item["element"]["location"].removeprefix(prefix(kind) + "/roads/"): item["weight"]
            for item in loaded["streets"]["elements"]}
        assert street_weights == ROAD_WEIGHTS[kind]
        building_weights = {
            item["element"]["location"].split("/")[-1]: item["weight"]
            for item in loaded["buildings"]["elements"]}
        house_count = 9 if kind == PLAINS else 8
        assert all(building_weights[f"house_{index}"] == 6 for index in range(1, house_count + 1))
        if kind == PLAINS:
            assert all(building_weights[name] == 3 for name in ("stall_1", "stall_2", "well"))
        else:
            assert building_weights["camel_shelter"] == 3
        decor_weights = {
            item["element"]["location"].split("/")[-1]: item["weight"]
            for item in loaded["decor"]["elements"]}
        if kind == PLAINS:
            assert decor_weights["fence"] == 4
            assert decor_weights["sand_pile"] == decor_weights["wood_pile"] == 2
            assert all(decor_weights[f"car_{index}"] == decor_weights[f"lamp_{index}"] == 3
                       for index in range(1, 4))
        else:
            assert decor_weights == {"hay_pile": 2}
        special = loaded["special_buildings"]["elements"]
        assert len(special) == 2
        assert special[0]["element"]["location"].endswith(
            "/large_field" if kind == PLAINS else "/pyramid")
        assert special[0]["weight"] == (3 if kind == PLAINS else 1)
        assert special[1]["element"]["element_type"] == "minecraft:empty_pool_element"
        assert special[1]["weight"] == (1 if kind == PLAINS else 3)
        center_path = (piece_path(kind, "tree") if kind == PLAINS else
                       structures(kind) / "town_centers/center_1.nbt")
        center = nbt.read_template(center_path)[1]
        center_ports = jigsaw_blocks(center)
        assert len(center_ports) == 4
        center_bottom = [block for block in nbt.blocks(center) if nbt.position(block)[1] == 0]
        assert len(center_bottom) == nbt.size(center)[0] * nbt.size(center)[2]
        assert all(nbt.block_name(center, block) != "minecraft:air" for block in center_bottom)
        assert {nbt.field(nbt.palette(center)[nbt.field(block, "state")].value["Properties"],
                          "orientation").removesuffix("_up") for block in center_ports} == {
                              "north", "south", "east", "west"}
        assert all(nbt.field(block.value["nbt"], "pool") == prefix(kind) + "/streets"
                   for block in center_ports)
        names = PLAINS_PIECES if kind == PLAINS else DESERT_PIECES
        for name in names:
            root = nbt.read_template(piece_path(kind, name))[1]
            bottom = [block for block in nbt.blocks(root) if nbt.position(block)[1] == 0]
            assert len(bottom) == nbt.size(root)[0] * nbt.size(root)[2], name
            assert all(nbt.block_name(root, block) != "minecraft:air" for block in bottom), name
            ports = jigsaw_blocks(root)
            assert len(ports) == 1, name
            slot = ("special" if name in ("large_field", "pyramid") else
                    "decor" if name in (PLAINS_DECOR if kind == PLAINS else ["hay_pile"]) else "building")
            assert nbt.field(ports[0].value["nbt"], "name") == road_socket(kind, slot), name
            assert nbt.field(ports[0].value["nbt"], "pool") == "minecraft:empty", name
            assert nbt.field(ports[0].value["nbt"], "target") == "minecraft:empty", name
        for road_name, (width, outputs, slots) in ROAD_SPECS.items():
            road = nbt.read_template(structures(kind) / "roads" / f"{road_name}.nbt")[1]
            assert nbt.size(road) == [width, 3, 7]
            ports = jigsaw_blocks(road)
            assert len(ports) == 1 + len(outputs) + len(slots) + (1 if width == 11 else 0), road_name
            assert sum(nbt.field(block.value["nbt"], "name") == prefix(kind) + "/street_out"
                       for block in ports) == len(outputs), road_name
            for block in ports:
                tag = block.value["nbt"]
                connector = nbt.field(tag, "name")
                if connector == prefix(kind) + "/street_out":
                    assert nbt.field(tag, "pool") == prefix(kind) + "/streets"
                    assert nbt.field(tag, "target") == prefix(kind) + "/street_in"
                elif connector.endswith("_port"):
                    slot = connector.removeprefix(prefix(kind) + "/").removesuffix("_port")
                    assert nbt.field(tag, "target") == road_socket(kind, slot)
                    assert nbt.field(tag, "pool") == prefix(kind) + "/" + {
                        "building": "buildings", "decor": "decor", "special": "special_buildings"}[slot]
            assert all(not nbt.field(block.value["nbt"], "pool").startswith(prefix(DESERT if kind == PLAINS else PLAINS))
                       for block in ports), road_name
        assert not any(nbt.field(block.value["nbt"], "pool") == prefix(kind) + "/streets"
                       for block in jigsaw_blocks(nbt.read_template(structures(kind) / "roads/end.nbt")[1]))
        assert len(loaded["buildings"]["elements"]) == (12 if kind == PLAINS else 9)
    print("Both settlement graphs, NBT floors, Jigsaws, pools, biomes and placements verified")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--verify", action="store_true")
    args = parser.parse_args()
    if args.verify:
        verify()
        return
    update_plains_pieces()
    update_desert_pieces()
    create_desert_center()
    for kind in KINDS:
        create_roads(kind)
        write_worldgen(kind)
    verify()


if __name__ == "__main__":
    main()
