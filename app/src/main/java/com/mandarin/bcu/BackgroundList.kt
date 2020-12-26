package com.mandarin.bcu

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.mandarin.bcu.androidutil.supports.adapter.BGListPager
import com.mandarin.bcu.androidutil.LocaleManager
import com.mandarin.bcu.androidutil.StaticStore
import com.mandarin.bcu.androidutil.supports.MeasureViewPager
import com.mandarin.bcu.androidutil.supports.SingleClick
import com.mandarin.bcu.androidutil.io.AContext
import com.mandarin.bcu.androidutil.io.DefineItf
import com.mandarin.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.pack.Identifier
import common.pack.PackData
import common.pack.UserProfile
import java.util.*
import kotlin.collections.ArrayList

class BackgroundList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val ed = shared.edit()

        if (!shared.contains("initial")) {
            ed.putBoolean("initial", true)
            ed.putBoolean("theme", true)
            ed.putBoolean("frame", true)
            ed.putBoolean("apktest", false)
            ed.putInt("default_level", 50)
            ed.putInt("Language", 0)
            ed.apply()
        } else {
            if (!shared.getBoolean("theme", false)) {
                setTheme(R.style.AppTheme_night)
            } else {
                setTheme(R.style.AppTheme_day)
            }
        }

        LeakCanaryManager.initCanary(shared)

        DefineItf.check(this)

        AContext.check()

        (CommonStatic.ctx as AContext).updateActivity(this)

        setContentView(R.layout.activity_background_list)

        val tab = findViewById<TabLayout>(R.id.bglisttab)
        val pager = findViewById<MeasureViewPager>(R.id.bglistpager)

        pager.removeAllViewsInLayout()
        pager.adapter = BGListTab(supportFragmentManager)
        pager.offscreenPageLimit = getExistingBGPack()
        pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab))

        tab.setupWithViewPager(pager)

        if(getExistingBGPack() == 1) {
            tab.visibility = View.GONE

            val collapse = findViewById<CollapsingToolbarLayout>(R.id.bgcollapse)

            val param = collapse.layoutParams as AppBarLayout.LayoutParams

            param.scrollFlags = 0

            collapse.layoutParams = param
        }

        val bck = findViewById<FloatingActionButton>(R.id.bgbck)

        bck.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                finish()
            }
        })
    }

    override fun attachBaseContext(newBase: Context) {
        val shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val lang = shared?.getInt("Language",0) ?: 0

        val config = Configuration()
        var language = StaticStore.lang[lang]
        var country = ""

        if(language == "") {
            language = Resources.getSystem().configuration.locales.get(0).language
            country = Resources.getSystem().configuration.locales.get(0).country
        }

        val loc = if(country.isNotEmpty()) {
            Locale(language, country)
        } else {
            Locale(language)
        }

        config.setLocale(loc)
        applyOverrideConfiguration(config)
        super.attachBaseContext(LocaleManager.langChange(newBase,shared?.getInt("Language",0) ?: 0))
    }

    public override fun onDestroy() {
        super.onDestroy()
        StaticStore.toast = null
    }

    override fun onResume() {
        AContext.check()

        if(CommonStatic.ctx is AContext)
            (CommonStatic.ctx as AContext).updateActivity(this)

        super.onResume()
    }

    private fun getExistingBGPack() : Int {
        var res = 0

        for(p in UserProfile.getAllPacks()) {
            if(p.bgs.list.isNotEmpty())
                res++
        }

        return res
    }

    inner class BGListTab(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val keys: ArrayList<String>

        init {
            val lit = fm.fragments
            val trans = fm.beginTransaction()

            for (f in lit) {
                trans.remove(f)
            }

            trans.commitAllowingStateLoss()

            keys = getExistingPack()
        }

        override fun getItem(position: Int): Fragment {
            return BGListPager.newInstance(keys[position])
        }

        override fun getCount(): Int {
            return keys.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return if (position == 0) {
                getString(R.string.pack_default)
            } else {
                val pack = UserProfile.getUserPack(keys[position])

                if (pack == null) {
                    keys[position]
                }

                val name = pack?.desc?.name ?: ""

                if (name.isEmpty()) {
                    keys[position]
                } else {
                    name
                }
            }
        }

        override fun saveState(): Parcelable? {
            return null
        }

        private fun getExistingPack(): ArrayList<String> {
            val list = UserProfile.getAllPacks()

            val res = ArrayList<String>()

            for(k in list) {
                if(k is PackData.DefPack) {
                    res.add(Identifier.DEF)
                } else if(k is PackData.UserPack && k.bgs.size() != 0) {
                    res.add(k.desc.id)
                }
            }

            return res
        }
    }
}