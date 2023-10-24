package com.justanotherdeveloper.listhub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class ListsFragment(private val homeActivity: HomeActivity) : Fragment() {

    private lateinit var view: ListsFragmentView
    private lateinit var dialogs: ListsFragmentDialogs
    private lateinit var manager: ListsFragmentManager
    private lateinit var listeners: ListsFragmentListeners

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lists, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        listeners = ListsFragmentListeners(this)
        manager = ListsFragmentManager(this)
        view = ListsFragmentView(this)
        dialogs = ListsFragmentDialogs(this)
    }

    fun getListsView(): ListsFragmentView {
        return view
    }

    fun getDialogs(): ListsFragmentDialogs {
        return dialogs
    }

    fun getDatabase(): ListsDatabase {
        return homeActivity.getDatabase()
    }

    fun getManager(): ListsFragmentManager {
        return manager
    }

    fun getHomeActivity(): HomeActivity {
        return homeActivity
    }
}