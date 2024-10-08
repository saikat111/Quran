package com.quantum.quran.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.quantum.quran.R
import com.quantum.quran.adapter.SurahAyatAdapter
import com.quantum.quran.application.Constant
import com.quantum.quran.constant.Name
import com.quantum.quran.database.ApplicationData
import com.quantum.quran.database.LastRead
import com.quantum.quran.databinding.FragmentSurahAyatBinding
import com.quantum.quran.model.Quran
import com.quantum.quran.sql.QuranHelper
import com.quantum.quran.sql.SurahHelper
import com.quantum.quran.utils.KeyboardUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList
import androidx.recyclerview.widget.LinearSmoothScroller

import androidx.recyclerview.widget.RecyclerView


class SurahAyat(private val position: Int, val ayat: Int, private val scroll: Boolean) : Fragment() {

    private var search = ""
    private lateinit var smoothScroller: RecyclerView.SmoothScroller
    private val data = ArrayList<Quran>()
    private var ayatFollower: BroadcastReceiver? = null
    private lateinit var layoutManager: LinearLayoutManager
    private var adapterSurah: SurahAyatAdapter? = null
    private var binding: FragmentSurahAyatBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        binding = FragmentSurahAyatBinding.inflate(inflater, container, false)
        binding?.searchIcon?.clipToOutline = true

        layoutManager = LinearLayoutManager(requireContext())

        smoothScroller = object : LinearSmoothScroller(activity) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }

        CoroutineScope(Dispatchers.Default).launch {
            data.add(Quran(0, 0, 0, "", "",
                    "", "", "", "", "")
            )
            data.addAll(QuranHelper(requireContext()).readSurahNo(position + 1))
            val temp = SurahHelper(requireContext()).readData()[position]
            val t = "${revelation(temp.revelation)}   |   ${NumberFormat.getInstance(
                Locale(ApplicationData(requireContext()).language)).format(temp.verse)}" +
                    "  " + resources.getString(R.string.verses)
            adapterSurah = SurahAyatAdapter(
                requireContext(),
                temp.name, Name().data()[position],
                t, data
            )

            activity?.runOnUiThread {
                binding?.ayatRecycler?.layoutManager = layoutManager
                binding?.ayatRecycler?.adapter = adapterSurah
                if (scroll) binding?.ayatRecycler?.scrollToPosition(ayat)
            }
        }

        click()

        binding?.searchIcon?.setOnClickListener {
            binding?.searchText?.text.toString().let { e->
                search = if (e.isNotEmpty()) {
                    try {
                        filter(e.toInt())
                    } catch (ex: Exception) {
                        filter(e)
                    }
                    e
                } else {
                    filter("")
                    ""
                }
            }
            closeKeyboard(binding?.searchText)
        }

        return binding?.root
    }

    private fun revelation(revelation: String): String {
        return if (revelation == "Meccan")
            resources.getString(R.string.meccan)
        else resources.getString(R.string.medinan)
    }

    private fun click() {
        binding?.searchText?.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val key = binding?.searchText?.text.toString()
                search = if (key.isNotEmpty()) {
                    try {
                        filter(key.toInt())
                    } catch (ex: Exception) {
                        filter(key)
                    }
                    key
                } else {
                    filter("")
                    ""
                }
                closeKeyboard(binding?.searchText)
                return@OnEditorActionListener true
            }
            false
        })
    }

    private fun filter(pos: Int) {
        val l = data.size
        data.clear()
        adapterSurah?.notifyItemRangeRemoved(0, l)
        data.add(Quran(0, 0, 0, "", "",
                "", "", "", "", "")
        )
        data.addAll(QuranHelper(requireContext()).readSurahNo(position + 1))
        adapterSurah?.notifyItemRangeInserted(0, data.size)
        if (pos < data.size) {
            binding?.ayatRecycler?.scrollToPosition(pos)
        }
    }

    private fun filter(filter: String) {
        adapterSurah?.notifyItemRangeRemoved(0, data.size)
        CoroutineScope(Dispatchers.Default).launch {
            data.clear()
            data.add(Quran(0, 0, 0, "", "",
                "", "", "", "", "")
            )
            val a = QuranHelper(requireContext()).readData().filter {
                when(ApplicationData(requireContext()).translation) {
                    ApplicationData.TAISIRUL -> it.terjemahan
                    ApplicationData.MUHIUDDIN -> it.jalalayn
                    else -> it.englishT.lowercase(Locale.getDefault())
                }.contains(filter.lowercase())
            }
            a.forEach {
                val temp = when(ApplicationData(requireContext()).translation) {
                    ApplicationData.TAISIRUL -> it.terjemahan
                    ApplicationData.MUHIUDDIN -> it.jalalayn
                    else -> it.englishT
                }
                val start = temp.lowercase(Locale.getDefault())
                    .indexOf(filter.lowercase(Locale.getDefault()))
                val translation = "${temp.substring(0, start)}<b><font color=#2979FF>" +
                        "${temp.substring(start, start+filter.length)}</font></b>${temp.substring(start+filter.length)}"

                data.add(
                    Quran(
                        pos = it.pos,
                        surah = it.surah,
                        ayat = it.ayat,
                        indopak = it.indopak,
                        utsmani = it.utsmani,
                        jalalayn = if (ApplicationData(requireContext()).translation
                            == ApplicationData.MUHIUDDIN) translation else it.jalalayn,
                        latin = it.latin,
                        terjemahan = if (ApplicationData(requireContext()).translation
                            == ApplicationData.TAISIRUL) translation else it.terjemahan,
                        englishPro = it.englishPro,
                        englishT = if (ApplicationData(requireContext()).translation
                            == ApplicationData.ENGLISH) translation else it.englishT
                    )
                )
            }
            requireActivity().runOnUiThread {
                adapterSurah?.notifyItemRangeRemoved(0, data.size)
                Toast.makeText(context,
                    "${data.size} Search result found."
                    , Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun closeKeyboard(edit: EditText?) {
        edit?.let {
            it.clearFocus()
            KeyboardUtils.hideKeyboard(it)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e("TAG", (Constant.SURAH+position).toString())
        ayatFollower = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    binding?.appBar?.setExpanded(false, true)
                    smoothScroller.targetPosition = intent.getIntExtra("AYAT", 0)+1
                    layoutManager.startSmoothScroll(smoothScroller)
                    adapterSurah?.read(intent.getIntExtra("AYAT", 0)+1)
                }
            }
        }

        activity?.registerReceiver(ayatFollower, IntentFilter(Constant.SURAH+position))
    }

    override fun onPause() {
        activity?.unregisterReceiver(ayatFollower)
        if (LastRead(requireContext()).surahNo == position) {
            LastRead(requireContext()).ayatNo =
                layoutManager.findFirstCompletelyVisibleItemPosition().let {
                    if (it < 0)
                        layoutManager.findFirstVisibleItemPosition()
                    else it
            }
        }
        super.onPause()
    }

    override fun onDetach() {
        super.onDetach()
        binding = null
    }
}