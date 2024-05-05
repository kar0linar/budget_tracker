package com.example.test2

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.test2.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.example.test2.TransactionDao
import com.google.android.material.snackbar.Snackbar

class MainActivity : ComponentActivity() {

    private lateinit var deletedTransaction: Transaction
    private lateinit var transactions : List<Transaction>
    private lateinit var oldTransactions : List<Transaction>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var binding: ActivityMainBinding
    private lateinit var db : AppDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactions = arrayListOf()

        transactionAdapter = TransactionAdapter(transactions)
        linearLayoutManager = LinearLayoutManager(this)

        db = Room.databaseBuilder(this, AppDatabase::class.java,"transactions").build()

        binding.recyclerview.apply {
            adapter = transactionAdapter
            layoutManager = linearLayoutManager
        }

//swipe to delete
        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteTransaction(transactions[viewHolder.adapterPosition])
            }

        }

        val swipeHelper = ItemTouchHelper(itemTouchHelper)
        swipeHelper.attachToRecyclerView(binding.recyclerview)

        binding.addBtn.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }


    private fun fetchAll(){

        GlobalScope.launch {

            transactions = db.transactionDao().getAll()

            runOnUiThread {
                updateDashboard()
                transactionAdapter.setData(transactions)
            }
        }
    }
    private fun updateDashboard(){
        val totalAmount = transactions.map { it.amount}.sum()
        val budgetAmount = transactions.filter{it.amount>0}.map{it.amount}.sum()
        val expenseAmount = totalAmount - budgetAmount

        binding.balance.text = "%.2f".format(totalAmount) + " zŁ"
        binding.budget.text = "%.2f".format(budgetAmount) + " zŁ"
        binding.expense.text = "%.2f".format(expenseAmount) + " zŁ"

    }


    private fun undoDelete(){
        GlobalScope.launch {
            db.transactionDao().insertAll(deletedTransaction)
            transactions = oldTransactions

            runOnUiThread{
                transactionAdapter.setData(transactions)
                updateDashboard()
            }
        }
    }
    private fun showSnackbar() {
        val view = findViewById<View>(R.id.coordinator)
        val snackbar = Snackbar.make(view, "transakcja usunięta!", Snackbar.LENGTH_LONG)
        snackbar.setAction("przywróć"){
            undoDelete()
        }
            .setActionTextColor(ContextCompat.getColor(this, R.color.red))
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .show()
    }
    private fun deleteTransaction(transaction: Transaction){
        deletedTransaction = transaction
        oldTransactions = transactions

        GlobalScope.launch {  db.transactionDao().delete(transaction)
            transactions = transactions.filter { it.id != transaction.id }
            runOnUiThread {
                updateDashboard()
                transactionAdapter.setData(transactions)
                showSnackbar()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        fetchAll()
    }
}
