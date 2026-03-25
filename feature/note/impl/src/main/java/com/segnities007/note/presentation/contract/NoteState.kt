package com.segnities007.note.presentation.contract

import androidx.annotation.StringRes
import com.segnities007.note.presentation.state.NotePageUiState

/**
 * ノート画面で一度だけ消費するユーザー向けフィードバック。
 */
sealed interface NoteUserMessage {
    /** 文字列リソースで Toast を一度表示する。 */
    data class ToastRes(@param:StringRes val messageRes: Int) : NoteUserMessage
}

/**
 * ノート画面から設定など別フローへ遷移する一方向ナビ要求。
 */
sealed interface NoteNavigationRequest {
    /** 設定フローへ遷移する。 */
    data object ToSettings : NoteNavigationRequest

    /** ロック／未登録時など、ログインへ遷移する。 */
    data object ToLogin : NoteNavigationRequest
}

/** ノート画面が購読する単一の表示・操作状態。 */
data class NoteState(
    /** ページャー上の各ノート（末尾に空ドラフトを含みうる）。 */
    val pages: List<NotePageUiState> = listOf(NotePageUiState.blank()),

    /** [pages] のうち現在フォーカスしているインデックス。 */
    val currentPageIndex: Int = 0,

    /** Markdown プレビューをオーバーレイ表示するか。 */
    val isPreviewEnabled: Boolean = false,

    /** 初回ロードや再読込中。 */
    val isLoading: Boolean = true,

    /** 一覧取得失敗など、画面に出すエラー文言。成功時は null。 */
    val error: String? = null,

    /** 未消費のユーザー向けメッセージ。 */
    val pendingUserMessage: NoteUserMessage? = null,

    /** 未処理の画面外ナビ要求。 */
    val pendingNavigation: NoteNavigationRequest? = null
) {
    /** 現在ページ。範囲外なら `null`。 */
    val currentPage: NotePageUiState?
        get() = pages.getOrNull(currentPageIndex)
}
