package com.baiyi.opscloud.facade;

import com.baiyi.opscloud.domain.generator.opscloud.User;
import com.baiyi.opscloud.domain.vo.user.UserCredentialVO;

/**
 * @Author baiyi
 * @Date 2021/6/9 3:35 下午
 * @Version 1.0
 */
public interface UserCredentialFacade {

    void clearCredential(int userId, String instanceUuid, int credentialType);

    void saveCredential(UserCredentialVO.Credential credential);

    /**
     * 内部使用
     *
     * @param credential
     * @param user
     */
    void saveCredential(UserCredentialVO.Credential credential, User user);


    /**
     * 创建用户MFA凭据
     *
     * @param user
     */
    void createMFACredential(User user);

}
