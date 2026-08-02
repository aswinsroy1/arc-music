with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

old_code = """
            } else {
                // Grid of tracks
"""

new_code = """
            } else if (tabName == "Artists") {
                val artists = listOf(
                    Track("Conan Gray", "", "https://lh3.googleusercontent.com/aida/AP1WRLv2WecEYcFjvYBf-M3uEQ_any0wLnlOIbEuk_z6TQbKqKTSZoFVZNYQ-1t8glDuBotg9yeGnMK8FZEE-kgwiLAVRBXzvQimz6mY682dnzbndydZF2E-RtA81Z-B73vftEA1FvCkglrC0eRpulttSej5eBpotQsOJDlrWXWG2NcJDqKcgI2WBx09sqJfbw09cTRFbu54vdDLp3z42vq-SHV4IdKULZGUnC2hu9U6zJ1iQMIX1k012Bm-U6I"),
                    Track("Olivia Rodrigo", "", "https://lh3.googleusercontent.com/aida-public/AB6AXuDpd4CIX-H8zlbsGd2i6jjWTt-WOc214as4xVDbr4pkrBf_vcjs72oEGP__NvVLxeVuCZNmsfAzwM_mFJCsW66dkwmiPUSShBwrcVCVr1_8E5XqjqNJrNWazqWkCebFeEIm07N4NFFTxz6sTcuXVmJ-NWCgOZ0IQXHH_Ke7iK75l9IABaK3cAE_DuxPtHifeZyY7VTzSf5OrscJBRwGx1xV2D8jDR-HWZrMDAsTaxN4V8IIAQnfBWCVk7lErZO4IXdaG5fWKwvX_eDE"),
                    Track("Clairo", "", "https://lh3.googleusercontent.com/aida-public/AB6AXuAK14xH_gIXC28sny0FVTa3ChyXCAuiXcSWHHgzDmg5Ib3jVOKimgZYJbRIS68RPWAqyXDwH4J5ANwbc6wWbCOm8PAwFXRGyIX8KqLUG0THDjGL5ozd76Q1I8ZwY3ovvNzbzdsae2NVWz5CCjg6HTO16ypLuwhvHZDYF9VkX2a2kONFcd0hNiPEAOPVRnPGAJ8pNvGtYpnE8ooOa_YUN9kTUtAOo8rs-VPhDecoDZ5olUoRccETDQWKk9kzI3LGfnz0WwgBV0vBH6R6"),
                    Track("The Weeknd", "", "https://lh3.googleusercontent.com/aida-public/AB6AXuAnLg8K9gI5Yw_rF8XzJ8qE_-Ie7nQ_pYfFhWp9-2_u8U_qP5qY_uK8M8vR_i4W8u6N5-R6YvX_I3s9R5zT-yM8P8vR5xY_Q2lP6-T5Yw_yT5-R_Q8vR5xY_p9nL9qR5zP5"),
                    Track("Taylor Swift", "", "https://lh3.googleusercontent.com/aida-public/AB6AXuC9rF_qM8I5Xw_rP8WzJ9qE_-Qe7nQ_pYfDhZp9-2_u8W_qP5qY_uK8M8vP_i4Z8u6N5-R6YvX_I3z9R5yT-yM8P8vR5xZ_Q2lP6-T5Yw_yT5-R_Q8vR5xY_p9nM9qR5zP6"),
                    Track("SZA", "", "https://lh3.googleusercontent.com/aida-public/AB6AXuB2rF_qM8I5Xw_rP8WzJ9qE_-Qe7nQ_pYfDhZp9-2_u8W_qP5qY_uK8M8vP_i4Z8u6N5-R6YvX_I3z9R5yT-yM8P8vR5xZ_Q2lP6-T5Yw_yT5-R_Q8vR5xY_p9nM9qR5zP7")
                )
                
                if (isGridView) {
                    for (i in artists.indices step 3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ArtistResultItem(name = artists[i].title, imageUrl = artists[i].imageUrl, modifier = Modifier.weight(1f))
                            if (i + 1 < artists.size) {
                                ArtistResultItem(name = artists[i + 1].title, imageUrl = artists[i + 1].imageUrl, modifier = Modifier.weight(1f))
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                            if (i + 2 < artists.size) {
                                ArtistResultItem(name = artists[i + 2].title, imageUrl = artists[i + 2].imageUrl, modifier = Modifier.weight(1f))
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    for (a in artists) {
                        PlaylistResultItem(
                            title = a.title, 
                            subtitle = "Artist", 
                            imageUrl = a.imageUrl, 
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {}
                        )
                    }
                }
            } else {
                // Grid of tracks
"""

content = content.replace(old_code.strip(), new_code.strip())

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
