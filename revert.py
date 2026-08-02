with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace("modifier = Modifier.padding(horizontal = 24.dp).clickable { onClick() }", "modifier = Modifier.padding(horizontal = 24.dp)")

# Now fix CollectionHealthSection specifically
start_idx = content.find("fun CollectionHealthSection(")
end_idx = content.find("GlassCard(", start_idx)

# We want to replace the FIRST GlassCard after CollectionHealthSection
glass_card_idx = content.find("GlassCard(", start_idx)
modifier_idx = content.find("modifier = Modifier.padding(horizontal = 24.dp)", glass_card_idx)
content = content[:modifier_idx] + "modifier = Modifier.padding(horizontal = 24.dp).clickable { onClick() }" + content[modifier_idx+len("modifier = Modifier.padding(horizontal = 24.dp)"):]

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

