package com.dev.trackone.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ============================================================================
// Credential Module — Google Blue Theme
// ============================================================================

private val CredentialBlue = Color(0xFF1A73E8)
private val CredentialBlueDark = Color(0xFF174EA6)
private val CredentialBlueLight = Color(0xFFD3E3FD)
private val CredentialBlueSurface = Color(0xFFF0F4FF)

private val CredentialLightScheme = lightColorScheme(
    primary = CredentialBlue,
    onPrimary = Color.White,
    primaryContainer = CredentialBlueLight,
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF5F6368),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8EAED),
    onSecondaryContainer = Color(0xFF1F1F1F),
    tertiary = Color(0xFF7BAAF7),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE8F0FE),
    onTertiaryContainer = Color(0xFF002D6D),
    error = Color(0xFFD93025),
    onError = Color.White,
    errorContainer = Color(0xFFFCE8E6),
    onErrorContainer = Color(0xFF3B0607),
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF1F1F1F),
    surface = Color.White,
    onSurface = Color(0xFF1F1F1F),
    surfaceVariant = CredentialBlueSurface,
    onSurfaceVariant = Color(0xFF5F6368),
    outline = Color(0xFFDADCE0),
    outlineVariant = Color(0xFFE8EAED),
    inverseSurface = Color(0xFF303134),
    inverseOnSurface = Color(0xFFF1F3F4),
    inversePrimary = Color(0xFF8AB4F8),
    surfaceTint = CredentialBlue,
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFE8EAED),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8F9FA),
    surfaceContainer = Color(0xFFF1F3F4),
    surfaceContainerHigh = Color(0xFFE8EAED),
    surfaceContainerHighest = Color(0xFFDADCE0),
)

@Composable
fun CredentialModuleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CredentialLightScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content,
    )
}

// ============================================================================
// Expense Module — Google Orange Theme
// ============================================================================

private val ExpenseOrange = Color(0xFFE8710A)
private val ExpenseOrangeDark = Color(0xFFC25E09)
private val ExpenseOrangeLight = Color(0xFFFEF0E1)
private val ExpenseOrangeSurface = Color(0xFFFFF8F0)

// Semantic: Income green, Expense red — matching the orange page
private val ExpenseIncomeGreen = Color(0xFF188038)
private val ExpenseDebitRed = Color(0xFFC5221F)

private val ExpenseLightScheme = lightColorScheme(
    primary = ExpenseOrange,
    onPrimary = Color.White,
    primaryContainer = ExpenseOrangeLight,
    onPrimaryContainer = Color(0xFF311300),
    secondary = Color(0xFF795548),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEFEBE9),
    onSecondaryContainer = Color(0xFF3E2723),
    tertiary = ExpenseIncomeGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCEEAD6),
    onTertiaryContainer = Color(0xFF002109),
    error = ExpenseDebitRed,
    onError = Color.White,
    errorContainer = Color(0xFFFCE8E6),
    onErrorContainer = Color(0xFF3B0607),
    background = Color(0xFFFFFBF8),
    onBackground = Color(0xFF1F1F1F),
    surface = Color.White,
    onSurface = Color(0xFF1F1F1F),
    surfaceVariant = ExpenseOrangeSurface,
    onSurfaceVariant = Color(0xFF5F6368),
    outline = Color(0xFFDADCE0),
    outlineVariant = Color(0xFFE8EAED),
    inverseSurface = Color(0xFF303134),
    inverseOnSurface = Color(0xFFF1F3F4),
    inversePrimary = Color(0xFFFBBC04),
    surfaceTint = ExpenseOrange,
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFE8EAED),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFFFBF8),
    surfaceContainer = Color(0xFFF5F0EB),
    surfaceContainerHigh = Color(0xFFEFEBE9),
    surfaceContainerHighest = Color(0xFFE0DBD8),
)

@Composable
fun ExpenseModuleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ExpenseLightScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content,
    )
}

// ============================================================================
// Expiry Module — Google Green Theme
// ============================================================================

private val ExpiryGreen = Color(0xFF188038)
private val ExpiryGreenDark = Color(0xFF137333)
private val ExpiryGreenLight = Color(0xFFCEEAD6)
private val ExpiryGreenSurface = Color(0xFFF0F9F2)

// Semantic: Non-expired = softer green, Expired = muted warm red (matching green page)
val ExpiryActiveGreen = Color(0xFF188038)
val ExpiryExpiredRed = Color(0xFFB3261E)

private val ExpiryLightScheme = lightColorScheme(
    primary = ExpiryGreen,
    onPrimary = Color.White,
    primaryContainer = ExpiryGreenLight,
    onPrimaryContainer = Color(0xFF002109),
    secondary = Color(0xFF4E6355),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0E8D6),
    onSecondaryContainer = Color(0xFF0C1F13),
    tertiary = Color(0xFF3C6939),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFBCF0B4),
    onTertiaryContainer = Color(0xFF002204),
    error = ExpiryExpiredRed,
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFF8FAF8),
    onBackground = Color(0xFF1F1F1F),
    surface = Color.White,
    onSurface = Color(0xFF1F1F1F),
    surfaceVariant = ExpiryGreenSurface,
    onSurfaceVariant = Color(0xFF5F6368),
    outline = Color(0xFFDADCE0),
    outlineVariant = Color(0xFFE8EAED),
    inverseSurface = Color(0xFF303134),
    inverseOnSurface = Color(0xFFF1F3F4),
    inversePrimary = Color(0xFF81C995),
    surfaceTint = ExpiryGreen,
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFE8EAED),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8FAF8),
    surfaceContainer = Color(0xFFF0F4F1),
    surfaceContainerHigh = Color(0xFFE2EBE4),
    surfaceContainerHighest = Color(0xFFD5DED7),
)

@Composable
fun ExpiryModuleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ExpiryLightScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content,
    )
}

@Composable
fun DynamicModuleTheme(moduleTitle: String, content: @Composable () -> Unit) {
    when {
        moduleTitle.contains("Credential", ignoreCase = true) -> {
            CredentialModuleTheme(content = content)
        }
        moduleTitle.contains("Expense", ignoreCase = true) -> {
            ExpenseModuleTheme(content = content)
        }
        moduleTitle.contains("Expiry", ignoreCase = true) -> {
            ExpiryModuleTheme(content = content)
        }
        else -> {
            content()
        }
    }
}
