/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package ru.recoin.implement.features.modules.combat.killaura.rotation.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;

final class NeuroModel {
    private final int hidden;
    private final int mix;
    private final int freezeCut;
    private final float[] limits;
    private final float[] error;
    private final float[][] wi;
    private final float[][] wh;
    private final float[] bi;
    private final float[] bh;
    private final float[][] outputWeights;
    private final float[] outputBias;

    private NeuroModel(JsonObject json) {
        if (!"gru".equals(json.get("kind").getAsString()) || json.get("features").getAsInt() != 17 || !"deg".equals(json.get("units").getAsString())) {
            throw new IllegalArgumentException("\u041d\u0435\u043f\u043e\u0434\u0434\u0435\u0440\u0436\u0438\u0432\u0430\u0435\u043c\u044b\u0439 \u0444\u043e\u0440\u043c\u0430\u0442 \u043d\u0435\u0439\u0440\u043e-\u043c\u043e\u0434\u0435\u043b\u0438");
        }
        this.hidden = json.get("hidden").getAsInt();
        this.mix = json.get("mix").getAsInt();
        this.freezeCut = json.has("freezeCut") ? json.get("freezeCut").getAsInt() : 12;
        this.limits = NeuroModel.vector(json.getAsJsonArray("limits"));
        this.error = json.has("error") ? NeuroModel.vector(json.getAsJsonArray("error")) : new float[]{};
        JsonObject gru = json.getAsJsonObject("gru");
        this.wi = NeuroModel.matrix(gru.getAsJsonArray("wi"));
        this.wh = NeuroModel.matrix(gru.getAsJsonArray("wh"));
        this.bi = NeuroModel.vector(gru.getAsJsonArray("bi"));
        this.bh = NeuroModel.vector(gru.getAsJsonArray("bh"));
        JsonObject out = json.getAsJsonObject("out");
        this.outputWeights = NeuroModel.matrix(out.getAsJsonArray("w"));
        this.outputBias = NeuroModel.vector(out.getAsJsonArray("b"));
        if (this.hidden < 1 || this.mix < 1 || this.limits.length != 2 || this.wi.length != this.hidden * 3 || this.wh.length != this.hidden * 3 || this.bi.length != this.hidden * 3 || this.bh.length != this.hidden * 3 || this.outputWeights.length != this.mix * 6 + 1 || this.outputBias.length != this.mix * 6 + 1) {
            throw new IllegalArgumentException("\u041f\u043e\u0432\u0440\u0435\u0436\u0434\u0451\u043d\u043d\u044b\u0435 \u0440\u0430\u0437\u043c\u0435\u0440\u044b \u043d\u0435\u0439\u0440\u043e-\u043c\u043e\u0434\u0435\u043b\u0438");
        }
        for (float[] row : this.wi) {
            if (row.length == 17) continue;
            throw new IllegalArgumentException("\u041d\u0435\u0432\u0435\u0440\u043d\u044b\u0439 \u0440\u0430\u0437\u043c\u0435\u0440 \u0432\u0445\u043e\u0434\u0430 GRU");
        }
        for (float[] row : this.wh) {
            if (row.length == this.hidden) continue;
            throw new IllegalArgumentException("\u041d\u0435\u0432\u0435\u0440\u043d\u044b\u0439 \u0440\u0430\u0437\u043c\u0435\u0440 \u0441\u043e\u0441\u0442\u043e\u044f\u043d\u0438\u044f GRU");
        }
        for (float[] row : this.outputWeights) {
            if (row.length == this.hidden) continue;
            throw new IllegalArgumentException("\u041d\u0435\u0432\u0435\u0440\u043d\u044b\u0439 \u0440\u0430\u0437\u043c\u0435\u0440 \u0432\u044b\u0445\u043e\u0434\u0430 GRU");
        }
    }

    static NeuroModel load(Path path) throws Exception {
        if (Files.isRegularFile(path, new LinkOption[0])) {
            return new NeuroModel(JsonParser.parseString((String)Files.readString(path)).getAsJsonObject());
        }
        if (!"default.json".equals(path.getFileName().toString())) {
            return null;
        }
        try (InputStream stream = NeuroModel.class.getClassLoader().getResourceAsStream("assets/recoin/neuro/default.json");){
            if (stream == null) {
                NeuroModel neuroModel = null;
                return neuroModel;
            }
            NeuroModel neuroModel = new NeuroModel(JsonParser.parseReader((Reader)new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject());
            return neuroModel;
        }
    }

    int freezeCut() {
        return this.freezeCut;
    }

    float[] state() {
        return new float[this.hidden];
    }

    float[] forward(float[] input, float[] state) {
        int i;
        float[] a = new float[this.hidden * 3];
        float[] b = new float[this.hidden * 3];
        for (i = 0; i < a.length; ++i) {
            int j;
            float x = this.bi[i];
            float h = this.bh[i];
            for (j = 0; j < 17; ++j) {
                x += this.wi[i][j] * input[j];
            }
            for (j = 0; j < this.hidden; ++j) {
                h += this.wh[i][j] * state[j];
            }
            a[i] = x;
            b[i] = h;
        }
        for (i = 0; i < this.hidden; ++i) {
            float reset = NeuroModel.sigmoid(a[i] + b[i]);
            float update = NeuroModel.sigmoid(a[this.hidden + i] + b[this.hidden + i]);
            float candidate = (float)Math.tanh(a[this.hidden * 2 + i] + reset * b[this.hidden * 2 + i]);
            state[i] = (1.0f - update) * candidate + update * state[i];
        }
        float[] output = new float[this.outputWeights.length];
        for (int i2 = 0; i2 < output.length; ++i2) {
            float sum = this.outputBias[i2];
            for (int j = 0; j < this.hidden; ++j) {
                sum += this.outputWeights[i2][j] * state[j];
            }
            output[i2] = sum;
        }
        return output;
    }

    int sampleMix(float[] output) {
        float max = output[1];
        for (int i = 1; i < this.mix; ++i) {
            max = Math.max(max, output[1 + 6 * i]);
        }
        float total = 0.0f;
        for (int i = 0; i < this.mix; ++i) {
            total += (float)Math.exp(output[1 + 6 * i] - max);
        }
        float value = ThreadLocalRandom.current().nextFloat() * total;
        for (int i = 0; i < this.mix; ++i) {
            if (!((value -= (float)Math.exp(output[1 + 6 * i] - max)) <= 0.0f)) continue;
            return i;
        }
        return this.mix - 1;
    }

    float error(float quantile) {
        if (this.error.length == 0) {
            return 0.0f;
        }
        float position = Math.max(0.0f, Math.min(1.0f, quantile)) * (float)(this.error.length - 1);
        int index = (int)position;
        if (index >= this.error.length - 1) {
            return this.error[this.error.length - 1];
        }
        return this.error[index] + (this.error[index + 1] - this.error[index]) * (position - (float)index);
    }

    float sample(float mean, float logScale, boolean yaw, float gaussian, float temperature) {
        float scale = (float)Math.exp(Math.max(-4.0f, Math.min(1.5f, logScale)));
        float latent = Math.max(-8.0f, Math.min(8.0f, mean + temperature * scale * gaussian));
        float limit = this.limits[yaw ? 0 : 1];
        return Math.max(-limit, Math.min(limit, (float)Math.sinh(latent)));
    }

    static float sigmoid(float value) {
        return 1.0f / (1.0f + (float)Math.exp(-value));
    }

    private static float[] vector(JsonArray json) {
        float[] result = new float[json.size()];
        for (int i = 0; i < result.length; ++i) {
            result[i] = json.get(i).getAsFloat();
        }
        return result;
    }

    private static float[][] matrix(JsonArray json) {
        float[][] result = new float[json.size()][];
        for (int i = 0; i < result.length; ++i) {
            result[i] = NeuroModel.vector(json.get(i).getAsJsonArray());
        }
        return result;
    }
}

