package com.aevum.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : Screen(
        route = "dashboard",
        title = "Day",
        icon = Icons.Rounded.Home
    )

    data object Steps : Screen(
        route = "steps",
        title = "Summit",
        icon = Icons.AutoMirrored.Rounded.DirectionsRun
    )

    data object Sleep : Screen(
        route = "sleep",
        title = "Sleep",
        icon = Icons.Rounded.Hotel
    )

    data object PulseAge : Screen(
        route = "pulse_age",
        title = "Pulse Age",
        icon = Icons.Rounded.Favorite
    )

    data object Streaks : Screen(
        route = "streaks",
        title = "Streaks",
        icon = Icons.Rounded.MilitaryTech
    )

    companion object {
        val items: List<Screen>
            get() = listOf(Dashboard, Steps, Sleep, PulseAge, Streaks)
    }
}
