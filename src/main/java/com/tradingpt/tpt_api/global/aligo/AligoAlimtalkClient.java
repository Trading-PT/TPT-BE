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

    @Value("${aligo.button.join.name}")
    private String joinButtonName;

    @Value("${aligo.button.join.link}")
    private String joinButtonLink;

    private final RestTemplate restTemplate = new RestTemplate();


    /**
     * 📌 회원가입 메시지 (UD_9185)
     * 고객님이 제공해주신 최종 템플릿 텍스트의 줄 바꿈/공백 구조를 그대로 반영했습니다.
     * @return 알리고 API 응답 전문 (String)
     */
    public String sendJoinTalk(String phone, String shopName, String name) {

        MultiValueMap<String, String> params = baseParams(joinTplCode, phone);

        // 템플릿 제목을 등록된 내용과 정확히 일치시켜야 합니다.
        params.add("subject_1", "회원가입");

        // 💡 최종 템플릿 구조 반영 (변수 순서: SHOPNAME, NAME, SHOPNAME)
        String message = """
%s입니다.
안녕하세요, %s님!
%s에 회원가입해주셔서 진심으로 감사드립니다. 

TPT의 핵심 서비스와 매매일지 작성법을 한눈에 안내해드리는
오리엔테이션 영상이 준비되어 있습니다.

▶ OT 영상 보기:  OT 매매일지 작성방법 

해당 영상에서는

TPT 시스템 트레이딩의 핵심 구조

매매일지 작성 및 피드백 활용법

성장형 트레이더를 위한 첫 단계 로드맵
을 확인하실 수 있습니다.

성공적인 시작을 위해 OT 영상을 꼭 시청해주세요.
        """.formatted(shopName, name, shopName);

        params.add("message_1", message);

        // 버튼 정보 (JSON 형식)
        String buttonJson =
                """
                [
                  {
                    "name": "%s",
                    "linkType": "WL",
                    "linkTypeName": "웹링크",
                    "linkM": "%s",
                    "linkP": "%s"
                  }
                ]
                """.formatted(joinButtonName, joinButtonLink, joinButtonLink);

        params.add("button_1", buttonJson);

        return send(params, joinTplCode, phone);
    }


    /**
     * 📌 회원가입 승인 메시지 (UD_9519)
     * 고객님이 제공해주신 최종 템플릿 텍스트의 줄 바꿈/공백 구조를 그대로 반영했습니다.
     * @return 알리고 API 응답 전문 (String)
     */
    public String sendApprovalTalk(String phone, String shopName) {

        MultiValueMap<String, String> params = baseParams(joinApprovedTplCode, phone);

        // 템플릿 제목을 등록된 내용과 정확히 일치시켜야 합니다.
        params.add("subject_1", "회원가입 승인");

        // 💡 최종 템플릿 구조 반영 (변수 순서: SHOPNAME, SHOPNAME, SHOPNAME)
        String message = """
안녕하세요, %s입니다.

정식 회원으로 승인 되신 것을 진심으로 환영합니다! 

지금부터는 토큰 제도 가이드라인에서 사용 방법을 확인하시고,

강의 수강과 매매일지 피드백 서비스를 자유롭게 활용해보세요.

또한 Pro Type 구독을 통해 시스템 트레이딩 강의를 체계적으로 경험하고,

Pro 전용 매매일지로 자신만의 수익 구조를 설계해보시기 바랍니다.

올바른 트레이딩,

%s가 끝까지 함께하겠습니다.

추가로,

카카오채널에서 “%s 트레이딩룸 입장” 메시지를 보내면

실시간 시장 분석 콘텐츠를 확인하며 매일 꾸준히 트레이딩 학습을 이어갈 수 있습니다.
        """.formatted(shopName, shopName, shopName);

        params.add("message_1", message);

        // 버튼 없음

        return send(params, joinApprovedTplCode, phone);
    }


    /** 공통 기본 파라미터 */
    private MultiValueMap<String, String> baseParams(String tplCode, String phone) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        params.add("apikey", apiKey);
        params.add("userid", userId);
        params.add("senderkey", senderKey);
        params.add("tpl_code", tplCode);
        params.add("sender", sender);

        params.add("receiver_1", phone);
        // recvname_1은 필수 파라미터가 아니므로, 실패 시 주석 처리하고 테스트해볼 수 있습니다.


        return params;
    }


    /** POST 요청 및 결과 반환 */
    private String send(MultiValueMap<String, String> params, String tplCode, String phone) {

        String url = "https://kakaoapi.aligo.in/akv10/alimtalk/send/";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        try {
            String result = restTemplate.postForObject(
                    url,
                    new HttpEntity<>(params, headers),
                    String.class
            );

            log.info("알림톡 발송 요청 성공 → {} (템플릿: {}) 응답={}", phone, tplCode, result);
            return result;

        } catch (Exception e) {
            log.error("알림톡 발송 실패 → {} 이유={}", phone, e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }
}