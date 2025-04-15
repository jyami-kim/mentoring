# Kotlin에서 발생하는 OutOfMemoryError (OOM) 이해하기

## 📌 소개
이 문서에서는 Kotlin 애플리케이션에서 **OutOfMemoryError (OOM)** 가 발생하는 원인을 탐색합니다.  
특정 코드 예제와 **JVM 가비지 컬렉션(GC) 로그**를 통해 문제를 설명하겠습니다.

---

## 📝 코드 예제
다음은 메모리 부족을 유발할 수 있는 Kotlin 코드입니다:

```kotlin
"loop" -> {
    println("start loop")
    var count = 0
    while (true) {
        val item = Random.nextInt(100).toString()
        list.add(item)
        if (count % 1000 == 0) {
            println("Item add count: $count")
        }
        count++
    }
}
```
### 🔹 문제점
이 코드에서는 랜덤 정수를 문자열로 변환한 후 리스트에 계속 추가합니다.
종료 조건이 없기 때문에 리스트 크기가 무한히 증가하며, 결국 메모리 부족(OOM) 오류가 발생합니다.

### ⚙️ JVM 옵션

JVM을 다음과 같은 옵션으로 실행한다고 가정합니다:

```
-Xms10m -Xmx50m -Xlog:gc*:logs/gc.log:t,l,tg:filecount=10,filesize=10M -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=logs
```

### 🔍 옵션 설명

| 옵션                                                      | 설명                      |
|---------------------------------------------------------|-------------------------|
| -Xms10m                                                 | 초기 힙(Heap) 크기를 10MB로 설정 |
| -Xmx50m                                                 | 최대 힙 크기를 50MB로 제한       |
| -Xlog:gc\*:logs/gc.log:t,l,tg:filecount=10,filesize=10M | GC 로그를 logs/gc.log에 기록  |
| -XX:+HeapDumpOnOutOfMemoryError                         | OOM 발생 시 힙 덤프 생성        |
| -XX:HeapDumpPath=logs                                   | 힙 덤프 저장 경로 지정           |

### 📊 GC 로그 분석

아래는 실제 GC 로그의 일부입니다
이런 로그를 직접 해석하기 어려우니 GUI tool을 사용합시다. : easy GC

```
[2025-02-17T23:53:48.082+0900][info][gc,heap        ] GC(47) Old regions: 51->51
[2025-02-17T23:53:48.083+0900][info][gc,heap        ] GC(48) Old regions: 51->2
[2025-02-17T23:53:48.083+0900][info][gc             ] GC(48) Pause Full (G1 Compaction Pause) 55M->1M(16M) 1.457ms
```

### 🧐 GC 로그 해석
1. 계속 증가하는 메모리 사용량
  - Eden regions, Old regions, Humongous regions 값이 계속 증가
  - 이는 힙 공간이 점점 채워지고 GC가 자주 발생하는 것을 의미
2. GC 시도
  - GC(47), GC(48) 등의 로그에서 GC가 반복적으로 실행
  - Pause Full (G1 Compaction Pause) → 전체 GC 수행됨
3. OOM 발생 가능성
  - Old regions: 51->2 → GC 후에도 메모리가 완전히 해소되지 않음
  - 최종적으로 GC가 힙을 확보하지 못하면 OOM 발생

---
## 실행시 jvm 옵션 넣는 방법.

### intellij

application 실행 configuration 에서 VM options 에 jvm 옵션을 넣어준다.
```
-Xms10m -Xmx50m -Xlog:gc*:logs/gc.log:t,l,tg:filecount=10,filesize=10M -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=logs
```
![img.png](./image/img.png)


### 외부 서버 vm 에서 command line으로 실행 할 경우

./gradlew build 후 command line으로 실행 할 때 JVM 옵션을 직접 설정해준다.
설정하지 않으면 머신의 default JVM 설정으로 실행된다.
- -Xms: 초기 힙 크기 (기본값은 시스템에 따라 다르며, 일반적으로 1/64의 물리적 메모리)
- -Xmx: 최대 힙 크기 (기본값은 시스템에 따라 다르며, 일반적으로 1/4의 물리적 메모리)

```shell
java -Xms10m -Xmx50m -Xlog:gc*:logs/gc.log:t,l,tg:filecount=10,filesize=10M -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=logs -jar build/libs/kotlin-oom-1.0-SNAPSHOT.jar
```

```shell
scp user@remote_server:/path/to/logs/gc.log /local/path/to/save/gc.log
scp user@remote_server:/path/to/logs/heapdump.hprof /local/path/to/save/heapdump.hprof
```
- user: 원격 서버 사용자 이름
- remote_server : 원격 서버의 주소(ip 또는 도메인)

> 참고 : scp [옵션] [source] [destination]
> [옵션]은 선택 사항으로, -r (디렉토리 복사) 등의 옵션을 사용할 수 있음.
> [source]는 복사할 파일 또는 디렉토리의 경로
> [destination]은 파일 또는 디렉토리를 복사할 대상 경로


----
## easy GC 참고
https://gceasy.io/diamondgc-report.jsp?oTxnId_value=4c2403be-48c5-4f99-80a0-ef4b70193415

### 메모리 영역 구분
- Young Generation: 새로 생성된 객체들이 할당되는 영역으로, 빠르게 수집(빠른 GC)된다.
- Generation: Young Generation에서 오래 살아남은 객체들이 옮겨지는 곳입니다. 여기서의 GC는 상대적으로 시간이 더 걸릴 수 있다.
- Humongous Objects: 매우 큰 객체들이 할당되는 영역으로, 특별한 방식으로 관리된다.
- Meta Space: 클래스 메타데이터 등이 저장되는 영역.

### KPI (Key Performance Indicator)
- Throughput: 전체 실행 시간 중 애플리케이션이 실제 작업을 수행한 비율
- CPU Time : GC로 인해 소비된 CPU 시간
- 정지 시간(Pause Time): GC가 실행되는 동안 STW로 인해 애플리케이션이 멈추는 시간
- Concurrent Time: GC가 실행되는 동안 애플리케이션이 계속 실행되는 시간

### GC 유발 원인
- G1 Compaction Pause: 메모리가 심하게 단편화되어 있을 때, G1 Compaction Pause가 실행되어 단편화된 메모리를 압축(정리)한다.
- G1 Evacuation Pause: 이 GC는 한 region에 있는 살아있는 객체들을 다른 영역으로 복사할 때 발생함. Young Generation 영역만 복사된다면 Young GC가 일어나고, Young + Tenured 영역을 모두 복사하면 Mixed GC가 발생한다.
- G1 Humongous Allocation: Humongous Allocation은 G1에서 하나의 Region 크기의 50%보다 큰 객체가 할당되는 것을 말한다. 이러한 거대 객체가 자주 할당되면 아래와 같은 성능 문제가 발생할 수 있다:
  1. 영역 안에 Humongous 객체가 존재하면, 해당 영역의 마지막 Humongous 객체와 영역 끝 사이 공간이 사용되지 않은 채 남게된다. 이와 같은 Humongous 객체가 여러 개라면, 사용되지 않은 공간들이 힙 단편화를 일으킬 수 있습니다.
  2. Java 1.8u40 이전에는 Humongous 영역의 회수(정리)가 오직 Full GC 이벤트에서만 이루어졌다. 그러나 더 새로운 JVM에서는 Cleanup 단계에서 Humongous 객체를 정리한다.
