package com.example.pidkonfig.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.pidkonfig.R

private val TAB_TITLES = arrayOf(
        R.string.tab_text_1,
        R.string.tab_text_2,
        R.string.tab_text_3,
        R.string.tab_text_4
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager)
    : FragmentPagerAdapter(fm) {
    private val fragmentHashMap: HashMap<Int, PlaceholderFragment> = HashMap()
    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        if (fragmentHashMap[position] != null) {
            return fragmentHashMap[position]!!;
        }else{
            val frag = PlaceholderFragment.newInstance(position + 1)
            fragmentHashMap[position] = frag
            return frag
        }
    }

    fun callOnSettingChange() {
        for (i in 0..getCount()) {
            if (fragmentHashMap[i] != null) {
                fragmentHashMap[i]?.onSettingChange()
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return 4
    }
}