package icu.pymili_blog;

import icu.pymili_blog.registry.ModItems;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class FireflyChat implements ModInitializer {
	// 这个记录器用于将文本写入控制台和日志文件。
	// 最佳实践是使用您的 mod ID 作为记录器的名称。
	// 这样，清楚地知道哪个 mod 写了信息、警告和错误。
	public static final String MOD_ID = "firefly-chat";

	// 初始化配置文件
	public static final File CONFIG_FOLDER = new File(FabricLoader.getInstance().getGameDir().toFile(), MOD_ID);
	public static File CONFIG_FILE;
	public static File CACHE_FILE;

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static boolean loaded = false;

	@Override
	public void onInitialize() {
		// 这段代码在 Minecraft 处于 mod 加载就绪状态时运行。
		// 但是，一些东西（如资源）可能仍然未初始化。
		// 请小心操作。

		LOGGER.info("Hello Fabric world!");
		LOGGER.info("Hello The First test !");

		if (!CONFIG_FOLDER.exists()) {
			CONFIG_FOLDER.mkdirs();
		}
		CONFIG_FILE = new File(CONFIG_FOLDER, "config.json");
		CACHE_FILE = new File(CONFIG_FOLDER, "cache.json");

		try {
			if (!CONFIG_FILE.exists()) {
				CONFIG_FILE.createNewFile();
			}
			if (!CACHE_FILE.exists()) {
				CACHE_FILE.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		ModItems.registerModItems();
		CommandEvent.registerCommand();

		// 在客户端 Tick 事件中调用 loadApiKey 和 loadCache

		// 注册事件监听器
		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			// 只在未加载过的情况下执行
			if (!loaded) {
				FireflyGPT.loadApiKey(client);
				ChatCache.loadCache(client);

				// 设置加载标志
				loaded = true;
			}
		});
	}
}
