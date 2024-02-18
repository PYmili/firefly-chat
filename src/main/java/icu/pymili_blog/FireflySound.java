package icu.pymili_blog;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import icu.pymili_blog.sounds.ModSoundEvents;
import icu.pymili_blog.sounds.SoundPlayer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.sound.SoundEvent;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FireflySound {
    private static final String GENSHINVOICE_API = "https://v2.genshinvoice.top/run/predict";
    private static final Map<String, String> HEADERS = new HashMap<>();
    private static final File SOUND_FOLDER = new File(FabricLoader.getInstance().getModContainer(FireflyChat.MOD_ID)
            .get().getRootPath().resolve("assets").resolve(FireflyChat.MOD_ID).resolve("sounds").toFile(), "firefly_sound.wav");

    static {
        HEADERS.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.");
        HEADERS.put("Cookie", "_gid=GA1.2.1077423415.1708097946; _gat_gtag_UA_156449732_1=1; _ga_R1FN4KJKJH=GS1.1.1708097945.1.1.1708100257.0.0.0; _ga=GA1.2.312353829.1708097946");
        HEADERS.put("Referer", "https://v2.genshinvoice.top/?");
    }

    public static String run(String content, String textPrompt) {
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();

        Map<String, Object> postData = new HashMap<>();
        postData.put("data", new Object[]{content, "流萤_ZH", 0.5, 0.6, 0.9, 1, "ZH", null, textPrompt, "Text prompt", "", 0.7});
        postData.put("event_data", null);
        postData.put("fn_index", 0);
        postData.put("session_hash", "uyzlm3r8d6");

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), gson.toJson(postData));
        Request request = new Request.Builder()
                .url(GENSHINVOICE_API)
                .headers(okhttp3.Headers.of(HEADERS))
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                if (responseBody != null) {
                    JsonObject responseData = gson.fromJson(responseBody, JsonObject.class);
                    JsonArray dataArray = responseData.getAsJsonArray("data");
                    if (dataArray.size() > 1) {
                        JsonObject audioData = dataArray.get(1).getAsJsonObject();
                        String audioFileName = audioData.get("name").getAsString();
                        String audioUrl = "https://v2.genshinvoice.top/file=" + audioFileName;
                        System.out.println("获取到音频：" + audioUrl);
                        saveAudioToFile(audioUrl);

                        // 音频播放
                        SoundEvent fireflySound = ModSoundEvents.getFireflySound();
                        if (fireflySound != null) {
                            System.out.println("尝试播放音频：" + FireflyChat.MOD_ID + "/sounds/firefly_sound.wav");
                            SoundPlayer.playSound(fireflySound);
                        } else {
                            System.err.println("尝试播放音频失败：声音事件未加载成功！");
                        }

                        return audioUrl; // 返回数据
                    } else {
                        return "API 返回数据格式错误，缺少音频信息";
                    }
                }
            } else {
                // 请求失败，返回状态码和错误信息
                return "请求失败，状态码：" + response.code() + "，错误信息：" + response.message();
            }
        } catch (IOException e) {
            e.printStackTrace();
            // 异常发生，返回异常信息
            return "发生异常：" + e.getMessage();
        }

        // 默认返回空字符串
        return "";
    }

    private static void saveAudioToFile(String audioUrl) {
        System.out.println("保存音频文件" + audioUrl);
        try (InputStream in = new URL(audioUrl).openStream();
             BufferedInputStream bin = new BufferedInputStream(in);
             BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(SOUND_FOLDER))) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = bin.read(buffer)) != -1) {
                bout.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void test(String content, String textPrompt) {
        run(content, textPrompt);
    }
}
