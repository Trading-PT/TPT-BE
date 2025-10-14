package com.tradingpt.tpt_api.domain.user.entity;

import com.tradingpt.tpt_api.domain.user.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "admin")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@DiscriminatorValue(value = "ROLE_ADMIN")
@PrimaryKeyJoinColumn(name = "user_id")
public class Admin extends User {

    @Column(name = "oneline_introduction")
    private String onelineIntroduction;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Override
    public Role getRole() {
        return Role.ROLE_ADMIN;
    }

    public void changePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void changeOnelineIntroduction(String intro) {
        this.onelineIntroduction = intro;
    }
}
