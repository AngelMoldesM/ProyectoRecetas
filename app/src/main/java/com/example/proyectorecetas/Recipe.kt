import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Recipe(
    @DocumentId var id: String = "",
    var userId: String = "",
    var title: String = "",
    var description: String = "",
    var ingredients: String = "",
    var time: String = "",
    var category: String = "",
    var imageUrl: String = "",
    var timestamp: Timestamp = Timestamp.now()
)