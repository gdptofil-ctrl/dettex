/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Vector2f
 *  org.joml.Vector3f
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL20
 *  org.lwjgl.system.MemoryStack
 */
package ru.recoin.common.util.render.shader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

public class Shader {
    private final int programId;
    private final String vertexPath;
    private final String fragmentPath;
    private final Map<String, Integer> uniformLocations = new HashMap<String, Integer>();

    public Shader(String folder, String name) {
        String vPath = "/assets/minecraft/shaders/" + folder + "/" + name + ".vsh";
        String fPath = "/assets/minecraft/shaders/" + folder + "/" + name + ".fsh";
        if (Shader.class.getResourceAsStream(vPath) == null) {
            vPath = "/assets/recoin/shaders/" + folder + "/" + name + ".vsh";
            fPath = "/assets/recoin/shaders/" + folder + "/" + name + ".fsh";
        }
        this.vertexPath = vPath;
        this.fragmentPath = fPath;
        int vertexId = this.createShader(this.vertexPath, 35633);
        int fragmentId = this.createShader(this.fragmentPath, 35632);
        this.programId = GL20.glCreateProgram();
        GL20.glAttachShader((int)this.programId, (int)vertexId);
        GL20.glAttachShader((int)this.programId, (int)fragmentId);
        GL20.glLinkProgram((int)this.programId);
        if (GL20.glGetProgrami((int)this.programId, (int)35714) == 0) {
            throw new RuntimeException("Failed to link shader program: " + GL20.glGetProgramInfoLog((int)this.programId));
        }
        GL20.glDeleteShader((int)vertexId);
        GL20.glDeleteShader((int)fragmentId);
    }

    private int createShader(String path, int type) {
        try {
            String source = this.readShaderFile(path);
            int shaderId = GL20.glCreateShader((int)type);
            GL20.glShaderSource((int)shaderId, (CharSequence)source);
            GL20.glCompileShader((int)shaderId);
            if (GL20.glGetShaderi((int)shaderId, (int)35713) == 0) {
                throw new RuntimeException("Failed to compile shader: " + path + "\n" + GL20.glGetShaderInfoLog((int)shaderId));
            }
            return shaderId;
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load shader: " + path, e);
        }
    }

    private String readShaderFile(String path) throws IOException {
        InputStream stream = Shader.class.getResourceAsStream(path);
        if (stream == null) {
            throw new IOException("Shader file not found: " + path);
        }
        StringBuilder source = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream));){
            String line;
            while ((line = reader.readLine()) != null) {
                source.append(line).append("\n");
            }
        }
        return source.toString();
    }

    public void bind() {
        GL20.glUseProgram((int)this.programId);
    }

    public void unbind() {
        GL20.glUseProgram((int)0);
    }

    public void setUniform1i(String name, int value) {
        this.ensureBound();
        int location = this.uniformLocation(name);
        if (location != -1) {
            GL20.glUniform1i((int)location, (int)value);
        }
    }

    public void setUniform1f(String name, float value) {
        this.ensureBound();
        int location = this.uniformLocation(name);
        if (location != -1) {
            GL20.glUniform1f((int)location, (float)value);
        }
    }

    public void setUniform2f(String name, float x, float y) {
        this.ensureBound();
        int location = this.uniformLocation(name);
        if (location != -1) {
            GL20.glUniform2f((int)location, (float)x, (float)y);
        }
    }

    public void setUniform2f(String name, Vector2f vec) {
        this.setUniform2f(name, vec.x, vec.y);
    }

    public void setUniform3f(String name, float x, float y, float z) {
        this.ensureBound();
        int location = this.uniformLocation(name);
        if (location != -1) {
            GL20.glUniform3f((int)location, (float)x, (float)y, (float)z);
        }
    }

    public void setUniform3f(String name, Vector3f vec) {
        this.setUniform3f(name, vec.x, vec.y, vec.z);
    }

    public void setUniform4f(String name, float x, float y, float z, float w) {
        this.ensureBound();
        int location = this.uniformLocation(name);
        if (location != -1) {
            GL20.glUniform4f((int)location, (float)x, (float)y, (float)z, (float)w);
        }
    }

    public void setUniformMatrix4f(String name, Matrix4f matrix) {
        this.ensureBound();
        int location = this.uniformLocation(name);
        if (location == -1) {
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush();){
            FloatBuffer buffer = stack.mallocFloat(16);
            matrix.get(buffer);
            GL20.glUniformMatrix4fv((int)location, (boolean)false, (FloatBuffer)buffer);
        }
    }

    public void setUniformBool(String name, boolean value) {
        this.setUniform1i(name, value ? 1 : 0);
    }

    private void ensureBound() {
        if (GL11.glGetInteger((int)35725) != this.programId) {
            GL20.glUseProgram((int)this.programId);
        }
    }

    private int uniformLocation(String name) {
        return this.uniformLocations.computeIfAbsent(name, key -> GL20.glGetUniformLocation((int)this.programId, (CharSequence)key));
    }

    public void delete() {
        GL20.glDeleteProgram((int)this.programId);
    }
}

