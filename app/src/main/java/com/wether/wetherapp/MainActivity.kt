package com.wether.wetherapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.wether.wetherapp.data.model.Condition
import com.wether.wetherapp.data.model.WeatherModel
import com.wether.wetherapp.screens.DialogSearch
import com.wether.wetherapp.screens.MainCard
import com.wether.wetherapp.screens.TabLayout
import com.wether.wetherapp.ui.theme.WetherAppTheme
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WetherAppTheme {
                val daysList = remember {
                    mutableStateOf(listOf<WeatherModel>())
                }
                val dialogState = remember {
                    mutableStateOf(false)
                }
                val currentDay = remember {
                    mutableStateOf(
                        WeatherModel(
                            city = "",
                            time = "",
                            current = "",
                            condition = Condition(
                                text = "",
                                icon = ""
                            ),
                            maxTemp = "",
                            minTemp = "",
                            hours = ""
                        )
                    )
                }
                if (dialogState.value) {
                    DialogSearch(dialogState = dialogState, onSubmit = {
                        getData(
                            city = it,
                            context = this,
                            daysList = daysList,
                            currentDay = currentDay
                        )
                    })
                }
                getData(
                    city = "Rostov-on-Don",
                    context = this,
                    daysList = daysList,
                    currentDay = currentDay
                )
                Image(
                    painter = painterResource(id = R.drawable.blue_sky),
                    contentDescription = "im1",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.5f),
                    contentScale = ContentScale.FillBounds
                )
                Column {
                    MainCard(currentDay = currentDay, onClickSinc = {
                        getData(
                            city = "Rostov-on-Don",
                            context = this@MainActivity,
                            daysList = daysList,
                            currentDay = currentDay
                        )
                    }, onClickSearch = {
                        dialogState.value = true
                    })
                    TabLayout(daysList = daysList, currentDay = currentDay)
                }

            }
        }
    }
}

const val API_KEY = "746be40c5e02489ba9a163047231210"
private fun getData(
    city: String,
    context: Context,
    daysList: MutableState<List<WeatherModel>>,
    currentDay: MutableState<WeatherModel>
) {
    val url = "https://api.weatherapi.com/v1/forecast.json" +
            "?key=$API_KEY" +
            "&q=$city" +
            "&days=" +
            "3" +
            "&api=no&alerts=no"
    val queue = Volley.newRequestQueue(context)
    val stringRequest = StringRequest(
        Request.Method.GET,
        url,
        { response ->
            val list = getWeatherByDays(response)
            currentDay.value = list[0]
            daysList.value = list
        },
        { error ->
            Log.d("MyLog", "Error $error")
        }
    )
    queue.add(stringRequest)
}

private fun getWeatherByDays(response: String): List<WeatherModel> {
    if (response.isEmpty()) return listOf()
    val list = arrayListOf<WeatherModel>()
    val mainObject = JSONObject(response)
    val city = mainObject.getJSONObject("location").getString("name")
    val days = mainObject.getJSONObject("forecast").getJSONArray("forecastday")
    for (i in 0 until days.length()) {
        val item = days[i] as JSONObject
        list.add(
            WeatherModel(
                city = city,
                time = item.getString("date"),
                current = "",
                condition = Condition(
                    text = item.getJSONObject("day").getJSONObject("condition").getString("text"),
                    icon = item.getJSONObject("day").getJSONObject("condition").getString("icon")
                ),
                maxTemp = item.getJSONObject("day").getString("maxtemp_c"),
                minTemp = item.getJSONObject("day").getString("mintemp_c"),
                hours = item.getJSONArray("hour").toString()
            )
        )
    }
    list[0] = list[0].copy(
        time = mainObject.getJSONObject("current").getString("last_updated"),
        current = mainObject.getJSONObject("current").getString("temp_c")
    )
    return list
}
