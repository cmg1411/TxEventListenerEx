package com.example.txeventlistenerex.entity.mysql

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity(name = "student")
class Student(
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    var id: Int? = null,
    @Column
    var name: String
)

@Repository
interface StudentRepository : JpaRepository<Student, Int> {
    fun findByName(name: String): Student?
}