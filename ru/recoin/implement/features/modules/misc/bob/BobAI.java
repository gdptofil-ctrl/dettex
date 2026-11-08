package ru.recoin.implement.features.modules.misc.bob;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bob's "smart" brain: a language model answers anything and can toggle client modules.
 * Providers:
 *  - Ollama: free, runs on your own PC (https://ollama.com), no key, nothing leaves your computer;
 *  - OpenAI-compatible: any service with the /v1/chat/completions API (OpenAI, OpenRouter, Groq, LM Studio...);
 *  - Anthropic: Claude models with your API key.
 * The model may put [ON:Module] / [OFF:Module] in its answer: Bob toggles the module and removes the tag.
 */
public final class BobAI {
    public record Config(String provider, String url, String key, String model, String context) {
    }

    public record Answer(String text, List<String> enable, List<String> disable) {
    }

    private static final Pattern ACTION = Pattern.compile("\\[(ON|OFF):\\s*([^\\]]+)]", Pattern.CASE_INSENSITIVE);
    private static final int MAX_TURNS = 10;
    private final List<String[]> history = new ArrayList<>();   // {role, text}
    private HttpClient http;

    public static String defaultUrl(String provider) {
        return switch (provider) {
            case "Ollama" -> "http://localhost:11434/v1/chat/completions";
            case "Anthropic" -> "https://api.anthropic.com/v1/messages";
            default -> "https://api.openai.com/v1/chat/completions";
        };
    }

    public static String defaultModel(String provider) {
        return switch (provider) {
            case "Ollama" -> "qwen2.5:7b";
            case "Anthropic" -> "claude-haiku-5-5";
            default -> "gpt-4o-mini";
        };
    }

    public void forget() {
        this.history.clear();
    }

    static String systemPrompt(String context) {
        return "Ты Боб, весёлый голосовой помощник-робот внутри Minecraft-клиента Dettex. "
                + "Отвечай по-русски, коротко: 1-3 предложения, без списков и без смайликов, текст будет озвучен голосом. "
                + "Ты хорошо знаешь Minecraft: крафты, мобов, механики, версии, сервера. "
                + "Если игрок просит включить или выключить функцию клиента, добавь в ответ тег [ON:ИмяМодуля] или [OFF:ИмяМодуля] "
                + "с точным именем модуля из списка. Не включай модули без просьбы. "
                + "Если не знаешь ответа, честно скажи об этом.\n" + context;
    }

    /** Blocking call (run it off the render thread). */
    public Answer ask(String message, Config cfg) throws Exception {
        if (this.http == null) {
            this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
        }
        String provider = cfg.provider();
        String url = cfg.url() == null || cfg.url().isBlank() ? BobAI.defaultUrl(provider) : cfg.url().trim();
        String model = cfg.model() == null || cfg.model().isBlank() ? BobAI.defaultModel(provider) : cfg.model().trim();
        String system = BobAI.systemPrompt(cfg.context());
        JsonArray messages = new JsonArray();
        List<String[]> turns = new ArrayList<>(this.history);
        turns.add(new String[]{"user", message});
        JsonObject body = new JsonObject();
        if ("Ollama".equals(provider) && (cfg.model() == null || cfg.model().isBlank())) {
            model = this.ollamaModel(url, model);   // use a model that is really installed
        }
        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(150)).header("Content-Type", "application/json");
        if ("Anthropic".equals(provider)) {
            for (String[] t : turns) messages.add(BobAI.msg(t[0], t[1]));
            body.addProperty("model", model);
            body.addProperty("max_tokens", 300);
            body.addProperty("system", system);
            body.add("messages", messages);
            req.header("x-api-key", cfg.key() == null ? "" : cfg.key().trim()).header("anthropic-version", "2023-06-01");
        } else {
            messages.add(BobAI.msg("system", system));
            for (String[] t : turns) messages.add(BobAI.msg(t[0], t[1]));
            body.addProperty("model", model);
            body.addProperty("max_tokens", 300);
            body.addProperty("temperature", 0.7);
            body.add("messages", messages);
            if (cfg.key() != null && !cfg.key().isBlank()) {
                req.header("Authorization", "Bearer " + cfg.key().trim());
            }
        }
        HttpResponse<String> resp = this.http.send(req.POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8)).build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp.statusCode() == 404 && "Ollama".equals(provider)) {
            this.installed = null;
            throw new IllegalStateException("в Ollama нет модели «" + model + "». Скачай её командой «ollama pull " + model + "» или впиши в Bob → Model имя модели из «ollama list».");
        }
        if (resp.statusCode() != 200) {
            throw new IllegalStateException("HTTP " + resp.statusCode() + ": " + BobAI.errorText(resp.body()));
        }
        String text = BobAI.extract(provider, resp.body());
        this.history.add(new String[]{"user", message});
        this.history.add(new String[]{"assistant", text});
        while (this.history.size() > MAX_TURNS * 2) {
            this.history.remove(0);
        }
        return BobAI.parseActions(text);
    }

    private String installed;

    /** First model from Ollama's list (preferring Qwen / Llama / Gemma / Mistral), or the default. */
    private String ollamaModel(String chatUrl, String fallback) {
        if (this.installed != null) {
            return this.installed;
        }
        try {
            URI u = URI.create(chatUrl);
            URI tags = new URI(u.getScheme(), null, u.getHost(), u.getPort(), "/api/tags", null, null);
            HttpResponse<String> r = this.http.send(HttpRequest.newBuilder(tags).timeout(Duration.ofSeconds(5)).GET().build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (r.statusCode() == 200) {
                JsonArray models = JsonParser.parseString(r.body()).getAsJsonObject().getAsJsonArray("models");
                String best = null;
                for (JsonElement e : models) {
                    String name = e.getAsJsonObject().get("name").getAsString();
                    if (name.contains("embed")) continue;
                    if (best == null) best = name;
                    if (name.startsWith("qwen") || name.startsWith("llama") || name.startsWith("gemma") || name.startsWith("mistral")) {
                        best = name;
                        break;
                    }
                }
                if (best != null) {
                    this.installed = best;
                    return best;
                }
                throw new IllegalStateException("в Ollama не скачано ни одной модели. Выполни в командной строке «ollama pull qwen2.5:7b».");
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception ignored) {
            // the chat request below will report the real problem
        }
        return fallback;
    }

    private static JsonObject msg(String role, String content) {
        JsonObject o = new JsonObject();
        o.addProperty("role", role);
        o.addProperty("content", content);
        return o;
    }

    static String extract(String provider, String json) {
        JsonObject o = JsonParser.parseString(json).getAsJsonObject();
        if ("Anthropic".equals(provider)) {
            StringBuilder sb = new StringBuilder();
            for (JsonElement e : o.getAsJsonArray("content")) {
                JsonObject block = e.getAsJsonObject();
                if (block.has("text")) sb.append(block.get("text").getAsString());
            }
            return sb.toString().trim();
        }
        return o.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString().trim();
    }

    static String errorText(String body) {
        try {
            JsonObject o = JsonParser.parseString(body).getAsJsonObject();
            if (o.has("error")) {
                JsonElement e = o.get("error");
                return e.isJsonObject() && e.getAsJsonObject().has("message") ? e.getAsJsonObject().get("message").getAsString() : e.toString();
            }
        } catch (Exception ignored) {
        }
        return body.length() > 160 ? body.substring(0, 160) : body;
    }

    /** Takes [ON:x] / [OFF:x] tags out of the text. */
    static Answer parseActions(String text) {
        List<String> on = new ArrayList<>();
        List<String> off = new ArrayList<>();
        Matcher m = ACTION.matcher(text);
        while (m.find()) {
            (m.group(1).equalsIgnoreCase("ON") ? on : off).add(m.group(2).trim());
        }
        String clean = ACTION.matcher(text).replaceAll("").replaceAll("[*_#`]", "").replaceAll("\\s{2,}", " ").trim();
        return new Answer(clean, on, off);
    }
}
