package com.example.test2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test2.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {

    private lateinit var transactions : ArrayList<Transaction>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactions = arrayListOf(
            Transaction("Weekend Budget", 400.00),
            Transaction("Bananas", -4.00),
            Transaction("Movie ticket", -20.00),
            Transaction("Dinner", -50.00),
            Transaction("Gas", -30.00),
            Transaction("Coffee", -5.00),
            Transaction("Groceries", -100.00),
            Transaction("Shopping", -80.00),
            Transaction("Books", -15.00),
            Transaction("Gym", -50.00),
        )

        transactionAdapter = TransactionAdapter(transactions)
        linearLayoutManager = LinearLayoutManager(this)

        binding.recyclerview.apply {
            adapter = transactionAdapter
            layoutManager = linearLayoutManager
        }

        updateDashboard()
    }

    private fun updateDashboard(){
        val totalAmount = transactions.map { it.amount}.sum()
        val budgetAmount = transactions.filter{it.amount>0}.map{it.amount}.sum()
        val expenseAmount = totalAmount - budgetAmount

        binding.balance.text = "$ %.2f".format(totalAmount)
        binding.budget.text = "$ %.2f".format(budgetAmount)
        binding.expense.text = "$ %.2f".format(expenseAmount)

    }
}
