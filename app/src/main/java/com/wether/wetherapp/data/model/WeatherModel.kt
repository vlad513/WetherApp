package com.wether.wetherapp.data.model

data class WeatherModel (
    val city:String,
    val time:String,
    val current :String,
    val condition:Condition,
    val maxTemp:String,
    val minTemp:String,
    val hours:String
)

data class Condition(
    val text:String,
    val icon:String
)