import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace(
    "CollectionGrowthScreen(\n                                    onNavigateBack = { navController.popBackStack() }\n                                )",
    "CollectionGrowthScreen(\n                                    onNavigateBack = { navController.popBackStack() },\n                                    glowIntensity = glowIntensity\n                                )"
)

content = content.replace(
    "CollectionHealthScreen(\n                                    onNavigateBack = { navController.popBackStack() }\n                                )",
    "CollectionHealthScreen(\n                                    onNavigateBack = { navController.popBackStack() },\n                                    glowIntensity = glowIntensity\n                                )"
)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
