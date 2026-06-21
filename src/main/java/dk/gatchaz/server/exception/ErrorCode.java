package dk.gatchaz.server.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Not Found Error

    // Bad Request Error
    NOT_END_POINT(40000, HttpStatus.BAD_REQUEST, "End Point가 존재하지 않습니다."),
    NOT_FOUND_RESOURCE(40000, HttpStatus.BAD_REQUEST, "해당 리소스가 존재하지 않습니다."),
    INVALID_ARGUMENT(40001, HttpStatus.BAD_REQUEST, "Invalid Argument"),
    INVALID_PROVIDER(40002, HttpStatus.BAD_REQUEST, "유효하지 않은 제공자입니다."),
    METHOD_NOT_ALLOWED(40003, HttpStatus.BAD_REQUEST, "지원하지 않는 HTTP Method 입니다."),
    UNSUPPORTED_MEDIA_TYPE(40004, HttpStatus.BAD_REQUEST, "지원하지 않는 미디어 타입입니다."),
    MISSING_REQUEST_PARAMETER(40005, HttpStatus.BAD_REQUEST, "필수 요청 파라미터가 누락되었습니다."),
    METHOD_ARGUMENT_TYPE_MISMATCH(40006, HttpStatus.BAD_REQUEST, "요청 파라미터의 형태가 잘못되었습니다."),

    UNKNOWN_NATIONAL_CODE_ERROR(40007, HttpStatus.BAD_REQUEST, "Unknown national code"),
    UNKNOWN_BLOOD_TYPE_ERROR(40008, HttpStatus.BAD_REQUEST, "Unknown blood type"),
    UNKNOWN_GENDER_ERROR(40008, HttpStatus.BAD_REQUEST, "Unknown gender"),

    EXPIRED_TOKEN_ERROR(40100, HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID_TOKEN_ERROR(40101, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_MALFORMED_ERROR(40102, HttpStatus.UNAUTHORIZED, "토큰이 올바르지 않습니다."),
    TOKEN_TYPE_ERROR(40103, HttpStatus.UNAUTHORIZED, "토큰 타입이 일치하지 않습니다."),
    TOKEN_UNSUPPORTED_ERROR(40104, HttpStatus.UNAUTHORIZED, "지원하지않는 토큰입니다."),
    TOKEN_GENERATION_ERROR(40105, HttpStatus.UNAUTHORIZED, "토큰 생성에 실패하였습니다."),
    FAILURE_LOGIN(40106, HttpStatus.UNAUTHORIZED, "로그인에 실패하였습니다."),
    FAILURE_LOGOUT(40107, HttpStatus.UNAUTHORIZED, "로그아웃에 실패하였습니다."),
    TOKEN_UNKNOWN_ERROR(40106, HttpStatus.UNAUTHORIZED, "알 수 없는 토큰입니다."),
    NO_MATCH_APPLE_PUBLIC_KEY_ERROR(40107, HttpStatus.UNAUTHORIZED, "No matching Apple public key found for kid"),
    FAILED_LOAD_OR_PARSE_APPLE_PUBLIC_KEY_ERROR(40108, HttpStatus.UNAUTHORIZED, "Failed to load or parse Apple public key"),
    SOCIAL_LOGIN_ERROR(40109, HttpStatus.UNAUTHORIZED, "Social Login Failed"),
    INVALID_APPLE_TOKEN_SIGNATURE_ERROR(40110, HttpStatus.UNAUTHORIZED, "Invalid identity token signature"),
    INVALID_APPLE_ISSUER_ERROR(40111, HttpStatus.UNAUTHORIZED, "Invalid apple issuer"),
    APPLE_IDENTITY_TOKEN_EXPIRED_ERROR(40112, HttpStatus.UNAUTHORIZED, "Invalid identity token signature"),
    APPLE_IDENTITY_TOKEN_VERIFICATION_ERROR(40113, HttpStatus.UNAUTHORIZED, "identity token verification failed"),
    INVALID_GOOGLE_TOKEN(40114, HttpStatus.UNAUTHORIZED, "유효하지 않은 Google ID 토큰입니다."),

    FILE_UPLOAD_ERROR(42201, HttpStatus.UNPROCESSABLE_ENTITY, "파일 업로드에 실패하였습니다."),

    ACCESS_DENIED_ERROR(40300, HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    NOT_FOUND_USER(40401, HttpStatus.NOT_FOUND, "해당 사용자가 존재하지 않습니다."),
    NOT_FOUND_METADATA_ITEM(40402,HttpStatus.NOT_FOUND,"해당 메타데이터 항목이 존재하지 않습니다."),
    NOT_FOUND_PROJECT(40403, HttpStatus.NOT_FOUND, "해당 프로젝트가 존재하지 않습니다."),

    ALREADY_EXISTS_METADATA(40900, HttpStatus.CONFLICT, "이미 존재하는 메타데이터입니다."),
    ALREADY_JOINED_PROJECT(40901, HttpStatus.CONFLICT, "이미 가입된 프로젝트입니다."),
    PROJECT_NOT_AVAILABLE(40009, HttpStatus.BAD_REQUEST, "해당 프로젝트는 현재 참여할 수 없습니다."),

    SERVER_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러 입니다."),
    ILLEGAL_STATE(50003, HttpStatus.INTERNAL_SERVER_ERROR, "잘못된 상태입니다."),
    RUNTIME_EXCEPTION(50004, HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류입니다."),
    EXTERNAL_API_ERROR(50300, HttpStatus.SERVICE_UNAVAILABLE, "외부 API 요청에 실패하였습니다."),

    // 여행 관련 에러 코드
    REROLL_NOT_AVAILABLE(50100, HttpStatus.CONFLICT, "더 이상 리롤할 수 없습니다."),
    NO_AVAILABLE_TRIP_REGION(50101, HttpStatus.BAD_REQUEST, "추천 가능한 여행 지역이 없습니다."),
    NOT_FOUND_TRIP(40404, HttpStatus.NOT_FOUND, "해당 여행이 존재하지 않습니다."),
    INVALID_TRIP_REGION_SELECTION(40010, HttpStatus.BAD_REQUEST, "선택한 여행 지역이 후보에 존재하지 않습니다."),
    INVALID_INVITE_CODE(40405, HttpStatus.NOT_FOUND, "유효하지 않은 초대 코드입니다."),
    TRIP_NOT_JOINABLE(40011, HttpStatus.BAD_REQUEST, "현재 참여할 수 없는 여행입니다."),
    ALREADY_JOINED_TRIP(40902, HttpStatus.CONFLICT, "이미 참여한 여행입니다."),
    TRIP_FULL(40903, HttpStatus.CONFLICT, "여행 정원이 가득 찼습니다."),

    ;

    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;
}
