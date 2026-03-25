package com.segnities007.note.domain.model

/**
 * 永続化層とドメインで共有するノート 1 件。
 *
 * [id] が 0 のときは未採番（挿入直前の一時値）を表す。
 */
data class Note(
    /** Room の主キー。未保存は 0。 */
    val id: Long = 0,

    /** Markdown 本文。フィールド暗号化の対象。 */
    val content: String = "",

    /** 作成時刻（エポック ms）。 */
    val createdAt: Long = System.currentTimeMillis(),

    /** 最終更新時刻（エポック ms）。 */
    val updatedAt: Long = System.currentTimeMillis()
)
