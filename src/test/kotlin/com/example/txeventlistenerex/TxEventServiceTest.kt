package com.example.txeventlistenerex

import com.example.txeventlistenerex.entity.h2.School
import com.example.txeventlistenerex.entity.h2.SchoolRepository
import com.example.txeventlistenerex.entity.mysql.Student
import com.example.txeventlistenerex.entity.mysql.StudentRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
internal class TxEventServiceTest {
    @Autowired private lateinit var txEventService: TxEventService
    @Autowired private lateinit var schoolRepository: SchoolRepository
    @Autowired private lateinit var studentRepository: StudentRepository


    /**
     * chainedTransactionManager 를 사용하는 서비스 메서드에 @TransactionalEventListener 가 붙어 있으면,
     * chained 된 트랜잭션 중 첫번째 커밋에 synchronize 되어 동작한다.
     * 따라서, 첫번쨰 커밋의 afterCompletion() 에서 예외가 터지면, 첫번쨰 트랜잭션의 커밋은 정상적으로 동작하고, 두번쨰 트랜잭션의 커밋은 커밋되지 못한다.
     *
     * 참고 : chainedTransactionManager 의 동작 순서는 t1 open -> t2 open -> t2 commit -> t1 commit 의 스택 구조이다.
     */
    @Test
    fun `chainedTransactionManager_에서_첫번째_트랜잭션_후_@TransactionalEventListener_의_얘외는_두번째_트랜잭션_커밋에_영향을_미친다`() {
        try {
            txEventService.publish("MIT", "Tomas")
        } catch (e: Exception) {
            println(e.cause)
        }

        val schoolRes: School? = schoolRepository.findByName("MIT")
        val studentRes: Student? = studentRepository.findByName("Tomas")

        assertEquals("Tomas", studentRes!!.name) // 첫번쨰 커밋은 정상적으로 되었다.
        assertNull(schoolRes) // 원래는 정상적으로 들어왔어야 하나, @TransactionalEventListener 의 예외 때문에 커밋되지 못했다.
    }
}