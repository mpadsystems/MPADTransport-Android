package com.wneth.mpadtransport.views.configurations

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.wneth.mpadtransport.R
import com.wneth.mpadtransport.databinding.ActivityDeductionBinding
import com.wneth.mpadtransport.models.DeductionModel
import com.wneth.mpadtransport.models.IngressoDeductionWithNameModel
import com.wneth.mpadtransport.utilities.amountToPHP
import com.wneth.mpadtransport.utilities.dpToPx
import com.wneth.mpadtransport.utilities.getDeductionTypeName
import com.wneth.mpadtransport.utilities.interfaces.RecyclerDialogEventListener
import com.wneth.mpadtransport.utilities.recyclerviews.MenuRecyclerViewAdapter
import com.wneth.mpadtransport.utilities.writeLogToSDCard
import com.wneth.mpadtransport.viewmodels.configurations.IngressoActivityViewModel
import java.util.UUID


class DeductionActivity : AppCompatActivity(), RecyclerDialogEventListener {

    private lateinit var viewModel: IngressoActivityViewModel
    private lateinit var viewBinding: ActivityDeductionBinding
    private lateinit var deductionListView: ListView
    private var deductionType: Int = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this)[IngressoActivityViewModel::class.java]
        viewBinding = ActivityDeductionBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        deductionListView = findViewById(R.id.deductionList)

        // >>Initialize Intent Data
        val intent = intent
        deductionType = intent.getIntExtra("deductionType", 0)
        if (deductionType > 0) {
            getDeductionData()
        }
        if (deductionType == 1){
            viewBinding.dataHeader.text = "TRIP EXPENSES"
        }else{
            viewBinding.dataHeader.text = "TRIP WITHHOLDING"
        }
        populateDeductions()
    }

    private fun getDeductionData(){
        try{
        if (deductionType == 1){
            val deductions = viewModel.getDeductionExpenses()
            populateDeductions(deductions)
        }
        if (deductionType == 2){
            val deductions = viewModel.getDeductionWithHoldings()
            populateDeductions(deductions)
        }
        }catch (e: Exception){
            writeLogToSDCard(this, e.message.toString())
Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun populateDeductions(deductions: List<DeductionModel>?) {
        try {
        val menuRecyclerView: RecyclerView = findViewById(R.id.recyclerItems);
        val manager = GridLayoutManager(this, 1)
        menuRecyclerView.layoutManager = manager

        val data = deductions?.map { deduction ->
            mapOf(
                "id" to deduction.id,
                "navigateTo" to "",
                "visible" to true,
                "title" to deduction.name,
                "subTitle" to getDeductionTypeName(deduction.deductionType),
                "icon" to null,
                "data" to deduction
            )
        }

        val menu = mapOf(
            "type" to "list",
            "data" to data
        )

        val adapter = MenuRecyclerViewAdapter(applicationContext, menu)
        adapter.recyclerViewListener = this
        menuRecyclerView.adapter = adapter
        }catch (e: Exception){
            writeLogToSDCard(this, e.message.toString())
Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRecyclerItemClicked(
        id: Int,
        position: Int,
        title: String,
        subTitle: String,
        navigateTo: String,
        data: Any?
    ) {
        showAmountDialog(title, subTitle, data)
    }

    override fun onRecyclerItemLongClicked(
        id: Int,
        position: Int,
        title: String,
        subTitle: String,
        navigateTo: String
    ) {
        // To be implemented
    }


    private fun showAmountDialog(title: String, subTitle: String, data: Any?) {
        try{
        val textInputLayout = TextInputLayout(this)

        textInputLayout.setPadding(
            20.dpToPx(this),
            0.dpToPx(this),
            20.dpToPx(this),
            0.dpToPx(this)
        )

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.textSize = 20f
        input.setText("0.00")
        input.textAlignment = EditText.TEXT_ALIGNMENT_CENTER
        input.setBackgroundResource(R.drawable.app_input_style)  // Apply the custom style
        textInputLayout.addView(input)

        val alert = AlertDialog.Builder(this)
            .setTitle(subTitle)
            .setView(textInputLayout)
            .setMessage("Please enter $title amount")
            .setPositiveButton("OK") { dialog, _ ->
                val updatedList = IngressoActivity.deductionItems.value?.toMutableList() ?: mutableListOf()
                if (data is DeductionModel) {
                    updatedList.add(
                        IngressoDeductionWithNameModel(
                            id = UUID.randomUUID().leastSignificantBits,
                            ingressoReferenceId = 0,
                            deductionId = data.id,
                            deductionType = data.deductionType,
                            deductionName = data.name,
                            amount = input.text.toString().toDouble(),
                            autoCompute = false,
                            computeAfter = data.computeAfter
                        )
                    )
                    IngressoActivity.deductionItems.value = updatedList
                }
                dialog.dismiss()
                populateDeductions()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }.create()

        alert.show()
        }catch (e: Exception){
            writeLogToSDCard(this, e.message.toString())
Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun populateDeductions() {
        try{
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, mutableListOf())
        deductionListView.adapter = adapter

        // Observe LiveData from the ViewModel
        IngressoActivity.deductionItems.observe(this, Observer { deductionList ->
            if (deductionList != null) {
                val data = deductionList.filter {
                    it.deductionType == deductionType
                }.map {
                    it.id to "${it.deductionName}: ${it.amount.amountToPHP()}"
                }

                (adapter as ArrayAdapter<String>).clear()
                (adapter as ArrayAdapter<String>).addAll(data.map { it.second })
                (adapter as ArrayAdapter<String>).notifyDataSetChanged()

                deductionListView.setOnItemClickListener { _, _, position, _ ->
                    val itemId = data[position].first
                    AlertDialog.Builder(this)
                        .setTitle("${data[position].second}")
                        .setMessage("Are you sure you want to remove this item?")
                        .setPositiveButton("Yes") { dialog, _ ->
                            removeDeduction(itemId)
                        }
                        .setNegativeButton("No", null)
                        .show()
                }
            }
        })
        }catch (e: Exception){
            writeLogToSDCard(this, e.message.toString())
Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun removeDeduction(id: Long) {
        try{
        IngressoActivity.deductionItems.value = IngressoActivity.deductionItems.value?.toMutableList()?.also { list ->
            val itemToRemove = list.find { it.id == id }
            itemToRemove?.let { list.remove(it) }
        }
        }catch (e: Exception){
            writeLogToSDCard(this, e.message.toString())
Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }



}