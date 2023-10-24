package com.justanotherdeveloper.listhub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class HomeFragment(private val homeActivity: HomeActivity) : Fragment() {

    private lateinit var listeners: HomeFragmentListeners
    private lateinit var manager: HomeFragmentManager
    private lateinit var view: HomeFragmentView
    private lateinit var dialogs: HomeFragmentDialogs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        listeners = HomeFragmentListeners(this)
        dialogs = HomeFragmentDialogs(this, homeActivity)
        manager = HomeFragmentManager(this, homeActivity)
        view = HomeFragmentView(this, homeActivity)

        manager.loadHomeSectionsItems()
    }

    fun getDatabase(): ListsDatabase {
        return homeActivity.getDatabase()
    }

    fun getManager(): HomeFragmentManager {
        return manager
    }

    fun getHomeView(): HomeFragmentView {
        return view
    }

    fun getDialogs(): HomeFragmentDialogs {
        return dialogs
    }
}