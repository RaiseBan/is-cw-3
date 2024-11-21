package com.example.prac.service.yandex;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import org.jsoup.Jsoup;

import org.jsoup.Connection;

@Service
public class YandexTranslateService {

    private static final String TRANSLATION_URL = "https://translate.yandex.net/api/v1/tr.json/translate";

    public String translate(String text) {
        try {
            // Формируем параметры строки запроса
            String queryParams = "id=a5dd6fe8.6737d421.286bcba8.74722d74657874-2-0&srv=tr-text&source_lang=en&target_lang=ru" +
                    "&reason=auto&format=text&strategy=0&disable_cache=false&ajax=1&yu=4253767081703529710&yum=1701466648369260812";

            Connection.Response response = Jsoup.connect(TRANSLATION_URL + "?" + queryParams)
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .header("accept", "*/*")
                    .header("accept-encoding", "gzip, deflate, br, zstd")
                    .header("accept-language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("content-type", "application/x-www-form-urlencoded")
                    .header("origin", "https://translate.yandex.ru")
                    .header("priority", "u=1, i")
                    .header("referer", "https://translate.yandex.ru/?source_lang=ru&target_lang=en")
                    .header("sec-ch-ua", "\"Chromium\";v=\"130\", \"Google Chrome\";v=\"130\", \"Not?A_Brand\";v=\"99\"")
                    .header("sec-ch-ua-mobile", "?0")
                    .header("sec-ch-ua-platform", "\"Windows\"")
                    .header("sec-fetch-dest", "empty")
                    .header("sec-fetch-mode", "cors")
                    .header("sec-fetch-site", "cross-site")
                    .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36")
                    .header("x-retpath-y", "https://translate.yandex.ru")
                    .data("text", text)
                    .data("options", "4")
                    .execute();

            if (response.statusCode() == 200) {
                // Парсим ответ как JSON
                return new JSONObject(response.body()).getJSONArray("text").getString(0);
            } else {
                System.err.printf("Ошибка при выполнении запроса на перевод: HTTP error fetching URL. Status=%d, URL=[%s]%n",
                        response.statusCode(), TRANSLATION_URL);
            }
        } catch (Exception e) {
            System.err.printf("Ошибка при выполнении запроса на перевод: %s%n", e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        YandexTranslateService service = new YandexTranslateService();
        System.out.println("translate " + service.translate("Bacon"));

        //        JSONObject jsonResponse = service.translate("Bacon");
//        if (jsonResponse != null) {
//            // Извлекаем конкретное поле "text"
//            String translatedText = jsonResponse.getJSONArray("text").getString(0);
//            System.out.println("Перевод: " + translatedText);
//        } else {
//            System.err.println("Не удалось получить перевод.");
//        }
    }
}
