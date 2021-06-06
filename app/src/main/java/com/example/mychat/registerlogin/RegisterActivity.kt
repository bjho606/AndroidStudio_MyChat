package com.example.mychat.registerlogin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.mychat.MessageActivity
import com.example.mychat.databinding.ActivityRegisterBinding
import com.example.mychat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class RegisterActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegisterBinding
    var selectedPhotoUri: Uri? = null
    // startActivityForResult가 이걸로 바뀜
    val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result -> binding.photoBtn.setImageURI(result.data?.data)
        selectedPhotoUri = result.data?.data
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init(){
        binding.apply {
            registerBtn.setOnClickListener {
                performRegister()
            }

            loginText.setOnClickListener {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
            }

            photoBtn.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                // startActivityForResult가 이걸로 바뀜
                getContent.launch(intent)
            }
        }
    }

    // 회원 등록
    private fun performRegister() {
        binding.apply {
            val name = nameEdit.text.toString()
            val email = emailEdit.text.toString()
            val password = pwEdit.text.toString()

            Log.d("Main", "Name is: " + name)
            Log.d("Main", "Email is: " + email)
            Log.d("Main", "Password: " + password)

            if(name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this@RegisterActivity, "입력 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                return
            }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if(!it.isSuccessful) {
                        Log.d("Main", "회원 생성 실패!")
                        return@addOnCompleteListener
                    }

                    // 비밀번호는 최소 6자리 이상 입력
                    Toast.makeText(this@RegisterActivity, "회원가입 성공", Toast.LENGTH_SHORT).show()
                    Log.d("Main", "회원 생성 성공 with uid: ${it.result!!.user.uid}")

                    uploadImageToFirebaseStorage()
                }.addOnFailureListener {
                    Toast.makeText(this@RegisterActivity, "회원 생성 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                    Log.d("Main", "회원 생성 실패: ${it.message}")
                }
        }
    }

    private fun uploadImageToFirebaseStorage() {
        if(selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("Register", "이미지 업로드 성공: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("Register", "파일 위치: $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }.addOnFailureListener {

            }
    }

    // 회원 등록한 사람 데이터베이스에 추가
    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, binding.nameEdit.text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("Register", "유저 등록됨")

                val intent = Intent(this@RegisterActivity, MessageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }.addOnFailureListener {
                Log.d("Register", "유저 등록 실패: ${it.message}")
            }
    }
}