package me.omigo.raspberryconfigurer.config.command;

public interface ConfigCommandService {
    void addAttribute(UpdateAttributesDTO dto);

    void removeAttribute(String key);

    void tryToConnectToWifi();
}
