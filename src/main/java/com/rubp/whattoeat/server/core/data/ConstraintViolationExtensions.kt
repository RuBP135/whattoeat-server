package com.rubp.whattoeat.server.core.data

import org.hibernate.exception.ConstraintViolationException
import org.hibernate.exception.ConstraintViolationException.ConstraintKind

private fun Throwable.causes(): Sequence<Throwable> =
    generateSequence(this) { it.cause }

fun Throwable.isUniqueViolation(): Boolean =
    causes().any {
        it is ConstraintViolationException && it.kind == ConstraintKind.UNIQUE
    }

fun Throwable.isUniqueViolation(constraintName: String): Boolean {
    return causes().any {
        it is ConstraintViolationException
                && it.kind == ConstraintKind.UNIQUE
                && it.constraintName?.substringAfterLast('.')?.equals(constraintName, ignoreCase = true) == true
    }
}
