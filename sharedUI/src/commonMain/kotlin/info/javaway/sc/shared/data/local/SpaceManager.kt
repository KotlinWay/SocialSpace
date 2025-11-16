package info.javaway.sc.shared.data.local

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Менеджер выбранного пространства на клиенте.
 */
class SpaceManager(private val settings: Settings) {

    companion object {
        private const val KEY_CURRENT_SPACE_ID = "current_space_id"
    }

    private val _currentSpaceId = MutableStateFlow(settings.getLongOrNull(KEY_CURRENT_SPACE_ID))
    val currentSpaceIdFlow: StateFlow<Long?> = _currentSpaceId.asStateFlow()

    fun getCurrentSpaceId(): Long? = _currentSpaceId.value

    fun hasSelectedSpace(): Boolean = _currentSpaceId.value != null

    fun selectSpace(spaceId: Long) {
        _currentSpaceId.value = spaceId
        settings[KEY_CURRENT_SPACE_ID] = spaceId
    }

    fun clearSpace() {
        _currentSpaceId.value = null
        settings.remove(KEY_CURRENT_SPACE_ID)
    }
}
