package com.traveller.android;

public class ChatPojo {
    private String chatText;
    private boolean isRobo;

    public String getChatText() {
        return chatText;
    }

    public void setChatText(String chatText) {
        this.chatText = chatText;
    }

    public boolean isRobo() {
        return isRobo;
    }

    public void setRobo(boolean robo) {
        isRobo = robo;
    }

    public ChatPojo(String chatText, boolean isRobo) {
        this.chatText = chatText;
        this.isRobo = isRobo;
    }
}
