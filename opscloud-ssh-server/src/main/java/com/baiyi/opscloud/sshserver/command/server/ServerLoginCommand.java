package com.baiyi.opscloud.sshserver.command.server;

import com.baiyi.opscloud.common.exception.ssh.SshCommonException;
import com.baiyi.opscloud.common.util.BeanCopierUtil;
import com.baiyi.opscloud.common.util.IdUtil;
import com.baiyi.opscloud.domain.generator.opscloud.Server;
import com.baiyi.opscloud.domain.generator.opscloud.TerminalSessionInstance;
import com.baiyi.opscloud.domain.vo.server.ServerVO;
import com.baiyi.opscloud.interceptor.SuperAdminInterceptor;
import com.baiyi.opscloud.service.server.ServerService;
import com.baiyi.opscloud.sshcore.audit.ServerCommandAudit;
import com.baiyi.opscloud.sshcore.builder.TerminalSessionInstanceBuilder;
import com.baiyi.opscloud.sshcore.enums.InstanceSessionTypeEnum;
import com.baiyi.opscloud.sshcore.facade.SimpleTerminalSessionFacade;
import com.baiyi.opscloud.sshcore.handler.HostSystemHandler;
import com.baiyi.opscloud.sshcore.handler.RemoteInvokeHandler;
import com.baiyi.opscloud.sshcore.model.HostSystem;
import com.baiyi.opscloud.sshcore.model.JSchSession;
import com.baiyi.opscloud.sshcore.model.JSchSessionContainer;
import com.baiyi.opscloud.sshcore.model.SessionIdMapper;
import com.baiyi.opscloud.sshserver.PromptColor;
import com.baiyi.opscloud.sshserver.SshContext;
import com.baiyi.opscloud.sshserver.SshShellCommandFactory;
import com.baiyi.opscloud.sshserver.SshShellHelper;
import com.baiyi.opscloud.sshserver.annotation.InvokeSessionUser;
import com.baiyi.opscloud.sshserver.annotation.SshShellComponent;
import com.baiyi.opscloud.sshserver.command.context.SessionCommandContext;
import com.baiyi.opscloud.sshserver.config.SshServerArthasConfig;
import com.baiyi.opscloud.sshserver.packer.SshServerPacker;
import com.baiyi.opscloud.sshserver.util.TerminalUtil;
import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.common.channel.ChannelOutputStream;
import org.apache.sshd.server.session.ServerSession;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author baiyi
 * @Date 2021/5/31 4:58 下午
 * @Version 1.0
 */
@Slf4j
@SshShellComponent
@ShellCommandGroup("Server")
@RequiredArgsConstructor
public class ServerLoginCommand implements InitializingBean {

    private static String SERVER_EXECUTE_ARTHAS = "cd /tmp && curl -O https://arthas.aliyun.com/arthas-boot.jar && sudo su - app -c 'java -jar /tmp/arthas-boot.jar'\n";

    private final SshServerArthasConfig arthasConfig;

    private final ServerCommandAudit serverCommandAudit;

    private final SshShellHelper sshShellHelper;

    private final ServerService serverService;

    private final HostSystemHandler hostSystemHandler;

    private final SshServerPacker sshServerPacker;

    private final SimpleTerminalSessionFacade simpleTerminalSessionFacade;

    private final SuperAdminInterceptor sAInterceptor;

    private String toInstanceId(Server server) {
        ServerVO.Server serverVO = BeanCopierUtil.copyProperties(server, ServerVO.Server.class);
        sshServerPacker.wrap(serverVO);
        return Joiner.on("#").join(serverVO.getDisplayName(), server.getPrivateIp(), IdUtil.buildUUID());
    }

    @InvokeSessionUser(invokeAdmin = true)
    @ShellMethod(value = "登录服务器(开启会话)", key = {"open", "login"})
    public void login(@ShellOption(help = "ID", defaultValue = "1") int id,
                      @ShellOption(help = "Account", defaultValue = "") String account,
                      @ShellOption(value = {"-R", "--arthas"}, help = "Arthas") boolean arthas,
                      @ShellOption(value = {"-A", "--admin"}, help = "Admin") boolean admin) {
        ServerSession serverSession = sshShellHelper.getSshSession();
        String sessionId = SessionIdMapper.getSessionId(serverSession.getIoSession());
        Terminal terminal = getTerminal();
        SshContext sshContext = getSshContext();
        Map<Integer, Integer> idMapper = SessionCommandContext.getIdMapper();
        Server server = serverService.getById(idMapper.get(id));
        sAInterceptor.interceptLoginServer(server.getId());
        String instanceId = toInstanceId(server);
        try {
            HostSystem hostSystem = hostSystemHandler.buildHostSystem(server, account, admin);
            hostSystem.setInstanceId(instanceId);
            hostSystem.setTerminalSize(sshShellHelper.terminalSize());
            TerminalSessionInstance terminalSessionInstance = TerminalSessionInstanceBuilder.build(sessionId, hostSystem, InstanceSessionTypeEnum.SERVER);
            simpleTerminalSessionFacade.recordTerminalSessionInstance(terminalSessionInstance);

            ChannelOutputStream out = (ChannelOutputStream) sshContext.getSshShellRunnable().getOs();
            // 无延迟
            out.setNoDelay(true);

            RemoteInvokeHandler.openWithSSHServer(sessionId, hostSystem, out);
            TerminalUtil.rawModeSupportVintr(terminal);
            // 计时
            Instant inst1 = Instant.now();
            Size size = terminal.getSize();
            if (!isClosed(sessionId, instanceId) && arthas) {
                try {
                    printWithSession(sessionId, instanceId, SERVER_EXECUTE_ARTHAS);
                    while (isClosed(sessionId, instanceId)) {
                        int ch = terminal.reader().read(1L);
                        if (ch < 0) break;
                    }
                } catch (Exception e) {
                    log.error("exec arthas error: {}", e.getMessage());
                }
            }
            try {
                while (true) {
                    if (isClosed(sessionId, instanceId)) {
                        TimeUnit.MILLISECONDS.sleep(150L);
                        printWithCloseSession("退出登录: 耗时%s/s", inst1);
                        break;
                    }
                    doResize(size, terminal, sessionId, instanceId);
                    int i = terminal.reader().read(5L);
                    printWithSession(sessionId, instanceId, i);
                }
            } catch (Exception e) {
                printWithCloseSession("服务端连接断开: 耗时%s/s", inst1);
            } finally {
                simpleTerminalSessionFacade.closeTerminalSessionInstance(terminalSessionInstance);
            }
        } catch (SshCommonException e) {
            String msg = String.format("ssh连接错误: %s", e.getMessage());
            log.error(msg);
            sshShellHelper.print(msg, PromptColor.RED);
        }
        serverCommandAudit.asyncRecordCommand(sessionId, instanceId);
        JSchSessionContainer.closeSession(sessionId, instanceId);
    }

    private void doResize(Size size, Terminal terminal, String sessionId, String instanceId) {
        if (!terminal.getSize().equals(size)) {
            size = terminal.getSize();
            TerminalUtil.resize(sessionId, instanceId, size);
        }
    }

    private Terminal getTerminal() {
        SshContext sshContext = SshShellCommandFactory.SSH_THREAD_CONTEXT.get();
        if (sshContext == null) {
            throw new IllegalStateException("Unable to find ssh context");
        } else {
            return sshContext.getTerminal();
        }
    }

    private SshContext getSshContext() {
        SshContext sshContext = SshShellCommandFactory.SSH_THREAD_CONTEXT.get();
        if (sshContext == null) {
            throw new IllegalStateException("Unable to find ssh context");
        } else {
            return sshContext;
        }
    }

    /**
     * 打印会话关闭信息
     *
     * @param logout
     * @param instant
     */
    private void printWithCloseSession(String logout, Instant instant) {
        sshShellHelper.print(
                String.format(logout, Duration.between(instant, Instant.now()).getSeconds()),
                PromptColor.RED);
    }

    private boolean isClosed(String sessionId, String instanceId) {
        JSchSession jSchSession = JSchSessionContainer.getBySessionId(sessionId, instanceId);
        assert jSchSession != null;
        return jSchSession.getChannel().isClosed();
    }

    private void printWithSession(String sessionId, String instanceId, int ch) throws Exception {
        if (ch < 0) return;
        JSchSession jSchSession = JSchSessionContainer.getBySessionId(sessionId, instanceId);
        if (jSchSession == null) throw new Exception();
        jSchSession.getCommander().print((char) ch);
    }

    private void printWithSession(String sessionId, String instanceId, String cmd) throws Exception {
        if (StringUtils.isEmpty(cmd)) return;
        JSchSession jSchSession = JSchSessionContainer.getBySessionId(sessionId, instanceId);
        if (jSchSession == null) throw new Exception();
        jSchSession.getCommander().print(cmd);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!StringUtils.isEmpty(arthasConfig.getServer())) {
            SERVER_EXECUTE_ARTHAS = arthasConfig.getServer();
            log.debug("read server arthas configuration: {}", arthasConfig.getServer());
        }
    }

}
