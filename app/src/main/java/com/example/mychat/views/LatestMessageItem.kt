package com.example.mychat.views

import android.widget.ImageView
import android.widget.TextView
import com.example.mychat.R
import com.example.mychat.models.ChatMessage
import com.example.mychat.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class LatestMessageItem(val chatMessage: ChatMessage): Item<GroupieViewHolder>(){
    var chatPartnerUser: User? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val chatPartnerId: String
        if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
            chatPartnerId = chatMessage.toId
        } else{
            chatPartnerId = chatMessage.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatPartnerUser = snapshot.getValue(User::class.java)
                viewHolder.itemView.findViewById<TextView>(R.id.username_textview).text = chatPartnerUser?.username

                Picasso.get().load(chatPartnerUser?.profileImageUrl).into(viewHolder.itemView.findViewById<ImageView>(R.id.userimage_imageview))
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        viewHolder.itemView.findViewById<TextView>(R.id.latestmessage_textView).text = chatMessage.text
    }

    override fun getLayout(): Int {
        return R.layout.chat_user_row
    }
}