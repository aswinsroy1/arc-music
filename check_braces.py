with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    lines = f.readlines()

count = 0
for i, line in enumerate(lines):
    if line.startswith('@Composable'):
        print(f"Line {i+1}: {line.strip()} (Current depth: {count})")
    count += line.count('{') - line.count('}')
    if count == 0 and i > 40:
        print(f"Braces balanced at line {i+1}")
