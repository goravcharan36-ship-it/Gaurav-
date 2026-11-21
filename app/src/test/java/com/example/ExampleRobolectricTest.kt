package com.example

import android.content.Context
import android.content.Intent
import androidx.core.content.res.ResourcesCompat
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("QuickDrop", appName)
  }

  @Test
  fun `launcher activity and custom resources resolve cleanly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
    assertNotNull(launchIntent)
    assertEquals(MainActivity::class.java.name, launchIntent?.component?.className)

    val brandLogo = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_quickdrop_bag, null)
    assertNotNull(brandLogo)

    val font = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans)
    assertNotNull(font)
  }
}
