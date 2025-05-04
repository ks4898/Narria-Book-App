package edu.rit.ks4898.narria

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Book(
    val id: String = "",
    val bookId: String = "",
    val title: String = "",
    val author: String = "",
    val description: String = "",
    val coverUrl: String = "",
    val rating: Float = 0f,
    var readingStatus: String = "To-Read",
    var isFavorite: Boolean = false,
    @ServerTimestamp val timestamp: Date = Date()
)

data class GoogleBooksResponse(
    val items: List<BookItem>? = null,
    val totalItems: Int = 0
)

data class BookItem(
    val id: String = "",
    val volumeInfo: BookInfo = BookInfo()
)

data class BookInfo(
    val title: String = "",
    val authors: List<String>? = null,
    val description: String = "",
    val imageLinks: ImageLinks? = null,
    val averageRating: Float? = null
)

data class ImageLinks(
    val thumbnail: String = ""
)