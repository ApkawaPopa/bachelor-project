import os


def print_filenames(directory):
    for root, _, files in os.walk(directory):
        for file_name in files:
            print(f"    '{root}\\{file_name}': 1,".replace('\\', '\\\\'))


if __name__ == '__main__':
    print_filenames(directory=r'C:\Users\ark13\MPU\Диплом\amina\frontend\src')
