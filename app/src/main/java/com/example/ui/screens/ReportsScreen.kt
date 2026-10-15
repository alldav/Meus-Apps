package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayStatus
import com.example.ui.theme.FolgaGreen
import com.example.ui.theme.FtAmber
import com.example.ui.theme.PlantaoBlue
import com.example.ui.theme.TrocaPurple
import com.example.ui.viewmodel.ShiftUiState
import com.example.ui.viewmodel.ShiftViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: ShiftViewModel,
    uiState: ShiftUiState
) {
    val context = LocalContext.current
    val currentMonth = uiState.currentMonth
    val monthTitle = currentMonth.format(DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() }

    // Filter overrides for this month
    val monthOverrides = uiState.overrides.values.filter {
        it.date.year == currentMonth.year && it.date.month == currentMonth.month
    }.sortedBy { it.date }

    val ftList = monthOverrides.filter { it.status == DayStatus.FT }
    val swapList = monthOverrides.filter { it.status == DayStatus.SWAP }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.onMonthChange(currentMonth.minusMonths(1)) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mês anterior")
                    }
                    Text(
                        text = monthTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { viewModel.onMonthChange(currentMonth.plusMonths(1)) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Próximo mês")
                    }
                }
            }
        }

        // Summary Metric Tiles
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Resumo de Produtividade & Escala",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MetricCard(
                            count = ftList.size,
                            label = "FTs Feitas",
                            color = FtAmber,
                            icon = Icons.Default.Paid
                        )
                        MetricCard(
                            count = swapList.size,
                            label = "Trocas",
                            color = TrocaPurple,
                            icon = Icons.Default.SwapHoriz
                        )
                        MetricCard(
                            count = uiState.monthSummary?.totalWorkDays ?: 0,
                            label = "Plantões",
                            color = PlantaoBlue,
                            icon = null
                        )
                        MetricCard(
                            count = uiState.monthSummary?.totalOffDays ?: 0,
                            label = "Folgas Gozadas",
                            color = FolgaGreen,
                            icon = null
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Share Report via WhatsApp
                    OutlinedButton(
                        onClick = {
                            shareMonthlyReport(
                                context = context,
                                monthTitle = monthTitle,
                                ftCount = ftList.size,
                                swapCount = swapList.size,
                                workCount = uiState.monthSummary?.totalWorkDays ?: 0,
                                offCount = uiState.monthSummary?.totalOffDays ?: 0,
                                fts = ftList.map { "${it.date.format(DateTimeFormatter.ofPattern("dd/MM"))}: ${it.extraValueOrLocation ?: "FT realizada"} ${it.notes?.let { n -> "($n)" } ?: ""}" }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("share_report_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compartilhar Resumo no WhatsApp")
                    }
                }
            }
        }

        // Section: Folgas Trabalhadas (FT)
        item {
            Text(
                text = "Folgas Trabalhadas - FTs (${ftList.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FtAmber
            )
        }

        if (ftList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Nenhuma Folga Trabalhada (FT) registrada neste mês. Para registrar, selecione uma folga no calendário e clique em 'Fazer FT'.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(ftList, key = { it.date.toString() }) { ft ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = FtAmber.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FtAmber.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ft.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy (EEEE)", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (!ft.extraValueOrLocation.isNullOrBlank()) {
                                Text(
                                    text = "Posto / Valor: ${ft.extraValueOrLocation}",
                                    fontSize = 13.sp,
                                    color = FtAmber,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (!ft.notes.isNullOrBlank()) {
                                Text(
                                    text = "Obs: ${ft.notes}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.removeOverride(ft.date) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remover FT", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // Section: Trocas de Plantão
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Trocas de Plantão (${swapList.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TrocaPurple
            )
        }

        if (swapList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "Nenhuma troca de plantão registrada neste mês.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(swapList, key = { it.date.toString() }) { swap ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TrocaPurple.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TrocaPurple.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = swap.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy (EEEE)", Locale("pt", "BR"))).replaceFirstChar { it.uppercase() },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (!swap.partnerName.isNullOrBlank()) {
                                Text(
                                    text = "Colega: ${swap.partnerName}",
                                    fontSize = 13.sp,
                                    color = TrocaPurple,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (!swap.extraValueOrLocation.isNullOrBlank()) {
                                Text(
                                    text = "Compensação: ${swap.extraValueOrLocation}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (swap.paybackDate != null) {
                                Text(
                                    text = "Pagar em: ${swap.paybackDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} ${if (swap.paybackCompleted) "✓ (Pago)" else "(Pendente)"}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (swap.paybackCompleted) FolgaGreen else TrocaPurple
                                )
                            }
                            if (!swap.notes.isNullOrBlank()) {
                                Text(
                                    text = "Obs: ${swap.notes}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.removeOverride(swap.date) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remover Troca", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    count: Int,
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector?
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun shareMonthlyReport(
    context: Context,
    monthTitle: String,
    ftCount: Int,
    swapCount: Int,
    workCount: Int,
    offCount: Int,
    fts: List<String>
) {
    val sb = StringBuilder()
    sb.appendLine("📋 *Relatório de Escala & FTs - $monthTitle*")
    sb.appendLine("• Plantões Ordinários: $workCount")
    sb.appendLine("• Folgas Gozadas: $offCount")
    sb.appendLine("• Folgas Trabalhadas (FT): $ftCount")
    sb.appendLine("• Trocas de Plantão: $swapCount")
    if (fts.isNotEmpty()) {
        sb.appendLine("\n💼 *Detalhamento de FTs:*")
        fts.forEach { sb.appendLine("  $it") }
    }
    sb.appendLine("\n_Gerado pelo app Escala Medina_")

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar Resumo de Escala"))
}
