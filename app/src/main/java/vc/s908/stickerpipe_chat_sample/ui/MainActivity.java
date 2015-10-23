package vc.s908.stickerpipe_chat_sample.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import vc.s908.stickerpipe_chat_sample.R;
import vc.s908.stickerpipe_chat_sample.manager.StorageManager;
import vc908.stickerfactory.StickersManager;
import vc908.stickerfactory.ui.OnEmojiBackspaceClickListener;
import vc908.stickerfactory.ui.OnStickerSelectedListener;
import vc908.stickerfactory.ui.activity.PackInfoActivity;
import vc908.stickerfactory.ui.fragment.StickersFragment;
import vc908.stickerfactory.ui.view.BadgedStickersButton;
import vc908.stickerfactory.ui.view.KeyboardHandleRelativeLayout;
import vc908.stickerfactory.utils.KeyboardUtils;


public class MainActivity extends AppCompatActivity implements KeyboardHandleRelativeLayout.KeyboardSizeChangeListener {

    private List<ChatItem> items = new ArrayList<>();
    private ChatAdapter adapter;
    private EditText editMessage;
    private ListView list;
    private View stickersFrame;
    private boolean isStickersFrameVisible;
    private static final String STICKERS_FRAME_STATE = "stickers_frame_state";
    private KeyboardHandleRelativeLayout keyboardHandleLayout;
    private View chatContentGroup;
    private BadgedStickersButton stickerButton;
    private boolean isStickerUsed;
    private View tryStickersView;
    private PaletteDialog paletteDialog;
    private Handler handler = new Handler();
    private boolean isBotJobRunning;
    private Random random = new Random();
    private int primaryLightColor;
    private int primaryColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            isStickersFrameVisible = savedInstanceState.getBoolean(STICKERS_FRAME_STATE);
        }
        primaryColor = StorageManager.getInstance(this).getPrimaryColor();
        primaryLightColor = StorageManager.getInstance(this).getPrimaryLightColor();
        int primaryDarkColor = StorageManager.getInstance(this).getPrimaryDarkColor();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(0xFFFFFFFF);
        toolbar.setBackgroundColor(getResources().getColor(primaryColor));
        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, primaryDarkColor));
        }

        isStickerUsed = StorageManager.getInstance(this).isStickerUsed();
        tryStickersView = findViewById(R.id.try_stickers);
        if (!isStickerUsed) {
            tryStickersView.setVisibility(View.VISIBLE);
        }
        list = (ListView) findViewById(R.id.listView);

        editMessage = (EditText) findViewById(R.id.editText);
        editMessage.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        chatContentGroup = findViewById(R.id.chat_content);
        ImageView buttonSend = (ImageView) findViewById(R.id.send_btn);
        buttonSend.setColorFilter(ContextCompat.getColor(this, primaryColor));
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editMessage.getText().toString();
                addMessage(message, false, System.currentTimeMillis());
            }
        });
        keyboardHandleLayout = (KeyboardHandleRelativeLayout) findViewById(R.id.sizeNotifierLayout);
        keyboardHandleLayout.setKeyboardSizeChangeListener(this);

        adapter = new ChatAdapter();
        list.setAdapter(adapter);
//        list.setStackFromBottom(true);
        stickersFrame = findViewById(R.id.frame);
        updateStickersFrameParams();
        StickersFragment stickersFragment = (StickersFragment) getSupportFragmentManager().findFragmentById(R.id.frame);
        if (stickersFragment == null) {
            stickersFragment = new StickersFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame, stickersFragment).commit();
        }
        stickersFragment.setOnStickerSelectedListener(stickerSelectedListener);
        stickersFragment.setOnEmojiBackspaceClickListener(new OnEmojiBackspaceClickListener() {
            @Override
            public void onEmojiBackspaceClicked() {
                KeyEvent event = new KeyEvent(
                        0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                editMessage.dispatchKeyEvent(event);
            }
        });
        setStickersFrameVisible(isStickersFrameVisible);
        stickerButton = ((BadgedStickersButton) findViewById(R.id.stickers_btn));
        stickerButton.setColorFilter(ContextCompat.getColor(this, primaryColor));
        stickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStickersFrameVisible) {
                    showKeyboard();
                    stickerButton.setImageResource(R.drawable.sp_ic_stickers);
                } else {
                    if (keyboardHandleLayout.isKeyboardVisible()) {
                        keyboardHandleLayout.hideKeyboard(MainActivity.this, new KeyboardHandleRelativeLayout.KeyboardHideCallback() {
                            @Override
                            public void onKeyboardHide() {
                                stickerButton.setImageResource(R.drawable.ic_keyboard);
                                setStickersFrameVisible(true);
                            }
                        });
                    } else {
                        stickerButton.setImageResource(R.drawable.ic_keyboard);
                        setStickersFrameVisible(true);
                    }
                }
            }
        });
        addMockData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isBotJobRunning) {
            switchBotJob(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        switchBotJob(true);
    }

    private void switchBotJob(boolean forceStop) {
        if (isBotJobRunning || forceStop) {
            handler.removeCallbacks(botJobRunnable);
            isBotJobRunning = false;
            if (!forceStop) {
//                Toast.makeText(this, "Bot job stopped", Toast.LENGTH_SHORT).show();
            }
        } else {
            scheduleBotJob();
//            Toast.makeText(this, "Bot job started", Toast.LENGTH_SHORT).show();
        }
    }


    private void scheduleBotJob() {
        handler.postDelayed(botJobRunnable, 10 * 1000);
        isBotJobRunning = true;
    }


    private Runnable botJobRunnable = new Runnable() {

        @Override
        public void run() {
            int randMessagePosition = random.nextInt(MESSAGES.length - 1);
            addMessage(MESSAGES[randMessagePosition], true, System.currentTimeMillis());
            scheduleBotJob();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_palette:
                if (paletteDialog == null) {
                    paletteDialog = new PaletteDialog(this);
                    paletteDialog.setOnColorPickedListener(new PaletteDialog.OnColorPickedListener() {
                        @Override
                        public void onColorPicked(int primaryColorRes, int primaryLightColorRes, int primaryDarkColorRes) {
                            StorageManager.getInstance(MainActivity.this).storePrimaryColor(primaryColorRes);
                            StorageManager.getInstance(MainActivity.this).storePrimaryLightColor(primaryLightColorRes);
                            StorageManager.getInstance(MainActivity.this).storePrimaryDarktColor(primaryDarkColorRes);
                            MainActivity.this.finish();
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    });
                }
                paletteDialog.show();
                break;
//            case R.id.action_bot:
//                switchBotJob(false);
//                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void addMockData() {
        long yesterdayTime = System.currentTimeMillis() - 34 * 60 * 60 * 1000;
        addMessage("Carl!", true, yesterdayTime - 60 * 1000);
        addMessage("Did you hear about the kidnapping back at the prison?", true, yesterdayTime - 50 * 1000);
        addMessage("Luke?! Molly?! Are they okay?", false, yesterdayTime - 40 * 1000);
        addMessage("Yeah, it's okay. They woke up.\uD83D\uDE0A", true, yesterdayTime - 30 * 1000);
//        addMessage("[[pinkgorilla_brutal]]", true, yesterdayTime - 25 * 1000);
        addMessage("Dad", false, yesterdayTime);
        addMessage("...", false, yesterdayTime + 10 * 1000);
//        addMessage("[[pinkgorilla_hm]]", false, yesterdayTime + 30 * 1000);
//        addMessage("[[mems_sadness]]", true, yesterdayTime + 100 * 1000);
        addMessage("[[flowers_flower1]]", true, yesterdayTime + 100 * 1000);
    }

    private void showKeyboard() {
        editMessage.requestFocus();
        ((InputMethodManager) editMessage.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editMessage, InputMethodManager.SHOW_IMPLICIT);
    }

    private OnStickerSelectedListener stickerSelectedListener = new OnStickerSelectedListener() {
        @Override
        public void onStickerSelected(String code) {
            if (StickersManager.isSticker(code)) {
                addMessage(code, false, System.currentTimeMillis());
                if (!isStickerUsed) {
                    tryStickersView.setVisibility(View.GONE);
                    StorageManager.getInstance(MainActivity.this).storeIsStickerUsed(true);
                    isStickerUsed = false;
                }
            } else {
                // append emoji to edit
                editMessage.append(code);
            }
        }
    };

    private void addMessage(String message, boolean isIn, long time) {
        if (TextUtils.isEmpty(message)) {
            return;
        }
        if (StickersManager.isSticker(message)) {
            items.add(new ChatItem(isIn ? ChatItem.ChatItemType.STICKER_IN : ChatItem.ChatItemType.STICKER_OUT, message, time));
        } else {
            items.add(new ChatItem(isIn ? ChatItem.ChatItemType.MESSAGE_IN : ChatItem.ChatItemType.MESSAGE_OUT, message, time));
        }
        if (!isIn) {
            editMessage.setText("");
        }
        updateList();
    }

    private void updateList() {
        adapter.notifyDataSetChanged();
        scrollToBottomIfNeed();
    }

    private void scrollToBottomIfNeed() {
        if (list.getLastVisiblePosition() + 1 >= adapter.getCount() - 1) {
            list.post(new Runnable() {
                @Override
                public void run() {
                    list.smoothScrollToPosition(adapter.getCount() - 1);
                }
            });
        }
    }


    private class ChatAdapter extends BaseAdapter {

        private Drawable downloadBgDrawable;

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
                    if (vh.downloadImage != null) {
                        if (StickersManager.isPackAtUserLibrary(item.getMessage())) {
                            vh.downloadImage.setVisibility(View.GONE);
                        } else {
                            if (downloadBgDrawable == null) {
                                downloadBgDrawable = getResources().getDrawable(R.drawable.download_pack_bg);
                            }
                            if (downloadBgDrawable != null) {
                                downloadBgDrawable.setColorFilter(getResources().getColor(primaryColor), PorterDuff.Mode.SRC_IN);
                                vh.downloadImage.setBackgroundDrawable(downloadBgDrawable);
                            }
                            vh.downloadImage.setVisibility(View.VISIBLE);
                        }
                    }
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

        private void setTime(TextView timeView, long time) {
            // TODO change this code
            timeView.setVisibility(View.VISIBLE);
            Date date = new Date(time);
            Date currentDate = new Date(System.currentTimeMillis());
            if (currentDate.getDate() != date.getDate() || currentDate.getMonth() != date.getMonth()) {
                timeView.setText((date.getMonth() + 1) + "/" + date.getDate() + ", " + date.getHours() + ":" + date.getMinutes());
            } else {
                timeView.setText("Today, " + date.getHours() + ":" + date.getMinutes());
            }
        }

        private class ViewHolder {
            String stickerCode;
            TextView messageView;
            TextView timeView;
            ImageView messageSticker;
            View downloadImage;
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
            vh.downloadImage = view.findViewById(R.id.download_icon);
            view.setTag(vh);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PackInfoActivity.show(MainActivity.this, vh.stickerCode);
                }
            });
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
                Drawable bgDrawable = getResources().getDrawable(R.drawable.chat_item_bg_out);
                if (bgDrawable != null) {
                    bgDrawable.setColorFilter(getResources().getColor(primaryLightColor), PorterDuff.Mode.MULTIPLY);
                    vh.messageView.setBackgroundDrawable(bgDrawable);
                }
            }
            view.setTag(vh);
            return view;
        }
    }

    @Override
    public void onBackPressed() {
        if (isStickersFrameVisible) {
            setStickersFrameVisible(false);
        } else {
            super.onBackPressed();
        }
        stickerButton.setImageResource(R.drawable.sp_ic_stickers);
    }

    private void setStickersFrameVisible(final boolean isVisible) {
        stickersFrame.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        isStickersFrameVisible = isVisible;
        if (stickersFrame.getHeight() != KeyboardUtils.getKeyboardHeight()) {
            updateStickersFrameParams();
        }
        final int padding = isVisible ? KeyboardUtils.getKeyboardHeight() : 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            keyboardHandleLayout.post(new Runnable() {
                @Override
                public void run() {
                    setContentBottomPadding(padding);
                }
            });
        } else {
            setContentBottomPadding(padding);
        }
        scrollToBottomIfNeed();
    }

    public void setContentBottomPadding(int padding) {
        chatContentGroup.setPadding(0, 0, 0, padding);
    }

    private void updateStickersFrameParams() {
        stickersFrame.getLayoutParams().height = KeyboardUtils.getKeyboardHeight();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STICKERS_FRAME_STATE, isStickersFrameVisible);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onKeyboardVisibilityChanged(boolean isVisible) {
        if (isVisible) {
            setStickersFrameVisible(false);
            stickerButton.setImageResource(R.drawable.sp_ic_stickers);
        } else {
            if (isStickersFrameVisible) {
                stickerButton.setImageResource(R.drawable.ic_keyboard);
            } else {
                stickerButton.setImageResource(R.drawable.sp_ic_stickers);
            }
        }
        scrollToBottomIfNeed();
    }


    private static final String[] MESSAGES = new String[]{
            "She walks in beauty, like the night",
            "Of cloudless climes and starry skies",
            "And all that's best of dark and bright",
            "Meet in her aspect and her eyes",
            "Thus mellow'd to that tender light",
            "Which heaven to gaudy day denies",
            "One shade the more, one ray the less",
            "Had half impair'd the nameless grace",
            "Which waves in every raven tress",
            "Or softly lightens o'er her face",
            "Where thoughts serenely sweet express",
            "How pure, how dear their dwelling-place",
            "And on that cheek, and o'er that brow",
            "So soft, so calm, yet eloquent",
            "The smiles that win. the tints that glow",
            "But tell of days in goodness spent",
            "A mind at peace with all below",
            "A heart whose love is innocent!",
            "Shall I compare thee to a summer's day?",
            "Thou art more lovely and more temperate",
            "Rough winds do shake the darling buds of May",
            "And summer's lease hath all too short a date",
            "Sometime too hot the eye of heaven shines",
            "And often is his gold complexion dimm'd",
            "And every fair from fair sometime declines",
            "By chance or natures changing course untrimm'd",
            "But thy eternal summer shall not fade",
            "Nor lose possession of that fair thou owest",
            "Nor shall death brag thou wandrest in his shade",
            "When in eternal lines to time thou growest",
            "So long as men can breathe or eyes can see",
            "So long lives this, and this gives life to thee",
            "Take this kiss upon the brow!",
            "And, in parting from you now",
            "Thus much let me avow",
            "You are not wrong, who deem",
            "That my days have been a dream",
            "Yet if hope has flown away",
            "In a night, or in a day",
            "In a vision, or in none",
            "Is it therefore the less gone?",
            "All that we see or seem",
            "Is but a dream within a dream",
            "I stand amid the roar",
            "Of a surf-tormented shore",
            "And I hold within my hand",
            "Grains of the golden sand",
            "How few! yet how they creep",
            "Through my fingers to the deep",
            "While I weep--while I weep!",
            "O God! can I not grasp",
            "Them with a tighter clasp?",
            "O God! can I not save",
            "One from the pitiless wave?",
            "Is all that we see or seem",
            "But a dream within a dream?",
            "I wandered lonely as a cloud",
            "That floats on high o'er vales and hills",
            "When all at once I saw a crowd",
            "A host, of golden daffodils",
            "Beside the lake, beneath the trees",
            "Fluttering and dancing in the breeze",
            "Continuous as the stars that shine",
            "And twinkle on the milky way",
            "They stretched in never-ending line",
            "Along the margin of a bay",
            "Ten thousand saw I at a glance",
            "Tossing their heads in sprightly dance",
            "The waves beside them danced, but they",
            "Out-did the sparkling leaves in glee",
            "A poet could not be but gay",
            "In such a jocund company!",
            "I gazed—and gazed—but little though",
            "What wealth the show to me had brought",
            "For oft, when on my couch I lie",
            "In vacant or in pensive mood",
            "They flash upon that inward ey",
            "Which is the bliss of solitude",
            "And then my heart with pleasure fills",
            "And dances with the daffodils"
    };

}
