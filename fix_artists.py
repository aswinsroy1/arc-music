with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

old_code = """
            item { ArtistResultItem("Conan Gray", onClick = onNavigateToArtistDetails, "https://lh3.googleusercontent.com/aida/AP1WRLv2WecEYcFjvYBf-M3uEQ_any0wLnlOIbEuk_z6TQbKqKTSZoFVZNYQ-1t8glDuBotg9yeGnMK8FZEE-kgwiLAVRBXzvQimz6mY682dnzbndydZF2E-RtA81Z-B73vftEA1FvCkglrC0eRpulttSej5eBpotQsOJDlrWXWG2NcJDqKcgI2WBx09sqJfbw09cTRFbu54vdDLp3z42vq-SHV4IdKULZGUnC2hu9U6zJ1iQMIX1k012Bm-U6I", true) }
            item { ArtistResultItem("Olivia Rodrigo", onClick = onNavigateToArtistDetails, "https://lh3.googleusercontent.com/aida-public/AB6AXuDpd4CIX-H8zlbsGd2i6jjWTt-WOc214as4xVDbr4pkrBf_vcjs72oEGP__NvVLxeVuCZNmsfAzwM_mFJCsW66dkwmiPUSShBwrcVCVr1_8E5XqjqNJrNWazqWkCebFeEIm07N4NFFTxz6sTcuXVmJ-NWCgOZ0IQXHH_Ke7iK75l9IABaK3cAE_DuxPtHifeZyY7VTzSf5OrscJBRwGx1xV2D8jDR-HWZrMDAsTaxN4V8IIAQnfBWCVk7lErZO4IXdaG5fWKwvX_eDE") }
            item { ArtistResultItem("Clairo", onClick = onNavigateToArtistDetails, "https://lh3.googleusercontent.com/aida-public/AB6AXuAK14xH_gIXC28sny0FVTa3ChyXCAuiXcSWHHgzDmg5Ib3jVOKimgZYJbRIS68RPWAqyXDwH4J5ANwbc6wWbCOm8PAwFXRGyIX8KqLUG0THDjGL5ozd76Q1I8ZwY3ovvNzbzdsae2NVWz5CCjg6HTO16ypLuwhvHZDYF9VkX2a2kONFcd0hNiPEAOPVRnPGAJ8pNvGtYpnE8ooOa_YUN9kTUtAOo8rs-VPhDecoDZ5olUoRccETDQWKk9kzI3LGfnz0WwgBV0vBH6R6") }
"""

new_code = """
            item { ArtistResultItem("Conan Gray", "https://lh3.googleusercontent.com/aida/AP1WRLv2WecEYcFjvYBf-M3uEQ_any0wLnlOIbEuk_z6TQbKqKTSZoFVZNYQ-1t8glDuBotg9yeGnMK8FZEE-kgwiLAVRBXzvQimz6mY682dnzbndydZF2E-RtA81Z-B73vftEA1FvCkglrC0eRpulttSej5eBpotQsOJDlrWXWG2NcJDqKcgI2WBx09sqJfbw09cTRFbu54vdDLp3z42vq-SHV4IdKULZGUnC2hu9U6zJ1iQMIX1k012Bm-U6I", true, onClick = onNavigateToArtistDetails) }
            item { ArtistResultItem("Olivia Rodrigo", "https://lh3.googleusercontent.com/aida-public/AB6AXuDpd4CIX-H8zlbsGd2i6jjWTt-WOc214as4xVDbr4pkrBf_vcjs72oEGP__NvVLxeVuCZNmsfAzwM_mFJCsW66dkwmiPUSShBwrcVCVr1_8E5XqjqNJrNWazqWkCebFeEIm07N4NFFTxz6sTcuXVmJ-NWCgOZ0IQXHH_Ke7iK75l9IABaK3cAE_DuxPtHifeZyY7VTzSf5OrscJBRwGx1xV2D8jDR-HWZrMDAsTaxN4V8IIAQnfBWCVk7lErZO4IXdaG5fWKwvX_eDE", onClick = onNavigateToArtistDetails) }
            item { ArtistResultItem("Clairo", "https://lh3.googleusercontent.com/aida-public/AB6AXuAK14xH_gIXC28sny0FVTa3ChyXCAuiXcSWHHgzDmg5Ib3jVOKimgZYJbRIS68RPWAqyXDwH4J5ANwbc6wWbCOm8PAwFXRGyIX8KqLUG0THDjGL5ozd76Q1I8ZwY3ovvNzbzdsae2NVWz5CCjg6HTO16ypLuwhvHZDYF9VkX2a2kONFcd0hNiPEAOPVRnPGAJ8pNvGtYpnE8ooOa_YUN9kTUtAOo8rs-VPhDecoDZ5olUoRccETDQWKk9kzI3LGfnz0WwgBV0vBH6R6", onClick = onNavigateToArtistDetails) }
"""

content = content.replace(old_code.strip(), new_code.strip())

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
