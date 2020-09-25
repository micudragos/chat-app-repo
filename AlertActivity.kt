package com.example.kotlinchatapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_alert.*

class AlertActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert)

        var currentUserName: String? = ""
        val uid = FirebaseAuth.getInstance().uid?:""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                var currentUser = p0.getValue(User::class.java)
                currentUserName = currentUser?.username
                alertTxt.text = "ATENTIE: Angajatul cu numele ${currentUserName?.toString()} a cazut sau nu poarta echipamentul de protectie."
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}