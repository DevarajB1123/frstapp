package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ItemEntity
import com.example.data.model.MatchEntity
import com.example.ui.LostAndFoundViewModel
import com.example.ui.components.ItemCard
import com.example.ui.theme.CampusAmber
import com.example.ui.theme.CampusBluePrimary
import com.example.ui.theme.CampusBlueSecondary
import com.example.ui.theme.FoundGreen
import com.example.ui.theme.LostRed
import com.example.ui.theme.MatchAmber
import com.example.ui.theme.MatchAmberBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: LostAndFoundViewModel,
    onReportLost: () -> Unit,
    onReportFound: () -> Unit,
    onSearchClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenAuth: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val approvedItems by viewModel.allApprovedItems.collectAsState()
    val allMatches by viewModel.allMatches.collectAsState()

    var activeTab by remember { mutableStateOf("ALL") } // "ALL", "LOST", "FOUND"

    val recentItems = remember(approvedItems, activeTab) {
        val filtered = when (activeTab) {
            "LOST" -> approvedItems.filter { it.itemType.equals("LOST", ignoreCase = true) }
            "FOUND" -> approvedItems.filter { it.itemType.equals("FOUND", ignoreCase = true) }
            else -> approvedItems
        }
        filtered.take(15)
    }

    val lostCount = approvedItems.count { it.itemType.equals("LOST", ignoreCase = true) && it.status != "Returned" }
    val foundCount = approvedItems.count { it.itemType.equals("FOUND", ignoreCase = true) && it.status != "Returned" }
    val returnedCount = approvedItems.count { it.status == "Returned" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // TOP USER BAR
        item {
            Surface(
                color = CampusBluePrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentUser != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenDashboard() }
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = currentUser!!.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${currentUser!!.collegeId} • ${currentUser!!.role.replaceFirstChar { it.uppercase() }}",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Guest User",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (currentUser?.role == "admin") {
                            IconButton(onClick = onOpenAdmin) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin Panel",
                                    tint = CampusAmber
                                )
                            }
                        }

                        IconButton(onClick = onOpenDashboard) {
                            Icon(
                                imageVector = Icons.Default.Dashboard,
                                contentDescription = "Dashboard",
                                tint = Color.White
                            )
                        }

                        IconButton(onClick = onOpenAuth) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Switch Account",
                                tint = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }

        // HERO HEADER SECTION
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                CampusBluePrimary,
                                CampusBlueSecondary
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "COLLEGE LOST & FOUND",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Find what you've lost. Return what you've found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // PRIMARY ACTIONS: REPORT LOST & REPORT FOUND
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onReportLost,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LostRed,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("btn_report_lost")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("REPORT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("LOST ITEM", fontSize = 14.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        Button(
                            onClick = onReportFound,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FoundGreen,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("btn_report_found")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("REPORT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("FOUND ITEM", fontSize = 14.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // SEARCH BAR LAUNCHER
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSearchClick() }
                            .testTag("btn_home_search"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = CampusBluePrimary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Search Items (e.g. Black Wallet, CSE Block, Keys...)",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // CAMPUS METRICS STATS BAR
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Active Lost",
                    count = lostCount.toString(),
                    color = LostRed,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Active Found",
                    count = foundCount.toString(),
                    color = FoundGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Reunited",
                    count = returnedCount.toString(),
                    color = Color(0xFF7C3AED),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // POSSIBLE MATCH HIGHLIGHT BANNER (If any matches exist)
        if (allMatches.isNotEmpty()) {
            item {
                val topMatch = allMatches.first()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onItemClick(topMatch.lostItemId) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MatchAmberBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MatchAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Possible Match Identified!",
                                    fontWeight = FontWeight.Bold,
                                    color = MatchAmber,
                                    fontSize = 15.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MatchAmber
                            ) {
                                Text(
                                    text = "${topMatch.matchScore}% Match",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = topMatch.matchReasons,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF92400E)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Compare & Verify Item Details →",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CampusBluePrimary
                            )
                        }
                    }
                }
            }
        }

        // RECENT ITEMS HEADER & FILTER TABS
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Campus Reports",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "View All (${approvedItems.size})",
                        color = CampusBluePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { onSearchClick() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = activeTab == "ALL",
                        onClick = { activeTab = "ALL" },
                        label = { Text("All Reports (${approvedItems.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CampusBluePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = activeTab == "LOST",
                        onClick = { activeTab = "LOST" },
                        label = { Text("Lost Items") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LostRed,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = activeTab == "FOUND",
                        onClick = { activeTab = "FOUND" },
                        label = { Text("Found Items") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FoundGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // RECENT ITEMS LIST
        if (recentItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No items reported in this category yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(recentItems, key = { it.itemId }) { item ->
                val hasMatch = allMatches.any { it.lostItemId == item.itemId || it.foundItemId == item.itemId }
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    ItemCard(
                        item = item,
                        hasPossibleMatch = hasMatch,
                        onClick = { onItemClick(item.itemId) }
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
