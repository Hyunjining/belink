package com.capstone.belink.Network

import com.capstone.belink.Model.*
import retrofit2.Call
import retrofit2.http.*

interface RetrofitService {

    @FormUrlEncoded
    @POST("/api/user/signup")
    fun registerUser(
        @Field("phNum")phNum:String,@Field("username")username:String
    ): Call<Sign>

    @PUT("api/user/edit-info")
    fun editUser(@Body user: User): Call<Map<String,Boolean>>

    @GET("api/user/edit-info")
    fun deleteUser():Call<Map<String,Boolean>>

    @FormUrlEncoded
    @POST("api/user/edit-team")
    fun makeTeam(@Field("teamName")teamName:String):Call<Team>

    @PUT("api/user/edit-team")
    fun editTeam(@Body teamName:String): Call<Map<String,Boolean>>

    @GET("api/user/edit-team")
    fun deleteTeam():Call<Map<String,Boolean>>

    @FormUrlEncoded
    @POST("api/user/contact-user")
    fun contactUser(@Field("phNum")phNum:List<String>):Call<ContactInfo>

    @FormUrlEncoded
    @POST("api/user/id-contact-user")
    fun idContactUser(@Field("id")id:List<String>):Call<ContactInfo>

    @POST("api/user/make-member")
    fun makeMember(@Body teamList:MutableList<Member>):Call<Map<String,Boolean>>

    @FormUrlEncoded
    @POST("api/user/login")
    fun login(@Field("phNum")phNum: String):Call<LoginResponse>

    @GET("api/user/check")
    fun check():Call<Map<String,Boolean>>

    @POST("api/user/edit-friend")
    fun makeFriend(@Body friendList:MutableList<Friend>):Call<Map<String,Boolean>>


}