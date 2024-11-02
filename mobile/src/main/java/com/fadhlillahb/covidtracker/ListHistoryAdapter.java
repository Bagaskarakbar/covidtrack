package com.fadhlillahb.covidtracker;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.widget.TextView;

import com.google.type.DateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class ListHistoryAdapter extends RecyclerView.Adapter<ListHistoryAdapter.ViewHolder> {

    private Context context;
    private List<VitalSignModel> vitalSigns;

    public ListHistoryAdapter(List<VitalSignModel> vitalSigns) {
        this.vitalSigns = vitalSigns;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.itemrow_history, parent, false);
        Log.d("VIEWTRIGGERED", "HISTORY ADAPTER");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VitalSignModel vitalsign = vitalSigns.get(position);
        Date timestamp = vitalsign.getTimestamp();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        holder.txTimestamp.setText(sdf.format(timestamp));
    }

    @Override
    public int getItemCount() {
        return vitalSigns.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txTimestamp = itemView.findViewById(R.id.txTimestamp);
        }
    }

    public interface ClickListener{
        void onItemClick(int position, View v);
        void onItemLongClick(int position, View v);
    }

    public static class RecylerTouchListener implements RecyclerView.OnItemTouchListener{

        private GestureDetector gestureDetector;
        private ListHistoryAdapter.ClickListener clickListener;

        public RecylerTouchListener(final Context context, final RecyclerView recyclerView, final ListHistoryAdapter.ClickListener clickListener){
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.OnGestureListener() {

                @Override
                public boolean onDown(@NonNull MotionEvent e) {
                    return false;
                }

                @Override
                public void onShowPress(@NonNull MotionEvent e) {

                }

                @Override
                public boolean onSingleTapUp(@NonNull MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
                    return false;
                }

                @Override
                public void onLongPress(@NonNull MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null){
                        clickListener.onItemLongClick(recyclerView.getChildAdapterPosition(child), child);
                    }
                }

                @Override
                public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                    return false;
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)){
                clickListener.onItemClick(rv.getChildAdapterPosition(child), child);
            }
            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}
