package com.example.mychat.registerlogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mychat.MessageActivity
import com.example.mychat.databinding.ActivityLoginBinding
import com.example.mychat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity: AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "로그인"
        init()
    }

    private fun init(){
        binding.apply {
            loginBtn.setOnClickListener {
                performLogin()
            }
        }
    }

    // 로그인
    private fun performLogin() {
        binding.apply {
            val email = emailLoginEdit.text.toString()
            val password = pwLoginEdit.text.toString()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val uid = FirebaseAuth.getInstance().uid
                    val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
                    ref.addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            MessageActivity.currentUser = snapshot.getValue(User::class.java)
                            val username = MessageActivity.currentUser?.username
                            Toast.makeText(this@LoginActivity, username + " 님, 환영합니다.", Toast.LENGTH_SHORT).show()
                            Log.d("User", "Current User ${MessageActivity.currentUser?.username}")
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })

                    val intent = Intent(this@LoginActivity, MessageActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this@LoginActivity, "로그인 정보가 잘못되었습니다.", Toast.LENGTH_SHORT).show()
                    emailLoginEdit.text.clear()
                    pwLoginEdit.text.clear()
                }
        }
    }
}