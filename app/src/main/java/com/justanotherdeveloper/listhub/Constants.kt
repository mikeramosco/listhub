package com.justanotherdeveloper.listhub

// Scroll View Content Transition Delay
const val TRANSITION_DELAY: Long = 140

// Delay time for dialog to cancel
const val DELAY_UNTIL_DIALOG_CANCELLED: Long = 200

// Search page delay
const val SEARCH_DELAY: Long = 250

// Start app delay
const val LOGO_FADE_DELAY: Long = 100
const val SPLASH_SCREEN_DELAY: Long = 400

// Animation Fade Duration
const val FADE_IN_DURATION = 100
const val FADE_OUT_DURATION = 200

// Number of items to show on first load
const val ITEM_COUNT_FIRST_LOAD = 15
const val ITEM_COUNT_PER_INTERVAL = 20

// Special
const val SENTINEL = -1

// Request Codes
const val OPEN_LIST_CODE = 0
const val REORDER_PAGE_CODE = 1
const val SEARCH_PAGE_CODE = 2

// Max lengths
const val MAX_TEXT_DISPLAY_LENGTH = 30

// Min & Max possible IDs
const val START_RANDOM_ID = 100000
const val END_RANDOM_ID = 999999

// Example List Source Links
const val porridgeRecipeSource = "https://www.bbcgoodfood.com/recipes/perfect-porridge"
const val omeletteRecipeSource = "https://www.acouplecooks.com/omelette-recipe"

// File database references
const val IDS_FILENAME = "idsFile"
const val LISTS_SORT_INDEX = "listsSortIndex"
const val LISTS_FILTERS = "listsFilters"
const val LISTS_DETAILS_SHOWN = "listsDetailsShown"
const val HOME_DETAILS_SHOWN = "homeDetailsShown"
const val EXAMPLE_LISTS_ADDED = "exampleListsAdded"

// File database date range references
const val DATE_RANGE_CALENDAR = "calendarDateRange"
const val DATE_RANGE_IMPORTANT = "importantDateRange"
const val DATE_RANGE_RECENTLY_ADDED = "recentlyAddedDateRange"
const val DATE_RANGE_REWARDS = "rewardsDateRange"
const val DATE_RANGE_COMPLETED = "completedDateRange"

// File database home section filter references
const val FILTER_CALENDAR = "calendarFilter"
const val FILTER_IMPORTANT = "importantFilter"
const val FILTER_RECENTLY_ADDED = "recentlyAddedFilter"
const val FILTER_REWARDS = "rewardsFilter"
const val FILTER_COMPLETED = "completedFilter"

// List Type Reference
const val archivedRef = "Archived"
const val favoritesRef = "Favorites"
const val toDoListRef = "ToDoList"
const val progressListRef = "ProgressList"
const val routineListRef = "RoutineList"
const val bulletedListRef = "BulletedList"

// Lists to Reorder Reference
const val reorderTasksRef = "reorderTasks"
const val reorderCompletedRef = "reorderCompleted"

// Intent Extra Reference
const val listTypeRef = "listType"
const val listIdRef = "listId"
const val listToReorderRef = "listToReorder"
const val listReorderedRef = "listReordered"
const val orderReversedRef = "orderReversed"
const val taskToOpenRef = "taskToOpen"

// Sort Indexes
const val sortAToZIndex = 1
const val sortZToAIndex = 2
const val sortNewestFirstIndex = 3
const val sortOldestFirstIndex = 4
const val sortDueDateDescendingIndex = 5
const val sortDueDateAscendingIndex = 6
const val sortLastUpdatedDescendingIndex = 7
const val sortLastUpdatedAscendingIndex = 8

// Home Section Indexes
const val calendarIndex = 0
const val importantIndex = 1
const val recentlyAddedIndex = 2
const val rewardsIndex = 3
const val completedIndex = 4