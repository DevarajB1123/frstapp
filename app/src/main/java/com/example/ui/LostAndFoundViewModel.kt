package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.ClaimEntity
import com.example.data.model.ItemEntity
import com.example.data.model.MatchEntity
import com.example.data.model.UserEntity
import com.example.data.repository.LostAndFoundRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FilterState(
    val query: String = "",
    val type: String = "ALL", // "ALL", "LOST", "FOUND"
    val category: String? = null,
    val location: String? = null,
    val status: String = "ALL", // "ALL", "Open", "Possible Match", "Claimed", "Returned"
    val color: String? = null
)

class LostAndFoundViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LostAndFoundRepository

    val currentUser = MutableStateFlow<UserEntity?>(null)
    val authError = MutableStateFlow<String?>(null)
    val authSuccessMessage = MutableStateFlow<String?>(null)

    val filterState = MutableStateFlow(FilterState())

    val allApprovedItems: StateFlow<List<ItemEntity>>
    val allItemsAdmin: StateFlow<List<ItemEntity>>
    val allMatches: StateFlow<List<MatchEntity>>
    val allClaims: StateFlow<List<ClaimEntity>>
    val allUsers: StateFlow<List<UserEntity>>

    init {
        val db = AppDatabase.getInstance(application)
        repository = LostAndFoundRepository(db)

        allApprovedItems = repository.allApprovedItems.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allItemsAdmin = repository.allItemsAdmin.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allMatches = repository.allMatches.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allClaims = repository.allClaims.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allUsers = repository.allUsers.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
            // Auto-login student Alex on initial launch for smooth demonstration
            val defaultStudent = repository.login("student@college.edu", "student123")
            currentUser.value = defaultStudent
        }
    }

    val filteredItems: StateFlow<List<ItemEntity>> = combine(
        allApprovedItems,
        filterState
    ) { items: List<ItemEntity>, filter: FilterState ->
        items.filter { item ->
            val matchQuery = if (filter.query.isBlank()) true else {
                item.itemName.contains(filter.query, ignoreCase = true) ||
                        item.description.contains(filter.query, ignoreCase = true) ||
                        item.brand.contains(filter.query, ignoreCase = true) ||
                        item.location.contains(filter.query, ignoreCase = true) ||
                        item.color.contains(filter.query, ignoreCase = true) ||
                        item.category.contains(filter.query, ignoreCase = true)
            }

            val matchType = if (filter.type == "ALL") true else item.itemType.equals(filter.type, ignoreCase = true)
            val matchCategory = if (filter.category == null || filter.category == "All") true else item.category.equals(filter.category, ignoreCase = true)
            val matchLocation = if (filter.location == null || filter.location == "All") true else item.location.equals(filter.location, ignoreCase = true)
            val matchStatus = if (filter.status == "ALL") true else item.status.equals(filter.status, ignoreCase = true)
            val matchColor = if (filter.color == null || filter.color == "All") true else item.color.equals(filter.color, ignoreCase = true)

            matchQuery && matchType && matchCategory && matchLocation && matchStatus && matchColor
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(q: String) {
        filterState.value = filterState.value.copy(query = q)
    }

    fun setFilterType(t: String) {
        filterState.value = filterState.value.copy(type = t)
    }

    fun setFilterCategory(c: String?) {
        filterState.value = filterState.value.copy(category = c)
    }

    fun setFilterLocation(l: String?) {
        filterState.value = filterState.value.copy(location = l)
    }

    fun setFilterStatus(s: String) {
        filterState.value = filterState.value.copy(status = s)
    }

    fun setFilterColor(col: String?) {
        filterState.value = filterState.value.copy(color = col)
    }

    fun resetFilters() {
        filterState.value = FilterState()
    }

    fun login(email: String, pass: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            authError.value = null
            val user = repository.login(email, pass)
            if (user != null) {
                currentUser.value = user
                onComplete(true)
            } else {
                authError.value = "Invalid email or password. Please check your credentials."
                onComplete(false)
            }
        }
    }

    fun register(
        name: String,
        collegeId: String,
        email: String,
        phone: String,
        pass: String,
        role: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            authError.value = null
            val result = repository.register(name, collegeId, email, phone, pass, role)
            result.fold(
                onSuccess = { user ->
                    currentUser.value = user
                    authSuccessMessage.value = "Registration successful! Welcome to Campus Lost & Found."
                    onComplete(true)
                },
                onFailure = { error ->
                    authError.value = error.message ?: "Registration failed."
                    onComplete(false)
                }
            )
        }
    }

    fun logout() {
        currentUser.value = null
    }

    fun reportItem(
        type: String,
        name: String,
        category: String,
        description: String,
        color: String,
        brand: String,
        location: String,
        date: String,
        time: String,
        imageName: String,
        onSuccess: (Long) -> Unit
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val newItem = ItemEntity(
                userId = user.userId,
                itemType = type.uppercase(),
                itemName = name.trim(),
                category = category,
                description = description.trim(),
                color = color,
                brand = brand.trim(),
                location = location,
                date = date,
                time = time,
                imageName = imageName,
                reporterName = user.name,
                contactPhone = user.phone,
                contactEmail = user.email,
                status = "Open",
                isApproved = true
            )
            val newId = repository.reportItem(newItem)
            onSuccess(newId)
        }
    }

    fun updateItemStatus(itemId: Long, status: String) {
        viewModelScope.launch {
            repository.updateItemStatus(itemId, status)
        }
    }

    fun setItemApproval(itemId: Long, approved: Boolean) {
        viewModelScope.launch {
            repository.setItemApproval(itemId, approved)
        }
    }

    fun deleteItem(item: ItemEntity) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun submitClaim(
        itemId: Long,
        verificationDetails: String,
        onSuccess: () -> Unit
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val claim = ClaimEntity(
                itemId = itemId,
                claimantUserId = user.userId,
                claimantName = user.name,
                claimantCollegeId = user.collegeId,
                claimantPhone = user.phone,
                verificationDetails = verificationDetails.trim(),
                status = "Pending"
            )
            repository.submitClaim(claim)
            onSuccess()
        }
    }

    fun updateClaimStatus(claim: ClaimEntity, newStatus: String, markReturned: Boolean = false) {
        viewModelScope.launch {
            repository.updateClaimStatus(claim, newStatus, markReturned)
        }
    }
}
