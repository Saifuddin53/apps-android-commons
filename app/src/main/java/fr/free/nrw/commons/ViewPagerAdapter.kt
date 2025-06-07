package fr.free.nrw.commons

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter : FragmentPagerAdapter {
    private var fragmentList: List<Fragment> = emptyList()
    private var fragmentTitleList: List<String> = emptyList()

    constructor(manager: FragmentManager) : super(manager)
    constructor(manager: FragmentManager, behavior: Int) : super(manager, behavior)

    override fun getItem(position: Int): Fragment = fragmentList[position]

    override fun getCount(): Int = fragmentList.size

    fun setTabData(fragmentList: List<Fragment>, fragmentTitleList: List<String>) {
        this.fragmentList = fragmentList
        this.fragmentTitleList = fragmentTitleList
    }

    override fun getPageTitle(position: Int): CharSequence = fragmentTitleList[position]
}

