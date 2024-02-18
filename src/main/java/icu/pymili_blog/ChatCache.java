package icu.pymili_blog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChatCache {
    // 缓存文件的标识符
    private static final Identifier CACHE_ID = new Identifier(FireflyChat.MOD_ID, "config/cache.json");
    // Gson 实例，用于 JSON 序列化和反序列化
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    // 缓存列表
    public static List<Message> CACHE = new ArrayList<>();

    // 加载缓存文件到缓存列表中
    public static void loadCache(MinecraftClient client) {
        String line;
        StringBuilder contentBuilder = new StringBuilder();
        try {
            if (FireflyChat.CACHE_FILE.exists() && FireflyChat.CACHE_FILE.length() > 0) {
                // 如果CACHE_FILE存在且文件长度大于0，则从文件中读取数据
                BufferedReader reader = new BufferedReader(new FileReader(FireflyChat.CACHE_FILE));
                // 读取缓存文件内容
                while ((line = reader.readLine()) != null) {
                    contentBuilder.append(line);
                }
                reader.close();
                // 写入缓存
                addCACHE(contentBuilder);
            } else {
                // 如果CACHE_FILE不存在或文件长度为0，则从其他地方获取数据，并将数据写入CACHE_FILE文件中
                ResourceManager resourceManager = client.getResourceManager();
                Optional<Resource> optionalResource = resourceManager.getResource(CACHE_ID);
                if (optionalResource.isPresent()) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(optionalResource.get().getInputStream(), StandardCharsets.UTF_8));
                    // 读取缓存文件内容
                    while ((line = reader.readLine()) != null) {
                        contentBuilder.append(line);
                    }
                    reader.close();
                    // 写入缓存
                    addCACHE(contentBuilder);
                    // 将读取到的数据写入CACHE_FILE文件中
                    FileWriter writer = new FileWriter(FireflyChat.CACHE_FILE);
                    writer.write(contentBuilder.toString());
                    writer.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addCACHE(StringBuilder contentBuilder) {
        // 解析缓存文件内容为 JSON 格式
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(contentBuilder.toString(), JsonObject.class);
        JsonArray cacheArray = jsonObject.getAsJsonArray("cache");

        // 遍历 JSON 数组并将内容添加到缓存列表中
        for (int i = 0; i < cacheArray.size(); i++) {
            JsonObject messageObject = cacheArray.get(i).getAsJsonObject();
            String role = messageObject.get("role").getAsString();
            String messageContent = messageObject.get("content").getAsString();
            CACHE.add(new Message(role, messageContent));
        }
    }


    // 向缓存列表中添加新的消息
    public static void addToCache(String role, String content) {
        CACHE.add(new Message(role, content));
        // 保存缓存
        saveCache();
    }

    // 将缓存列表保存到缓存文件中
    public static void saveCache() {
        try {
            // 转换缓存列表为 JSON 字符串
            JsonObject jsonObject = new JsonObject();
            JsonArray cacheArray = new JsonArray();
            for (Message message : CACHE) {
                JsonObject messageObject = new JsonObject();
                messageObject.addProperty("role", message.getRole());
                messageObject.addProperty("content", message.getContent());
                cacheArray.add(messageObject);
            }
            jsonObject.add("cache", cacheArray);
            String json = GSON.toJson(jsonObject);

            // 将 JSON 写入文件
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FireflyChat.CACHE_FILE))) {
                writer.write(json);
            }

            System.out.println("缓存文件保存成功");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 内部类，用于表示消息对象
    public static class Message {
        private final String role;
        private final String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }
}
