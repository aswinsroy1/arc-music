content = open("app/src/main/java/com/example/GenreHubScreen.kt").read()
if "import androidx.lifecycle.viewmodel.compose.viewModel" not in content:
    content = "import androidx.lifecycle.viewmodel.compose.viewModel\n" + content
with open("app/src/main/java/com/example/GenreHubScreen.kt", "w") as f:
    f.write(content)
