package com.segnities007.setting.presentation.navigation

import android.content.Context
import android.content.Intent
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

internal fun openOpenSourceLicenses(
    context: Context,
    title: String
) {
    OssLicensesMenuActivity.setActivityTitle(title)
    context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
}
