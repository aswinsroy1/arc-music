with open('app/src/main/java/com/example/QueueScreen.kt', 'r') as f:
    content = f.read()

old_up_next = """    val upNext = remember { mutableStateListOf(
        Track("GUTS", "Olivia Rodrigo", "https://lh3.googleusercontent.com/aida-public/AB6AXuAZJNn1JOYIGd_PenEus8adDrGQdKPRvpz8n3T4gjRfSXviuM9TdpA4qPpVLUVB866YKeziCQEy0dUAjnAfN-Fji6_UuPod28rEanUFccx3PkXfR9IaYcPx2FWz8GmLQWxRVx-M8bo7GmtZypNgD89th_DtXD7R86Vgu_OnM58EDsGTkLWYAaKnPM2GMp4Utp-Fo_y8v_SJkpRBt6IaKiSb64QAJNkcTwPApTKf8ub9SVdLy23u-1v8VBcdPBCRAhyNrIWcOCzfmd75"),
        Track("Sunset Season", "Conan Gray", "https://lh3.googleusercontent.com/aida-public/AB6AXuA53_T6j9Q0J5lqqjoK-rCH08PQQPkMhfl4S-R-RW7P0nQmpqwIaFdcp_LkX1C1wHyW9YrewuhDwelZg8Vtll_tj2W-tlzszHm9Li1Y1CJ0pLoGXWrZyXXq48s_Zsrckc99DeNI6kT3jTOukW7bKvJjcqboDa8R2jOpVVubEvR14JUYOfd-dw8AVuHDqm9kUGihm8INYbG_yGGSIvC2n6SBuXx29bomSjtwIaj3esB4XhTHG9KqzMslqTDXiCO3YdRCMvCZrqb9dzvl"),
        Track("Manchild", "Sabrina Carpenter", "https://lh3.googleusercontent.com/aida-public/AB6AXuCYsozvWVSSoYnfScqd0AapPlwDZDgA4K5kGUqgNf-aFySsrFYq7H4e0KQXtK9dKyr2zCKO1ibpumeNEaJRsY5sszYDBZpKTYFehLDb-Y6zCsf-fRnmvNxKbm9clKfUNyk5DZcVHPUHGioQOz9HOpreOysdSkz1U1IRDlF7J8yXUjUYIZkWNPTKoGqoHvSGSEldwe34oWZp0tAStoGr7IkWNFJHkvxFWdfqRrX1oKUBpETc3j7M0iICeh2alIw82akQDOLJMME28MuO"),
        Track("Dawn FM", "The Weeknd", "https://lh3.googleusercontent.com/aida-public/AB6AXuDJlrFigK4sbWpLI5KofdBF7m38P3Hebh2d5k2DJATC_biEwneGmUl-eTzcs0d9lrfLGKOWXjv44k3vTLdnhfBxQGwj1j32Ip2M0oWDVGaabAjL2cxHOJPNE_cioG-8OOTZ0aorphZb5D5BsAM0w70ICRPvLft9xr-CbDNkskCrnmqyGShngUD7kOFRrzYj4GszxxUFlm_8yPxV2785SZAHF6SSjWOKAuNEkG5wfROHzvS03HnvXWHt2UeZkgUyccrDDb58DOlteCXu")
    ) }"""
new_up_next = """    val upNext = remember { mutableStateListOf<Track>() }"""

old_later = """    val laterInQueue = remember { mutableStateListOf(
        Track("brutal", "Olivia Rodrigo", "https://lh3.googleusercontent.com/aida-public/AB6AXuCLmFTlSinLH9zgq7J0YsKsH9zm8TiUYvrhF2oI289wb3rlb5G1rG6uAbCWsR_bVfY1T649uF94WhU55aptElHRuq1AMTdVgkR84xbubzAT1sahJGQiEspAkbFQFJ0b_GQYVM9f0sD1qqKwQ4S_LFJNUIIa3eKlLyaQV1jR9_3hx3y4zrgr0MiH9VkxvX9rKej5EQrwFDotZ66cd-DAyBzTDFHl2BUnHs0MP4tlm4obfcogfUpEfHrxH8Uqizsww1crKNviM4VsR6-q"),
        Track("Beautiful Things", "Benson Boone", "https://lh3.googleusercontent.com/aida-public/AB6AXuAh0B18V_wVKHHyzG1XZwAVfyGuXIa5pCGtY-I-FQL0Ay-5xDzP7DaQ449tZjF7mwdmaEk53GxYlHWXeCBi9lU3ZWvU0HFqwhKW2N5WwRBhmqD-_hchDWI0XU1HMnjmYPm_4xr968yJ7NTmqIpWSEjvFheqTuVP4meIlkJ6Q6LILOpLyetj1Xtuq-cpHbIgHHoVMpTuH12dptZ-APmfaD-2vqFeZUe7bp219i92HMWFiyIgEKmD_l4ql_34XUWoniMY-ZKxkNesHXrw")
    ) }"""
new_later = """    val laterInQueue = remember { mutableStateListOf<Track>() }"""

content = content.replace(old_up_next, new_up_next).replace(old_later, new_later)

with open('app/src/main/java/com/example/QueueScreen.kt', 'w') as f:
    f.write(content)
