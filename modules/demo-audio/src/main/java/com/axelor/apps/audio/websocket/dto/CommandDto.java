package com.axelor.apps.audio.websocket.dto;

import com.axelor.app.AppSettings;

public class CommandDto {
    protected static final AppSettings APP_SETTINGS = AppSettings.get();
    private String command;
    private ObjectData data;

    public static CommandDto toCommandDto(String command, Long id) {
        CommandDto commandDto = new CommandDto();
        ObjectData data = new ObjectData();
        commandDto.setCommand(command);
        String baseHost = APP_SETTINGS.get("application.base.host", "http://localhost:8080/open-platform-demo");
        String apiHost = APP_SETTINGS.get("application.download.audio.api", "/ws/public/download/");
        String fullUrl = baseHost + apiHost + "?id=" + id;
        data.setUrl(fullUrl);
        commandDto.setData(data);
        return commandDto;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public ObjectData getData() {
        return data;
    }

    public void setData(ObjectData data) {
        this.data = data;
    }
    public static String COMMAND_PLAY = "play";

}
