import os
import re

directory = '.'
pattern = re.compile(r'(Scaffold\([\s\S]*?)containerColor\s*=\s*Color\.Transparent', re.MULTILINE)

for filename in os.listdir(directory):
    if filename.endswith('Screen.kt'):
        with open(filename, 'r', encoding='utf-8') as f:
            content = f.read()
        
        new_content = pattern.sub(r'\1containerColor = MaterialTheme.colorScheme.background', content)
        
        if new_content != content:
            with open(filename, 'w', encoding='utf-8') as f:
                f.write(new_content)
            print(f'Updated {filename}')
