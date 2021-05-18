package com.capstone.belink.UIActivity

import android.app.Activity
import android.content.Intent

import android.content.SharedPreferences
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.Ndef
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils.replace
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import android.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

import androidx.viewpager2.widget.ViewPager2
import com.capstone.belink.Adapter.FragmentStateAdapter
import com.capstone.belink.Network.RetrofitClient
import com.capstone.belink.Network.RetrofitService
import com.capstone.belink.R
import com.capstone.belink.Ui.*
import com.capstone.belink.Utils.CardService
import com.capstone.belink.Utils.HexUtils
import com.capstone.belink.databinding.ActivityMainBinding
import retrofit2.Retrofit


class MainActivity : AppCompatActivity() {//, NfcAdapter.ReaderCallback
    private var mBinding:ActivityMainBinding?=null
    val binding get() = mBinding!!

    private lateinit var retrofit : Retrofit
    private lateinit var supplementService : RetrofitService


    private lateinit var pref: SharedPreferences
    private lateinit var prefEdit: SharedPreferences.Editor

//    private lateinit var nfcAdapter: NfcAdapter

    private val fragmentMain by lazy { FragmentMain()}
    private val fragmentGroup by lazy { FragmentGroup()}
    private val fragmentMap by lazy { FragmentMap()}
    private val fragmentEtcetra by lazy { FragmentEtcetra()}



    private var fragmentLists = listOf(FragmentMain(), FragmentGroup(), FragmentMap(), FragmentEtcetra())


    override fun onResume() {

        super.onResume()
//        nfcAdapter.enableReaderMode(
//                this,
//                this,
//                NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
//                null
//        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
      //  startActivity(intent)

        pref =getSharedPreferences("auto", Activity.MODE_PRIVATE)!!
        prefEdit=pref.edit()

        invalidateOptionsMenu()


        initRetrofit()
        init()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

//        if(NfcAdapter.ACTION_NDEF_DISCOVERED == intent?.action){
//
//            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMessages->
//                val messages: List<NdefMessage> = rawMessages.map { it as NdefMessage }
//            }
//        }
    }

    /**
     * onActivityResult 상속 함수는 startActivityForResult 인텐트된 액티비티에서 활동을 끝나고 값을 전달했을떼
     * 그에 관한 처리를 담당하는 함수
     * requestCode는 코드작성자가 명시한 숫자로서 그에 대하여 when 분기문으로 처리하면 된다.
     * 그리고 resultCode를 통해 해당 메시지에 따른 처리를 어떻게 할 것인지 결정하면 된다.*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("requestCode: $requestCode, resultCode: $resultCode")
            when(requestCode){
                0 ->{
                    if(resultCode==Activity.RESULT_OK ) {
                        Log.d("0번", "팀액티비티 아웃")
                        Toast.makeText(this, "방이 만들어졌습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                1 ->{
                    if(resultCode==Activity.RESULT_OK ) {
                        Log.d("1번", "개인정보수정액티비티 아웃")
                        Toast.makeText(this, "회원 정보 수정이 완료 되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }



    /**
     * 액션바의 메뉴를 클릭했을 때 그에 해당하는 내용을 처리할 수 있는 상속 함수
     * 반환값은 진리값으로 반환한다.*/
    override fun onOptionsItemSelected(item: MenuItem): Boolean= when(item.itemId){
            R.id.action_plus ->{
                val intent = Intent(this, TeamActivity::class.java)
                startActivityForResult(intent,0)
                true
            }
            R.id.action_alert ->{
                val intent = Intent(this, AlarmActivity::class.java)
                startActivity(intent)
                true
            }
//            R.id.action_setting_group_delete->{
//                val intent = Intent(this, TeamDeleteActivity::class.java)
//                startActivity(intent)
//                true
//            }
//            R.id.action_setting_group_update->{
//                val intent = Intent(this, TeamUpdateActivity::class.java)
//                startActivity(intent)
//                true
//        }
            else -> super.onOptionsItemSelected(item)
        }


    /**
     * 우리가 원하는 메뉴를 추가하고 싶을 때에는 onCreateOptionMenu 상속 함수를 호출하고
     * menuInflater를 호출하여 여기에 해당하는 메뉴를 inflate 시키면 된다.*/
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actionbar_menu,menu)
        return true
    }



    // 서버 관련 함수
    private fun initRetrofit() {
        retrofit = RetrofitClient.getInstance(this)
        supplementService = retrofit.create(RetrofitService::class.java)
    }

    private fun changeFragment(fragment: Fragment){
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_layout, fragment)
                .commit()
    }

    /**
     * 뷰페이저에 관한 것을 묶은 함수
     * 처음에 해당 컨텍스트를 adapter에 전달하고 뷰페이저에 전달할 fragmentList를 전달한다.
     * 그리고 페이지가 넘어감에 따라 when 분기문으로 그에 맞는 내용을 작성한다.
     * 밑에 바텀네비게이션뷰에 대한 처리는 해당 페이지가 넘어갔을 때 그에 맞는 아이콘을 표시하기 위해
     * 처리한 구문이다.*/
    private fun init() {
        binding.bottomNavigationView.run{
            setOnNavigationItemSelectedListener {
                when(it.itemId){
                    R.id.main -> {
                    changeFragment(fragmentMain)
                }
                R.id.friend -> {
                    changeFragment(fragmentGroup)
                }
                R.id.map -> {
                    changeFragment(fragmentMap)
                }
                R.id.etcetra -> {
                    changeFragment(fragmentEtcetra)
                }
                }
                true
            }
            selectedItemId = R.id.main
        }

    }


    override fun onDestroy() {
        mBinding=null
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()

//        nfcAdapter.disableReaderMode(this)

    }

//    override fun onTagDiscovered(tag: Tag?) {
//        val isoDep = IsoDep.get(tag)
//        isoDep.connect()
//        val response = isoDep.transceive(
//                HexUtils.hexToByte("00A4040007A0000002471001")
//        )
//        runOnUiThread {
//            println("\n# Card:\ntag=${tag} isoDep=${isoDep}\n transceive=${HexUtils.byteToHex(response)}")
//        }
//        isoDep.close()
//    }
//
}

