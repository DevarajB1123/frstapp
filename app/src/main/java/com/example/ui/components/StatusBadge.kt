package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FoundGreen
import com.example.ui.theme.FoundGreenBg
import com.example.ui.theme.LostRed
import com.example.ui.theme.LostRedBg
import com.example.ui.theme.MatchAmber
import com.example.ui.theme.MatchAmberBg
import com.example.ui.theme.ReturnedPurple
import com.example.ui.theme.ReturnedPurpleBg

@Composable
fun ItemTypeBadge(type: String, modifier: Modifier = Modifier) {
    val isLost = type.equals("LOST", ignoreCase = true)
    val bgColor = if (isLost) LostRedBg else FoundGreenBg
    val textColor = if (isLost) LostRed else FoundGreen

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isLost) "LOST ITEM" else "FOUND ITEM",
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun ItemStatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (status) {
        "Possible Match" -> Triple(MatchAmberBg, MatchAmber, Icons.Default.AutoAwesome)
        "Returned" -> Triple(ReturnedPurpleBg, ReturnedPurple, Icons.Default.CheckCircle)
        "Claimed" -> Triple(Color(0xFFE0F2FE), Color(0xFF0369A1), Icons.Default.HourglassTop)
        "Closed" -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), null)
        else -> Triple(Color(0xFFEFF6FF), Color(0xFF1D4ED8), Icons.Default.Search)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier
                    .size(12.dp)
                    .padding(end = 4.dp)
            )
        }
        Text(
            text = status,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
