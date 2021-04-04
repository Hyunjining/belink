package com.capstone.belink

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.capstone.belink.Model.SignDTO
import com.capstone.belink.Network.RetrofitClient
import com.capstone.belink.Network.RetrofitService
import com.capstone.belink.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class LoginActivity : AppCompatActivity() {
    private var mBinding:ActivityLoginBinding?=null
    private val binding get() = mBinding!!

    private var firstNum=""
    private var secondNum=""
    private var thirdNum=""

    private var phoneNum:String?=null
    private var name:String?=null

    private lateinit var retrofit : Retrofit
    private lateinit var supplementService : RetrofitService

    private lateinit var auto:SharedPreferences
    private lateinit var autoLogin:SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRetrofit()

        auto =getSharedPreferences("auto", Activity.MODE_PRIVATE)!!
        autoLogin=auto.edit()

        phoneNum=auto.getString("inputPhone",null)
        name=auto.getString("inputName",null)

        if(!name.isNullOrBlank() && !phoneNum.isNullOrBlank()){
            Log.d("status","first_if")
            login(phoneNum!!)
        }

        binding.btnLoginSignup.setOnClickListener {
            getEditString()
            autoLogin.apply()
            signup(phoneNum!!,name!!)
        }
        binding.btnLoginLogin.setOnClickListener {
            getEditString()
            login(phoneNum!!)
        }
    }
    fun getEditString(){
        firstNum=binding.etLoginPhoneFirst.text.toString()
        secondNum=binding.etLoginPhoneSecond.text.toString()
        thirdNum=binding.etLoginPhoneThird.text.toString()
        name=binding.etLoginName.text.toString()
        phoneNum=firstNum+secondNum+thirdNum
        autoLogin.clear()
        autoLogin.putString("inputPhone",phoneNum)
        autoLogin.putString("inputName",name)
    }

    private fun initRetrofit() {
        retrofit = RetrofitClient.getInstance()
        supplementService = retrofit.create(RetrofitService::class.java)
    }

    private fun signup(Phone: String, name: String) {

        supplementService.registerUser(Phone,name).enqueue(object : Callback<SignDTO>{
            override fun onResponse(call: Call<SignDTO>, response: Response<SignDTO>) {
                Log.d("Phone",Phone)
                Log.d("Name",name)
                Log.d("success",response.message())
            }

            override fun onFailure(call: Call<SignDTO>, t: Throwable) {
                Log.d("fail","$t")
            }
        })
    }

    fun login(phoneNum: String) {
        supplementService.getuser(phoneNum).enqueue(object :Callback<SignDTO>{
            override fun onResponse(call: Call<SignDTO>, response: Response<SignDTO>) {
                if(response.message()=="OK"){
                    val intent = Intent(this@LoginActivity,MainActivity::class.java)
                    autoLogin.putString("userId",response.body()?.data?.id.toString())
                    println(response.body())
                    println(response.body()?.data?.id.toString())
                    autoLogin.apply()
                    startActivity(intent)
                    finish()
                }
            }
            override fun onFailure(call: Call<SignDTO>, t: Throwable) {
            }
        })
    }
    override fun onDestroy() {
        mBinding=null
        super.onDestroy()
    }
}