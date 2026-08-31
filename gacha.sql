CREATE TABLE `trip` (
	`trip_id`	BIGINT	NOT NULL,
	`owner_member_id`	BIGINT	NOT NULL	COMMENT '팀장 id',
	`trip_region_id`	BIGINT	NULL	COMMENT '선택한 여행지 (지역 선택 전에는 NULL)',
	`title`	VARCHAR(100)	NULL,
	`start_date`	DATETIME	NULL,
	`end_date`	DATETIME	NULL,
	`member_limit`	INT	NULL	COMMENT '여행 최대 인원',
	`mission_min`	INT	NULL	COMMENT '하루 최소 미션 수',
	`mission_max`	INT	NULL	COMMENT '하루 최대 미션 수',
	`mission_start_at`	DATETIME	NULL	COMMENT '첫 미션 받을 일시',
	`status`	VARCHAR(30)	NULL	COMMENT '예정/진행/완료',
	`invite_code`	VARCHAR(100)	NULL	COMMENT '초대 코드 (영구 발급)',
	`completed_at`	DATETIME	NULL,
	`cancelled_at`	DATETIME	NULL,
	`created_at`	DATETIME	NULL,
	`updated_at`	DATETIME	NULL
);

CREATE TABLE `policy` (
	`policy_id`	BIGINT	NOT NULL,
	`policy_type`	VARCHAR(50)	NULL,
	`title`	VARCHAR(100)	NULL,
	`content`	BLOB	NULL,
	`version`	VARCHAR(30)	NULL,
	`use_yn`	CHAR(1)	NULL	DEFAULT 'Y',
	`created_at`	DATETIME	NULL
);

CREATE TABLE `location_verification_log` (
	`location_verification_id`	BIGINT	NOT NULL,
	`trip_id`	BIGINT	NOT NULL,
	`trip_mission_id`	BIGINT	NULL,
	`member_id`	BIGINT	NOT NULL,
	`latitude`	DECIMAL(10,7)	NULL,
	`longitude`	DECIMAL(10,7)	NULL,
	`distance_meter`	DECIMAL(10,2)	NULL,
	`success_yn`	CHAR(1)	NULL	DEFAULT 'N',
	`created_at`	DATETIME	NULL
);

CREATE TABLE `trip_region` (
	`trip_region_id`	BIGINT	NOT NULL,
	`trip_region_code`	VARCHAR(50)	NULL,
	`trip_region_name`	VARCHAR(100)	NULL,
	`latitude`	DECIMAL(10,7)	NULL,
	`longitude`	DECIMAL(10,7)	NULL,
	`use_yn`	CHAR(1)	NULL	DEFAULT 'Y',
	`created_at`	DATETIME	NULL
);

CREATE TABLE `trip_invite` (
	`trip_invite_id`	BIGINT	NOT NULL,
	`trip_id`	BIGINT	NOT NULL,
	`invite_code`	VARCHAR(100)	NULL,
	`status`	VARCHAR(30)	NULL,
	`expires_at`	DATETIME	NULL,
	`created_by`	BIGINT	NULL,
	`created_at`	DATETIME	NULL
);

CREATE TABLE `member_rel_collection_item` (
	`rel_id`	BIGINT	NOT NULL,
	`member_id`	BIGINT	NOT NULL,
	`collection_item_id`	BIGINT	NOT NULL,
	`trip_id`	BIGINT	NULL,
	`created_at`	DATETIME	NULL
);

CREATE TABLE `setlog` (
	`setlog_id`	BIGINT	NOT NULL,
	`trip_id`	BIGINT	NOT NULL,
	`trip_mission_id`	BIGINT	NOT NULL,
	`member_id`	BIGINT	NOT NULL,
	`file_url`	VARCHAR(500)	NULL,
	`status`	VARCHAR(30)	NULL,
	`created_at`	DATETIME	NULL,
	`deleted_at`	DATETIME	NULL,
	`slot_no`	INT	NULL
);

CREATE TABLE `refresh_token` (
	`refresh_token_id`	BIGINT	NOT NULL,
	`member_id`	BIGINT	NOT NULL,
	`token`	VARCHAR(500)	NULL,
	`expires_at`	DATETIME	NULL,
	`revoked_yn`	CHAR(1)	NULL	DEFAULT 'N',
	`created_at`	DATETIME	NULL
);

CREATE TABLE `diary_ai_generation` (
	`diary_ai_generation_id`	BIGINT	NOT NULL,
	`diary_id`	BIGINT	NOT NULL,
	`source_content`	TEXT	NULL,
	`generated_content`	TEXT	NULL,
	`selected_yn`	CHAR(1)	NULL	DEFAULT 'N',
	`created_at`	DATETIME	NULL
);

CREATE TABLE `mission_reroll_log` (
	`mission_reroll_log_id`	BIGINT	NOT NULL,
	`trip_id`	BIGINT	NOT NULL,
	`day_no`	INT	NOT NULL	COMMENT '몇 일차에 발생한 리롤인지',
	`assigned_order`	INT	NOT NULL	COMMENT '리롤이 발생한 미션 라운드 (해당 일자 내 순번, mission_candidate.assigned_order 와 동일 기준)',
	`member_id`	BIGINT	NOT NULL,
	`old_mission_id`	BIGINT	NULL,
	`new_mission_id`	BIGINT	NOT NULL,
	`created_at`	DATETIME	NULL
);

CREATE TABLE `mission_daily_goal` (
	`mission_daily_goal_id`	BIGINT	NOT NULL,
	`trip_id`	BIGINT	NOT NULL,
	`day_no`	INT	NOT NULL	COMMENT '몇 일차인지',
	`target_round_count`	INT	NOT NULL	COMMENT '그날의 목표 미션 라운드 수 (trip.mission_min~mission_max 사이 랜덤으로 정해서 하루 동안 고정)',
	`created_at`	DATETIME	NULL
);

CREATE TABLE `common_code` (
	`common_code_id`	BIGINT	NOT NULL,
	`group_code`	VARCHAR(50)	NULL,
	`code`	VARCHAR(50)	NULL,
	`code_name`	VARCHAR(100)	NULL,
	`sort_order`	INT	NULL,
	`use_yn`	CHAR(1)	NULL	DEFAULT 'Y',
	`created_at`	DATETIME	NULL
);

CREATE TABLE `mission` (
	`mission_id`	BIGINT	NOT NULL,
	`region_id`	BIGINT	NULL,
	`mission_type`	VARCHAR(30)	NULL,
	`title`	VARCHAR(100)	NULL,
	`description`	TEXT	NULL,
	`difficulty`	INT	NULL,
	`use_yn`	CHAR(1)	NULL	DEFAULT 'Y',
	`created_at`	DATETIME	NULL
);

CREATE TABLE `collection_item` (
	`collection_item_id`	BIGINT	NOT NULL,
	`region_id`	BIGINT	NOT NULL,
	`item_name`	VARCHAR(100)	NULL,
	`item_type`	VARCHAR(30)	NULL,
	`image_url`	VARCHAR(500)	NULL,
	`description`	TEXT	NULL,
	`use_yn`	CHAR(1)	NULL	DEFAULT 'Y',
	`created_at`	DATETIME	NULL
);

CREATE TABLE `device` (
	`device_id`	BIGINT	NOT NULL,
	`member_id`	BIGINT	NOT NULL,
	`fcm_token`	VARCHAR(500)	NULL,
	`os_type`	VARCHAR(30)	NULL,
	`app_version`	VARCHAR(50)	NULL,
	`notification_enabled`	CHAR(1)	NULL,
	`last_active_at`	DATETIME	NULL,
	`created_at`	DATETIME	NULL,
	`updated_at`	DATETIME	NULL
);

CREATE TABLE `trip_event_log` (
	`trip_event_log_id`	BIGINT	NOT NULL	COMMENT '여행 이벤트 로그 ID',
	`trip_id`	BIGINT	NOT NULL,
	`member_id`	BIGINT	NOT NULL,
	`trip_region_id`	BIGINT	NOT NULL,
	`mission_id`	BIGINT	NOT NULL,
	`trip_mission_id`	BIGINT	NOT NULL,
	`event_type`	VARCHAR(50)	NULL	COMMENT '이벤트 유형',
	`event_name`	VARCHAR(100)	NULL	COMMENT '이벤트명',
	`before_status`	VARCHAR(30)	NULL	COMMENT '변경 전 상태',
	`after_status`	VARCHAR(30)	NULL	COMMENT '변경 후 상태',
	`event_payload`	TEXT	NULL	COMMENT '이벤트 상세 데이터 JSON 문자열',
	`created_at`	DATETIME	NULL	COMMENT '생성일시'
);

CREATE TABLE `member` (
	`member_id`	BIGINT	NOT NULL,
	`nickname`	VARCHAR(50)	NULL,
	`age`	INT	NULL,
	`profile_image_url`	VARCHAR(500)	NULL,
	`deleted_yn`	TINYINT	NULL,
	`deleted_at`	DATETIME	NULL,
	`created_at`	DATETIME	NULL,
	`updated_at`	DATETIME	NULL
);

CREATE TABLE `share` (
	`share_id`	BIGINT	NOT NULL,
	`member_id`	BIGINT	NOT NULL,
	`target_type`	VARCHAR(50)	NULL,
	`target_id`	BIGINT	NULL,
	`channel`	VARCHAR(50)	NULL,
	`share_status`	VARCHAR(30)	NULL,
	`created_at`	DATETIME	NULL
);

CREATE TABLE `trip_candidate` (
	`trip_candidate_id`	BIGINT	NOT NULL,
	`trip_id`	BIGINT	NULL,
	`trip_region_id`	BIGINT	NOT NULL,
	`reroll_count`	INT	NULL,
	`selected_yn`	CHAR(1)	NULL	DEFAULT 'N',
	`use_yn`	CHAR(1)	NULL	DEFAULT 'Y'	COMMENT '리롤 시 기존 후보 비활성화 (Y→N), 이력 보관',
	`created_at`	DATETIME	NULL
);

CREATE TABLE `mission_candidate` (
	`mission_candidate_id`	BIGINT	NOT NULL,
	`trip_id`	BIGINT	NOT NULL,
	`day_no`	INT	NOT NULL	COMMENT '몇 일차 후보인지',
	`assigned_order`	INT	NOT NULL	COMMENT '해당 일자 내 몇 번째 미션 라운드인지 (trip_mission.assigned_order 와 동일 기준)',
	`mission_id`	BIGINT	NOT NULL,
	`selected_yn`	CHAR(1)	NOT NULL	DEFAULT 'N',
	`rerolled_yn`	BOOLEAN	NOT NULL	DEFAULT FALSE	COMMENT 'true = 리롤되어 교체된(비활성) 후보. 리롤 시 이 행은 그대로 두고(이력 보존) 새 행을 추가한다',
	`reroll_count`	INT	NULL	COMMENT '남은 리롤 가능 횟수. 최초 생성 후보는 1, 리롤로 새로 생긴 후보는 0(더 이상 리롤 불가)',
	`created_at`	DATETIME	NULL
);

CREATE TABLE `member_rel_trip` (
	`member_rel_id`	BIGINT	NOT NULL,
	`member_id`	BIGINT	NOT NULL,
	`trip_id`	BIGINT	NOT NULL,
	`role`	VARCHAR(30)	NULL,
	`status`	VARCHAR(30)	NULL,
	`joined_at`	DATETIME	NULL,
	`left_at`	DATETIME	NULL,
	`created_at`	DATETIME	NULL,
	`updated_at`	DATETIME	NULL
);

CREATE TABLE `notification_send_log` (
	`notification_send_log_id`	BIGINT	NOT NULL,
	`notification_id`	BIGINT	NOT NULL,
	`device_id`	BIGINT	NOT NULL,
	`send_status`	VARCHAR(30)	NULL,
	`error_message`	TEXT	NULL,
	`created_at`	DATETIME	NULL
);

CREATE TABLE `social_account` (
	`social_account_id`	BIGINT	NOT NULL,
	`member_id`	BIGINT	NOT NULL,
	`provider`	VARCHAR(30)	NULL,
	`provider_user_id`	VARCHAR(255)	NULL,
	`email`	VARCHAR(255)	NULL,
	`created_at`	DATETIME	NULL
);

CREATE TABLE `setlog_download_log` (
	`vlog_id`	BIGINT	NOT NULL,
	`setlog_id`	BIGINT	NOT NULL,
	`member_id`	BIGINT	NOT NULL,
	`status`	VARCHAR(30)	NULL,
	`created_at`	DATETIME	NULL,
	`updated_at`	DATETIME	NULL
);

CREATE TABLE `trip_mission` (
	`trip_mission_id`	BIGINT	NOT NULL,
	`trip_id`	BIGINT	NOT NULL,
	`mission_id`	BIGINT	NOT NULL,
	`day_no`	INT	NOT NULL	COMMENT '몇 일차 미션인지',
	`assigned_order`	INT	NULL	COMMENT '해당 일자 내 몇 번째 라운드에서 선택된 미션인지',
	`status`	VARCHAR(30)	NULL,
	`started_at`	DATETIME	NULL,
	`completed_at`	DATETIME	NULL,
	`failed_at`	DATETIME	NULL,
	`created_at`	DATETIME	NULL,
	`updated_at`	DATETIME	NULL
);

CREATE TABLE `diary` (
	`diary_id`	BIGINT	NOT NULL,
	`trip_id`	BIGINT	NOT NULL,
	`member_id`	BIGINT	NOT NULL,
	`content`	TEXT	NULL,
	`visibility`	VARCHAR(30)	NULL	DEFAULT 'TEAM',
	`status`	VARCHAR(30)	NULL,
	`is_ai_generated`	CHAR(1)	NOT NULL	DEFAULT 'N',
	`created_at`	DATETIME	NULL,
	`updated_at`	DATETIME	NULL
);

CREATE TABLE `notification` (
	`notification_id`	BIGINT	NOT NULL,
	`member_id`	BIGINT	NOT NULL,
	`type`	VARCHAR(50)	NULL,
	`title`	VARCHAR(100)	NULL,
	`body`	VARCHAR(500)	NULL,
	`target_type`	VARCHAR(50)	NULL,
	`target_id`	BIGINT	NULL,
	`read_yn`	CHAR(1)	NULL	DEFAULT 'N',
	`created_at`	DATETIME	NOT NULL
);

CREATE TABLE `device_permission` (
	`device_permission_id`	BIGINT	NOT NULL,
	`device_id`	BIGINT	NOT NULL,
	`location_status`	VARCHAR(30)	NULL,
	`camera_status`	VARCHAR(30)	NULL,
	`notification_status`	VARCHAR(30)	NULL,
	`updated_at`	DATETIME	NULL
);

ALTER TABLE `trip` ADD CONSTRAINT `PK_TRIP` PRIMARY KEY (
	`trip_id`
);

ALTER TABLE `policy` ADD CONSTRAINT `PK_POLICY` PRIMARY KEY (
	`policy_id`
);

ALTER TABLE `location_verification_log` ADD CONSTRAINT `PK_LOCATION_VERIFICATION_LOG` PRIMARY KEY (
	`location_verification_id`
);

ALTER TABLE `trip_region` ADD CONSTRAINT `PK_TRIP_REGION` PRIMARY KEY (
	`trip_region_id`
);

ALTER TABLE `trip_invite` ADD CONSTRAINT `PK_TRIP_INVITE` PRIMARY KEY (
	`trip_invite_id`
);

ALTER TABLE `member_rel_collection_item` ADD CONSTRAINT `PK_MEMBER_REL_COLLECTION_ITEM` PRIMARY KEY (
	`rel_id`
);

ALTER TABLE `setlog` ADD CONSTRAINT `PK_SETLOG` PRIMARY KEY (
	`setlog_id`
);

ALTER TABLE `refresh_token` ADD CONSTRAINT `PK_REFRESH_TOKEN` PRIMARY KEY (
	`refresh_token_id`
);

ALTER TABLE `diary_ai_generation` ADD CONSTRAINT `PK_DIARY_AI_GENERATION` PRIMARY KEY (
	`diary_ai_generation_id`
);

ALTER TABLE `mission_reroll_log` ADD CONSTRAINT `PK_MISSION_REROLL_LOG` PRIMARY KEY (
	`mission_reroll_log_id`
);

ALTER TABLE `mission_daily_goal` ADD CONSTRAINT `PK_MISSION_DAILY_GOAL` PRIMARY KEY (
	`mission_daily_goal_id`
);

ALTER TABLE `common_code` ADD CONSTRAINT `PK_COMMON_CODE` PRIMARY KEY (
	`common_code_id`
);

ALTER TABLE `mission` ADD CONSTRAINT `PK_MISSION` PRIMARY KEY (
	`mission_id`
);

ALTER TABLE `collection_item` ADD CONSTRAINT `PK_COLLECTION_ITEM` PRIMARY KEY (
	`collection_item_id`
);

ALTER TABLE `device` ADD CONSTRAINT `PK_DEVICE` PRIMARY KEY (
	`device_id`
);

ALTER TABLE `trip_event_log` ADD CONSTRAINT `PK_TRIP_EVENT_LOG` PRIMARY KEY (
	`trip_event_log_id`
);

ALTER TABLE `member` ADD CONSTRAINT `PK_MEMBER` PRIMARY KEY (
	`member_id`
);

ALTER TABLE `share` ADD CONSTRAINT `PK_SHARE` PRIMARY KEY (
	`share_id`
);

ALTER TABLE `trip_candidate` ADD CONSTRAINT `PK_TRIP_CANDIDATE` PRIMARY KEY (
	`trip_candidate_id`
);

ALTER TABLE `mission_candidate` ADD CONSTRAINT `PK_MISSION_CANDIDATE` PRIMARY KEY (
	`mission_candidate_id`
);

ALTER TABLE `member_rel_trip` ADD CONSTRAINT `PK_MEMBER_REL_TRIP` PRIMARY KEY (
	`member_rel_id`
);

ALTER TABLE `notification_send_log` ADD CONSTRAINT `PK_NOTIFICATION_SEND_LOG` PRIMARY KEY (
	`notification_send_log_id`
);

ALTER TABLE `social_account` ADD CONSTRAINT `PK_SOCIAL_ACCOUNT` PRIMARY KEY (
	`social_account_id`
);

ALTER TABLE `setlog_download_log` ADD CONSTRAINT `PK_SETLOG_DOWNLOAD_LOG` PRIMARY KEY (
	`vlog_id`
);

ALTER TABLE `trip_mission` ADD CONSTRAINT `PK_TRIP_MISSION` PRIMARY KEY (
	`trip_mission_id`
);

ALTER TABLE `diary` ADD CONSTRAINT `PK_DIARY` PRIMARY KEY (
	`diary_id`
);

ALTER TABLE `notification` ADD CONSTRAINT `PK_NOTIFICATION` PRIMARY KEY (
	`notification_id`
);

ALTER TABLE `device_permission` ADD CONSTRAINT `PK_DEVICE_PERMISSION` PRIMARY KEY (
	`device_permission_id`
);

-- =====================================================================
-- Foreign Key 제약조건
-- (share.target_type/target_id, notification.target_type/target_id 는
--  다형성(polymorphic) 참조라 단일 FK로 표현할 수 없어 제외)
-- =====================================================================

ALTER TABLE `trip` ADD CONSTRAINT `FK_TRIP_OWNER_MEMBER` FOREIGN KEY (`owner_member_id`) REFERENCES `member` (`member_id`);
ALTER TABLE `trip` ADD CONSTRAINT `FK_TRIP_TRIP_REGION` FOREIGN KEY (`trip_region_id`) REFERENCES `trip_region` (`trip_region_id`);

ALTER TABLE `location_verification_log` ADD CONSTRAINT `FK_LOCATION_VERIFICATION_LOG_TRIP` FOREIGN KEY (`trip_id`) REFERENCES `trip` (`trip_id`);
ALTER TABLE `location_verification_log` ADD CONSTRAINT `FK_LOCATION_VERIFICATION_LOG_TRIP_MISSION` FOREIGN KEY (`trip_mission_id`) REFERENCES `trip_mission` (`trip_mission_id`);
ALTER TABLE `location_verification_log` ADD CONSTRAINT `FK_LOCATION_VERIFICATION_LOG_MEMBER` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`);

ALTER TABLE `trip_invite` ADD CONSTRAINT `FK_TRIP_INVITE_TRIP` FOREIGN KEY (`trip_id`) REFERENCES `trip` (`trip_id`);
ALTER TABLE `trip_invite` ADD CONSTRAINT `FK_TRIP_INVITE_CREATED_BY` FOREIGN KEY (`created_by`) REFERENCES `member` (`member_id`);

ALTER TABLE `member_rel_collection_item` ADD CONSTRAINT `FK_MEMBER_REL_COLLECTION_ITEM_MEMBER` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`);
ALTER TABLE `member_rel_collection_item` ADD CONSTRAINT `FK_MEMBER_REL_COLLECTION_ITEM_COLLECTION_ITEM` FOREIGN KEY (`collection_item_id`) REFERENCES `collection_item` (`collection_item_id`);
ALTER TABLE `member_rel_collection_item` ADD CONSTRAINT `FK_MEMBER_REL_COLLECTION_ITEM_TRIP` FOREIGN KEY (`trip_id`) REFERENCES `trip` (`trip_id`);

ALTER TABLE `setlog` ADD CONSTRAINT `FK_SETLOG_TRIP` FOREIGN KEY (`trip_id`) REFERENCES `trip` (`trip_id`);
ALTER TABLE `setlog` ADD CONSTRAINT `FK_SETLOG_TRIP_MISSION` FOREIGN KEY (`trip_mission_id`) REFERENCES `trip_mission` (`trip_mission_id`);
ALTER TABLE `setlog` ADD CONSTRAINT `FK_SETLOG_MEMBER` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`);

ALTER TABLE `refresh_token` ADD CONSTRAINT `FK_REFRESH_TOKEN_MEMBER` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`);

ALTER TABLE `diary_ai_generation` ADD CONSTRAINT `FK_DIARY_AI_GENERATION_DIARY` FOREIGN KEY (`diary_id`) REFERENCES `diary` (`diary_id`);

ALTER TABLE `mission_reroll_log` ADD CONSTRAINT `FK_MISSION_REROLL_LOG_TRIP` FOREIGN KEY (`trip_id`) REFERENCES `trip` (`trip_id`);
ALTER TABLE `mission_reroll_log` ADD CONSTRAINT `FK_MISSION_REROLL_LOG_MEMBER` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`);
ALTER TABLE `mission_reroll_log` ADD CONSTRAINT `FK_MISSION_REROLL_LOG_OLD_MISSION` FOREIGN KEY (`old_mission_id`) REFERENCES `mission` (`mission_id`);
ALTER TABLE `mission_reroll_log` ADD CONSTRAINT `FK_MISSION_REROLL_LOG_NEW_MISSION` FOREIGN KEY (`new_mission_id`) REFERENCES `mission` (`mission_id`);

ALTER TABLE `mission_daily_goal` ADD CONSTRAINT `FK_MISSION_DAILY_GOAL_TRIP` FOREIGN KEY (`trip_id`) REFERENCES `trip` (`trip_id`);

ALTER TABLE `mission` ADD CONSTRAINT `FK_MISSION_TRIP_REGION` FOREIGN KEY (`region_id`) REFERENCES `trip_region` (`trip_region_id`);

ALTER TABLE `collection_item` ADD CONSTRAINT `FK_COLLECTION_ITEM_TRIP_REGION` FOREIGN KEY (`region_id`) REFERENCES `trip_region` (`trip_region_id`);

ALTER TABLE `device` ADD CONSTRAINT `FK_DEVICE_MEMBER` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`);

ALTER TABLE `trip_event_log` ADD CONSTRAINT `FK_TRIP_EVENT_LOG_TRIP` FOREIGN KEY (`trip_id`) REFERENCES `trip` (`trip_id`);
ALTER TABLE `trip_event_log` ADD CONSTRAINT `FK_TRIP_EVENT_LOG_MEMBER` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`);
ALTER TABLE `trip_event_log` ADD CONSTRAINT `FK_TRIP_EVENT_LOG_TRIP_REGION` FOREIGN KEY (`trip_region_id`) REFERENCES `trip_region` (`trip_region_id`);
ALTER TABLE `trip_event_log` ADD CONSTRAINT `FK_TRIP_EVENT_LOG_MISSION` FOREIGN KEY (`mission_id`) REFERENCES `mission` (`mission_id`);
ALTER TABLE `trip_event_log` ADD CONSTRAINT `FK_TRIP_EVENT_LOG_TRIP_MISSION` FOREIGN KEY (`trip_mission_id`) REFERENCES `trip_mission` (`trip_mission_id`);

ALTER TABLE `share` ADD CONSTRAINT `FK_SHARE_MEMBER` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`);

ALTER TABLE `trip_candidate` ADD CONSTRAINT `FK_TRIP_CANDIDATE_TRIP` FOREIGN KEY (`trip_id`) REFERENCES `trip` (`trip_id`);
ALTER TABLE `trip_candidate` ADD CONSTRAINT `FK_TRIP_CANDIDATE_TRIP_REGION` FOREIGN KEY (`trip_region_id`) REFERENCES `trip_region` (`trip_region_id`);

ALTER TABLE `mission_candidate` ADD CONSTRAINT `FK_MISSION_CANDIDATE_TRIP` FOREIGN KEY (`trip_id`) REFERENCES `trip` (`trip_id`);
ALTER TABLE `mission_candidate` ADD CONSTRAINT `FK_MISSION_CANDIDATE_MISSION` FOREIGN KEY (`mission_id`) REFERENCES `mission` (`mission_id`);

ALTER TABLE `member_rel_trip` ADD CONSTRAINT `FK_MEMBER_REL_TRIP_MEMBER` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`);
ALTER TABLE `member_rel_trip` ADD CONSTRAINT `FK_MEMBER_REL_TRIP_TRIP` FOREIGN KEY (`trip_id`) REFERENCES `trip` (`trip_id`);

ALTER TABLE `notification_send_log` ADD CONSTRAINT `FK_NOTIFICATION_SEND_LOG_NOTIFICATION` FOREIGN KEY (`notification_id`) REFERENCES `notification` (`notification_id`);
ALTER TABLE `notification_send_log` ADD CONSTRAINT `FK_NOTIFICATION_SEND_LOG_DEVICE` FOREIGN KEY (`device_id`) REFERENCES `device` (`device_id`);

ALTER TABLE `social_account` ADD CONSTRAINT `FK_SOCIAL_ACCOUNT_MEMBER` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`);

ALTER TABLE `setlog_download_log` ADD CONSTRAINT `FK_SETLOG_DOWNLOAD_LOG_SETLOG` FOREIGN KEY (`setlog_id`) REFERENCES `setlog` (`setlog_id`);
ALTER TABLE `setlog_download_log` ADD CONSTRAINT `FK_SETLOG_DOWNLOAD_LOG_MEMBER` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`);

ALTER TABLE `trip_mission` ADD CONSTRAINT `FK_TRIP_MISSION_TRIP` FOREIGN KEY (`trip_id`) REFERENCES `trip` (`trip_id`);
ALTER TABLE `trip_mission` ADD CONSTRAINT `FK_TRIP_MISSION_MISSION` FOREIGN KEY (`mission_id`) REFERENCES `mission` (`mission_id`);

ALTER TABLE `diary` ADD CONSTRAINT `FK_DIARY_TRIP` FOREIGN KEY (`trip_id`) REFERENCES `trip` (`trip_id`);
ALTER TABLE `diary` ADD CONSTRAINT `FK_DIARY_MEMBER` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`);

ALTER TABLE `notification` ADD CONSTRAINT `FK_NOTIFICATION_MEMBER` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`);

ALTER TABLE `device_permission` ADD CONSTRAINT `FK_DEVICE_PERMISSION_DEVICE` FOREIGN KEY (`device_id`) REFERENCES `device` (`device_id`);
