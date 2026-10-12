package com.aevum.app.ui.screens.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aevum.app.ui.theme.MidnightDark
import com.aevum.app.ui.theme.TextWhite

@Composable
fun StepsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightDark),
        contentAlignment = Alignment.Center
    ) {
        Text("Steps & Summit Climb", color = TextWhite)
    }
}
