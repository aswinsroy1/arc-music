with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

new_content = content.replace(
"""                        item {
                            ListeningStatsSection()
                        }""",
"""                        item {
                            CollectionHealthSection()
                        }
                        item {
                            ListeningStatsSection()
                        }"""
)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(new_content)
