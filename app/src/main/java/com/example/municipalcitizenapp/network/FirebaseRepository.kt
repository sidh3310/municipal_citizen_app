package com.example.municipalcitizenapp.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.municipalcitizenapp.models.Complaint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FirebaseRepository {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage get() = FirebaseStorage.getInstance()

    // --- AUTHENTICATION ---

    fun registerUser(
        name: String,
        email: String,
        phone: String,
        password: String,
        role: String = "CITIZEN",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""
                val userData = mapOf(
                    "id" to uid,
                    "full_name" to name,
                    "email" to email,
                    "phone" to phone,
                    "role" to role.uppercase()
                )
                db.collection("users").document(uid)
                    .set(userData)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onError(e.message ?: "Failed to save user details") }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Registration failed")
            }
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: (uid: String, role: String, fullName: String) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""
                db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val role = document.getString("role") ?: "CITIZEN"
                            val fullName = document.getString("full_name") ?: email
                            onSuccess(uid, role, fullName)
                        } else {
                            onSuccess(uid, "CITIZEN", email)
                        }
                    }
                    .addOnFailureListener {
                        onSuccess(uid, "CITIZEN", email)
                    }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Invalid email or password")
            }
    }

    // --- COMPLAINTS ---

    fun submitComplaint(
        category: String,
        title: String,
        description: String,
        latitude: String,
        longitude: String,
        imageFilePath: String?,
        imageUri: Uri?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser
        val userId = user?.uid ?: ""
        val userEmail = user?.email ?: ""
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val createdAt = dateFormat.format(Date())

        val base64ImageFallback = encodeImageToBase64(imageFilePath)

        val saveComplaintToFirestore = { imageUrl: String? ->
            val finalImage = if (!imageUrl.isNullOrEmpty()) imageUrl else (base64ImageFallback ?: "")
            val complaintData = mapOf(
                "category" to category,
                "title" to title,
                "description" to description,
                "latitude" to latitude,
                "longitude" to longitude,
                "image" to finalImage,
                "status" to "SUBMITTED",
                "priority" to "MEDIUM",
                "resolution_notes" to "",
                "created_at" to createdAt,
                "userId" to userId,
                "userEmail" to userEmail,
                "upvotes" to 0,
                "upvotedBy" to emptyList<String>()
            )

            db.collection("complaints")
                .add(complaintData)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onError(e.message ?: "Failed to submit complaint") }
        }

        val fileUriToUpload = when {
            imageUri != null -> imageUri
            !imageFilePath.isNullOrEmpty() -> Uri.fromFile(File(imageFilePath))
            else -> null
        }

        if (fileUriToUpload != null) {
            val imageRef = storage.reference.child("complaints/${UUID.randomUUID()}.jpg")
            imageRef.putFile(fileUriToUpload)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    imageRef.downloadUrl
                }
                .addOnSuccessListener { uri ->
                    saveComplaintToFirestore(uri.toString())
                }
                .addOnFailureListener {
                    saveComplaintToFirestore(base64ImageFallback ?: "")
                }
        } else {
            saveComplaintToFirestore("")
        }
    }

    private fun encodeImageToBase64(filePath: String?): String? {
        if (filePath.isNullOrEmpty()) return null
        return try {
            val file = File(filePath)
            if (!file.exists()) return null
            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return null
            
            val maxDimension = 640
            val ratio = Math.min(
                maxDimension.toFloat() / bitmap.width,
                maxDimension.toFloat() / bitmap.height
            )
            val width = Math.round(ratio * bitmap.width)
            val height = Math.round(ratio * bitmap.height)
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)

            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
            val imageBytes = outputStream.toByteArray()
            "data:image/jpeg;base64," + Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    fun getUserComplaints(
        userId: String,
        onSuccess: (List<Complaint>) -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUid = auth.currentUser?.uid ?: ""
        val currentEmail = auth.currentUser?.email ?: ""

        db.collection("complaints").get()
            .addOnSuccessListener { result ->
                val list = mutableListOf<Complaint>()
                for (doc in result) {
                    val complaint = doc.toObject(Complaint::class.java)
                    complaint.docId = doc.id

                    // Match user complaints or fallback to returning all
                    if (userId.isEmpty() ||
                        complaint.userId == currentUid ||
                        complaint.userId == userId ||
                        (currentEmail.isNotEmpty() && complaint.userEmail == currentEmail)
                    ) {
                        list.add(complaint)
                    }
                }

                if (list.isEmpty()) {
                    // Fallback to all complaints if specific match is empty
                    val allList = mutableListOf<Complaint>()
                    for (doc in result) {
                        val c = doc.toObject(Complaint::class.java)
                        c.docId = doc.id
                        allList.add(c)
                    }
                    onSuccess(allList)
                } else {
                    onSuccess(list)
                }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to fetch complaints")
            }
    }

    fun getAllComplaints(
        onSuccess: (List<Complaint>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("complaints")
            .get()
            .addOnSuccessListener { result ->
                val list = mutableListOf<Complaint>()
                for (doc in result) {
                    val complaint = doc.toObject(Complaint::class.java)
                    complaint.docId = doc.id
                    list.add(complaint)
                }
                onSuccess(list)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to fetch complaints")
            }
    }

    fun upvoteComplaint(
        docId: String,
        userId: String,
        onSuccess: (newVotes: Int) -> Unit,
        onError: (String) -> Unit
    ) {
        if (docId.isEmpty()) {
            onError("Invalid document ID")
            return
        }

        val docRef = db.collection("complaints").document(docId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val currentVotes = snapshot.getLong("upvotes")?.toInt() ?: 0
            val upvotedBy = (snapshot.get("upvotedBy") as? List<*>)?.mapNotNull { it.toString() } ?: emptyList()

            if (!upvotedBy.contains(userId)) {
                val newVotes = currentVotes + 1
                val newUpvotedBy = upvotedBy + userId
                transaction.update(docRef, "upvotes", newVotes)
                transaction.update(docRef, "upvotedBy", newUpvotedBy)
                newVotes
            } else {
                currentVotes
            }
        }.addOnSuccessListener { newVotes ->
            onSuccess(newVotes)
        }.addOnFailureListener { e ->
            onError(e.message ?: "Failed to upvote")
        }
    }

    fun updateComplaintStatus(
        docId: String,
        status: String,
        priority: String,
        resolutionNotes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val updateData = mapOf(
            "status" to status,
            "priority" to priority,
            "resolution_notes" to resolutionNotes
        )

        db.collection("complaints").document(docId)
            .update(updateData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to update complaint") }
    }

    fun updateComplaintStatusWithPhoto(
        docId: String,
        status: String,
        priority: String,
        resolutionNotes: String,
        resolvedImageFilePath: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val base64ResolvedImage = encodeImageToBase64(resolvedImageFilePath)

        val updateData = mutableMapOf<String, Any>(
            "status" to status,
            "priority" to priority,
            "resolution_notes" to resolutionNotes
        )
        if (!base64ResolvedImage.isNullOrEmpty()) {
            updateData["resolved_image"] = base64ResolvedImage
        }

        db.collection("complaints").document(docId)
            .update(updateData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to update complaint") }
    }

    // --- ANNOUNCEMENTS ---

    fun getAnnouncements(
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("announcements")
            .get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { it.data }
                onSuccess(list)
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to fetch announcements")
            }
    }

    fun postAnnouncement(
        title: String,
        message: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val data = mapOf(
            "title" to title,
            "message" to message,
            "created_at" to SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("announcements")
            .add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to post announcement") }
    }

    // --- DEPARTMENTS ---

    fun getDepartments(
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("departments")
            .get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { it.data }
                onSuccess(list)
            }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to fetch departments") }
    }

    fun addDepartment(
        name: String,
        description: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val data = mapOf(
            "name" to name,
            "description" to description
        )

        db.collection("departments")
            .add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to add department") }
    }

    // --- USERS & ADMIN STATS ---

    fun getUsers(
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { it.data }
                onSuccess(list)
            }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to fetch users") }
    }

    fun getAdminStats(
        onSuccess: (totalUsers: Int, totalComplaints: Int) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("users").get().addOnSuccessListener { usersSnap ->
            val userCount = usersSnap.size()
            db.collection("complaints").get().addOnSuccessListener { complaintsSnap ->
                val complaintCount = complaintsSnap.size()
                onSuccess(userCount, complaintCount)
            }.addOnFailureListener {
                onSuccess(userCount, 0)
            }
        }.addOnFailureListener { e ->
            onError(e.message ?: "Failed to fetch stats")
        }
    }

    // --- NOTIFICATIONS ---

    fun getNotifications(
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("notifications")
            .get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { it.data }
                onSuccess(list)
            }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to fetch notifications") }
    }
}
