package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val collegeId: String, // USN or College ID
    val email: String,
    val phone: String,
    val password: String,
    val role: String = "student", // "student", "staff", "admin"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val itemId: Long = 0,
    val userId: String,
    val itemType: String, // "LOST" or "FOUND"
    val itemName: String,
    val category: String,
    val description: String,
    val color: String,
    val brand: String = "",
    val location: String,
    val date: String,
    val time: String,
    val imageName: String = "default",
    val reporterName: String,
    val contactPhone: String,
    val contactEmail: String,
    val status: String = "Open", // "Open", "Possible Match", "Claimed", "Returned", "Closed"
    val isApproved: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val matchId: Long = 0,
    val lostItemId: Long,
    val foundItemId: Long,
    val matchScore: Int, // 0 - 100
    val matchReasons: String,
    val status: String = "Suggested", // "Suggested", "Verified", "Dismissed"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "claims")
data class ClaimEntity(
    @PrimaryKey(autoGenerate = true) val claimId: Long = 0,
    val itemId: Long,
    val claimantUserId: String,
    val claimantName: String,
    val claimantCollegeId: String,
    val claimantPhone: String,
    val verificationDetails: String,
    val status: String = "Pending", // "Pending", "Approved", "Rejected", "Resolved"
    val createdAt: Long = System.currentTimeMillis()
)

object CampusCategories {
    val list = listOf(
        "Wallet & Purse",
        "Electronics & Gadgets",
        "Keys & Keychains",
        "College ID & Cards",
        "Books & Study Notes",
        "Bags & Backpacks",
        "Watches & Wearables",
        "Clothing & Hoodies",
        "Water Bottles",
        "Calculators & Equipment",
        "Eyeglasses",
        "Other"
    )
}

object CampusLocations {
    val list = listOf(
        "CSE Block",
        "ECE Block",
        "Mechanical Block",
        "Civil Block",
        "Central Library",
        "Main Cafeteria / Canteen",
        "College Auditorium",
        "Sports Complex / Ground",
        "Admin Block",
        "Student Activity Center",
        "Hostel Block A (Boys)",
        "Hostel Block B (Girls)",
        "Computer Center / Lab 3",
        "Chemistry / Physics Lab",
        "Campus Parking Lot"
    )
}

object CommonColors {
    val list = listOf(
        "Black",
        "Blue",
        "Silver/Grey",
        "Red",
        "White",
        "Brown",
        "Green",
        "Gold/Yellow",
        "Purple",
        "Other"
    )
}
