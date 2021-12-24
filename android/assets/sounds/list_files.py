import os

for (root, doss, files) in os.walk("./"):
    for file in files:
        print("%s(\"%s\")," % (file.upper().split(".")[0], file.split(".")[0]))