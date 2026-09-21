package com.example

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.data.model.ItemEntity
import com.example.ui.components.ItemCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun item_card_screenshot() {
    val sampleItem = ItemEntity(
      itemId = 1,
      userId = "user_alex",
      itemType = "LOST",
      itemName = "Black Leather Wallet",
      category = "Wallets & Purses",
      description = "Bi-fold wallet with student ID and transit card",
      color = "Black",
      brand = "Wildhorn",
      location = "Central Library",
      date = "2026-09-20",
      time = "10:30 AM",
      imageName = "wallets",
      reporterName = "Alex Johnson",
      contactPhone = "+1 555-0101",
      contactEmail = "alex@college.edu",
      status = "Possible Match"
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        ItemCard(
          item = sampleItem,
          hasPossibleMatch = true,
          onClick = {},
          modifier = Modifier.padding(16.dp)
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/item_card.png")
  }
}
