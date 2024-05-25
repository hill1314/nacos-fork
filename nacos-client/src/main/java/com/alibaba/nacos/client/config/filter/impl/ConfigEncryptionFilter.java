/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.client.config.filter.impl;

import com.alibaba.nacos.api.config.filter.AbstractConfigFilter;
import com.alibaba.nacos.api.config.filter.IConfigFilterChain;
import com.alibaba.nacos.api.config.filter.IConfigRequest;
import com.alibaba.nacos.api.config.filter.IConfigResponse;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.utils.Pair;
import com.alibaba.nacos.plugin.encryption.handler.EncryptionHandler;

import java.util.Objects;
import java.util.Properties;

/**
 * 配置加密过滤器
 * Configure encryption filter.
 *
 * @author lixiaoshuang
 */
public class ConfigEncryptionFilter extends AbstractConfigFilter {
    
    private static final String DEFAULT_NAME = ConfigEncryptionFilter.class.getName();
    
    @Override
    public void init(Properties properties) {
    
    }
    
    @Override
    public void doFilter(IConfigRequest request, IConfigResponse response, IConfigFilterChain filterChain)
            throws NacosException {
        if (Objects.nonNull(request) && request instanceof ConfigRequest && Objects.isNull(response)) {
            
            // Publish configuration, encrypt
            ConfigRequest configRequest = (ConfigRequest) request;
            String dataId = configRequest.getDataId();
            String content = configRequest.getContent();
            
            Pair<String, String> pair = EncryptionHandler.encryptHandler(dataId, content);
            String secretKey = pair.getFirst();
            String encryptContent = pair.getSecond();
            
            ((ConfigRequest) request).setContent(encryptContent);
            ((ConfigRequest) request).setEncryptedDataKey(secretKey);
        }
        if (Objects.nonNull(response) && response instanceof ConfigResponse && Objects.isNull(request)) {
            
            // Get configuration, decrypt
            ConfigResponse configResponse = (ConfigResponse) response;
            
            String dataId = configResponse.getDataId();
            String encryptedDataKey = configResponse.getEncryptedDataKey();
            String content = configResponse.getContent();
            
            Pair<String, String> pair = EncryptionHandler.decryptHandler(dataId, encryptedDataKey, content);
            String decryptContent = pair.getSecond();
            ((ConfigResponse) response).setContent(decryptContent);
        }
        filterChain.doFilter(request, response);
    }
    
    @Override
    public int getOrder() {
        return 0;
    }
    
    @Override
    public String getFilterName() {
        return DEFAULT_NAME;
    }
    
}
