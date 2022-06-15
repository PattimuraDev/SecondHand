package com.example.secondhand.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.asLiveData
import com.example.secondhand.R
import com.example.secondhand.datastore.UserLoginTokenManager

class SplashAcivity : AppCompatActivity() {

    private lateinit var userLoginTokenManager: UserLoginTokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_acivity)

        userLoginTokenManager = UserLoginTokenManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            userLoginTokenManager.booelan.asLiveData().observe(this){
                if(it == true){
                    startActivity(Intent(this, MainActivity::class.java))
                }else{
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            }
        }, 3000)

    }
}