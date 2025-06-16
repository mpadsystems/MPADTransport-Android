package com.wneth.mpadtransport.views.configurations

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wneth.mpadtransport.R
import com.wneth.mpadtransport.databinding.ActivityRouteSegmentBinding
import com.wneth.mpadtransport.models.RouteSegmentModel
import com.wneth.mpadtransport.utilities.interfaces.SearchFragmentListener
import com.wneth.mpadtransport.utilities.recyclerviews.MenuRecyclerViewAdapter
import com.wneth.mpadtransport.viewmodels.configurations.RouteActivityViewModel
import com.wneth.mpadtransport.views.fragments.SearchFragment

class RouteSegmentActivity : AppCompatActivity(), SearchFragmentListener {

    private lateinit var viewModel: RouteActivityViewModel
    private lateinit var viewBinding: ActivityRouteSegmentBinding
    private var routeId: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[RouteActivityViewModel::class.java]

        // Initialize viewBinding
        viewBinding = ActivityRouteSegmentBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)


        // Initialize Fragments
        if (savedInstanceState == null) {
            val bundle = bundleOf("some_int" to 0)
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<SearchFragment>(R.id.searchFragment, args = bundle)
            }
        }

        // Initialize Intent Data
        val intent = intent
        routeId = intent.extras?.getString("routeSegments","")?.toInt()!!

        populateRouteSegments()
    }


    private fun populateRouteSegments() {
        val menuRecyclerView: RecyclerView = findViewById(R.id.recyclerItems)
        val manager = GridLayoutManager(this, 1)
        menuRecyclerView.layoutManager = manager

        viewModel.getRouteSegmentsByRouteId(routeId)

        viewModel.routeSegments.observe(this, Observer { segments ->
            val data = segments.map { segment ->
                mapOf(
                    "id" to segment.id.toString(),
                    "navigateTo" to "",
                    "visible" to true,
                    "title" to segment.name,
                    "subTitle" to "${segment.distanceKM} KM",
                    "icon" to R.drawable.appicon_segment
                )
            }

            val menu = mapOf(
                "type" to "list",
                "data" to data
            )

            val adapter = MenuRecyclerViewAdapter(applicationContext, menu)
            menuRecyclerView.adapter = adapter
        })
    }

    override fun onSearchTextReceived(text: String) {
        viewModel.filterRouteSegmentsByRouteId(routeId, text)
    }
}
