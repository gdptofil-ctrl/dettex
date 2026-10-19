/*
 * Decompiled with CFR 0.152.
 */
package ru.recoin.api.system.discord.utils;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DiscordWebhook {
    private final String url;
    private String content;
    private String username;
    private String avatarUrl;
    private boolean tts;
    private final List<EmbedObject> embeds = new ArrayList<EmbedObject>();

    public DiscordWebhook(String url) {
        this.url = url;
    }

    public void addEmbed(EmbedObject embed) {
        this.embeds.add(embed);
    }

    public void execute() throws IOException {
        // INTERNET DISABLED FOR SECURITY
        return;
    }

    private class JSONObject {
        private final HashMap<String, Object> map = new HashMap();

        private JSONObject(DiscordWebhook discordWebhook) {
        }

        void put(String key, Object value) {
            if (value != null) {
                this.map.put(key, value);
            }
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            Set<Map.Entry<String, Object>> entrySet = this.map.entrySet();
            builder.append("{");
            int i = 0;
            for (Map.Entry<String, Object> entry : entrySet) {
                Object val = entry.getValue();
                builder.append(this.quote(entry.getKey())).append(":");
                if (val instanceof String) {
                    builder.append(this.quote(String.valueOf(val)));
                } else if (val instanceof Integer) {
                    builder.append(Integer.valueOf(String.valueOf(val)));
                } else if (val instanceof Boolean) {
                    builder.append(val);
                } else if (val instanceof JSONObject) {
                    builder.append(val);
                } else if (val.getClass().isArray()) {
                    builder.append("[");
                    int len = Array.getLength(val);
                    for (int j = 0; j < len; ++j) {
                        builder.append(Array.get(val, j).toString()).append(j != len - 1 ? "," : "");
                    }
                    builder.append("]");
                }
                builder.append(++i == entrySet.size() ? "}" : ",");
            }
            return builder.toString();
        }

        private String quote(String string) {
            return "\"" + string + "\"";
        }
    }

    public static class EmbedObject {
        private String title;
        private String description;
        private String url;
        private Color color;
        private Footer footer;
        private Thumbnail thumbnail;
        private Image image;
        private Author author;
        private final List<Field> fields = new ArrayList<Field>();

        public EmbedObject setTitle(String title) {
            this.title = title;
            return this;
        }

        public EmbedObject setDescription(String description) {
            this.description = description;
            return this;
        }

        public EmbedObject setUrl(String url) {
            this.url = url;
            return this;
        }

        public EmbedObject setColor(Color color) {
            this.color = color;
            return this;
        }

        public EmbedObject setFooter(String text, String icon) {
            this.footer = new Footer(this, text, icon);
            return this;
        }

        public EmbedObject setThumbnail(String url) {
            this.thumbnail = new Thumbnail(this, url);
            return this;
        }

        public EmbedObject setImage(String url) {
            this.image = new Image(this, url);
            return this;
        }

        public EmbedObject setAuthor(String name, String url, String icon) {
            this.author = new Author(this, name, url, icon);
            return this;
        }

        public EmbedObject addField(String name, String value, boolean inline) {
            this.fields.add(new Field(this, name, value, inline));
            return this;
        }

        private class Footer {
            private final String text;
            private final String iconUrl;

            private Footer(EmbedObject embedObject, String text, String iconUrl) {
                this.text = text;
                this.iconUrl = iconUrl;
            }

            private String getText() {
                return this.text;
            }

            private String getIconUrl() {
                return this.iconUrl;
            }
        }

        private class Thumbnail {
            private final String url;

            private Thumbnail(EmbedObject embedObject, String url) {
                this.url = url;
            }

            private String getUrl() {
                return this.url;
            }
        }

        private class Image {
            private String url;

            private Image(EmbedObject embedObject, String url) {
                this.url = url;
            }

            private String getUrl() {
                return this.url;
            }
        }

        private class Author {
            private final String name;
            private final String url;
            private final String iconUrl;

            private Author(EmbedObject embedObject, String name, String url, String iconUrl) {
                this.name = name;
                this.url = url;
                this.iconUrl = iconUrl;
            }

            private String getName() {
                return this.name;
            }

            private String getUrl() {
                return this.url;
            }

            private String getIconUrl() {
                return this.iconUrl;
            }
        }

        private class Field {
            private final String name;
            private final String value;
            private final boolean inline;

            private Field(EmbedObject embedObject, String name, String value, boolean inline) {
                this.name = name;
                this.value = value;
                this.inline = inline;
            }

            private String getName() {
                return this.name;
            }

            private String getValue() {
                return this.value;
            }

            private boolean isInline() {
                return this.inline;
            }
        }
    }
}

