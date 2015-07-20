package vc.s908.stickerpipe_chat_sample.ui;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import vc.s908.stickerpipe_chat_sample.R;
import vc.s908.stickerpipe_chat_sample.manager.StorageManager;
import vc908.stickerfactory.utils.Utils;

/**
 * @author Dmitry Nezhydenko (dehimb@gmail.com)
 */
public class PaletteDialog extends AlertDialog {
    private static final int[][] COLORS = new int[][]{
            {R.color.red_500, R.color.red_100, R.color.red_700},
            {R.color.pink_500, R.color.pink_100, R.color.pink_700},
            {R.color.purple_500, R.color.purple_100, R.color.purple_700},
            {R.color.deep_purple_500, R.color.deep_purple_100, R.color.deep_purple_700},
            {R.color.indigo_500, R.color.indigo_100, R.color.indigo_700},
            {R.color.blue_500, R.color.blue_100, R.color.blue_700},
            {R.color.light_blue_500, R.color.light_blue_100, R.color.light_blue_700},
            {R.color.cyan_500, R.color.cyan_100, R.color.cyan_700},
            {R.color.teal_500, R.color.teal_100, R.color.teal_700},
            {R.color.green_500, R.color.green_100, R.color.green_700},
            {R.color.light_green_500, R.color.light_green_100, R.color.light_green_700},
            {R.color.lime_500, R.color.lime_100, R.color.lime_700},
            {R.color.yellow_500, R.color.yellow_100, R.color.yellow_700},
            {R.color.amber_500, R.color.amber_100, R.color.amber_700},
            {R.color.orange_500, R.color.orange_100, R.color.orange_700},
            {R.color.deep_orange_500, R.color.deep_orange_100, R.color.deep_orange_700},
            {R.color.grey_500, R.color.grey_100, R.color.grey_700},
            {R.color.blue_gray_500, R.color.blue_gray_100, R.color.blue_gray_700}
    };
    private OnColorPickedListener colorPickListener;
    private int currentPrimaryColor;

    protected PaletteDialog(Context context) {
        super(context);
        init();
    }

    protected PaletteDialog(Context context, int theme) {
        super(context, theme);
        init();
    }

    protected PaletteDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    private void init() {
        currentPrimaryColor = StorageManager.getInstance(getContext()).getPrimaryColor();
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_palette, null);
        GridView gridView = (GridView) view.findViewById(R.id.grid);
        gridView.setNumColumns(6);
        gridView.setAdapter(new ColorsAdapter());
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (colorPickListener != null) {
                    colorPickListener.onColorPicked(COLORS[position][0], COLORS[position][1], COLORS[position][2]);
                }
            }
        });
        gridView.setHorizontalSpacing(Utils.dp(16));
        gridView.setVerticalSpacing(Utils.dp(16));
        gridView.setColumnWidth(Utils.dp(24));
        setView(view);
    }

    private class ColorsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return COLORS.length;
        }

        @Override
        public Integer getItem(int position) {
            return COLORS[position][0];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView view = new ImageView(getContext());
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(Utils.dp(24), Utils.dp(24));
            view.setLayoutParams(lp);
            view.setBackgroundColor(getContext().getResources().getColor(getItem(position)));
            if (getItem(position) == currentPrimaryColor) {
                view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                view.setImageResource(R.drawable.ic_action_check);
            }
            return view;
        }
    }

    public void setOnColorPickedListener(OnColorPickedListener colorPickListener) {
        this.colorPickListener = colorPickListener;
    }

    public interface OnColorPickedListener {
        void onColorPicked(@ColorRes int primaryColorRes, @ColorRes int primaryLightColorRes, @ColorRes int primaryDarkColorRes);
    }
}
