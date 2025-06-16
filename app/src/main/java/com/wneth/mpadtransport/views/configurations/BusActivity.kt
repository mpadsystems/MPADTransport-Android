package com.wneth.mpadtransport.views.configurations

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
import com.wneth.mpadtransport.databinding.ActivityBusBinding
import com.wneth.mpadtransport.models.BusModel
import com.wneth.mpadtransport.utilities.getBusVentName

import com.wneth.mpadtransport.utilities.interfaces.RecyclerDialogEventListener
import com.wneth.mpadtransport.utilities.interfaces.SearchFragmentListener
import com.wneth.mpadtransport.utilities.recyclerviews.MenuRecyclerViewAdapter
import com.wneth.mpadtransport.viewmodels.configurations.BusActivityViewModel
import com.wneth.mpadtransport.views.fragments.SearchFragment

class BusActivity : AppCompatActivity(), SearchFragmentListener, RecyclerDialogEventListener {

    // >>Variables Declarations
    /*
    * Code Below
    */

    private lateinit var viewModel: BusActivityViewModel
    private lateinit var viewBinding: ActivityBusBinding
    // =================================


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewModel
        /*
        * Code Below
        */
        viewModel = ViewModelProvider(this)[BusActivityViewModel::class.java]
        // =================================


        // Initialize Binding
        /*
        * Code Below
        */
        viewBinding = ActivityBusBinding.inflate(layoutInflater)
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

        populateBuses()
    }


    private fun populateBuses() {
        viewModel.buses.observe(this) {
            populateBuses(it)
        }
    }

    private fun populateBuses(buses: List<BusModel>) {

        val menuRecyclerView: RecyclerView = findViewById(R.id.recyclerItems);
        val manager = GridLayoutManager(this, 1)
        menuRecyclerView.layoutManager = manager

        val data = buses.map { bus ->
            mapOf(
                "id" to bus.id,
                "navigateTo" to "",
                "visible" to true,
                "title" to "Bus: ${bus.busNumber}\nPlate: ${bus.plateNumber}",
                "subTitle" to getBusVentName(bus.airCondition),
                "icon" to R.drawable.appicon_bus
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
        viewModel.filterBuses(text)
    }

    override fun onRecyclerItemClicked(id: Int, position: Int, title: String, subTitle: String, navigateTo: String, data: Any?) {

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
                        val bus = BusModel(id.toInt(), 0, 0, "", true, true,"")
                        viewModel.deleteBus(bus)
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