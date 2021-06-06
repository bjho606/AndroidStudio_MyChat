package com.example.mychat.views

import android.widget.ImageView
import android.widget.TextView
import com.example.mychat.R
import com.example.mychat.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class UserItem(val user: User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.username_textview).text = user.username

        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<ImageView>(R.id.userimage_imageview))
    }

    override fun getLayout(): Int {
        return R.layout.chat_user_row
    }
}
