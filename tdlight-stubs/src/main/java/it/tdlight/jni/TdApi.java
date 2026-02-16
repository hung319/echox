package it.tdlight.jni;

public class TdApi {
    public static class Object {}

    public static class Function extends Object {}

    public static class Ok extends Object {}

    public static class Error extends Object {
        public int code;
        public String message;

        public Error(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    public static class Chats extends Object {
        public long[] chatIds;

        public Chats(long[] chatIds) {
            this.chatIds = chatIds;
        }
    }

    public static class Chat extends Object {
        public long id;
        public String title;
        public ChatType type;

        public Chat(long id, String title, ChatType type) {
            this.id = id;
            this.title = title;
            this.type = type;
        }
    }

    public static class ChatType extends Object {}

    public static class ChatTypeSupergroup extends ChatType {
        public boolean isChannel;

        public ChatTypeSupergroup(boolean isChannel) {
            this.isChannel = isChannel;
        }
    }

    public static class Message extends Object {
        public long id;
        public int date;
        public MessageContent content;

        public Message(long id, int date, MessageContent content) {
            this.id = id;
            this.date = date;
            this.content = content;
        }
    }

    public static class MessageContent extends Object {}

    public static class MessageVideo extends MessageContent {
        public Video video;
        public FormattedText caption;

        public MessageVideo(Video video, FormattedText caption) {
            this.video = video;
            this.caption = caption;
        }
    }

    public static class Video extends Object {
        public String fileName;
        public File video;
        public int duration;
        public String mimeType;

        public Video(String fileName, File video, int duration, String mimeType) {
            this.fileName = fileName;
            this.video = video;
            this.duration = duration;
            this.mimeType = mimeType;
        }
    }

    public static class File extends Object {
        public int id;
        public long size;
        public LocalFile local;

        public File(int id, long size, LocalFile local) {
            this.id = id;
            this.size = size;
            this.local = local;
        }
    }

    public static class LocalFile extends Object {
        public boolean isDownloadingCompleted;
        public String path;

        public LocalFile(boolean isDownloadingCompleted, String path) {
            this.isDownloadingCompleted = isDownloadingCompleted;
            this.path = path;
        }
    }

    public static class Messages extends Object {
        public Message[] messages;

        public Messages(Message[] messages) {
            this.messages = messages;
        }
    }

    public static class FormattedText extends Object {
        public String text;

        public FormattedText(String text) {
            this.text = text;
        }
    }

    public static class User extends Object {
        public long id;
        public String phoneNumber;
        public String firstName;
        public String lastName;
        public Usernames usernames;

        public User(long id, String phoneNumber, String firstName, String lastName, Usernames usernames) {
            this.id = id;
            this.phoneNumber = phoneNumber;
            this.firstName = firstName;
            this.lastName = lastName;
            this.usernames = usernames;
        }
    }

    public static class Usernames extends Object {
        public String[] activeUsernames;

        public Usernames(String[] activeUsernames) {
            this.activeUsernames = activeUsernames;
        }
    }

    public static class PhoneNumberAuthenticationSettings extends Object {
        public boolean allowFlashCall;
        public boolean isCurrentPhoneNumber;
        public boolean allowSmsRetrieverApi;
    }

    public static class ChatList extends Object {}

    public static class ChatListMain extends ChatList {}

    public static class SetAuthenticationPhoneNumber extends Function {
        public String phoneNumber;
        public PhoneNumberAuthenticationSettings settings;

        public SetAuthenticationPhoneNumber(String phoneNumber, PhoneNumberAuthenticationSettings settings) {
            this.phoneNumber = phoneNumber;
            this.settings = settings;
        }
    }

    public static class CheckAuthenticationCode extends Function {
        public String code;

        public CheckAuthenticationCode(String code) {
            this.code = code;
        }
    }

    public static class GetChats extends Function {
        public ChatList chatList;
        public int limit;

        public GetChats(ChatList chatList, int limit) {
            this.chatList = chatList;
            this.limit = limit;
        }
    }

    public static class GetChat extends Function {
        public long chatId;

        public GetChat(long chatId) {
            this.chatId = chatId;
        }
    }

    public static class GetChatHistory extends Function {
        public long chatId;
        public long fromMessageId;
        public int offset;
        public int limit;
        public boolean onlyLocal;

        public GetChatHistory(long chatId, long fromMessageId, int offset, int limit, boolean onlyLocal) {
            this.chatId = chatId;
            this.fromMessageId = fromMessageId;
            this.offset = offset;
            this.limit = limit;
            this.onlyLocal = onlyLocal;
        }
    }

    public static class DownloadFile extends Function {
        public int fileId;
        public int priority;
        public int offset;
        public int limit;
        public boolean synchronous;

        public DownloadFile(int fileId, int priority, int offset, int limit, boolean synchronous) {
            this.fileId = fileId;
            this.priority = priority;
            this.offset = offset;
            this.limit = limit;
            this.synchronous = synchronous;
        }
    }

    public static class GetMe extends Function {}

    public static class LogOut extends Function {}
}
