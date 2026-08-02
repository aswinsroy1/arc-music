import os
import shutil

old_pkg = "com.example"
new_pkg = "com.aeswox.arcmusic"

old_dir_part = old_pkg.replace('.', '/')
new_dir_part = new_pkg.replace('.', '/')

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    new_content = content.replace(old_pkg, new_pkg)
    new_content = new_content.replace('Theme.MyApplication', 'Theme.ArcMusic')
    
    if filepath.endswith('strings.xml'):
        new_content = new_content.replace('>Music<', '>Arc Music<')
        
    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated content in: {filepath}")

def move_directories():
    base_dirs = [
        os.path.join('app', 'src', 'main', 'java'),
        os.path.join('app', 'src', 'test', 'java'),
        os.path.join('app', 'src', 'androidTest', 'java')
    ]
    
    for base in base_dirs:
        old_path = os.path.join(base, *old_pkg.split('.'))
        new_path = os.path.join(base, *new_pkg.split('.'))
        
        if os.path.exists(old_path):
            os.makedirs(new_path, exist_ok=True)
            # move all contents of old_path to new_path
            for item in os.listdir(old_path):
                s = os.path.join(old_path, item)
                d = os.path.join(new_path, item)
                shutil.move(s, d)
            print(f"Moved directory {old_path} to {new_path}")
            
            # optionally remove old_path if empty
            try:
                os.rmdir(old_path)
                os.rmdir(os.path.dirname(old_path)) # try to remove 'com' if empty
            except OSError:
                pass

if __name__ == "__main__":
    for root, dirs, files in os.walk('app'):
        for file in files:
            if file.endswith('.kt') or file.endswith('.xml'):
                process_file(os.path.join(root, file))
                
    move_directories()
    print("Done")
