package com.justanotherdeveloper.listhub

import kotlinx.android.synthetic.main.fragment_lists.*

class ListsFragmentManager(private val fragment: ListsFragment) {

    private var inSelectState = false
    private var selectedCount = 0
    private val listSelectedMap = HashMap<List, Boolean>()
    private val lists = ArrayList<List>()

    fun clearLists() {
        lists.clear()
    }

    fun archiveOptionClicked() {
        val archiveListsPrompt = if(selectedCount > 1)
            fragment.getString(R.string.archiveSelectedListsPrompt)
        else fragment.getString(R.string.archiveSelectedListPrompt)
        fragment.getDialogs().showConfirmDialog(archiveListsPrompt,
            fragment.getString(R.string.archiveCapsString), archiveLists = true)
    }

    fun unarchiveOptionClicked() {
        val unarchiveListsPrompt = if(selectedCount > 1)
            fragment.getString(R.string.unarchiveSelectedListsPrompt)
        else fragment.getString(R.string.unarchiveSelectedListPrompt)
        fragment.getDialogs().showConfirmDialog(unarchiveListsPrompt,
            fragment.getString(R.string.unarchiveCapsString), unarchiveLists = true)
    }

    fun deleteOptionClicked() {
        val deleteListsPrompt = if(selectedCount > 1)
            fragment.getString(R.string.deleteSelectedListsPrompt)
        else fragment.getString(R.string.deleteSelectedListPrompt)
        fragment.getDialogs().showConfirmDialog(deleteListsPrompt,
            fragment.getString(R.string.deleteString), deleteLists = true)
    }

    fun duplicateOptionClicked() {
        val duplicateListsPrompt = if(selectedCount > 1)
            fragment.getString(R.string.duplicateListsPrompt)
        else fragment.getString(R.string.duplicateListPrompt)
        fragment.getDialogs().showConfirmDialog(duplicateListsPrompt,
            fragment.getString(R.string.duplicateString), duplicateLists = true)
    }

    fun refreshListsOrder() {
        val ids = fragment.getDatabase().getListIds()
        val tempLists = ArrayList<List>()
        for(list in lists) tempLists.add(list)
        lists.clear()
        for(id in ids) {
            for(list in tempLists) {
                if(id == list.getListId()) {
                    lists.add(list)
                    break
                }
            }
        }
    }

    fun sortList(sortIndex: Int) {
        fragment.getDatabase().setSortIndex(sortIndex)
        fragment.getListsView().sortList(sortIndex)
    }

    fun getSortedListTitlesOrder(sortIndex: Int): ArrayList<List> {
        return when(sortIndex) {
            sortLastUpdatedDescendingIndex -> sortListsByLastUpdated(lists)
            sortLastUpdatedAscendingIndex -> sortListsByLastUpdated(lists,false)
            sortNewestFirstIndex -> sortListsByCreationDate(lists)
            sortOldestFirstIndex -> sortListsByCreationDate(lists,false)
            sortAToZIndex -> sortListsAlphabetically(lists)
            sortZToAIndex -> sortListsAlphabetically(lists,false)
            else -> this.lists
        }
    }

    fun deleteSelectedLists() {
        val listsToDelete = ArrayList<List>()
        for(list in lists) {
            val listIsSelected = listIsSelected(list)?: false
            if(listIsSelected) {
                listsToDelete.add(list)
                fragment.getDatabase()
                    .deleteList(list.getListId())
                val listTitleView = fragment.getListsView()
                    .getListTitleView(list.getListId())
                fragment.listTitlesContainer.removeView(listTitleView)
            }
        }
        for(list in listsToDelete)
            lists.remove(list)
        fragment.getListsView().applyFilters(fragment.getDatabase().getFilters(), false)
        fragment.getHomeActivity().getHomeFragment().getHomeView().reloadSection()
    }

    fun duplicateSelectedLists() {
        for(list in lists) {
            val listIsSelected = listIsSelected(list)?: false
            if(listIsSelected) fragment.getDatabase().duplicateList(list)
        }
        fragment.getListsView().setToDefaultState()
        fragment.getListsView().reloadLists()
        fragment.getHomeActivity().getHomeFragment().getHomeView().reloadSection()
    }

    fun archiveSelectedLists() {
        for(list in lists) {
            val listIsSelected = listIsSelected(list)?: false
            if(listIsSelected) {
                list.archive()
                fragment.getDatabase().archiveList(list)
            }
        }
        fragment.getListsView().setToDefaultState()
        fragment.getListsView().applyFilters(fragment.getDatabase().getFilters())
        fragment.getHomeActivity().getHomeFragment().getHomeView().reloadSection()
    }

    fun unarchiveSelectedLists() {
        for(list in lists) {
            val listIsSelected = listIsSelected(list)?: false
            if(listIsSelected) {
                list.unarchive()
                fragment.getDatabase().unarchiveList(list)
            }
        }
        fragment.getListsView().setToDefaultState()
        fragment.getListsView().applyFilters(fragment.getDatabase().getFilters())
        fragment.getHomeActivity().getHomeFragment().getHomeView().reloadSection()
    }

    fun updateLabelsOfSelected(labelsToAddOrRemove: ArrayList<String>,
                               checkedLabels: HashMap<String, Boolean>) {
        for(list in lists) {
            val listIsSelected = listIsSelected(list)?: false
            if(listIsSelected) fragment.getDatabase().addAndRemoveLabels(
                list, labelsToAddOrRemove, checkedLabels)
        }
        fragment.getListsView().applyFilters(fragment.getDatabase().getFilters(), false)
    }

    fun getAllLabelsOfSelected(labels: ArrayList<String>): ArrayList<String> {
        val allLabels = ArrayList<String>()
        for(label in labels) {
            var addLabel = false
            for(list in lists) {
                val listIsSelected = listIsSelected(list)?: false
                if(listIsSelected && list.getLabels().contains(label)) {
                    addLabel = true
                    break
                }
            }
            if(addLabel) allLabels.add(label)
        }
        return allLabels
    }

    fun selectedListsHaveLabels(): Boolean {
        for(list in lists) {
            val listSelected = listIsSelected(list)?: false
            if(listSelected && list.hasLabels()) return true
        }
        return false
    }

    fun setSelectState(inSelectState: Boolean) {
        this.inSelectState = inSelectState
        if(inSelectState) initSelectedListsMap()
    }

    fun inSelectState(): Boolean {
        return inSelectState
    }

    fun getLists(): ArrayList<List> {
        return lists
    }

    private fun initSelectedListsMap() {
        selectedCount = 0
        listSelectedMap.clear()
        for(list in lists)
            listSelectedMap[list] = false
    }

    fun listIsSelected(list: List): Boolean? {
        return listSelectedMap[list]
    }

    fun toggleSelectedList(list: List): Boolean? {
        val listIsSelected = listIsSelected(list)?: return null
        listSelectedMap[list] = !listIsSelected
        if(!listIsSelected) selectedCount++ else selectedCount--
        return listSelectedMap[list]
    }

    fun getSelectedCount(): Int {
        return selectedCount
    }

    fun addList(list: List) {
        lists.add(list)
    }
}