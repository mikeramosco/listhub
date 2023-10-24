package com.justanotherdeveloper.listhub

import kotlinx.android.synthetic.main.activity_home.*

class HomeListeners(private val activity: HomeActivity) {

    init {
        initButtonAnimationListeners()
        initOnClickListeners()
    }

    private fun initButtonAnimationListeners() {
        initButtonAnimationListener(activity.addButton)
        initButtonAnimationListener(activity.searchButton)
        initButtonAnimationListener(activity.examplesButton)
    }

    private fun initOnClickListeners() {
        activity.addButton.setOnClickListener {
            activity.getDialogs().showAddListOrItemDialog()
        }

        activity.searchButton.setOnClickListener {
            activity.openSearchPage()
        }

        activity.examplesButton.setOnClickListener {
            activity.getDialogs().showConfirmDialog(
                activity.getString(R.string.createExamplesPrompt),
                activity.getString(R.string.addExamplesString),
                createExamples = true)
        }
    }
}