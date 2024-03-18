# 2. 스프링 핵심 원리 이해1 - 예제 만들기

## 프로젝트 생성
- https://jih3508.tistory.com/159 글에서 프로젝트 생성 방법 확인
- JDK 17 이상 설치
- 프로젝트 선택
    - Project: Gradke-Groovy
    -  Project Spring Boot: 3.x.x 
    -  Language: Java 
    -  Packaging: Jar Java: 17
 - Project Metadata
   - groupId: hello
   - artifactId: core
 - Dependencies: 선택하지 않는다.
 - 스프링 부트 3.0 이상은 JDK 17 이상 사용해야 한다.

### Gradle 전체 설정
```
plugins {
	id 'java'
	id 'org.springframework.boot' version '3.1.9'
	id 'io.spring.dependency-management' version '1.1.4'
}

group = 'hello'
version = '0.0.1-SNAPSHOT'

java {
	sourceCompatibility = '17'
}

repositories {
	mavenCentral()
}

dependencies {
	implementation 'org.springframework.boot:spring-boot-starter'
	testImplementation ('org.springframework.boot:spring-boot-starter-test'){
		exclude group: 'org.junit.vintage', module: 'junit-vintage-engine'
	}
}

tasks.named('bootBuildImage') {
	builder = 'paketobuildpacks/builder-jammy-base:latest'
}

tasks.named('test') {
	useJUnitPlatform()
}

```
### IntelliJ Gradle 대신에 자바 직접 실행
- 최근 IntelliJ 버전은 Gradle을 통해서 실행 하는 것이 기본 설정이다. 이렇게 하면 실행속도가 느리다. 다음과 같이 변경하면 자바로 바로 실행해서 실행속도가 더 빠르다.
- 아래 설정후 gradle build시 오류 나면 여기 글 확인(https://jih3508.tistory.com/202)
- Preferences Build, Execution, Deployment Build Tools Gradle
  - Build and run using: Gradle IntelliJ IDEA
  - Run tests using: Gradle IntelliJ IDEA
![Gradle 설정](./img/인텔리제이%20Gradle%20설정.png)
- https://jih3508.tistory.com/202 

## 비즈니스 요구사항과 설계
- 회원
  - 회원을 가입하고 조회할 수 있다.
  - 회원은 일반과 VIP 두 가지 등급이 있다.
  - 회원 데이터는 자체 DB를 구축할 수 있고, 외부 시스템과 연동할 수 있다. (미확정)
- 주문과 할인 정책
  - 회원은 상품을 주문할 수 있다.
  - 회원 등급에 따라 할인 정책을 적용할 수 있다.
  - 할인 정책은 모든 VIP는 1000원을 할인해주는 고정 금액 할인을 적용해달라. (나중에 변경 될 수 있다.)
  - 할인 정책은 변경 가능성이 높다. 회사의 기본 할인 정책을 아직 정하지 못했고, 오픈 직전까지 고민을 미루고 싶다. 최악의 경우 할인을 적용하지 않을 수 도 있다. (미확정)
- 요구사항을 보면 회원 데이터, 할인 정책 같은 부분은 지금 결정하기 어려운 부분이다. 그렇다고 이런 정책이 결정될 때까지 개발을 무기한 기다릴 수 도 없다.
- 위 문제는 인터페이스를 만들고 구현체를 언제든지 갈아끼울 수 있도록 설계하면 된다.

## 회원 도메인 설계
- 회원 도메인 요구사항
  - 회원을 가입하고 조회할 수 있다.
  - 회원은 일반과 VIP 두 가지 등급이 있다.
  - 회원 데이터는 자체 DB를 구축할 수 있고, 외부 시스템과 연동할 수 있다. (미확정)
### 회원 도메인 협력 관계
![회원 도메인 협력 관계](./img/회원%20도메인%20협력%20관계.png)
### 회원 클래스 다이어그램
![회원 클래스 다이어그램](./img/회원%20클래스%20다이어그램.png)
### 회원 객체 다이어그램
![회원 객체 다이어그램](./img/회원%20객체%20다이어그램.png)
- 회원 서비스: MemberServiceImpl

## 회원 도메인 개발
### 회원 등급
```java
package hello.core.member;

public enum Gradle {
    BASIC,
    VIP
}

```

### 회원 엔티티
```java
package hello.core.member;

public class Member {

    private Long id;
    private String name;
    private Gradle gradle;

    public Member(Long id, String name, Gradle gradle) {
        this.id = id;
        this.name = name;
        this.gradle = gradle;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Gradle getGradle() {
        return gradle;
    }

    public void setGradle(Gradle gradle) {
        this.gradle = gradle;
    }
}

```
- Generate: [window] alt + insert로 Generate
  - Constructor, Getter, Setter 불러옴

### 회원 저장소
#### 회원 저장소 인터페이스
```java
package hello.core.member;

public interface MemberRepository {

    void save(Member member);

    Member findById(Long memberId);

}
```

#### 메모리 회원 저장소 구현채
```java
package hello.core.member;

import java.util.HashMap;
import java.util.Map;

public class MemoryMemberRepository implements MemberRepository{

    private static Map<Long, Member> store = new HashMap<>();

    @Override
    public void save(Member member) {
        store.put(member.getId(), member);
    }

    @Override
    public Member findById(Long memberId) {
        return store.get(memberId);
    }
}
```
- 데이터베이스에서 확정이 안되서 HashMap으로 저장소를 관리한다.

### 회원 서비스 인터페이스
```java
package hello.core.member;

public interface MemberService {

    void join(Member member);

    Member findMember(Long memberId);
}
```
### 회원 서비스 구현채
```java
package hello.core.member;

public class MemberServiceImpl implements MemberService{

    private final MemberRepository memberRepository = new MemoryMemberRepository();
    @Override
    public void join(Member member) {
        memberRepository.save(member);
    }

    @Override
    public Member findMember(Long memberId) {
        return memberRepository.findById(memberId);
    }
}
```

## 회원 도메인 실행과 테스트

### 회원 도매인 - 회원 가입 main
```java
package hello.core;

import hello.core.member.Gradle;
import hello.core.member.Member;
import hello.core.member.MemberService;
import hello.core.member.MemberServiceImpl;

public class MemberApp {

    public static void main(String[] args){
        MemberService memberService = new MemberServiceImpl();
        Member member = new Member(1L, "memberA", Gradle.VIP);
        memberService.join(member);

        Member findMember = memberService.findMember(1L);
        System.out.println("new member = " + member.getName());
        System.out.println("find member = " + findMember.getName());
    }
}
```
### 회원 도매인 - 회원 가입 테스트
- main 함수로 테스트로 하는데 좋은 방법이 아님
- Junit으로 테스트 해보자.
```java
package hello.core.member;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class MemberServiceTest {

    MemberService memberService = new MemberServiceImpl();

    @Test
    void join() {
        // given
        Member member = new Member(1L, "memberA", Gradle.VIP);

        //when
        memberService.join(member);
        Member findMember = memberService.findMember(1L);

        //then
        Assertions.assertThat(member).isEqualTo(findMember);
    }
}

```

### 회원 도매인 설계의 문제점
- 의존관계가 인터페이스 뿐만 아니라 구현까지 모두 의존하는 문제점이 있음
  - 저장소 변경 떼 OCP원칙 위해
  - DIP 위배

## 주문과 할인 도매인 설계
- 주문과 할인 정책
  - 회원은 상품을 주문할 수 있다.
  - 회원 등급에 따라 할인 정책을 적용할 수 있다.
  - 할인 정책은 모든 VIP는 1000원을 할인해주는 고정 금액 할인을 적용해달라. (나중에 변경 될 수 있다.)
  - 할인 정책은 변경 가능성이 높다. 회사의 기본 할인 정책을 아직 정하지 못했고, 오픈 직전까지 고민을 미루고 싶다. 최악의 경우 할인을 적용하지 않을 수 도 있다. (미확정)

### 주문 도메인 협력, 역활, 책임
![주문 도메인 협력, 역활, 책임](./img/주문%20도메인%20협력,%20역활,%20책임.png)
1. 주문 생성: 클라이언트는 주문 서비스에 주문 생성을 요청한다.
2. 회원 조회: 할인을 위해서는 회원 등급이 필요하다. 그래서 주문 서비스는 회원 저장소에서 회원을 조회한다.
3. 할인 적용: 주문 서비스는 회원 등급에 따른 할인 여부를 할인 정책에 위임한다.
4. 주문 결과 반환: 주문 서비스는 할인 결과를 포함한 주문 결과를 반환한다.

### 주문 도메인 전체
![주문 도메인 전체](./img/주문%20도메인%20전체.png)
- 역활과 구현을 분리해서 자유롭게 구현 객ㅊ레를 조립할 수 있게 설계했다. 
- 회원 저장소, 할인 정책도 유연하게 변경 할 수 있다.

### 주문 도매인 클래스 다이어그램
![주문 도매인 클래스 다이어그램](./img/주문%20도매인%20클래스%20다이어그램.png)

### 주문 도메인 객체 다이어그램1
![주문 도메인 객체 다이어그램1](./img/주문%20도메인%20객체%20다이어그램1.png)
- 회원을 메모리에서 조회하고, 정액 할인 정책(고정 금액)을 지원해도 주문 서비스를 변경하지 않아도 된다.
- 역할들의 협력 관계를 그대로 재사용 할 수 있다.
### 주문 도메인 객체 다이어그램2
![주문 도메인 객체 다이어그램2](./img/주문%20도메인%20객체%20다이어그램2.png)
- 회원을 메모리가 아닌 실제 DB에서 조회하고, 정률 할인 정책(주문 금액에 따라 % 할인)을 지원해도 주문 서비스를 변경하지 않아도 된다.
- 협력 관계를 그대로 재사용 할 수 있다.

## 주문과 할인 도메인 개발

### 할인 정책 인터페이스
```java
package hello.core.discount;

import hello.core.member.Member;

public interface DiscountPolicy {

    /**
     * @ return 할인 대상 금액
     */
    int discount(Member member, int price);

}

```

### 정액 할인 정책 구현체
```java
package hello.core.discount;


import hello.core.member.Gradle;
import hello.core.member.Member;

public class FixDiscountPolicy  implements DiscountPolicy{
    private int discountFixAmount = 1000; // 1000원 할인

    @Override
    public int discount(Member member, int price) {
        if (member.getGradle() == Gradle.VIP){
            return discountFixAmount;
        }else {
            return 0;
        }
    }
}

```
- VIP면 1000원 할인 해준다.
### 주문 엔티티
```java
package hello.core.order;

public class Order {

    private Long memberId;
    private String itemName;
    private int itemPrice;
    private int discountPrice;

    public Order(Long memberId, String itemName, int itemPrice, int discountPrice) {
        this.memberId = memberId;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.discountPrice = discountPrice;
    }

    public int calculatePrice(){
        return itemPrice - discountPrice;
    }
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(int itemPrice) {
        this.itemPrice = itemPrice;
    }

    public int getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(int discountPrice) {
        this.discountPrice = discountPrice;
    }

    @Override
    public String toString() {
        return "Order{" +
                "memberId=" + memberId +
                ", itemName='" + itemName + '\'' +
                ", itemPrice=" + itemPrice +
                ", discountPrice=" + discountPrice +
                '}';
    }
}
```
### 주문 서비스 인터페이스
```java
package hello.core.order;

public interface OrderService {

    Order createOrder(Long memberId, String itemName, int itemPrice);
}
```

### 주문 서비스 구현체
```java
package hello.core.order;

import hello.core.discount.DiscountPolicy;
import hello.core.discount.FixDiscountPolicy;
import hello.core.member.Member;
import hello.core.member.MemberRepository;
import hello.core.member.MemoryMemberRepository;

public class OrderServiceImpl implements OrderService{

    private final MemberRepository memberRepository = new MemoryMemberRepository();
    private final DiscountPolicy discountPolicy = new FixDiscountPolicy();
    @Override
    public Order createOrder(Long memberId, String itemName, int itemPrice) {
        Member member = memberRepository.findById(memberId);
        int discountPrice = discountPolicy.discount(member, itemPrice);
        return new Order(memberId, itemName, itemPrice, discountPrice);
    }
}
```
- 주문이 요청 들어오면 회원 조회하고나서, 할인 정책을 정한후 주문 객체를 생성한다.
- 메모리 회원 리포지토리, 고정 금액 할인 정책을 구현체로 생성

## 주문과 할인 도메인 실행과 테스트
### 주문과 할인 정책 실행
```java
package hello.core;

import hello.core.member.Gradle;
import hello.core.member.Member;
import hello.core.member.MemberService;
import hello.core.member.MemberServiceImpl;
import hello.core.order.Order;
import hello.core.order.OrderService;
import hello.core.order.OrderServiceImpl;

public class OrderApp {

    public static void main(String[] args){
        MemberService memberService = new MemberServiceImpl();
        OrderService orderService = new OrderServiceImpl();

        Long memberId = 1L;
        Member member = new Member(memberId, "memberA", Gradle.VIP);
        memberService.join(member);

        Order order = orderService.createOrder(memberId, "itemA", 10000);

        System.out.println("order = " + order);

    }
}
```
- 


### 주문과 할인 도메인 실행과 테스트
- 애플리케이션 로직으로 이렇게 테스트 하는 것은 좋은 방법이 아니다. JUnit 테스트를 사용하자.
```java
package hello.core.order;

import hello.core.member.Gradle;
import hello.core.member.Member;
import hello.core.member.MemberService;
import hello.core.member.MemberServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class OrderServiceTest {

    MemberService memberService = new MemberServiceImpl();
    OrderService orderService = new OrderServiceImpl();

    @Test
    void createOrder(){
        Long memberId = 1L;
        Member member = new Member(memberId, "memberA", Gradle.VIP);
        memberService.join(member);

        Order order = orderService.createOrder(memberId, "item!", 10000);
        Assertions.assertThat(order.getDiscountPrice()).isEqualTo(1000);
    }
}
```