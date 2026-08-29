package dk.gatchaz.server.notification.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 일기 작성 알림 문구를 만드는 데 필요한 값.
 */
@Getter
@Setter
@NoArgsConstructor
public class DiaryNotificationContext {

    /** 여행 이름. 알림 제목으로 쓴다. */
    private String tripTitle;

    /** 일기 작성자 닉네임. 설정하지 않았으면 null. */
    private String writerNickname;
}
