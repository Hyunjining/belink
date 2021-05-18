   package com.capstone.belink.Ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment


import com.capstone.belink.UIActivity.FriendSettingActivity
import com.capstone.belink.UIActivity.MainActivity
import com.capstone.belink.databinding.FragmentMainBinding



   class FragmentMain : Fragment() {
    private var mBinding:FragmentMainBinding?=null
    private val binding get() = mBinding!!

    private var mContext:Context?=null
    private val xContext get() = mContext!!

    private var mActivity: Activity?=null
    private val xActivity get() = mActivity!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentMainBinding.inflate(inflater, container, false)
        val view = binding.root

        (activity as AppCompatActivity).supportActionBar?.title="개인"
        return view
    }

    override fun onAttach(context: Context) {
        mContext = context
        mActivity = activity as MainActivity?
        super.onAttach(context)
    }



   override fun onDestroy() {
    mBinding=null
    super.onDestroy()
    }
    companion object{
        const val KEY="key"
        fun newInstance(data: String) = FragmentMain().apply {
            arguments = Bundle().apply {
                putString(KEY, data)
            }
        }
    }
}


