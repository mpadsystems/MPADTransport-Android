package com.wneth.mpadtransport.views.configurations

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wneth.mpadtransport.R
import com.wneth.mpadtransport.databinding.ActivityHotspotBinding
import com.wneth.mpadtransport.models.HotspotWithNameModel
import com.wneth.mpadtransport.utilities.amountToPHP
import com.wneth.mpadtransport.utilities.getStateName
import com.wneth.mpadtransport.utilities.interfaces.RecyclerDialogEventListener
import com.wneth.mpadtransport.utilities.interfaces.SearchFragmentListener
import com.wneth.mpadtransport.utilities.recyclerviews.MenuRecyclerViewAdapter
import com.wneth.mpadtransport.viewmodels.configurations.HotspotActivityViewModel
import com.wneth.mpadtransport.views.fragments.SearchFragment

class HotspotActivity : AppCompatActivity(), SearchFragmentListener, RecyclerDialogEventListener {

    private lateinit var viewModel: HotspotActivityViewModel
    private lateinit var viewBinding: ActivityHotspotBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_hotspot)

        viewModel = ViewModelProvider(this)[HotspotActivityViewModel::class.java]
        viewBinding = ActivityHotspotBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Initialize Search Fragments
        if (savedInstanceState == null) {
            val bundle = bundleOf("some_int" to 0)
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<SearchFragment>(R.id.searchFragment, args = bundle)
            }
        }
        // =================================

        populateHotspots()
    }

    private fun populateHotspots(){
        viewModel.hotspots.observe(this) {
            populateHotspots(it)
        }
    }

    private fun populateHotspots(hotspots: List<HotspotWithNameModel>) {

        val menuRecyclerView: RecyclerView = findViewById(R.id.recyclerItems);
        val manager = GridLayoutManager(this, 1)
        menuRecyclerView.layoutManager = manager

        val data = hotspots.map { hotspot ->
            mapOf(
                "id" to hotspot.id,
                "navigateTo" to "",
                "visible" to true,
                "title" to hotspot.routeName,
                "subTitle" to
                        "From:       ${hotspot.fromSegmentName} \n" +
                        "To:            ${hotspot.toSegmentName}\n" +
                        "Amount:   ${hotspot.amount.amountToPHP()}\n" +
                        "Active:      ${getStateName(hotspot.isActive)}",
                "icon" to null
            )
        }

        val menu = mapOf(
            "type" to "list",
            "data" to data
        )

        val adapter = MenuRecyclerViewAdapter(applicationContext, menu)
        menuRecyclerView.adapter = adapter
    }

    override fun onSearchTextReceived(text: String) {
        viewModel.filterHotspots(text)
    }

    override fun onRecyclerItemClicked(
        id: Int,
        position: Int,
        title: String,
        subTitle: String,
        navigateTo: String,
        data: Any?
    ) {
        //
    }

    override fun onRecyclerItemLongClicked(
        id: Int,
        position: Int,
        title: String,
        subTitle: String,
        navigateTo: String
    ) {
        //
    }
}