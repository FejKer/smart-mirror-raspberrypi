package me.omigo.raspberryconfigurer.config.domain;

import lombok.Data;
import lombok.ToString;
import me.omigo.raspberryconfigurer.config.command.UpdateAttributesDTO;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@ToString
public class Config implements Serializable {
    private Map<String, String> attributes;

    public Config(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public Map<String, String> getAttributes() {
        return Map.copyOf(attributes);
    }

    public void addAttribute(UpdateAttributesDTO dto) {
        attributes.put(dto.key(), dto.value());
    }

    public void removeAttribute(String key) {
        attributes.remove(key);
    }
}
