package com.sendbird.uikit.customsample.groupchannel.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.groupchannel.components.CustomUserTypedHeaderComponent;
import com.sendbird.uikit.fragments.BannedUserListFragment;
import com.sendbird.uikit.modules.BannedUserListModule;
import com.sendbird.uikit.modules.components.HeaderComponent;

/**
 * Implements the customized <code>BannedUserListFragment</code>.
 */
public class CustomBannedUserListFragment extends BannedUserListFragment {
    @NonNull
    @Override
    protected BannedUserListModule onCreateModule(@NonNull Bundle args) {
        BannedUserListModule module = super.onCreateModule(args);
        module.setHeaderComponent(new CustomUserTypedHeaderComponent());
        return module;
    }

    @Override
    protected void onConfigureParams(@NonNull BannedUserListModule module, @NonNull Bundle args) {
        super.onConfigureParams(module, args);

        HeaderComponent.Params headerParams = module.getHeaderComponent().getParams();
        if (isFragmentAlive()) {
            headerParams.setTitle(requireContext().getString(R.string.sb_text_menu_banned_users));
            headerParams.setLeftButtonIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_arrow_left, null));
            headerParams.setUseRightButton(false);
        }
    }
}
