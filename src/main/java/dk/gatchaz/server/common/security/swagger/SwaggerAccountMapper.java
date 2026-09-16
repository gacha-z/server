package dk.gatchaz.server.common.security.swagger;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SwaggerAccountMapper {

    /**
     * 사용 중(use_yn = 'Y')인 스웨거 열람 계정을 username 으로 조회한다. 없으면 null.
     */
    SwaggerAccountInfo selectByUsername(@Param("username") String username);
}
