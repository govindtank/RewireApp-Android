package com.example.taha.sigraylamcadele.API

import com.example.taha.sigraylamcadele.Model.LoginResponse
import com.example.taha.sigraylamcadele.Model.User
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by Taha on 28-Feb-18.
 */
interface ApiInterface {

    @FormUrlEncoded
    @POST("getToken")
    fun tokenAl(@Field("username") username:String,
                @Field("password") password:String,
                @Field("grant_type") grant_type:String):Call<LoginResponse>


    @POST("Register/RegisterUser")
    fun userRegister(@Header("Content-Type") content_type:String,@Body user: User):Call<String>

    @GET("User/GetUserInfo")
    fun userInfo(@Header("Authorization") access_token:String):Call<User>


}