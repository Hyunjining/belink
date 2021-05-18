//package com.capstone.belink.UIActivity
//
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import com.capstone.belink.Network.RetrofitClient
//import com.capstone.belink.Network.RetrofitService
//import com.capstone.belink.databinding.ActivityAlarmBinding
//import retrofit2.Callback
//import retrofit2.Retrofit
//
//class TeamUpdateActivity : AppCompatActivity() {
//    private var mBinding: ActivityAlarmBinding?=null
//    private val binding get() = mBinding!!
//
//    private lateinit var retrofit: Retrofit
//    private lateinit var supplementService: RetrofitService
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        mBinding= ActivityAlarmBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        initRetrofit()
//
//    }
//
//    private fun initRetrofit() {
//        retrofit= RetrofitClient.getInstance(this)
//        supplementService=retrofit.create(RetrofitService::class.java)
//    }
//
//    supplementService.editTeam(teamName).enqueue(object : Callback<Map<String, Boolean>> {
//
//    }
//
//    override fun onDestroy() {
//        mBinding=null
//        super.onDestroy()
//    }
//}