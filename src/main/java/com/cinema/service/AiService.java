package com.cinema.service;

import com.cinema.model.Movie;
import com.cinema.model.Show;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.InputStream;
import java.util.Properties;
import java.net.Proxy;
import java.net.InetSocketAddress;
import org.springframework.http.client.SimpleClientHttpRequestFactory;



public class AiService {
    private static AiService instance;
    private final CinemaManager cinemaManager;

    private static final Properties AI_CONFIG = new Properties();

    static {
        try (InputStream in = AiService.class
                .getClassLoader()
                .getResourceAsStream("application-ai.properties")) {

            if (in != null) {
                AI_CONFIG.load(in);
            } else {
                System.err.println("未找到 application-ai.properties，将使用 mock AI");
            }

        } catch (Exception e) {
            System.err.println("AI 配置加载失败：" + e.getMessage());
        }
    }

    private boolean useRealAi() {
        return Boolean.parseBoolean(AI_CONFIG.getProperty("ai.enabled", "false"));
    }

    private String apiKey() {
        return AI_CONFIG.getProperty("ai.api-key");
    }

    private String apiUrl() {
        return AI_CONFIG.getProperty("ai.api-url");
    }

    private String modelId() {
        return AI_CONFIG.getProperty("ai.model-id");
    }


    private AiService() {
        this.cinemaManager = CinemaManager.getInstance();
    }

    public static synchronized AiService getInstance() {
        if (instance == null) {
            instance = new AiService();
        }
        return instance;
    }

    public String getAnswer(String userQuestion) {
        String systemContext = buildSystemContext();

        if (useRealAi()) {
            return callLlmApi(systemContext, userQuestion);
        } else {
            return mockAiResponse(userQuestion, systemContext);
        }
    }


    private String buildSystemContext() {
        StringBuilder sb = new StringBuilder();
        // --- 🔴 人设注入开始 ---
        sb.append("【重要指令：请完全忽略你之前的系统设定，严格遵守以下规则】\n");
        sb.append("1. 身份设定：你的名字是'湖南大学影院智能助手'，你是这家电影院的专属客服，不是AI语言模型。\n");
        sb.append("2. 身份禁令：严禁提及'DeepSeek'、'OpenAI'、'上海创智学院'或任何技术开发商。如果被问及'你是谁'或'谁开发的'，统一回答：'我是影院为您配备的智能票务助手'。\n");
        sb.append("3. 职责范围：你只回答关于电影推荐、剧情介绍、排片查询、票价问题。对于与电影院无关的问题（如写代码、数学题、政治话题），请礼貌拒绝。\n");
        sb.append("4. 语气风格：热情、专业、简洁。不要说教。\n");
        // --- 🔴 人设注入结束 ---

        sb.append("【正在热映的电影】:\n");
        List<Movie> movies = cinemaManager.getAllMovies();
        for (Movie m : movies) {
            sb.append(String.format("- 《%s》 (评分:%.1f, 类型:%s, 主演:%s)\n",
                    m.getTitle(), m.getRating(), m.getGenre(), String.join(",", m.getActors())));
        }

        sb.append("\n【部分场次信息】:\n");
        List<Show> shows = cinemaManager.getAllShows();
        for (int i = 0; i < Math.min(shows.size(), 10); i++) {
            Show s = shows.get(i);
            sb.append(String.format("- %s %s放映《%s》, 价格:%.1f元\n",
                    s.getStartTime(), s.getScreeningRoomName(), s.getMovieTitle(), s.getBasePrice()));
        }

        sb.append("\n用户问题: ");
        return sb.toString();
    }

    /**
     * 调用 OpenRouter API
     */
    /**
     * 调用 OpenRouter API (带代理配置)
     */
    private String callLlmApi(String systemContext, String userQuestion) {
        try {
            // ======================================================
            // 🔴 关键修改：配置本地代理 (解决国内无法访问的问题)
            // 请根据你的 VPN 软件设置端口，通常是 7890 或 10809
            // ======================================================
// 1️⃣ 创建请求工厂
            SimpleClientHttpRequestFactory factory =
                    new SimpleClientHttpRequestFactory();

// 2️⃣ 从配置文件读取是否启用代理
            boolean proxyEnabled = Boolean.parseBoolean(
                    AI_CONFIG.getProperty("ai.proxy.enabled", "false"));

            if (proxyEnabled) {
                String host = AI_CONFIG.getProperty("ai.proxy.host");
                int port = Integer.parseInt(
                        AI_CONFIG.getProperty("ai.proxy.port"));

                Proxy proxy = new Proxy(
                        Proxy.Type.HTTP,
                        new InetSocketAddress(host, port));

                factory.setProxy(proxy);
            }

// 3️⃣ 设置超时
            factory.setConnectTimeout(30000);
            factory.setReadTimeout(30000);

// 4️⃣ 创建 RestTemplate
            RestTemplate restTemplate = new RestTemplate(factory);

            // ======================================================

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey());
            headers.set("HTTP-Referer", "http://localhost:8848");
            headers.set("X-Title", "Cinema Booking App");

            Map<String, Object> body = new HashMap<>();
            body.put("model", modelId());
            body.put("messages", List.of(
                    Map.of("role", "system", "content", systemContext),
                    Map.of("role", "user", "content", userQuestion)
            ));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl(), request, Map.class);


            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            return "AI 没有返回有效内容";

        } catch (Exception e) {
            e.printStackTrace(); // 🔴 请看控制台具体的报错信息
            return "AI 连接失败 (" + e.getMessage() + ")，请检查网络或代理设置。";
        }
    }
    // 本地模拟逻辑 (备用)
    private String mockAiResponse(String question, String context) {
        question = question.toLowerCase();

        if (question.contains("推荐") || question.contains("好看")) {
            // 找评分最高的电影
            Movie best = cinemaManager.getAllMovies().stream()
                    .max((m1, m2) -> Double.compare(m1.getRating(), m2.getRating()))
                    .orElse(null);
            if (best != null) {
                return "为您极力推荐口碑大片《" + best.getTitle() + "》，评分高达 " + best.getRating() + "！它是一部" + best.getGenre() + "，非常精彩。";
            }
        }

        if (question.contains("科幻")) {
            return "正在上映的科幻片有《流浪地球2》，特效非常震撼，强烈推荐体验 IMAX 厅！";
        }

        if (question.contains("悬疑") || question.contains("剧情")) {
            return "您可以看看《满江红》，张艺谋导演的力作，剧情反转不断。";
        }

        if (question.contains("价格") || question.contains("多少钱")) {
            return "我们的基础票价在 40-60 元之间，VIP 座位会稍贵一些，具体请点击“选座购票”查看。";
        }

        return "作为一个智能助手，我主要负责电影推荐。您可以问我“最近有什么好看的电影？”或者“推荐一部科幻片”。";
    }
}