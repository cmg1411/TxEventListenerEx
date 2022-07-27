package com.example.txeventlistenerex

import com.example.txeventlistenerex.entity.h2.School
import com.example.txeventlistenerex.entity.h2.SchoolRepository
import com.example.txeventlistenerex.entity.mysql.Student
import com.example.txeventlistenerex.entity.mysql.StudentRepository
import com.example.txeventlistenerex.event.TransactionEvent
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger { }

@RequestMapping("/call-event")
@RestController
class EventController(private val eventService: TxEventService) {
    @GetMapping("/transactional")
    fun callTransactionalEvent() {
        eventService.publish("MIT", "Tomas")
    }
}


@Service
class TxEventService(
    private val eventPublisher: ApplicationEventPublisher,
    private val schoolRepository: SchoolRepository,
    private val studentRepository: StudentRepository,
) {
    @Transactional(transactionManager = "chainedTransactionManager")
    fun publish(school: String, student: String) {
        logger.info { "Transactional Caller Method" }

        schoolRepository.save(School(name = school))
        studentRepository.save(Student(name = student))

        eventPublisher.publishEvent(TransactionEvent("payload"))
    }
}
