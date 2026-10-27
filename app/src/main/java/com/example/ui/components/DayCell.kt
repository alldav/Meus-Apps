package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrazilianHoliday
import com.example.data.model.DayStatus
import com.example.ui.theme.FeriadoGold
import com.example.ui.theme.FolgaGreen
import com.example.ui.theme.FtAmber
import com.example.ui.theme.PlantaoBlue
import com.example.ui.theme.TrocaPurple
import com.example.ui.theme.TrocaWorkCyan
import com.example.ui.theme.VacationTeal
import java.time.LocalDate

@Composable
fun DayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    status: DayStatus,
    holiday: BrazilianHoliday?,
    hasAppointments: Boolean,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusLabel, textColor) = when (status) {
        DayStatus.OFF -> Triple(FolgaGreen, "Folga", Color.White)
        DayStatus.WORK -> Triple(PlantaoBlue, "Plantão", Color.White)
        DayStatus.FT -> Triple(FtAmber, "FT", Color.White)
        DayStatus.SWAP -> Triple(TrocaPurple, "Folga (T)", Color.White)
        DayStatus.SWAP_WORK -> Triple(TrocaWorkCyan, "Plantão (T)", Color.White)
        DayStatus.VACATION -> Triple(VacationTeal, "Férias", Color.White)
    }

    val cellBackground = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    } else if (!isCurrentMonth) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.surface
    }

    val cellBorder = if (isSelected) {
        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
    } else if (isToday) {
        Modifier.border(1.5.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp))
    } else {
        Modifier.border(0.5.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    }

    Box(
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(12.dp))
            .background(cellBackground)
            .then(cellBorder)
            .clickable { onSelectDate(date) }
            .testTag("day_cell_${date}")
            .padding(4.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row: Day number and Holiday/Appointment dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    fontSize = 15.sp,
                    fontWeight = if (isToday || isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                    color = when {
                        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        isToday -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (holiday != null) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = holiday.name,
                            tint = FeriadoGold,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    if (hasAppointments) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Compromisso",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }

            // Bottom badge with Shift Status Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isCurrentMonth) statusColor else statusColor.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = statusLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
