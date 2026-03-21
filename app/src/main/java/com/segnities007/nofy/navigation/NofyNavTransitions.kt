package com.segnities007.nofy.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation3.scene.Scene
import androidx.navigationevent.NavigationEvent

private const val NavigationTransitionDurationMillis = 220

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

internal fun <T : Any> nofyPredictivePopTransitionSpec():
    AnimatedContentTransitionScope<Scene<T>>.(
        @NavigationEvent.SwipeEdge Int
    ) -> ContentTransform = {
    nofyTransitionSpec<T>().invoke(this)
}
