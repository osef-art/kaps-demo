import os

"""
SPRITES ARBORESCENCE:

sprites/
    caps/
        [:kind]/
            color?/
                [:orientation].png
        color?/
            [:orientation].png
            pop_?.png
    germs/
        [:kind]/
            color?/
                idle_?.png
                pop_?.png
        wall/
            level?/
                color?/
                    idle_?.png
                    pop_?.png
    
"""


def is_file(filename):
    return "." in filename


def is_dir(filename):
    return not is_file(filename)


def dir_list(path):
    return [path + doss + "/" for doss in os.listdir(path) if is_dir(doss)]


def file_list(path):
    return [path + f for f in os.listdir(path) if is_file(f)]


def rename(path, src, dst):
    os.rename(path + src, path + dst)


root = "./germs/"
folders = dir_list(root)

for wall_folder in folders:
    if wall_folder.split("/")[-2] == "wall":
        for level_folder in dir_list(wall_folder):
            # os.mkdir(level_folder + "color0/")
            for file in file_list(level_folder):
                pass
                # print(level_folder + file.split("/")[-1], level_folder + "color0/" +
                # file.split("/")[-1]) rename(level_folder, file.split("/")[-1],
                # "color0/" + file.split("/")[-1])
