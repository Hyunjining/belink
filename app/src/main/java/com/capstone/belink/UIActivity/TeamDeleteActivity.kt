//package com.capstone.belink.UIActivity
//
//import android.app.Activity
//import android.os.Bundle
//import android.util.Log
//import android.view.Menu
//import android.view.MenuItem
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.capstone.belink.Adapter.AlarmAdapter
//import com.capstone.belink.Model.ContactInfo
//import com.capstone.belink.Model.Member
//import com.capstone.belink.Model.Team
//import com.capstone.belink.Model.TeamRoom
//import com.capstone.belink.Network.RetrofitClient
//import com.capstone.belink.Network.RetrofitService
//import com.capstone.belink.R
//import com.capstone.belink.Utils.getGroupPref
//import com.capstone.belink.Utils.getMemberPref
//import com.capstone.belink.Utils.setGroupPref
//import com.capstone.belink.databinding.ActivityAlarmBinding
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import retrofit2.Retrofit
//
//class TeamDeleteActivity : AppCompatActivity() {
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
//
//    }
//
//    private fun initRetrofit() {
//        retrofit= RetrofitClient.getInstance(this)
//        supplementService=retrofit.create(RetrofitService::class.java)
//    }
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when(item.itemId){
//            /**
//             * 클릭된 유저중에 true 값을 가진 유저만을 따로 저장한다.*/
//            R.id.action_check -> { //확인 클릭시 그룹방 만들기
//                val member = getMemberPref(this, "team") // 그룹에 관한 멤버 가져오기, 이에 관한 값들은 adapter에서 사용중
//                var teamMember: MutableList<String> = ArrayList()
//                for ((k, v) in member) {
//                    println("$k  $v")
//                    if (v) {// 아이디 중 체크박스 중 체크된 것들만 가져오기
//                        teamMember.add(k)
//                    }
//                }
//                var teamName: String = ""
//                println(teamMember.toString())
//
//                /**
//                 * 팀 이름을 나 자신을 제외한 유저 이름으로 하기 위해 호출된 서비스이다.*/
//                supplementService.deleteTeam().enqueue(object : Callback<Map<String, Boolean>> {
//
//
//                })
//                true
//            }
//            else -> {
//                super.onOptionsItemSelected(item)
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        mBinding=null
//        super.onDestroy()
//    }
//}