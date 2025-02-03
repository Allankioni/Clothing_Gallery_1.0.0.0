package com.example.test3.Groups;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.test3.R;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    private List<Group> groups;
    private Context context;

    public GroupAdapter(Context context) {
        this.context = context;
        this.groups = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Group group = groups.get(position);

        holder.groupNameTextView.setText(group.getName());
        if (!group.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(group.getImageUrl())
                    .placeholder(R.drawable.place_holder) // Placeholder image
                    .into(holder.groupImageView);
        } else {
            holder.groupImageView.setImageResource(R.drawable.place_holder);
        }
        // Implement click listener for group selection here
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(v, position);
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public void setGroups(List<String> groups) {

        notifyDataSetChanged();
    }

    public List<Group> getGroups() {
        return groups;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView groupImageView;
        TextView groupNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupImageView = itemView.findViewById(R.id.category_image);
            groupNameTextView = itemView.findViewById(R.id.category_name);
        }
    }
}
