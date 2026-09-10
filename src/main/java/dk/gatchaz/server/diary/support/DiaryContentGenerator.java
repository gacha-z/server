package dk.gatchaz.server.diary.support;

import dk.gatchaz.server.diary.dto.DiaryTripContext;
import dk.gatchaz.server.common.exception.CommonException;
import dk.gatchaz.server.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 생성형 AI로 여행 일기 본문을 생성하는 컴포넌트.
 * 시스템 프롬프트(작성 규칙)는 서버 내부 고정값(DEFAULT_SYSTEM_PROMPT)이다.
 * 요청에 promptOverride 가 오면 그 값으로 시스템 프롬프트를 덮어쓰고, 없으면 고정 프롬프트를 사용한다.
 * 공급자는 application yml 설정으로 교체 가능(OpenAI 호환).
 */
@Slf4j
@Component
public class DiaryContentGenerator {

    /** 서버 내부 고정 시스템 프롬프트. 요청에 promptOverride 가 없으면 이 값을 사용한다. */
    private static final String DEFAULT_SYSTEM_PROMPT = """
            당신은 여행 일기를 쓰는 작가입니다. 아래 규칙을 반드시 지키세요.
            1. 반드시 순 한글로만 작성하고, 한자나 중국어 문자는 절대 사용하지 마세요. 한자어도 모두 한글로 풀어서 표기합니다. (예: '期間'→'기간', '時間'→'시간', '忙碌'→'바쁨')
            2. 일기를 쓰는 자연스럽고 담백한 톤으로 작성하세요.
            3. 주어진 내용 안에서만 작성하고, 주어지지 않은 내용은 지어내지 마세요.
            4. 제목 없이 본문만 작성하세요.
            5. 주어진 내용에 비해 길이는 최대 글자수 5배로만 생성하세요.
            6. 날짜는 작성하지 마세요.
            """;

    private final ChatClient chatClient;

    public DiaryContentGenerator(final ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 사용자 입력(userInput)과 (선택)여행 컨텍스트로 일기 본문을 생성한다.
     * promptOverride 가 있으면 그 값으로 시스템 프롬프트를 덮어쓰고, 없으면 DEFAULT_SYSTEM_PROMPT 를 사용한다.
     * 응답이 비었거나 호출에 실패하면 CommonException(AI_GENERATION_FAILED) 로 변환한다.
     */
    public String generate(final String userInput, final String promptOverride, final DiaryTripContext context) {
        // promptOverride 가 있으면 그 값으로 덮어쓰고, 없으면 서버 고정 프롬프트를 사용한다.
        final String systemPrompt = StringUtils.hasText(promptOverride) ? promptOverride : DEFAULT_SYSTEM_PROMPT;
        final String userPrompt = buildUserPrompt(userInput, context);
        try {
            final String content = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            if (!StringUtils.hasText(content)) {
                throw new CommonException(ErrorCode.AI_GENERATION_FAILED);
            }
            return content.strip();
        } catch (final CommonException e) {
            throw e;
        } catch (final Exception e) {
            log.error("AI 일기 생성 실패: {}", e.getMessage(), e);
            throw new CommonException(ErrorCode.AI_GENERATION_FAILED);
        }
    }

    private String buildUserPrompt(final String userInput, final DiaryTripContext context) {
        final StringBuilder sb = new StringBuilder();
        if (context != null) {
            if (StringUtils.hasText(context.getRegionName())) {
                sb.append("여행지: ").append(context.getRegionName()).append('\n');
            }
            if (context.getStartDate() != null) {
                sb.append("여행 기간: ").append(context.getStartDate());
                if (context.getEndDate() != null) {
                    sb.append(" ~ ").append(context.getEndDate());
                }
                sb.append('\n');
            }
            if (StringUtils.hasText(context.getTitle())) {
                sb.append("여행 제목: ").append(context.getTitle()).append('\n');
            }
        }
        sb.append("사용자가 쓴 내용:\n").append(userInput).append('\n');
        sb.append("위 내용을 바탕으로 여행 일기를 작성해 줘.");
        return sb.toString();
    }
}
