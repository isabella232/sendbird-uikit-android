package com.sendbird.uikit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.uikit.model.FileInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PendingMessageRepository {

    private PendingMessageRepository() {}

    private static class PendingMessageManagerHolder {
        static final PendingMessageRepository INSTANCE = new PendingMessageRepository();
    }
    public static PendingMessageRepository getInstance() {
        return PendingMessageManagerHolder.INSTANCE;
    }

    private final Map<String, List<BaseMessage>> pendingMessageMap = new ConcurrentHashMap<>();
    private final Map<String, FileInfo> cachedFileInfos = new ConcurrentHashMap<>();

    public FileInfo getFileInfo(@NonNull BaseMessage message) {
        return cachedFileInfos.get(message.getRequestId());
    }

    public void addFileInfo(@NonNull FileMessage message, @NonNull FileInfo fileInfo) {
        cachedFileInfos.put(message.getRequestId(), fileInfo);
    }

    void addPendingMessage(@NonNull String channelUrl, @NonNull BaseMessage message) {
        List<BaseMessage> pendingMessages = pendingMessageMap.get(channelUrl);
        if (pendingMessages == null) {
            pendingMessages = new ArrayList<>();
        }
        pendingMessages.add(0, message);
        pendingMessageMap.put(channelUrl, pendingMessages);
    }

    void updatePendingMessage(@NonNull String channelUrl, @Nullable BaseMessage message) {
        if (message == null) return;

        final List<BaseMessage> pendingMessages = pendingMessageMap.get(channelUrl);
        if (pendingMessages != null) {
            BaseMessage msg;
            for (int i = pendingMessages.size() - 1; i >= 0; i--) {
                msg = pendingMessages.get(i);
                if (msg.getRequestId().equals(message.getRequestId())) {
                    pendingMessages.set(i, message);
                    break;
                }
            }
            pendingMessageMap.put(channelUrl, pendingMessages);
        }
    }

    void removePendingMessage(@NonNull String channelUrl, @NonNull BaseMessage message) {
        final List<BaseMessage> pendingMessages = pendingMessageMap.get(channelUrl);
        final String reqId = message.getRequestId();
        final FileInfo fileInfo = getFileInfo(message);

        if (fileInfo != null) {
            fileInfo.clear();
        }
        //failedFileInfo.remove(reqId);
        if (pendingMessages != null && reqId != null) {
            // because this is temp message so it must compare by request id not using equals function.
            for (BaseMessage pendingMessage : pendingMessages) {
                if (message.getClass() != pendingMessage.getClass()) {
                    continue;
                }
                String pendingMessageReqId = pendingMessage.getRequestId();
                if (reqId.equals(pendingMessageReqId)) {
                    pendingMessages.remove(pendingMessage);
                    break;
                }
            }
            pendingMessageMap.put(channelUrl, pendingMessages);
        }
    }

    @NonNull
    List<BaseMessage> getPendingMessageList(@NonNull String channelUrl) {
        List<BaseMessage> pendingList = pendingMessageMap.get(channelUrl);
        return pendingList == null ? new ArrayList<>() : pendingList;
    }
}
