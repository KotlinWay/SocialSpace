package info.javaway.sc.shared.presentation.screens.spaces

import com.arkivanov.decompose.ComponentContext
import info.javaway.sc.shared.data.local.SpaceManager
import info.javaway.sc.shared.domain.models.Space
import info.javaway.sc.shared.domain.models.SpaceType
import info.javaway.sc.shared.domain.repository.SpaceRepository
import info.javaway.sc.shared.presentation.core.BaseComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface SpaceSelectionComponent {
    val state: StateFlow<SpaceSelectionUiState>
    val effect: StateFlow<SpaceSelectionEffect?>

    fun refresh()
    fun joinSpace(space: Space, inviteCode: String? = null)
    fun createSpace(
        name: String,
        slug: String,
        description: String?,
        type: SpaceType,
        inviteCode: String?
    )
    fun consumeEffect()
}

class DefaultSpaceSelectionComponent(
    componentContext: ComponentContext,
    private val spaceRepository: SpaceRepository,
    private val spaceManager: SpaceManager
) : BaseComponent(componentContext), SpaceSelectionComponent {

    private val _state = MutableStateFlow(SpaceSelectionUiState())
    override val state: StateFlow<SpaceSelectionUiState> = _state.asStateFlow()

    private val _effect = MutableStateFlow<SpaceSelectionEffect?>(null)
    override val effect: StateFlow<SpaceSelectionEffect?> = _effect.asStateFlow()

    init {
        loadSpaces()
    }

    override fun refresh() {
        loadSpaces()
    }

    override fun joinSpace(space: Space, inviteCode: String?) {
        componentScope.launch {
            _state.value = _state.value.copy(isJoining = true, error = null)
            spaceRepository.joinSpace(space.id, inviteCode)
                .onSuccess { joined ->
                    spaceManager.selectSpace(joined.id)
                    _state.value = _state.value.copy(isJoining = false)
                    _effect.value = SpaceSelectionEffect.SpaceSelected
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isJoining = false,
                        error = error.message ?: "Не удалось вступить в пространство"
                    )
                }
        }
    }

    override fun createSpace(
        name: String,
        slug: String,
        description: String?,
        type: SpaceType,
        inviteCode: String?
    ) {
        componentScope.launch {
            _state.value = _state.value.copy(isCreating = true, error = null)
            spaceRepository.createSpace(
                name = name,
                slug = slug,
                description = description,
                type = type,
                inviteCode = inviteCode
            ).onSuccess { created ->
                spaceManager.selectSpace(created.id)
                _state.value = _state.value.copy(isCreating = false)
                _effect.value = SpaceSelectionEffect.SpaceSelected
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isCreating = false,
                    error = error.message ?: "Не удалось создать пространство"
                )
            }
        }
    }

    override fun consumeEffect() {
        _effect.value = null
    }

    private fun loadSpaces() {
        componentScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            spaceRepository.getSpaces(type = SpaceType.PUBLIC)
                .onSuccess { spaces ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        spaces = spaces
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Не удалось загрузить пространства"
                    )
                }
        }
    }
}

data class SpaceSelectionUiState(
    val isLoading: Boolean = false,
    val spaces: List<Space> = emptyList(),
    val error: String? = null,
    val isJoining: Boolean = false,
    val isCreating: Boolean = false
)

sealed interface SpaceSelectionEffect {
    data object SpaceSelected : SpaceSelectionEffect
}
