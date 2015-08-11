## About

**StickerPipe** is a stickers SDK for Android platform. 
This sample demonstrates how to add stickers to your chat.

![android](sample.gif)

## Instalation
If you use Eclipse IDE - follow [this instructions](https://github.com/908Inc/stickerpipe-android-sdk-for-eclipse).

Add stickers repository in build.gradle:
```android
repositories {
   maven { url  'http://maven.stickerpipe.com/artifactory/stickerfactory' }
}
```
Add library dependency in build.gradle:
```android
compile('vc908.stickers:stickerfactory:x.x.x@aar') {
     transitive = true;
}
```
List of available versions you can find [here](http://maven.stickerpipe.com/artifactory/stickerfactory/vc908/stickers/stickerfactory/)

Add content provider with your application package to your manifest file:
```android
<provider
     android:name="vc908.stickerfactory.provider.StickersProvider"
     android:authorities="<YOUR PACKAGE>.stickersProvider"
     android:exported="false"/>
```
## Using
### Initializing
Initialize library at your Application onCreate() method
```android
StickersManager.initialize(“72921666b5ff8651f374747bfefaf7b2", this);
```
You can get your own API Key on http://stickerpipe.com to have customized packs set.
### Showing stickers fragment
Create stickers fragment
```android
stickersFragment = new StickersFragment.Builder().build();
```

Then you only need to show fragment. See exaple with best practice.

### Sending stickers
To send stickers you need to set listener and handle results
```android
// create listener
private OnStickerSelectedListener stickerSelectedListener = new OnStickerSelectedListener() {
    @Override
    public void onStickerSelected(String code) {
        if (StickersManager.isSticker(code)) {
	    // send message
        } else {
            // append emoji to your edittext
        }
    }
};
// set listener to your stickers fragment
stickersFragment.setOnStickerSelectedListener(stickerSelectedListener)
```

Listener can take an emoji, so you need to check code first, and then send sticker code or append emoji to your edittext.

### Displaying stickers
```android
// Show sticker in adapter
if (StickersManager.isSticker(message)){ // check your chat message
StickersManager.with(context) // your context - activity, fragment, etc
        .loadSticker(message)
          .into((imageView)); // your image view
} else {
	// show a message as it is
}
```
As an additionl feature, you can check, is sticker exists at user's library, and show some marks
```android
if (StickersManager.isPackAtUserLibrary(stickerCode)) {
	// show mark
} else {
	// hide mark
}
```
![listmark](listmark.png)  

Then you can set click listener and show pack info, where user can download pack.
### Showing pack info
You can show pack info using builder.
```android
 new PackInfoActivity.Builder(MainActivity.this)
                            .show(stickerCode);
```

<img src="pack.png" width="300">

### Showing new packs marker
You can use MarkedImageView to indicate user, that he has a new pack
```android
            <vc908.stickerfactory.ui.view.MarkedImageView
                android:id="@+id/stickers_btn"
                android:layout_width="@dimen/material_48"
                android:layout_height="@dimen/material_48"
                android:layout_centerVertical="true"
                app:centerColor="@android:color/black"
                app:middleColor="@android:color/white"
                app:outColor="@android:color/white"
                android:background="?android:attr/selectableItemBackground"/>
```
![markers](marker.png)  

Use this view as ImageView, other work will be doing by SDK
```android
MarkedImageView markedImageView = (MarkedImageView) findViewById(R.id.marked_image_view));
imageView = markedImageView.getImageView(); // work with image view
```
## Customization
### Stickers fragment
You can customize stickers fragment with builder:

- Set sticker selected listener
```android
setOnStickerSelectedListener(OnStickerSelectedListener listener)
```
It’s highly recommended to set listener outside of builder, directly to fragment when creating or restoring activity.

- Set custom placeholder for stickers in list
```android
setStickerPlaceholderDrawableRes(@DrawableRes int stickerPlaceholderDrawableRes)
```

- Set color filter for sticker’s placeholder
```android
setStickerPlaceholderColorFilterRes(@ColorRes int stickerPlaceholderColorFilterRes)
```
- Set background drawable resource for stickers list with Shader.TileMode.REPEAT
```android
setStickersListBackgroundDrawableRes(@DrawableRes int stickersListBgRes)
```

- Set stickers list background color
```android
setStickersListBackgroundColorRes(@ColorRes int stickersListBackgroundColorRes)
```
- Set custom placeholder for tab icons
```android
setTabPlaceholderDrawableRes(@DrawableRes int tabPlaceholderDrawableRes)
```
- Set tab placeholder filter color
```android
setTabPlaceholderFilterColorRes(@ColorRes int tabPlaceholderColorFilterRes)
```
- Set tab panal background color
```android
setTabBackgroundColorRes(@ColorRes int tabBackgroundColorRes)
```
- Set selected tab underline color
```android
setTabUnderlineColorRes(@ColorRes int tabUnderlineColorRes)
```
- Set color filter for default tab icons placeholder
```android
setTabIconsFilterColorRes(@ColorRes int tabIconsFilterColorRes)
```
- Set max width for sticker cell
```android
setMaxStickerWidth(int maxStickerWidth)
```
- Set color filter for backspace button at emoji tab
```android
setBackspaceFilterColorRes(@ColorRes int backspaceFilterColorRes)
```
- Set text for empty view at recent tab
```android
setEmptyRecentTextRes(@StringRes int emptyRecentTextRes)
```
- Set text color for empty view at recent tab
```android
setEmptyRecentTextColorRes(@ColorRes int emptyRecentTextColorRes)
```
- Set image for empty view at recent tab
```android
setEmptyRecentImageRes(@DrawableRes int emptyRecentImageRes)
```
- Set image color filter for empty view at recent tab
```android
setEmptyRecentColorFilterRes(@ColorRes int emptyRecentColorFilterRes)
```

### Loading process
You can customize sticker loading process with next setters:

- Set sticker placeholder drawable
```android
setPlaceholderDrawableRes(@DrawableRes int placeholderRes)
```
- Set color filter for default placeholder
```android
setPlaceholderColorFilterRes(@ColorRes int colorFilterRes)
```

### Pack info
You can customize pack info screen with next setters:
- Set primary color color, which used for toolbar, action link and progress under action link.
```android
setPrimaryLightColorRes(@ColorRes int colorRes)
```
- Set light color, which used for progress bar under action link
```android
setPrimaryLightColorRes(@ColorRes int colorRes)
```
- Set background color
```android
setBackgroundColor(@ColorRes int colorRes)
````
- Set placeholder drawable for stickers
```android
setPlaceholderDrawable(@DrawableRes int drawableRes)
````        
- Set placeholder color filter for stickers
```android
setPlaceholderColor(@ColorRes int colorRes)
```

### Marked image
You can customize MarkedImageView marker with attributes
```android
<vc908.stickerfactory.ui.view.MarkedImageView
	...
	app:centerColor="@android:color/black"
	app:middleColor="@android:color/white"
    app:outColor="@android:color/white"
    ... />
```
or setter
```android
setMarkerColors(@ColorRes int center, @ColorRes int middle, @ColorRes int outer) {
```

## Credits

908 Inc.

## Contact

mail@908.vc

## License

StickerPipe is available under the Apache 2 license. See the [LICENSE](LICENSE) file for more information.