package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountBalance
import com.example.data.model.KasTransaction
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedLight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.NavySecondary
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasTopAppBar(
    title: String,
    subtitle: String = "Sistem Kas Google Sheets",
    isConnected: Boolean = true,
    isSyncing: Boolean = false,
    onSyncClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isConnected) EmeraldLight else GoldLight,
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = "Status Koneksi",
                            tint = if (isConnected) EmeraldPrimary else GoldAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isConnected) "Sheets Live" else "Offline Queue",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isConnected) EmeraldPrimary else GoldAccent
                        )
                    }
                }

                IconButton(
                    onClick = onSyncClick,
                    enabled = !isSyncing,
                    modifier = Modifier.testTag("appbar_sync_button")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = EmeraldPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync Sekarang",
                            tint = EmeraldPrimary
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun AccountBalanceCard(
    account: AccountBalance,
    onClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() }
            .testTag("account_card_${account.name.replace(" ", "_")}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (account.type == "Kas") EmeraldLight else NavySecondary.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (account.type == "Kas") Icons.Default.Payments else Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = if (account.type == "Kas") EmeraldPrimary else NavySecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = account.type,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = account.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = FormatUtils.formatRupiah(account.currentBalance),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (account.currentBalance >= 0) EmeraldPrimary else DangerRed
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "In: " + FormatUtils.formatRupiah(account.totalIn),
                    fontSize = 10.sp,
                    color = EmeraldPrimary
                )
                Text(
                    text = "Out: " + FormatUtils.formatRupiah(account.totalOut),
                    fontSize = 10.sp,
                    color = DangerRed
                )
            }
        }
    }
}

@Composable
fun TransactionItemCard(
    transaction: KasTransaction,
    onItemClick: () -> Unit,
    onShareWhatsApp: () -> Unit
) {
    val isMasuk = transaction.type == "MASUK"
    val isTransfer = transaction.type == "TRANSFER"

    val iconBg = when {
        isMasuk -> EmeraldLight
        isTransfer -> GoldLight
        else -> DangerRedLight
    }

    val iconTint = when {
        isMasuk -> EmeraldPrimary
        isTransfer -> GoldAccent
        else -> DangerRed
    }

    val iconVector = when {
        isMasuk -> Icons.Default.ArrowDownward
        isTransfer -> Icons.Default.SwapHoriz
        else -> Icons.Default.ArrowUpward
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .testTag("tx_item_${transaction.id}")
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = transaction.type,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = transaction.transactionName.ifEmpty { transaction.description },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = (if (isMasuk) "+ " else if (isTransfer) "⇄ " else "- ") + FormatUtils.formatRupiah(transaction.amount),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = iconTint
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${transaction.account} • ${transaction.category}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${transaction.date} ${transaction.time.take(5)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                AnimatedVisibility(visible = transaction.proofUrl.isNotEmpty()) {
                    Text(
                        text = "📎 Ada Bukti Drive",
                        fontSize = 10.sp,
                        color = NavySecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            IconButton(
                onClick = onShareWhatsApp,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("share_wa_${transaction.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Kirim WhatsApp",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
