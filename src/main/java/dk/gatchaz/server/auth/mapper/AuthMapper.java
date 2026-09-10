package dk.gatchaz.server.auth.mapper;

import dk.gatchaz.server.auth.dto.RefreshTokenResponse;
import dk.gatchaz.server.auth.dto.RefreshTokenSaveParam;
import dk.gatchaz.server.auth.dto.SocialAccountResponse;
import dk.gatchaz.server.auth.dto.SocialAccountSaveParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthMapper {

    /**
     * 소셜 제공자(provider)+제공자 사용자 ID(providerUserId) 로 연결된 소셜 계정을 조회한다.
     * 이미 가입한 사용자인지 판단(로그인/최초가입 분기)하는 용도. 없으면 null.
     * TODO: 탈퇴한 회원의 계정에 연결된 소셜 계정도 그대로 조회됨 - 재가입(소셜 계정 재사용) 정책 확정 필요.
     */
    SocialAccountResponse selectSocialAccount(@Param("provider") String provider, @Param("providerUserId") String providerUserId);

    /**
     * 소셜 계정을 새로 연결(등록)한다. 생성된 socialAccountId 는 param.socialAccountId 에 채워진다.
     */
    int insertSocialAccount(SocialAccountSaveParam param);

    /**
     * 리프레시 토큰을 새로 발급/저장한다. 생성된 refreshTokenId 는 param.refreshTokenId 에 채워진다.
     */
    int insertRefreshToken(RefreshTokenSaveParam param);

    /**
     * 폐기(revoke)되지 않았고 만료되지 않은 리프레시 토큰을 조회한다. (토큰 재발급/검증용) 없으면 null.
     */
    RefreshTokenResponse selectValidRefreshToken(@Param("token") String token);

    /**
     * 리프레시 토큰 1건을 폐기 처리한다. (재발급 시 기존 토큰 무효화, 로그아웃 등)
     */
    int revokeRefreshToken(@Param("token") String token);

    /**
     * 회원(memberId)의 유효한 리프레시 토큰을 모두 폐기 처리한다. (전체 기기 로그아웃 등)
     */
    int revokeAllRefreshTokensByMember(@Param("memberId") Long memberId);
}
