package dk.gatchaz.server.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * LocalTime 역직렬화를 관대하게 처리한다. 정상적인 문자열("HH:mm" 또는 "HH:mm:ss")뿐 아니라,
 * 클라이언트가 실수로 {"hour":10,"minute":0} 같은 객체로 보내는 경우까지 받아줘서
 * "Cannot deserialize value of type LocalTime from Object value" 파싱 에러를 막는다.
 */
public class LenientLocalTimeDeserializer extends StdDeserializer<LocalTime> {

    public LenientLocalTimeDeserializer() {
        super(LocalTime.class);
    }

    @Override
    public LocalTime deserialize(final JsonParser parser, final DeserializationContext ctxt) throws IOException {
        if (parser.currentToken() == JsonToken.VALUE_STRING) {
            final String text = parser.getText().trim();
            return text.isEmpty() ? null : parseText(text, parser, ctxt);
        }

        if (parser.currentToken() == JsonToken.START_OBJECT) {
            final JsonNode node = parser.readValueAsTree();
            final int hour = intField(node, 0, "hour", "hours");
            final int minute = intField(node, 0, "minute", "minutes");
            final int second = intField(node, 0, "second", "seconds");
            final int nano = intField(node, 0, "nano", "nanos", "nanosecond", "nanoseconds");
            try {
                return LocalTime.of(hour, minute, second, nano);
            } catch (final java.time.DateTimeException e) {
                // 클라이언트가 실제 시각이 아닌 값(초기화 안 된 값 등)을 그대로 보낸 경우.
                // 그대로 던지면 원인을 알 수 없는 예외가 되므로, 실제로 어떤 값이 왔는지 메시지에 남긴다.
                throw JsonMappingException.from(parser,
                        "LocalTime 객체 필드 값이 유효하지 않습니다: hour=" + hour + ", minute=" + minute
                                + ", second=" + second + ", nano=" + nano, e);
            }
        }

        return (LocalTime) ctxt.handleUnexpectedToken(LocalTime.class, parser);
    }

    private LocalTime parseText(final String text, final JsonParser parser, final DeserializationContext ctxt)
            throws IOException {
        try {
            return LocalTime.parse(text, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (final DateTimeParseException e) {
            try {
                return LocalTime.parse(text); // ISO_LOCAL_TIME (HH:mm:ss[.nnn])
            } catch (final DateTimeParseException e2) {
                throw JsonMappingException.from(parser, "LocalTime 형식이 올바르지 않습니다: " + text, e2);
            }
        }
    }

    private int intField(final JsonNode node, final int defaultValue, final String... names) {
        for (final String name : names) {
            final JsonNode value = node.get(name);
            if (value != null && value.isInt()) {
                return value.asInt();
            }
        }
        return defaultValue;
    }
}
