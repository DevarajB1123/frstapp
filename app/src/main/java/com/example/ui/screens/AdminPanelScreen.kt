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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ItemEntity
import com.example.ui.LostAndFoundViewModel
import com.example.ui.components.ItemStatusBadge
import com.example.ui.components.ItemTypeBadge
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.CampusAmber
import com.example.ui.theme.CampusBluePrimary
import com.example.ui.theme.FoundGreen
import com.example.ui.theme.LostRed
import com.example.ui.theme.MatchAmber
import com.example.ui.theme.MatchAmberBg
import com.example.ui.theme.ReturnedPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: LostAndFoundViewModel,
    onNavigateBack: () -> Unit,
    onItemClick: (Long) -> Unit
) {
    val allItems by viewModel.allItemsAdmin.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allMatches by viewModel.allMatches.collectAsState()
    val allClaims by viewModel.allClaims.collectAsState()

    var selectedAdminTab by remember { mutableIntStateOf(0) } // 0 = Reports, 1 = Users, 2 = Matches
    var reportFilter by remember { mutableStateOf("ALL") } // "ALL", "LOST", "FOUND", "Possible Match", "Returned"

    val filteredAdminItems = remember(allItems, reportFilter) {
        when (reportFilter) {
            "LOST" -> allItems.filter { it.itemType.equals("LOST", ignoreCase = true) }
            "FOUND" -> allItems.filter { it.itemType.equals("FOUND", ignoreCase = true) }
            "Possible Match" -> allItems.filter { it.status == "Possible Match" }
            "Returned" -> allItems.filter { it.status == "Returned" }
            else -> allItems
        }
    }

    val totalLost = allItems.count { it.itemType.equals("LOST", ignoreCase = true) }
    val totalFound = allItems.count { it.itemType.equals("FOUND", ignoreCase = true) }
    val totalReturned = allItems.count { it.status == "Returned" }
    val resolutionRate = if (allItems.isNotEmpty()) ((totalReturned.toFloat() / allItems.size) * 100).toInt() else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = CampusAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("College Admin Authority", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ADMIN OVERVIEW METRICS
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Campus Lost & Found Analytics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CampusBluePrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AdminStatBox(title = "Total Reports", value = allItems.size.toString(), color = CampusBluePrimary, modifier = Modifier.weight(1f))
                            AdminStatBox(title = "Lost Items", value = totalLost.toString(), color = LostRed, modifier = Modifier.weight(1f))
                            AdminStatBox(title = "Found Items", value = totalFound.toString(), color = FoundGreen, modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AdminStatBox(title = "Returned", value = totalReturned.toString(), color = ReturnedPurple, modifier = Modifier.weight(1f))
                            AdminStatBox(title = "Resolution", value = "$resolutionRate%", color = FoundGreen, modifier = Modifier.weight(1f))
                            AdminStatBox(title = "Matches", value = allMatches.size.toString(), color = MatchAmber, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // ADMIN NAVIGATION TABS
            item {
                TabRow(
                    selectedTabIndex = selectedAdminTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = CampusBluePrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Tab(
                        selected = selectedAdminTab == 0,
                        onClick = { selectedAdminTab = 0 },
                        text = { Text("Reports (${allItems.size})", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedAdminTab == 1,
                        onClick = { selectedAdminTab = 1 },
                        text = { Text("Users (${allUsers.size})", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedAdminTab == 2,
                        onClick = { selectedAdminTab = 2 },
                        text = { Text("Matches (${allMatches.size})", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            when (selectedAdminTab) {
                0 -> {
                    // REPORTS MANAGEMENT TAB
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = reportFilter == "ALL",
                                onClick = { reportFilter = "ALL" },
                                label = { Text("All (${allItems.size})", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = reportFilter == "LOST",
                                onClick = { reportFilter = "LOST" },
                                label = { Text("Lost", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = reportFilter == "FOUND",
                                onClick = { reportFilter = "FOUND" },
                                label = { Text("Found", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = reportFilter == "Possible Match",
                                onClick = { reportFilter = "Possible Match" },
                                label = { Text("Matches", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = reportFilter == "Returned",
                                onClick = { reportFilter = "Returned" },
                                label = { Text("Returned", fontSize = 11.sp) }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(filteredAdminItems, key = { it.itemId }) { item ->
                        AdminItemRow(
                            item = item,
                            onStatusChange = { newStatus -> viewModel.updateItemStatus(item.itemId, newStatus) },
                            onToggleApproval = { viewModel.setItemApproval(item.itemId, !item.isApproved) },
                            onDelete = { viewModel.deleteItem(item) },
                            onClick = { onItemClick(item.itemId) }
                        )
                    }
                }

                1 -> {
                    // USER DIRECTORY TAB
                    items(allUsers, key = { it.userId }) { user ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (user.role == "admin") CampusAmber else CampusBluePrimary
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (user.role == "admin") Icons.Default.Security else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (user.role == "admin") Color(0xFFFEF3C7) else Color(0xFFEFF6FF)
                                        ) {
                                            Text(
                                                text = user.role.uppercase(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (user.role == "admin") CampusAmber else CampusBluePrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "ID: ${user.collegeId} • ${user.email}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Phone: ${user.phone}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // MATCH SUPERVISOR TAB
                    if (allMatches.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Text("No matches detected by the engine yet.")
                            }
                        }
                    } else {
                        items(allMatches, key = { it.matchId }) { match ->
                            val lostItem = allItems.firstOrNull { it.itemId == match.lostItemId }
                            val foundItem = allItems.firstOrNull { it.itemId == match.foundItemId }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MatchAmberBg)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MatchAmber)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Match #${match.matchId} (${match.matchScore}% Score)",
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF78350F)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MatchAmber
                                        ) {
                                            Text(
                                                text = match.status,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = match.matchReasons,
                                        fontSize = 12.sp,
                                        color = Color(0xFF92400E)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (lostItem != null) {
                                            OutlinedButton(
                                                onClick = { onItemClick(lostItem.itemId) },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Lost: ${lostItem.itemName}", fontSize = 11.sp, maxLines = 1)
                                            }
                                        }
                                        if (foundItem != null) {
                                            OutlinedButton(
                                                onClick = { onItemClick(foundItem.itemId) },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Found: ${foundItem.itemName}", fontSize = 11.sp, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminItemRow(
    item: ItemEntity,
    onStatusChange: (String) -> Unit,
    onToggleApproval: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var statusMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ItemTypeBadge(type = item.itemType)
                    Spacer(modifier = Modifier.width(6.dp))
                    if (!item.isApproved) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFEE2E2)
                        ) {
                            Text(
                                text = "HIDDEN",
                                color = LostRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                ItemStatusBadge(status = item.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${item.itemName} (${item.category})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "Location: ${item.location} • Date: ${item.date} • Reporter: ${item.reporterName}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick status buttons and moderation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    OutlinedButton(onClick = { statusMenuExpanded = true }) {
                        Text("Status: ${item.status}", fontSize = 11.sp)
                    }
                    androidx.compose.material3.DropdownMenu(
                        expanded = statusMenuExpanded,
                        onDismissRequest = { statusMenuExpanded = false }
                    ) {
                        listOf("Open", "Possible Match", "Claimed", "Returned", "Closed").forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st) },
                                onClick = {
                                    onStatusChange(st)
                                    statusMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onToggleApproval) {
                        Icon(
                            imageVector = if (item.isApproved) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Visibility",
                            tint = if (item.isApproved) FoundGreen else LostRed
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LostRed)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = color)
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
