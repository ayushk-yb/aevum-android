package com.aevum.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aevum.app.data.model.BiometricBadge
import com.aevum.app.ui.theme.CardBackgroundGlass
import com.aevum.app.ui.theme.CardBorderSubtle
import com.aevum.app.ui.theme.TextMuted
import com.aevum.app.ui.theme.TextSecondary
import com.aevum.app.ui.theme.TextWhite

@Composable
fun BadgesCarousel(
    badges: List<BiometricBadge>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Badges",
            color = TextWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            badges.forEach { badge ->
                GlassmorphicCard(
                    modifier = Modifier.width(130.dp),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = CardBackgroundGlass,
                    borderColor = CardBorderSubtle
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Badge Emblem Circle
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (badge.isUnlocked) badge.iconColor.copy(alpha = 0.18f)
                                    else Color(0xFF22222E)
                                )
                                .border(
                                    1.dp,
                                    if (badge.isUnlocked) badge.iconColor.copy(alpha = 0.5f)
                                    else Color(0x22FFFFFF),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (badge.isUnlocked) Icons.Rounded.Stars else Icons.Rounded.Lock,
                                contentDescription = badge.title,
                                tint = if (badge.isUnlocked) badge.iconColor else TextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Text(
                            text = badge.title,
                            color = if (badge.isUnlocked) TextWhite else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            modifier = Modifier.padding(top = 10.dp)
                        )

                        Text(
                            text = badge.category,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            maxLines = 1,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
