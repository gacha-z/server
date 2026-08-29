package dk.gatchaz.server.notification.controller;

import dk.gatchaz.server.dto.ResponseDto;
import dk.gatchaz.server.notification.dto.DeviceRegisterRequest;
import dk.gatchaz.server.notification.dto.DeviceRegisterResponse;
import dk.gatchaz.server.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notification", description = "알림 API")
@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final NotificationService notificationService;

    /**
     * 기기 등록/갱신 (푸시 수신용)
     */
    @Operation(
            summary = "기기 등록/갱신",
            description = """
                    푸시를 받을 기기의 FCM 토큰을 등록하거나 갱신한다.

                    ### 언제 호출하나
                    1. 로그인 직후 FCM 토큰을 발급받았을 때
                    2. FCM 토큰이 갱신되었을 때 (`onTokenRefresh`)
                    3. 앱에서 알림 수신 설정을 켜거나 껐을 때 (`notificationEnabled` 를 바꿔 다시 호출)

                    ### 동작
                    - 같은 `fcmToken` 이 이미 등록되어 있으면 **새로 만들지 않고 갱신**한다.
                      기기를 재설치하거나 다른 계정으로 로그인해도 토큰이 같으면 같은 기기로 본다.
                    - `notificationEnabled` 를 생략하면 **수신 동의(true)** 로 저장된다.
                    - `notificationEnabled = false` 인 기기에는 푸시를 보내지 않는다. (알림함에는 그대로 쌓인다)

                    ### 참고
                    - 매번 같은 토큰으로 호출해도 기기가 늘어나지 않으므로 앱 실행 시마다 호출해도 된다.
                    - OS 알림 권한 자체가 꺼져 있으면 발송은 성공해도 사용자에게 보이지 않는다. 앱에서 권한을 먼저 확인한다.
                    """)
    @PostMapping
    public ResponseDto<DeviceRegisterResponse> registerDevice(
            @Valid @RequestBody final DeviceRegisterRequest request) {
        return ResponseDto.ok(notificationService.registerDevice(request));
    }
}
