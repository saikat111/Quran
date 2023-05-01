package com.quantum.quran.activity

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.quantum.quran.R
import com.quantum.quran.adapter.PagerAdapter
import com.quantum.quran.constant.Para
import com.quantum.quran.database.ApplicationData
import com.quantum.quran.databinding.ActivityParaBinding
import com.quantum.quran.fragment.ParaAyat
import com.quantum.quran.theme.ApplicationTheme
import com.quantum.quran.utils.ContextUtils
import java.text.NumberFormat
import java.util.*

class ParaActivity : AppCompatActivity() {

    companion object {
        fun launch(context: Context, paraNo: Int) {
            context.startActivity(
                Intent(
                    context,
                    ParaActivity::class.java
                ).putExtra("PARA_NO", paraNo)
            )
        }
    }
    private var binding: ActivityParaBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApplicationTheme(this)
        binding = ActivityParaBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        binding?.back?.setOnClickListener { finish() }

        val numberFormat: NumberFormat =
            NumberFormat.getInstance(Locale(ApplicationData(this).language))
        binding?.qPager?.adapter = PagerAdapter(
            supportFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ).apply {
            Para().Position().reversed()
                .forEach {
                    addFragment(ParaAyat(it.paraNo))
                    binding?.tabLayout?.newTab()?.setText(
                        resources.getString(R.string.para) +
                            " -> ${numberFormat.format(it.paraNo)}")?.let { it1 ->
                        binding?.tabLayout?.addTab(it1)
                    }
                }
        }

        binding?.tabLayout?.tabGravity = TabLayout.GRAVITY_FILL

        binding?.qPager?.addOnPageChangeListener(
            TabLayout.TabLayoutOnPageChangeListener(binding?.tabLayout)
        )

        binding?.tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                /****/
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                /****/
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                binding?.qPager?.currentItem = tab.position
            }
        })

        binding?.qPager?.offscreenPageLimit = 3
        binding?.qPager?.currentItem = Para().Position().size -
                intent.getIntExtra("PARA_NO", 0)
    }

    override fun attachBaseContext(newBase: Context?) {
        val localeToSwitchTo = Locale(ApplicationData(newBase!!).language)
        val localeUpdatedContext: ContextWrapper = ContextUtils.updateLocale(newBase, localeToSwitchTo)
        super.attachBaseContext(localeUpdatedContext)
    }
}