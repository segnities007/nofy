package com.segnities007.nofy.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation3.scene.Scene
import androidx.navigationevent.NavigationEvent

private const val NavigationTransitionDurationMillis = 220

/** Navigation3 シーン切替のフェードイン／アウト（約 220ms）。 */
internal fun <T : Any> nofyTransitionSpec():
    AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    ContentTransform(
        targetContentEnter = fadeIn(
            animationSpec = tween(NavigationTransitionDurationMillis)
        ),
        initialContentExit = fadeOut(
            animationSpec = tween(NavigationTransitionDurationMillis)
        )
    )
}

/** 予測バック（スワイス）時も通常遷移と同じトランジションを使う。 */
internal fun <T : Any> nofyPredictivePopTransitionSpec():
    AnimatedContentTransitionScope<Scene<T>>.(
        @NavigationEvent.SwipeEdge Int
    ) -> ContentTransform = {
    nofyTransitionSpec<T>().invoke(this)
}
