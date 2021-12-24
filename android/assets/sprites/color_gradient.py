src = (175, 150, 250)
dest = (250, 85, 160)

def value(index, step, n):
    return int(src[index] + (dest[index] - src[index]) * step / n)

def print_steps(n):
    for step in range(0, n+1):
        new_color = (value(0, step, n),
                     value(1, step, n),
                     value(2, step, n))
        print(new_color)


print_steps(6)
