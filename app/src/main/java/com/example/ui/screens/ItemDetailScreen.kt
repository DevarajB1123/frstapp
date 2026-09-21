package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClaimEntity
import com.example.data.model.ItemEntity
import com.example.data.model.MatchEntity
import com.example.ui.LostAndFoundViewModel
import com.example.ui.components.ItemStatusBadge
import com.example.ui.components.ItemTypeBadge
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.CampusAmber
import com.example.ui.theme.CampusBluePrimary
import com.example.ui.theme.CampusBlueSecondary
import com.example.ui.theme.FoundGreen
import com.example.ui.theme.LostRed
import com.example.ui.theme.MatchAmber
import com.example.ui.theme.MatchAmberBg
import com.example.ui.theme.ReturnedPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: Long,
    viewModel: LostAndFoundViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToItem: (Long) -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allApprovedItems by viewModel.allApprovedItems.collectAsState()
    val allAdminItems by viewModel.allItemsAdmin.collectAsState()
    val allMatches by viewModel.allMatches.collectAsState()
    val allClaims by viewModel.allClaims.collectAsState()

    val item = (allAdminItems.firstOrNull { it.itemId == itemId }
        ?: allApprovedItems.firstOrNull { it.itemId == itemId })

    var showClaimDialog by remember { mutableStateOf(false) }
    var claimVerificationDetails by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    var adminStatusExpanded by remember { mutableStateOf(false) }

    if (item == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Item report not found.")
        }
        return
    }

    val isLost = item.itemType.equals("LOST", ignoreCase = true)
    val isOwner = currentUser?.userId == item.userId
    val isAdmin = currentUser?.role == "admin"

    // Related matches for this item
    val itemMatches = allMatches.filter { it.lostItemId == item.itemId || it.foundItemId == item.itemId }
    // Claims on this item
    val itemClaims = allClaims.filter { it.itemId == item.itemId }

    // Claim Dialog
    if (showClaimDialog) {
        AlertDialog(
            onDismissRequest = { showClaimDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = CampusBluePrimary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Submit Ownership Claim",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "To verify ownership of \"${item.itemName}\", please provide identifying features only the real owner would know (e.g., lock code, internal serial number, exact ID card name, contents, distinctive stickers):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = claimVerificationDetails,
                        onValueChange = { claimVerificationDetails = it },
                        label = { Text("Verification Details *") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_claim_details")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (claimVerificationDetails.isNotBlank()) {
                            viewModel.submitClaim(item.itemId, claimVerificationDetails) {
                                showClaimDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CampusBluePrimary)
                ) {
                    Text("Submit Claim")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClaimDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Report?") },
            text = { Text("Are you sure you want to permanently delete this lost & found report?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteItem(item)
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LostRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report #${item.itemId}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isOwner || isAdmin) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Report", tint = LostRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // MAIN SUMMARY CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ItemTypeBadge(type = item.itemType)
                        ItemStatusBadge(status = item.status)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isLost) Color(0xFFFEE2E2) else Color(0xFFD1FAE5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(item.category),
                                contentDescription = null,
                                tint = if (isLost) LostRed else FoundGreen,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = item.itemName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.category,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Description",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = CampusBluePrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(14.dp))

                    // SPECS GRID
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            SpecRow(label = "Location", value = item.location, icon = Icons.Default.Place)
                            Spacer(modifier = Modifier.height(8.dp))
                            SpecRow(label = "Date", value = item.date, icon = Icons.Default.CalendarToday)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            SpecRow(label = "Color", value = item.color, icon = Icons.Default.Info)
                            Spacer(modifier = Modifier.height(8.dp))
                            SpecRow(label = "Approx. Time", value = item.time, icon = Icons.Default.Schedule)
                        }
                    }

                    if (item.brand.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SpecRow(label = "Brand", value = item.brand, icon = Icons.Default.Info)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // POSSIBLE MATCH CARD (Crucial requirement from prompt!)
            if (itemMatches.isNotEmpty()) {
                val match = itemMatches.first()
                val counterpartId = if (match.lostItemId == item.itemId) match.foundItemId else match.lostItemId
                val counterpartItem = allAdminItems.firstOrNull { it.itemId == counterpartId }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MatchAmberBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MatchAmber,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Possible Match Found",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF78350F)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MatchAmber
                            ) {
                                Text(
                                    text = "${match.matchScore}% Similarity",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Matching Factors:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = match.matchReasons,
                            fontSize = 13.sp,
                            color = Color(0xFF78350F)
                        )

                        if (counterpartItem != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToItem(counterpartItem.itemId) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (counterpartItem.itemType == "LOST") Color(0xFFFEE2E2) else Color(0xFFD1FAE5)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getCategoryIcon(counterpartItem.category),
                                            contentDescription = null,
                                            tint = if (counterpartItem.itemType == "LOST") LostRed else FoundGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Matched ${counterpartItem.itemType}: ${counterpartItem.itemName}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Reported at ${counterpartItem.location} (${counterpartItem.date})",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "View →",
                                        fontWeight = FontWeight.Bold,
                                        color = CampusBluePrimary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // CONTACT / REPORTER INFORMATION CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isLost) "Owner Contact Information" else "Finder Contact Information",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CampusBluePrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = CampusBluePrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = item.reporterName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = item.contactPhone, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.contactPhone}"))
                                context.startActivity(intent)
                            }
                        ) {
                            Text("Call", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = item.contactEmail, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${item.contactEmail}"))
                                context.startActivity(intent)
                            }
                        ) {
                            Text("Email", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // CLAIMS LIST SECTION (IF ANY)
            if (itemClaims.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Claims & Verification Submissions (${itemClaims.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CampusBluePrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        itemClaims.forEach { claim ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${claim.claimantName} (${claim.claimantCollegeId})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
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
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (claim.status) {
                                                    "Approved" -> Color(0xFF059669)
                                                    "Rejected" -> Color(0xFFDC2626)
                                                    else -> Color(0xFFD97706)
                                                },
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Verification: \"${claim.verificationDetails}\"",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (isAdmin || isOwner) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (claim.status != "Approved") {
                                                Button(
                                                    onClick = {
                                                        viewModel.updateClaimStatus(claim, "Approved", markReturned = true)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = FoundGreen),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Approve & Mark Returned", fontSize = 11.sp)
                                                }
                                            }
                                            if (claim.status != "Rejected") {
                                                OutlinedButton(
                                                    onClick = {
                                                        viewModel.updateClaimStatus(claim, "Rejected")
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Reject Claim", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // PRIMARY ACTIONS (CLAIM, MARK RETURNED, ADMIN STATUS)
            if (item.status != "Returned") {
                // Submit claim button (available if not reporter)
                if (!isOwner) {
                    Button(
                        onClick = { showClaimDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CampusBluePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_claim_item")
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Claim This Item / Verify Ownership", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Mark as Returned / Resolved Button
                Button(
                    onClick = {
                        viewModel.updateItemStatus(item.itemId, "Returned")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ReturnedPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_mark_returned")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mark as Returned / Resolved", fontWeight = FontWeight.Bold)
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEDE9FE),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ReturnedPurple)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "This item has been safely returned to its owner.",
                            fontWeight = FontWeight.Bold,
                            color = ReturnedPurple
                        )
                    }
                }
            }

            // ADMIN CONTROLS SECTION
            if (isAdmin) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = CampusAmber)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Admin Authority Controls",
                                fontWeight = FontWeight.Bold,
                                color = CampusBluePrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        ExposedDropdownMenuBox(
                            expanded = adminStatusExpanded,
                            onExpandedChange = { adminStatusExpanded = !adminStatusExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = "Status: ${item.status}",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Update Item Status") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = adminStatusExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = adminStatusExpanded,
                                onDismissRequest = { adminStatusExpanded = false }
                            ) {
                                listOf("Open", "Possible Match", "Claimed", "Returned", "Closed").forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text(status) },
                                        onClick = {
                                            viewModel.updateItemStatus(item.itemId, status)
                                            adminStatusExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.setItemApproval(item.itemId, !item.isApproved)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (item.isApproved) "Reject / Hide" else "Approve Report")
                            }

                            Button(
                                onClick = { showDeleteConfirm = true },
                                colors = ButtonDefaults.buttonColors(containerColor = LostRed),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Remove Report")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SpecRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
