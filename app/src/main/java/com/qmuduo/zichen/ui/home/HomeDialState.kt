package com.qmuduo.zichen.ui.home

data class DialRenderState(
    val actualIndex: Int,
    val displayIndex: Int,
    val isPreviewing: Boolean,
)

data class HomeDialState(val previewedIndex: Int? = null) {
    init {
        require(previewedIndex == null || previewedIndex in 0..11) {
            "previewedIndex must be null or in 0..11"
        }
    }

    fun select(index: Int): HomeDialState {
        require(index in 0..11) { "index must be in 0..11" }
        return copy(previewedIndex = index)
    }

    fun returnToCurrent(): HomeDialState = copy(previewedIndex = null)

    fun renderFor(currentIndex: Int): DialRenderState {
        require(currentIndex in 0..11) { "currentIndex must be in 0..11" }
        return DialRenderState(
            actualIndex = currentIndex,
            displayIndex = previewedIndex ?: currentIndex,
            isPreviewing = previewedIndex != null,
        )
    }
}
