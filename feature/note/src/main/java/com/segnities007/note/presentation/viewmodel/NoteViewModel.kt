package com.segnities007.note.presentation.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.segnities007.auth.domain.repository.AuthRepository
import com.segnities007.note.R
import com.segnities007.note.domain.error.NoteRepositoryException
import com.segnities007.note.domain.model.Note
import com.segnities007.note.domain.repository.NoteRepository
import com.segnities007.note.presentation.contract.NoteEffect
import com.segnities007.note.presentation.contract.NoteIntent
import com.segnities007.note.presentation.contract.NoteState
import com.segnities007.note.presentation.state.NotePageUiState
import com.segnities007.note.presentation.state.buildPagesAfterRemoval
import com.segnities007.note.presentation.state.ensureTrailingBlankPage
import com.segnities007.note.presentation.state.findPage
import com.segnities007.note.presentation.state.replacePage
import com.segnities007.note.presentation.state.resolvePageChange
import com.segnities007.note.presentation.state.resolveCurrentPageIndexAfterRemoval
import com.segnities007.note.presentation.state.toNotePages
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NoteViewModel(
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteState())
    val uiState = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<NoteEffect>(extraBufferCapacity = 1)
    val effect = _effect.asSharedFlow()

    private val saveJobs = mutableMapOf<String, Job>()

    init {
        observeLockState()
        loadNotes()
    }

    fun onIntent(intent: NoteIntent) {
        when (intent) {
            is NoteIntent.EditContent -> editContent(intent.pageId, intent.content)
            is NoteIntent.PageChanged -> updateCurrentPageIndex(intent.index)
            is NoteIntent.DeletePage -> deletePage(intent.pageId)
            NoteIntent.Reload -> loadNotes()
            NoteIntent.TogglePreview -> togglePreview()
            NoteIntent.Lock -> lock()
            NoteIntent.NavigateToSettings -> navigateToSettings()
            NoteIntent.NavigateToNextPage -> navigateToPage(_uiState.value.currentPageIndex + 1)
            NoteIntent.NavigateToPreviousPage -> navigateToPage(_uiState.value.currentPageIndex - 1)
        }
    }

    private fun loadNotes() {
        viewModelScope.launch {
            startNotesLoad()
            val result = fetchNotePages()
            reduceNotesLoad(result)
        }
    }

    private fun editContent(pageId: String, content: String) {
        val page = _uiState.value.pages.findPage(pageId) ?: return
        if (page.content == content) return

        applyEditedContent(pageId = pageId, page = page, content = content)

        if (content.isBlank()) {
            cancelPendingSave(pageId)
            return
        }

        scheduleSave(pageId)
    }

    private fun scheduleSave(pageId: String) {
        cancelPendingSave(pageId)
        saveJobs[pageId] = viewModelScope.launch {
            delay(SaveDelayMillis)
            val page = findSavablePage(pageId) ?: return@launch
            persistPage(pageId, page)
        }
    }

    private fun deletePage(pageId: String, silent: Boolean = false) {
        val page = findDeletablePage(pageId) ?: return

        cancelPendingSave(pageId)
        removePageFromState(pageId)
        launchDeletionPersistence(page, silent)
    }

    private fun removePageFromState(pageId: String): Int {
        val state = _uiState.value
        val removedIndex = state.pages.indexOfFirst { it.pageId == pageId }
        if (removedIndex == -1) return state.currentPageIndex

        val pages = buildPagesAfterRemoval(state.pages, pageId)
        val newIndex = resolveCurrentPageIndexAfterRemoval(state, removedIndex, pages)

        _uiState.update {
            it.copy(
                pages = pages,
                currentPageIndex = newIndex
            )
        }

        return newIndex
    }

    private fun lock() {
        viewModelScope.launch {
            val result = authRepository.lock()
            reduceLock(result)
        }
    }

    private suspend fun fetchNotePages(): Result<List<NotePageUiState>> {
        return runCatching { noteRepository.getNotes().first() }
            .mapCatching { notes -> notes.toNotePages() }
    }

    private fun startNotesLoad() {
        _uiState.update { it.copy(isLoading = true, error = null) }
    }

    private fun reduceNotesLoad(result: Result<List<NotePageUiState>>) {
        val pages = result.getOrNull()
        if (pages != null) {
            showLoadedPages(pages)
            return
        }

        handleLoadFailure(
            error = result.exceptionOrNull() ?: IllegalStateException("Unknown note load failure")
        )
    }

    private fun showLoadedPages(pages: List<NotePageUiState>) {
        _uiState.update { state ->
            state.copy(
                pages = pages,
                currentPageIndex = state.currentPageIndex.coerceIn(0, pages.lastIndex),
                isLoading = false
            )
        }
    }

    private fun handleLoadFailure(error: Throwable) {
        if (error is NoteRepositoryException.UntrustedEnvironment) {
            handleUntrustedEnvironment()
            return
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                error = error.message
            )
        }
        emitError(R.string.note_toast_load_failed)
    }

    private fun applyEditedContent(
        pageId: String,
        page: NotePageUiState,
        content: String
    ) {
        val updatedPage = page.copy(
            content = content,
            updatedAt = System.currentTimeMillis()
        )

        _uiState.update { state ->
            state.copy(
                pages = replacePage(state.pages, pageId, updatedPage),
                error = null
            )
        }
    }

    private fun cancelPendingSave(pageId: String) {
        saveJobs.remove(pageId)?.cancel()
    }

    private fun findSavablePage(pageId: String): NotePageUiState? {
        return _uiState.value.pages.findPage(pageId)?.takeIf { it.content.isNotBlank() }
    }

    private suspend fun persistPage(
        pageId: String,
        page: NotePageUiState
    ) {
        val result = noteRepository.saveNote(page.toDomain())
        reducePageSave(pageId, result)
    }

    private suspend fun reducePageSave(
        pageId: String,
        result: Result<Note>
    ) {
        val savedNote = result.getOrNull()
        if (savedNote != null) {
            applySavedNote(pageId, savedNote)
            return
        }

        if (result.exceptionOrNull() is NoteRepositoryException.UntrustedEnvironment) {
            handleUntrustedEnvironment()
            return
        }

        emitToast(R.string.note_toast_save_failed)
    }

    private fun applySavedNote(pageId: String, savedNote: Note) {
        _uiState.update { state ->
            state.copy(
                pages = ensureTrailingBlankPage(
                    state.pages.map { current ->
                        if (current.pageId != pageId) {
                            current
                        } else {
                            current.copy(
                                noteId = savedNote.id,
                                createdAt = savedNote.createdAt,
                                updatedAt = savedNote.updatedAt
                            )
                        }
                    }
                )
            )
        }
    }

    private fun findDeletablePage(pageId: String): NotePageUiState? {
        val page = _uiState.value.pages.findPage(pageId) ?: return null
        if (page.noteId == null && page.content.isBlank()) return null
        return page
    }

    private fun launchDeletionPersistence(
        page: NotePageUiState,
        silent: Boolean
    ) {
        viewModelScope.launch {
            val result = deletePersistedPage(page)
            reduceDeletion(result, silent)
        }
    }

    private suspend fun deletePersistedPage(page: NotePageUiState): Result<Unit> {
        val noteId = page.noteId ?: return Result.success(Unit)
        return noteRepository.deleteNote(noteId)
    }

    private suspend fun reduceDeletion(
        result: Result<Unit>,
        silent: Boolean
    ) {
        if (result.isSuccess) {
            if (!silent) {
                emitEffect(NoteEffect.ShowToastRes(R.string.note_toast_page_deleted))
            }
            return
        }

        if (result.exceptionOrNull() is NoteRepositoryException.UntrustedEnvironment) {
            handleUntrustedEnvironment()
            return
        }

        emitToast(R.string.note_toast_delete_failed)
        loadNotes()
    }

    private fun updateCurrentPageIndex(index: Int) {
        val resolution = _uiState.value.resolvePageChange(index)
        val removedPage = resolution.removedPage

        if (removedPage != null) {
            cancelPendingSave(removedPage.pageId)
        }

        _uiState.value = resolution.state

        if (removedPage != null) {
            launchDeletionPersistence(
                page = removedPage,
                silent = true
            )
        }
    }

    private fun togglePreview() {
        _uiState.update { it.copy(isPreviewEnabled = !it.isPreviewEnabled) }
    }

    private fun navigateToSettings() {
        viewModelScope.launch {
            emitEffect(NoteEffect.NavigateToSettings)
        }
    }

    private fun navigateToPage(index: Int) {
        if (index !in 0.._uiState.value.pages.lastIndex) return

        updateCurrentPageIndex(index)
    }

    private suspend fun reduceLock(result: Result<Unit>) {
        if (result.isSuccess) {
            wipeInMemoryNotes()
            emitEffect(NoteEffect.NavigateToLogin)
            return
        }

        emitToast(R.string.note_toast_lock_failed)
    }

    private fun observeLockState() {
        viewModelScope.launch {
            authRepository.isLocked().collectLatest { isLocked ->
                if (isLocked) {
                    wipeInMemoryNotes()
                }
            }
        }
    }

    private fun handleUntrustedEnvironment() {
        viewModelScope.launch {
            authRepository.lock()
            wipeInMemoryNotes()
            emitToast(R.string.note_toast_untrusted_environment)
        }
    }

    private fun wipeInMemoryNotes() {
        saveJobs.values.forEach(Job::cancel)
        saveJobs.clear()
        _uiState.value = NoteState(isLoading = false)
    }

    private fun emitError(@StringRes messageRes: Int) {
        viewModelScope.launch {
            emitToast(messageRes)
        }
    }

    private suspend fun emitToast(@StringRes messageRes: Int) {
        emitEffect(NoteEffect.ShowToastRes(messageRes))
    }

    private suspend fun emitEffect(effect: NoteEffect) {
        _effect.emit(effect)
    }

    override fun onCleared() {
        wipeInMemoryNotes()
        super.onCleared()
    }

    private companion object {
        const val SaveDelayMillis = 350L
    }
}
