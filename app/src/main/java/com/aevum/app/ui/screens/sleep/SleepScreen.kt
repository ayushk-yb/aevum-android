package com.aevum.app.ui.screens.sleep

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
fun SleepScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightDark),
        contentAlignment = Alignment.Center
    ) {
        Text("Sleep & Hypnogram", color = TextWhite)
    }
}
