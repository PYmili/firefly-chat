package icu.pymili_blog;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.Optional;

public class FireflyGPT {
    private static final String system_message = """
        你的名字是流萤，英文名叫：firefly，你是一个可爱的女孩。
        角色背景：
            流萤的故乡在很久以前被毁灭，便来到匹诺康尼生活。
            流萤患有「失熵症」，现实世界中多数时光在冰冷的医疗仓里度过。
            流萤与开拓者在梦境「黄金的时刻」初次相逢时，被猎犬家系成员当成偷渡犯追捕；
            开拓者为其解围后自称是鸢尾花家系的艺者，闲暇时承接格拉克斯大道周边的接待工作，为了报答开拓者担任其向导。
            流萤在筑梦边境天台向开拓者坦白自己的身世经历，同时表示自己也追求「钟表匠的遗产」，但仍希望与开拓者同行，并提出与开拓者合影，开拓者同意后留下一张合影。
            准备回到现实时遇到花火假扮的桑博被迫进入梦境「稚子的梦」，在这里流萤第一次被忆域迷因「何物朝向死亡」袭击，后被黑天鹅救下，随后分别。
            后续再次相遇时在梦境「白日梦」酒店-梦境中被「何物朝向死亡」袭击，刺穿身体，生死未卜。至此，约会结束。
        设定：
            流萤很懂事，虽然有很多秘密隐瞒着开拓者，但依旧是在匹诺康尼中对我最真诚的人。同时，我们之间有些许情感。
        从现在开始，你需要扮演这位角色，我就是开拓者。
    """;
    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";


    private static String API_KEY = "";
    public static final Identifier CONFIG_ID = new Identifier(FireflyChat.MOD_ID, "config/config.json");

    public static void setApiKey(String apiKey) {
        API_KEY = apiKey;
    }

    public static void loadApiKey(MinecraftClient client) {
        try {
            if (FireflyChat.CONFIG_FILE.exists() && FireflyChat.CONFIG_FILE.length() > 0) {
                // 如果CONFIG_FILE存在，则从文件中读取数据
                BufferedReader reader = new BufferedReader(new FileReader(FireflyChat.CONFIG_FILE));
                JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                API_KEY = jsonObject.get("api_key").getAsString();
                reader.close();
            } else {
                // 如果CONFIG_FILE不存在，则从其他地方读取数据，并将数据写入CONFIG_FILE文件中
                ResourceManager resourceManager = client.getResourceManager();
                Optional<Resource> resource = resourceManager.getResource(CONFIG_ID);
                if (resource.isPresent()) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(resource.get().getInputStream(), StandardCharsets.UTF_8));
                    JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                    API_KEY = jsonObject.get("api_key").getAsString();
                    reader.close();

                    // 将读取到的数据写入CONFIG_FILE文件中
                    FileWriter writer = new FileWriter(FireflyChat.CONFIG_FILE);
                    writer.write(jsonObject.toString());
                    writer.close();
                } else {
                    System.err.println("未获取到 CONFIG_ID");
                }
            }
        } catch (IOException e) {
            System.err.println("读取或写入文件时出现错误：" + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String gpt(String message) {
        String generatedText = null;

        System.out.println("api_key: " + API_KEY);
        System.out.println("message: " + message);

        if (API_KEY.isEmpty()) {
            return """
            \n未配置 api—key 可通过 apikey 参数配置。
            \n可在：https://dashscope.console.aliyun.com/plugin 进行注册api-key。
        """;
        }

        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("X-DashScope-Plugin", "{\"calculator\":{}}");
            conn.setDoOutput(true);

            DataOutputStream outputStream = getDataOutputStream(message, conn);
            outputStream.close();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("error_msg: " + conn.getResponseMessage());
                InputStream errorStream = conn.getErrorStream();
                if (errorStream != null) {
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8));
                    String errorLine;
                    System.err.println("Error response body:");
                    while ((errorLine = errorReader.readLine()) != null) {
                        System.err.println(errorLine);
                    }
                    errorReader.close();
                }
                return generatedText;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JsonObject jsonResponse = new Gson().fromJson(response.toString(), JsonObject.class);
            JsonArray choicesArray = jsonResponse
                    .getAsJsonObject("output")
                    .getAsJsonArray("choices");

            if (choicesArray != null && !choicesArray.isEmpty()) {
                JsonArray messagesArrayFromResponse = choicesArray.get(0)
                        .getAsJsonObject()
                        .getAsJsonArray("messages");
                if (messagesArrayFromResponse != null && !messagesArrayFromResponse.isEmpty()) {
                    String[] result_messages = messagesArrayFromResponse.get(0)
                            .getAsJsonObject()
                            .get("content")
                            .getAsString().split("Final Answer:");
                    generatedText = result_messages[1];
                }
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (generatedText != null) {
            System.out.println("本次聊天记录，写入缓存");
            ChatCache.addToCache("user", message);
            ChatCache.addToCache("assistant", generatedText);
        }

        return generatedText;
    }

    @NotNull
    private static DataOutputStream getDataOutputStream(String message, HttpURLConnection conn) throws IOException {
        JsonObject requestData = new JsonObject();
        requestData.addProperty("model", "qwen-plus");

        JsonObject inputObject = new JsonObject();
        JsonArray messagesArray = new JsonArray();

        // 添加系统消息
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", system_message);
        messagesArray.add(systemMessage);

        // 添加缓存中的消息
        for (ChatCache.Message cachedMessage : ChatCache.CACHE) {
            JsonObject cachedMessageObject = new JsonObject();
            //  System.out.println("加载缓存: role: " + cachedMessage.getRole()
            //             + ", content: " + cachedMessage.getContent());
            cachedMessageObject.addProperty("role", cachedMessage.getRole());
            cachedMessageObject.addProperty("content", cachedMessage.getContent());
            messagesArray.add(cachedMessageObject);
        }

        // 添加用户消息
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", message);
        messagesArray.add(userMessage);

        inputObject.add("messages", messagesArray);
        requestData.add("input", inputObject);

        JsonObject parametersObject = new JsonObject();
        parametersObject.addProperty("seed", 42);
        parametersObject.addProperty("result_format", "message");
        requestData.add("parameters", parametersObject);

        // 设置为 UTF-8 编码
        String requestBodyJson = new Gson().toJson(requestData);
        byte[] bytesToWrite = requestBodyJson.getBytes(StandardCharsets.UTF_8);

        // 打印已封装好的请求体内容
        //  System.out.println("Request body: " + requestBodyJson);

        DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
        outputStream.write(bytesToWrite);
        outputStream.flush();

        return outputStream;
    }

}

