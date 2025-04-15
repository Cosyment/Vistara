package com.vistara.aestheticwalls.ui.screens.feedback

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 评分与反馈页面的ViewModel
 */
@HiltViewModel
class FeedbackViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "FeedbackViewModel"
        private const val APP_PACKAGE_NAME = "com.vistara.aestheticwalls"
        private const val FEEDBACK_EMAIL = "support@vistara.com"
    }

    // 反馈内容
    private val _feedbackText = MutableStateFlow("")
    val feedbackText: StateFlow<String> = _feedbackText.asStateFlow()

    // 联系方式
    private val _contactInfo = MutableStateFlow("")
    val contactInfo: StateFlow<String> = _contactInfo.asStateFlow()

    // 提交状态
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    // 提交结果
    private val _submitResult = MutableStateFlow<SubmitResult?>(null)
    val submitResult: StateFlow<SubmitResult?> = _submitResult.asStateFlow()

    /**
     * 更新反馈内容
     */
    fun updateFeedbackText(text: String) {
        _feedbackText.value = text
    }

    /**
     * 更新联系方式
     */
    fun updateContactInfo(info: String) {
        _contactInfo.value = info
    }

    /**
     * 提交反馈
     */
    fun submitFeedback() {
        if (_feedbackText.value.isBlank()) {
            _submitResult.value = SubmitResult.Error("请输入反馈内容")
            return
        }

        viewModelScope.launch {
            try {
                _isSubmitting.value = true
                
                // 模拟网络请求
                // 在实际应用中，这里应该调用API发送反馈
                Log.d(TAG, "Submitting feedback: ${_feedbackText.value}, contact: ${_contactInfo.value}")
                
                // 延迟1秒模拟网络请求
                kotlinx.coroutines.delay(1000)
                
                _submitResult.value = SubmitResult.Success("反馈提交成功，感谢您的宝贵意见！")
                // 清空输入
                _feedbackText.value = ""
                _contactInfo.value = ""
            } catch (e: Exception) {
                Log.e(TAG, "Error submitting feedback: ${e.message}")
                _submitResult.value = SubmitResult.Error("提交失败，请稍后再试")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /**
     * 清除提交结果
     */
    fun clearSubmitResult() {
        _submitResult.value = null
    }

    /**
     * 打开应用商店评分页面
     */
    fun openAppRating(): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$APP_PACKAGE_NAME")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                // 如果没有安装应用商店，则打开浏览器访问Google Play
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=$APP_PACKAGE_NAME")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error opening app rating: ${e.message}")
                false
            }
        }
    }

    /**
     * 发送邮件反馈
     */
    fun sendEmailFeedback(): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$FEEDBACK_EMAIL")
                putExtra(Intent.EXTRA_SUBJECT, "Vistara壁纸应用反馈")
                putExtra(Intent.EXTRA_TEXT, "我想反馈以下问题：\n\n")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error sending email feedback: ${e.message}")
            false
        }
    }
}

/**
 * 提交结果
 */
sealed class SubmitResult {
    data class Success(val message: String) : SubmitResult()
    data class Error(val message: String) : SubmitResult()
}
