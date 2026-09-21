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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.LostAndFoundViewModel
import com.example.ui.components.ItemCard
import com.example.ui.theme.CampusBluePrimary
import com.example.ui.theme.FoundGreen
import com.example.ui.theme.LostRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardScreen(
    viewModel: LostAndFoundViewModel,
    onNavigateBack: () -> Unit,
    onReportLost: () -> Unit,
    onReportFound: () -> Unit,
    onSearchItems: () -> Unit,
    onItemClick: (Long) -> Unit,
    onLogout: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allAdminItems by viewModel.allItemsAdmin.collectAsState()
    val allMatches by viewModel.allMatches.collectAsState()
    val allClaims by viewModel.allClaims.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Lost, 1 = Found, 2 = Returned, 3 = Claims

    val userItems = allAdminItems.filter { it.userId == currentUser?.userId }
    val userLostItems = userItems.filter { it.itemType.equals("LOST", ignoreCase = true) && it.status != "Returned" }
    val userFoundItems = userItems.filter { it.itemType.equals("FOUND", ignoreCase = true) && it.status != "Returned" }
    val userReturnedItems = userItems.filter { it.status == "Returned" }
    val userClaims = allClaims.filter { it.claimantUserId == currentUser?.userId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.logout()
                            onLogout()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = LostRed)
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
            // 1. WELCOME BANNER (Literal prompt specification)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CampusBluePrimary)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Welcome, ${currentUser?.name ?: "Student"}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "College ID: ${currentUser?.collegeId ?: "N/A"} • ${currentUser?.role?.uppercase()}",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = currentUser?.email ?: "", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = currentUser?.phone ?: "", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                        }
                    }
                }
            }

            // 2. QUICK ACTIONS ROW
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CampusBluePrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onReportLost,
                            colors = ButtonDefaults.buttonColors(containerColor = LostRed),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Report Lost", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onReportFound,
                            colors = ButtonDefaults.buttonColors(containerColor = FoundGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Report Found", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onSearchItems,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Search", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. MY REPORTS SECTION TABS
            item {
                Column(modifier = Modifier.padding(top = 20.dp)) {
                    Text(
                        text = "My Reports",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = CampusBluePrimary
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Lost (${userLostItems.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Found (${userFoundItems.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Returned (${userReturnedItems.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            text = { Text("Claims (${userClaims.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // LIST CONTENT BASED ON SELECTED TAB
            when (selectedTab) {
                0 -> {
                    // Lost Items
                    if (userLostItems.isEmpty()) {
                        item {
                            EmptyDashboardTab(message = "You have not reported any lost items.", actionText = "Report a Lost Item", onAction = onReportLost)
                        }
                    } else {
                        items(userLostItems, key = { it.itemId }) { item ->
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
                1 -> {
                    // Found Items
                    if (userFoundItems.isEmpty()) {
                        item {
                            EmptyDashboardTab(message = "You have not reported any found items.", actionText = "Report a Found Item", onAction = onReportFound)
                        }
                    } else {
                        items(userFoundItems, key = { it.itemId }) { item ->
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
                2 -> {
                    // Returned Items
                    if (userReturnedItems.isEmpty()) {
                        item {
                            EmptyDashboardTab(message = "No returned or resolved items yet.", actionText = null, onAction = {})
                        }
                    } else {
                        items(userReturnedItems, key = { it.itemId }) { item ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                ItemCard(
                                    item = item,
                                    hasPossibleMatch = false,
                                    onClick = { onItemClick(item.itemId) }
                                )
                            }
                        }
                    }
                }
                3 -> {
                    // My Claims
                    if (userClaims.isEmpty()) {
                        item {
                            EmptyDashboardTab(message = "You have not submitted any item claims.", actionText = "Search Items to Claim", onAction = onSearchItems)
                        }
                    } else {
                        items(userClaims, key = { it.claimId }) { claim ->
                            val claimedItem = allAdminItems.firstOrNull { it.itemId == claim.itemId }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .clickable { onItemClick(claim.itemId) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Claim for: ${claimedItem?.itemName ?: "Item #${claim.itemId}"}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = when (claim.status) {
                                                "Approved" -> Color(0xFFD1FAE5)
                                                "Rejected" -> Color(0xFFFEE2E2)
                                                else -> Color(0xFFFEF3C7)
                                            }
                                        ) {
                                            Text(
                                                text = claim.status,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (claim.status) {
                                                    "Approved" -> Color(0xFF059669)
                                                    "Rejected" -> Color(0xFFDC2626)
                                                    else -> Color(0xFFD97706)
                                                },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "Your verification details: \"${claim.verificationDetails}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyDashboardTab(
    message: String,
    actionText: String?,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
            if (actionText != null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(onClick = onAction) {
                    Text(actionText)
                }
            }
        }
    }
}
