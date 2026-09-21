package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.ClaimEntity
import com.example.data.model.ItemEntity
import com.example.data.model.UserEntity
import com.example.data.repository.LostAndFoundRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: LostAndFoundRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = LostAndFoundRepository(database)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Campus Lost & Found", appName)
    }

    @Test
    fun testLostAndFoundMatchingEngine() = runBlocking {
        // 1. Insert a user
        val userId = "user_test_alex"
        val user = UserEntity(
            userId = userId,
            name = "Alex Johnson",
            collegeId = "USN-2024-CS045",
            email = "alex@college.edu",
            phone = "+1 555-0101",
            password = "password123",
            role = "student"
        )
        database.userDao().insertUser(user)

        // 2. Report a Lost Item: Black Leather Wallet at Central Library
        val lostItem = ItemEntity(
            userId = userId,
            itemType = "LOST",
            itemName = "Black Leather Wallet",
            category = "Wallets & Purses",
            description = "Black leather bi-fold wallet containing student ID and transit pass",
            color = "Black",
            brand = "Wildhorn",
            location = "Central Library",
            date = "2026-09-20",
            time = "10:30 AM",
            imageName = "wallets",
            reporterName = "Alex Johnson",
            contactPhone = "+1 555-0101",
            contactEmail = "alex@college.edu",
            status = "Open"
        )
        val lostId = repository.reportItem(lostItem)

        // 3. Report a Found Item: Leather Wallet found at Central Library
        val foundItem = ItemEntity(
            userId = userId,
            itemType = "FOUND",
            itemName = "Men's Black Leather Wallet",
            category = "Wallets & Purses",
            description = "Found black wallet near study cubicle 4B in library with ID cards",
            color = "Black",
            brand = "Wildhorn",
            location = "Central Library",
            date = "2026-09-20",
            time = "11:15 AM",
            imageName = "wallets",
            reporterName = "Campus Security",
            contactPhone = "+1 555-0100",
            contactEmail = "security@college.edu",
            status = "Open"
        )
        val foundId = repository.reportItem(foundItem)

        // 4. Verify Matching Engine generated a match record
        val matches = database.matchDao().getAllMatches().first()
        assertTrue("Matching engine should identify a match between the two items", matches.isNotEmpty())

        val match = matches.first()
        assertEquals(lostId, match.lostItemId)
        assertEquals(foundId, match.foundItemId)
        assertTrue("Match score should be high due to matching category, location, color, brand", match.matchScore >= 70)

        // 5. Test Ownership Claim
        val claim = ClaimEntity(
            itemId = foundId,
            claimantUserId = userId,
            claimantName = "Alex Johnson",
            claimantCollegeId = "USN-2024-CS045",
            claimantPhone = "+1 555-0101",
            verificationDetails = "Has my college ID USN-2024-CS045 and $20 bill inside the coin pouch"
        )
        val claimId = repository.submitClaim(claim)
        assertTrue(claimId > 0)

        // 6. Test Mark Item as Returned
        repository.updateItemStatus(lostId, "Returned")
        repository.updateItemStatus(foundId, "Returned")

        val updatedLost = database.itemDao().getItemById(lostId)
        val updatedFound = database.itemDao().getItemById(foundId)
        assertNotNull(updatedLost)
        assertNotNull(updatedFound)
        assertEquals("Returned", updatedLost?.status)
        assertEquals("Returned", updatedFound?.status)
    }
}
