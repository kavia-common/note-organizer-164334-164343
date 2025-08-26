package org.example.app

import java.util.UUID

/**
 * PUBLIC_INTERFACE
 * Note is the domain model representing a user's note.
 */
data class Note(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var content: String = "",
    var category: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
)
