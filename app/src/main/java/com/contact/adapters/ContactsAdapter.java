package com.contact.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.contact.R;
import com.contact.models.ContactInfo;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ContactsAdapter extends ArrayAdapter<ContactInfo> {

    public ContactsAdapter(Context context, List<ContactInfo> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ContactInfo contact = getItem(position);
        ViewHolder viewHolder;
        if (null == convertView) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_contact, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvName.setText(contact.getName());
        /*byte[] photo = contact.getProfileImage();
        if (photo != null){
            Bitmap bitmap = BitmapFactory.decodeByteArray(photo, 0, photo.length);
            viewHolder.ivProfileImage.setImageBitmap(bitmap);
        }*/
        return convertView;
    }

    static class ViewHolder{
        @Bind(R.id.ivProfileImage) ImageView ivProfileImage;
        @Bind(R.id.tvName) TextView tvName;

        public ViewHolder(View v) {
            ButterKnife.bind(this, v);
        }

    }
}
