package com.axelor.apps.audio.websocket.dto;

public class CommandDto {
    private String command;
    private String url;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
