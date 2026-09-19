package com.tanmay.buddie.presentation.screens.settings

import android.os.Build
import java.util.Locale

/**
 * Tailored guidance for aggressive battery management across different Android OEM skins.
 */
enum class BatteryGuideBrand(
    val brandName: String,
    val skinName: String,
    val steps: List<String>,
    val proTip: String? = null
) {
    XIAOMI(
        brandName = "Xiaomi / Redmi / POCO",
        skinName = "MIUI / HyperOS",
        steps = listOf(
            "Open Settings > Apps > Manage apps > Buddie.",
            "Turn ON 'Autostart' (and 'Background autostart' if shown).",
            "Tap 'Battery saver' and choose 'No restrictions'.",
            "In 'Other permissions', ensure 'Display pop-up windows while running in the background' is Allowed."
        ),
        proTip = "In the Recent Apps overview, long-press Buddie's card and tap the Lock icon so MIUI does not kill it when clearing apps."
    ),

    SAMSUNG(
        brandName = "Samsung",
        skinName = "One UI",
        steps = listOf(
            "Open Settings > Apps > Buddie > Battery and select 'Unrestricted'.",
            "Go to Settings > Battery and device care > Battery > Background usage limits.",
            "Tap 'Never sleeping apps', tap '+' and add Buddie.",
            "Ensure 'Auto optimization' (Restart when needed) does not force-close background apps."
        ),
        proTip = "Turn off 'Put unused apps to sleep' in Background usage limits to prevent One UI from freezing Buddie."
    ),

    OPPO(
        brandName = "Oppo / Realme",
        skinName = "ColorOS / Realme UI",
        steps = listOf(
            "Open Settings > Apps > App management > Buddie > Battery usage.",
            "Turn ON 'Allow background activity' and 'Allow auto-launch'.",
            "Go to Settings > Battery > More settings > App battery management > Buddie, and select 'Don't optimize'.",
            "Ensure 'Display over other apps' permission is Allowed."
        ),
        proTip = "In Recent Tasks, swipe down on Buddie's window and tap the Lock icon to pin it in memory."
    ),

    VIVO(
        brandName = "Vivo / iQOO",
        skinName = "Funtouch OS / OriginOS",
        steps = listOf(
            "Open Settings > Battery > Background power consumption management > Buddie.",
            "Choose 'High background power consumption' (or 'Don't restrict').",
            "Go to Settings > Apps > Permission manager > Autostart, and enable Buddie.",
            "Ensure 'Floating window' / 'Display over other apps' is Allowed."
        ),
        proTip = "Pull down on Buddie in Recent Apps and tap the Lock icon so Funtouch OS preserves it."
    ),

    ONEPLUS(
        brandName = "OnePlus",
        skinName = "OxygenOS",
        steps = listOf(
            "Open Settings > Apps > App management > Buddie > Battery usage.",
            "Enable 'Allow background activity' and 'Allow auto-launch'.",
            "Go to Settings > Battery > More settings > Optimize battery use > Buddie, and choose 'Don't optimize'.",
            "Ensure 'Sleep standby optimization' does not restrict Buddie during doze."
        ),
        proTip = "Lock Buddie in Recent Apps by tapping the three dots on its card and selecting 'Lock'."
    ),

    MOTOROLA(
        brandName = "Motorola",
        skinName = "My UX / Hello UI",
        steps = listOf(
            "Open Settings > Apps > All apps > Buddie > Battery.",
            "Select 'Unrestricted' under Manage battery usage.",
            "Under App info, turn OFF 'Pause app activity if unused'.",
            "If Motorola Adaptive Battery is active, exclude Buddie from background limits."
        ),
        proTip = "Motorola's stock-like software respects the 'Unrestricted' battery setting with minimal extra restrictions."
    ),

    GENERIC(
        brandName = "Generic Android / Pixel",
        skinName = "Stock Android",
        steps = listOf(
            "Open Settings > Apps > All apps > Buddie > Battery.",
            "Select 'Unrestricted' battery usage.",
            "Under App info, toggle OFF 'Pause app activity if unused'.",
            "Ensure 'Display over other apps' permission remains Allowed."
        ),
        proTip = "On Google Pixel or AOSP devices, selecting 'Unrestricted' prevents background service termination."
    );

    companion object {
        fun detect(manufacturer: String = Build.MANUFACTURER ?: "", brand: String = Build.BRAND ?: ""): BatteryGuideBrand {
            val combined = "${manufacturer.trim()} ${brand.trim()}".lowercase(Locale.ROOT)
            return when {
                combined.contains("xiaomi") || combined.contains("redmi") || combined.contains("poco") -> XIAOMI
                combined.contains("samsung") -> SAMSUNG
                combined.contains("oppo") || combined.contains("realme") -> OPPO
                combined.contains("vivo") || combined.contains("iqoo") -> VIVO
                combined.contains("oneplus") -> ONEPLUS
                combined.contains("motorola") || combined.contains("moto") || combined.contains("lenovo") -> MOTOROLA
                else -> GENERIC
            }
        }
    }
}
