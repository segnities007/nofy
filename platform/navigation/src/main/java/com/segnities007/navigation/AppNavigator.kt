package com.segnities007.navigation

import androidx.navigation3.runtime.NavKey

/**
 * Navigation3 のバックスタックに対する画面遷移（追加・置換・pop）の抽象。
 */
interface AppNavigator {
    /** バックスタックに [route] を push する。 */
    fun navigateTo(route: NavKey)

    /** 先頭を [route] で置き換える（スタック内容は実装依存）。 */
    fun replaceWith(route: NavKey)

    /** 現在の画面を pop する。 */
    fun pop()
}
