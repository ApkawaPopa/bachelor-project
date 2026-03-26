import os


def print_filenames(directory):
    for root, _, files in os.walk(directory):
        for file_name in files:
            print(f"    r'{root}\\{file_name}',")


if __name__ == '__main__':
    print_filenames(directory=r'C:\Users\ark13\MPU\Диплом\amina\frontend\src')
