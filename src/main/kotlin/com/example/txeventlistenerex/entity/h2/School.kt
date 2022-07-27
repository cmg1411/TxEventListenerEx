package com.example.txeventlistenerex.entity.h2

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity
class School(
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    var id: Long? = null,
    @Column
    var name: String
)

@Repository
interface SchoolRepository : JpaRepository<School, Long> {
    fun findByName(name: String): School?
}