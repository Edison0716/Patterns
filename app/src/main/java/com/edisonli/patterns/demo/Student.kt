package com.edisonli.patterns.demo

import com.edisonli.patterns.anno.BuilderAnno

@BuilderAnno
class Student private constructor(
    age: Int,
    name: String,
    teacher: Teacher
)

class Teacher()

fun main() {
    StudentBuilderCreator
        .Builder()
        .age(1)
        .name("edison")
        .teacher(Teacher())
        .build()

}



