package com.segnities007.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * Shared spacing rhythm for screens, cards, and forms (macro + micro).
 */
object NofySpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp

    /** Horizontal gutter for scrollable body / card interior (matches floating bar content inset). */
    val screenEdgeGutter = 20.dp

    /** Material minimum touch target width (toolbar balance, icon buttons). */
    val minTouchTarget = 48.dp

    /** Vertical gap between password fields / primary form controls. */
    val formFieldGap = xl

    /** Between full-width buttons stacked in a column. */
    val stackedButtonGap = sm

    /** Space between lazy list / card sections. */
    val sectionCardGap = lg

    /** Logo + title + subtitle stack in auth / marketing hero (same dp as [NofyCornerRadius.small]). */
    val logoBlockVerticalSpacing = NofyCornerRadius.small

    /** Inner vertical padding for [com.segnities007.designsystem.atom.floatingbar.NofyFloatingBar] rows. */
    val floatingBarInnerVertical = sm

    /** Inner horizontal padding for top floating bar row content. */
    val floatingBarTopRowPaddingHorizontal = lg

    /** Inner horizontal padding for bottom floating bar (icon strip). */
    val floatingBarBottomRowPaddingHorizontal = sm

    /** Drop shadow elevation for top floating bar (visual only). */
    val floatingBarTopShadowElevation = 6.dp

    /** Height of bottom edge fade strip in [com.segnities007.designsystem.template.NofyBrushedFloatingBarScreen]. */
    val brushedBottomFadeHeight = 144.dp

    /** Standard padding around previews in @Preview / @NofyPreview (equals [xl]). */
    val previewCanvasPadding = xl

    /** Hairline border width for cards / floating bars. */
    val hairlineWidth = 1.dp

    /** Camera / live scanner region (vault send, etc.). */
    val qrScannerSlotHeight = 320.dp

    /** Static QR bitmap region (vault receive, etc.). */
    val qrCodeSlotHeight = 280.dp

    /** Default logo mark size in auth / marketing blocks. */
    val logoMarkSize = 120.dp

    /** Primary filled button min height ([com.segnities007.designsystem.atom.button.NofyButton]). */
    val primaryButtonMinHeight = 58.dp

    /** Min height of top floating bar row ([com.segnities007.designsystem.atom.floatingbar.NofyFloatingBarDefaults]). */
    val floatingBarTopMinHeight = 72.dp

    /** Min height of bottom floating bar row. */
    val floatingBarBottomMinHeight = 84.dp
}
