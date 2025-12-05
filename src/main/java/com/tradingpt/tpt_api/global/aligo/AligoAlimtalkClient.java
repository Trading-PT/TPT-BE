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

        /**
         *  템플릿 변수 #{SHOPNAME} 단 하나이므로
         * message에는 그 변수값만 넣는다.
         */
        params.add("message", shopName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        try {
            restTemplate.postForObject(url, new HttpEntity<>(params, headers), String.class);
            log.info("알림톡 발송 성공 → {} (템플릿: {})", phone, tplCode);
        } catch (Exception e) {
            log.error("알림톡 발송 실패 → {}", e.getMessage());
        }
    }
}
