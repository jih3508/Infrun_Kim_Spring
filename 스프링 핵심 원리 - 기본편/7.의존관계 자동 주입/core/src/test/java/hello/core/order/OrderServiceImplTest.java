package hello.core.order;

import hello.core.discount.FixDiscountPolicy;
import hello.core.member.Gradle;
import hello.core.member.Member;
import hello.core.member.MemoryMemberRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceImplTest {

    @Test
    void createOrder(){
        MemoryMemberRepository memberRepository = new MemoryMemberRepository();
        memberRepository.save(new Member(1L, "name", Gradle.VIP));

        OrderService orderService = new OrderServiceImpl(memberRepository, new FixDiscountPolicy());
        orderService.createOrder(1L, "itemA", 10000);
    }

}