package com.tradingpt.tpt_api.domain.consultation.service.command;

import com.tradingpt.tpt_api.domain.consultation.dto.request.ConsultationCreateRequestDTO;
import com.tradingpt.tpt_api.domain.consultation.dto.request.ConsultationUpdateRequestDTO;
import com.tradingpt.tpt_api.domain.consultation.entity.Consultation;
import com.tradingpt.tpt_api.domain.consultation.exception.ConsultationErrorStatus;
import com.tradingpt.tpt_api.domain.consultation.exception.ConsultationException;
import com.tradingpt.tpt_api.domain.consultation.repository.ConsultationBlockRepository;
import com.tradingpt.tpt_api.domain.consultation.repository.ConsultationRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ConsultationCommandServiceImpl implements ConsultationCommandService {

    private static final int MAX_CAPACITY = 2; // 한 시간대 최대 2명

    private final ConsultationRepository consultationRepository;
    private final ConsultationBlockRepository blockRepository;
    private final CustomerRepository customerRepository;

    @Override
    public Long createReservation(Long customerId, ConsultationCreateRequestDTO request) {
        // 1) 먼저 해당 슬롯의 예약행들을 비관적 락으로 잡아 동시성 창을 닫는다.
        List<Consultation> existing =
                consultationRepository.findAllForCapacityWithPessimisticLock(request.getDate(),request.getTime());

        // 2) 같은 트랜잭션 안에서 차단 여부를 확인 (관리자 차단이 먼저였으면 여기서 true)
        boolean isBlocked =
                blockRepository.existsByConsultationBlockDateAndConsultationBlockTime(request.getDate(),request.getTime());
        if (isBlocked) {
            throw new ConsultationException(ConsultationErrorStatus.BLOCKED);
        }
        // 3) 동일 고객 중복 예약방지
        if (consultationRepository.existsByCustomerIdAndConsultationDateAndConsultationTime(customerId,request.getDate(), request.getTime())) {
            throw new ConsultationException(ConsultationErrorStatus.DUPLICATE);
        }

        // 4) 정원 체크
        if (existing.size() >= MAX_CAPACITY) {
            throw new ConsultationException(ConsultationErrorStatus.FULL);
        }

        Customer customer = customerRepository.getReferenceById(customerId);

        // 5) 예약 저장
        Consultation consultation = Consultation.builder()
                .customer(customer)
                .consultationDate(request.getDate())
                .consultationTime(request.getTime())
                .build();

        return consultationRepository.save(consultation).getId();
    }

    @Override
    @Transactional
    public Long updateReservation(Long customerId, ConsultationUpdateRequestDTO request) {

        // 기존 예약 확인 + 소유권 검증
        Consultation old = consultationRepository.findByIdAndCustomerId(request.getConsultationId(), customerId)
                .orElseThrow(() -> new ConsultationException(ConsultationErrorStatus.NOT_FOUND));

        //  새 슬롯 락 확보
        List<Consultation> existing =
                consultationRepository.findAllForCapacityWithPessimisticLock(request.getNewDate(), request.getNewTime());

        //  차단 확인
        if (blockRepository.existsByConsultationBlockDateAndConsultationBlockTime(request.getNewDate(),request.getNewTime())) {
            throw new ConsultationException(ConsultationErrorStatus.BLOCKED);
        }

        //  중복 예약 확인 (같은 슬롯이면 통과)
        boolean sameSlot = old.getConsultationDate().equals(request.getNewDate())
                && old.getConsultationTime().equals(request.getNewTime());
        if (!sameSlot && consultationRepository.existsByCustomerIdAndConsultationDateAndConsultationTime(customerId, request.getNewDate(), request.getNewTime())) {
            throw new ConsultationException(ConsultationErrorStatus.DUPLICATE);
        }

        // 정원 체크 (sameSlot이면 본인 포함)
        if (!sameSlot && existing.size() >= MAX_CAPACITY) {
            throw new ConsultationException(ConsultationErrorStatus.FULL);
        }

        // 새 상담 생성 후 기존 예약 삭제 (트랜잭션으로 원자성 보장)
        Customer customer = old.getCustomer();

        Consultation newConsultation = Consultation.builder()
                .customer(customer)
                .consultationDate(request.getNewDate())
                .consultationTime(request.getNewTime())
                .build();

        consultationRepository.save(newConsultation);
        consultationRepository.deleteById(old.getId());

        return newConsultation.getId();
    }


    @Override
    public void deleteReservation(Long customerId, Long consultationId) {
        // 본인 소유인지 확인
        Consultation target = consultationRepository.findByIdAndCustomerId(consultationId, customerId)
                .orElseThrow(() -> new ConsultationException(ConsultationErrorStatus.NOT_FOUND));

        consultationRepository.deleteById(target.getId());
    }
}
