package com.helwigdev.helpdesk.view

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.helwigdev.helpdesk.R
import com.helwigdev.helpdesk.SettingsActivity
import com.helwigdev.helpdesk.model.AuthModel
import com.helwigdev.helpdesk.model.Ticket
import kotlinx.android.synthetic.main.a_main_drawer.*
import kotlinx.android.synthetic.main.drawer_header.*
import org.jetbrains.anko.doAsync
import org.json.JSONException
import org.json.JSONObject


class DrawerView : AppCompatActivity(), TicketFragment.OnListFragmentInteractionListener {

    private var menuRefresh: MenuItem? = null

    override fun onListFragmentInteraction(ticket: Ticket?) {
        val i = Intent(this, TicketView::class.java)
        i.putExtra(TicketView.ARG_TICKET_ID, ticket?.id)
        startActivity(i)
    }

    override fun setProgressVisibility(visible: Boolean) {
        menuRefresh?.isVisible = !visible
        if(visible){
            pb_main_drawer?.visibility = View.VISIBLE
        } else {
            pb_main_drawer?.visibility = View.GONE
        }
    }

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


        nav_view.setNavigationItemSelectedListener { menuItem ->
            // set item as selected to persist highlight
            menuItem.isChecked = true
            // close drawer when item is tapped
            drawer_layout.closeDrawers()

            // update the UI based on the item selected
            handleNavSelection(menuItem)
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

        updateTechInfo()
        nav_view.setCheckedItem(R.id.dm_my_tickets)
        handleNavSelection(nav_view.checkedItem)
    }

    override fun onResume() {
        super.onResume()
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
        updateTechInfo()
    }


    private fun handleNavSelection(item: MenuItem?): Boolean{
        Log.d("DrawerViewLog","Selected item " + item?.title)

        return when(item?.itemId){
            R.id.dm_my_tickets -> {
                drawer_toolbar.title = getString(R.string.my_tickets)
                supportFragmentManager.beginTransaction()
                        .replace(R.id.content_frame, TicketFragment.newInstance(TicketFragment.TYPE_MY))
                        .commit()
                true
            }
            R.id.dm_group_tickets -> {
                drawer_toolbar.title = getString(R.string.group_tickets)
                supportFragmentManager.beginTransaction()
                        .replace(R.id.content_frame, TicketFragment.newInstance(TicketFragment.TYPE_GROUP))
                        .commit()
                true
            }
            R.id.dm_flagged_tickets -> {
                drawer_toolbar.title = getString(R.string.flagged_tickets)
                supportFragmentManager.beginTransaction()
                        .replace(R.id.content_frame, TicketFragment.newInstance(TicketFragment.TYPE_FLAGGED))
                        .commit()
                true
            }
            R.id.dm_recent_tickets -> {
                drawer_toolbar.title = getString(R.string.recent_tickets)
                supportFragmentManager.beginTransaction()
                        .replace(R.id.content_frame, TicketFragment.newInstance(TicketFragment.TYPE_GROUP))
                        .commit()
                true
            }
            R.id.dm_search_tickets -> {
                drawer_toolbar.title = getString(R.string.search)
                false
            }
            else -> false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_drawer_view, menu)
        menuRefresh = menu?.findItem(R.id.menu_dv_refresh)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawer_layout.openDrawer(GravityCompat.START)
                true
            }
            R.id.menu_dv_refresh -> {
                val currentFragment: TicketFragment = supportFragmentManager.findFragmentById(R.id.content_frame) as TicketFragment
                currentFragment.startTicketRefresh()
                true
            }
            R.id.menu_feedback -> {
                val intent = Intent(Intent.ACTION_VIEW)
                val data = Uri.parse("mailto:helwigdev@gmail.com?subject=Feedback for Web Help Desk app")
                return try {
                    intent.data = data
                    startActivity(intent)
                    true
                } catch (e: Exception){
                    Log.e("Feedback error ","something went wrong",e)
                    Crashlytics.logException(e)
                    false
                }
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateTechInfo(){
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        doAsync {
            val prefix = if (prefs.getBoolean(AuthModel.PREF_USE_SSL, true)) "https://" else "http://"
            val cookie = prefs.getString(AuthModel.PREF_COOKIE, "")
            val tech = prefs.getString(AuthModel.PREF_TECH_ID, "")
            val s = "/helpdesk/WebObjects/Helpdesk.woa/ra/Techs/" + tech + "?sessionKey=" +
                    prefs.getString(AuthModel.PREF_SESSION_KEY, "")
            FuelManager.instance.basePath = prefix + prefs.getString(AuthModel.PREF_SERVER, "")


            val response = s.httpGet()
                    .header("Cookie" to cookie)
                    .responseString()
            val result = response.third.component1()
            result ?: return@doAsync

            try {
                val o = JSONObject(result)
                val techName = o.getString("displayName")
                runOnUiThread{
                    try {
                        if(tv_header_user != null) {
                            tv_header_user.text = techName
                            tv_header_user.visibility = View.VISIBLE
                        }
                    } catch (e: Exception){
                        e.printStackTrace()
                    }
                }
            } catch (e: JSONException){
                e.printStackTrace()
            }

        }
    }

}
