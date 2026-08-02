with open('app/src/main/java/com/example/LibraryComponents.kt', 'r') as f:
    content = f.read()

old_code = """
            if (tabName == "Playlists") {
                val playlists = listOf(
                    Track("This Is Conan Gray", "50 songs", "https://lh3.googleusercontent.com/aida-public/AB6AXuAN2IRvdhR_bkbDMJ8TqVqG2y4rQ3L_f1eRHAi_gAsteuGmZIEL0FgQOJ7eagpNOd8RyZJEar704LgA2EaC_eTYUgCmOiMzxQpaVHQJ8M6flPFUIJ71YUtKiIKyo6UAJW1DAoztixz1AbUm17RLgT5jOGGPPEHgGrkOwf0WxcPSFfkL_vvYISYxu70oxAu8dCHUOY7vYG371MR9sBrRRiEMLJ30KgLLrV0hg9_zUe8qs294_IXoKmQpLvb3X8pgvDIEYaT6kFzuQnqG"),
                    Track("Conan Gray Complete", "92 songs", "https://lh3.googleusercontent.com/aida-public/AB6AXuBoqgZeT99UHwbjmcDIfQ71twNukb3Tzd6G0dsOGEVmLDnD0HXbeipJGkmNY9O9jEZmz2oFg0vvlWWcin0dGwUVfoPwskoe8c4oYobJajOAkkxzb37yRJyhMINPt8VjjLF_6LNVXoE0lMjq4rRHPngOSRTiUsB4XfaYdshllx1R9y55Lfv6Texk356Nj71-a8U5r8eU33_lhmgL7cxyPCDo13d9oHJOiqTN2K8-aKMogfnmKcgzCNBO_-ctALvKFyota4_U73THmnSM")
                )
                if (isGridView) {
                    for (i in playlists.indices step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AlbumResultItem(title = playlists[i].title, year = playlists[i].artist, imageUrl = playlists[i].imageUrl, modifier = Modifier.weight(1f), onClick = onNavigateToPlaylistDetails)
                            if (i + 1 < playlists.size) {
                                AlbumResultItem(title = playlists[i + 1].title, year = playlists[i + 1].artist, imageUrl = playlists[i + 1].imageUrl, modifier = Modifier.weight(1f), onClick = onNavigateToPlaylistDetails)
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    for (p in playlists) {
                        PlaylistResultItem(
                            title = p.title, 
                            subtitle = p.artist, 
                            imageUrl = p.imageUrl, 
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onNavigateToPlaylistDetails
                        )
                    }
                }
            } else if (tabName == "Albums") {
                // Grid of albums
                val albums = listOf(
                    Track("Wishbone", "2024", "https://lh3.googleusercontent.com/aida-public/AB6AXuCJx6j_t12Lixrr_kYaU8GRKRWceLe29sZI9YzJc0GLeyahwMHKbEC0ARLLWL-0RmYqBHjxwmZzgNqNyRZsCh8PjVOmyd_6FpltGB2ZT_2jSJjOT8ipdJzKfCUS1h3RWY2qsxJWOi3EOD8t0KBaOzpsuut79QihAF69rulfi88J3UM0uEC9UWv59NcUIbAA4flMoQKK67G82bGMK3o8oiFXpsKMS4SmVxG6s9JjrZ_ulmWltpRwT18rZusm2Ui_DldbXtXHUGkiFqXA"),
                    Track("Found Heaven", "2024", "https://lh3.googleusercontent.com/aida-public/AB6AXuCODHaqT8FgGkmll_uWxqILgVV36ouYMwPInauRKx6wBVbadKgCbI6Gu2l-6tMLWgUewsi722Aj7grQhFcjobgqH-rvv97f3l73MGicKm6UZaN__B7Y4wqGzfDLgO5_e4tkkT6iz8fh0i9NhWceHQZplyQvKA0rQORrodWwsg0VlqVGSU6-fCQd4hI0EvZxWW2QLYHdPOrvqch7wmytx-dgCjuGXDkV84Xii0_Zh8bO5Yw_k-Cr8vywNKEVW-avcnusLhxNXdjHMyUk"),
                    Track("Sunset Season", "2018", "https://lh3.googleusercontent.com/aida-public/AB6AXuAuiVAn7oAHL_KdHRDZ4mLsedDYz5p4qbtHc7i_mCkC0sA1AcSlpcPOr4JG3zFqISryKhW0xw9DLg1VH7v0I-grVZxCKOdRpXOS8JF45Bvtdp7ttKl94_O8zSrGE8UjsHGAckPBHvT2sEUBjTUkbW2UgStiAr4UoKUtUjH7ipmJnciqe2EY_-eHJwV3Hynr3KoJ3KA0ETbkuPiwl5WSrrAxcTQUOZHcm6ex1VvaLO4cvd6ps0DWAazvY0O2hvX7HmUc9z2-64-f7JJP"),
                    Track("Kid Krow", "2020", "https://lh3.googleusercontent.com/aida-public/AB6AXuBk6_5E-fMV7ZV0GQOXs1uNdR1fVPPFyMpsyDIKr5xnx03lD7qROZ9040-4FKtgbLwRJU6pgwCljp2F8FRqt4UcKC7URBzbrumdteldMiK-cwFRCschfM1rZxbS4IdeQvOTMHpkcKjyjRAjCjCZ6ORIByQiK94f35mOI8W2Edo3WPDTwKK3iC9w3BLVgM7Ic8-JOHLJMF9eERBzYRc6gRFXpi5zXS5z1nWtwrx_GK6nQN6_HlPe8u2i2xBf0JjvQEnUyqbyP3zviVCL")
                )
                
                for (i in albums.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AlbumResultItem(title = albums[i].title, year = albums[i].artist, imageUrl = albums[i].imageUrl, modifier = Modifier.weight(1f), onClick = onNavigateToAlbumDetails)
                        if (i + 1 < albums.size) {
                            AlbumResultItem(title = albums[i + 1].title, year = albums[i + 1].artist, imageUrl = albums[i + 1].imageUrl, modifier = Modifier.weight(1f), onClick = onNavigateToAlbumDetails)
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                // Grid of tracks
                val tracks = listOf(
                    Track("Nobya", "Adie", "https://lh3.googleusercontent.com/aida-public/AB6AXuChvTrTHTqsVcwIhsfGRXFHJEKmVM6yatNP25YOxWlyJH4Fjzlnk_-k4SAM4l9prAwritBfKhydgXtDk9D_IrsHKO2SwWGqxRo7aDgxeL-9FxVzt9NT92qD1KH4rXtQuZ4INROQrIL1KxSDWwdm72ocwywyMiecbHzCaxX6SA63x8FpTJj_g6PnLCbH46sXi-0C7pMpOtUF1FF0gQ92CjP3G7DHt9RXkJXr6kOg3RexdHGcLLK2nhSrx34MiNfvVe067yFhIp3VNpJV"),
                    Track("Eye of the Night", "Conan Gray", "https://lh3.googleusercontent.com/aida-public/AB6AXuCyGXpg5kFnZwRj8SmnlUaNJ247hgAZMJtR7actWvE64injbHJEvItbJ9X3bi-KDOj65204qVDGu5Ozhewq8V2QSWVzFc7B2lmyLtzw7hU4BPc6SxbqLoDyJNMhRC6oCPOGZpZqRS8tShuJ0-Mxi8KBHCQfsdKZ-NDlM31InS1v_6dbhMxWkeApQnmlJqVRzXLsFnDDFk52uugN9_F5ICGA2ye7-w3DxGUAGG1kj6DZ4-6pSWrZqZfqS6-nHhRFPxfaTUTRCXHPLtTs"),
                    Track("brutal", "Olivia Rodrigo", "https://lh3.googleusercontent.com/aida-public/AB6AXuAANIL56mQT4724vTnY-7GXMojS-p8TQUaihDk3K4fUP4miM3SutxlI4s_FSDLIM5e0clIZMg0RYEJJs5t0h3HfgbQpgJn0_ko7q22X-F_GIcDLITtRhpDFVD-lBkFzyHgpnDbmjAriwmiU21wA71rO7YFDVIrWQGVCc186YYwyNSK3EiHuSmSCNTa9fn5O34xfsEPcCAk375ScV76WN0VMwNeEhRVZiRQvPVcrrWvrg0WKiTgCve8oWneNtV5RiAdniXfDA4EFHfwY"),
                    Track("Beautiful Things", "Benson Boone", "https://lh3.googleusercontent.com/aida-public/AB6AXuD9YtHeOQWq363b3DFSXAHuP5-UHi9KIIwRNFoQE8_3eupMOpBQwyGUwrW1d6Vspi98o7sAYofTBlTiXs5lwyztmGTmf-5vUMZNVPTLForjJEM4fpViE-fv36jlr7oIoVURNNmzqm2zQN5apGzwqxUa2Q_I77uCi37gYWcLBQQ6lNXHLhFNx5BfMHSAcycCPpmUGLEBFlkXOS3DTma_oXlAS6z7FkbWz3HSODYmyA1-OplLTsNTEFY9_7b_OyBFtIAQOsMsNgX4e1dX"),
                    Track("Midnight City", "M83", "https://lh3.googleusercontent.com/aida-public/AB6AXuA6RuXZTI7C9o2m_kuKATUcE9jsXV0-jkCotOQDiCfqqmOD5KsJoAXxjOce15oHvjqG2H3e_1Omatre_S_mBAflMSq4wQr_K8heU7QAYlonWZCaGf5Hu9RnSQdN9dRTMdUqMlr-gwaRa-GNr2oCaunItALF1wID6NE_aK7vesTj7N7lzEBAcF2Y9vU7TiWIIvoPRdzYCxxn8Wjeg7W6CAi0o-WvPMjMSHE9Vbys7EAJ1JNAzrfciKAe-_2-xzCAplOkpb7hFXi9RFj1"),
                    Track("Weightless", "Marconi Union", "https://lh3.googleusercontent.com/aida-public/AB6AXuBVltuTQFJeOcuy2C_KOu04dqTrxfvBAbt8eoxQxGRn1bhTlEZ_OSvAe1OLsavdRLATEKuUjAZj_E_MS6ijtj8QXJoPXdjslVH3V5snHKRKlH1TZF-yM_z5LjSaYz5dsuN5s4FVcmVHyJIRaelrBOZMRI4oRHgRr86RaJMCtT90nawdoQyKtSO3csfKJ198nOMHspH4a7xqidwa2aFO7KJsi0hEqaq86BS9tdtAM1DXzdGzp5Nmz7p_Gf0cUYQOLQrrEqIXvXwsGOqN")
                )
                
                for (i in tracks.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TrackGridItem(track = tracks[i], modifier = Modifier.weight(1f))
                        if (i + 1 < tracks.size) {
                            TrackGridItem(track = tracks[i + 1], modifier = Modifier.weight(1f))
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
"""

new_code = """
            if (tabName == "Playlists") {
                val playlists = listOf(
                    Track("This Is Conan Gray", "50 songs", "https://lh3.googleusercontent.com/aida-public/AB6AXuAN2IRvdhR_bkbDMJ8TqVqG2y4rQ3L_f1eRHAi_gAsteuGmZIEL0FgQOJ7eagpNOd8RyZJEar704LgA2EaC_eTYUgCmOiMzxQpaVHQJ8M6flPFUIJ71YUtKiIKyo6UAJW1DAoztixz1AbUm17RLgT5jOGGPPEHgGrkOwf0WxcPSFfkL_vvYISYxu70oxAu8dCHUOY7vYG371MR9sBrRRiEMLJ30KgLLrV0hg9_zUe8qs294_IXoKmQpLvb3X8pgvDIEYaT6kFzuQnqG"),
                    Track("Conan Gray Complete", "92 songs", "https://lh3.googleusercontent.com/aida-public/AB6AXuBoqgZeT99UHwbjmcDIfQ71twNukb3Tzd6G0dsOGEVmLDnD0HXbeipJGkmNY9O9jEZmz2oFg0vvlWWcin0dGwUVfoPwskoe8c4oYobJajOAkkxzb37yRJyhMINPt8VjjLF_6LNVXoE0lMjq4rRHPngOSRTiUsB4XfaYdshllx1R9y55Lfv6Texk356Nj71-a8U5r8eU33_lhmgL7cxyPCDo13d9oHJOiqTN2K8-aKMogfnmKcgzCNBO_-ctALvKFyota4_U73THmnSM")
                )
                if (isGridView) {
                    for (i in playlists.indices step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AlbumResultItem(title = playlists[i].title, year = playlists[i].artist, imageUrl = playlists[i].imageUrl, modifier = Modifier.weight(1f), onClick = onNavigateToPlaylistDetails)
                            if (i + 1 < playlists.size) {
                                AlbumResultItem(title = playlists[i + 1].title, year = playlists[i + 1].artist, imageUrl = playlists[i + 1].imageUrl, modifier = Modifier.weight(1f), onClick = onNavigateToPlaylistDetails)
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    for (p in playlists) {
                        PlaylistResultItem(
                            title = p.title, 
                            subtitle = p.artist, 
                            imageUrl = p.imageUrl, 
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onNavigateToPlaylistDetails
                        )
                    }
                }
            } else if (tabName == "Albums") {
                // Grid of albums
                val albums = listOf(
                    Track("Wishbone", "2024", "https://lh3.googleusercontent.com/aida-public/AB6AXuCJx6j_t12Lixrr_kYaU8GRKRWceLe29sZI9YzJc0GLeyahwMHKbEC0ARLLWL-0RmYqBHjxwmZzgNqNyRZsCh8PjVOmyd_6FpltGB2ZT_2jSJjOT8ipdJzKfCUS1h3RWY2qsxJWOi3EOD8t0KBaOzpsuut79QihAF69rulfi88J3UM0uEC9UWv59NcUIbAA4flMoQKK67G82bGMK3o8oiFXpsKMS4SmVxG6s9JjrZ_ulmWltpRwT18rZusm2Ui_DldbXtXHUGkiFqXA"),
                    Track("Found Heaven", "2024", "https://lh3.googleusercontent.com/aida-public/AB6AXuCODHaqT8FgGkmll_uWxqILgVV36ouYMwPInauRKx6wBVbadKgCbI6Gu2l-6tMLWgUewsi722Aj7grQhFcjobgqH-rvv97f3l73MGicKm6UZaN__B7Y4wqGzfDLgO5_e4tkkT6iz8fh0i9NhWceHQZplyQvKA0rQORrodWwsg0VlqVGSU6-fCQd4hI0EvZxWW2QLYHdPOrvqch7wmytx-dgCjuGXDkV84Xii0_Zh8bO5Yw_k-Cr8vywNKEVW-avcnusLhxNXdjHMyUk"),
                    Track("Sunset Season", "2018", "https://lh3.googleusercontent.com/aida-public/AB6AXuAuiVAn7oAHL_KdHRDZ4mLsedDYz5p4qbtHc7i_mCkC0sA1AcSlpcPOr4JG3zFqISryKhW0xw9DLg1VH7v0I-grVZxCKOdRpXOS8JF45Bvtdp7ttKl94_O8zSrGE8UjsHGAckPBHvT2sEUBjTUkbW2UgStiAr4UoKUtUjH7ipmJnciqe2EY_-eHJwV3Hynr3KoJ3KA0ETbkuPiwl5WSrrAxcTQUOZHcm6ex1VvaLO4cvd6ps0DWAazvY0O2hvX7HmUc9z2-64-f7JJP"),
                    Track("Kid Krow", "2020", "https://lh3.googleusercontent.com/aida-public/AB6AXuBk6_5E-fMV7ZV0GQOXs1uNdR1fVPPFyMpsyDIKr5xnx03lD7qROZ9040-4FKtgbLwRJU6pgwCljp2F8FRqt4UcKC7URBzbrumdteldMiK-cwFRCschfM1rZxbS4IdeQvOTMHpkcKjyjRAjCjCZ6ORIByQiK94f35mOI8W2Edo3WPDTwKK3iC9w3BLVgM7Ic8-JOHLJMF9eERBzYRc6gRFXpi5zXS5z1nWtwrx_GK6nQN6_HlPe8u2i2xBf0JjvQEnUyqbyP3zviVCL")
                )
                
                if (isGridView) {
                    for (i in albums.indices step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AlbumResultItem(title = albums[i].title, year = albums[i].artist, imageUrl = albums[i].imageUrl, modifier = Modifier.weight(1f), onClick = onNavigateToAlbumDetails)
                            if (i + 1 < albums.size) {
                                AlbumResultItem(title = albums[i + 1].title, year = albums[i + 1].artist, imageUrl = albums[i + 1].imageUrl, modifier = Modifier.weight(1f), onClick = onNavigateToAlbumDetails)
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    for (a in albums) {
                        PlaylistResultItem(
                            title = a.title, 
                            subtitle = a.artist, 
                            imageUrl = a.imageUrl, 
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onNavigateToAlbumDetails
                        )
                    }
                }
            } else {
                // Grid of tracks
                val tracks = listOf(
                    Track("Nobya", "Adie", "https://lh3.googleusercontent.com/aida-public/AB6AXuChvTrTHTqsVcwIhsfGRXFHJEKmVM6yatNP25YOxWlyJH4Fjzlnk_-k4SAM4l9prAwritBfKhydgXtDk9D_IrsHKO2SwWGqxRo7aDgxeL-9FxVzt9NT92qD1KH4rXtQuZ4INROQrIL1KxSDWwdm72ocwywyMiecbHzCaxX6SA63x8FpTJj_g6PnLCbH46sXi-0C7pMpOtUF1FF0gQ92CjP3G7DHt9RXkJXr6kOg3RexdHGcLLK2nhSrx34MiNfvVe067yFhIp3VNpJV"),
                    Track("Eye of the Night", "Conan Gray", "https://lh3.googleusercontent.com/aida-public/AB6AXuCyGXpg5kFnZwRj8SmnlUaNJ247hgAZMJtR7actWvE64injbHJEvItbJ9X3bi-KDOj65204qVDGu5Ozhewq8V2QSWVzFc7B2lmyLtzw7hU4BPc6SxbqLoDyJNMhRC6oCPOGZpZqRS8tShuJ0-Mxi8KBHCQfsdKZ-NDlM31InS1v_6dbhMxWkeApQnmlJqVRzXLsFnDDFk52uugN9_F5ICGA2ye7-w3DxGUAGG1kj6DZ4-6pSWrZqZfqS6-nHhRFPxfaTUTRCXHPLtTs"),
                    Track("brutal", "Olivia Rodrigo", "https://lh3.googleusercontent.com/aida-public/AB6AXuAANIL56mQT4724vTnY-7GXMojS-p8TQUaihDk3K4fUP4miM3SutxlI4s_FSDLIM5e0clIZMg0RYEJJs5t0h3HfgbQpgJn0_ko7q22X-F_GIcDLITtRhpDFVD-lBkFzyHgpnDbmjAriwmiU21wA71rO7YFDVIrWQGVCc186YYwyNSK3EiHuSmSCNTa9fn5O34xfsEPcCAk375ScV76WN0VMwNeEhRVZiRQvPVcrrWvrg0WKiTgCve8oWneNtV5RiAdniXfDA4EFHfwY"),
                    Track("Beautiful Things", "Benson Boone", "https://lh3.googleusercontent.com/aida-public/AB6AXuD9YtHeOQWq363b3DFSXAHuP5-UHi9KIIwRNFoQE8_3eupMOpBQwyGUwrW1d6Vspi98o7sAYofTBlTiXs5lwyztmGTmf-5vUMZNVPTLForjJEM4fpViE-fv36jlr7oIoVURNNmzqm2zQN5apGzwqxUa2Q_I77uCi37gYWcLBQQ6lNXHLhFNx5BfMHSAcycCPpmUGLEBFlkXOS3DTma_oXlAS6z7FkbWz3HSODYmyA1-OplLTsNTEFY9_7b_OyBFtIAQOsMsNgX4e1dX"),
                    Track("Midnight City", "M83", "https://lh3.googleusercontent.com/aida-public/AB6AXuA6RuXZTI7C9o2m_kuKATUcE9jsXV0-jkCotOQDiCfqqmOD5KsJoAXxjOce15oHvjqG2H3e_1Omatre_S_mBAflMSq4wQr_K8heU7QAYlonWZCaGf5Hu9RnSQdN9dRTMdUqMlr-gwaRa-GNr2oCaunItALF1wID6NE_aK7vesTj7N7lzEBAcF2Y9vU7TiWIIvoPRdzYCxxn8Wjeg7W6CAi0o-WvPMjMSHE9Vbys7EAJ1JNAzrfciKAe-_2-xzCAplOkpb7hFXi9RFj1"),
                    Track("Weightless", "Marconi Union", "https://lh3.googleusercontent.com/aida-public/AB6AXuBVltuTQFJeOcuy2C_KOu04dqTrxfvBAbt8eoxQxGRn1bhTlEZ_OSvAe1OLsavdRLATEKuUjAZj_E_MS6ijtj8QXJoPXdjslVH3V5snHKRKlH1TZF-yM_z5LjSaYz5dsuN5s4FVcmVHyJIRaelrBOZMRI4oRHgRr86RaJMCtT90nawdoQyKtSO3csfKJ198nOMHspH4a7xqidwa2aFO7KJsi0hEqaq86BS9tdtAM1DXzdGzp5Nmz7p_Gf0cUYQOLQrrEqIXvXwsGOqN")
                )
                
                if (isGridView) {
                    for (i in tracks.indices step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            TrackGridItem(track = tracks[i], modifier = Modifier.weight(1f))
                            if (i + 1 < tracks.size) {
                                TrackGridItem(track = tracks[i + 1], modifier = Modifier.weight(1f))
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    for (t in tracks) {
                        SongResultItem(
                            title = t.title, 
                            artist = t.artist, 
                            duration = "3:30", 
                            imageUrl = t.imageUrl
                        )
                    }
                }
            }
"""

content = content.replace(old_code.strip(), new_code.strip())

with open('app/src/main/java/com/example/LibraryComponents.kt', 'w') as f:
    f.write(content)
