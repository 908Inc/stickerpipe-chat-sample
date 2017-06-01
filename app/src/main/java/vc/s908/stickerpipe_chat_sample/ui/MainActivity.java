package vc.s908.stickerpipe_chat_sample.ui;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import vc.s908.stickerpipe_chat_sample.R;
import vc908.stickerfactory.StickersKeyboardController;
import vc908.stickerfactory.StickersManager;
import vc908.stickerfactory.ui.OnStickerSelectedListener;
import vc908.stickerfactory.ui.fragment.StickersFragment;
import vc908.stickerfactory.ui.view.BadgedStickersButton;
import vc908.stickerfactory.ui.view.StickersKeyboardLayout;
import vc908.stickerfactory.utils.CompatUtils;
import vc908.stickerpipe.gcmintegration.NotificationManager;


public class MainActivity extends AppCompatActivity {

    private List<ChatItem> items = new ArrayList<>();
    private ChatAdapter adapter;
    private EditText editMessage;
    private ListView list;
    private StickersKeyboardController stickersKeyboardController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(0xFFFFFFFF);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
        setSupportActionBar(toolbar);

        list = (ListView) findViewById(R.id.listView);

        editMessage = (EditText) findViewById(R.id.editText);
        ImageView buttonSend = (ImageView) findViewById(R.id.send_btn);
        buttonSend.setColorFilter(ContextCompat.getColor(this, R.color.primary));
        buttonSend.setOnClickListener(v -> {
            String message = editMessage.getText().toString();
            addMessage(message, false, System.currentTimeMillis());
        });

        adapter = new ChatAdapter();
        list.setAdapter(adapter);
        StickersFragment stickersFragment = (StickersFragment) getSupportFragmentManager().findFragmentById(R.id.frame);
        if (stickersFragment == null) {
            stickersFragment = new StickersFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame, stickersFragment).commit();
        }
        stickersFragment.setOnStickerSelectedListener(stickerSelectedListener);

        BadgedStickersButton stickerButton = ((BadgedStickersButton) findViewById(R.id.stickers_btn));
        View stickersFrame = findViewById(R.id.frame);
        View chatContentGroup = findViewById(R.id.chat_content);
        RecyclerView suggestsList = (RecyclerView) findViewById(R.id.suggests_list);
        StickersKeyboardLayout stickersLayout = (StickersKeyboardLayout) findViewById(R.id.sizeNotifierLayout);
        stickersKeyboardController = new StickersKeyboardController.Builder(this)
                .setStickersKeyboardLayout(stickersLayout)
                .setStickersFragment(stickersFragment)
                .setStickersFrame(stickersFrame)
                .setContentContainer(chatContentGroup)
                .setStickersButton(stickerButton)
                .setChatEdit(editMessage)
                .setSuggestContainer(suggestsList)
                .build();

        stickersKeyboardController.setKeyboardVisibilityChangeListener(new StickersKeyboardController.KeyboardVisibilityChangeListener() {
            @Override
            public void onTextKeyboardVisibilityChanged(boolean isVisible) {
                scrollToBottomIfNeed();
            }

            @Override
            public void onStickersKeyboardVisibilityChanged(boolean isVisible) {

            }
        });

        addMockData();
        processIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    private void processIntent(Intent intent) {
        NotificationManager.processIntent(this, intent, stickersKeyboardController);
    }

    private void addMockData() {
        long yesterdayTime = System.currentTimeMillis() - 34 * 60 * 60 * 1000;
        addMessage("Hi!", true, yesterdayTime - 60 * 1000);
        addMessage("[[1419]]", true, yesterdayTime - 55 * 1000);
    }

    private OnStickerSelectedListener stickerSelectedListener = new OnStickerSelectedListener() {
        @Override
        public void onStickerSelected(String code) {
            addMessage(code, false, System.currentTimeMillis());
        }

        @Override
        public void onEmojiSelected(String emoji) {
            editMessage.append(emoji);
        }
    };

    private void addMessage(String message, boolean isIn, long time) {
        if (TextUtils.isEmpty(message)) {
            return;
        }
        if (StickersManager.isSticker(message)) {
            items.add(new ChatItem(isIn ? ChatItem.ChatItemType.STICKER_IN : ChatItem.ChatItemType.STICKER_OUT, message, time));
            StickersManager.onUserMessageSent(true);
        } else {
            items.add(new ChatItem(isIn ? ChatItem.ChatItemType.MESSAGE_IN : ChatItem.ChatItemType.MESSAGE_OUT, message, time));
            StickersManager.onUserMessageSent(false);
            if (!isIn) {
                editMessage.setText("");
            }
        }
        updateList(!isIn);
    }

    private void updateList(boolean forceScroll) {
        adapter.notifyDataSetChanged();
        scrollToBottomIfNeed(forceScroll);
    }

    private void scrollToBottomIfNeed() {
        scrollToBottomIfNeed(false);
    }

    private void scrollToBottomIfNeed(boolean force) {
        if (list.getLastVisiblePosition() + 1 >= adapter.getCount() - 1 || force) {
            list.smoothScrollToPosition(adapter.getCount() - 1);
        }
    }

    private class ChatAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public ChatItem getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return ChatItem.ChatItemType.values().length;
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).getType().ordinal();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ChatItem.ChatItemType[] vals = ChatItem.ChatItemType.values();
            switch (vals[getItemViewType(position)]) {
                case MESSAGE_OUT:
                    if (convertView == null) {
                        convertView = createMessageView(ChatItem.ChatItemType.MESSAGE_OUT, parent);
                    }
                    populateView(convertView, getItem(position), ChatItem.ChatItemType.MESSAGE_OUT, position);
                    break;
                case MESSAGE_IN:
                    if (convertView == null) {
                        convertView = createMessageView(ChatItem.ChatItemType.MESSAGE_IN, parent);
                    }
                    populateView(convertView, getItem(position), ChatItem.ChatItemType.MESSAGE_IN, position);
                    break;
                case STICKER_OUT:
                    if (convertView == null) {
                        convertView = createStickerView(ChatItem.ChatItemType.STICKER_OUT, parent);
                    }
                    populateView(convertView, getItem(position), ChatItem.ChatItemType.STICKER_OUT, position);
                    break;
                case STICKER_IN:
                    if (convertView == null) {
                        convertView = createStickerView(ChatItem.ChatItemType.STICKER_IN, parent);
                    }
                    populateView(convertView, getItem(position), ChatItem.ChatItemType.STICKER_IN, position);
                    break;
                default:
                    throw new RuntimeException("Unknown item type");
            }
            return convertView;
        }

        private void populateView(View convertView, ChatItem item, ChatItem.ChatItemType itemType, int position) {
            ViewHolder vh = (ViewHolder) convertView.getTag();
            switch (itemType) {
                case MESSAGE_OUT:
                case MESSAGE_IN:
                    vh.messageView.setText(item.getMessage());
                    break;
                case STICKER_IN:
                case STICKER_OUT:
                    loadSticker(vh.messageSticker, item.getMessage());
                    vh.stickerCode = item.getMessage();
                    break;
                default:
                    throw new RuntimeException("Unknown item type");
            }
            updateTime(vh.timeView, position, item.getTime(), itemType);
            updateAvatarVisibility(vh.avatar, position);
        }

        private void updateAvatarVisibility(View avatar, int position) {
            if (avatar != null) {
                if (position != 0) {
                    ChatItem.ChatItemType prevItemType = ChatItem.ChatItemType.values()[getItemViewType(position - 1)];
                    if (prevItemType == ChatItem.ChatItemType.MESSAGE_IN || prevItemType == ChatItem.ChatItemType.STICKER_IN) {
                        avatar.setVisibility(View.GONE);
                    } else {
                        avatar.setVisibility(View.VISIBLE);
                    }
                } else {
                    avatar.setVisibility(View.VISIBLE);
                }
            }
        }

        private void updateTime(TextView timeView, int position, long time, ChatItem.ChatItemType currentItemType) {
            if (position != items.size() - 1) {
                ChatItem.ChatItemType nextItemType = ChatItem.ChatItemType.values()[getItemViewType(position + 1)];
                switch (nextItemType) {
                    case STICKER_OUT:
                    case MESSAGE_OUT:
                        if (currentItemType == ChatItem.ChatItemType.MESSAGE_OUT || currentItemType == ChatItem.ChatItemType.STICKER_OUT) {
                            hideTime(timeView);
                        } else {
                            setTime(timeView, time);
                        }
                        break;
                    case STICKER_IN:
                    case MESSAGE_IN:
                        if (currentItemType == ChatItem.ChatItemType.MESSAGE_IN || currentItemType == ChatItem.ChatItemType.STICKER_IN) {
                            hideTime(timeView);
                        } else {
                            setTime(timeView, time);
                        }
                        break;
                    default:
                        throw new RuntimeException("Unknown item type");
                }
            } else {
                setTime(timeView, time);
            }
        }

        private void hideTime(TextView timeView) {
            timeView.setVisibility(View.GONE);
        }

        private final SimpleDateFormat MESSAGE_TIME_FORMAT = new SimpleDateFormat("M/dd, k:mm", Locale.US);

        private void setTime(TextView timeView, long time) {
            timeView.setText(MESSAGE_TIME_FORMAT.format(time));
        }

        private class ViewHolder {
            String stickerCode;
            TextView messageView;
            TextView timeView;
            ImageView messageSticker;
            View avatar;
        }

        private View createStickerView(ChatItem.ChatItemType itemType, ViewGroup parent) {
            int layoutId;
            if (itemType == ChatItem.ChatItemType.STICKER_IN) {
                layoutId = R.layout.list_item_sticker_in;
            } else {
                layoutId = R.layout.list_item_sticker_out;
            }
            View view = getLayoutInflater().inflate(layoutId, parent, false);
            final ViewHolder vh = new ViewHolder();
            vh.messageSticker = (ImageView) view.findViewById(R.id.chat_item_sticker);
            vh.timeView = (TextView) view.findViewById(R.id.chat_item_time);
            vh.avatar = view.findViewById(R.id.avatar);
            view.setTag(vh);
            view.setOnClickListener(v -> StickersManager.showPackInfoByCode(MainActivity.this, vh.stickerCode));
            return view;
        }

        private void loadSticker(ImageView convertView, String message) {
            StickersManager.with(MainActivity.this)
                    .loadSticker(message)
                    .into((convertView));
        }

        private View createMessageView(ChatItem.ChatItemType itemType, ViewGroup parent) {
            int layoutId;
            if (itemType == ChatItem.ChatItemType.MESSAGE_IN) {
                layoutId = R.layout.list_item_message_in;
            } else {
                layoutId = R.layout.list_item_message_out;
            }
            View view = getLayoutInflater().inflate(layoutId, parent, false);
            ViewHolder vh = new ViewHolder();
            vh.messageView = (TextView) view.findViewById(R.id.chat_item_message);
            vh.timeView = (TextView) view.findViewById(R.id.chat_item_time);
            vh.avatar = view.findViewById(R.id.avatar);
            if (itemType == ChatItem.ChatItemType.MESSAGE_OUT) {
                Drawable bgDrawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.chat_item_bg_out);
                if (bgDrawable != null) {
                    bgDrawable.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.primary_light), PorterDuff.Mode.MULTIPLY);
                    CompatUtils.setBackgroundDrawable(vh.messageView, bgDrawable);
                }
            }
            view.setTag(vh);
            return view;
        }
    }

    @Override
    public void onBackPressed() {
        if (!stickersKeyboardController.hideStickersKeyboard()) {
            super.onBackPressed();
        }
    }
}
