import re
with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

# Hoist state logic
hoisted_state = """    val playlists = remember { mutableStateListOf(
        Track("This Is Conan Gray", "50 songs", "https://lh3.googleusercontent.com/aida-public/AB6AXuAN2IRvdhR_bkbDMJ8TqVqG2y4rQ3L_f1eRHAi_gAsteuGmZIEL0FgQOJ7eagpNOd8RyZJEar704LgA2EaC_eTYUgCmOiMzxQpaVHQJ8M6flPFUIJ71YUtKiIKyo6UAJW1DAoztixz1AbUm17RLgT5jOGGPPEHgGrkOwf0WxcPSFfkL_vvYISYxu70oxAu8dCHUOY7vYG371MR9sBrRRiEMLJ30KgLLrV0hg9_zUe8qs294_IXoKmQpLvb3X8pgvDIEYaT6kFzuQnqG"),
        Track("Conan Gray Complete", "92 songs", "https://lh3.googleusercontent.com/aida-public/AB6AXuBoqgZeT99UHwbjmcDIfQ71twNukb3Tzd6G0dsOGEVmLDnD0HXbeipJGkmNY9O9jEZmz2oFg0vvlWWcin0dGwUVfoPwskoe8c4oYobJajOAkkxzb37yRJyhMINPt8VjjLF_6LNVXoE0lMjq4rRHPngOSRTiUsB4XfaYdshllx1R9y55Lfv6Texk356Nj71-a8U5r8eU33_lhmgL7cxyPCDo13d9oHJOiqTN2K8-aKMogfnmKcgzCNBO_-ctALvKFyota4_U73THmnSM")
    ) }
    val albums = remember { mutableStateListOf(
        Track("Wishbone", "2024", "https://lh3.googleusercontent.com/aida-public/AB6AXuCJx6j_t12Lixrr_kYaU8GRKRWceLe29sZI9YzJc0GLeyahwMHKbEC0ARLLWL-0RmYqBHjxwmZzgNqNyRZsCh8PjVOmyd_6FpltGB2ZT_2jSJjOT8ipdJzKfCUS1h3RWY2qsxJWOi3EOD8t0KBaOzpsuut79QihAF69rulfi88J3UM0uEC9UWv59NcUIbAA4flMoQKK67G82bGMK3o8oiFXpsKMS4SmVxG6s9JjrZ_ulmWltpRwT18rZusm2Ui_DldbXtXHUGkiFqXA"),
        Track("Found Heaven", "2024", "https://lh3.googleusercontent.com/aida-public/AB6AXuCODHaqT8FgGkmll_uWxqILgVV36ouYMwPInauRKx6wBVbadKgCbI6Gu2l-6tMLWgUewsi722Aj7grQhFcjobgqH-rvv97f3l73MGicKm6UZaN__B7Y4wqGzfDLgO5_e4tkkT6iz8fh0i9NhWceHQZplyQvKA0rQORrodWwsg0VlqVGSU6-fCQd4hI0EvZxWW2QLYHdPOrvqch7wmytx-dgCjuGXDkV84Xii0_Zh8bO5Yw_k-Cr8vywNKEVW-avcnusLhxNXdjHMyUk"),
        Track("Sunset Season", "2018", "https://lh3.googleusercontent.com/aida-public/AB6AXuAuiVAn7oAHL_KdHRDZ4mLsedDYz5p4qbtHc7i_mCkC0sA1AcSlpcPOr4JG3zFqISryKhW0xw9DLg1VH7v0I-grVZxCKOdRpXOS8JF45Bvtdp7ttKl94_O8zSrGE8UjsHGAckPBHvT2sEUBjTUkbW2UgStiAr4UoKUtUjH7ipmJnciqe2EY_-eHJwV3Hynr3KoJ3KA0ETbkuPiwl5WSrrAxcTQUOZHcm6ex1VvaLO4cvd6ps0DWAazvY0O2hvX7HmUc9z2-64-f7JJP"),
        Track("Kid Krow", "2020", "https://lh3.googleusercontent.com/aida-public/AB6AXuBk6_5E-fMV7ZV0GQOXs1uNdR1fVPPFyMpsyDIKr5xnx03lD7qROZ9040-4FKtgbLwRJU6pgwCljp2F8FRqt4UcKC7URBzbrumdteldMiK-cwFRCschfM1rZxbS4IdeQvOTMHpkcKjyjRAjCjCZ6ORIByQiK94f35mOI8W2Edo3WPDTwKK3iC9w3BLVgM7Ic8-JOHLJMF9eERBzYRc6gRFXpi5zXS5z1nWtwrx_GK6nQN6_HlPe8u2i2xBf0JjvQEnUyqbyP3zviVCL")
    ) }
    val artists = remember { mutableStateListOf(
        Track("Conan Gray", "", "https://lh3.googleusercontent.com/aida/AP1WRLv2WecEYcFjvYBf-M3uEQ_any0wLnlOIbEuk_z6TQbKqKTSZoFVZNYQ-1t8glDuBotg9yeGnMK8FZEE-kgwiLAVRBXzvQimz6mY682dnzbndydZF2E-RtA81Z-B73vftEA1FvCkglrC0eRpulttSej5eBpotQsOJDlrWXWG2NcJDqKcgI2WBx09sqJfbw09cTRFbu54vdDLp3z42vq-SHV4IdKULZGUnC2hu9U6zJ1iQMIX1k012Bm-U6I"),
        Track("Olivia Rodrigo", "", "https://lh3.googleusercontent.com/aida-public/AB6AXuDpd4CIX-H8zlbsGd2i6jjWTt-WOc214as4xVDbr4pkrBf_vcjs72oEGP__NvVLxeVuCZNmsfAzwM_mFJCsW66dkwmiPUSShBwrcVCVr1_8E5XqjqNJrNWazqWkCebFeEIm07N4NFFTxz6sTcuXVmJ-NWCgOZ0IQXHH_Ke7iK75l9IABaK3cAE_DuxPtHifeZyY7VTzSf5OrscJBRwGx1xV2D8jDR-HWZrMDAsTaxN4V8IIAQnfBWCVk7lErZO4IXdaG5fWKwvX_eDE"),
        Track("Clairo", "", "https://lh3.googleusercontent.com/aida-public/AB6AXuAK14xH_gIXC28sny0FVTa3ChyXCAuiXcSWHHgzDmg5Ib3jVOKimgZYJbRIS68RPWAqyXDwH4J5ANwbc6wWbCOm8PAwFXRGyIX8KqLUG0THDjGL5ozd76Q1I8ZwY3ovvNzbzdsae2NVWz5CCjg6HTO16ypLuwhvHZDYF9VkX2a2kONFcd0hNiPEAOPVRnPGAJ8pNvGtYpnE8ooOa_YUN9kTUtAOo8rs-VPhDecoDZ5olUoRccETDQWKk9kzI3LGfnz0WwgBV0vBH6R6"),
        Track("The Weeknd", "", "https://lh3.googleusercontent.com/aida-public/AB6AXuAnLg8K9gI5Yw_rF8XzJ8qE_-Ie7nQ_pYfFhWp9-2_u8U_qP5qY_uK8M8vR_i4W8u6N5-R6YvX_I3s9R5zT-yM8P8vR5xY_Q2lP6-T5Yw_yT5-R_Q8vR5xY_p9nL9qR5zP5"),
        Track("Taylor Swift", "", "https://lh3.googleusercontent.com/aida-public/AB6AXuC9rF_qM8I5Xw_rP8WzJ9qE_-Qe7nQ_pYfDhZp9-2_u8W_qP5qY_uK8M8vP_i4Z8u6N5-R6YvX_I3z9R5yT-yM8P8vR5xZ_Q2lP6-T5Yw_yT5-R_Q8vR5xY_p9nM9qR5zP6"),
        Track("SZA", "", "https://lh3.googleusercontent.com/aida-public/AB6AXuB2rF_qM8I5Xw_rP8WzJ9qE_-Qe7nQ_pYfDhZp9-2_u8W_qP5qY_uK8M8vP_i4Z8u6N5-R6YvX_I3z9R5yT-yM8P8vR5xZ_Q2lP6-T5Yw_yT5-R_Q8vR5xY_p9nM9qR5zP7")
    ) }
    val tracks = remember { mutableStateListOf(
        Track("Nobya", "Adie", "https://lh3.googleusercontent.com/aida-public/AB6AXuChvTrTHTqsVcwIhsfGRXFHJEKmVM6yatNP25YOxWlyJH4Fjzlnk_-k4SAM4l9prAwritBfKhydgXtDk9D_IrsHKO2SwWGqxRo7aDgxeL-9FxVzt9NT92qD1KH4rXtQuZ4INROQrIL1KxSDWwdm72ocwywyMiecbHzCaxX6SA63x8FpTJj_g6PnLCbH46sXi-0C7pMpOtUF1FF0gQ92CjP3G7DHt9RXkJXr6kOg3RexdHGcLLK2nhSrx34MiNfvVe067yFhIp3VNpJV"),
        Track("Eye of the Night", "Conan Gray", "https://lh3.googleusercontent.com/aida-public/AB6AXuCyGXpg5kFnZwRj8SmnlUaNJ247hgAZMJtR7actWvE64injbHJEvItbJ9X3bi-KDOj65204qVDGu5Ozhewq8V2QSWVzFc7B2lmyLtzw7hU4BPc6SxbqLoDyJNMhRC6oCPOGZpZqRS8tShuJ0-Mxi8KBHCQfsdKZ-NDlM31InS1v_6dbhMxWkeApQnmlJqVRzXLsFnDDFk52uugN9_F5ICGA2ye7-w3DxGUAGG1kj6DZ4-6pSWrZqZfqS6-nHhRFPxfaTUTRCXHPLtTs"),
        Track("brutal", "Olivia Rodrigo", "https://lh3.googleusercontent.com/aida-public/AB6AXuAANIL56mQT4724vTnY-7GXMojS-p8TQUaihDk3K4fUP4miM3SutxlI4s_FSDLIM5e0clIZMg0RYEJJs5t0h3HfgbQpgJn0_ko7q22X-F_GIcDLITtRhpDFVD-lBkFzyHgpnDbmjAriwmiU21wA71rO7YFDVIrWQGVCc186YYwyNSK3EiHuSmSCNTa9fn5O34xfsEPcCAk375ScV76WN0VMwNeEhRVZiRQvPVcrrWvrg0WKiTgCve8oWneNtV5RiAdniXfDA4EFHfwY"),
        Track("Beautiful Things", "Benson Boone", "https://lh3.googleusercontent.com/aida-public/AB6AXuD9YtHeOQWq363b3DFSXAHuP5-UHi9KIIwRNFoQE8_3eupMOpBQwyGUwrW1d6Vspi98o7sAYofTBlTiXs5lwyztmGTmf-5vUMZNVPTLForjJEM4fpViE-fv36jlr7oIoVURNNmzqm2zQN5apGzwqxUa2Q_I77uCi37gYWcLBQQ6lNXHLhFNx5BfMHSAcycCPpmUGLEBFlkXOS3DTma_oXlAS6z7FkbWz3HSODYmyA1-OplLTsNTEFY9_7b_OyBFtIAQOsMsNgX4e1dX"),
        Track("Midnight City", "M83", "https://lh3.googleusercontent.com/aida-public/AB6AXuA6RuXZTI7C9o2m_kuKATUcE9jsXV0-jkCotOQDiCfqqmOD5KsJoAXxjOce15oHvjqG2H3e_1Omatre_S_mBAflMSq4wQr_K8heU7QAYlonWZCaGf5Hu9RnSQdN9dRTMdUqMlr-gwaRa-GNr2oCaunItALF1wID6NE_aK7vesTj7N7lzEBAcF2Y9vU7TiWIIvoPRdzYCxxn8Wjeg7W6CAi0o-WvPMjMSHE9Vbys7EAJ1JNAzrfciKAe-_2-xzCAplOkpb7hFXi9RFj1"),
        Track("Weightless", "Marconi Union", "https://lh3.googleusercontent.com/aida-public/AB6AXuBVltuTQFJeOcuy2C_KOu04dqTrxfvBAbt8eoxQxGRn1bhTlEZ_OSvAe1OLsavdRLATEKuUjAZj_E_MS6ijtj8QXJoPXdjslVH3V5snHKRKlH1TZF-yM_z5LjSaYz5dsuN5s4FVcmVHyJIRaelrBOZMRI4oRHgRr86RaJMCtT90nawdoQyKtSO3csfKJ198nOMHspH4a7xqidwa2aFO7KJsi0hEqaq86BS9tdtAM1DXzdGzp5Nmz7p_Gf0cUYQOLQrrEqIXvXwsGOqN")
    ) }
    var showDeleteDialog by remember { mutableStateOf(false) }
"""

content = content.replace("    var isGridView by androidx.compose.runtime.saveable.rememberSaveable(tabName) { mutableStateOf(true) }", 
                          "    var isGridView by androidx.compose.runtime.saveable.rememberSaveable(tabName) { mutableStateOf(true) }\n" + hoisted_state)

content = content.replace("""                    TextButton(onClick = { onSelectAll(emptyList()) }) {
                        Text("Select all", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                }""", """                    TextButton(onClick = { onSelectAll(emptyList()) }) {
                        Text("Select all", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }""")


# Add delete dialog
delete_dialog = """
    if (showDeleteDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Delete ${if (selectedItems.size == 1) "item" else "${selectedItems.size} items"}",
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            text = {
                val itemName = selectedItems.firstOrNull() ?: ""
                Text(
                    text = if (selectedItems.size == 1) 
                        "Are you sure you want to delete \\"${itemName}\\"? This action cannot be undone." 
                    else 
                        "Are you sure you want to delete ${selectedItems.size} items? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    playlists.removeIf { selectedItems.contains(it.title) }
                    albums.removeIf { selectedItems.contains(it.title) }
                    artists.removeIf { selectedItems.contains(it.title) }
                    tracks.removeIf { selectedItems.contains(it.title) }
                    onClearSelection()
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
"""

content = content.replace("    if (sortExpanded) {", delete_dialog + "\n    if (sortExpanded) {")

content = content.replace("""                val playlists = listOf(
                    Track("This Is Conan Gray", "50 songs", "https://lh3.googleusercontent.com/aida-public/AB6AXuAN2IRvdhR_bkbDMJ8TqVqG2y4rQ3L_f1eRHAi_gAsteuGmZIEL0FgQOJ7eagpNOd8RyZJEar704LgA2EaC_eTYUgCmOiMzxQpaVHQJ8M6flPFUIJ71YUtKiIKyo6UAJW1DAoztixz1AbUm17RLgT5jOGGPPEHgGrkOwf0WxcPSFfkL_vvYISYxu70oxAu8dCHUOY7vYG371MR9sBrRRiEMLJ30KgLLrV0hg9_zUe8qs294_IXoKmQpLvb3X8pgvDIEYaT6kFzuQnqG"),
                    Track("Conan Gray Complete", "92 songs", "https://lh3.googleusercontent.com/aida-public/AB6AXuBoqgZeT99UHwbjmcDIfQ71twNukb3Tzd6G0dsOGEVmLDnD0HXbeipJGkmNY9O9jEZmz2oFg0vvlWWcin0dGwUVfoPwskoe8c4oYobJajOAkkxzb37yRJyhMINPt8VjjLF_6LNVXoE0lMjq4rRHPngOSRTiUsB4XfaYdshllx1R9y55Lfv6Texk356Nj71-a8U5r8eU33_lhmgL7cxyPCDo13d9oHJOiqTN2K8-aKMogfnmKcgzCNBO_-ctALvKFyota4_U73THmnSM")
                )""", "")

content = content.replace("""                val albums = listOf(
                    Track("Wishbone", "2024", "https://lh3.googleusercontent.com/aida-public/AB6AXuCJx6j_t12Lixrr_kYaU8GRKRWceLe29sZI9YzJc0GLeyahwMHKbEC0ARLLWL-0RmYqBHjxwmZzgNqNyRZsCh8PjVOmyd_6FpltGB2ZT_2jSJjOT8ipdJzKfCUS1h3RWY2qsxJWOi3EOD8t0KBaOzpsuut79QihAF69rulfi88J3UM0uEC9UWv59NcUIbAA4flMoQKK67G82bGMK3o8oiFXpsKMS4SmVxG6s9JjrZ_ulmWltpRwT18rZusm2Ui_DldbXtXHUGkiFqXA"),
                    Track("Found Heaven", "2024", "https://lh3.googleusercontent.com/aida-public/AB6AXuCODHaqT8FgGkmll_uWxqILgVV36ouYMwPInauRKx6wBVbadKgCbI6Gu2l-6tMLWgUewsi722Aj7grQhFcjobgqH-rvv97f3l73MGicKm6UZaN__B7Y4wqGzfDLgO5_e4tkkT6iz8fh0i9NhWceHQZplyQvKA0rQORrodWwsg0VlqVGSU6-fCQd4hI0EvZxWW2QLYHdPOrvqch7wmytx-dgCjuGXDkV84Xii0_Zh8bO5Yw_k-Cr8vywNKEVW-avcnusLhxNXdjHMyUk"),
                    Track("Sunset Season", "2018", "https://lh3.googleusercontent.com/aida-public/AB6AXuAuiVAn7oAHL_KdHRDZ4mLsedDYz5p4qbtHc7i_mCkC0sA1AcSlpcPOr4JG3zFqISryKhW0xw9DLg1VH7v0I-grVZxCKOdRpXOS8JF45Bvtdp7ttKl94_O8zSrGE8UjsHGAckPBHvT2sEUBjTUkbW2UgStiAr4UoKUtUjH7ipmJnciqe2EY_-eHJwV3Hynr3KoJ3KA0ETbkuPiwl5WSrrAxcTQUOZHcm6ex1VvaLO4cvd6ps0DWAazvY0O2hvX7HmUc9z2-64-f7JJP"),
                    Track("Kid Krow", "2020", "https://lh3.googleusercontent.com/aida-public/AB6AXuBk6_5E-fMV7ZV0GQOXs1uNdR1fVPPFyMpsyDIKr5xnx03lD7qROZ9040-4FKtgbLwRJU6pgwCljp2F8FRqt4UcKC7URBzbrumdteldMiK-cwFRCschfM1rZxbS4IdeQvOTMHpkcKjyjRAjCjCZ6ORIByQiK94f35mOI8W2Edo3WPDTwKK3iC9w3BLVgM7Ic8-JOHLJMF9eERBzYRc6gRFXpi5zXS5z1nWtwrx_GK6nQN6_HlPe8u2i2xBf0JjvQEnUyqbyP3zviVCL")
                )""", "")

content = content.replace("""                val artists = listOf(
                    Track("Conan Gray", "", "https://lh3.googleusercontent.com/aida/AP1WRLv2WecEYcFjvYBf-M3uEQ_any0wLnlOIbEuk_z6TQbKqKTSZoFVZNYQ-1t8glDuBotg9yeGnMK8FZEE-kgwiLAVRBXzvQimz6mY682dnzbndydZF2E-RtA81Z-B73vftEA1FvCkglrC0eRpulttSej5eBpotQsOJDlrWXWG2NcJDqKcgI2WBx09sqJfbw09cTRFbu54vdDLp3z42vq-SHV4IdKULZGUnC2hu9U6zJ1iQMIX1k012Bm-U6I"),
                    Track("Olivia Rodrigo", "", "https://lh3.googleusercontent.com/aida-public/AB6AXuDpd4CIX-H8zlbsGd2i6jjWTt-WOc214as4xVDbr4pkrBf_vcjs72oEGP__NvVLxeVuCZNmsfAzwM_mFJCsW66dkwmiPUSShBwrcVCVr1_8E5XqjqNJrNWazqWkCebFeEIm07N4NFFTxz6sTcuXVmJ-NWCgOZ0IQXHH_Ke7iK75l9IABaK3cAE_DuxPtHifeZyY7VTzSf5OrscJBRwGx1xV2D8jDR-HWZrMDAsTaxN4V8IIAQnfBWCVk7lErZO4IXdaG5fWKwvX_eDE"),
                    Track("Clairo", "", "https://lh3.googleusercontent.com/aida-public/AB6AXuAK14xH_gIXC28sny0FVTa3ChyXCAuiXcSWHHgzDmg5Ib3jVOKimgZYJbRIS68RPWAqyXDwH4J5ANwbc6wWbCOm8PAwFXRGyIX8KqLUG0THDjGL5ozd76Q1I8ZwY3ovvNzbzdsae2NVWz5CCjg6HTO16ypLuwhvHZDYF9VkX2a2kONFcd0hNiPEAOPVRnPGAJ8pNvGtYpnE8ooOa_YUN9kTUtAOo8rs-VPhDecoDZ5olUoRccETDQWKk9kzI3LGfnz0WwgBV0vBH6R6"),
                    Track("The Weeknd", "", "https://lh3.googleusercontent.com/aida-public/AB6AXuAnLg8K9gI5Yw_rF8XzJ8qE_-Ie7nQ_pYfFhWp9-2_u8U_qP5qY_uK8M8vR_i4W8u6N5-R6YvX_I3s9R5zT-yM8P8vR5xY_Q2lP6-T5Yw_yT5-R_Q8vR5xY_p9nL9qR5zP5"),
                    Track("Taylor Swift", "", "https://lh3.googleusercontent.com/aida-public/AB6AXuC9rF_qM8I5Xw_rP8WzJ9qE_-Qe7nQ_pYfDhZp9-2_u8W_qP5qY_uK8M8vP_i4Z8u6N5-R6YvX_I3z9R5yT-yM8P8vR5xZ_Q2lP6-T5Yw_yT5-R_Q8vR5xY_p9nM9qR5zP6"),
                    Track("SZA", "", "https://lh3.googleusercontent.com/aida-public/AB6AXuB2rF_qM8I5Xw_rP8WzJ9qE_-Qe7nQ_pYfDhZp9-2_u8W_qP5qY_uK8M8vP_i4Z8u6N5-R6YvX_I3z9R5yT-yM8P8vR5xZ_Q2lP6-T5Yw_yT5-R_Q8vR5xY_p9nM9qR5zP7")
                )""", "")

content = content.replace("""                val tracks = listOf(
                    Track("Nobya", "Adie", "https://lh3.googleusercontent.com/aida-public/AB6AXuChvTrTHTqsVcwIhsfGRXFHJEKmVM6yatNP25YOxWlyJH4Fjzlnk_-k4SAM4l9prAwritBfKhydgXtDk9D_IrsHKO2SwWGqxRo7aDgxeL-9FxVzt9NT92qD1KH4rXtQuZ4INROQrIL1KxSDWwdm72ocwywyMiecbHzCaxX6SA63x8FpTJj_g6PnLCbH46sXi-0C7pMpOtUF1FF0gQ92CjP3G7DHt9RXkJXr6kOg3RexdHGcLLK2nhSrx34MiNfvVe067yFhIp3VNpJV"),
                    Track("Eye of the Night", "Conan Gray", "https://lh3.googleusercontent.com/aida-public/AB6AXuCyGXpg5kFnZwRj8SmnlUaNJ247hgAZMJtR7actWvE64injbHJEvItbJ9X3bi-KDOj65204qVDGu5Ozhewq8V2QSWVzFc7B2lmyLtzw7hU4BPc6SxbqLoDyJNMhRC6oCPOGZpZqRS8tShuJ0-Mxi8KBHCQfsdKZ-NDlM31InS1v_6dbhMxWkeApQnmlJqVRzXLsFnDDFk52uugN9_F5ICGA2ye7-w3DxGUAGG1kj6DZ4-6pSWrZqZfqS6-nHhRFPxfaTUTRCXHPLtTs"),
                    Track("brutal", "Olivia Rodrigo", "https://lh3.googleusercontent.com/aida-public/AB6AXuAANIL56mQT4724vTnY-7GXMojS-p8TQUaihDk3K4fUP4miM3SutxlI4s_FSDLIM5e0clIZMg0RYEJJs5t0h3HfgbQpgJn0_ko7q22X-F_GIcDLITtRhpDFVD-lBkFzyHgpnDbmjAriwmiU21wA71rO7YFDVIrWQGVCc186YYwyNSK3EiHuSmSCNTa9fn5O34xfsEPcCAk375ScV76WN0VMwNeEhRVZiRQvPVcrrWvrg0WKiTgCve8oWneNtV5RiAdniXfDA4EFHfwY"),
                    Track("Beautiful Things", "Benson Boone", "https://lh3.googleusercontent.com/aida-public/AB6AXuD9YtHeOQWq363b3DFSXAHuP5-UHi9KIIwRNFoQE8_3eupMOpBQwyGUwrW1d6Vspi98o7sAYofTBlTiXs5lwyztmGTmf-5vUMZNVPTLForjJEM4fpViE-fv36jlr7oIoVURNNmzqm2zQN5apGzwqxUa2Q_I77uCi37gYWcLBQQ6lNXHLhFNx5BfMHSAcycCPpmUGLEBFlkXOS3DTma_oXlAS6z7FkbWz3HSODYmyA1-OplLTsNTEFY9_7b_OyBFtIAQOsMsNgX4e1dX"),
                    Track("Midnight City", "M83", "https://lh3.googleusercontent.com/aida-public/AB6AXuA6RuXZTI7C9o2m_kuKATUcE9jsXV0-jkCotOQDiCfqqmOD5KsJoAXxjOce15oHvjqG2H3e_1Omatre_S_mBAflMSq4wQr_K8heU7QAYlonWZCaGf5Hu9RnSQdN9dRTMdUqMlr-gwaRa-GNr2oCaunItALF1wID6NE_aK7vesTj7N7lzEBAcF2Y9vU7TiWIIvoPRdzYCxxn8Wjeg7W6CAi0o-WvPMjMSHE9Vbys7EAJ1JNAzrfciKAe-_2-xzCAplOkpb7hFXi9RFj1"),
                    Track("Weightless", "Marconi Union", "https://lh3.googleusercontent.com/aida-public/AB6AXuBVltuTQFJeOcuy2C_KOu04dqTrxfvBAbt8eoxQxGRn1bhTlEZ_OSvAe1OLsavdRLATEKuUjAZj_E_MS6ijtj8QXJoPXdjslVH3V5snHKRKlH1TZF-yM_z5LjSaYz5dsuN5s4FVcmVHyJIRaelrBOZMRI4oRHgRr86RaJMCtT90nawdoQyKtSO3csfKJ198nOMHspH4a7xqidwa2aFO7KJsi0hEqaq86BS9tdtAM1DXzdGzp5Nmz7p_Gf0cUYQOLQrrEqIXvXwsGOqN")
                )""", "")


with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
