import gzip
import struct
from dataclasses import dataclass


@dataclass(eq=True)
class Tag:
    kind: int
    value: object


class Reader:
    def __init__(self, data):
        self.data = data
        self.offset = 0

    def take(self, length):
        result = self.data[self.offset:self.offset + length]
        if len(result) != length:
            raise ValueError("truncated NBT")
        self.offset += length
        return result

    def number(self, fmt):
        return struct.unpack(">" + fmt, self.take(struct.calcsize(fmt)))[0]

    def string(self):
        return self.take(self.number("H")).decode("utf-8")

    def payload(self, kind):
        if kind in (1, 2, 3, 4, 5, 6):
            return Tag(kind, self.number({1: "b", 2: "h", 3: "i", 4: "q", 5: "f", 6: "d"}[kind]))
        if kind == 7:
            return Tag(kind, self.take(self.number("i")))
        if kind == 8:
            return Tag(kind, self.string())
        if kind == 9:
            item_kind = self.number("B")
            size = self.number("i")
            return Tag(kind, (item_kind, [self.payload(item_kind) for _ in range(size)]))
        if kind == 10:
            entries = {}
            while item_kind := self.number("B"):
                name = self.string()
                entries[name] = self.payload(item_kind)
            return Tag(kind, entries)
        if kind in (11, 12):
            fmt = "i" if kind == 11 else "q"
            return Tag(kind, [self.number(fmt) for _ in range(self.number("i"))])
        raise ValueError(f"unsupported NBT type {kind}")


def pack(fmt, *values):
    return struct.pack(">" + fmt, *values)


def string(value):
    data = value.encode("utf-8")
    return pack("H", len(data)) + data


def payload(tag):
    kind, value = tag.kind, tag.value
    if kind in (1, 2, 3, 4, 5, 6):
        return pack({1: "b", 2: "h", 3: "i", 4: "q", 5: "f", 6: "d"}[kind], value)
    if kind == 7:
        return pack("i", len(value)) + value
    if kind == 8:
        return string(value)
    if kind == 9:
        item_kind, values = value
        return pack("Bi", item_kind, len(values)) + b"".join(payload(item) for item in values)
    if kind == 10:
        return b"".join(pack("B", item.kind) + string(name) + payload(item)
                        for name, item in value.items()) + b"\0"
    if kind in (11, 12):
        fmt = "i" if kind == 11 else "q"
        return pack("i", len(value)) + b"".join(pack(fmt, item) for item in value)
    raise ValueError(f"unsupported NBT type {kind}")


def decode(data):
    reader = Reader(data)
    kind = reader.number("B")
    name = reader.string()
    root = reader.payload(kind)
    if reader.offset != len(data):
        raise ValueError("trailing NBT bytes")
    return name, root


def encode(name, root):
    return pack("B", root.kind) + string(name) + payload(root)


def compound(**entries):
    return Tag(10, entries)


def text(value):
    return Tag(8, value)


def integer(value):
    return Tag(3, value)


def list_tag(kind, values):
    return Tag(9, (kind, values))


def read_template(path):
    raw = gzip.decompress(path.read_bytes())
    name, root = decode(raw)
    if decode(encode(name, root)) != (name, root):
        raise ValueError(f"NBT round-trip failed: {path}")
    return name, root


def write_template(path, name, root):
    raw = encode(name, root)
    if decode(raw) != (name, root):
        raise ValueError(f"modified NBT round-trip failed: {path}")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(gzip.compress(raw, mtime=0))


def field(tag, key):
    return tag.value[key].value


def palette(root):
    return field(root, "palette")[1]


def blocks(root):
    return field(root, "blocks")[1]


def size(root):
    return [item.value for item in field(root, "size")[1]]


def position(block):
    return [item.value for item in field(block, "pos")[1]]


def block_name(root, block):
    return field(palette(root)[field(block, "state")], "Name")


def block_state_string(root, block):
    state = palette(root)[field(block, "state")]
    name = field(state, "Name")
    properties = state.value.get("Properties")
    if not properties:
        return name
    values = ",".join(f"{key}={value.value}" for key, value in properties.value.items())
    return f"{name}[{values}]"


def state_index(root, name, properties=None):
    properties = properties or {}
    for index, state in enumerate(palette(root)):
        if field(state, "Name") == name and (
            {key: value.value for key, value in state.value.get("Properties", compound()).value.items()}
            == properties
        ):
            return index
    state = compound(Name=text(name))
    if properties:
        state.value["Properties"] = compound(**{key: text(value) for key, value in properties.items()})
    palette(root).append(state)
    return len(palette(root)) - 1


def at(root, pos):
    return next((block for block in blocks(root) if position(block) == list(pos)), None)


def set_block(root, pos, state, block_entity=None, only_air=False):
    existing = at(root, pos)
    if existing is not None:
        if only_air and block_name(root, existing) != "minecraft:air":
            raise ValueError(f"non-air block at {pos}: {block_name(root, existing)}")
        blocks(root).remove(existing)
    elif only_air and not (0 <= pos[0] < size(root)[0] and 0 <= pos[1] < size(root)[1]
                            and 0 <= pos[2] < size(root)[2]):
        raise ValueError(f"out-of-range port {pos}")
    item = compound(pos=list_tag(3, [integer(value) for value in pos]), state=integer(state))
    if block_entity is not None:
        item.value["nbt"] = block_entity
    blocks(root).append(item)


def jigsaw(root, pos, facing, name, target, pool, final_state="minecraft:air", only_air=True):
    index = state_index(root, "minecraft:jigsaw", {"orientation": facing + "_up"})
    data = compound(id=text("minecraft:jigsaw"), name=text(name), target=text(target),
                    pool=text(pool), final_state=text(final_state), joint=text("aligned"))
    set_block(root, pos, index, data, only_air=only_air)


def open_edge(root, side, preferred):
    width, _, depth = size(root)
    if side in ("north", "south"):
        options = [(x, 1, 0 if side == "north" else depth - 1) for x in range(1, width - 1)]
    else:
        options = [(0 if side == "west" else width - 1, 1, z) for z in range(1, depth - 1)]
    def score(pos):
        block = at(root, pos)
        name = block_name(root, block) if block else "minecraft:air"
        penalty = 0 if name == "minecraft:air" else (
            1 if any(part in name for part in ("fence_gate", "trapdoor", "fence", "leaves",
                                                "stairs", "slab", "dirt_path")) else 10)
        coordinate = pos[0] if side in ("north", "south") else pos[2]
        return abs(coordinate - preferred) * 2 + penalty
    return min(options, key=score)


def choose_entrance(root):
    width, _, depth = size(root)
    doors = []
    for block in blocks(root):
        state = palette(root)[field(block, "state")]
        name = field(state, "Name")
        if name.endswith("_door") and not name.endswith("trapdoor"):
            properties = {key: value.value for key, value in state.value.get("Properties", compound()).value.items()}
            if properties.get("half") == "lower":
                doors.append((position(block), properties.get("facing")))
    if doors:
        (x, _, z), facing = doors[0]
        distances = {"north": z, "south": depth - 1 - z,
                     "west": x, "east": width - 1 - x}
        side = min(distances, key=lambda candidate: distances[candidate] +
                   (0 if candidate == facing else 2))
        return side, x if side in ("north", "south") else z
    candidates = []
    for side, preferred in (("north", width // 2), ("south", width // 2),
                            ("west", depth // 2), ("east", depth // 2)):
        try:
            pos = open_edge(root, side, preferred)
            candidates.append((abs((pos[0] if side in ("north", "south") else pos[2]) - preferred),
                               side, preferred))
        except ValueError:
            pass
    if not candidates:
        raise ValueError("building has no open boundary")
    _, side, preferred = min(candidates)
    return side, preferred


def verify():
    import rebuild_settlements
    return rebuild_settlements.verify()


def main():
    import rebuild_settlements
    rebuild_settlements.main()


if __name__ == "__main__":
    main()
