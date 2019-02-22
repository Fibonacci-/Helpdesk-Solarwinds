package com.helwigdev.helpdesk.view

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.TranslateAnimation
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.helwigdev.helpdesk.BlankFragment
import com.helwigdev.helpdesk.R
import com.helwigdev.helpdesk.SettingsActivity
import kotlinx.android.synthetic.main.a_main_drawer.*


class DrawerView : AppCompatActivity() {

    private lateinit var preferences: SharedPreferences
    private var lastTranslate = 0.0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_main_drawer)

        setSupportActionBar(drawer_toolbar)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        MobileAds.initialize(applicationContext, "ca-app-pub-5637328886369714~1187638383")
        //if ads have not been removed
        if (!preferences.getBoolean(SettingsActivity.PREF_ADS_REMOVED, false)) {
            val adRequest = AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("4283C4B685567508FFF2525F7AB7B819")
                    .build()

            av_tickets_bottom.loadAd(adRequest)
        } else {
            av_tickets_bottom.visibility = View.GONE
        }

        nav_view.setNavigationItemSelectedListener { menuItem ->
            // set item as selected to persist highlight
            menuItem.isChecked = true
            // close drawer when item is tapped
            drawer_layout.closeDrawers()

            // update the UI based on the item selected
            handleNavSelection(menuItem)

            true
        }

        drawer_layout.addDrawerListener(
                object : DrawerLayout.DrawerListener {
                    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                        // Respond when the drawer's position changes
                        val moveFactor = drawer_layout.width * slideOffset / 4
                        val anim = TranslateAnimation(lastTranslate, moveFactor, 0.0f, 0.0f)
                        anim.duration = 0
                        anim.fillAfter = true
                        content_frame_parent.startAnimation(anim)

                        lastTranslate = moveFactor
                    }

                    override fun onDrawerOpened(drawerView: View) {
                        // Respond when the drawer is opened
                    }

                    override fun onDrawerClosed(drawerView: View) {
                        // Respond when the drawer is closed
                    }

                    override fun onDrawerStateChanged(newState: Int) {
                        // Respond when the drawer motion state changes
                    }
                }
        )


        nav_view.setCheckedItem(R.id.dm_my_tickets)
        handleNavSelection(nav_view.checkedItem)
    }

    private fun handleNavSelection(item: MenuItem?){
        Log.d("DrawerViewLog","Selected item " + item?.title)
        supportFragmentManager.beginTransaction()
                .replace(R.id.content_frame, BlankFragment.newInstance("one","two"))
                .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawer_layout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
