package com.tradingpt.tpt_api.domain.auth.dto.response;

import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;

import com.tradingpt.tpt_api.domain.user.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MeResponse {
	String name; //유저 이름
	CourseStatus courseStatus;
	boolean isPremium;
	private String username; // 아이디
	private String investmentType;
	private Boolean isCourseCompleted;
	private UserStatus userStatus;
}
