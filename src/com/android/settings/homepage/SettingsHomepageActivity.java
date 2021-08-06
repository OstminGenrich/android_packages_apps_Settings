/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.homepage;

import android.animation.LayoutTransition;
import android.app.ActivityManager;
import android.app.settings.SettingsEnums;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.content.Intent;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.SystemProperties;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;
import androidx.cardview.widget.CardView;

import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.internal.util.UserIcons;

import com.android.settings.R;
import com.android.settings.accounts.AvatarViewMixin;
import com.android.settings.core.HideNonSystemOverlayMixin;
import com.android.settings.homepage.contextualcards.ContextualCardsFragment;
import com.android.settings.overlay.FeatureFactory;

import com.airbnb.lottie.LottieAnimationView;
import com.android.settingslib.drawable.CircleFramedDrawable;

public class SettingsHomepageActivity extends FragmentActivity {

    private static final String ROM_RELEASETYPE_PROP = "ro.corvus.build.type";

    TextView mRavenLair;
    TextView mRavenThemes;
    TextView mCorvusOTA;
    ImageView arrow;
    CardView cardView;
    LinearLayout hiddenView;
    LinearLayout visibleView;
    LottieAnimationView lottieAnimationView;

    Context context;
    ImageView avatarView;
    UserManager mUserManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_homepage_container);
        final View root = findViewById(R.id.settings_homepage_container);
        root.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        setHomepageContainerPaddingTop();

        Context context = getApplicationContext();

        mUserManager = context.getSystemService(UserManager.class);

        final Toolbar toolbar = findViewById(R.id.search_action_bar);
        FeatureFactory.getFactory(this).getSearchFeatureProvider()
                .initSearchToolbar(this /* activity */, toolbar, SettingsEnums.SETTINGS_HOMEPAGE);

	    getLifecycle().addObserver(new HideNonSystemOverlayMixin(this));

	    avatarView = root.findViewById(R.id.account_avatar);
        avatarView.setImageDrawable(getCircularUserIcon(context));
        avatarView.setOnClickListener(new View.OnClickListener() {
	    @Override
            public void onClick(View v) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$UserSettingsActivity"));
                startActivity(intent);
	    }
	});

	    String crvsReleasetype =  SystemProperties.get(ROM_RELEASETYPE_PROP);

	    visibleView = findViewById(R.id.parent_layout);
        hiddenView = findViewById(R.id.hidden_view);

        cardView = findViewById(R.id.corvus_settings_card);
	    cardView.setBackground(getDrawable(R.drawable.version_bg));

        lottieAnimationView = findViewById(R.id.corvusdash_anim);
        
        arrow = findViewById(R.id.expand_card_arrow);
	    String officialTag = "Official";
        String betaTag = "Beta-Official";
	    cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hiddenView.getVisibility() == View.VISIBLE) {
                    TransitionManager.beginDelayedTransition(cardView,
                            new AutoTransition());
                    hiddenView.setVisibility(View.GONE);
                    visibleView.setVisibility(View.VISIBLE);
                    arrow.setImageResource(R.drawable.arrow_right);
                    lottieAnimationView.playAnimation();
                } else {
                    TransitionManager.beginDelayedTransition(cardView,
                            new AutoTransition());
                    hiddenView.setVisibility(View.VISIBLE);
                    visibleView.setVisibility(View.GONE);
                    arrow.setImageResource(R.drawable.arrow_left);
                    lottieAnimationView.playAnimation();
                }
            }
        });


        // Custom Cardviews
        mRavenLair = findViewById(R.id.raven_lair);
        mRavenLair.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            Intent nIntent = new Intent(Intent.ACTION_MAIN);
            nIntent.setClassName("com.android.settings",
            "com.android.settings.Settings$RavenLairActivity");
            startActivity(nIntent);
        }
    });

	    mRavenThemes = findViewById(R.id.raven_themes);
        mRavenThemes.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            Intent nIntent = new Intent(Intent.ACTION_MAIN);
            nIntent.setClassName("com.corvus.themes",
                "com.corvus.themes.MainActivity");
            startActivity(nIntent);
        }
    });

	mCorvusOTA = findViewById(R.id.corvus_ota);
	if(crvsReleasetype.equals(officialTag) || crvsReleasetype.equals(betaTag)){
	    mCorvusOTA.setVisibility(View.VISIBLE);
	}
        mCorvusOTA.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            Intent nIntent = new Intent(Intent.ACTION_MAIN);
            nIntent.setClassName("com.corvus.ota",
                "com.corvus.ota.MainActivity");
            startActivity(nIntent);
        }
    });

        if (!getSystemService(ActivityManager.class).isLowRamDevice()) {
            // Only allow contextual feature on high ram devices.
            showFragment(new ContextualCardsFragment(), R.id.contextual_cards_content);
        }
        showFragment(new TopLevelSettings(), R.id.main_content);
        ((FrameLayout) findViewById(R.id.main_content))
                .getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
    }

    private void showFragment(Fragment fragment, int id) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        final Fragment showFragment = fragmentManager.findFragmentById(id);

        if (showFragment == null) {
            fragmentTransaction.add(id, fragment);
        } else {
            fragmentTransaction.show(showFragment);
        }
        fragmentTransaction.commit();
    }

    @VisibleForTesting
    void setHomepageContainerPaddingTop() {
        final View view = this.findViewById(R.id.homepage_container);

        final int searchBarHeight = getResources().getDimensionPixelSize(R.dimen.search_bar_height);
        final int searchBarMargin = getResources().getDimensionPixelSize(R.dimen.search_bar_margin);

        // The top padding is the height of action bar(48dp) + top/bottom margins(16dp)
        final int paddingTop = searchBarHeight + searchBarMargin * 2;
        view.setPadding(0 /* left */, paddingTop, 0 /* right */, 0 /* bottom */);

        // Prevent inner RecyclerView gets focus and invokes scrolling.
        view.setFocusableInTouchMode(true);
        view.requestFocus();
    }

	private Drawable getCircularUserIcon(Context context) {
        Bitmap bitmapUserIcon = mUserManager.getUserIcon(UserHandle.myUserId());

        if (bitmapUserIcon == null) {
            // get default user icon.
            final Drawable defaultUserIcon = UserIcons.getDefaultUserIcon(
                    context.getResources(), UserHandle.myUserId(), false);
            bitmapUserIcon = UserIcons.convertToBitmap(defaultUserIcon);
        }
        Drawable drawableUserIcon = new CircleFramedDrawable(bitmapUserIcon,
                (int) context.getResources().getDimension(R.dimen.circle_avatar_size));

        return drawableUserIcon;
    }

    @Override
    public void onResume() {
        super.onResume();
        avatarView.setImageDrawable(getCircularUserIcon(getApplicationContext()));
    }
}
