package com.quantum.quran.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.quantum.quran.adapter.SurahListAdapter
import com.quantum.quran.databinding.FragmentSurahBinding
import com.quantum.quran.model.SurahList
import com.quantum.quran.sql.SurahHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Surah : Fragment() {

    private val data = ArrayList<SurahList>()
    private var adapter: SurahListAdapter? = null
    private var binding: FragmentSurahBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        binding = FragmentSurahBinding.inflate(inflater, container, false)

        adapter = SurahListAdapter(requireContext(), data)

        binding?.surahRecycler?.layoutManager = LinearLayoutManager(requireContext())
        binding?.surahRecycler?.adapter = adapter

        loadData()

        return binding?.root
    }

    private fun loadData() {
        CoroutineScope(Dispatchers.Default).launch {
            data.clear()
            data.addAll(SurahHelper(requireContext()).readData())
            activity?.runOnUiThread { adapter?.notifyItemRangeChanged(0, data.size) }
        }
    }

    override fun onDetach() {
        super.onDetach()
        binding = null
    }
}