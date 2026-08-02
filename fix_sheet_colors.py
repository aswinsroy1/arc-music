import re

with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'r') as f:
    content = f.read()

start_idx = content.find("ModalBottomSheet(")
end_idx = content.find("}\n    }", start_idx)

if start_idx != -1 and end_idx != -1:
    old_sheet = content[start_idx:end_idx + 1]
    
    new_sheet = old_sheet.replace(
        "color = textColor", "color = MaterialTheme.colorScheme.onSurface"
    ).replace(
        "color = textColor.copy(alpha = 0.6f)", "color = MaterialTheme.colorScheme.onSurfaceVariant"
    ).replace(
        "color = textColor.copy(alpha = 0.1f)", "color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)"
    ).replace(
        "tint = textColor.copy(alpha = 0.7f)", "tint = MaterialTheme.colorScheme.onSurfaceVariant"
    )
    
    # We removed Download option so hasToggle is always false now, but I can leave the switch logic or just let the colors be replaced.
    # Replace checkedTrackColor = textColor
    new_sheet = new_sheet.replace(
        "checkedTrackColor = textColor", "checkedTrackColor = MaterialTheme.colorScheme.primary"
    )
    
    content = content[:start_idx] + new_sheet + content[end_idx + 1:]
    
    with open('app/src/main/java/com/example/NowPlayingScreen.kt', 'w') as f:
        f.write(content)
    print("Done")
else:
    print("Not found")
