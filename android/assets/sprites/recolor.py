import os

from PIL import Image

sidekicks_rgb = [
    (110, 80, 235),  # SEAN
    (90, 190, 235),  # ZYRAME
    (220, 60, 40),  # RED
    (180, 235, 60),  # MIMAPS
    (50, 235, 215),  # PAINTER
    (215, 50, 100),  # XERETH
    (220, 235, 160),  # BOMBER
    (40, 50, 60),
    (180, 200, 220),
    (100, 110, 170),  # JIM
    (50, 180, 180),  # UNI
    (235, 150, 130),  # SNIPER
    (50, 100, 225),
    (175, 225, 235),
    (175, 175, 235),
    (235, 175, 235),
    (175, 235, 175),
    (120, 225, 100),
    (235, 180, 60),
    (220, 70, 180),
    (75, 75, 115),
]


class ColorSet:
    def __init__(self, rgb, code):
        self.colors = {}
        self.rgb = rgb
        self.code = code
        (r, g, b) = rgb

        for color in rgb:
            if color < 20 or 235 < color:
                print("   INVALID COLOR: rgb(%d,%d,%d)" % rgb)
                exit(1)

        self.colors['principal'] = rgb
        self.colors['outline'] = (r - 20, g - 20, b - 20)
        self.colors['shade'] = (r - 10, g - 10, b - 10)
        self.colors['mouth'] = (r // 2, g // 2, b // 2)

        self.colors['flash-filling'] = (r + 20, g + 20, b + 20)
        self.colors['flash-outline'] = rgb
        self.colors['flash-shade'] = (r + 10, g + 10, b + 10)

        for key in self.colors:
            self.colors[key] += (255,)

    def folder_name(self):
        return "color%d" % self.code

    def code_str(self):
        return str(self.code)

    def color(self, key):
        return self.colors[key]

    def __iter__(self):
        return self.colors.__iter__()


def rgb_to_hexa(c):
    # converts an rgb tuple to an hexadecimal code string
    return '#%02x%02x%02x' % (c[0], c[1], c[2])


def hex_to_rgb(value):
    # converts an hexadecimal code string to  an rgb tuple
    value = value.lstrip('#')
    lv = len(value.lstrip('#'))
    return tuple(int(value[i:i + lv // 3], 16) for i in range(0, lv, lv // 3)) + (255,)


def all_paths_of_set(path: str, color_set: ColorSet):
    # returns a list of paths of all .png files that can be found
    # in the [path]/ folder (recursive search)
    paths = []
    for (root, doss, files) in (os.walk("./%s" % path)):
        if "" in root.split(color_set.folder_name()):
            for file in files:
                paths.append(os.path.join(root, file).replace("\\", "/"))
    return paths


def new_path(path: str, color_set: ColorSet):
    # returns an equivalent destination path of the file for the code [code]
    return "/".join(
        [(color_set.folder_name() if "color" in split else split)
         for split in path.split("/")]
    )


def replace_colors(pix, set1: ColorSet, set2: ColorSet):
    # replace all pix's pixels of colors from set1 with colors from set2 of same key
    for i in range(64):
        for j in range(64):
            for key in set1:
                if set1.color(key) == pix[i, j]:
                    pix[i, j] = set2.color(key)


def generate_new_color_folder(set1: ColorSet, set2: ColorSet):
    paths = all_paths_of_set("caps", set1) + all_paths_of_set("germs", set1)
    total = len(paths)

    for i, path in enumerate(paths):
        dst_path = new_path(path, set2)

        img = Image.open(path)
        pix = img.load()

        replace_colors(pix, set1, set2)

        basedir = os.path.dirname(dst_path)
        if not os.path.exists(basedir):
            os.makedirs(basedir)

        open(dst_path, 'w').close()
        img.save(dst_path)

        print(
            "[", "{:.1f}".format((i + 1) * 100 / total), "% ", "exported... ]",
            dst_path, end="\r"
        )


if __name__ == "__main__":
    default = ColorSet(sidekicks_rgb[0], 1)
    output = [ColorSet(sidekicks_rgb[i], i + 1) for i, sidekick
              in enumerate(sidekicks_rgb)][12:]

    for sdk_color_set in output:
        generate_new_color_folder(default, sdk_color_set)
        print(sdk_color_set.folder_name(), "done !")
