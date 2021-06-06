package com.example.mychat.messages

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mychat.MessageActivity
import com.example.mychat.R
import com.example.mychat.databinding.ActivityChatBinding
import com.example.mychat.models.ChatMessage
import com.example.mychat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class ChatActivity : AppCompatActivity() {
    companion object {
        val TAG = "Chat Log"
    }

    lateinit var binding: ActivityChatBinding

    val adapter = GroupAdapter<GroupieViewHolder>()

    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init(){
        binding.apply {
            recyclerviewChat.adapter = adapter

//            val username = intent.getStringExtra(MessageActivity.USER_KEY)
            toUser = intent.getParcelableExtra<User>(MessageActivity.USER_KEY)
            supportActionBar?.title = toUser?.username

//            setDummyData()
            getPrevMessages()

            sendChatBtn.setOnClickListener {
                Log.d(TAG, "Attempt to send message")
                sendMessage()
                chatEdittext.text.clear()
            }
        }
    }

    private fun getPrevMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                if (chatMessage != null){
    //                Log.d(TAG, chatMessage.text)

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        val currentUser = MessageActivity.currentUser
                        adapter.add(ChatFromItem(chatMessage.text, currentUser ?: return))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }
                }

                binding.recyclerviewChat.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    // Firebase로 메시지 보내기
    private fun sendMessage() {
        binding.apply {
            val text = chatEdittext.text.toString()

            val fromId = FirebaseAuth.getInstance().uid
            if (fromId == null) return

            val user = intent.getParcelableExtra<User>(MessageActivity.USER_KEY)
            val toId = user!!.uid

//            val ref = FirebaseDatabase.getInstance().getReference("/messages").push()
            val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

            val toRef = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

            val chatMessage = ChatMessage(ref.key!!, text, fromId, toId, System.currentTimeMillis()/1000)

            ref.setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d(TAG, "Saved our chat message: ${ref.key}")
                    chatEdittext.text.clear()
                    recyclerviewChat.scrollToPosition(adapter.itemCount - 1)
                }
            toRef.setValue(chatMessage)

            val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
            latestMessageRef.setValue(chatMessage)
            val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
            latestMessageToRef.setValue(chatMessage)
        }
    }

    // [임시]로 만든 채팅 더미 데이터
//    private fun setDummyData() {
//        val adapter = GroupAdapter<GroupieViewHolder>()
//        adapter.add(ChatFromItem("From Message......"))
//        adapter.add(ChatToItem("To Message\nTo Message..."))
//
//        binding.recyclerviewChat.adapter = adapter
//    }
}

class ChatFromItem(val text: String, val user: User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.from_textview).text = text

        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<ImageView>(R.id.from_imageview))
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.to_textview).text = text

        // 사용자 프로필 사진 보여주기
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<ImageView>(R.id.to_imageview))
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}