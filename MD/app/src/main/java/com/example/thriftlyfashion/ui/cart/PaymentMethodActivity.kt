package com.example.thriftlyfashion.ui.cart

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.thriftlyfashion.PaymentMethod
import com.example.thriftlyfashion.R
import com.example.thriftlyfashion.ui.payment.PaymentMethodAdapter

class PaymentMethodActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_payment_method)
        val btnBack: ImageView = findViewById(R.id.id_btnBack)

        val isConnected: RecyclerView = findViewById(R.id.isConnected)
        val paymentMethodsIsConnected = listOf(
            PaymentMethod(R.drawable.logo_ovo, "OVO"),
            PaymentMethod(R.drawable.logo_linkaja, "linkAja!"),
            PaymentMethod(R.drawable.logo_mastercard, "Mastercard**46")
        )

        isConnected.adapter = PaymentMethodAdapter(paymentMethodsIsConnected) { paymentMethod ->
            Toast.makeText(this, "Selected: ${paymentMethod.name}", Toast.LENGTH_SHORT).show()
        }
        isConnected.layoutManager = LinearLayoutManager(this)

        val addMethod: RecyclerView = findViewById(R.id.addMethod)
        val paymentMethodsAddMethod = listOf(
            PaymentMethod(R.drawable.logo_shopeepay, "ShopeePay"),
            PaymentMethod(R.drawable.logo_gopay, "GoPay"),
        )

        addMethod.adapter = PaymentMethodAdapter(paymentMethodsAddMethod) { paymentMethod ->
            Toast.makeText(this, "Selected: ${paymentMethod.name}", Toast.LENGTH_SHORT).show()
        }
        addMethod.layoutManager = LinearLayoutManager(this)

        btnBack.setOnClickListener {
            finish()
        }
    }
}