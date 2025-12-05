package com.tradingpt.tpt_api.global.aligo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AligoAlimtalkClient {

    @Value("${aligo.user-id}")
    private String userId;

    @Value("${aligo.api-key}")
    private String apiKey;

    @Value("${aligo.sender}")
    private String sender;

    @Value("${aligo.senderkey}")
    private String senderKey;

    @Value("${aligo.template.join}")
    private String joinTplCode;

    @Value("${aligo.template.join-approved}")
    private String joinApprovedTplCode;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 1) 일반 회원가입 시 발송되는 알림톡
     */
    public void sendJoinTalk(String phone, String shopName) {
        sendTalkInternal(phone, shopName, joinTplCode);
    }

    /**
     * 2) 정식 회원 승인 시 발송되는 알림톡
     */
    public void sendApprovalTalk(String phone, String shopName) {
        sendTalkInternal(phone, shopName, joinApprovedTplCode);
    }

    /**
     * 공통 전송 로직
     */
    private void sendTalkInternal(String phone, String shopName, String tplCode) {

        String url = "https://kakaoapi.aligo.in/akv10/alimtalk/send/";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("apikey", apiKey);
        params.add("userid", userId);
        params.add("senderkey", senderKey);
        params.add("tpl_code", tplCode);
        params.add("sender", sender);

        params.add("receiver_1", phone);
        params.add("recvname_1", shopName);

        // 템플릿 변수 매핑 — 템플릿 내 모든 #{V} 에 대해 넣어야 함
        params.add("message_1", shopName); // 템플릿이 #{SHOPNAME}만 필요한 경우

        // 버튼 JSON
        String buttonJson = """
    [
      {
        "name": "채널추가",
        "linkType": "BOT",
        "linkTypeName": "채널추가"
      },
      {
        "name": "OT 매매일지 작성방법",
        "linkType": "WL",
        "linkTypeName": "웹링크",
        "linkM": "https://www.tradingpt.kr/menu/class-list",
        "linkP": "https://www.tradingpt.kr/menu/class-list"
      }
    ]
    """;

        params.add("button_1", buttonJson);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        try {
            String result = restTemplate.postForObject(url, new HttpEntity<>(params, headers), String.class);
            log.info("알림톡 발송 성공 → {} (템플릿: {}) 응답={}", phone, tplCode, result);
        } catch (Exception e) {
            log.error("알림톡 발송 실패 → {}", e.getMessage());
        }
    }
}
