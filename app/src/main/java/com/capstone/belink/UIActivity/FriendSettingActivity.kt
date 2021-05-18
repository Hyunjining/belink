package com.capstone.belink.UIActivity

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.capstone.belink.Model.ContactInfo
import com.capstone.belink.Model.Friend
import com.capstone.belink.Model.User
import com.capstone.belink.Network.RetrofitClient
import com.capstone.belink.Network.RetrofitService
import com.capstone.belink.Utils.getStringArrayPref
import com.capstone.belink.Utils.setStringArrayPref
import com.capstone.belink.databinding.ActivityFriendSettingBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class FriendSettingActivity : AppCompatActivity() {

    private var mBinding:ActivityFriendSettingBinding?=null
    val binding get() = mBinding!!
    private var mContext: Context? = null

    private lateinit var retrofit: Retrofit
    private lateinit var supplementService: RetrofitService

    private lateinit var auto: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding= ActivityFriendSettingBinding.inflate(layoutInflater)
        mContext = this
        setContentView(binding.root)
        initRetrofit()
        auto =getSharedPreferences("auto", Activity.MODE_PRIVATE)!!

        binding.tvFriendReadContact.setOnClickListener {
            getContact()
            syncContact()

        }

    }


    private fun initRetrofit() {
        retrofit = RetrofitClient.getInstance(this)
        supplementService = retrofit.create(RetrofitService::class.java)
    }

    fun readContacts(view: View){
        getContact()
        syncContact()
    }


    /**
     * 디바이스에 있는 주소록 정보를 가져오는 함수
     * 처음에 주소록 내용을 담을 리스트를 만든다.
     * 그리고 주소록을 불러올 contentResolver 객체를 호출하고 그에 맞는 메소드를 호출한다.
     * 그리고 주소록에 있는 정보 중에 필요한 정보인 이름과 전화번호를 추출하고 데이터클래스에 맵핑하여 저장한다.
     * 해당 주소록에 관한 일이 끝났으면 close()를 할 것
     * setStringArrayPref는 Utils.ConactInfo안에 있는 함수로서 주소록 정보를 json형식으로 바꾼 뒤 스트링 형식으로 저장하는 함수이다*/
    fun getContact(){ //주소 연락처 가져오기
        val contactList: MutableList<User> = ArrayList()
        val contacts = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
        )
        while (contacts!!.moveToNext()){
            val name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val number = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            val obj = User(id = "", username = name, phNum = number)

            contactList.add(obj)
        }
        contacts.close()

        setStringArrayPref(this, "contact", contactList)//연락처를 preferences에 저장

        Toast.makeText(this, "연락처 정보를 가져왔습니다.", Toast.LENGTH_SHORT).show()
    }


    /**
     * DataBase안에 있는 유저와 주소록 안에 있는 유저를 비교하여 DataBase에 있는 유저 정보만을 디바이스가 가지도록 하는
     * 함수이다. 여기서 getStringArrayPref는 현재 주소록을 저장하고 있는 String을 hashmap형태로 불러오는 함수이다
     * 여기서 필요한 것은 전화번호이므로 .keys를 통해 해당 전화번호만을 추출한다. 그리고 contactUser 서비스를 통해
     * 디비 안에 저장되어 있는 유저 정보를 가져오고 이를 다시 디바이스에 저장한다.*/

     fun syncContact(){ //주소 연락처에 있는 전화번호 중에 가입된 유저만 주소록 가져오기
        val contactUser=getStringArrayPref(this, "contact")

        println("contactUser.isEmpty() is ${contactUser.isEmpty()}")
        println("contactUser.keys is ${contactUser.keys}")
        val phNumList=contactUser.keys
        println("phNumList is $phNumList")
        val userList:MutableList<User> = ArrayList()
        supplementService.contactUser(phNumList.toList()).enqueue(object : Callback<ContactInfo> {
            override fun onResponse(call: Call<ContactInfo>, response: Response<ContactInfo>) {
                val data = response.body()?.data
                println("pass the response")
                println("${response.body()?.data}")
                if (data != null) {
                    for (i in data.indices) {
                        println("pass the for loop")
                        val id = data[i].id
                        val username = data[i].username
                        val phNum = data[i].phNum
                        println("$id  $username   $phNum")
                        userList.add(User(id = id, username = username, phNum = phNum))
                    }
                }
                setStringArrayPref((this@FriendSettingActivity), "contact", userList) //연락처를 갱신
                println("여기 통과하니?")
                var friendIdList: MutableList<Friend> = ArrayList()
                val id = auto.getString("userId", "")
                println("id : $id")
                for (i in 0 until userList.size) {
                    friendIdList.add(Friend(device = id!!, myFriend = userList[i].id))
                }
                println("************************")
                println("getStringArrayPref을 통과중")
                println(getStringArrayPref(this@FriendSettingActivity, "contact").toString())
                println("************************")
                println("friendList통과중")
                println(userList.toString())
                println("************************")
                println(friendIdList.toString())
                supplementService.makeFriend(friendIdList).enqueue(object : Callback<Map<String, Boolean>> {
                    override fun onResponse(
                            call: Call<Map<String, Boolean>>,
                            response: Response<Map<String, Boolean>>
                    ) {
                        println("--------pass--------")
                    }

                    override fun onFailure(call: Call<Map<String, Boolean>>, t: Throwable) {
                        println("--------fail--------")
                    }

                })
            }

            override fun onFailure(call: Call<ContactInfo>, t: Throwable) {
                Log.d("fail", "$t")
            }

        })
    }


    override fun onDestroy() {
        mBinding=null
        super.onDestroy()
    }




}