package com.dev.habitwallpaper

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dev.habitwallpaper.data.local.HabitDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        HabitDatabase::class.java,
        listOf(), // No migrations to provide yet
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate10To11() {
        // 1. Create the database with version 10
        val db = helper.createDatabase(TEST_DB, 10)

        // 2. Insert some test data using SQL (Room cannot be used here as it uses current entities)
        db.execSQL("INSERT INTO habits (name, description, durationDays, startDate, createdAt, goalValue) VALUES ('Test Habit', 'Desc', 30, 1625097600000, 1625097600000, 1.0)")

        // Close the database
        db.close()

        // 3. Re-open and migrate to version 11
        // helper.runMigrationsAndValidate(TEST_DB, 11, true, HabitDatabase.MIGRATION_10_11)
        
        // Note: Uncomment the line above once you have actually created MIGRATION_10_11 in your code
    }
}
