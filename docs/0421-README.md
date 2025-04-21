# Spring Scheduler

Spring Scheduler는 Spring 프레임워크에서 제공하는 시간 기반 작업을 자동으로 실행할 수 있도록 도와주는 기능. 
주기적인 작업, 배치 작업, 특정 시간에 실행해야 하는 작업 등을 쉽게 구현가능하다.
스케줄러는 운영 중 자동 실행되는 ‘백그라운드 작업’이 핵심!

참고 문서: https://spring.io/guides/gs/scheduling-tasks

## 1. Java 코드로만 반복되는 시간 기반 작업 구현하기

Spring의 Scheduler 기능을 사용하지 않고 순수 Java 코드만으로 주기적인 작업을 구현하는 방법입니다. 
주로 Thread를 사용하여 무한 루프를 구현합니다.

### 1.1 Thread와 Runnable 사용

```java
public class SimpleScheduler {
    public static void main(String[] args) {
        // 작업을 정의하는 Runnable 구현
        Runnable task = () -> {
            while (true) {
                try {
                    System.out.println("작업 실행: " + new Date());
                    // 10초 대기
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        };

        // 스레드 생성 및 시작
        Thread thread = new Thread(task);
        thread.setDaemon(true); // 메인 스레드가 종료되면 같이 종료되도록 설정
        thread.start();

        // 메인 스레드가 바로 종료되지 않도록 대기
        try {
            Thread.sleep(60000); // 60초 동안 실행
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

### 1.2 ScheduledExecutorService 사용 (Java 5 이상)

```java
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Date;

public class JavaScheduler {
    public static void main(String[] args) {
        // 스케줄러 생성
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // 주기적으로 실행할 작업 정의
        Runnable task = () -> {
            System.out.println("작업 실행: " + new Date());
        };

        // 작업 스케줄링: 초기 지연 1초, 이후 10초마다 반복
        scheduler.scheduleAtFixedRate(task, 1, 10, TimeUnit.SECONDS);

        // 프로그램이 바로 종료되지 않도록 대기
        try {
            Thread.sleep(60000); // 60초 동안 실행
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 스케줄러 종료
        scheduler.shutdown();
    }
}
```

### 1.3 Timer와 TimerTask 사용 (레거시 방식)

```java
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;

public class TimerScheduler {
    public static void main(String[] args) {
        // 타이머 생성
        Timer timer = new Timer();

        // 작업 정의
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                System.out.println("작업 실행: " + new Date());
            }
        };

        // 작업 스케줄링: 1초 후 시작, 10초마다 반복
        timer.scheduleAtFixedRate(task, 1000, 10000);

        // 프로그램이 바로 종료되지 않도록 대기
        try {
            Thread.sleep(60000); // 60초 동안 실행
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 타이머 종료
        timer.cancel();
    }
}
```

## 2. Spring @Scheduler 사용법

`@Scheduled` 어노테이션을 사용하여 간편하게 스케줄링 작업을 구현할 수 있음.
작은 프로젝트에서는 @Scheduled 만으로 충분하지만, 큰 서비스는 **Spring Batch**, Quartz, Job 큐(RabbitMQ 등) 으로 확장됩니다.

### 2.1 기본 설정

먼저 Spring Boot 애플리케이션에서 스케줄링을 활성화하기 위해 `@EnableScheduling` 어노테이션을 추가합니다.

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 2.2 @Scheduled 어노테이션 사용

```java
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class ScheduledTasks {

    // 고정 지연 방식: 이전 작업 완료 후 5초 대기 후 다음 작업 실행
    @Scheduled(fixedDelay = 5000)
    public void scheduleFixedDelayTask() {
        System.out.println("Fixed Delay Task: " + LocalDateTime.now());
    }

    // 고정 속도 방식: 이전 작업 시작 시간으로부터 5초마다 실행
    @Scheduled(fixedRate = 5000)
    public void scheduleFixedRateTask() {
        System.out.println("Fixed Rate Task: " + LocalDateTime.now());
    }

    // 초기 지연 설정: 애플리케이션 시작 후 3초 대기 후 첫 작업 실행, 이후 5초마다 실행
    @Scheduled(fixedRate = 5000, initialDelay = 3000)
    public void scheduleFixedRateWithInitialDelayTask() {
        System.out.println("Fixed Rate Task with Initial Delay: " + LocalDateTime.now());
    }

    // Cron 표현식 사용: 매일 오전 8시에 실행
    @Scheduled(cron = "0 0 8 * * ?")
    public void scheduleCronTask() {
        System.out.println("Cron Task: " + LocalDateTime.now());
    }
}
```

cron 표현식이란? https://cron.help/

## 3. 실무에서 사용하는 패턴

### 3.1 스케줄러 작동 여부 설정

실무에서는 환경(개발, 테스트, 운영)에 따라 스케줄러의 작동 여부를 설정할 필요가 있다.
개발에서는 작동하지 않는다거나 Profile을 설정한다.

#### 3.1.1 프로퍼티를 통한 스케줄러 활성화/비활성화

application.properties 또는 application.yml 파일:
```yaml
# application.yml
scheduler:
  enabled: true
```

Java 설정 클래스:
```java
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulerConfig {
    // 스케줄러 관련 추가 설정이 필요한 경우 여기에 작성
}
```

#### 3.1.2 프로필에 따른 스케줄러 활성화/비활성화

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@Profile({"production"}) // production, staging 프로필에서만 스케줄러 활성화
public class SchedulerConfig {
    // 스케줄러 관련 추가 설정
}
```

### 3.2 분산 환경에서의 스케줄러 (분산락)

여러 서버에서 동일한 스케줄러가 실행될 경우, 중복 실행을 방지하기 위해 분산락(Distributed Lock)을 사용합니다.

#### 3.2.1 Redis를 이용한 분산락 구현

```java
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class RedisLockScheduler {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisLockScheduler(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void scheduledTask() {
        String lockKey = "scheduler:lock:task1";
        boolean acquired = false;

        try {
            // 락 획득 시도
            acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofMinutes(5));

            if (acquired) {
                // 락을 획득한 경우에만 작업 수행
                System.out.println("Executing scheduled task...");
                // 실제 작업 로직
            } else {
                System.out.println("Task is already being executed by another instance");
            }
        } finally {
            // 작업이 완료되면 락 해제
            if (acquired) {
                redisTemplate.delete(lockKey);
            }
        }
    }
}
```

----

# Spring Profiles 이해하기

## 1. 프로필(Profile)이란?

https://docs.spring.io/spring-boot/reference/features/profiles.html

애플리케이션이 실행되는 환경에 따라 다른 설정을 적용할 수 있게 해주는 Spring의 기능
로컬 개발 환경과 실제 운영 환경에서 서로 다른 설정을 적용하게 한다.

- **로컬 환경**: 개발자의 로컬 컴퓨터에서 실행될 때 사용하는 설정
- **개발 환경**: 개발 서버에서 실행될 때 사용하는 설정
- **테스트 환경**: 테스트 서버에서 실행될 때 사용하는 설정
- **운영 환경**: 실제 사용자가 접근하는 운영 서버에서 실행될 때 사용하는 설정

여러분들은 로컬 환경 (local) + 운영 환경만 (production, release) 가져가는 것을 추천함.

### 1.1 프로필 사용의 장점

1. **환경별 설정 분리**: 개발, 테스트, 운영 환경에 맞는 설정을 분리하여 관리
2. **코드 변경 없이 설정 변경**: 동일한 코드로 다양한 환경에서 실행 가능
3. **보안 강화**: 민감한 정보(DB 접속 정보, API 키 등)를 환경별로 분리하여 관리
4. **조건부 빈 등록**: 특정 환경에서만 필요한 빈을 조건부로 등록 가능

---

## 2. 프로필 설정 방법

### 2.1 application.properties/yaml 파일에서 프로필 설정

#### 2.1.1 application.yaml 파일 분리 방식

각 프로필별로 별도의 파일을 만드는 방법:

아래와 같이 했을때 **spring.active.profiles=local** 과 같이 설정하여 app 을 실행하면 해당하는 환경설정의 내용만 override 되어 실행된다. 

- `application.yaml`: 공통 설정
- `application-local.yaml`: 로컬 환경 설정
- `application-prod.yaml`: 운영 환경 설정

#### 2.1.2 하나의 application.yaml 파일에서 프로필 구분

하나의 파일 내에서 `---`(세 개의 대시)를 사용하여 프로필을 구분하는 방법:

```yaml
# 공통 설정
spring:
  application:
    name: mentoring-app

---
# 로컬 환경 설정
spring:
  config:
    activate:
      on-profile: local
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: create-drop

---
# 운영 환경 설정
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: jdbc:mysql://prod-db-server:3306/mentoring
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: ${DB_USERNAME}  # 환경 변수에서 가져옴
    password: ${DB_PASSWORD}  # 환경 변수에서 가져옴
  jpa:
    hibernate:
      ddl-auto: validate  # 운영 환경에서는 스키마 변경 방지
```

### 2.2 Java 코드에서 프로필 설정

#### 2.2.1 @Profile 어노테이션 사용

특정 프로필에서만 빈을 등록하려면 `@Profile` 어노테이션을 사용

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class DatabaseConfig {

    @Bean
    @Profile("local")
    public DataSource localDataSource() {
        // 로컬 환경용 데이터소스 설정
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .build();
    }

    @Bean
    @Profile("prod")
    public DataSource productionDataSource() {
        // 운영 환경용 데이터소스 설정
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://prod-db-server:3306/mentoring");
        dataSource.setUsername(System.getenv("DB_USERNAME"));
        dataSource.setPassword(System.getenv("DB_PASSWORD"));
        return dataSource;
    }
}
```

## 3. 프로필 활성화 방법

### 3.1 application.properties/yaml에서 활성화

```yaml
spring:
  profiles:
    active: local
```

### 3.2 환경 변수로 활성화

```bash
export SPRING_PROFILES_ACTIVE=prod
```

### 3.3 JVM 시스템 속성으로 활성화

```bash
java -Dspring.profiles.active=prod -jar app.jar
```

### 3.4 명령행 인수로 활성화

```bash
java -jar app.jar --spring.profiles.active=prod
```

### 3.5 프로그래밍 방식으로 활성화

```java
SpringApplication app = new SpringApplication(Application.class);
app.setAdditionalProfiles("prod");
app.run(args);
```

#### 운영 환경에서 실행

```bash
# 환경 변수 설정
export DB_USERNAME=prod_user
export DB_PASSWORD=secure_password
export SPRING_PROFILES_ACTIVE=prod

# 애플리케이션 실행
java -jar mentoring-app.jar
```
