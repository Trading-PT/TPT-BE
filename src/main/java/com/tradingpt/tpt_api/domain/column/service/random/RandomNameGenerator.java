package com.tradingpt.tpt_api.domain.column.service.random;

import java.util.List;
import java.util.Random;

public class RandomNameGenerator {

    private static final List<String> LAST_NAMES = List.of(
            "김", "이", "박", "최", "정", "윤", "장", "임", "한", "오",
            "서", "신", "권", "황", "안", "송", "홍", "유", "양", "조"
    );

    private static final List<String> FIRST_NAMES_1 = List.of(
            "민", "서", "도", "하", "지", "유", "아", "준", "현", "시",
            "예", "수", "윤", "채", "은", "태", "나", "지", "연", "한"
    );

    private static final List<String> FIRST_NAMES_2 = List.of(
            "우", "윤", "현", "호", "원", "준", "아", "린", "율", "영",
            "빈", "민", "진", "연", "성", "혁", "슬", "인", "주", "경"
    );

    private static final Random RANDOM = new Random();

    public static String generate() {
        String last = LAST_NAMES.get(RANDOM.nextInt(LAST_NAMES.size()));
        String first1 = FIRST_NAMES_1.get(RANDOM.nextInt(FIRST_NAMES_1.size()));
        String first2 = FIRST_NAMES_2.get(RANDOM.nextInt(FIRST_NAMES_2.size()));
        return last + first1 + first2;
    }
}
