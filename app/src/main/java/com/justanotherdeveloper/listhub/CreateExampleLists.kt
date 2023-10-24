package com.justanotherdeveloper.listhub

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CreateExampleLists(private val database: ListsDatabase) {

    private val context = database.context

    private var omeletRecipeListId = 0
    private var porridgeRecipeListId = 0

    private var morningListId = 0
    private var bedtimeListId = 0
    private var generalListId = 0
    private var eventsListId = 0
    private var cookingListId = 0
    private var groceriesListId = 0
    private var shoppingListId = 0
    private var remindersListId = 0

    init { createExampleLists() }

    private fun createExampleLists() {
        createExampleRoutineLists()
        createExampleBulletedLists()
        createExampleToDoLists()
        createExampleSectionsLists()
        markImportantLists()
    }

    private fun markImportantLists() {
        val importantListIds = ArrayList<Int>()
        importantListIds.add(remindersListId)
        importantListIds.add(shoppingListId)
        importantListIds.add(groceriesListId)
        importantListIds.add(cookingListId)
        importantListIds.add(eventsListId)
        importantListIds.add(generalListId)
        importantListIds.add(bedtimeListId)
        importantListIds.add(morningListId)

        for(listId in importantListIds) {
            database.toggleStarOfList(database.getList(listId))
            database.addListToFavorites(listId)
        }
    }

    private fun createExampleRoutineLists() {
        createExamplePorridgeRecipe()
        createExampleOmeletRecipe()
        createExampleMorningRoutine()
        createExampleBedtimeRoutine()
    }

    private fun createExampleBulletedLists() {
        createExampleRemindersBulletpoints()
        createExampleRecipesBulletpoints()
    }

    private fun createExampleToDoLists() {
        createExampleGroceryList()
        createExampleCookingList()
        createExampleGeneralList()
    }

    private fun createExampleSectionsLists() {
        createExampleSBABusinessPlanChecklist()
        createExampleShoppingList()
        createExampleEventsList()
    }

    private fun addLabelsToList(listId: Int, labels: ArrayList<String>) {
        val checkedLabels = HashMap<String, Boolean>()
        for(label in labels) checkedLabels[label] = true
        database.addAndRemoveLabels(database.getList(listId), labels, checkedLabels)
    }

    private fun getListTypeString(listType: String): String {
        val stringCode = when(listType) {
            toDoListRef -> R.string.toDoListString
            progressListRef -> R.string.progressListString
            routineListRef -> R.string.routineListString
            else -> R.string.bulletedListString
        }
        return context.getString(stringCode)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun getLabels(listType: String, isExample: Boolean = false): ArrayList<String> {
        val labels = ArrayList<String>()
        val exampleString = context.getString(R.string.exampleString)
        // if(isExample)
            labels.add(exampleString)
        labels.add(getListTypeString(listType))
        return labels
    }

    private fun createExampleGroceryList() {
        groceriesListId = createList(R.string.groceryListTitle,
            R.string.groceryListTasks,
            4, getLabels(progressListRef, true), progressListRef)
    }

    private fun createExampleGeneralList() {
        generalListId = createList(R.string.generalListTitle,
            R.string.generalListTasks,
            3, getLabels(progressListRef), progressListRef)
    }

    private fun createExampleCookingList() {
        val listId = createList(R.string.cookingListTitle,
            R.string.cookingListTasks,
            4, getLabels(progressListRef, true), progressListRef)
        cookingListId = listId
        val cookingListTasks =
            context.getString(
            R.string.cookingListTasks).split("\n")
        val buyOmeletteIngredientsString = cookingListTasks[0]
        val cookOmeletteString = cookingListTasks[1]
        val buyPorridgeIngredientsString = cookingListTasks[2]
        val cookPorridgeString = cookingListTasks[3]

        val nextWeek = getTodaysDate()
        nextWeek.add(Calendar.DATE, 7)
        val nextWeekFromYesterday = getYesterdaysDate()
        nextWeekFromYesterday.add(Calendar.DATE, 7)

        val cookingList = database.getProgressList(listId)
        for(taskId in cookingList.getCurrentTasks()) {
            val task = cookingList.getTask(taskId)
            if(task != null) {
                when(task.getTask()) {
                    buyOmeletteIngredientsString -> {
                        task.linkToList(groceriesListId)
                        task.setDate(nextWeekFromYesterday)
                    }
                    cookOmeletteString -> {
                        task.linkToList(omeletRecipeListId)
                        task.setDate(nextWeekFromYesterday)
                    }
                    buyPorridgeIngredientsString -> {
                        task.linkToList(groceriesListId)
                        task.setDate(nextWeek)
                    }
                    cookPorridgeString -> {
                        task.linkToList(porridgeRecipeListId)
                        task.setDate(nextWeek)
                    }
                }
            }
        }
        database.updateProgressList(cookingList)
    }

    private fun createList(listTitleStringCode: Int, listTasksStringCode: Int,
                           colorThemeIndex: Int, labels: ArrayList<String>,
                           listType: String): Int {
        val listId = database.quickCreateList(listType, colorThemeIndex,
            context.getString(listTitleStringCode))
        addLabelsToList(listId, labels)
        val taskStringsCombined = context.getString(listTasksStringCode)
        if(taskStringsCombined.isEmpty()) return listId
        var taskStrings = taskStringsCombined.split("\n")
        if(listType != routineListRef) taskStrings = taskStrings.asReversed()
        for(taskString in taskStrings)
            database.quickAddTask(taskString, listId)
        return listId
    }

    private fun createSectionsList(listTitleStringCode: Int, listSectionsStringCode: Int,
                                   colorThemeIndex: Int, labels: ArrayList<String>): Int {
        val listId = database.quickCreateList(progressListRef, colorThemeIndex,
            context.getString(listTitleStringCode))
        addLabelsToList(listId, labels)
        val listSections = context.getString(
            listSectionsStringCode).split("\n")
        val sectionsList = database.getProgressList(listId)
        for((i, sectionTitle) in listSections.withIndex()) {
            when(i) {
                0 -> sectionsList.renameCompletedList(sectionTitle)
                1 -> sectionsList.renameInProgressList(sectionTitle)
                else -> sectionsList.addListSection(sectionTitle)
            }
        }
        database.updateProgressList(sectionsList)
        return listId
    }

    private fun createExampleShoppingList() {
        shoppingListId = createSectionsList(R.string.shoppingListTitle,
            R.string.shoppingListSections,
            4, getLabels(progressListRef))
    }

    private fun getSectionsListTasks(sectionTasksStringCode: Int): ArrayList<ArrayList<String>> {
        val sectionsListTasks = ArrayList<ArrayList<String>>()
        val sectionsListTasksStrings = context.getString(
            sectionTasksStringCode).split("\t")
        for(sectionTasksString in sectionsListTasksStrings) {
            val sectionTasks = sectionTasksString.split("\n")
            val sectionTasksAsArray = ArrayList<String>()
            for(task in sectionTasks) sectionTasksAsArray.add(task)
            sectionsListTasks.add(sectionTasksAsArray)
        }
        return sectionsListTasks
    }

    private fun createExampleEventsList() {
        val listId = createSectionsList(R.string.eventsListTitle,
            R.string.eventsListSections,
            1, getLabels(progressListRef, true))
        eventsListId = listId

        var sectionsList = database.getProgressList(listId)
        database.updateProgressList(sectionsList)

        val eventsListTasks =
            getSectionsListTasks(R.string.eventsListTasks)

        val weddingShower = eventsListTasks[0][0]
        val birthday = eventsListTasks[0][1]
        val christmas = eventsListTasks[1][0]
        val newYears = eventsListTasks[1][1]

        database.quickAddTask(birthday, listId)
        database.quickAddTask(weddingShower, listId)

        moveInProgressTasksToOtherSection(listId, 0)

        database.quickAddTask(newYears, listId)
        database.quickAddTask(christmas, listId)

        sectionsList = database.getProgressList(listId)
        sectionsList.setSortIndex(sortDueDateDescendingIndex)
        sectionsList.toggleSectionVisibility(0)
        sectionsList.toggleInProgressVisibility()

        for(taskId in sectionsList.taskIds) {
            val task = sectionsList.getTask(taskId)
            if(task != null) {
                val year = getTodaysDate().get(Calendar.YEAR)
                val date = when(task.getTask()) {
                    weddingShower -> createCalendar(year, 10, 28)
                    birthday -> createCalendar(year, 8, 7)
                    christmas -> createCalendar(year, 11, 25)
                    newYears -> createCalendar(year, 0, 1)
                    else -> null
                }
                if(date != null) {
                    if (getTodaysDate().comesAfter(date))
                        date.set(Calendar.YEAR, year + 1)
                    task.setDate(date)
                }
            }
        }

        database.updateProgressList(sectionsList)
    }

    private fun moveInProgressTasksToOtherSection(listId: Int, sectionIndex: Int) {
        val sectionsList = database.getProgressList(listId)
        val sectionToMoveTo = sectionsList.getListSectionTitle(sectionIndex)
        val tasksToMove = ArrayList<Int>()
        for(taskId in sectionsList.getCurrentTasks())
            tasksToMove.add(taskId)
        for(taskId in tasksToMove.asReversed()) {
            val task = sectionsList.getTask(taskId)
            if(task != null) sectionsList.setTaskForOtherSection(task, sectionToMoveTo)
        }
        database.updateProgressList(sectionsList)
    }

    private fun createExampleSBABusinessPlanChecklist() {
        val listId = createSectionsList(R.string.businessPlanListTitle,
            R.string.businessPlanListSections,
            1, getLabels(progressListRef, true))
        val businessPlanListTasks =
            getSectionsListTasks(R.string.businessPlanListTasks)

        val businessPlanList = database.getProgressList(listId)
        businessPlanList.setNote(context.getString(R.string.businessPlanListNote))
        database.updateProgressList(businessPlanList)

        for(i in 0 until businessPlanListTasks.size) {
            for (taskString in businessPlanListTasks[i].asReversed())
                database.quickAddTask(taskString, listId)
            moveInProgressTasksToOtherSection(listId, i)
        }
    }

    private fun createExampleMorningRoutine() {
        val listId = createList(R.string.morningRoutineTitle,
            R.string.morningRoutineSteps,
            3, getLabels(routineListRef), routineListRef)
        morningListId = listId
        val morningRoutineSteps =
            context.getString(
                R.string.morningRoutineSteps).split("\n")
        val step1 = morningRoutineSteps[0]
        val step2 = morningRoutineSteps[1]
        val step3 = morningRoutineSteps[2]
        val step4 = morningRoutineSteps[3]
        val step5 = morningRoutineSteps[4]
        val step6 = morningRoutineSteps[5]

        val morningRoutine = database.getRoutineList(listId)
        morningRoutine.setRepeatingDays(getRepeatingWeekdays())
        for(taskId in morningRoutine.getCurrentTasks()) {
            val task = morningRoutine.getTask(taskId)
            if (task != null) {
                when (task.getTask()) {
                    step1 -> task.setTime(6, 0)
                    step2 -> task.setTime(6, 10)
                    step3 -> task.setTime(6, 30)
                    step4 -> task.setTime(7, 0)
                    step5 -> task.setTime(7, 30)
                    step6 -> task.setTime(8, 0)
                }
            }
        }
        database.updateRoutineList(morningRoutine)
    }

    private fun createExampleBedtimeRoutine() {
        val listId = createList(R.string.bedtimeRoutineTitle,
            R.string.bedtimeRoutineSteps,
            3, getLabels(routineListRef), routineListRef)
        bedtimeListId = listId
        val bedtimeRoutineSteps =
            context.getString(
                R.string.bedtimeRoutineSteps).split("\n")
        val step1 = bedtimeRoutineSteps[0]
        val step2 = bedtimeRoutineSteps[1]
        val step3 = bedtimeRoutineSteps[2]
        val step4 = bedtimeRoutineSteps[3]
        val step5 = bedtimeRoutineSteps[4]
        val step6 = bedtimeRoutineSteps[5]
        val step7 = bedtimeRoutineSteps[6]

        val bedtimeRoutine = database.getRoutineList(listId)
        bedtimeRoutine.setRepeatingDays(getRepeatingWeekdays())
        for(taskId in bedtimeRoutine.getCurrentTasks()) {
            val task = bedtimeRoutine.getTask(taskId)
            if (task != null) {
                when (task.getTask()) {
                    step1 -> task.setTime(18, 0)
                    step2 -> task.setTime(19, 0)
                    step3 -> task.setTime(19, 30)
                    step4 -> task.setTime(20, 0)
                    step5 -> task.setTime(20, 30)
                    step6 -> task.setTime(20, 50)
                    step7 -> task.setTime(21, 0)
                }
            }
        }
        database.updateRoutineList(bedtimeRoutine)
    }

    private fun createExampleOmeletRecipe() {
        omeletRecipeListId = createList(R.string.omeletRecipeTitle,
            R.string.omeletRecipeSteps,
            4, getLabels(routineListRef, true), routineListRef)
        val omeletteRecipe = database.getRoutineList(omeletRecipeListId)
        omeletteRecipe.setNote(context.getString(R.string.omeletRecipeNote))
        database.updateRoutineList(omeletteRecipe)
    }

    private fun createExamplePorridgeRecipe() {
        porridgeRecipeListId = createList(
            R.string.porridgeRecipeTitle,
            R.string.porridgeRecipeSteps,
            4, getLabels(routineListRef, true), routineListRef)
        val porridgeRecipe = database.getRoutineList(porridgeRecipeListId)
        porridgeRecipe.setNote(context.getString(R.string.porridgeRecipeNote))
        database.updateRoutineList(porridgeRecipe)
    }

    private fun createExampleRecipesBulletpoints() {
        val listId = createList(R.string.recipesListTitle,
            R.string.recipesListItems,
            4, getLabels(bulletedListRef, true), bulletedListRef)

        val recipesListItems = context.getString(
            R.string.recipesListItems).split("\n")
        val omeletteString = recipesListItems[0]
        val porridgeString = recipesListItems[1]

        val recipesList = database.getBulletedList(listId)
        for(taskId in recipesList.getCurrentTasks()) {
            val task = recipesList.getTask(taskId)
            if(task != null) {
                val taskString = task.getTask()
                if(taskString == porridgeString) {
                    task.linkToList(porridgeRecipeListId)
                    task.setWebsiteLink(porridgeRecipeSource)
                } else if(taskString == omeletteString) {
                    task.linkToList(omeletRecipeListId)
                    task.setWebsiteLink(omeletteRecipeSource)
                }
            }
        }
        database.updateBulletedList(recipesList)
    }

    private fun getRepeatingWeekdays(): ArrayList<Int> {
        val repeatingDays = getRepeatingEveryday()
        repeatingDays.remove(Calendar.SUNDAY)
        repeatingDays.remove(Calendar.SATURDAY)
        return repeatingDays
    }

    private fun getRepeatingEveryday(): ArrayList<Int> {
        val repeatingDays = java.util.ArrayList<Int>()
        repeatingDays.add(Calendar.SUNDAY)
        repeatingDays.add(Calendar.MONDAY)
        repeatingDays.add(Calendar.TUESDAY)
        repeatingDays.add(Calendar.WEDNESDAY)
        repeatingDays.add(Calendar.THURSDAY)
        repeatingDays.add(Calendar.FRIDAY)
        repeatingDays.add(Calendar.SATURDAY)
        return repeatingDays
    }

    private fun createExampleRemindersBulletpoints() {
        val listId = createList(R.string.remindersListTitle,
            R.string.remindersListItems,
            2, getLabels(bulletedListRef), bulletedListRef)
        remindersListId = listId

        val reminders = database.getBulletedList(listId)
        for(taskId in reminders.getCurrentTasks())
            reminders.getTask(taskId)?.setRepeatingDays(
                getRepeatingEveryday())
        database.updateBulletedList(reminders)
    }
}