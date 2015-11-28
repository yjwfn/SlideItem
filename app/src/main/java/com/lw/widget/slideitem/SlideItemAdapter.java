package com.lw.widget.slideitem;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by yjwfn on 15-11-28.
 */
public class SlideItemAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemSlideHelper.Callback{

    private RecyclerView mRecyclerView;

    private SparseBooleanArray  mItemState = new SparseBooleanArray();
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slide, parent, false);
        return new TextVH(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String text  = "item: " + position;
        TextVH textVH = (TextVH) holder;
        textVH.textView.setText(text);

        boolean expand = mItemState.get(position);
        if(expand){
            ItemSlideHelper.expand(holder, getHorizontalRange(holder));
        }else{
            ItemSlideHelper.collapse(holder);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
        mRecyclerView.addOnItemTouchListener(new ItemSlideHelper(mRecyclerView.getContext(), this));
    }

    @Override
    public int getItemCount() {
        return 20;
    }

    @Override
    public void onCollapsed(RecyclerView.ViewHolder holder) {
        mItemState.put(holder.getAdapterPosition(), false);
        Log.d("slide", "onCollapsed");
    }

    @Override
    public void onExpanded(RecyclerView.ViewHolder holder) {
        mItemState.put(holder.getAdapterPosition(), true);
        Log.d("slide", "onExpanded");
    }

    @Override
    public int getHorizontalRange(RecyclerView.ViewHolder holder) {

        if(holder.itemView instanceof LinearLayout){
            ViewGroup viewGroup = (ViewGroup) holder.itemView;
            if(viewGroup.getChildCount() == 2){
                return viewGroup.getChildAt(1).getLayoutParams().width;
            }
        }


        return 0;
    }

    @Override
    public RecyclerView.ViewHolder getChildViewHolder(View childView) {
        return mRecyclerView.getChildViewHolder(childView);
    }

    @Override
    public View findTargetView(float x, float y) {
        return mRecyclerView.findChildViewUnder(x, y);
    }

    @Override
    public boolean isEnable() {
        return mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE;
    }
}

class TextVH extends RecyclerView.ViewHolder{

    TextView    textView;


    public TextVH(View itemView) {
        super(itemView);

        textView = (TextView) itemView.findViewById(R.id.tv_text);
    }
}