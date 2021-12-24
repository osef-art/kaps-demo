from PIL import Image
import os

sidekicks = [
    (110, 80, 235),  # SEAN
    (90, 190, 235),  # ZYRAME
    (220, 60, 40),  # RED
    (180, 235, 60),  # MIMAPS
    (50, 235, 215),  # PAINT
    (215, 50, 100),  # XERETH
    (220, 235, 160),  # SIDEKICK_7
    (40, 50, 60),  # SIDEKICK_8
    (180, 200, 220),  # SIDEKICK_9
    (100, 110, 170),  # JIM
    (50, 180, 180),  # COLOR
    (235, 150, 130),  # SIDEKICK_12
    (70, 50, 130)  # SIDEKICK_13
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
        self.colors['mouth'] = (int(r / 2), int(g / 2), int(b / 2))

        self.colors['flash-filling'] = (r + 20, g + 20, b + 20)
        self.colors['flash-outline'] = rgb
        self.colors['flash-shade'] = (r + 10, g + 10, b + 10)

        for key in self.colors:
            self.colors[key] += (255,)

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


def all_paths(code):
    # returns a list of paths of all .png files that can be found
    # in the [code]/ folder (recursive search)
    paths = []
    for (root, doss, files) in (os.walk("./%d" % code)):
        for file in files:
            paths.append(os.path.join(root, file).replace("\\", "/"))
    return paths


def replace_colors(pix, set1, set2):
    # replace all pix's pixels of colors from set1 with colors from set2 of same key
    for i in range(64):
        for j in range(64):
            for key in set1:
                if set1.color(key) == pix[i, j]:
                    pix[i, j] = set2.color(key)


def generate_new_color_folder(set1, set2):
    set1: ColorSet
    set2: ColorSet
    paths = [files for (root, doss, files) in os.walk("./1/germs/wall1")][0]
    paths = ["./1/germs/wall1/" + file for file in paths]
    paths = all_paths(set1.code)
    total = len(paths)

    for i, path in enumerate(paths):
        tmp = path.split(set1.code_str())
        dst_path = set2.code_str().join(tmp[:2])
        if len(tmp) > 2:
            dst_path += set1.code_str() + set1.code_str().join(tmp[2:])

        im = Image.open(path)
        pix = im.load()

        replace_colors(pix, set1, set2)

        basedir = os.path.dirname(dst_path)
        if not os.path.exists(basedir):
            os.makedirs(basedir)
        open(dst_path, 'w').close()
        im.save(dst_path)

        print("[", "{:.1f}".format((i + 1) * 100 / total), "% ", "exported... ]", dst_path, end="\r")


if __name__ == "__main__":
    default = ColorSet(sidekicks[0], 1)
    output = [ColorSet(sidekicks[i], i+1) for i, sidekick in enumerate(sidekicks)][1:]

    for colorset in output:
        generate_new_color_folder(default, colorset)
