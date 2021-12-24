import os

def is_file(filename):
    return "." in filename


def is_doss(filename):
    return not is_file(filename)


def doss_list(path):
    return [path + doss + "/" for doss in os.listdir(path) if is_doss(doss)]


def file_list(path):
    return [path + doss for doss in os.listdir(path) if is_file(doss)]


def rename(path, src, dst):
    os.rename(path + src, path + dst)


def create_13_doss(path):
    [os.mkdir(path + "/" + str(i)) for i in range(1, 13 + 1)]


def create_13_doss_in(path, new):
    [os.mkdir(path + "/" + str(i) + "/" + new) for i in range(1, 13 + 1)]


def move_in_doss(path, file):
    # moves [num]-filename into '[num]/' folder
    rename(path, file, file.replace("-", "/"))


root = "./1/"
files = file_list(root)

for file in files:
    print(file, file.replace("atk/", "").replace("_", "/atk_"))