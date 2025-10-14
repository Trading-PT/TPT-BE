package com.tradingpt.tpt_api.domain.user.repository;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

public interface AdminRepositoryCustom {

    List<AdminRow> findAdminListRows();

    @Getter
    @AllArgsConstructor
    class AdminRow {
        private Long id;
        private String name;
        private String username;
        private String phone;
        private String onelineIntroduction;
        private String profileImageUrl;
    }
}
