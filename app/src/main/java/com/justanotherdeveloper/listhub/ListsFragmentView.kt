package com.justanotherdeveloper.listhub

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Handler
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.util.set
import androidx.core.view.iterator
import kotlinx.android.synthetic.main.fragment_lists.*
import kotlinx.android.synthetic.main.fragment_lists.reorderOption
import kotlinx.android.synthetic.main.fragment_lists.sortByOption

@SuppressLint("InflateParams")
class ListsFragmentView(private val fragment: ListsFragment) {

    private val listTitleViewMap = SparseArray<View>()
    private val listTitleViewIsPressedMap = HashMap<View, Boolean>()
    private val listTitleViewIsHighlightedMap = HashMap<View, Boolean>()

    init {
        updateShowDetailsOptionState()
        reloadLists()
    }

    fun toggleShowDetails() {
        beginTransition(fragment.listsFragmentParent)
        val detailsShown = fragment.getDatabase().listsDetailsShown()
        updateShowDetailsOptionState(detailsShown)

        fun toggleDetailsIconVisibility(view: View) {
            val detailsIcon = view.findViewById<ImageView>(R.id.detailsIcon)
            detailsIcon.visibility = if(detailsShown)
                View.VISIBLE else View.GONE
        }

        for(view in fragment.listTitlesContainer.iterator())
            toggleDetailsIconVisibility(view)
    }

    private fun updateShowDetailsOptionState(
        detailsShown: Boolean = fragment.getDatabase().listsDetailsShown()) {
        val showDetailsCode = if(detailsShown)
            R.string.hideDetailsString else R.string.showDetailsString
        fragment.showDetailsText.text = fragment.getString(showDetailsCode)
    }

    fun releaseTaskViews() {
        fun releaseTaskView(view: View) {
            val viewIsHighlighted = listTitleViewIsHighlightedMap[view]
            if(viewIsHighlighted != null && viewIsHighlighted) {
                listTitleViewIsHighlightedMap[view] = false
                if(view == fragment.createNewListLayout)
                    animateButton(view, false)
                else {
                    val listTitleLayout = view
                        .findViewById<LinearLayout>(R.id.listTitleLayout)
                    animateButton(listTitleLayout, false)
                }
            }

            val viewIsPressed = listTitleViewIsPressedMap[view]
            if(viewIsPressed != null && viewIsPressed)
                listTitleViewIsPressedMap[view] = false
        }

        for(view in fragment.listTitlesContainer.iterator()) releaseTaskView(view)
        releaseTaskView(fragment.createNewListLayout)
    }

    private fun ArrayList<String>.labelsAreChecked(): Boolean {
        val allLabels = fragment.getDatabase().getLabels()
        val noLabelsString = fragment.getString(R.string.noLabelsString)
        for(filter in this) {
            when(filter) {
                archivedRef, favoritesRef, toDoListRef, progressListRef,
                routineListRef, bulletedListRef -> {}
                noLabelsString -> return true
                else -> if(allLabels.contains(filter)) return true
            }
        }
        return false
    }

    private fun refreshListViews(animate: Boolean = false) {
        refreshListOrderIfSorted(animate = animate)
        applyFilters(fragment.getDatabase()
            .getFilters(), animate = animate)
    }

    private fun noFiltersChecked(filters: ArrayList<String>): Boolean {
        if(filters.size == 0) return true
        val allLabels = fragment.getDatabase().getLabels()
        val noLabelsString = fragment.getString(R.string.noLabelsString)
        for(filter in filters) {
            when(filter) {
                archivedRef, favoritesRef, toDoListRef, progressListRef,
                routineListRef, bulletedListRef, noLabelsString -> return false
                else -> if(allLabels.contains(filter)) return false
            }
        }
        return true

    }

    fun applyFilters(filters: ArrayList<String>, animate: Boolean = true) {
        if(animate) beginTransition(fragment.listsFragmentParent)
        if(noFiltersChecked(filters)) {
            fragment.filterListsText.text =
                fragment.getString(R.string.filterListsString)
            fragment.filterListsOption.background =
                ContextCompat.getDrawable(fragment.requireContext(),
                    R.drawable.transition_layout_rounded_border)
            for(list in fragment.getManager().getLists()) {
                val view = listTitleViewMap[list.getListId()]
                view.visibility = if(list.isArchived())
                    View.GONE else View.VISIBLE
            }
            return
        }

        fragment.filterListsText.text =
            fragment.getString(R.string.listsFilteredString)
        fragment.filterListsOption.background =
            ContextCompat.getDrawable(fragment.requireContext(),
                R.drawable.transition_layout_rounded_border_selected)

        val noLabelsString = fragment.getString(R.string.noLabelsString)

        val archivedOnly = filters.contains(archivedRef)
        val favoritesOnly = filters.contains(favoritesRef)
        val toDoListsChecked = filters.contains(toDoListRef)
        val progressListsChecked = filters.contains(progressListRef)
        val routineListsChecked = filters.contains(routineListRef)
        val bulletedListsChecked = filters.contains(bulletedListRef)
        val noLabelsChecked = filters.contains(noLabelsString)
        val specificListTypesOnly = toDoListsChecked || progressListsChecked
                || routineListsChecked || bulletedListsChecked
        val checkedLabelsOnly = filters.labelsAreChecked()

        fun List.includedForLabel(): Boolean {
            val labels = getLabels()
            for(filter in filters) {
                when(filter) {
                    archivedRef, favoritesRef, toDoListRef, progressListRef,
                    routineListRef, bulletedListRef -> {}
                    else -> if(labels.contains(filter)) return true
                }
            }
            return false
        }

        for(list in fragment.getManager().getLists()) {

            val includedForArchived = archivedOnly == list.isArchived()

            var includedForStar = true
            if(favoritesOnly && !list.isStarred())
                includedForStar = false

            var includedForListType = true
            if(specificListTypesOnly) {
                includedForListType = false
                when {
                    toDoListsChecked && list.getListType() == toDoListRef ->
                        includedForListType = true
                    progressListsChecked && list.getListType() == progressListRef ->
                        includedForListType = true
                    routineListsChecked && list.getListType() == routineListRef ->
                        includedForListType = true
                    bulletedListsChecked && list.getListType() == bulletedListRef ->
                        includedForListType = true
                }
            }

            var includedForLabel = true
            if(checkedLabelsOnly)
                includedForLabel = if(noLabelsChecked && list.getLabels().isEmpty())
                     true else list.includedForLabel()

            val view = listTitleViewMap[list.getListId()]

            view.visibility = if(includedForArchived
                && includedForStar && includedForListType
                && includedForLabel) View.VISIBLE else View.GONE
        }
    }

    private fun refreshListOrderIfSorted(animate: Boolean = false) {
        if(fragment.getDatabase().listsAreSorted())
            sortList(fragment.getDatabase().getSortIndex(), animate)
    }

    private fun moveListTitleViewsToSortedOrder(sortIndex: Int) {
        val sortedListTitlesOrder =
            fragment.getManager().getSortedListTitlesOrder(sortIndex)
        fragment.listTitlesContainer.removeAllViews()
        for(list in sortedListTitlesOrder)
            fragment.listTitlesContainer.addView(listTitleViewMap[list.getListId()])
    }

    fun sortList(sortIndex: Int, animate: Boolean = true) {
        if(animate) beginTransition(fragment.listsFragmentParent)
        if(sortIndex == 0) {
            fragment.sortByOption.background =
                ContextCompat.getDrawable(fragment.requireContext(),
                    R.drawable.transition_layout_rounded_border)
            fragment.sortByIcon.setImageResource(R.drawable.ic_sort_white)
            fragment.sortedByText.text = fragment.getString(R.string.sortByString)
        } else {
            fragment.sortByOption.background =
                ContextCompat.getDrawable(fragment.requireContext(),
                    R.drawable.transition_layout_rounded_border_selected)
            val sortIsDescending = sortIndex % 2 != 0
            val sortByIconCode = if(sortIsDescending)
                R.drawable.ic_keyboard_arrow_down_white
            else R.drawable.ic_keyboard_arrow_up_white
            fragment.sortByIcon.setImageResource(sortByIconCode)
            fragment.sortedByText.text = getSortedByString(sortIndex)
        }

        moveListTitleViewsToSortedOrder(sortIndex)
    }

    private fun getSortedByString(sortIndex: Int): String {
        val stringCode = when(sortIndex) {
            sortAToZIndex, sortZToAIndex -> R.string.sortedAlphabeticallyString
            sortNewestFirstIndex, sortOldestFirstIndex -> R.string.sortedByCreationDateString
            else -> R.string.sortedByLastUpdatedString
        }
        return fragment.getString(stringCode)
    }

    fun getListTitleView(listId: Int): View {
        return listTitleViewMap[listId]
    }

    fun reloadLists() {
        listTitleViewMap.clear()
        listTitleViewIsPressedMap.clear()
        listTitleViewIsHighlightedMap.clear()
        fragment.listTitlesContainer.removeAllViews()
        fragment.getManager().clearLists()
        val detailsShown = fragment.getDatabase().listsDetailsShown()
        for(id in fragment.getDatabase().getListIds()) {
            val list = fragment.getDatabase().getList(id)
            addListView(list, detailsShown)
        }
        initListTitleLayoutAnimationListener(fragment.createNewListLayout)
        refreshListViews()
    }

    @Suppress("DEPRECATION")
    private fun initListTitleLayoutAnimationListener(view: View, button: View = view) {
        val handler = Handler()
        var viewBounds = Rect()
        button.setOnTouchListener { v, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    listTitleViewIsPressedMap[view] = true
                    viewBounds = Rect(v.left, v.top, v.right, v.bottom)
                    handler.postDelayed({
                        val listTitleViewIsPressed = listTitleViewIsPressedMap[view]
                        if(listTitleViewIsPressed != null && listTitleViewIsPressed) {
                            listTitleViewIsHighlightedMap[view] = true
                            animateButton(button, true)
                        }
                    }, TRANSITION_DELAY)
                }
                MotionEvent.ACTION_UP -> {
                    val listTitleViewIsPressed = listTitleViewIsPressedMap[view]
                    if(listTitleViewIsPressed != null && listTitleViewIsPressed &&
                        viewBounds.contains(v.left + event.x.toInt(), v.top + event.y.toInt())) {
                        listTitleViewIsHighlightedMap[view] = false
                        listTitleViewIsPressedMap[view] = false
                        animateButton(button, false)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    val listTitleViewIsPressed = listTitleViewIsPressedMap[view]
                    if(listTitleViewIsPressed != null && listTitleViewIsPressed &&
                        !viewBounds.contains(v.left + event.x.toInt(), v.top + event.y.toInt())) {
                        listTitleViewIsPressedMap[view] = false

                        val listTitleViewIsHighlighted = listTitleViewIsHighlightedMap[view]
                        if(listTitleViewIsHighlighted != null && listTitleViewIsHighlighted) {
                            listTitleViewIsHighlightedMap[view] = false
                            animateButton(button, false)
                        }
                    }
                }
            }
            false
        }
    }

    private fun addListView(list: List, detailsShown: Boolean) {
        val view = fragment.layoutInflater.inflate(R.layout.view_list_title, null)

        val listTitleLayout = view.findViewById<LinearLayout>(R.id.listTitleLayout)
        val listTypeIcon = view.findViewById<ImageView>(R.id.listTypeIcon)
        val listTitleText = view.findViewById<TextView>(R.id.listTitleText)
        val listDetailsLayout = view.findViewById<LinearLayout>(R.id.listDetailsLayout)
        val noteIcon = view.findViewById<ImageView>(R.id.noteIcon)
        val listDetailsDivider1 = view.findViewById<TextView>(R.id.listDetailsDivider1)
        val dateIcon = view.findViewById<ImageView>(R.id.dateIcon)
        val dateText = view.findViewById<TextView>(R.id.dateText)
        val listDetailsDivider2 = view.findViewById<TextView>(R.id.listDetailsDivider2)
        val labelIcon = view.findViewById<ImageView>(R.id.labelIcon)
        val labelText = view.findViewById<TextView>(R.id.labelText)
        val detailsIcon = view.findViewById<ImageView>(R.id.detailsIcon)
        val star = view.findViewById<ImageView>(R.id.star)

        listTitleText.text = list.getTitle()

        if(list.hasLabels()) {
            labelIcon.visibility = View.VISIBLE
            labelText.visibility = View.VISIBLE
            labelText.text = list.getLabelsString()
        }

        var hasDate = false
        if(list.getListType() == routineListRef){
            val routineList = fragment.getDatabase()
                .getRoutineList(list.getListId())
            hasDate = routineList.hasDate() || routineList.isRepeating()
            if(hasDate) {
                dateIcon.visibility = View.VISIBLE
                dateText.visibility = View.VISIBLE
                var dateString = ""
                if(routineList.hasDate()) {
                    val date = routineList.getDate()?: getTodaysDate()
                    dateString = fragment.requireActivity().getRecencyText(date)
                } else if(routineList.isRepeating()) {
                    dateIcon.setImageResource(R.drawable.ic_autorenew_gray)
                    dateString = routineList.getRepeatingDaysString(
                        fragment.requireContext())
                }
                dateText.text = dateString
            }
        }

        if(list.hasNote()) noteIcon.visibility = View.VISIBLE

        val hasLabels = list.hasLabels()
        val hasNote = list.hasNote()

        listDetailsLayout.visibility =
            if(hasLabels || hasNote || hasDate)
                View.VISIBLE else View.GONE

        var div1IsVisible = false
        val div2IsVisible: Boolean
        if(hasNote) {
            div1IsVisible = hasDate
            div2IsVisible = hasLabels
        } else div2IsVisible = hasDate && hasLabels
        listDetailsDivider1.visibility =
            if(div1IsVisible) View.VISIBLE else View.GONE
        listDetailsDivider2.visibility =
            if(div2IsVisible) View.VISIBLE else View.GONE

        if(detailsShown) detailsIcon.visibility = View.VISIBLE
        detailsIcon.setOnClickListener {
            when {
                fragment.getManager().inSelectState() -> selectList(view, list)
                else ->  when(list.getListType()) {
                    toDoListRef -> fragment.getHomeActivity().openToDoListDetailsDialog(
                        fragment.getHomeActivity().getDatabase().getToDoList(list.getListId()))
                    progressListRef -> fragment.getHomeActivity().openProgressListDetailsDialog(
                        fragment.getHomeActivity().getDatabase().getProgressList(list.getListId()))
                    routineListRef -> fragment.getHomeActivity().openRoutineListDetailsDialog(
                        fragment.getHomeActivity().getDatabase().getRoutineList(list.getListId()))
                    bulletedListRef -> fragment.getHomeActivity().openBulletedListDetailsDialog(
                        fragment.getHomeActivity().getDatabase().getBulletedList(list.getListId()))
                }
            }
        }

        detailsIcon.setOnLongClickListener {
            if(!fragment.getManager().inSelectState())
                setToListSelectState(view, list)
            else fragment.getDialogs().showSelectedMoreOptionsDialog()
            true
        }

        val listTypeImageRes = when(list.getListType()) {
            toDoListRef -> R.drawable.ic_list_white
            progressListRef -> R.drawable.ic_view_list_white
            routineListRef -> R.drawable.ic_format_list_numbered_white
            else -> R.drawable.ic_format_list_bulleted_white
        }

        listTypeIcon.setImageResource(listTypeImageRes)

        fun toggleListStar(index: Int = 0) {
            if(!fragment.getDatabase().listsAreSorted()) {
                val currentIndex = fragment.listTitlesContainer.indexOfChild(view)
                if(index != currentIndex) {
                    beginTransition(fragment.listsFragmentParent)
                    fragment.listTitlesContainer.removeView(view)
                    fragment.listTitlesContainer.addView(view, index)
                }
            }

            val colorTheme = fragment.requireActivity().getColorTheme()
            if(list.isStarred())
                star.setColoredImageResource(R.drawable.ic_star_custom, colorTheme)
            else star.setImageResource(R.drawable.ic_star_border_gray)
            refreshListViews(animate = true)
            fragment.getHomeActivity().getHomeFragment().getHomeView().reloadSection()
        }

        star.setOnClickListener {
            when {
                fragment.getManager().inSelectState() -> selectList(view, list)
                list.isStarred() -> {
                    list.listUpdated()
                    list.toggleStar()
                    fragment.getDatabase().toggleStarOfList(list)
                    val index = fragment.getDatabase()
                        .removeListFromFavorites(list.getListId())
                    fragment.getManager().refreshListsOrder()
                    toggleListStar(index)
                }
                else -> {
                    list.listUpdated()
                    list.toggleStar()
                    fragment.getDatabase().toggleStarOfList(list)
                    fragment.getDatabase().addListToFavorites(list.getListId())
                    fragment.getManager().refreshListsOrder()
                    toggleListStar()
                }
            }
        }

        star.setOnLongClickListener {
            if(!fragment.getManager().inSelectState())
                setToListSelectState(view, list)
            else fragment.getDialogs().showSelectedMoreOptionsDialog()
            true
        }

        val colorTheme = fragment.requireActivity().getColorTheme()
        if(list.isStarred()) star.setColoredImageResource(R.drawable.ic_star_custom, colorTheme)

        listTitleViewIsPressedMap[view] = false
        listTitleViewIsHighlightedMap[view] = false
        initListTitleLayoutAnimationListener(view, listTitleLayout)
        listTitleLayout.setOnClickListener {
            if(fragment.getManager().inSelectState())
                selectList(view, list)
            else if(!fragment.getHomeActivity().listAlreadyOpened()) {
                fragment.getHomeActivity().setListOpened()
                when(list.getListType()) {
                    toDoListRef -> fragment.getHomeActivity().openToDoList(list.getListId())
                    progressListRef -> fragment.getHomeActivity().openProgressList(list.getListId())
                    routineListRef -> fragment.getHomeActivity().openRoutineList(list.getListId())
                    bulletedListRef -> fragment.getHomeActivity().openBulletedList(list.getListId())
                }
            }

        }

        listTitleLayout.setOnLongClickListener {
            if(!fragment.getManager().inSelectState())
                setToListSelectState(view, list)
            else fragment.getDialogs().showSelectedMoreOptionsDialog()
            true
        }

        fragment.listTitlesContainer.addView(view)

        listTitleViewMap[list.getListId()] = view
        fragment.getManager().addList(list)
    }

    fun setToDefaultState() {
        if(!fragment.getManager().inSelectState()) return
        fragment.getManager().setSelectState(false)

        beginTransition(fragment.listsFragmentParent)
        fragment.sortByOption.visibility = View.VISIBLE
        fragment.filterListsOption.visibility = View.VISIBLE
        fragment.showDetailsOption.visibility = View.VISIBLE
        fragment.reorderOption.visibility = View.VISIBLE
        fragment.deleteOption.visibility = View.GONE
        fragment.duplicateOption.visibility = View.GONE
        fragment.archiveOption.visibility = View.GONE
        fragment.unarchiveOption.visibility = View.GONE
        fragment.manageLabelsOption.visibility = View.GONE
        fragment.clearSelectionOption.visibility = View.GONE

        fragment.listTitlesOptionsScrollView.fullScroll(HorizontalScrollView.FOCUS_LEFT)

        fun resetListTitleView(list: List) {
            val selectedListTitleView = listTitleViewMap[list.getListId()]
            val listTitleLayout = selectedListTitleView.findViewById<LinearLayout>(R.id.listTitleLayout)
            listTitleLayout.background = ContextCompat.getDrawable(
                fragment.requireContext(), R.drawable.transition_layout_list_item)
            val listTypeIcon = selectedListTitleView.findViewById<ImageView>(R.id.listTypeIcon)
            val listTypeImageRes = when(list.getListType()) {
                toDoListRef -> R.drawable.ic_list_white
                progressListRef -> R.drawable.ic_view_list_white
                routineListRef -> R.drawable.ic_format_list_numbered_white
                else -> R.drawable.ic_format_list_bulleted_white
            }

            listTypeIcon.setImageResource(listTypeImageRes)
        }

        for(list in fragment.getManager().getLists())
            resetListTitleView(list)
    }

    private fun selectList(selectedListTitleView: View, list: List,
                           selectOnly: Boolean = false, animate: Boolean = true) {
        val listIsSelected = fragment.getManager().listIsSelected(list) ?: return
        if(selectOnly && listIsSelected) return

        val isSelected = fragment.getManager().toggleSelectedList(list)?: return
        val selectedCount = fragment.getManager().getSelectedCount()

        if(selectedCount == 0) {
            setToDefaultState()
            return
        }

        val listTypeIcon = selectedListTitleView.findViewById<ImageView>(R.id.listTypeIcon)
        val radioButtonCode = if(isSelected) R.drawable.ic_radio_button_checked_custom
        else R.drawable.ic_radio_button_unchecked_custom
        val colorTheme = fragment.requireActivity().getColorTheme()
        listTypeIcon.setColoredImageResource(radioButtonCode, colorTheme)
        val listTitleLayout = selectedListTitleView.findViewById<LinearLayout>(R.id.listTitleLayout)
        val backgroundCode = if(isSelected) R.drawable.transition_layout_list_item_selected
        else R.drawable.transition_layout_list_item
        listTitleLayout.background = ContextCompat.getDrawable(fragment.requireContext(), backgroundCode)

        if(animate) beginTransition(fragment.listsFragmentParent)
        fragment.manageLabelsText.text = if(fragment.getManager().selectedListsHaveLabels())
            fragment.getString(R.string.manageLabelsString) else fragment.getString(R.string.addLabelString)
    }

    private fun setToListSelectState(selectedListTitleView: View? = null, list: List? = null) {
        fragment.getManager().setSelectState(true)

        fragment.sortByOption.visibility = View.GONE
        fragment.filterListsOption.visibility = View.GONE
        fragment.showDetailsOption.visibility = View.GONE
        fragment.reorderOption.visibility = View.GONE
        fragment.deleteOption.visibility = View.VISIBLE
        fragment.duplicateOption.visibility = View.VISIBLE
        fragment.manageLabelsOption.visibility = View.VISIBLE
        fragment.clearSelectionOption.visibility = View.VISIBLE

        if(fragment.getDatabase().getFilters().contains(archivedRef))
            fragment.unarchiveOption.visibility = View.VISIBLE
        else fragment.archiveOption.visibility = View.VISIBLE

        val colorTheme = fragment.requireActivity().getColorTheme()
        for(listTitleView in fragment.listTitlesContainer.iterator()) {
            val listTypeIcon = listTitleView.findViewById<ImageView>(R.id.listTypeIcon)
            listTypeIcon.setColoredImageResource(R.drawable.ic_radio_button_unchecked_custom, colorTheme)
        }

        if(selectedListTitleView != null && list != null)
            selectList(selectedListTitleView, list, animate = false)

        fragment.listTitlesContainer.post {
            fragment.listTitlesOptionsScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
        }
    }

    fun selectAll() {
        var noViewsVisible = true
        for(list in fragment.getManager().getLists()) {
            val listView = listTitleViewMap[list.getListId()]
            if (listView.visibility == View.VISIBLE) {
                noViewsVisible = false
                break
            }
        }
        if(noViewsVisible) return

        if(!fragment.getManager().inSelectState())
            setToListSelectState()

        for(list in fragment.getManager().getLists()) {
            val listView = listTitleViewMap[list.getListId()]
            if(listView.visibility == View.VISIBLE)
                selectList(listView, list, selectOnly = true, animate = false)
        }
    }
}