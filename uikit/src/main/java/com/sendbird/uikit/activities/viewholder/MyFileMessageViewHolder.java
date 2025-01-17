package com.sendbird.uikit.activities.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.channel.GroupChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.Reaction;
import com.sendbird.uikit.consts.ClickableViewIdentifier;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.databinding.SbViewMyFileMessageBinding;
import com.sendbird.uikit.interfaces.OnItemClickListener;
import com.sendbird.uikit.interfaces.OnItemLongClickListener;
import com.sendbird.uikit.internal.ui.messages.MyFileMessageView;
import com.sendbird.uikit.internal.ui.reactions.EmojiReactionListView;

import java.util.List;
import java.util.Map;

public final class MyFileMessageViewHolder extends GroupChannelMessageViewHolder {
    @NonNull
    private final EmojiReactionListView emojiReactionListView;
    @NonNull
    private final MyFileMessageView myFileMessageView;

    MyFileMessageViewHolder(@NonNull SbViewMyFileMessageBinding binding, boolean useMessageGroupUI) {
        super(binding.getRoot(), useMessageGroupUI);
        myFileMessageView = binding.myFileMessageView;
        emojiReactionListView = myFileMessageView.getBinding().rvEmojiReactionList;
        clickableViewMap.put(ClickableViewIdentifier.Chat.name(), myFileMessageView.getBinding().contentPanelWithReactions);
        clickableViewMap.put(ClickableViewIdentifier.QuoteReply.name(), myFileMessageView.getBinding().quoteReplyPanel);
    }

    @Override
    public void bind(@NonNull BaseChannel channel, @NonNull BaseMessage message, @NonNull MessageGroupType messageGroupType) {
        myFileMessageView.setMessageUIConfig(messageUIConfig);
        if (channel instanceof GroupChannel) {
            myFileMessageView.drawMessage((GroupChannel) channel, message, messageGroupType);
        }
    }

    @Override
    public void setEmojiReaction(@NonNull List<Reaction> reactionList,
                                 @Nullable OnItemClickListener<String> emojiReactionClickListener,
                                 @Nullable OnItemLongClickListener<String> emojiReactionLongClickListener,
                                 @Nullable View.OnClickListener moreButtonClickListener) {
        emojiReactionListView.setReactionList(reactionList);
        emojiReactionListView.setEmojiReactionClickListener(emojiReactionClickListener);
        emojiReactionListView.setEmojiReactionLongClickListener(emojiReactionLongClickListener);
        emojiReactionListView.setMoreButtonClickListener(moreButtonClickListener);
    }

    @Override
    @NonNull
    public Map<String, View> getClickableViewMap() {
        return clickableViewMap;
    }
}
