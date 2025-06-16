package com.wneth.mpadtransport.views.configurations

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wneth.mpadtransport.R
import com.wneth.mpadtransport.databinding.ActivityRouteBinding
import com.wneth.mpadtransport.models.RouteModel
import com.wneth.mpadtransport.repositories.RouteWithSegments
import com.wneth.mpadtransport.utilities.getRouteDirectionName
import com.wneth.mpadtransport.utilities.interfaces.RecyclerDialogEventListener
import com.wneth.mpadtransport.utilities.interfaces.SearchFragmentListener
import com.wneth.mpadtransport.utilities.recyclerviews.MenuRecyclerViewAdapter
import com.wneth.mpadtransport.viewmodels.configurations.RouteActivityViewModel
import com.wneth.mpadtransport.views.fragments.SearchFragment

class RouteActivity : AppCompatActivity(), SearchFragmentListener, RecyclerDialogEventListener {

    // >>Variables Declarations
    /*
    * Code Below
    */

    private lateinit var viewModel: RouteActivityViewModel
    private lateinit var viewBinding: ActivityRouteBinding
    // =================================


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewModel
        /*
        * Code Below
        */
        viewModel = ViewModelProvider(this)[RouteActivityViewModel::class.java]
        // =================================


        // Initialize viewBinding
        /*
        * Code Below
        */
        viewBinding = ActivityRouteBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        // =================================



        // Initialize Fragments
        /*
        * Code Below
        */
        if (savedInstanceState == null) {
            val bundle = bundleOf("some_int" to 0)
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<SearchFragment>(R.id.searchFragment, args = bundle)
            }
        }
        // =================================

        populateRoutes()
    }

    private fun populateRoutes() {
        viewModel.routes.observe(this) {
            populateRoutes(it)
        }
    }

    private fun populateRoutes(routes: List<RouteWithSegments>) {

        val menuRecyclerView: RecyclerView = findViewById(R.id.recyclerItems);
        val manager = GridLayoutManager(this, 1)
        menuRecyclerView.layoutManager = manager


        val data = routes.map { route ->
            mapOf(
                "id" to route.route.id,
                "navigateTo" to "",
                "visible" to true,
                "title" to route.route.name,
                "subTitle" to getRouteDirectionName(route.route.directionId),
                "icon" to R.drawable.appicon_map,
                "data" to route.segments
            )
        }

        val menu = mapOf(
            "type" to "list",
            "data" to data
        )

        val adapter = MenuRecyclerViewAdapter(applicationContext, menu)
        adapter.recyclerViewListener = this
        menuRecyclerView.adapter = adapter
    }

    override fun onSearchTextReceived(text: String) {
        viewModel.filterRoutes(text)
    }

    override fun onRecyclerItemClicked(id: Int, position: Int, title: String, subTitle: String, navigateTo: String, data: Any?) {
        val page = Intent(this, RouteSegmentActivity::class.java)
        page.putExtra("routeSegments", id)
        startActivity(page)

    }

    override fun onRecyclerItemLongClicked(id: Int, position: Int, title: String, subTitle: String, navigateTo: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setTitle("Select Action")
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .setItems(arrayOf("Delete", "Disable", "Enable")) { dialog, which ->
                when (which) {
                    0 -> {
                        val route = RouteModel(id.toInt(), 0, "", 1, true, "")
                        viewModel.deleteRoute(route)
                        Toast.makeText(this, "Deleted", Toast.LENGTH_LONG).show()
                    }
                    1 -> {
                        Toast.makeText(this, "Disabled", Toast.LENGTH_SHORT).show()
                    }
                    2 -> {
                        Toast.makeText(this, "Enabled", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

}