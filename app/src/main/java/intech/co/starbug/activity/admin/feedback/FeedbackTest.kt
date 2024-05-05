package intech.co.starbug.activity.admin.feedback

import java.util.*

class FeedbackTest {
    private val feedbackList: MutableList<Feedback> = mutableListOf()

    // Thêm một phản hồi mới vào danh sách
    fun addFeedback(feedback: Feedback) {
        feedbackList.add(feedback)
        // Thêm mã để thông báo hoặc xử lý các công việc liên quan khi phản hồi được thêm vào danh sách
    }

    // Sửa thông tin của một phản hồi
    fun editFeedback(feedbackId: Int, newFeedback: Feedback) {
        val index = feedbackList.indexOfFirst { it.feedbackId == feedbackId }
        if (index != -1) {
            feedbackList[index] = newFeedback
            // Thêm mã để thông báo hoặc xử lý các công việc liên quan khi phản hồi được sửa
        } else {
            println("Feedback with ID $feedbackId not found.")
        }
    }

    // Xóa một phản hồi khỏi danh sách
    fun deleteFeedback(feedbackId: Int) {
        val removed = feedbackList.removeIf { it.feedbackId == feedbackId }
        if (!removed) {
            println("Feedback with ID $feedbackId not found.")
        } else {
            // Thêm mã để thông báo hoặc xử lý các công việc liên quan khi phản hồi được xóa
        }
    }

    // Lấy danh sách tất cả các phản hồi
    fun getAllFeedbacks(): List<Feedback> {
        return feedbackList
    }

    // Lấy một phản hồi dựa trên ID
    fun getFeedbackById(feedbackId: Int): Feedback? {
        return feedbackList.find { it.feedbackId == feedbackId }
    }

    // Thêm các phương thức khác nếu cần
}
