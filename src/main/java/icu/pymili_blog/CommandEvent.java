package icu.pymili_blog;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.*;
import java.io.*;

public class CommandEvent {
    // 注册自定义命令
    public static void registerCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    literal("firefly")
                            // 可以通过此参数设置 api key
                            .then(literal("apikey")
                                    .then(argument("key", StringArgumentType.string())
                                            .executes(context -> {
                                                String apiKey = StringArgumentType.getString(context, "key");
                                                storeApiKey(apiKey); // 调用存储 API Key 的函数
                                                context.getSource().sendFeedback(() -> Text.literal("API Key stored: " + apiKey), false);
                                                return 1;
                                            })
                                    )
                            )
                            // 获取信息
                            .then(argument("message", StringArgumentType.string())
                                    .executes(context -> {
                                        String message = StringArgumentType.getString(context, "message");
                                        ServerCommandSource source = context.getSource();
                                        source.sendFeedback(() -> Text.literal("User: " + message), false);
                                        handleFireFlyChat(context.getSource(), message); // 处理 FireFly 聊天
                                        return 1;
                                    })
                            )
            );
        });
    }

    // 处理 FireFly 聊天
    private static void handleFireFlyChat(ServerCommandSource source, String message) {
        // 使用 FireflyGPT 类的 gpt 方法生成文本
        String[] generatedText = FireflyGPT.gpt(message).split("</end>");
        // 构建反馈文本
        Text feedbackText = Text.of("FireFly: " + generatedText[0]);
        // 发送生成的文本到 Minecraft 服务器
        source.sendFeedback(() -> feedbackText, false);

        // 播放音频
        // 未实现，此处应该调用 firefly_sound.run(generatedText[0], generatedText[1]) 方法获取音频文件并播放
        // String sound_result = firefly_sound.run(generatedText[0], generatedText[1]);
        // System.out.println("音频事件返回：" + sound_result);
    }

    public static void storeApiKey(String apiKey) {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("api_key", apiKey);
        String json = gson.toJson(jsonObject);

        FireflyGPT.setApiKey(apiKey);

        try (FileWriter writer = new FileWriter(FireflyChat.CONFIG_FILE)) {
            writer.write(json);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
