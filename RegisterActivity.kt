package com.example.kotlinchatapp

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_button_register.setOnClickListener {
        performRegister()
        }

        already_have_account_textview.setOnClickListener {
            Log.d("RegisterActivity", "Try to show login activity")

            //launch the login activity
            val intent = Intent(this, LoginActivity::class.java) // the actual activity class
             startActivity(intent)
        }
        select_photo_button_register.setOnClickListener{
            Log.d("RegisterActivity", "Try to show photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)

        }
    }

            var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data!= null){
            //what img was selected
            Log.d("RegisterActivity", "Photo was selected")

            selectedPhotoUri = data.data
            //uri este locatia

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            selectphoto_imageview_register.setImageBitmap(bitmap)

            select_photo_button_register.alpha = 0f // hidden button

           // val bitmapDrawable = BitmapDrawable(bitmap)
           // select_photo_button_register.setBackgroundDrawable(bitmapDrawable)
        }
    }


    private fun performRegister() {
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        if (email.isEmpty() || password.isEmpty()) {

            Toast.makeText(this, "Please enter email/password", Toast.LENGTH_SHORT).show()
            return
        }

        //val username = username_edittext_register.toString()
        //Log.d("RegisterActivity","Email is:", + email)

        Log.d("RegisterActivity", "Email is " + email)
        Log.d("RegisterActivity", "Password: $password")

        //Firebase Authentication to create user and pass

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    Log.w("createUserWithEmail:failure", it.exception)
                    return@addOnCompleteListener
                }

                    //else if successful
                Log.d("Main", "Succesfully created user with uid: ${it.result?.user?.uid}")

                if (selectedPhotoUri == null){
                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                else {
                    uploadImagetoFirebaseStorage()
                }
            }
            .addOnFailureListener {
                    Log.d("Main", "Fail to create user: ${it.message}")
                    Toast.makeText(this, "Please enter email/password", Toast.LENGTH_SHORT).show()
                    }


    }

    //Firebase Storage
    private fun  uploadImagetoFirebaseStorage(){
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Succesfully uploaded image: ${it.metadata?.path}")

                //Acces to file location

                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Log.d("RegisterActivity","File location $it")
                    //salvarea users in DB
                    saveUserToFirebaseDatabase(it.toString())

                }
            }
            .addOnFailureListener{
                //logging
                Log.d("", "Fail to upload img to storage: ${it.message}")            }

    }

    //DataBase Storage
    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid?:""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username_edittext_register.text.toString(), profileImageUrl )

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity","Finally we saved the user to Firebase database")

                //for activity launch
                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener{
                Log.d("RegisterActivity", "Failed to set value to d: ${it.message}")
            }
    }
}

