package akinai.minikgurme.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import akinai.minikgurme.data.repository.FeedingRepository

/** Android'e özel ViewModel fabrikası (iOS'ta ViewModel doğrudan oluşturulur). */
class FeedingViewModelFactory(
    private val repository: FeedingRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeedingViewModel::class.java)) {
            return FeedingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
