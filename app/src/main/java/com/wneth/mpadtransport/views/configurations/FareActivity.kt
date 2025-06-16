package com.wneth.mpadtransport.views.configurations

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wneth.mpadtransport.R
import com.wneth.mpadtransport.databinding.ActivityFareBinding
import com.wneth.mpadtransport.models.FareSetupModel
import com.wneth.mpadtransport.utilities.amountToPHP
import com.wneth.mpadtransport.utilities.getStateName
import com.wneth.mpadtransport.utilities.recyclerviews.MenuRecyclerViewAdapter
import com.wneth.mpadtransport.viewmodels.configurations.FareActivityViewModel

class FareActivity : AppCompatActivity() {

    private lateinit var viewModel: FareActivityViewModel
    private lateinit var viewBinding: ActivityFareBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[FareActivityViewModel::class.java]
        // =================================


        // Initialize viewBinding
        viewBinding = ActivityFareBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        // =================================

        populateFare()
        populateFareSetups()
    }

    private fun populateFare(){
        viewModel.fare.observe(this) {
            viewBinding.txtBaseDistance.text = "${it.distanceKM} KM"
            viewBinding.txtBaseAmount.text = "${it.baseAmount.amountToPHP()}"
        }
    }

    private fun populateFareSetups(){
        viewModel.fareSetups.observe(this) {
            populateFareSetups(it)
        }
    }

    private fun populateFareSetups(fareSetups: List<FareSetupModel>) {

        val menuRecyclerView: RecyclerView = findViewById(R.id.recyclerItems);
        val manager = GridLayoutManager(this, 1)
        menuRecyclerView.layoutManager = manager

        val data = fareSetups.map { setup ->
            mapOf(
                "id" to setup.id.toString(),
                "navigateTo" to "",
                "visible" to true,
                "title" to "${setup.distanceKM} KM",
                "subTitle" to "${setup.amount.amountToPHP()} \n${getStateName(setup.isActive)}",
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