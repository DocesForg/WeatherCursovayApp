package com.docesforg.bura.common

import java.util.Locale

fun String.capitalize(locale: Locale): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }