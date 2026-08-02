def fix_file(filepath):
    content = open(filepath).read()
    if content.startswith("import androidx.lifecycle.viewmodel.compose.viewModel\npackage com.example"):
        content = content.replace("import androidx.lifecycle.viewmodel.compose.viewModel\npackage com.example", "package com.example\nimport androidx.lifecycle.viewmodel.compose.viewModel")
        with open(filepath, "w") as f:
            f.write(content)

fix_file("app/src/main/java/com/example/GenreHubScreen.kt")
fix_file("app/src/main/java/com/example/LibraryComponents.kt")
