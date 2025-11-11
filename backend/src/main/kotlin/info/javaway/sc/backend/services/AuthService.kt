package info.javaway.sc.backend.services

import info.javaway.sc.api.models.*
import info.javaway.sc.backend.repository.UserRepository
import info.javaway.sc.backend.utils.JwtConfig
import info.javaway.sc.backend.utils.PasswordHasher

/**
 * Сервис аутентификации и авторизации
 */
class AuthService(
    private val userRepository: UserRepository = UserRepository()
) {

    /**
     * Регистрация нового пользователя
     */
    fun register(request: RegisterRequest): AuthResult {
        // Валидация входных данных
        val phoneValidation = ValidationService.validatePhone(request.phone)
        if (!phoneValidation.isValid) {
            return AuthResult.Error(phoneValidation.errorMessage!!)
        }

        val emailValidation = ValidationService.validateEmail(request.email)
        if (!emailValidation.isValid) {
            return AuthResult.Error(emailValidation.errorMessage!!)
        }

        val nameValidation = ValidationService.validateName(request.name)
        if (!nameValidation.isValid) {
            return AuthResult.Error(nameValidation.errorMessage!!)
        }

        val passwordValidation = ValidationService.validatePassword(request.password)
        if (!passwordValidation.isValid) {
            return AuthResult.Error(passwordValidation.errorMessage!!)
        }

        // Нормализация телефона
        val normalizedPhone = ValidationService.normalizePhone(request.phone)

        // Проверка на существование пользователя
        if (userRepository.existsByPhone(normalizedPhone)) {
            return AuthResult.Error("Пользователь с таким номером телефона уже существует")
        }
        val email = request.email
        if (email != null && userRepository.existsByEmail(email)) {
            return AuthResult.Error("Пользователь с таким email уже существует")
        }

        // Хеширование пароля
        val passwordHash = PasswordHasher.hash(request.password)

        // Создание пользователя
        val user = userRepository.createUser(
            phone = normalizedPhone,
            email = request.email,
            name = request.name,
            passwordHash = passwordHash
        ) ?: return AuthResult.Error("Ошибка при создании пользователя")

        // Генерация JWT токена
        val token = JwtConfig.makeToken(user.id)

        return AuthResult.Success(
            AuthResponse(
                token = token,
                user = user
            )
        )
    }

    /**
     * Вход пользователя
     */
    fun login(request: LoginRequest): AuthResult {
        // Валидация входных данных
        val phoneValidation = ValidationService.validatePhone(request.phone)
        if (!phoneValidation.isValid) {
            return AuthResult.Error(phoneValidation.errorMessage!!)
        }

        val passwordValidation = ValidationService.validatePassword(request.password)
        if (!passwordValidation.isValid) {
            return AuthResult.Error(passwordValidation.errorMessage!!)
        }

        // Нормализация телефона
        val normalizedPhone = ValidationService.normalizePhone(request.phone)

        // Поиск пользователя
        val user = userRepository.findByPhone(normalizedPhone)
            ?: return AuthResult.Error("Неверный номер телефона или пароль")

        // Получение хеша пароля
        val passwordHash = userRepository.getPasswordHash(normalizedPhone)
            ?: return AuthResult.Error("Неверный номер телефона или пароль")

        // Проверка пароля
        if (!PasswordHasher.verify(request.password, passwordHash)) {
            return AuthResult.Error("Неверный номер телефона или пароль")
        }

        // Генерация JWT токена
        val token = JwtConfig.makeToken(user.id)

        return AuthResult.Success(
            AuthResponse(
                token = token,
                user = user
            )
        )
    }

    /**
     * Получение пользователя по ID
     */
    fun getUserById(userId: Long): User? {
        return userRepository.findById(userId)
    }

    /**
     * Обновление профиля пользователя
     */
    fun updateProfile(userId: Long, request: UpdateProfileRequest): UpdateResult {
        // Валидация
        val name = request.name
        val email = request.email
        if (name != null) {
            val nameValidation = ValidationService.validateName(name)
            if (!nameValidation.isValid) {
                return UpdateResult.Error(nameValidation.errorMessage!!)
            }
        }

        if (email != null) {
            val emailValidation = ValidationService.validateEmail(email)
            if (!emailValidation.isValid) {
                return UpdateResult.Error(emailValidation.errorMessage!!)
            }

            // Проверка на существование email у другого пользователя
            val existingUser = userRepository.findByEmail(email)
            if (existingUser != null && existingUser.id != userId) {
                return UpdateResult.Error("Пользователь с таким email уже существует")
            }
        }

        if (request.bio != null) {
            val bioValidation = ValidationService.validateBio(request.bio)
            if (!bioValidation.isValid) {
                return UpdateResult.Error(bioValidation.errorMessage!!)
            }
        }

        // Обновление профиля
        val updatedUser = userRepository.updateUser(
            userId = userId,
            name = request.name,
            email = request.email,
            bio = request.bio
        ) ?: return UpdateResult.Error("Пользователь не найден")

        return UpdateResult.Success(updatedUser)
    }
}

/**
 * Результат аутентификации
 */
sealed class AuthResult {
    data class Success(val data: AuthResponse) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * Результат обновления
 */
sealed class UpdateResult {
    data class Success(val user: User) : UpdateResult()
    data class Error(val message: String) : UpdateResult()
}
