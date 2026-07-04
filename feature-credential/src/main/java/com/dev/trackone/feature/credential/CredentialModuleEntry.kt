package com.dev.trackone.feature.credential

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.dev.trackone.core.navigation.ModuleEntry
import com.dev.trackone.feature.credential.navigation.CredentialNavigation
import javax.inject.Inject

/**
 * Entry point for the Credential Manager module.
 */
class CredentialModuleEntry @Inject constructor() : ModuleEntry {

    override val title: String = "Credentials"
    override val icon: ImageVector = Icons.Default.VpnKey
    override val order: Int = 2

    @Composable
    override fun Content() {
        CredentialNavigation()
    }
}
