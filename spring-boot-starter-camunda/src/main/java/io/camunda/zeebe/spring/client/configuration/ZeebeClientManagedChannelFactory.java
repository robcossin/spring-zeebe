package io.camunda.zeebe.spring.client.configuration;

import io.camunda.zeebe.spring.client.properties.ZeebeClientConfigurationProperties;
import io.grpc.ManagedChannel;

public interface ZeebeClientManagedChannelFactory {
  ManagedChannel createChannel(ZeebeClientConfigurationProperties configurationProperties);
}
