with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'PlaylistResultItem(onClick = onNavigateToPlaylistDetails, title = "This Is Conan Gray", "50 songs", "https://lh3.googleusercontent.com/aida-public/AB6AXuAN2IRvdhR_bkbDMJ8TqVqG2y4rQ3L_f1eRHAi_gAsteuGmZIEL0FgQOJ7eagpNOd8RyZJEar704LgA2EaC_eTYUgCmOiMzxQpaVHQJ8M6flPFUIJ71YUtKiIKyo6UAJW1DAoztixz1AbUm17RLgT5jOGGPPEHgGrkOwf0WxcPSFfkL_vvYISYxu70oxAu8dCHUOY7vYG371MR9sBrRRiEMLJ30KgLLrV0hg9_zUe8qs294_IXoKmQpLvb3X8pgvDIEYaT6kFzuQnqG")',
    'PlaylistResultItem(title = "This Is Conan Gray", subtitle = "50 songs", imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAN2IRvdhR_bkbDMJ8TqVqG2y4rQ3L_f1eRHAi_gAsteuGmZIEL0FgQOJ7eagpNOd8RyZJEar704LgA2EaC_eTYUgCmOiMzxQpaVHQJ8M6flPFUIJ71YUtKiIKyo6UAJW1DAoztixz1AbUm17RLgT5jOGGPPEHgGrkOwf0WxcPSFfkL_vvYISYxu70oxAu8dCHUOY7vYG371MR9sBrRRiEMLJ30KgLLrV0hg9_zUe8qs294_IXoKmQpLvb3X8pgvDIEYaT6kFzuQnqG", onClick = onNavigateToPlaylistDetails)'
)

content = content.replace(
    'PlaylistResultItem(onClick = onNavigateToPlaylistDetails, title = "Conan Gray Complete", "92 songs", "https://lh3.googleusercontent.com/aida-public/AB6AXuBoqgZeT99UHwbjmcDIfQ71twNukb3Tzd6G0dsOGEVmLDnD0HXbeipJGkmNY9O9jEZmz2oFg0vvlWWcin0dGwUVfoPwskoe8c4oYobJajOAkkxzb37yRJyhMINPt8VjjLF_6LNVXoE0lMjq4rRHPngOSRTiUsB4XfaYdshllx1R9y55Lfv6Texk356Nj71-a8U5r8eU33_lhmgL7cxyPCDo13d9oHJOiqTN2K8-aKMogfnmKcgzCNBO_-ctALvKFyota4_U73THmnSM")',
    'PlaylistResultItem(title = "Conan Gray Complete", subtitle = "92 songs", imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBoqgZeT99UHwbjmcDIfQ71twNukb3Tzd6G0dsOGEVmLDnD0HXbeipJGkmNY9O9jEZmz2oFg0vvlWWcin0dGwUVfoPwskoe8c4oYobJajOAkkxzb37yRJyhMINPt8VjjLF_6LNVXoE0lMjq4rRHPngOSRTiUsB4XfaYdshllx1R9y55Lfv6Texk356Nj71-a8U5r8eU33_lhmgL7cxyPCDo13d9oHJOiqTN2K8-aKMogfnmKcgzCNBO_-ctALvKFyota4_U73THmnSM", onClick = onNavigateToPlaylistDetails)'
)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
