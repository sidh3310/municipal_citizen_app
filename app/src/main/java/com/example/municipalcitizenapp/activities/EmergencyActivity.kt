package com.example.municipalcitizenapp.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.municipalcitizenapp.R
import com.google.android.material.card.MaterialCardView

class EmergencyActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency)

        findViewById<MaterialCardView>(R.id.cardCallWater).setOnClickListener {
            dialNumber("108")
        }

        findViewById<MaterialCardView>(R.id.cardCallPower).setOnClickListener {
            dialNumber("109")
        }

        findViewById<MaterialCardView>(R.id.cardCallTree).setOnClickListener {
            dialNumber("112")
        }

        findViewById<MaterialCardView>(R.id.cardCallFire).setOnClickListener {
            dialNumber("115")
        }
    }

    private fun dialNumber(number: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$number")
        startActivity(intent)
    }
}
