package com.psl.pasalhub.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = "com.psl.pasalhub",
        includeInStartupProfile = true
    ) {
        pressHome()
        startActivityAndWait()

        // Wait for Splash to finish and Home to load
        // We'll wait for a known tag from Dashboard or Home
        device.wait(Until.hasObject(By.res("dashboard_scaffold")), 10_000)

        // Scroll the product grid to capture scrolling profiles
        val productGrid = device.findObject(By.res("home_product_grid"))
        if (productGrid != null) {
            productGrid.fling(androidx.test.uiautomator.Direction.DOWN)
            device.waitForIdle()
        }

        // Navigate to Cart
        val cartTab = device.findObject(By.res("nav_cart_tab"))
        cartTab?.click()
        device.waitForIdle()

        // Navigate back to Home
        val homeTab = device.findObject(By.res("nav_home_tab"))
        homeTab?.click()
        device.waitForIdle()
    }
}
