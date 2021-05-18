package com.capstone.belink.Ui

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.belink.Adapter.ProfileData
import com.capstone.belink.Adapter.RecyclerAdapter
import com.capstone.belink.R
import com.capstone.belink.Utils.ItemMoveCallbackListener
import com.capstone.belink.Utils.getGroupPref
import com.capstone.belink.databinding.FragmentGroupBinding
import com.google.android.material.snackbar.Snackbar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentGroup.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentGroup : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var mBinding:FragmentGroupBinding?=null
    private val binding get() = mBinding!!

    private var mContext: Context?=null
    private val xContext get() = mContext!!

    private lateinit var adapter:RecyclerAdapter


    private lateinit var touchHelper: ItemTouchHelper


    override fun onAttach(context: Context) {
        mContext=context
        super.onAttach(context)
    }

    val DataList = arrayListOf(
        ProfileData(R.drawable.picachu, "팀플용"),
        ProfileData(R.drawable.picachu, "팀플용"),
        ProfileData(R.drawable.picachu, "팀플용"),
        ProfileData(R.drawable.picachu, "팀플용"),
        ProfileData(R.drawable.picachu, "팀플용")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.group_menu,menu)//그룹 화면으로 변했을 때 액션바에서 세팅 아이콘 추가
    }

    /**
     * 활동이 재개됨 상태로 전환되었을 때 onResume 생명주기가 실행된다. 앱이 실질적으로 실행되기전 단계
     * 이 상태에서 수명 주기 구성요소가 foreground에서 사용자에게 보이는 동안 실행해야 하는 모든 기능을 활성화
     * 그러므로 onResume 생명주기때 액션바에 장착된 메뉴바를 다시 그리게 하는 메소드인 invalidate~~ 를 쓰는 이유가 된다.*/
    override fun onResume() {//생명주기 중 한 부분
        super.onResume()
        activity!!.invalidateOptionsMenu() // 메뉴를 다시 그리게 할 때 쓰이는 메소드
        refreshAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentGroupBinding.inflate(inflater, container, false)


        val teamList= getGroupPref(xContext, "groupContext")
        println(teamList.toString())
        setHasOptionsMenu(true)//세팅 아이콘 추가로 할 때 쓰이는 메소드(액티비티의 옵션보다 우선순위를 높이기 위한 메소드)

        adapter = RecyclerAdapter(xContext)
        val callback:ItemTouchHelper.Callback = ItemMoveCallbackListener(adapter)
        touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(binding.viewRecycler)
        binding.viewRecycler.layoutManager= LinearLayoutManager(xContext)
        adapter.DataList=teamList
        binding.viewRecycler.adapter=adapter
        return binding.root
    }

    fun refreshAdapter(){
        val teamList= getGroupPref(xContext, "groupContext")
        adapter = RecyclerAdapter(xContext)
        adapter.DataList=teamList
        binding.viewRecycler.adapter=adapter
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentGroup.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FragmentGroup().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    override fun onDestroy() {
        mBinding=null
        super.onDestroy()
    }
}