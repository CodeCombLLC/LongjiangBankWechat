package com.ufreedom.simplewebview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * Author SunMeng
 * Date : 2015 七月 04
 */
public class TabMenu extends PopupWindow {
    private LinearLayout mLayout;
    private ImageView mImageView;
    private TextView mTextView;

    /**
     * @param context 上下文  
     * @param onClickListener 单击事件   
     * @param resID 图片资源  
     * @param text 显示的文字  
     * @param fontSize 显示的文字大小  
     * @param fontColor 文字的颜色   
     * @param colorBgTabMenu 背景颜色  
     * @param aniTabMenu 消失的动画  
     * @return
     */
    public TabMenu(Context context,View.OnClickListener onClickListener,int resID,String text,int fontSize,
                   int fontColor,int colorBgTabMenu,int aniTabMenu){
        super(context);

        mLayout=new LinearLayout(context);
        mLayout.setOrientation(LinearLayout.VERTICAL);
        mLayout.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        mLayout.setPadding(10, 10, 10, 10);

        mTextView = new TextView(context);
        mTextView.setTextSize(fontSize);
        mTextView.setTextColor(fontColor);
        mTextView.setBackgroundColor(Color.BLACK);
        mTextView.setText(text);
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setPadding(5, 5, 5, 5);

        mImageView=new ImageView(context);
        
        mImageView.setBackgroundResource(resID);

        
        LinearLayout.LayoutParams  layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        
        mLayout.addView(mImageView,layoutParams);
        
        
        mLayout.addView(mTextView);
        mLayout.setOnClickListener(onClickListener);

        this.setContentView(mLayout);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setBackgroundDrawable(new ColorDrawable(colorBgTabMenu));
        this.setAnimationStyle(aniTabMenu);
        this.setFocusable(true);
    }
}  