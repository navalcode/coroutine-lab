package com.example.coroutinelab.ui

import android.app.LocaleManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.LocaleList
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.example.coroutinelab.CoroutineLabViewModel
import com.example.coroutinelab.MainActivity
import com.example.coroutinelab.R
import com.example.coroutinelab.model.EventKind
import com.example.coroutinelab.model.FlowStrategy
import com.example.coroutinelab.model.ErrorStrategy
import java.io.File
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 33)
class ScenarioJourneyTest {
    private val compose = createAndroidComposeRule<MainActivity>()
    private lateinit var model: CoroutineLabViewModel
    private var previousLocales: LocaleList? = null
    private val localeRule = object : ExternalResource() {
        override fun before() {
            val language = InstrumentationRegistry.getArguments().getString("qaLanguage") ?: return
            require(language in setOf("en", "es"))
            val manager = InstrumentationRegistry.getInstrumentation().targetContext.getSystemService(LocaleManager::class.java)
            previousLocales = manager.applicationLocales
            manager.applicationLocales = LocaleList.forLanguageTags(language)
        }
        override fun after() {
            previousLocales?.let {
                InstrumentationRegistry.getInstrumentation().targetContext.getSystemService(LocaleManager::class.java).applicationLocales = it
            }
        }
    }
    @get:Rule val rules: RuleChain = RuleChain.outerRule(localeRule).around(compose)
    @Before fun bindOwner() {
        compose.runOnIdle { model = ViewModelProvider(compose.activity)[CoroutineLabViewModel::class.java] }
    }
    private fun text(id: Int) = compose.activity.getString(id)
    private fun list() = compose.onNode(hasScrollAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange))
    private fun tab(id: Int) = compose.onNode(hasText(text(id)) and hasClickAction()).performScrollTo().performClick()
    private fun click(id: Int) {
        list().performScrollToNode(hasText(text(id)))
        compose.onNodeWithText(text(id)).performClick()
    }
    private fun waitForStopped() { compose.waitUntil(10_000) { !model.uiState.value.isRunning } }
    private fun capture(name: String, heading: Int) {
        if (InstrumentationRegistry.getArguments().getString("captureScreenshots") != "true") return
        list().performScrollToIndex(0)
        compose.mainClock.advanceTimeByFrame(); compose.waitForIdle()
        compose.onNodeWithText(text(heading)).assertIsDisplayed()
        val language = compose.activity.resources.configuration.locales[0].language
        val mode = if (compose.activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) "dark" else "light"
        val image = compose.onRoot().captureToImage().asAndroidBitmap()
        File(compose.activity.filesDir, "$name-$language-$mode.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
        image.recycle()
    }
    @Test fun latestFlowProducesAnOrderedTraceAndCanBeCleared() {
        tab(R.string.tab_flow)
        compose.onNodeWithText("latest").performClick()
        val label = compose.activity.getString(R.string.run_operator, FlowStrategy.LATEST.label)
        list().performScrollToNode(hasText(label))
        val action = compose.onNodeWithText(label)
        assertTrue(action.fetchSemanticsNode().boundsInRoot.height / compose.activity.resources.displayMetrics.density >= 48f)
        action.performClick(); waitForStopped()
        val events = model.uiState.value.events
        assertTrue(events.isNotEmpty())
        assertEquals(EventKind.SUCCESS, events.last().kind)
        assertEquals("Escenario terminado", events.last().message)
        assertEquals(events.map { it.id }.distinct().sorted(), events.map { it.id })
        list().performScrollToNode(hasText("Escenario terminado"))
        compose.onNodeWithText("Escenario terminado").assertIsDisplayed()
        capture("coroutine-flow", R.string.flow_title)
        click(R.string.clear)
        compose.runOnIdle { assertTrue(model.uiState.value.events.isEmpty()) }
    }
    @Test fun cancellationRemainsReachableFromTheGuide() {
        tab(R.string.tab_structured); click(R.string.run_cancellable)
        assertTrue(model.uiState.value.isRunning)
        tab(R.string.tab_guide)
        compose.onNodeWithText(text(R.string.cancel)).performClick()
        waitForStopped()
        assertEquals("Escenario cancelado", model.uiState.value.events.last().message)
        assertEquals(EventKind.WARNING, model.uiState.value.events.last().kind)
    }
    @Test fun recreationPreservesTheScenarioOwnerAndItsControls() {
        tab(R.string.tab_structured); click(R.string.run_cancellable)
        val owner = model
        compose.activityRule.scenario.recreate()
        compose.runOnIdle { model = ViewModelProvider(compose.activity)[CoroutineLabViewModel::class.java] }
        assertSame(owner, model)
        compose.onNodeWithText(text(R.string.cancel)).performClick()
        waitForStopped()
        assertEquals("Escenario cancelado", model.uiState.value.events.last().message)
    }

    @Test fun rootHandlerReportsTheFailureWithoutClosingTheScreen() {
        tab(R.string.tab_errors)
        list().performScrollToNode(hasText("CoroutineExceptionHandler"))
        compose.onNodeWithText("CoroutineExceptionHandler").performClick()
        val label = compose.activity.getString(R.string.run_operator, ErrorStrategy.EXCEPTION_HANDLER.label)
        list().performScrollToNode(hasText(label))
        compose.onNodeWithText(label).performClick()
        compose.waitUntil(10_000) {
            !model.uiState.value.isRunning && model.uiState.value.events.any { it.source == "handler" }
        }
        assertEquals(EventKind.ERROR, model.uiState.value.events.last().kind)
        tab(R.string.tab_guide)
        compose.onNodeWithText(text(R.string.guide_title)).assertIsDisplayed()
    }
}
