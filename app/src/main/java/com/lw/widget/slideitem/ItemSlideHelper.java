package com.lw.widget.slideitem;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Created by yjwfn on 15-11-26.
 * 帮助显示左滑菜单
 */
public class ItemSlideHelper implements RecyclerView.OnItemTouchListener, GestureDetector.OnGestureListener {


    private static final String TAG = "ItemSwipeHelper";


    private final int DEFAULT_DURATION = 200;

    private View mTargetView;

    private int mActivePointerId;

    private int mTouchSlop;
    private int mMaxVelocity;
    private int mMinVelocity;
    private int mLastX;
    private int mLastY;


    private boolean mIsDragging;

    private Animator mExpandAndCollapseAnim;

    private GestureDetectorCompat mGestureDetector;

    private Callback mCallback;


    public ItemSlideHelper(Context context, Callback callback) {
        this.mCallback = callback;

        //手势用于处理fling
        mGestureDetector = new GestureDetectorCompat(context, this);

        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaxVelocity = configuration.getScaledMaximumFlingVelocity();
        mMinVelocity = configuration.getScaledMinimumFlingVelocity();
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

        if(!mCallback.isEnable())
            return false;

        int action =  MotionEventCompat.getActionMasked(e);
        int x = (int) e.getX();
        int y = (int) e.getY();

        /*
        * 当我们没有发生drag事件的时候cancel或up事件会发生interceptTouchEvent里面，如果TargetView等于空的时候直接
        * 返回false,不拦截事件
        * */
        if(action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP)
            if(mTargetView == null)
                return false;

        boolean needIntercept = false;
        switch (action) {
            case MotionEvent.ACTION_DOWN:


                mActivePointerId = MotionEventCompat.getPointerId(e, 0);
                mLastX = (int) e.getX();
                mLastY = (int) e.getY();

                //查找需要显示菜单的view;
                mTargetView = mCallback.findTargetView(x, y);


                /*
                * 如果正在动画则拦截事件,并取消动画
                * */
                if (mExpandAndCollapseAnim != null) {
                    //mExpandAndCollapseAnim.cancel();
                    mExpandAndCollapseAnim = null;
                    needIntercept = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:

                int deltaX = (x - mLastX);
                int deltaY = (y - mLastY);

                if(Math.abs(deltaY) > Math.abs(deltaX))
                    return false;

                //如果移动距离达到要求，则拦截
                needIntercept = mIsDragging = mTargetView != null && Math.abs(deltaX) >= mTouchSlop;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                /*
                * 当一个up事件发生在正常的范围内且scrollX等于scrollRange则折叠view并拦截UP事件
                * 防止view响应点击事件
                * */
                if(isExpanded()){

                    if (inView(x, y)) {
                        //拦截事件,防止targetView执行onClick事件
                        needIntercept = true;
                    }else{
                        //如果走这那行这个ACTION_UP的事件会发生在右侧的菜单中
                    }

                    //折叠菜单
                    mTargetView.setScrollX(0);
                }
                dispatchCollapsedOrExpanded();
                break;
        }

        return  needIntercept && mTargetView != null;
    }



    private boolean isExpanded() {
        int scrollX = mTargetView.getScrollX();
        return scrollX == getHorizontalRange();
    }

    private boolean isCollapsed() {
        int scrollX = mTargetView.getScrollX();
        return scrollX == 0;
    }

    /*
    * 根据targetView的scrollX计算出targetView的偏移，这样能够知道这个point
    * 是在右侧的菜单中
    * */
    private boolean inView(int x, int y) {

        if (mTargetView == null)
            return false;

        int scrollX = mTargetView.getScrollX();
        int left = mTargetView.getLeft() - scrollX;
        int top = mTargetView.getTop();
        int right = left + mTargetView.getWidth() ;
        int bottom = mTargetView.getBottom();
        Rect rect = new Rect(left, top, right, bottom);
        return rect.contains(x, y);
    }



    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        //如果要响应fling事件设置将mIsDragging设为false
        if (mGestureDetector.onTouchEvent(e)) {
            mIsDragging = false;
            return;
        }


        int x = (int) e.getX();
        int y = (int) e.getY();
        int action =  MotionEventCompat.getActionMasked(e);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //RecyclerView 不会转发这个Down事件

                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) (mLastX - e.getX());
                horizontalDrag(deltaX);
                mLastX = x;
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                 if(mIsDragging){
                    smoothHorizontalExpandOrCollapse(0);
                }

                dispatchCollapsedOrExpanded();
                mIsDragging = false;

                break;
        }


    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    /**
     *
     * 根据touch事件来滚动View的scrollX
     *
     * @param delta
     */
    private void horizontalDrag(int delta) {
        int scrollX = mTargetView.getScrollX();
        int scrollY = mTargetView.getScrollY();
        if ((scrollX + delta) <= 0) {
            mTargetView.scrollTo(0, scrollY);
            return;
        }


        int horRange = getHorizontalRange();
        scrollX += delta;
        if (Math.abs(scrollX) < horRange) {
            mTargetView.scrollTo(scrollX, scrollY);
        } else {
            mTargetView.scrollTo(horRange, scrollY);
        }


    }


    /**
     * 根据当前scrollX的位置判断是展开还是折叠
     *
     * @param velocityX
     *  如果不等于0那么这是一次fling事件,否则是一次ACTION_UP或者ACTION_CANCEL
     */
    private void smoothHorizontalExpandOrCollapse(float velocityX) {


        int scrollX = mTargetView.getScrollX();
        int scrollRange = getHorizontalRange();

        if (mExpandAndCollapseAnim != null)
            return;


        int to = 0;
        int duration = DEFAULT_DURATION;

        if (velocityX == 0) {
            //如果已经展一半，平滑展开
            if (scrollX > scrollRange / 2) {
                to = scrollRange;
            }
        } else {


            if (velocityX > 0)
                to = 0;
            else
                to = scrollRange;

            duration = (int) ((1.f - Math.abs(velocityX) / mMaxVelocity) * DEFAULT_DURATION);
        }

        mExpandAndCollapseAnim = ObjectAnimator.ofInt(mTargetView, "scrollX", to);
        mExpandAndCollapseAnim.setDuration(duration);
        mExpandAndCollapseAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mExpandAndCollapseAnim = null;


                dispatchCollapsedOrExpanded();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mExpandAndCollapseAnim.start();
    }


    /**
     * 发送事件
     */
    private void dispatchCollapsedOrExpanded() {

        if (mCallback == null)
            return;

        RecyclerView.ViewHolder viewHolder = mCallback.getChildViewHolder(mTargetView);
        if (isCollapsed()) {
            mCallback.onCollapsed(viewHolder);
        } else if (isExpanded()) {
            mCallback.onExpanded(viewHolder);

        }

    }

    public  int getHorizontalRange(   ) {
        RecyclerView.ViewHolder viewHolder = mCallback.getChildViewHolder(mTargetView);
        return mCallback.getHorizontalRange(viewHolder);
    }


    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        if(Math.abs(velocityX) > mMinVelocity && Math.abs(velocityX) < mMaxVelocity) {
            smoothHorizontalExpandOrCollapse(velocityX);
            return true;
        }
        return false;
    }

    /**
     * 封闭一些操作
     * @param holder
     */
    public static void collapse(RecyclerView.ViewHolder holder){
        holder.itemView.setScrollX(0);
    }

    public static void  expand(RecyclerView.ViewHolder holder, int maxRange){
        holder.itemView.setScrollX(maxRange);
    }


    public interface Callback {

        void onCollapsed(RecyclerView.ViewHolder holder);

        void onExpanded(RecyclerView.ViewHolder holder);

        int getHorizontalRange(RecyclerView.ViewHolder holder);

        RecyclerView.ViewHolder getChildViewHolder(View childView);

        View findTargetView(float x, float y);

        boolean isEnable();
    }
}
