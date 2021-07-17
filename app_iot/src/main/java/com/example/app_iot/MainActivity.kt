package com.example.app_iot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    val database = Firebase.database
    val buzeerRef = database.getReference("buzeer")
//    val cdsRef = database.getReference("cds")
//    val dht11Ref = database.getReference("DHT")
    val doorRef = database.getReference("door")
    val ledRef = database.getReference("led")
    val logRef = database.getReference("log")
//    val faceRef = database.getReference("face")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        intRef()
        // 自動按下 btn_message
        openWeatherOnClick(btn_message)
    }

    fun warningOnClick(view: View){
        buzeerRef.setValue(1)
    }

    fun redLedOnClick(view: View){
        ledRef.setValue(1)
    }

    fun greenLedOnClick(view: View){
        ledRef.setValue(2)
    }

    fun yellowLedOnClick(view: View){
        ledRef.setValue(3)
    }

    fun doorOnClick(view: View){
        doorRef.setValue(1)
    }

    fun openWeatherOnClick(view: View){
        GlobalScope.launch {
            val client = OkHttpClient()
            val area = "new taipei"
            val country = "TW"
            val key = resources.getString(R.string.open_weather_key2)
            var base_url = resources.getString(R.string.open_weather_base_url)
            base_url = String.format(base_url, area, country, key)
//            Log.d("MainActivity", base_url)
            val request = Request.Builder()
                .url(base_url)
                .build()
            client.newCall(request).enqueue(object  :Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("MainActivity", e.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val json = response.body?.string()
                    Log.d("MainActivity", json.toString())
                    val gson = Gson()
                    val root = JsonParser.parseString(json).asJsonObject
                    val weather = root.getAsJsonArray("weather")[0].asJsonObject
                    val main = root.getAsJsonObject("main")
                    var icon_url = resources.getString(R.string.open_weather_icon_url)
                    icon_url = String.format(icon_url, weather.get("icon").asString)
                    Log.d("MainActivity", icon_url)
                    // UI 設置
                    runOnUiThread {
                        Picasso.get().load(icon_url).into(ib_icon)
                        btn_message.text = weather.get("description").asString
                        tv_temp.text = String.format("%.1f °C", main.get("temp").asDouble - 273.15)
                        tv_temp_feel.text = String.format("%.1f °C", main.get("feels_like").asDouble - 273.15)
                        tv_humi.text = main.get("humidity").toString() + " %"
                    }
                }

            })
        }
    }

    fun buttinState(state: Int){
        if ((state and 16) == 16){
            ib_warning.setImageResource(R.drawable.buzzer_on)
        }
        else{
            ib_warning.setImageResource(R.drawable.buzzer_off)
        }

        if ((state and 4) == 4){
            ib_door.setImageResource(R.drawable.door_open)
        }
        else{
            ib_door.setImageResource(R.drawable.door_close)
        }
        if((state and 3) == 3){
            ib_redLed.setImageResource(R.drawable.red_ball)
            ib_greenLed.setImageResource(R.drawable.green_ball)
            ib_yellowLed.setImageResource(R.drawable.yellow_ball)
        }
        else {
            ib_yellowLed.setImageResource(R.drawable.groy_ball)
            if ((state and 1) == 1){
                ib_redLed.setImageResource(R.drawable.red_ball)
                ib_yellowLed.setImageResource(R.drawable.red_ball)
            }
            else{
                ib_redLed.setImageResource(R.drawable.groy_ball)
            }
            if((state and 2) == 2){
                ib_greenLed.setImageResource(R.drawable.green_ball)
                ib_yellowLed.setImageResource(R.drawable.green_ball)
            }
            else{
                ib_greenLed.setImageResource(R.drawable.groy_ball)
            }
        }
    }
    fun intRef(){
        logRef.addValueEventListener(object  : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.child("data").value.toString()
                tv_log.text = value
                val value_list = value.split(',')
                tv_cds_scanner.text = value_list[0] + " lu"
                tv_temp_scanner.text = value_list[1] + " °C"
                tv_humi_scanner.text = value_list[2] + " %"
                buttinState(value_list[3].toInt())
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

//        ledRef.addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val value = snapshot.value.toString().toInt()
//                ib_redLed.setImageResource(R.drawable.groy_ball);
//                ib_greenLed.setImageResource(R.drawable.groy_ball);
//                ib_yellowLed.setImageResource(R.drawable.groy_ball);
//                when(value){
//                    1 ->{
//                        ib_redLed.setImageResource(R.drawable.red_ball)
//                        ib_yellowLed.setImageResource(R.drawable.red_ball)
//                    }
//                    2 ->{
//                        ib_greenLed.setImageResource(R.drawable.green_ball)
//                        ib_yellowLed.setImageResource(R.drawable.green_ball)
//                    }
//                    3 ->{
//                        ib_redLed.setImageResource(R.drawable.red_ball)
//                        ib_greenLed.setImageResource(R.drawable.green_ball)
//                        ib_yellowLed.setImageResource(R.drawable.yellow_ball)
//                    }
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//            }
//
//        })

//        cdsRef.addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val value = snapshot.value.toString()
////                Log.d("MainActivity", "cdsvalue:" + value)
//                tv_cds_scanner.text = value + " lu"
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//            }
//
//        })

//        dht11Ref.addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val temp_value = snapshot.child("temp").value.toString()
//                val humi_value = snapshot.child("humi").value.toString()
////                Log.d("MainActivity", "tmep_value:" + temp_value + "humi_value:" + humi_value)
//                tv_temp_scanner.text = temp_value + " °C"
//                tv_humi_scanner.text = humi_value + " %"
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//            }
//
//        })

//        doorRef.addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val door_value = snapshot.value.toString().toInt()
////                Log.d("MainActivity", "door_value:" + door_value)
//                if(door_value == 1){
//                    ib_door.setImageResource(R.drawable.door_open)
//                }
//                else{
//                    ib_door.setImageResource(R.drawable.door_close)
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//            }
//
//        })

//        buzeerRef.addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val value = snapshot.value.toString().toInt()
////                Log.d("MainActivity", "buzzer:" + value)
//                if(value == 1){
//                    ib_warning.setImageResource(R.drawable.buzzer_on)
//                }
//                else{
//                    ib_warning.setImageResource(R.drawable.buzzer_off)
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//            }
//
//        })

    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menu?.add("face")?.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
//        return super.onCreateOptionsMenu(menu)
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.title.equals("face")) {
//            faceRef.setValue(1)
//        }
//        return super.onOptionsItemSelected(item)
//    }
}