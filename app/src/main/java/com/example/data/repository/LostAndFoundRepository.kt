package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.ClaimEntity
import com.example.data.model.ItemEntity
import com.example.data.model.MatchEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class LostAndFoundRepository(private val db: AppDatabase) {
    private val userDao = db.userDao()
    private val itemDao = db.itemDao()
    private val matchDao = db.matchDao()
    private val claimDao = db.claimDao()

    val allApprovedItems: Flow<List<ItemEntity>> = itemDao.getAllApprovedItems()
    val allItemsAdmin: Flow<List<ItemEntity>> = itemDao.getAllItemsAdmin()
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allMatches: Flow<List<MatchEntity>> = matchDao.getAllMatches()
    val allClaims: Flow<List<ClaimEntity>> = claimDao.getAllClaims()

    fun getItemsByUser(userId: String): Flow<List<ItemEntity>> = itemDao.getItemsByUser(userId)
    fun getItemByIdFlow(itemId: Long): Flow<ItemEntity?> = itemDao.getItemByIdFlow(itemId)
    suspend fun getItemById(itemId: Long): ItemEntity? = itemDao.getItemById(itemId)
    fun getClaimsForItem(itemId: Long): Flow<List<ClaimEntity>> = claimDao.getClaimsForItem(itemId)
    fun getClaimsByUser(userId: String): Flow<List<ClaimEntity>> = claimDao.getClaimsByUser(userId)

    suspend fun login(email: String, pass: String): UserEntity? {
        val user = userDao.getUserByEmail(email.trim().lowercase())
        return if (user != null && user.password == pass) user else null
    }

    suspend fun register(
        name: String,
        collegeId: String,
        email: String,
        phone: String,
        password: String,
        role: String
    ): Result<UserEntity> {
        val existing = userDao.getUserByEmail(email.trim().lowercase())
        if (existing != null) {
            return Result.failure(Exception("An account with this email already exists."))
        }
        val newUser = UserEntity(
            userId = UUID.randomUUID().toString(),
            name = name.trim(),
            collegeId = collegeId.trim().uppercase(),
            email = email.trim().lowercase(),
            phone = phone.trim(),
            password = password,
            role = role
        )
        userDao.insertUser(newUser)
        return Result.success(newUser)
    }

    suspend fun reportItem(item: ItemEntity): Long {
        val insertedId = itemDao.insertItem(item)
        val fullItem = item.copy(itemId = insertedId)
        runMatchingEngine(fullItem)
        return insertedId
    }

    suspend fun updateItem(item: ItemEntity) {
        itemDao.updateItem(item)
        runMatchingEngine(item)
    }

    suspend fun updateItemStatus(itemId: Long, status: String) {
        itemDao.updateItemStatus(itemId, status)
    }

    suspend fun setItemApproval(itemId: Long, approved: Boolean) {
        itemDao.setItemApproval(itemId, approved)
    }

    suspend fun deleteItem(item: ItemEntity) {
        matchDao.deleteMatchesForItem(item.itemId)
        itemDao.deleteItem(item)
    }

    suspend fun submitClaim(claim: ClaimEntity): Long {
        val id = claimDao.insertClaim(claim)
        itemDao.updateItemStatus(claim.itemId, "Claimed")
        return id
    }

    suspend fun updateClaimStatus(claim: ClaimEntity, newStatus: String, itemReturned: Boolean = false) {
        claimDao.updateClaim(claim.copy(status = newStatus))
        if (newStatus == "Approved" || itemReturned) {
            itemDao.updateItemStatus(claim.itemId, "Returned")
        }
    }

    /**
     * Matching Engine compares newly reported/updated items against opposite open items
     */
    private suspend fun runMatchingEngine(newItem: ItemEntity) {
        if (newItem.status == "Returned" || newItem.status == "Closed") return

        val oppositeType = if (newItem.itemType.equals("LOST", ignoreCase = true)) "FOUND" else "LOST"
        val candidates = itemDao.getActiveItemsByType(oppositeType)

        for (candidate in candidates) {
            if (candidate.itemId == newItem.itemId) continue

            var score = 0
            val reasons = mutableListOf<String>()

            // 1. Category match (35%)
            if (newItem.category.equals(candidate.category, ignoreCase = true)) {
                score += 35
                reasons.add("Exact Category: ${newItem.category}")
            }

            // 2. Location match (25%)
            if (newItem.location.equals(candidate.location, ignoreCase = true)) {
                score += 25
                reasons.add("Same Location: ${newItem.location}")
            } else if (newItem.location.contains(candidate.location, ignoreCase = true) ||
                candidate.location.contains(newItem.location, ignoreCase = true)
            ) {
                score += 15
                reasons.add("Nearby Location: ${candidate.location}")
            }

            // 3. Color match (15%)
            if (newItem.color.isNotBlank() && candidate.color.isNotBlank()) {
                if (newItem.color.equals(candidate.color, ignoreCase = true)) {
                    score += 15
                    reasons.add("Matching Color: ${newItem.color}")
                }
            }

            // 4. Brand match (15%)
            if (newItem.brand.isNotBlank() && candidate.brand.isNotBlank()) {
                if (newItem.brand.equals(candidate.brand, ignoreCase = true)) {
                    score += 15
                    reasons.add("Same Brand: ${newItem.brand}")
                }
            }

            // 5. Name / Description keyword similarity (up to 15%)
            val textA = "${newItem.itemName} ${newItem.description}".lowercase()
            val textB = "${candidate.itemName} ${candidate.description}".lowercase()
            val wordsA = textA.split(Regex("[^a-z0-9]+")).filter { it.length > 2 }.toSet()
            val wordsB = textB.split(Regex("[^a-z0-9]+")).filter { it.length > 2 }.toSet()
            val intersection = wordsA.intersect(wordsB)
            if (intersection.isNotEmpty()) {
                val bonus = (intersection.size * 5).coerceAtMost(15)
                score += bonus
                reasons.add("Matching terms: ${intersection.take(3).joinToString(", ")}")
            }

            if (score >= 45) {
                val lostId = if (newItem.itemType.equals("LOST", ignoreCase = true)) newItem.itemId else candidate.itemId
                val foundId = if (newItem.itemType.equals("FOUND", ignoreCase = true)) newItem.itemId else candidate.itemId

                matchDao.insertMatch(
                    MatchEntity(
                        lostItemId = lostId,
                        foundItemId = foundId,
                        matchScore = score.coerceAtMost(100),
                        matchReasons = reasons.joinToString(" • "),
                        status = "Suggested"
                    )
                )

                // Update items to "Possible Match" if they are currently "Open"
                if (newItem.status == "Open") {
                    itemDao.updateItemStatus(newItem.itemId, "Possible Match")
                }
                if (candidate.status == "Open") {
                    itemDao.updateItemStatus(candidate.itemId, "Possible Match")
                }
            }
        }
    }

    suspend fun seedSampleDataIfEmpty() {
        val existingUsers = userDao.getUserByEmail("admin@college.edu")
        if (existingUsers != null) return

        // 1. Seed Accounts
        val admin = UserEntity(
            userId = "usr_admin_001",
            name = "Campus Security Admin",
            collegeId = "ADM-SEC-01",
            email = "admin@college.edu",
            phone = "+1 (555) 019-2834",
            password = "admin123",
            role = "admin"
        )
        val studentA = UserEntity(
            userId = "usr_student_001",
            name = "Alex Johnson",
            collegeId = "USN-2024-CS045",
            email = "student@college.edu",
            phone = "+1 (555) 782-9012",
            password = "student123",
            role = "student"
        )
        val studentB = UserEntity(
            userId = "usr_student_002",
            name = "Samira Patel",
            collegeId = "USN-2024-EC088",
            email = "finder@college.edu",
            phone = "+1 (555) 349-1120",
            password = "student123",
            role = "student"
        )

        userDao.insertUser(admin)
        userDao.insertUser(studentA)
        userDao.insertUser(studentB)

        // 2. Seed realistic lost and found scenario items
        val lostWallet = ItemEntity(
            userId = studentA.userId,
            itemType = "LOST",
            itemName = "Black Leather Wallet",
            category = "Wallet & Purse",
            description = "Black leather bi-fold wallet containing student USN card (Alex Johnson), metro card, and college ID.",
            color = "Black",
            brand = "Wildhorn",
            location = "CSE Block",
            date = "2026-09-21",
            time = "09:30 AM",
            imageName = "wallet",
            reporterName = "Alex Johnson",
            contactPhone = "+1 (555) 782-9012",
            contactEmail = "student@college.edu",
            status = "Possible Match",
            isApproved = true
        )
        val lostWalletId = itemDao.insertItem(lostWallet)

        val foundWallet = ItemEntity(
            userId = studentB.userId,
            itemType = "FOUND",
            itemName = "Black Wallet",
            category = "Wallet & Purse",
            description = "Black leather wallet found on the CSE Block 2nd floor staircase near room 204.",
            color = "Black",
            brand = "Wildhorn",
            location = "CSE Block",
            date = "2026-09-21",
            time = "10:15 AM",
            imageName = "wallet",
            reporterName = "Samira Patel",
            contactPhone = "+1 (555) 349-1120",
            contactEmail = "finder@college.edu",
            status = "Possible Match",
            isApproved = true
        )
        val foundWalletId = itemDao.insertItem(foundWallet)

        // Match between the two
        matchDao.insertMatch(
            MatchEntity(
                lostItemId = lostWalletId,
                foundItemId = foundWalletId,
                matchScore = 95,
                matchReasons = "Exact Category: Wallet & Purse • Same Location: CSE Block • Matching Color: Black • Same Brand: Wildhorn • Matching terms: wallet, black, leather",
                status = "Suggested"
            )
        )

        // Other campus items
        itemDao.insertItem(
            ItemEntity(
                userId = studentB.userId,
                itemType = "FOUND",
                itemName = "Casio Scientific Calculator",
                category = "Calculators & Equipment",
                description = "Casio fx-991EX ClassWiz calculator left on table 7 in Computer Center Lab 3.",
                color = "Black",
                brand = "Casio",
                location = "Computer Center / Lab 3",
                date = "2026-09-20",
                time = "04:30 PM",
                imageName = "calculator",
                reporterName = "Samira Patel",
                contactPhone = "+1 (555) 349-1120",
                contactEmail = "finder@college.edu",
                status = "Open",
                isApproved = true
            )
        )

        itemDao.insertItem(
            ItemEntity(
                userId = studentA.userId,
                itemType = "LOST",
                itemName = "Blue Hydro Flask Bottle",
                category = "Water Bottles",
                description = "Dark blue 32oz Hydro Flask with an astronaut sticker on the side.",
                color = "Blue",
                brand = "Hydro Flask",
                location = "Central Library",
                date = "2026-09-20",
                time = "11:00 AM",
                imageName = "bottle",
                reporterName = "Alex Johnson",
                contactPhone = "+1 (555) 782-9012",
                contactEmail = "student@college.edu",
                status = "Open",
                isApproved = true
            )
        )

        itemDao.insertItem(
            ItemEntity(
                userId = studentA.userId,
                itemType = "FOUND",
                itemName = "Set of Dorm Keys",
                category = "Keys & Keychains",
                description = "3 brass keys on a red Ferrari metal keychain found near table 4.",
                color = "Red",
                brand = "Ferrari",
                location = "Main Cafeteria / Canteen",
                date = "2026-09-19",
                time = "01:15 PM",
                imageName = "keys",
                reporterName = "Alex Johnson",
                contactPhone = "+1 (555) 782-9012",
                contactEmail = "student@college.edu",
                status = "Open",
                isApproved = true
            )
        )

        itemDao.insertItem(
            ItemEntity(
                userId = studentB.userId,
                itemType = "LOST",
                itemName = "Silver Sony Headphones",
                category = "Electronics & Gadgets",
                description = "Sony WH-1000XM4 noise cancelling headphones in grey carrying case.",
                color = "Silver/Grey",
                brand = "Sony",
                location = "College Auditorium",
                date = "2026-09-18",
                time = "06:00 PM",
                imageName = "headphones",
                reporterName = "Samira Patel",
                contactPhone = "+1 (555) 349-1120",
                contactEmail = "finder@college.edu",
                status = "Returned",
                isApproved = true
            )
        )
    }
}
