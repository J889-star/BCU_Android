package com.mandarin.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.adapters.SingleClick
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.pack.PackConflict
import com.mandarin.bcu.androidutil.pack.conflict.adapters.PackConfListAdapter
import com.mandarin.bcu.androidutil.pack.conflict.adapters.asynchs.PackConfSolver
import leakcanary.AppWatcher
import leakcanary.LeakCanary
import java.util.*
import kotlin.collections.ArrayList

class PackConflictSolve : AppCompatActivity() {
    companion object {
        const val REQUEST = 800
        const val RESULT_OK = 801
        val data = ArrayList<Int>()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val ed: SharedPreferences.Editor

        if (!shared.contains("initial")) {
            ed = shared.edit()
            ed.putBoolean("initial", true)
            ed.putBoolean("theme", true)
            ed.apply()
        } else {
            if (!shared.getBoolean("theme", false)) {
                setTheme(R.style.AppTheme_night)
            } else {
                setTheme(R.style.AppTheme_day)
            }
        }

        when {
            shared.getInt("Orientation", 0) == 1 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            shared.getInt("Orientation", 0) == 2 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            shared.getInt("Orientation", 0) == 0 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }

        if (!shared.getBoolean("DEV_MODE", false)) {
            AppWatcher.config = AppWatcher.config.copy(enabled = false)
            LeakCanary.showLeakDisplayActivityLauncherIcon(false)
        } else {
            AppWatcher.config = AppWatcher.config.copy(enabled = true)
            LeakCanary.showLeakDisplayActivityLauncherIcon(true)
        }

        DefineItf.check(this)

        setContentView(R.layout.activity_pack_conflict_solve)

        val bck = findViewById<FloatingActionButton>(R.id.packconfbck)
        val pclist = findViewById<ListView>(R.id.packconflist)
        val prog = findViewById<ProgressBar>(R.id.packconfprog)
        val solve = findViewById<Button>(R.id.packconfsolve)

        solve.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                PackConfSolver(this@PackConflictSolve).execute()
            }

        })

        prog.visibility = View.GONE

        bck.setOnClickListener {
            finish()
        }

        val names = ArrayList<String>()

        for(pc in PackConflict.conflicts) {
            names.add(pc.toString())
        }

        if(data.isEmpty()) {
            for(pc in PackConflict.conflicts) {
                data.add(pc.action)
            }
        }

        val adapter = PackConfListAdapter(this, names)

        pclist.adapter = adapter

        pclist.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val intent = Intent(this, PackConflictDetail::class.java)

            intent.putExtra("position", position)

            startActivityForResult(intent, REQUEST)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val lang = shared?.getInt("Language",0) ?: 0

        val config = Configuration()
        var language = StaticStore.lang[lang]

        if(language == "")
            language = Resources.getSystem().configuration.locales.get(0).language

        config.setLocale(Locale(language))
        applyOverrideConfiguration(config)
        super.attachBaseContext(LocaleManager.langChange(newBase,shared?.getInt("Language",0) ?: 0))
    }

    override fun onBackPressed() {
        val bck = findViewById<FloatingActionButton>(R.id.packconfbck)

        bck.performClick()
    }

    public override fun onDestroy() {
        super.onDestroy()
        StaticStore.toast = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK && dataChanged()) {
            val pclist = findViewById<ListView>(R.id.packconflist)
            val names = ArrayList<String>()

            for(pc in PackConflict.conflicts) {
                names.add(pc.toString())
            }

            val adapter = PackConfListAdapter(this, names)

            pclist.adapter = adapter

            pclist.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                val intent = Intent(this, PackConflictDetail::class.java)

                intent.putExtra("position", position)

                startActivityForResult(intent, REQUEST)
            }
        }
    }

    private fun dataChanged() : Boolean {
        for(d in data.indices) {
            val pc = PackConflict.conflicts[d]

            if(data[d] != pc.action) {
                data.clear()

                for(p in PackConflict.conflicts)
                    data.add(p.action)

                return true
            }

        }

        return false
    }
}