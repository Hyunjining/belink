package com.capstone.belink.UIActivity

import android.app.Activity
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.capstone.belink.Adapter.FriendAdapter
import com.capstone.belink.Model.*
import com.capstone.belink.Network.RetrofitClient
import com.capstone.belink.Network.RetrofitService
import com.capstone.belink.R
import com.capstone.belink.Ui.FragmentFriend
import com.capstone.belink.Utils.getGroupPref
import com.capstone.belink.Utils.getMemberPref
import com.capstone.belink.Utils.setGroupPref
import com.capstone.belink.databinding.ActivityTeamBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class TeamActivity : AppCompatActivity() {
    private var mBinding:ActivityTeamBinding?=null
    private val binding get() = mBinding!!

    private lateinit var retrofit:Retrofit
    private lateinit var supplementService:RetrofitService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding= ActivityTeamBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRetrofit()

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.team_frame_layout,FragmentFriend()).commit()
    }

    private fun initRetrofit() {
        retrofit=RetrofitClient.getInstance(this)
        supplementService=retrofit.create(RetrofitService::class.java)
    }

    /**
     * 프라그먼트에서 직접적으로 프라그먼트를 교체할 수 없기에 액티비티에서 프라그먼트를
     * 교체하는 함수를 미리 정의한다. 그러면 프라그먼트의 교체를 용이하게 할 수 있다.*/

    fun replaceFragment(fragment:Fragment){ //액티비티에서 프라그먼토 교체 함수
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.team_frame_layout,fragment).commit()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean { //팀 액티비티에 관한 액션바 아이콘 수정
        menuInflater.inflate(R.menu.team_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            /**
             * 클릭된 유저중에 true 값을 가진 유저만을 따로 저장한다.*/
            R.id.action_check -> { //확인 클릭시 그룹방 만들기
                val member = getMemberPref(this, "team") // 그룹에 관한 멤버 가져오기, 이에 관한 값들은 adapter에서 사용중
                var teamMember: MutableList<String> = ArrayList()
                for ((k, v) in member) {
                    println("$k  $v")
                    if (v) {// 아이디 중 체크박스 중 체크된 것들만 가져오기
                        teamMember.add(k)
                    }
                }
                var teamName: String = ""
                println(teamMember.toString())

                /**
                 * 팀 이름을 나 자신을 제외한 유저 이름으로 하기 위해 호출된 서비스이다.*/
                supplementService.idContactUser(teamMember).enqueue(object : Callback<ContactInfo> {// id값 기준으로 연락처 조회
                    override fun onResponse(call: Call<ContactInfo>, response: Response<ContactInfo>) {
                        val data = response.body()?.data
                        println("idContactUser")
                        println(data.toString())
                        if (data != null) {
                            for (i in data.indices) {
                                teamName += if(i != data.size - 1) {// 그룹 이름을 유저 이름 제외한 유저이름들로 구성
                                    data[i].username + " , "
                                } else {
                                    data[i].username
                                }
                            }
                        }

                    /**
                     * 데이터베이스에 저장될 때 만든 장본인도 저장되어야 하므로 sharedPreferences에 저장되어 있는 유저 아이디를 추가한다.
                     * 그리고 위에서 만든 팀이름을 데이터베이스에 저장한 다음에 그를 통해 구한 팀 아이디와 유정 아이디들을 맵핑하여
                     * 멤버 테이블에 저장한다. 그리고 따로 앱에서 그룹에 관련된 정보들도 또한 저장한다(setGroupPref)*/
                        teamMember.add(getSharedPreferences("auto", MODE_PRIVATE).getString("userId","")!!)
                        supplementService.makeTeam(teamName).enqueue(object : Callback<Team> {// 그룹 생성
                            override fun onResponse(call: Call<Team>, response: Response<Team>) {
                                println(response.message())
                                if (response.message() == "Created") {
                                    val id = response.body()?.id

                                    if (id!!.isNotEmpty()) {
                                        var teamList: MutableList<Member> = ArrayList()
                                        for (i in 0 until teamMember.size) {
                                            teamList.add(Member(id, teamMember[i]))
                                        }
                                        supplementService.makeMember(teamList).enqueue(object : Callback<Map<String,Boolean>> { // 유저와 그룹 맵핑
                                            override fun onResponse(call: Call<Map<String,Boolean>>, response: Response<Map<String,Boolean>>) {
                                                println("makeMember 수행중")
                                                println(response.message())
                                                if (response.message() == "OK") {
                                                    val teamList= getGroupPref(this@TeamActivity,"groupContext")
                                                    val obj = TeamRoom(id =id!!, teamName = teamName, data =teamMember)
                                                    teamList.add(obj)
                                                    setGroupPref(this@TeamActivity,"groupContext",teamList)
                                                    getSharedPreferences("team", MODE_PRIVATE).edit().clear().commit()

                                                    setResult(Activity.RESULT_OK)
                                                    finish()
                                                }
                                            }
                                            override fun onFailure(call: Call<Map<String,Boolean>>, t: Throwable) {
                                                Log.d("memberFail", "$t")
                                            }
                                        })
                                    }
                                }
                            }
                            override fun onFailure(call: Call<Team>, t: Throwable) {
                                Log.d("makeTeamFail", "$t")
                            }
                        })
                    }
                    override fun onFailure(call: Call<ContactInfo>, t: Throwable) {
                        Log.d("contactUserFail", "$t")
                    }

                })
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onDestroy() {
        mBinding=null
        super.onDestroy()
    }
}