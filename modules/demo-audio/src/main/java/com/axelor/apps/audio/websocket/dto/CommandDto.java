package com.axelor.apps.audio.websocket.dto;

public class CommandDto {
    private String command;
    private ObjectData data;

    public static CommandDto toCommandDto(String command, String url) {
        CommandDto commandDto = new CommandDto();
        ObjectData data = new ObjectData();
        commandDto.setCommand(command);
        data.setUrl(url);
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
