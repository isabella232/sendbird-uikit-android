package com.sendbird.uikit.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FileMessage
import com.sendbird.uikit.R
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.consts.KeyboardDisplayType
import com.sendbird.uikit.consts.StringSet
import com.sendbird.uikit.databinding.SbViewMessageInputBinding
import com.sendbird.uikit.interfaces.OnInputModeChangedListener
import com.sendbird.uikit.interfaces.OnInputTextChangedListener
import com.sendbird.uikit.internal.extensions.setAppearance
import com.sendbird.uikit.internal.extensions.setCursorDrawable
import com.sendbird.uikit.internal.ui.reactions.DialogView
import com.sendbird.uikit.model.TextUIConfig
import com.sendbird.uikit.utils.SoftInputUtils
import com.sendbird.uikit.utils.TextUtils
import com.sendbird.uikit.utils.ViewUtils
import java.util.Locale

class MessageInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    val textAppearance: Int
    val binding: SbViewMessageInputBinding
    val layout: View
        get() = this
    var keyboardDisplayType = KeyboardDisplayType.Plane
    var onSendClickListener: OnClickListener? = null
        set(value) {
            field = value
            binding.ibtnSend.setOnClickListener(value)
        }
    var onAddClickListener: OnClickListener? = null
        set(value) {
            field = value
            binding.ibtnAdd.setOnClickListener(value)
        }
    var onEditCancelClickListener: OnClickListener? = null
        set(value) {
            field = value
            binding.btnCancel.setOnClickListener(value)
        }
    var onEditSaveClickListener: OnClickListener? = null
        set(value) {
            field = value
            binding.btnSave.setOnClickListener(value)
        }
    var onReplyCloseClickListener: OnClickListener? = null
        set(value) {
            field = value
            binding.ivQuoteReplyClose.setOnClickListener(value)
        }
    var onInputTextChangedListener: OnInputTextChangedListener? = null
    var onEditModeTextChangedListener: OnInputTextChangedListener? = null
    var onInputModeChangedListener: OnInputModeChangedListener? = null
    var mode = Mode.DEFAULT
    var addButtonVisibility = VISIBLE
        set(value) {
            field = value
            binding.ibtnAdd.visibility = value
        }
    var showSendButtonAlways = false
    var useOverlay = false
    var inputText: CharSequence?
        get() = binding.etInputText.text?.trim { it <= ' ' }
        set(text) {
            binding.etInputText.setText(text)
            text?.let { binding.etInputText.setSelection(text.length) }
        }

    val inputEditText: EditText
        get() = binding.etInputText

    var inputMode: Mode
        get() = mode
        set(mode) {
            val before = this.mode
            this.mode = mode
            when (mode) {
                Mode.EDIT -> {
                    setQuoteReplyPanelVisibility(GONE)
                    setEditPanelVisibility(VISIBLE)
                    binding.ibtnAdd.visibility = GONE
                }
                Mode.QUOTE_REPLY -> {
                    setQuoteReplyPanelVisibility(VISIBLE)
                    setEditPanelVisibility(GONE)
                    addButtonVisibility = this@MessageInputView.addButtonVisibility
                }
                else -> {
                    setQuoteReplyPanelVisibility(GONE)
                    setEditPanelVisibility(GONE)
                    addButtonVisibility = this@MessageInputView.addButtonVisibility
                }
            }
            onInputModeChangedListener?.onInputModeChanged(before, mode)
        }

    enum class Mode {
        /**
         * A mode to be able to send a message normally.
         */
        DEFAULT,

        /**
         * A mode to edit current message.
         */
        EDIT,

        /**
         * A mode to send a reply message about current message.
         */
        QUOTE_REPLY
    }

    fun showKeyboard() =
        if (keyboardDisplayType == KeyboardDisplayType.Dialog) showInputDialog() else SoftInputUtils.showSoftKeyboard(
            binding.etInputText
        )

    fun drawMessageToReply(message: BaseMessage) {
        var displayMessage = message.message
        if (message is FileMessage) {
            ViewUtils.drawFileMessageIconToReply(binding.ivQuoteReplyMessageIcon, message)
            ViewUtils.drawThumbnail(binding.ivQuoteReplyMessageImage, message)
            binding.ivQuoteReplyMessageIcon.visibility = VISIBLE
            binding.ivQuoteReplyMessageImage.visibility = VISIBLE
            displayMessage = if (message.type.contains(StringSet.gif)) {
                StringSet.gif.uppercase(Locale.getDefault())
            } else if (message.type.startsWith(StringSet.image)) {
                TextUtils.capitalize(StringSet.photo)
            } else if (message.type.startsWith(StringSet.video)) {
                TextUtils.capitalize(StringSet.video)
            } else if (message.type.startsWith(StringSet.audio)) {
                TextUtils.capitalize(StringSet.audio)
            } else {
                message.name
            }
        } else {
            binding.ivQuoteReplyMessageIcon.visibility = GONE
            binding.ivQuoteReplyMessageImage.visibility = GONE
        }

        message.sender?.let {
            binding.tvQuoteReplyTitle.text =
                String.format(context.getString(R.string.sb_text_reply_to), it.nickname)
        }
        binding.tvQuoteReplyMessage.text = displayMessage
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.ibtnAdd.isEnabled = enabled
        binding.etInputText.isEnabled = enabled
        binding.ibtnSend.isEnabled = enabled
    }

    fun setSendButtonVisibility(visibility: Int) {
        binding.ibtnSend.visibility = visibility
    }

    fun setSendImageResource(@DrawableRes sendImageResource: Int) {
        binding.ibtnSend.setImageResource(sendImageResource)
    }

    fun setSendImageDrawable(drawable: Drawable?) {
        binding.ibtnSend.setImageDrawable(drawable)
    }

    fun setSendImageButtonTint(tint: ColorStateList?) {
        binding.ibtnSend.imageTintList = tint
    }

    fun setAddImageResource(@DrawableRes addImageResource: Int) {
        binding.ibtnAdd.setImageResource(addImageResource)
    }

    fun setAddImageDrawable(drawable: Drawable?) {
        binding.ibtnAdd.setImageDrawable(drawable)
    }

    fun setAddImageButtonTint(tint: ColorStateList?) {
        binding.ibtnAdd.imageTintList = tint
    }

    fun setEditPanelVisibility(visibility: Int) {
        binding.editPanel.visibility = visibility
    }

    fun setQuoteReplyPanelVisibility(visibility: Int) {
        binding.quoteReplyPanel.visibility = visibility
        binding.ivReplyDivider.visibility = visibility
    }

    fun setInputTextHint(hint: CharSequence?) {
        binding.etInputText.hint = hint
    }

    fun applyTextUIConfig(textUIConfig: TextUIConfig) {
        binding.etInputText.applyTextUIConfig(textUIConfig)
    }

    private fun showInputDialog() {
        val messageInputView = createDialogInputView()
        val themeResId: Int = if (useOverlay) {
            R.style.Widget_Sendbird_Overlay_DialogView
        } else {
            if (SendbirdUIKit.isDarkMode()) R.style.Widget_Sendbird_Dark_DialogView else R.style.Widget_Sendbird_DialogView
        }
        val themeWrapperContext: Context = ContextThemeWrapper(context, themeResId)
        val dialogView = DialogView(themeWrapperContext).apply {
            setContentView(messageInputView)
            setBackgroundBottom()
        }
        val builder = AlertDialog.Builder(context, R.style.Sendbird_Dialog_Bottom)
        builder.setView(dialogView)
        val dialog = builder.create()
        val context = messageInputView.context
        val prevSoftInputMode = SoftInputUtils.getSoftInputMode(context)
        SoftInputUtils.setSoftInputMode(context, WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        onSendClickListener?.let {
            messageInputView.onSendClickListener = OnClickListener {
                dialog.dismiss()
                binding.ibtnSend.postDelayed({
                    onSendClickListener?.onClick(binding.ibtnSend)
                    SoftInputUtils.setSoftInputMode(context, prevSoftInputMode)
                }, 200)
            }
        }
        onAddClickListener?.let {
            messageInputView.onAddClickListener = OnClickListener {
                dialog.dismiss()
                binding.ibtnAdd.postDelayed({
                    onAddClickListener?.onClick(binding.ibtnAdd)
                    SoftInputUtils.setSoftInputMode(context, prevSoftInputMode)
                }, 200)
            }
        }
        onEditSaveClickListener?.let {
            messageInputView.onEditSaveClickListener = OnClickListener {
                inputText = messageInputView.inputText
                dialog.dismiss()
                binding.btnSave.postDelayed({
                    onEditSaveClickListener?.onClick(binding.btnSave)
                    SoftInputUtils.setSoftInputMode(context, prevSoftInputMode)
                }, 200)
            }
        }
        onEditCancelClickListener?.let {
            messageInputView.onEditCancelClickListener = OnClickListener {
                dialog.dismiss()
                binding.btnCancel.postDelayed({
                    onEditCancelClickListener?.onClick(binding.btnCancel)
                    SoftInputUtils.setSoftInputMode(context, prevSoftInputMode)
                }, 200)
            }
        }
        onReplyCloseClickListener?.let {
            messageInputView.onReplyCloseClickListener = OnClickListener {
                dialog.dismiss()
                binding.ivQuoteReplyClose.postDelayed({
                    onReplyCloseClickListener?.onClick(binding.ivQuoteReplyClose)
                    SoftInputUtils.setSoftInputMode(context, prevSoftInputMode)
                }, 200)
            }
        }
        messageInputView.onInputTextChangedListener =
            OnInputTextChangedListener { s, start, before, count ->
                if (Mode.EDIT == inputMode) {
                    onEditModeTextChangedListener?.onInputTextChanged(s, start, before, count)
                } else {
                    onInputTextChangedListener?.onInputTextChanged(s, start, before, count)
                    inputText = s.toString()
                }
            }
        dialog.apply {
            setOnCancelListener {
                inputMode = Mode.DEFAULT
                binding.root.postDelayed({ SoftInputUtils.setSoftInputMode(context, prevSoftInputMode) }, 200)
            }
            show()
            window?.apply {
                setGravity(Gravity.BOTTOM)
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }
        messageInputView.showKeyboard()
    }

    private fun createDialogInputView(): MessageInputView {
        return MessageInputView(context).apply {
            if (showSendButtonAlways) setSendButtonVisibility(VISIBLE)
            showSendButtonAlways = this@MessageInputView.showSendButtonAlways
            inputMode = this@MessageInputView.mode
            when (mode) {
                Mode.EDIT -> {
                    inputText = this@MessageInputView.inputText
                }
                Mode.QUOTE_REPLY -> {
                    binding.ivQuoteReplyMessageIcon.visibility =
                        this@MessageInputView.binding.ivQuoteReplyMessageIcon.visibility
                    binding.ivQuoteReplyMessageImage.visibility =
                        this@MessageInputView.binding.ivQuoteReplyMessageImage.visibility
                    binding.ivQuoteReplyMessageIcon.setImageDrawable(this@MessageInputView.binding.ivQuoteReplyMessageIcon.drawable)
                    binding.ivQuoteReplyMessageImage.content.setImageDrawable(this@MessageInputView.binding.ivQuoteReplyMessageImage.content.drawable)
                    binding.tvQuoteReplyTitle.text = this@MessageInputView.binding.tvQuoteReplyTitle.text
                    binding.tvQuoteReplyMessage.text = this@MessageInputView.binding.tvQuoteReplyMessage.text
                }
                Mode.DEFAULT -> {}
            }
            binding.ibtnSend.setImageDrawable(this@MessageInputView.binding.ibtnSend.drawable)
            binding.ibtnAdd.setImageDrawable(this@MessageInputView.binding.ibtnAdd.drawable)
            binding.etInputText.hint = this@MessageInputView.binding.etInputText.hint
        }
    }

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MessageInputComponent, defStyle, 0)
        try {
            binding = SbViewMessageInputBinding.inflate(LayoutInflater.from(getContext()), this, true)
            val backgroundId =
                a.getResourceId(R.styleable.MessageInputComponent_sb_message_input_background, R.color.background_50)
            val textBackgroundId = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_text_background,
                R.drawable.sb_message_input_text_background_light
            )
            textAppearance = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_text_appearance,
                R.style.SendbirdBody3OnLight01
            )
            val hint = a.getString(R.styleable.MessageInputComponent_sb_message_input_text_hint)
            val hintColor = a.getColorStateList(R.styleable.MessageInputComponent_sb_message_input_text_hint_color)
            val textCursorDrawable = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_text_cursor_drawable,
                R.drawable.sb_message_input_cursor_light
            )
            val leftButtonIcon = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_left_button_icon,
                R.drawable.icon_add
            )
            val leftButtonTint =
                a.getColorStateList(R.styleable.MessageInputComponent_sb_message_input_left_button_tint)
            val leftButtonBackground = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_left_button_background,
                R.drawable.sb_button_uncontained_background_light
            )
            val rightButtonIcon = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_right_button_icon,
                R.drawable.icon_send
            )
            val rightButtonTint =
                a.getColorStateList(R.styleable.MessageInputComponent_sb_message_input_right_button_tint)
            val rightButtonBackground = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_right_button_background,
                R.drawable.sb_button_uncontained_background_light
            )
            val editSaveButtonTextAppearance = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_edit_save_button_text_appearance,
                R.style.SendbirdButtonOnDark01
            )
            val editSaveButtonTextColor =
                a.getColorStateList(R.styleable.MessageInputComponent_sb_message_input_edit_save_button_text_color)
            val editSaveButtonBackground = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_edit_save_button_background,
                R.drawable.sb_button_contained_background_light
            )
            val editCancelButtonTextAppearance = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_edit_cancel_button_text_appearance,
                R.style.SendbirdButtonPrimary300
            )
            val editCancelButtonTextColor =
                a.getColorStateList(R.styleable.MessageInputComponent_sb_message_input_edit_cancel_button_text_color)
            val editCancelButtonBackground = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_edit_cancel_button_background,
                R.drawable.sb_button_uncontained_background_light
            )
            val replyTitleAppearance = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_quote_reply_title_text_appearance,
                R.style.SendbirdCaption1OnLight01
            )
            val replyMessageAppearance = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_quoted_message_text_appearance,
                R.style.SendbirdCaption2OnLight03
            )
            val replyRightButtonIcon = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_quote_reply_right_icon,
                R.drawable.icon_close
            )
            val replyRightButtonTint =
                a.getColorStateList(R.styleable.MessageInputComponent_sb_message_input_quote_reply_right_icon_tint)
            val replyRightButtonBackground = a.getResourceId(
                R.styleable.MessageInputComponent_sb_message_input_quote_reply_right_icon_background,
                R.drawable.sb_button_uncontained_background_light
            )
            binding.messageInputParent.setBackgroundResource(backgroundId)
            binding.etInputText.setBackgroundResource(textBackgroundId)
            binding.etInputText.setAppearance(context, textAppearance)
            hint?.let { setInputTextHint(it) }
            if (hintColor != null) {
                binding.etInputText.setHintTextColor(hintColor)
            }
            binding.etInputText.setCursorDrawable(context, textCursorDrawable)
            isEnabled = true
            binding.ibtnAdd.setBackgroundResource(leftButtonBackground)
            setAddImageResource(leftButtonIcon)
            binding.ibtnAdd.imageTintList = leftButtonTint
            binding.ibtnSend.setBackgroundResource(rightButtonBackground)
            setSendImageResource(rightButtonIcon)
            binding.ibtnSend.imageTintList = rightButtonTint
            binding.btnSave.setAppearance(context, editSaveButtonTextAppearance)
            if (editSaveButtonTextColor != null) {
                binding.btnSave.setTextColor(editSaveButtonTextColor)
            }
            binding.btnSave.setBackgroundResource(editSaveButtonBackground)
            binding.btnCancel.setAppearance(context, editCancelButtonTextAppearance)
            if (editCancelButtonTextColor != null) {
                binding.btnCancel.setTextColor(editCancelButtonTextColor)
            }
            binding.btnCancel.setBackgroundResource(editCancelButtonBackground)
            binding.ivQuoteReplyMessageImage.radius = resources.getDimensionPixelSize(R.dimen.sb_size_8).toFloat()
            binding.tvQuoteReplyTitle.setAppearance(context, replyTitleAppearance)
            binding.tvQuoteReplyMessage.setAppearance(context, replyMessageAppearance)
            binding.ivQuoteReplyClose.setImageResource(replyRightButtonIcon)
            binding.ivQuoteReplyClose.imageTintList = replyRightButtonTint
            binding.ivQuoteReplyClose.setBackgroundResource(replyRightButtonBackground)
            val dividerColor = if (SendbirdUIKit.isDarkMode()) R.color.ondark_04 else R.color.onlight_04
            binding.ivReplyDivider.setBackgroundColor(ContextCompat.getColor(context, dividerColor))
            binding.etInputText.setOnClickListener { showKeyboard() }
            binding.etInputText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                    if (!TextUtils.isEmpty(s) && Mode.EDIT != inputMode || showSendButtonAlways) {
                        setSendButtonVisibility(VISIBLE)
                    } else {
                        setSendButtonVisibility(GONE)
                    }
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (Mode.EDIT == inputMode) {
                        onEditModeTextChangedListener?.onInputTextChanged(s, start, before, count)
                    } else {
                        onInputTextChangedListener?.onInputTextChanged(s, start, before, count)
                    }
                }

                override fun afterTextChanged(s: Editable) {
                    if (!TextUtils.isEmpty(s) && Mode.EDIT != inputMode || showSendButtonAlways) {
                        setSendButtonVisibility(VISIBLE)
                    } else {
                        setSendButtonVisibility(GONE)
                    }
                }
            })
            binding.etInputText.inputType = (
                InputType.TYPE_CLASS_TEXT
                    or InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                )
        } finally {
            a.recycle()
        }
    }
}
