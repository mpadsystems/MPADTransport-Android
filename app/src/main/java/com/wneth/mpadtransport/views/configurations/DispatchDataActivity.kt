package com.wneth.mpadtransport.views.configurations

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wneth.mpadtransport.R
import com.wneth.mpadtransport.databinding.ActivityDispatchDataBinding
import com.wneth.mpadtransport.models.DispatchWithNameModel
import com.wneth.mpadtransport.utilities.formatToHumanDateTime
import com.wneth.mpadtransport.utilities.recyclerviews.MenuRecyclerViewAdapter
import com.wneth.mpadtransport.viewmodels.DispatchActivityViewModel

class DispatchDataActivity : AppCompatActivity() {

    private lateinit var viewModel: DispatchActivityViewModel
    private lateinit var viewBinding: ActivityDispatchDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this)[DispatchActivityViewModel::class.java]
        viewBinding = ActivityDispatchDataBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        populateDispatchData()
    }

    private fun populateDispatchData() {
        viewModel.dispatches.observe(this) {
            populateDispatchData(it)
        }
    }

    private fun populateDispatchData(dispatches: List<DispatchWithNameModel>) {

        val menuRecyclerView: RecyclerView = findViewById(R.id.recyclerItems);
        val manager = GridLayoutManager(this, 1)
        menuRecyclerView.layoutManager = manager

        val data = dispatches.map { dispatch ->
            mapOf(
                "id" to dispatch.dispatchId,
                "navigateTo" to "",
                "visible" to true,
                "title" to dispatch.routeName,
                "subTitle" to
                        "Date:               ${dispatch.dateCreated.toString().formatToHumanDateTime()}\n" +
                        "Device:            ${dispatch.deviceName} \n" +
                        "Terminal:        ${dispatch.terminalName}\n" +
                        "Dispatcher:     ${dispatch.dispatcherName}\n" +
                        "Conductor:     ${dispatch.conductorName}\n" +
                        "Driver:            ${dispatch.driverName}\n" +
                        "Bus:                ${dispatch.busPlateNumber}",
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

}