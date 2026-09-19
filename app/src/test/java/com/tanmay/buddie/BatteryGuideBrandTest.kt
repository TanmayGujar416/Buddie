package com.tanmay.buddie

import com.tanmay.buddie.presentation.screens.settings.BatteryGuideBrand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class BatteryGuideBrandTest {

    @Test
    fun testDetectXiaomi() {
        assertEquals(BatteryGuideBrand.XIAOMI, BatteryGuideBrand.detect(manufacturer = "Xiaomi", brand = "xiaomi"))
        assertEquals(BatteryGuideBrand.XIAOMI, BatteryGuideBrand.detect(manufacturer = "Redmi", brand = "redmi"))
        assertEquals(BatteryGuideBrand.XIAOMI, BatteryGuideBrand.detect(manufacturer = "POCO", brand = "poco"))
    }

    @Test
    fun testDetectSamsung() {
        assertEquals(BatteryGuideBrand.SAMSUNG, BatteryGuideBrand.detect(manufacturer = "samsung", brand = "samsung"))
        assertEquals(BatteryGuideBrand.SAMSUNG, BatteryGuideBrand.detect(manufacturer = "SAMSUNG", brand = "galaxy"))
    }

    @Test
    fun testDetectOppoAndRealme() {
        assertEquals(BatteryGuideBrand.OPPO, BatteryGuideBrand.detect(manufacturer = "OPPO", brand = "oppo"))
        assertEquals(BatteryGuideBrand.OPPO, BatteryGuideBrand.detect(manufacturer = "Realme", brand = "realme"))
    }

    @Test
    fun testDetectVivoAndIqoo() {
        assertEquals(BatteryGuideBrand.VIVO, BatteryGuideBrand.detect(manufacturer = "vivo", brand = "vivo"))
        assertEquals(BatteryGuideBrand.VIVO, BatteryGuideBrand.detect(manufacturer = "iQOO", brand = "iqoo"))
    }

    @Test
    fun testDetectOnePlus() {
        assertEquals(BatteryGuideBrand.ONEPLUS, BatteryGuideBrand.detect(manufacturer = "OnePlus", brand = "oneplus"))
        assertEquals(BatteryGuideBrand.ONEPLUS, BatteryGuideBrand.detect(manufacturer = "ONEPLUS", brand = "OnePlus"))
    }

    @Test
    fun testDetectMotorola() {
        assertEquals(BatteryGuideBrand.MOTOROLA, BatteryGuideBrand.detect(manufacturer = "motorola", brand = "motorola"))
        assertEquals(BatteryGuideBrand.MOTOROLA, BatteryGuideBrand.detect(manufacturer = "Motorola", brand = "moto"))
        assertEquals(BatteryGuideBrand.MOTOROLA, BatteryGuideBrand.detect(manufacturer = "Lenovo", brand = "lenovo"))
    }

    @Test
    fun testDetectFallbackToGeneric() {
        assertEquals(BatteryGuideBrand.GENERIC, BatteryGuideBrand.detect(manufacturer = "Google", brand = "pixel"))
        assertEquals(BatteryGuideBrand.GENERIC, BatteryGuideBrand.detect(manufacturer = "Sony", brand = "xperia"))
        assertEquals(BatteryGuideBrand.GENERIC, BatteryGuideBrand.detect(manufacturer = "Asus", brand = "zenfone"))
        assertEquals(BatteryGuideBrand.GENERIC, BatteryGuideBrand.detect(manufacturer = "", brand = ""))
    }

    @Test
    fun testAllBrandsHaveActionableStepsAndSkinName() {
        for (brand in BatteryGuideBrand.values()) {
            assertFalse("Steps should not be empty for $brand", brand.steps.isEmpty())
            assertNotNull("Skin name should be defined for $brand", brand.skinName)
            assertNotNull("Brand name should be defined for $brand", brand.brandName)
            for (step in brand.steps) {
                assertFalse("Step in $brand should not be blank", step.isBlank())
            }
        }
    }
}
