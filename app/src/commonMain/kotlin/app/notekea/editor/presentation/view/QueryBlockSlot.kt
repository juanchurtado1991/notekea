package app.notekea.editor.presentation.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

/**
 * How Read mode draws a query block: the vault's window gives it, as features do not know each other, and without
 * one the block is the code it is. The block's YAML and its whole text are given.
 */
val LocalQueryBlock = compositionLocalOf<(@Composable (String, String) -> Unit)?> { null }
