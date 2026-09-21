package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CampusCategories
import com.example.data.model.CampusLocations
import com.example.data.model.CommonColors
import com.example.ui.LostAndFoundViewModel
import com.example.ui.components.ItemCard
import com.example.ui.theme.CampusBluePrimary
import com.example.ui.theme.FoundGreen
import com.example.ui.theme.LostRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: LostAndFoundViewModel,
    onNavigateBack: () -> Unit,
    onItemClick: (Long) -> Unit
) {
    val filterState by viewModel.filterState.collectAsState()
    val filteredItems by viewModel.filteredItems.collectAsState()
    val allMatches by viewModel.allMatches.collectAsState()

    var showAdvancedFilters by remember { mutableStateOf(false) }

    var catExpanded by remember { mutableStateOf(false) }
    var locExpanded by remember { mutableStateOf(false) }
    var colorExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Campus Items", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAdvancedFilters = !showAdvancedFilters }) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Filters",
                            tint = if (showAdvancedFilters) CampusBluePrimary else MaterialTheme.colorScheme.onSurface
                        )
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            // SEARCH TEXT FIELD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = filterState.query,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search by name, brand, location, color...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CampusBluePrimary) },
                    trailingIcon = {
                        if (filterState.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_query_input")
                )
            }

            // PRIMARY FILTER CHIPS: TYPE & STATUS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterState.type == "ALL",
                    onClick = { viewModel.setFilterType("ALL") },
                    label = { Text("All Types") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CampusBluePrimary, selectedLabelColor = Color.White)
                )
                FilterChip(
                    selected = filterState.type == "LOST",
                    onClick = { viewModel.setFilterType("LOST") },
                    label = { Text("Lost Only") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = LostRed, selectedLabelColor = Color.White)
                )
                FilterChip(
                    selected = filterState.type == "FOUND",
                    onClick = { viewModel.setFilterType("FOUND") },
                    label = { Text("Found Only") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = FoundGreen, selectedLabelColor = Color.White)
                )

                // Quick status filter chips
                FilterChip(
                    selected = filterState.status == "Open",
                    onClick = {
                        viewModel.setFilterStatus(if (filterState.status == "Open") "ALL" else "Open")
                    },
                    label = { Text("Open") }
                )
                FilterChip(
                    selected = filterState.status == "Possible Match",
                    onClick = {
                        viewModel.setFilterStatus(if (filterState.status == "Possible Match") "ALL" else "Possible Match")
                    },
                    label = { Text("Possible Match") }
                )
                FilterChip(
                    selected = filterState.status == "Returned",
                    onClick = {
                        viewModel.setFilterStatus(if (filterState.status == "Returned") "ALL" else "Returned")
                    },
                    label = { Text("Returned") }
                )
            }

            // ADVANCED FILTERS EXPANDABLE PANEL
            if (showAdvancedFilters) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Filter Options",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = CampusBluePrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Category Dropdown
                        ExposedDropdownMenuBox(
                            expanded = catExpanded,
                            onExpandedChange = { catExpanded = !catExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = filterState.category ?: "All Categories",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = catExpanded,
                                onDismissRequest = { catExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Categories") },
                                    onClick = {
                                        viewModel.setFilterCategory(null)
                                        catExpanded = false
                                    }
                                )
                                CampusCategories.list.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            viewModel.setFilterCategory(cat)
                                            catExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Location Dropdown
                        ExposedDropdownMenuBox(
                            expanded = locExpanded,
                            onExpandedChange = { locExpanded = !locExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = filterState.location ?: "All Locations",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Campus Location") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = locExpanded,
                                onDismissRequest = { locExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Locations") },
                                    onClick = {
                                        viewModel.setFilterLocation(null)
                                        locExpanded = false
                                    }
                                )
                                CampusLocations.list.forEach { loc ->
                                    DropdownMenuItem(
                                        text = { Text(loc) },
                                        onClick = {
                                            viewModel.setFilterLocation(loc)
                                            locExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Color Dropdown
                        ExposedDropdownMenuBox(
                            expanded = colorExpanded,
                            onExpandedChange = { colorExpanded = !colorExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = filterState.color ?: "All Colors",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Item Color") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = colorExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = colorExpanded,
                                onDismissRequest = { colorExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Colors") },
                                    onClick = {
                                        viewModel.setFilterColor(null)
                                        colorExpanded = false
                                    }
                                )
                                CommonColors.list.forEach { col ->
                                    DropdownMenuItem(
                                        text = { Text(col) },
                                        onClick = {
                                            viewModel.setFilterColor(col)
                                            colorExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { viewModel.resetFilters() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset All Filters")
                        }
                    }
                }
            }

            // RESULTS HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredItems.size} items found",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // RESULTS LIST
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No reports match your search criteria.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { viewModel.resetFilters() }) {
                            Text("Clear Filters")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredItems, key = { it.itemId }) { item ->
                        val hasMatch = allMatches.any { it.lostItemId == item.itemId || it.foundItemId == item.itemId }
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
}
