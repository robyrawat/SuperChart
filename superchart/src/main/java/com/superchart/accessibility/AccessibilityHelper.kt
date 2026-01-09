package com.superchart.accessibility

import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

/**
 * Accessibility helper to detect if TalkBack or other screen readers are enabled
 */
object AccessibilityHelper {

    /**
     * Check if accessibility services (TalkBack) are enabled on the device
     */
    fun isAccessibilityEnabled(context: Context): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        return accessibilityManager?.isEnabled == true && accessibilityManager.isTouchExplorationEnabled
    }

    /**
     * Check if TalkBack specifically is enabled
     */
    fun isTalkBackEnabled(context: Context): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        return accessibilityManager?.isTouchExplorationEnabled == true
    }

    /**
     * Get accessibility service info
     */
    fun getAccessibilityInfo(context: Context): AccessibilityStatus {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager

        return AccessibilityStatus(
            isEnabled = accessibilityManager?.isEnabled == true,
            isTouchExplorationEnabled = accessibilityManager?.isTouchExplorationEnabled == true,
            enabledServices = accessibilityManager?.getEnabledAccessibilityServiceList(
                android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            )?.map { it.id } ?: emptyList()
        )
    }
}

/**
 * Data class holding accessibility status
 */
data class AccessibilityStatus(
    val isEnabled: Boolean,
    val isTouchExplorationEnabled: Boolean,
    val enabledServices: List<String>
)

/**
 * Composable to remember accessibility state and auto-update
 */
@Composable
fun rememberAccessibilityState(): State<Boolean> {
    val context = LocalContext.current

    // Create state that updates when accessibility changes
    return remember {
        derivedStateOf {
            AccessibilityHelper.isAccessibilityEnabled(context)
        }
    }
}

/**
 * Composable effect that listens for accessibility changes
 */
@Composable
fun ObserveAccessibilityState(
    onAccessibilityChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager

        val listener = AccessibilityManager.AccessibilityStateChangeListener { enabled ->
            onAccessibilityChanged(enabled)
        }

        val touchExplorationListener = AccessibilityManager.TouchExplorationStateChangeListener { enabled ->
            onAccessibilityChanged(enabled)
        }

        accessibilityManager?.addAccessibilityStateChangeListener(listener)
        accessibilityManager?.addTouchExplorationStateChangeListener(touchExplorationListener)

        // Initial state
        onAccessibilityChanged(AccessibilityHelper.isAccessibilityEnabled(context))

        onDispose {
            accessibilityManager?.removeAccessibilityStateChangeListener(listener)
            accessibilityManager?.removeTouchExplorationStateChangeListener(touchExplorationListener)
        }
    }
}

