package com.capstone.belink.UIActivity

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.capstone.belink.Model.*
import com.capstone.belink.Network.RetrofitClient
import com.capstone.belink.Network.RetrofitService
import com.capstone.belink.Network.SessionManager
import com.capstone.belink.Utils.getStringArrayPref
import com.capstone.belink.Utils.setStringArrayPref
import com.capstone.belink.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.*


/**
 * LoginActivity는 로그인 또는 회원가입에 관한 액티비티
 * 이때 해당 유저이름과 전화번호를 sharedPreferences에 key-value 형식으로 저장한다.
 * 그리고 로그인 버튼을 눌렀을 때 jwt토큰을 저장하기 위해 sessionManager를 호출하여
 * saveAuthToken메소드를 호출한다.
 * */
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

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        initRetrofit()

        auto =getSharedPreferences("auto", Activity.MODE_PRIVATE)!!
        autoLogin=auto.edit()

        phoneNum=auto.getString("inputPhone", null)
        name=auto.getString("inputName", null)

        if(!name.isNullOrBlank() && !phoneNum.isNullOrBlank()){
            Log.d("status", "first_if")
            login(phoneNum!!)
        }

        binding.btnLoginSignup.setOnClickListener {
            getEditString()
            autoLogin.apply()
            signup(phoneNum!!, name!!)
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
        phoneNum= "$firstNum-$secondNum-$thirdNum"
//        phoneNum =firstNum+secondNum+thirdNum
        autoLogin.clear()
        autoLogin.putString("inputPhone", phoneNum)
        autoLogin.putString("inputName", name)
    }

    private fun initRetrofit() {
        retrofit = RetrofitClient.getInstance(this)
        supplementService = retrofit.create(RetrofitService::class.java)
    }

    private fun signup(Phone: String, name: String) {

        supplementService.registerUser(Phone, name).enqueue(object : Callback<Sign> {
            override fun onResponse(call: Call<Sign>, response: Response<Sign>) {
                Log.d("Phone", Phone)
                Log.d("Name", name)
                Log.d("success", response.message())
            }

            override fun onFailure(call: Call<Sign>, t: Throwable) {
                Log.d("fail", "$t")
            }
        })
    }

    fun login(phoneNum: String) {

        supplementService.login(phoneNum).enqueue(object : Callback<LoginResponse>{
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val loginResponse = response.body()

                if(response.message()=="OK" && loginResponse?.accessToken!=null){
                    sessionManager.saveAuthToken(loginResponse!!.accessToken)
                    getContact()
                    syncContact()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    autoLogin.putString("userToken", response.body()?.accessToken)
                    autoLogin.putString("inputName",name)
                    autoLogin.putString("inputPhone", phoneNum)
                    autoLogin.putString("userId",response.body()!!.id)
                    autoLogin.apply()

                    startActivity(intent)
                    this@LoginActivity.finish()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {

            }

        })

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
        val contactUser= getStringArrayPref(this, "contact")

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
                setStringArrayPref((this@LoginActivity), "contact", userList) //연락처를 갱신
                println("여기 통과하니?")
                var friendIdList: MutableList<Friend> = ArrayList()
                val id = auto.getString("userId", "")
                println("id : $id")
                for (i in 0 until userList.size) {
                    friendIdList.add(Friend(device = id!!, myFriend = userList[i].id))
                }
                println("************************")
                println("getStringArrayPref을 통과중")
                println(getStringArrayPref(this@LoginActivity, "contact").toString())
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

