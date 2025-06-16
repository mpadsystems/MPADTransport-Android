package com.wneth.mpadtransport.views.configurations

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wneth.mpadtransport.R
import com.wneth.mpadtransport.databinding.ActivityIncentiveBinding
import com.wneth.mpadtransport.utilities.interfaces.RecyclerDialogEventListener
import com.wneth.mpadtransport.utilities.recyclerviews.MenuRecyclerViewAdapter
import com.wneth.mpadtransport.viewmodels.configurations.IncentiveActivityViewModel

class IncentiveActivity : AppCompatActivity(), RecyclerDialogEventListener {

    private lateinit var viewModel: IncentiveActivityViewModel
    private lateinit var viewBinding: ActivityIncentiveBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this).get(IncentiveActivityViewModel::class.java)
        viewBinding = ActivityIncentiveBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        populateCommissions()
    }

    private fun populateCommissions() {

        val commissions = viewModel.getCommissions();

        val menuRecyclerView: RecyclerView = findViewById(R.id.recyclerItems);
        val manager = GridLayoutManager(this, 1)
        menuRecyclerView.layoutManager = manager

        val data = commissions?.map { commission ->
            mapOf(
                "id" to commission.id,
                "navigateTo" to "",
                "visible" to true,
                "title" to commission.type,
                "subTitle" to "${commission.fraction}%",
                "icon" to null
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

    override fun onRecyclerItemClicked(
        id: Int,
        position: Int,
        title: String,
        subTitle: String,
        navigateTo: String,
        data: Any?
    ) {

    }

    override fun onRecyclerItemLongClicked(
        id: Int,
        position: Int,
        title: String,
        subTitle: String,
        navigateTo: String
    ) {

    }

}