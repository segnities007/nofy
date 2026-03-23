package com.segnities007.note.presentation.state

import com.segnities007.note.domain.model.Note
import java.util.UUID

/**
 * ノート画面の 1 ページ分の UI 状態（永続化前のドラフトも同型で扱う）。
 */
data class NotePageUiState(
    /** ページャー内での安定 ID（未保存ドラフトは `draft-…`）。 */
    val pageId: String,

    /** Room に保存されたノートの ID。未保存なら `null`。 */
    val noteId: Long? = null,

    /** エディタ本文（Markdown）。 */
    val content: String = "",

    /** 作成時刻（エポック ms）。 */
    val createdAt: Long = System.currentTimeMillis(),

    /** 最終更新時刻（エポック ms）。 */
    val updatedAt: Long = System.currentTimeMillis()
) {
    /** 一覧・タブ用に本文先頭から抽出したタイトル。 */
    val title: String
        get() = extractNoteTitle(content)

    /** 本文が空のみか。 */
    val isBlank: Boolean
        get() = content.isBlank()

    /** ドメイン [Note] へ変換する（未保存は id=0）。 */
    fun toDomain(): Note {
        return Note(
            id = noteId ?: 0L,
            content = content,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun blank(pageId: String = newDraftPageId()): NotePageUiState = NotePageUiState(pageId = pageId)
    }
}

fun Note.toUiPage(pageId: String = "note-$id"): NotePageUiState {
    return NotePageUiState(
        pageId = pageId,
        noteId = id,
        content = content,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/** 未保存ページ用の一意な `pageId` 接頭辞付き文字列を生成する。 */
fun newDraftPageId(): String = "draft-${UUID.randomUUID()}"
