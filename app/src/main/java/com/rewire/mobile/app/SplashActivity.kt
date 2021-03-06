package com.rewire.mobile.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import com.rewire.mobile.app.API.ApiClient
import com.rewire.mobile.app.API.ApiInterface
import com.rewire.mobile.app.Library.UserPortal
import com.rewire.mobile.app.Library.Portal
import com.rewire.mobile.app.Model.LoginResponse
import com.rewire.mobile.app.Model.UserDate
import com.rewire.mobile.app.PaperHelper.LocaleHelper
import com.rewire.mobile.app.Service.CheckUpService
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_splash.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import android.view.animation.AnimationSet
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric





class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_splash)

        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.interpolator = DecelerateInterpolator() //add this
        fadeIn.duration = 1500

        val animation = AnimationSet(false) //change to false
        animation.addAnimation(fadeIn)

        tvHosGeldin.animation = animation

        aviLoading.smoothToShow()
        Portal.raiseUp()
        startService()
        /*Portal.deleteNotfiy(this)*/
        Paper.init(this)
        val lang = Paper.book().read<String>("language")
        if(lang == null)
        {
            Paper.book().write("language","en")
        }
        val context = LocaleHelper.setLocale(this,Paper.book().read<String>("language"))
        UserPortal.myLangResource = context.resources

        tvHosGeldin.setText(context.getString(R.string.hos_geldin))

        val myUser = Portal.autoLogin(this)

        if(myUser != null)
        {
            val apiInterface = ApiClient.client?.create(ApiInterface::class.java)

            val result = apiInterface?.tokenAl(myUser.Username!! ,myUser.Password!!,"password")

            result?.enqueue(object: Callback<LoginResponse>{
                override fun onFailure(call: Call<LoginResponse>?, t: Throwable?) {
                    redirectToLogin()
                }

                override fun onResponse(call: Call<LoginResponse>?, response: Response<LoginResponse>?) {
                    if(response?.message()?.toString() == "OK")
                    {
                        val body = response.body()
                        myUser.AccessToken = body?.access_token
                        UserPortal.loggedInUser = myUser
                        Portal.updateUserTimeZone(this@SplashActivity,myUser.Username!!)
                        UserPortal.updateUserInfo()
                        UserPortal.getLikes()

                        val getDates = apiInterface.getDates("Bearer ${body!!.access_token!!}")
                        getDates.enqueue(object:Callback<ArrayList<UserDate>>{
                            override fun onFailure(call: Call<ArrayList<UserDate>>?, t: Throwable?) {
                                redirectToLogin()
                            }

                            override fun onResponse(call: Call<ArrayList<UserDate>>?, response: Response<ArrayList<UserDate>>?) {
                                if(response?.code() == 200) {
                                    UserPortal.userDates = response.body()
                                    /*Portal.updateUserSettingsCheckUpTime(this@SplashActivity,
                                            "11:07",
                                            UserPortal.loggedInUser!!.Username!!)*/
                                    val intent = Intent(this@SplashActivity,HomeActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }else {
                                    redirectToLogin()
                                }
                            }
                        })
                    }else
                    {
                        redirectToLogin()
                    }
                }
            })
        }else
        {
            redirectToLogin()
        }

    }


    private fun redirectToLogin()
    {
        object:CountDownTimer(2000,1000)
        {
            override fun onFinish() {
                val intent = Intent(this@SplashActivity,LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

            override fun onTick(millisUntilFinished: Long) {
                var asd = 1
            }

        }.start()
    }

    private fun startService()
    {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this@SplashActivity,CheckUpService::class.java)
        val pendingIntent = PendingIntent.getService(this@SplashActivity,
                100,intent,PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                1000,180000,
                pendingIntent)

    }
}
