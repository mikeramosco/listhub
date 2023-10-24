package com.justanotherdeveloper.listhub;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import static com.justanotherdeveloper.listhub.ConstantsKt.bulletedListRef;
import static com.justanotherdeveloper.listhub.ConstantsKt.progressListRef;
import static com.justanotherdeveloper.listhub.ConstantsKt.routineListRef;
import static com.justanotherdeveloper.listhub.ConstantsKt.toDoListRef;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private List<String> list;
    private Activity activity;
    private Boolean forList;

    public RecyclerAdapter(List<String> list, Activity activity, Boolean forList) {
        this.list = list;
        this.activity = activity;
        this.forList = forList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.row_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        String listItem = list.get(position);
        String[] itemContents = listItem.split("\t");
        if(itemContents.length == 1) {
            holder.textView.setText(listItem);
        } else {
            holder.taskTextSpacer.setVisibility(View.GONE);
            holder.listTypeIcon.setVisibility(View.VISIBLE);
            holder.textView.setText(itemContents[0]);

            String listType = itemContents[1];
            int iconCode = 0;
            switch (listType) {
                case toDoListRef:
                    iconCode = R.drawable.ic_list_white;
                    break;
                case progressListRef:
                    iconCode = R.drawable.ic_view_list_white;
                    break;
                case routineListRef:
                    iconCode = R.drawable.ic_format_list_numbered_white;
                    break;
                case bulletedListRef:
                    iconCode = R.drawable.ic_format_list_bulleted_white;
                    break;
            }
            holder.listTypeIcon.setImageResource(iconCode);
        }

        holder.dragIcon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                ItemTouchHelper itemTouchHelper;

                if(forList) itemTouchHelper = ((ReorderListActivity) activity).getItemTouchHelper();
                else itemTouchHelper = ((ReorderListTitlesActivity) activity).getItemTouchHelper();

                itemTouchHelper.startDrag(holder);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View taskTextSpacer;
        ImageView listTypeIcon, dragIcon;
        TextView textView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            taskTextSpacer = itemView.findViewById(R.id.taskTextSpacer);
            listTypeIcon = itemView.findViewById(R.id.listTypeIcon);
            textView = itemView.findViewById(R.id.textView);
            dragIcon = itemView.findViewById(R.id.dragIcon);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) { }
    }
}
