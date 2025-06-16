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
import com.wneth.mpadtransport.databinding.ActivityTerminalBinding
import com.wneth.mpadtransport.models.TerminalModel
import com.wneth.mpadtransport.utilities.interfaces.RecyclerDialogEventListener
import com.wneth.mpadtransport.utilities.interfaces.SearchFragmentListener
import com.wneth.mpadtransport.utilities.recyclerviews.MenuRecyclerViewAdapter
import com.wneth.mpadtransport.viewmodels.configurations.TerminalActivityViewModel
import com.wneth.mpadtransport.views.fragments.SearchFragment

class TerminalActivity : AppCompatActivity(), SearchFragmentListener, RecyclerDialogEventListener {

    // >>Variables Declarations
    private lateinit var viewModel: TerminalActivityViewModel
    private lateinit var viewBinding: ActivityTerminalBinding
    // =================================


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewModel
        /*
        * Code Below
        */
        viewModel = ViewModelProvider(this)[TerminalActivityViewModel::class.java]
        // =================================


        // Initialize viewBinding
        /*
        * Code Below
        */
        viewBinding = ActivityTerminalBinding.inflate(layoutInflater)
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

        populateTerminals()
    }

    private fun populateTerminals() {
        viewModel.terminals.observe(this) {
            populateTerminals(it)
        }
    }

    private fun populateTerminals(terminals: List<TerminalModel>) {

        val menuRecyclerView: RecyclerView = findViewById(R.id.recyclerItems);
        val manager = GridLayoutManager(this, 1)
        menuRecyclerView.layoutManager = manager

        val data = terminals.map { terminal ->
            mapOf(
                "id" to terminal.id,
                "navigateTo" to "",
                "visible" to true,
                "title" to terminal.name,
                "subTitle" to terminal.address,
                "icon" to R.drawable.appicon_terminal
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
        viewModel.filterTerminals(text)
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
                        val terminal = TerminalModel(id.toInt(), 0, "", "", true, "")
                        viewModel.deleteTerminal(terminal)
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