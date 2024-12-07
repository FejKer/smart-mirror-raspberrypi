package me.omigo.raspberryconfigurer.config.command;

public interface ConfigCommandRepository {
    void updateAttribute(UpdateAttributesDTO dto);
    void deleteAttribute(String key);
}
