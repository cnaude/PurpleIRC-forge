/*
 * Copyright (C) 2014 cnaude
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.cnaude.purpleirc.Utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.scanner.ScannerException;

/**
 *
 * @author cnaude
 */
public final class PurpleConfiguration {

    private final DumperOptions options;
    private final Representer representer;
    private final Yaml yaml;
    private final File file;
    private Map<String, Object> result;

    final String JOIN_NOTICE = "join-notice";
    final String CHANNELS = "channels";
    final String COMMANDS = "commands";
    final String MESSAGE_FORMAT = "message-format";

    public PurpleConfiguration(File file, boolean load) throws FileNotFoundException, IOException, ScannerException {
        options = new DumperOptions();
        representer = new Representer();
        yaml = new Yaml(representer, options);
        result = new HashMap<>();
        this.file = file;
        if (load) {
            load();
        }
        if (result == null) {
            result = new HashMap<>();
        }
    }

    public void load() throws FileNotFoundException, IOException, ScannerException {
        try (InputStream ios = new FileInputStream(file)) {
            result = (Map<String, Object>) yaml.load(ios);
            if (result == null) {
                result = new HashMap<>();
            }
        }
    }

    public <T> T getOption(String field, T def) {
        if (result.containsKey(field)) {
            if (result.get(field).getClass().equals(def.getClass())) {
                return (T) result.get(field);
            }
        }
        set(field, (T) def);
        return (T) def;
    }

    public <T> T getOption(String field, String subField, T def) {
        if (result.containsKey(field)) {
            if (result.get(field) instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) result.get(field);
                if (map.containsKey(subField)) {
                    if (map.get(subField).getClass().equals(def.getClass())) {
                        return (T) map.get(subField);
                    }
                }
            }
        }
        set(field, field, (T) def);
        return (T) def;
    }

    public Map<String, Object> getMap(String field) {
        if (result.containsKey(field)) {
            if (result.get(field) instanceof Map) {
                return (Map<String, Object>) result.get(field);
            }
        }
        return new HashMap<>();
    }

    public <T> void set(String field, T value) {
        result.put(field, (T) value);
    }
    
    public <T> void set(String field, String subField, T value) {
        if (result.containsKey(field)) {
            if (result.get(field) instanceof Map) {
                ((Map<String, Object>)result.get(field)).put(subField, (T) value);
            }
        }
    }

    public void save() throws IOException {
        options.setIndent(2);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setAllowUnicode(Charset.defaultCharset().name().contains("UTF"));
        representer.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        try (FileWriter writer = new FileWriter(file)) {
            yaml.dump(result, writer);
        }
    }

    public boolean contains(String section) {
        return result.containsKey(section);
    }

    public Map<String, String> getMessageTemplates() {
        if (result.containsKey(MESSAGE_FORMAT)) {
            return (Map<String, String>) result.get(MESSAGE_FORMAT);
        }
        return new HashMap<>();
    }

    public <T> T getChannelOption(String channel, String option, T def) {
        if (result.containsKey(CHANNELS)) {
            Map<String, Object> map = (Map<String, Object>) result.get(CHANNELS);
            if (map.containsKey(channel)) {
                if (map.get(channel) instanceof Map) {
                    Map<String, Object> optionMap = (Map<String, Object>) map.get(channel);
                    if (optionMap.containsKey(option)) {
                        if (optionMap.get(option).getClass().equals(def.getClass())) {
                            return (T) optionMap.get(option);
                        }
                    }
                }
            }
        }
        return def;
    }

    public <T> T getJoinNoticeOption(String channel, String option, T def) {
        if (result.containsKey(CHANNELS)) {
            Map<String, Object> channelMap = (Map<String, Object>) result.get(CHANNELS);
            if (channelMap.containsKey(JOIN_NOTICE)) {
                if (channelMap.get(JOIN_NOTICE) instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) channelMap.get(JOIN_NOTICE);
                    if (map.containsKey(option)) {
                        if (map.get(option).getClass().equals(def.getClass())) {
                            return (T) map.get(option);
                        }
                    }
                }
            }
        }
        return def;
    }

    public Map<String, Object> getChannelCommands(String channel) {
        if (result.containsKey(CHANNELS)) {
            Map<String, Object> channelMap = (Map<String, Object>) result.get(CHANNELS);
            if (channelMap.containsKey(channel)) {
                if (channelMap.get(channel) instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) channelMap.get(channel);
                    if (map.containsKey(COMMANDS)) {
                        return (Map<String, Object>) map.get(COMMANDS);
                    }
                }
            }
        }
        return new HashMap<>();
    }

    public <T> T getChannelCommandOption(Map<String, Object> commandMap, String command, String option, T def) {
        if (commandMap.containsKey(command)) {
            if (commandMap.get(command) instanceof Map) {
                Map<String, Object> optionsMap = (Map<String, Object>) commandMap.get(command);
                if (optionsMap.containsKey(option)) {
                    if (optionsMap.get(option).getClass().equals(def.getClass())) {
                        return (T) optionsMap.get(option);
                    }
                }
            }
        }
        return def;
    }

}
