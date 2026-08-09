package com.dev.trackone.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ============================================================================
// Unified App Theme
// ----------------------------------------------------------------------------
// The app now uses a SINGLE color theme (the blue TrackOneTheme applied at the
// root) across every module. These per-module wrappers are kept only so existing
// call sites keep compiling; each one simply renders its content with the
// inherited app theme instead of swapping in a green/orange/blue palette.
// ============================================================================

@Composable
fun CredentialModuleTheme(content: @Composable () -> Unit) = content()

@Composable
fun ExpenseModuleTheme(content: @Composable () -> Unit) = content()

@Composable
fun ExpiryModuleTheme(content: @Composable () -> Unit) = content()

@Composable
fun DynamicModuleTheme(moduleTitle: String, content: @Composable () -> Unit) = content()

// ============================================================================
// Semantic status colors used by Expiry screens (kept module-agnostic).
// ============================================================================

val ExpiryActiveGreen = Color(0xFF188038)
val ExpiryExpiredRed = Color(0xFFB3261E)
