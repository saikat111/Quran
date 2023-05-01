package com.quantum.quran.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.quantum.quran.adapter.ParaListAdapter
import com.quantum.quran.constant.Para
import com.quantum.quran.databinding.FragmentParaBinding

class Para : Fragment() {

    private var binding: FragmentParaBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        binding = FragmentParaBinding.inflate(inflater, container, false)

        binding?.paraRecycler?.layoutManager = LinearLayoutManager(requireContext())
        binding?.paraRecycler?.adapter = ParaListAdapter(
            requireContext(), Para().Position()
        )

        return binding?.root
    }

    override fun onDetach() {
        super.onDetach()
        binding = null
    }
}