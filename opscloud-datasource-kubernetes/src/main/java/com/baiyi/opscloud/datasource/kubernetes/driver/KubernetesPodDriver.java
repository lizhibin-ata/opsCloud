package com.baiyi.opscloud.datasource.kubernetes.driver;

import com.baiyi.opscloud.common.datasource.KubernetesConfig;
import com.baiyi.opscloud.datasource.kubernetes.client.KubeClient;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.kubernetes.client.dsl.Listable;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @Author baiyi
 * @Date 2021/6/24 11:16 下午
 * @Version 1.0
 */
public class KubernetesPodDriver {

    public static List<Pod> listPod(KubernetesConfig.Kubernetes kubernetes) {
        PodList podList = KubeClient.build(kubernetes)
                .pods()
                .list();
        return podList.getItems();
    }

    public static List<Pod> listPod(KubernetesConfig.Kubernetes kubernetes, String namespace) {
        PodList podList = KubeClient.build(kubernetes)
                .pods()
                .inNamespace(namespace)
                .list();
        if (CollectionUtils.isEmpty(podList.getItems()))
            return Collections.emptyList();
        return podList.getItems();
    }

    public static List<Pod> listPod(KubernetesConfig.Kubernetes kubernetes, String namespace, String deploymentName) {
        KubernetesClient client = KubeClient.build(kubernetes);
        Map<String, String> matchLabels = client.apps()
                .deployments()
                .inNamespace(namespace)
                .withName(deploymentName)
                .get()
                .getSpec()
                .getSelector()
                .getMatchLabels();
        if (matchLabels.isEmpty()) return Collections.emptyList();
        return Optional.of(client.pods().inNamespace(namespace).withLabels(matchLabels))
                .map(Listable::list)
                .map(PodList::getItems)
                .orElse(Collections.emptyList());
    }

    public static List<Pod> listPod(KubernetesConfig.Kubernetes kubernetes, String namespace, Map<String, String> labels) {
        PodList podList = KubeClient.build(kubernetes)
                .pods()
                .inNamespace(namespace)
                .withLabels(labels)
                .list();
        if (CollectionUtils.isEmpty(podList.getItems()))
            return Collections.emptyList();
        return podList.getItems();
    }

    /**
     * @param kubernetes
     * @param namespace
     * @param name       podName
     * @return
     */
    public static Pod getPod(KubernetesConfig.Kubernetes kubernetes, String namespace, String name) {
        return KubeClient.build(kubernetes)
                .pods()
                .inNamespace(namespace)
                .withName(name)
                .get();
    }

    public static String getPodLog(KubernetesConfig.Kubernetes kubernetes, String namespace, String name, String container) {
        return KubeClient.build(kubernetes)
                .pods()
                .inNamespace(namespace)
                .withName(name)
                .inContainer(container)
                .getLog();
    }

    public static LogWatch getPodLogWatch(KubernetesConfig.Kubernetes kubernetes, String namespace, String podName) {
        return KubeClient.build(kubernetes).pods()
                .inNamespace(namespace)
                .withName(podName)
                .watchLog();
    }

    public static LogWatch getPodLogWatch(KubernetesConfig.Kubernetes kubernetes, String namespace, String podName, String containerName, Integer lines, OutputStream outputStream) {
        return KubeClient.build(kubernetes)
                .pods()
                .inNamespace(namespace)
                .withName(podName)
                .inContainer(containerName)
                .tailingLines(lines)
                .watchLog(outputStream);
    }

    public static LogWatch getPodLogWatch2(KubernetesConfig.Kubernetes kubernetes, String namespace, String podName, String containerName, Integer lines, OutputStream outputStream) {
        return KubeClient.build(kubernetes)
                .pods()
                .inNamespace(namespace)
                .withName(podName)
                .inContainer(containerName)
                .withLogWaitTimeout(0)
                .watchLog(outputStream);
    }


    /**
     * @param kubernetes
     * @param namespace
     * @return
     */
    public static ExecWatch loginPodContainer(KubernetesConfig.Kubernetes kubernetes,
                                              String namespace,
                                              String podName,
                                              String containerName,
                                              SimpleListener listener,
                                              OutputStream out
    ) {
        return KubeClient.build(kubernetes).pods()
                .inNamespace(namespace)
                .withName(podName)
                .inContainer(containerName) // 如果Pod中只有一个容器，不需要指定
                .redirectingInput()
                //.redirectingOutput()
                //.redirectingError()
                //.redirectingErrorChannel()
                .writingOutput(out)
                .writingError(out)
                .withTTY()
                .usingListener(listener)
                .exec("env", "TERM=xterm", "sh");
    }


    @Data
    public static class SimpleListener implements ExecListener {

        private boolean isClosed = false;

        @Override
        public void onOpen() {
        }

        @Override
        public void onFailure(Throwable t, Response response) {
            this.isClosed = true;
            // throw new SshRuntimeException("Kubernetes Container Failure!");
        }

        @Override
        public void onClose(int code, String reason) {
            this.isClosed = true;
            // throw new SshRuntimeException("Kubernetes Container Close!");
        }
    }

}
