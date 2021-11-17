package lozn.spinner;

import static android.widget.Spinner.MODE_DIALOG;
import static android.widget.Spinner.MODE_DROPDOWN;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SpinnerAdapter;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatSpinner;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

/**
 * Author:Lozn
 * Email:qssq521@gmail.com
 * 2021/10/29
 * 9:37
 */
public class EditSpinner extends LinearLayout {

    private EditText editText;
    private EditInnerSpinner appCoSpinner;
    private ImageView imageView;
    private OnSpinnerClickListener spinnerClickListener;
    private boolean rebuildID=true;

    public String getBelong() {
        return belong;
    }

    public void setBelong(String belong) {
        this.belong = belong;
    }

    /**
     * 这是一个tagert可以用来判断是否改变来控制是否重新拉取数据!
     */
    private String belong;

    public AppCompatSpinner getAppCompatSpinner() {
        return appCoSpinner;
    }


    public TextInputLayout getTextInputLayout() {
        return textInputLayout;
    }

    private TextInputLayout textInputLayout;

    public EditSpinner(Context context) {
        super(context);
        init(context, null, 0);
    }

    public EditSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public EditSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public EditSpinner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    protected void init(Context context, AttributeSet attrs, int defStyleAttr) {
        setGravity(Gravity.CENTER_VERTICAL);
        //https://github.com/jzcruiser/firefox/blob/e1319f0458078f24ab9b82fb6882418f49114b2e/mobile/android/base/MultiChoicePreference.java

        ViewGroup edit_layout = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.edit_spinner_edit_layout, this, false);//add view

        //edittview
        editText = (EditText) edit_layout.findViewById(R.id.spinner_edittext);
        textInputLayout =edit_layout.findViewById(R.id.spinner_textinput_layout);
//        textInputLayout.setId(getId());
        textInputLayout.setBoxStrokeWidthFocused(0);
        textInputLayout.setBoxStrokeWidth(0);
        appCoSpinner = edit_layout.findViewById(R.id.spinner_inner);

        String hint_str = "";
        int gap = 0;
        editText.setBackgroundColor(Color.TRANSPARENT);
        textInputLayout.setBackgroundColor(Color.TRANSPARENT);//清除背景颜色
        imageView = new ImageView(context);
        imageView.setId(R.id.image);
        CharSequence[] mEntries = null;
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EditSpinner);
            mEntries = typedArray.getTextArray(R.styleable.EditSpinner_spinner_item);
            int resourceId = typedArray.getResourceId(R.styleable.EditSpinner_spinner_bg, 0);
            int mode = typedArray.getInt(R.styleable.EditSpinner_spinner_mode, MODE_DROPDOWN);
            if(mode==MODE_DIALOG){
                //REBUILD VIEW 或者在这之前就重新加载布局
                ViewGroup parent = edit_layout.findViewById(R.id.spinner_container);
                parent.removeAllViews();
                appCoSpinner=new EditInnerSpinner(context,mode);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity=Gravity.BOTTOM;
                appCoSpinner.setVisibility(INVISIBLE);
                parent.addView(appCoSpinner, params);

            }

            Drawable drawable = null;
            if (resourceId > 0) {
                drawable = AppCompatResources.getDrawable(context, resourceId);
                ;
            } else {//兼容矢量图片
                drawable = AppCompatResources.getDrawable(context, R.drawable.down_arrow);
                ;
            }

            if (drawable != null) {
                imageView.setImageDrawable(drawable);
            }
            hint_str = typedArray.getString(R.styleable.EditSpinner_spinner_hint);
            appCoSpinner.setPrompt(hint_str);
            textInputLayout.setHint(hint_str);

            String value = typedArray.getString(R.styleable.EditSpinner_spinner_value);

            boolean editable = typedArray.getBoolean(R.styleable.EditSpinner_spinner_editable, true);
            rebuildID = typedArray.getBoolean(R.styleable.EditSpinner_spinner_rebuild_Id, rebuildID);
            gap = typedArray.getDimensionPixelSize(R.styleable.EditSpinner_spinner_gap, 5);
            editText.setEnabled(editable);
            if (!TextUtils.isEmpty(value)) {

                editText.setText(value);
            }
            typedArray.recycle();
        } else {

        }
        if(rebuildID){//重建id,避免fragment重建 后，导致hint内容重复，
            textInputLayout.setId(View.generateViewId());
            imageView.setId(View.generateViewId());
            editText.setId(View.generateViewId());
            appCoSpinner.setId(View.generateViewId());
        }
//        textInputLayout.setId(R.id.text);
        //paddding 清除
        textInputLayout.setPadding(0, 0, 0, 0);
        imageView.setPadding(0, 0, 0, 0);
        //背景清除
//        int minwidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, context.getResources().getDisplayMetrics());
        addView(imageView);
        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
        if (mEntries != null && mEntries.length > 0) {
            MySpinnerRecycleAdapter<CharSequence> adapter = new MySpinnerRecycleAdapter<>();
            ArrayList<CharSequence> data = new ArrayList<>();
            for (CharSequence mEntry : mEntries) {
                data.add(mEntry);
            }
            adapter.setData(data);
            setAdapter(adapter);
        }


        //set layout param
        modifyLayout(edit_layout,gap);
        initEvent();


        //        addView(textInputLayout, labelparam);
    }
    private void modifyLayout(View edit_layout,int gap){

        LayoutParams paramArrowDown = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paramArrowDown.gravity = Gravity.CENTER;
        imageView.setMinimumWidth(30);
        imageView.setLayoutParams(paramArrowDown);
        LayoutParams edittextparam = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        edittextparam.width = 0;
        edittextparam.weight = 1;
        edittextparam.gravity = Gravity.CENTER;
        edittextparam.rightMargin = gap;
        addView(edit_layout,0,edittextparam);
    }

    private void initEvent() {
        appCoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                appCoSpinner.setMyselection(position);
                Object selectedItem = appCoSpinner.getSelectedItem();
                String beforeText = editText.getText().toString();
                editText.setText(String.valueOf(selectedItem));
                editText.selectAll();
//                editText.requestFocus();
                if (onItemSelectedListener != null) {
                    onItemSelectedListener.onItemSelected(parent, view, position, id);
                }
                if (onValueChangeListener != null && beforeText != null && (!beforeText.equals(selectedItem + ""))) {
                    if (onValueChangeListener.onItemSelectPostionChanged(position, String.valueOf(selectedItem))) {
                        focusText = String.valueOf(selectedItem);
                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                onItemSelectedListener.onNothingSelected(parent);
            }
        });
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinnerClickListener != null && spinnerClickListener.onClick()) {
                    return;
                }
                if (appCoSpinner.getSelectedItemPosition() == 0) {
                    Object selectedItem = appCoSpinner.getSelectedItem();
                    if (selectedItem != null) {
                        editText.setText(selectedItem + "");//当clearText的时候又选中当前的时候，不会触发onOptionChange,所以点击就自动给它设置吧。没其他更好的办法了!

                    }
                }
                appCoSpinner.performClick();
            }
        });


        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    focusText = editText.getText().toString();
                    if (onValueChangeListener != null) {
                        onValueChangeListener.onGainFocus();
                    }
                } else {
                    if (!editText.getText().toString().equals(focusText)) {
                        focusText = editText.getText().toString();
                        if (onValueChangeListener != null) {
                            onValueChangeListener.onLossFocusAndTextChange();
                        }
                    } else {
                        if (onValueChangeListener != null) {
                            onValueChangeListener.onLossFocus();
                        }
                    }
                }

            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (onValueChangeListener != null) {
                    onValueChangeListener.onTextChanged(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    String focusText;

    public Context getPopupContext() {
        return appCoSpinner.getPopupContext();
    }

    public void setPopupBackgroundDrawable(Drawable background) {
        appCoSpinner.setPopupBackgroundDrawable(background);
    }

    public void setPopupBackgroundResource(int resId) {
        appCoSpinner.setPopupBackgroundResource(resId);
    }

    public Drawable getPopupBackground() {
        return appCoSpinner.getPopupBackground();
    }

    public void setDropDownVerticalOffset(int pixels) {
        appCoSpinner.setDropDownVerticalOffset(pixels);
    }

    public int getDropDownVerticalOffset() {
        return appCoSpinner.getDropDownVerticalOffset();
    }

    public void setDropDownHorizontalOffset(int pixels) {
        appCoSpinner.setDropDownHorizontalOffset(pixels);
    }

    public int getDropDownHorizontalOffset() {
        return appCoSpinner.getDropDownHorizontalOffset();
    }

    public void setDropDownWidth(int pixels) {
        appCoSpinner.setDropDownWidth(pixels);
    }

    public int getDropDownWidth() {
        return appCoSpinner.getDropDownWidth();
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener l) {
        appCoSpinner.setOnItemClickListener(l);
    }

    public void setPrompt(CharSequence prompt) {
        appCoSpinner.setPrompt(prompt);
    }

    public void setPromptId(int promptId) {
        appCoSpinner.setPromptId(promptId);
    }

    public CharSequence getPrompt() {
        return appCoSpinner.getPrompt();
    }

    public void setSelection(int position, boolean animate) {
        appCoSpinner.setSelection(position, animate);
    }

    public void setSelection(int position) {
        appCoSpinner.setSelection(position);
    }

    public View getSelectedView() {
        return appCoSpinner.getSelectedView();
    }

    public SpinnerAdapter getAdapter() {
        return appCoSpinner.getAdapter();
    }

    public int getCount() {
        return appCoSpinner.getCount();
    }

    public boolean performItemClick(View view, int position, long id) {
        return appCoSpinner.performItemClick(view, position, id);
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
        appCoSpinner.setOnItemLongClickListener(listener);
    }

    AdapterView.OnItemSelectedListener onItemSelectedListener;

    public void setOnItemSelectedListener(@Nullable AdapterView.OnItemSelectedListener listener) {
        this.onItemSelectedListener = listener;
    }

    public String getText() {

        String selectedItem = editText.getText().toString();
        return selectedItem;
    }

    public void clearSelect() {
        editText.setText("");
        appCoSpinner.setSelection(-1);

    }

    /**
     * 点了这个下拉列表箭头
     *
     * @param spinnerClickListener
     */
    public void setOnSpinnerClickListener(OnSpinnerClickListener spinnerClickListener) {
        this.spinnerClickListener = spinnerClickListener;
    }

    public boolean isEmptyList() {
        return getAdapter() == null || getAdapter().isEmpty();
    }

    public void setText(String text) {
        editText.setText(text);
        appCoSpinner.setSelection(-1);
    }

    public CharSequence getHint() {
        return textInputLayout.getHint();
    }

    public interface OnSpinnerClickListener {
        /**
         * 返回true,表示拦截系统的，不让逻辑继续往下走!
         *
         * @return
         */
        boolean onClick();

    }

    public interface OnValueChangeListener {
        void onLossFocus();

        void onLossFocusAndTextChange();

        void onTextChanged(CharSequence s);

        void onGainFocus();

        /**
         * return true  change focusText status
         *
         * @param position
         * @param selectedItem
         * @return
         */
        boolean onItemSelectPostionChanged(int position, String selectedItem);
    }

    public OnValueChangeListener getOnValueChangeListener() {
        return onValueChangeListener;
    }

    public void setOnValueChangeListener(OnValueChangeListener onValueChangeListener) {
        this.onValueChangeListener = onValueChangeListener;
    }

    OnValueChangeListener onValueChangeListener;

    public int getSelectedItemPosition() {
        return appCoSpinner.getSelectedItemPosition();
    }

    public long getSelectedItemId() {
        return appCoSpinner.getSelectedItemId();
    }

    public String getSelectedItem() {
       /* if(appCoSpinner.getSelectedItemPosition()<=0){
            return editText.getText().toString();//清空选择的时候返回空
        },*/
        Object selectedItem = appCoSpinner.getSelectedItem();
        if (appCoSpinner.getSelectedItemPosition() < 0 || selectedItem == null) {
            if (!TextUtils.isEmpty(editText.getText())) {
                return editText.getText().toString();
            } else {
                return "";
            }
        }
        return selectedItem + "";
    }

    public Object getSelectedItemObj() {
        Object selectedItem = appCoSpinner.getSelectedItem();
        return selectedItem;
    }

    public int getPositionForView(View view) {
        return appCoSpinner.getPositionForView(view);
    }

    public int getFirstVisiblePosition() {
        return appCoSpinner.getFirstVisiblePosition();
    }

    public int getLastVisiblePosition() {
        return appCoSpinner.getLastVisiblePosition();
    }

    public void setEmptyView(View emptyView) {
        appCoSpinner.setEmptyView(emptyView);
    }

    public View getEmptyView() {
        return appCoSpinner.getEmptyView();
    }


    public Object getItemAtPosition(int position) {
        return appCoSpinner.getItemAtPosition(position);
    }

    public long getItemIdAtPosition(int position) {
        return appCoSpinner.getItemIdAtPosition(position);
    }

    /*
    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
        appCompatSpinner.setOnClickListener(l);
    }*/

    public void setAdapter(SpinnerAdapter adapter) {
        int count = adapter.getCount();
        appCoSpinner.setAdapter(adapter);
        if (count > 0) {
            Object item = adapter.getItem(0);
            if (item != null) {
                editText.setText(item + "");
                if (appCoSpinner.getSelectedItemPosition() != 0) {
                    appCoSpinner.setSelection(0);
                }
            }
        }
    }

    public EditText getEditText() {
        return editText;
    }

}