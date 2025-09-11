package com.tradingpt.tpt_api.domain.auth.support;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SensSmsClient {

    private final DefaultMessageService messageService;
    private final String senderNumber;

    public SensSmsClient(
            @Value("${solapi.api-key}") String apiKey,
            @Value("${solapi.api-secret}") String apiSecret,
            @Value("${solapi.sender-number}") String senderNumber
    ) {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.solapi.com");
        this.senderNumber = normalizeKr(senderNumber);
    }

    /** Solapi로 단문(SMS) 발송. 90byte 초과 시 LMS로 자동 전환 */
    public void send(String to, String text) {
        String recipient = normalizeKr(to);

        Message m = new Message();
        m.setFrom(senderNumber);
        m.setTo(recipient);
        m.setText(text);

        // 실패 시 SDK가 런타임 예외를 던짐 → 상위에서 잡아 응답 변환
        messageService.sendOne(new SingleMessageSendingRequest(m));
    }

    /** 한국 번호 정규화: 하이픈 제거, +82/82 → 0 치환 */
    private static String normalizeKr(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("[^0-9+]", "");
        if (digits.startsWith("+82")) {
            return "0" + digits.substring(3);
        } else if (digits.startsWith("82")) {
            return "0" + digits.substring(2);
        }
        return digits;
    }
}
