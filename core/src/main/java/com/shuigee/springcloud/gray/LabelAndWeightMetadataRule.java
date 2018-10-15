package com.shuigee.springcloud.gray;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

public class LabelAndWeightMetadataRule extends ZoneAvoidanceRule {
    private static final Logger logger = LoggerFactory.getLogger(LabelAndWeightMetadataRule.class);
    public static final String META_DATA_KEY_LABEL_AND = "labelAnd";
    public static final String META_DATA_KEY_LABEL_OR = "labelOr";

    public static final String META_DATA_KEY_WEIGHT = "weight";

    private Random random = new Random();

    @Override
    public Server choose(Object key) {
        logger.info("自定义服务选择器");
        List<Server> serverList = this.getPredicate().getEligibleServers(this.getLoadBalancer().getAllServers(), key);

        if (CollectionUtils.isEmpty(serverList)) {
            return null;
        }

        List<Server> servers = null;

        // 优先匹配label的服务(去除不符合的服务)
        if (CoreHeaderInterceptor.label.get() != null && !CoreHeaderInterceptor.label.get().isEmpty()) {
            servers = new ArrayList<>();
            for (Server server : serverList) {
                InstanceInfo instanceInfo = ((DiscoveryEnabledServer) server).getInstanceInfo();
                // 只处理DB层
                if (!instanceInfo.getAppName().toUpperCase().startsWith("REPOSITORY-DATABASE")) {
                    servers.add(server);
                    continue;
                }
                Map<String, String> metadata = ((DiscoveryEnabledServer) server).getInstanceInfo().getMetadata();

                // 优先匹配label
                String labelOr = metadata.get(META_DATA_KEY_LABEL_OR);
                if (!StringUtils.isEmpty(labelOr)) {
                    List<String> metadataLabel = Arrays.asList(labelOr.split(CoreHeaderInterceptor.HEADER_LABEL_SPLIT));
                    for (String label : metadataLabel) {
                        if (CoreHeaderInterceptor.label.get().contains(label)) {
//                        return server;
                            servers.add(server);
                        }
                    }
                }

//            String labelAnd = metadata.get(META_DATA_KEY_LABEL_AND);
//            if (!StringUtils.isEmpty(labelAnd)) {
//                List<String> metadataLabel = Arrays.asList(labelAnd.split(CoreHeaderInterceptor.HEADER_LABEL_SPLIT));
//                if (CoreHeaderInterceptor.label.get().containsAll(metadataLabel)) {
//                    return server;
//                }
//            }
            }
        } else {
            servers = serverList;
        }

        if (servers != null && !servers.isEmpty()) {
            // 计算总值并剔除0权重节点
            int totalWeight = 0;
            Map<Server, Integer> serverWeightMap = new HashMap<>();
            for (Server server : servers) {
                Map<String, String> metadata = ((DiscoveryEnabledServer) server).getInstanceInfo().getMetadata();

//            // 优先匹配label
//            String labelOr = metadata.get(META_DATA_KEY_LABEL_OR);
//            if (!StringUtils.isEmpty(labelOr)) {
//                List<String> metadataLabel = Arrays.asList(labelOr.split(CoreHeaderInterceptor.HEADER_LABEL_SPLIT));
//                for (String label : metadataLabel) {
//                    if (CoreHeaderInterceptor.label.get().contains(label)) {
////                        return server;
//                        serverWeightMapLabel.put(server)
//                    }
//                }
//            }

//            String labelAnd = metadata.get(META_DATA_KEY_LABEL_AND);
//            if (!StringUtils.isEmpty(labelAnd)) {
//                List<String> metadataLabel = Arrays.asList(labelAnd.split(CoreHeaderInterceptor.HEADER_LABEL_SPLIT));
//                if (CoreHeaderInterceptor.label.get().containsAll(metadataLabel)) {
//                    return server;
//                }
//            }

                String strWeight = metadata.get(META_DATA_KEY_WEIGHT);

                int weight = 100;
                try {
                    weight = Integer.parseInt(strWeight);
                } catch (Exception e) {
                    // 无需处理
                }

                if (weight <= 0) {
                    continue;
                }

                serverWeightMap.put(server, weight);
                totalWeight += weight;
            }

            // 权重随机
            int randomWight = this.random.nextInt(totalWeight);
            int current = 0;
            for (Map.Entry<Server, Integer> entry : serverWeightMap.entrySet()) {
                current += entry.getValue();
                if (randomWight <= current) {
                    return entry.getKey();
                }
            }
        }

        System.out.println("--------------------未找到有效服务--------------------");

        return null;
    }
}
