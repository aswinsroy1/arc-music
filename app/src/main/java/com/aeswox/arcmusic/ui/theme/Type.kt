package com.aeswox.arcmusic.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.aeswox.arcmusic.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val ManropeFont = GoogleFont("Manrope")

val ManropeFontFamily = FontFamily(
    Font(googleFont = ManropeFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = ManropeFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = ManropeFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = ManropeFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = ManropeFont, fontProvider = provider, weight = FontWeight.ExtraBold),
)

val CustomTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 52.sp,
        lineHeight = 62.sp,
        letterSpacing = (-0.02).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.05.sp
    ),
    // Set Manrope as default for all remaining Material3 roles
    displayMedium = TextStyle(fontFamily = ManropeFontFamily),
    displaySmall = TextStyle(fontFamily = ManropeFontFamily),
    headlineSmall = TextStyle(fontFamily = ManropeFontFamily),
    titleLarge = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.Medium),
    bodySmall = TextStyle(fontFamily = ManropeFontFamily),
    labelLarge = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontFamily = ManropeFontFamily, fontWeight = FontWeight.SemiBold),
)
