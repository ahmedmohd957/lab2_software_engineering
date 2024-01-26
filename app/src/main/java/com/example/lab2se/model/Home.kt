package com.example.lab2se.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Home(
    var light: Int? = null,
    var door: Int? = null,
    var window: Int? = null,
)