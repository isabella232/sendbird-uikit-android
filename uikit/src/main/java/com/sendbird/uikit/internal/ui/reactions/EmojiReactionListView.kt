package com.sendbird.uikit.internal.ui.reactions

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.sendbird.android.message.Reaction
import com.sendbird.uikit.activities.adapter.EmojiReactionListAdapter
import com.sendbird.uikit.databinding.SbViewEmojiReactionListBinding
import com.sendbird.uikit.interfaces.OnItemClickListener
import com.sendbird.uikit.interfaces.OnItemLongClickListener
import kotlin.math.min

internal class EmojiReactionListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {
    val binding: SbViewEmojiReactionListBinding
    val layout: EmojiReactionListView
        get() = this

    private val adapter: EmojiReactionListAdapter
    private val layoutManager: GridLayoutManager

    init {
        binding = SbViewEmojiReactionListBinding.inflate(LayoutInflater.from(context), this, true)
        binding.rvEmojiReactionList.setUseDivider(false)
        layoutManager = GridLayoutManager(context, MAX_SPAN_SIZE)
        binding.rvEmojiReactionList.layoutManager = layoutManager
        binding.rvEmojiReactionList.setHasFixedSize(true)
        adapter = EmojiReactionListAdapter()
        binding.rvEmojiReactionList.adapter = adapter
    }

    companion object {
        private const val MAX_SPAN_SIZE = 4
    }

    fun setReactionList(reactionList: List<Reaction?>) {
        adapter.setReactionList(reactionList)
        resetSpanSize()
    }

    private fun resetSpanSize() {
        val itemSize = adapter.itemCount
        if (itemSize > 0) {
            layoutManager.spanCount = min(itemSize, MAX_SPAN_SIZE)
        }
    }

    fun setEmojiReactionClickListener(emojiReactionClickListener: OnItemClickListener<String?>?) {
        adapter.setEmojiReactionClickListener(emojiReactionClickListener)
    }

    fun setEmojiReactionLongClickListener(emojiReactionLongClickListener: OnItemLongClickListener<String?>?) {
        adapter.setEmojiReactionLongClickListener(emojiReactionLongClickListener)
    }

    fun setMoreButtonClickListener(moreButtonClickListener: OnClickListener?) {
        adapter.setMoreButtonClickListener(moreButtonClickListener)
    }

    fun setUseMoreButton(useMoreButton: Boolean) {
        adapter.setUseMoreButton(useMoreButton)
    }

    fun useMoreButton(): Boolean {
        return adapter.useMoreButton()
    }

    fun refresh() {
        resetSpanSize()
        adapter.notifyItemRangeChanged(0, adapter.itemCount)
    }

    override fun setClickable(clickable: Boolean) {
        super.setClickable(clickable)
        adapter.setClickable(clickable)
    }

    override fun setLongClickable(clickable: Boolean) {
        super.setLongClickable(clickable)
        adapter.setClickable(clickable)
    }
}
