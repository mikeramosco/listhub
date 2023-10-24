package com.justanotherdeveloper.listhub

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_lists.*

class HomeActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    private lateinit var homeFragment: HomeFragment
    private lateinit var listsFragment: ListsFragment

    private lateinit var listeners : HomeListeners
    private lateinit var dialogs : HomeDialogs
    private lateinit var database: ListsDatabase

    private var listOpened = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        homeFragment = HomeFragment(this)
        listsFragment = ListsFragment(this)

        listeners = HomeListeners(this)
        dialogs = HomeDialogs(this)
        database = ListsDatabase(this)

        if(database.exampleListsAdded()) examplesButton.visibility = View.GONE

        bottomNavigation.setOnNavigationItemSelectedListener(navListener)
        setupFragments()
    }

    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener {
        var listsVisibility = View.GONE
        var homeVisibility = View.GONE

        when(it.itemId) {
            R.id.nav_lists -> listsVisibility = View.VISIBLE
            else -> homeVisibility = View.VISIBLE
        }

        listsFragmentContainer.visibility = listsVisibility
        homeFragmentContainer.visibility = homeVisibility
        true
    }

    private fun setupFragments() {
        supportFragmentManager.beginTransaction().replace(
            R.id.homeFragmentContainer, homeFragment).commit()
        supportFragmentManager.beginTransaction().replace(
            R.id.listsFragmentContainer, listsFragment).commit()
    }

    private fun goToHomeScreen() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }

    fun getDatabase(): ListsDatabase {
        return database
    }

    fun getDialogs(): HomeDialogs {
        return dialogs
    }

    fun listAlreadyOpened(): Boolean {
        return listOpened
    }

    fun setListOpened() {
        listOpened = true
    }

    fun openNewBulletedList() {
        val bulletedListPage = Intent(this, BulletedListActivity::class.java)
        startActivityForResult(bulletedListPage, OPEN_LIST_CODE)
    }

    fun openNewRoutineList() {
        val routineListPage = Intent(this, RoutineListActivity::class.java)
        startActivityForResult(routineListPage, OPEN_LIST_CODE)
    }

    fun openNewProgressList() {
        val progressListPage = Intent(this, ProgressListActivity::class.java)
        startActivityForResult(progressListPage, OPEN_LIST_CODE)
    }

    fun openNewToDoList() {
        val toDoListPage = Intent(this, ToDoListActivity::class.java)
        startActivityForResult(toDoListPage, OPEN_LIST_CODE)
    }

    fun openSearchPage() {
        val searchPage = Intent(this, SearchPageActivity::class.java)
        startActivityForResult(searchPage, SEARCH_PAGE_CODE)
    }

    fun openReorderPage() {
        if(listsFragment.getManager().inSelectState())
            listsFragment.getListsView().setToDefaultState()
        val reorderPage = Intent(this, ReorderListTitlesActivity::class.java)
        startActivityForResult(reorderPage, REORDER_PAGE_CODE)
    }

    fun createExamples() {
        database.createExampleLists()
        if(listsFragmentContainer.visibility == View.VISIBLE)
            beginTransition(listsFragment.listsFragmentParent)
        if(homeFragmentContainer.visibility == View.VISIBLE)
            beginTransition(homeFragment.homeFragmentParent)
        examplesButton.visibility = View.GONE
        listsFragment.getListsView().reloadLists()
        homeFragment.getHomeView().reloadSection()
    }

    fun getHomeFragment(): HomeFragment {
        return homeFragment
    }

    fun getListFragment(): ListsFragment {
        return listsFragment
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            OPEN_LIST_CODE, SEARCH_PAGE_CODE -> {
                listOpened = false
                listsFragment.getListsView().reloadLists()
                homeFragment.getHomeView().reloadSection()
            }
            REORDER_PAGE_CODE -> {
                val listReordered = data?.getBooleanExtra(
                    listReorderedRef, false)?: false
                if(listReordered) {
                    listsFragment.getListsView().reloadLists()
                    homeFragment.getManager().loadSection()
                }
            }
        }
    }

    override fun onBackPressed() {
        if(listsFragmentContainer.visibility == View.VISIBLE
            && listsFragment.getManager().inSelectState())
            listsFragment.getListsView().setToDefaultState()
        else goToHomeScreen()
    }

    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, day: Int) {
        homeFragment.getDialogs().setDateOnDialog(createCalendar(year, month, day))
    }

    override fun onResume() {
        super.onResume()
        homeFragment.getManager().sectionReloadedIfDateIsOutdated()
    }
}
