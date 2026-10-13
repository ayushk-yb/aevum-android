package com.aevum.app.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoMode
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aevum.app.ui.theme.AmberGlow
import com.aevum.app.ui.theme.CardBackgroundGlass
import com.aevum.app.ui.theme.CardBorderSubtle
import com.aevum.app.ui.theme.CyanGlow
import com.aevum.app.ui.theme.EmeraldGlow
import com.aevum.app.ui.theme.TextMuted
import com.aevum.app.ui.theme.TextSecondary
import com.aevum.app.ui.theme.TextWhite
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ConfigureAgeDialog(
    initialAge: Int,
    initialBirthDateIso: String? = null,
    onSaveAge: (Int) -> Unit,
    onSaveBirthDate: (Int, Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedAge by remember { mutableIntStateOf(initialAge) }
    var selectedBirthDateIso by remember { mutableStateOf(initialBirthDateIso) }

    val parsedBirthDate: LocalDate? = remember(selectedBirthDateIso) {
        selectedBirthDateIso?.let {
            try { LocalDate.parse(it) } catch (_: Exception) { null }
        }
    }

    val displayFormatter = remember { DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.US) }

    fun showDatePicker() {
        val now = LocalDate.now(ZoneId.systemDefault())
        val initialYear = parsedBirthDate?.year ?: (now.year - selectedAge)
        val initialMonth = (parsedBirthDate?.monthValue ?: now.monthValue) - 1
        val initialDay = parsedBirthDate?.dayOfMonth ?: now.dayOfMonth

        val dpd = DatePickerDialog(
            context,
            { _, year, monthOfYear, dayOfMonth ->
                val newDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                val computedAge = Period.between(newDate, now).years.coerceIn(10, 115)
                selectedBirthDateIso = newDate.toString()
                selectedAge = computedAge
            },
            initialYear,
            initialMonth,
            initialDay
        )
        // Disallow future birthdays
        dpd.datePicker.maxDate = System.currentTimeMillis()
        dpd.show()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, CardBorderSubtle, RoundedCornerShape(26.dp)),
            color = Color(0xFF14141C),
            shape = RoundedCornerShape(26.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AmberGlow.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Cake,
                        contentDescription = null,
                        tint = AmberGlow,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Calendar Age & Birthday",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Set your date of birth so Aevum automatically increments your chronological age on every birthday, continuously updating your biological pulse delta.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 6.dp, bottom = 16.dp),
                    textAlign = TextAlign.Center
                )

                // Date of Birth Picker Button (Auto-Updating)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (parsedBirthDate != null) EmeraldGlow.copy(alpha = 0.12f) else CardBackgroundGlass)
                        .border(
                            1.dp,
                            if (parsedBirthDate != null) EmeraldGlow.copy(alpha = 0.5f) else Color(0x1AFFFFFF),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { showDatePicker() }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (parsedBirthDate != null) EmeraldGlow.copy(alpha = 0.2f) else Color(0xFF262634)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarMonth,
                                    contentDescription = null,
                                    tint = if (parsedBirthDate != null) EmeraldGlow else TextWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = if (parsedBirthDate != null) parsedBirthDate.format(displayFormatter) else "Set Date of Birth",
                                    color = TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (parsedBirthDate != null) "Auto-updates age on birthday" else "Tap to choose birthday",
                                    color = if (parsedBirthDate != null) EmeraldGlow else TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF22222E))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (parsedBirthDate != null) "Change" else "Select",
                                color = CyanGlow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Age Adjuster Control
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardBackgroundGlass)
                        .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(20.dp))
                        .padding(vertical = 10.dp, horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Minus button
                        IconButton(
                            onClick = {
                                if (selectedAge > 15) {
                                    selectedAge--
                                    selectedBirthDateIso = null // Clear strict DOB if manually overridden
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF262634))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Remove,
                                contentDescription = "Decrease",
                                tint = TextWhite
                            )
                        }

                        // Age display
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$selectedAge",
                                color = TextWhite,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-1.5).sp
                            )
                            Text(
                                text = "years old",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        // Plus button
                        IconButton(
                            onClick = {
                                if (selectedAge < 100) {
                                    selectedAge++
                                    selectedBirthDateIso = null // Clear strict DOB if manually overridden
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF262634))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Increase",
                                tint = TextWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Button(
                    onClick = {
                        val dob = parsedBirthDate
                        if (dob != null) {
                            onSaveBirthDate(dob.year, dob.monthValue, dob.dayOfMonth)
                        } else {
                            onSaveAge(selectedAge)
                        }
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGlow),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = if (parsedBirthDate != null) "Save Birthday & Auto-Age" else "Save Calendar Age",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22222E)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "Cancel",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
