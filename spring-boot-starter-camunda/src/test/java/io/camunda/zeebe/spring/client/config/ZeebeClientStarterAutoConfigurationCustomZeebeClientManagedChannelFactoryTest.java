package io.camunda.zeebe.spring.client.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.JsonMapper;
import io.camunda.zeebe.client.impl.ZeebeClientImpl;
import io.camunda.zeebe.spring.client.CamundaAutoConfiguration;
import io.camunda.zeebe.spring.client.configuration.ZeebeClientManagedChannelFactory;
import io.camunda.zeebe.spring.client.configuration.ZeebeClientProdAutoConfiguration;
import io.camunda.zeebe.spring.client.properties.ZeebeClientConfigurationProperties;
import io.grpc.ManagedChannel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@TestPropertySource(
  properties = {
    "zeebe.client.broker.gatewayAddress=localhost12345",
    "zeebe.client.requestTimeout=99s",
    "zeebe.client.job.timeout=99s",
    "zeebe.client.job.pollInterval=99s",
    "zeebe.client.worker.maxJobsActive=99",
    "zeebe.client.worker.threads=99",
    "zeebe.client.worker.defaultName=testName",
    "zeebe.client.worker.defaultType=testType",
    "zeebe.client.worker.override.foo.enabled=false",
    "zeebe.client.message.timeToLive=99s",
    "zeebe.client.security.certpath=aPath",
    "zeebe.client.security.plaintext=true"
  }
)
@ContextConfiguration(classes = {CamundaAutoConfiguration.class, ZeebeClientStarterAutoConfigurationCustomZeebeClientManagedChannelFactoryTest.TestConfig.class})
public class ZeebeClientStarterAutoConfigurationCustomZeebeClientManagedChannelFactoryTest {

  public static class TestConfig {
    @Bean
    public ObjectMapper objectMapper() {
      return new ObjectMapper();
    }

    @Bean
    public ZeebeClientManagedChannelFactory customZeebeChannelFactory() {
      return new TestZeebeClientManagedChannelFactory();
    }

    private static class TestZeebeClientManagedChannelFactory implements ZeebeClientManagedChannelFactory {
      @Override
      public ManagedChannel createChannel(ZeebeClientConfigurationProperties configurationProperties) {
        return ZeebeClientImpl.buildChannel(configurationProperties);
      }
    }
  }

  @Autowired
  private JsonMapper jsonMapper;
  @Autowired
  private ZeebeClientProdAutoConfiguration autoConfiguration;
  @Autowired
  private ApplicationContext applicationContext;
  @Autowired
  private ZeebeClient zeebeClient;
  @Autowired
  private ZeebeClientManagedChannelFactory zeebeClientManagedChannelFactory;/////


  @Test
  void getJsonMapper() {
    assertThat(jsonMapper).isNotNull();
    assertThat(autoConfiguration).isNotNull();
    assertThat(zeebeClient).isNotNull();
    assertThat(zeebeClientManagedChannelFactory).isNotNull();
    assertThat(zeebeClientManagedChannelFactory).isInstanceOf(TestConfig.TestZeebeClientManagedChannelFactory.class);
    Map<String, JsonMapper> jsonMapperBeans = applicationContext.getBeansOfType(JsonMapper.class);
    Object objectMapper = ReflectionTestUtils.getField(jsonMapper, "objectMapper");

    assertThat(jsonMapperBeans.size()).isEqualTo(1);
    assertThat(jsonMapperBeans.containsKey("zeebeJsonMapper")).isTrue();
    assertThat(jsonMapperBeans.get("zeebeJsonMapper")).isSameAs(jsonMapper);
    assertThat(objectMapper).isNotNull();
    assertThat(objectMapper).isInstanceOf(ObjectMapper.class);
    assertThat(((ObjectMapper) objectMapper).getDeserializationConfig()).isNotNull();
    assertThat(((ObjectMapper) objectMapper).getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)).isFalse();
    assertThat(((ObjectMapper) objectMapper).getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)).isFalse();
  }

}
