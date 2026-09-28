package com.example.kuikly.navigation

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit coverage for the Me login redirect seam (ticket 03):
 * Mine guest CTA sets a Main/Me resume token via [LoginRedirect] + [MainTabLaunch];
 * LoginPage consumes the route, and the resumed MainPage consumes the Me seed once.
 */
class MainTabLaunchTest {

    @BeforeTest
    fun setUp() {
        LoginRedirect.clear()
        MainTabLaunch.clear()
    }

    @AfterTest
    fun tearDown() {
        LoginRedirect.clear()
        MainTabLaunch.clear()
    }

    @Test
    fun default_launch_is_not_me() {
        assertFalse(MainTabLaunch.consumeMe())
    }

    @Test
    fun request_me_is_consumed_once() {
        MainTabLaunch.requestMe()
        assertTrue(MainTabLaunch.selectMe)
        assertTrue(MainTabLaunch.consumeMe())
        // Second read (e.g. next MainPage instance) must not stay on Me.
        assertFalse(MainTabLaunch.consumeMe())
        assertFalse(MainTabLaunch.selectMe)
    }

    @Test
    fun clear_resets_pending_me_selection() {
        MainTabLaunch.requestMe()
        MainTabLaunch.clear()
        assertFalse(MainTabLaunch.consumeMe())
    }

    @Test
    fun me_login_redirect_flow_sets_main_route_and_me_seed() {
        // MineTab.openLogin()
        LoginRedirect.setPending(PageNames.Main)
        MainTabLaunch.requestMe()

        // LoginPage.finishAfterLogin()
        assertEquals(PageNames.Main, LoginRedirect.consume())

        // Resumed MainPage init
        assertTrue(MainTabLaunch.consumeMe())
    }

    @Test
    fun used_car_gate_does_not_seed_me_tab() {
        // HomeShellTab 二手车 gate only sets a route, never Me selection.
        LoginRedirect.setPending(PageNames.UsedCarList)

        assertEquals(PageNames.UsedCarList, LoginRedirect.consume())
        assertFalse(MainTabLaunch.consumeMe())
    }

    @Test
    fun login_redirect_consume_is_one_shot() {
        LoginRedirect.setPending(PageNames.Main)
        assertEquals(PageNames.Main, LoginRedirect.consume())
        assertEquals(null, LoginRedirect.consume())
    }
}
