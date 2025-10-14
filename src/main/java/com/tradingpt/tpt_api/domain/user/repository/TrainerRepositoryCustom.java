package com.tradingpt.tpt_api.domain.user.repository;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.transaction.reactive.TransactionalOperatorExtensionsKt;

public interface TrainerRepositoryCustom {

    List<TrainerRow> findTrainerListRows();

    @Getter
    @AllArgsConstructor
    class TrainerRow {
        private Long id;
        private String name;
        private String username;
        private String phone;
        private String onelineIntroduction;
        private String profileImageUrl;
    }
}
