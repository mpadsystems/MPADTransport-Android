package com.wneth.mpadtransport.views.configurations

import android.content.Intent
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
import com.wneth.mpadtransport.databinding.ActivityEmployeeBinding
import com.wneth.mpadtransport.models.UserWithRole

import com.wneth.mpadtransport.utilities.interfaces.RecyclerDialogEventListener
import com.wneth.mpadtransport.utilities.interfaces.SearchFragmentListener
import com.wneth.mpadtransport.utilities.recyclerviews.MenuRecyclerViewAdapter
import com.wneth.mpadtransport.viewmodels.configurations.EmployeeActivityViewModel
import com.wneth.mpadtransport.views.MPADPayRegistrationActivity
import com.wneth.mpadtransport.views.fragments.SearchFragment


class EmployeeActivity : AppCompatActivity(), SearchFragmentListener, RecyclerDialogEventListener {

    // >>Variables Declarations
    /*
    * Code Below
    */

    private lateinit var viewModel: EmployeeActivityViewModel
    private lateinit var viewBinding: ActivityEmployeeBinding
    // =================================


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()



        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[EmployeeActivityViewModel::class.java]
        // =================================


        // Initialize viewBinding
        viewBinding = ActivityEmployeeBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        // =================================



        // Initialize Fragments
        if (savedInstanceState == null) {
            val bundle = bundleOf("some_int" to 0)
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<SearchFragment>(R.id.searchFragment, args = bundle)
            }
        }
        // =================================




        // Initialize Event Listeners
        /*
        viewBinding.btnAddUser.setOnClickListener {
            goToEmployeeFormPage()
        }
        */
        // =================================


        populateEmployees()
    }


    private fun populateEmployees() {
        viewModel.employees.observe(this) {
            populateEmployees(it)
        }
    }

    private fun populateEmployees(employees: List<UserWithRole>) {

        val menuRecyclerView: RecyclerView = findViewById(R.id.recyclerItems);
        val manager = GridLayoutManager(this, 1)
        menuRecyclerView.layoutManager = manager

        val data = employees.map { employee ->
            val userIcon = if (employee.gender == 1) {
                R.drawable.appicon_usermale
            } else {
                R.drawable.appicon_userfemale
            }
            mapOf(
                "id" to employee.id,
                "navigateTo" to "",
                "visible" to true,
                "title" to employee.fullName,
                "subTitle" to employee.roleName,
                "icon" to userIcon
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


    private fun createNewEmployeePage(userId: Int? = null) {
        startActivity(Intent(this, MPADPayRegistrationActivity::class.java))
    }


    override fun onSearchTextReceived(text: String) {
        viewModel.filterEmployees(text)
    }


    override fun onRecyclerItemClicked(id:Int, position: Int, title: String, subTitle: String, navigateTo: String, data: Any?) {
        val page = Intent(this, MPADPayRegistrationActivity::class.java)
        if (id.toInt() > 0) {
            page.putExtra("userId", id)
        }
        startActivity(page)
    }

    override fun onRecyclerItemLongClicked(id:Int, position: Int, title: String, subTitle: String, navigateTo: String) {
        /*val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setTitle("Select Action")
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .setItems(arrayOf("Delete", "Disable", "Enable")) { dialog, which ->
                when (which) {
                    0 -> {
                        val user = UserModel(id, 0, 0, "", 1, "","","","")
                        viewModel.deleteEmployee(user)
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
        */
    }

}