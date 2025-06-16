package com.wneth.mpadtransport.views.configurations

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wneth.mpadtransport.R
import com.wneth.mpadtransport.databinding.ActivityTicketReceiptDataBinding
import com.wneth.mpadtransport.utilities.amountToPHP
import com.wneth.mpadtransport.utilities.dialogs.DialogRecyclerItems
import com.wneth.mpadtransport.utilities.formatToHumanDateTime
import com.wneth.mpadtransport.utilities.interfaces.RecyclerDialogEventListener
import com.wneth.mpadtransport.utilities.interfaces.SearchFragmentListener
import com.wneth.mpadtransport.utilities.recyclerviews.MenuRecyclerViewAdapter
import com.wneth.mpadtransport.utilities.reverseStringParts
import com.wneth.mpadtransport.viewmodels.TicketReceiptActivityViewModel

class TicketReceiptDataActivity : AppCompatActivity(), SearchFragmentListener,
    RecyclerDialogEventListener {

    private lateinit var viewModel: TicketReceiptActivityViewModel
    private lateinit var viewBinding: ActivityTicketReceiptDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[TicketReceiptActivityViewModel::class.java]
        // =================================


        // Initialize viewBinding
        /*
        * Code Below
        */
        viewBinding = ActivityTicketReceiptDataBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        // =================================

        populateDispatchTicketReceipts()
    }

    private fun populateDispatchTicketReceipts(){
        val dispatchId = viewModel.sharedPrefs.getInt("sharedDeviceLastDispatchId",0)
        val allTripReceipt = viewModel.getDispatchTripReceiptGroup(dispatchId)

        val menuRecyclerView: RecyclerView = findViewById(R.id.recyclerItems);
        val manager = GridLayoutManager(this, 1)
        menuRecyclerView.layoutManager = manager

        val data: List<Map<String, Any?>> = if (allTripReceipt.isEmpty()) {
            listOf(
                mapOf(
                    "id" to "",
                    "navigateTo" to "",
                    "visible" to true,
                    "title" to "Oops! No Data Found",
                    "subTitle" to "No tickets issued yet",
                    "icon" to R.drawable.appicon_carcrash
                )
            )
        } else {
            allTripReceipt.map { trip ->

                val routeName = if (trip.routeDirectionId == 1){
                    trip.routeName ?: ""
                }else{
                    reverseStringParts(trip.routeName ?: "")
                }

                mapOf(
                    "id" to trip.dispatchTripReferenceId,
                    "navigateTo" to "",
                    "visible" to true,
                    "title" to routeName,
                    "subTitle" to
                            "Date:                 ${trip.dateCreated.formatToHumanDateTime()}\n" +
                            "Amount:           ${trip.ticketTotalAmount.amountToPHP()}\n" +
                            "Issued Tickets:  ${trip.ticketCount}\n",
                    "icon" to null
                )
            }
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
        //
    }


    override fun onRecyclerItemClicked(
        id: Int,
        position: Int,
        title: String,
        subTitle: String,
        navigateTo: String,
        data: Any?
    ) {
        val dialog = DialogRecyclerItems.newInstance(6, id)
        dialog.show(supportFragmentManager, "DialogForm")
        dialog.recyclerEventListener = this
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