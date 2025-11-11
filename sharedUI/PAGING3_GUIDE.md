# Paging 3 Architecture Guide

## 📚 Обзор

Проект использует **Paging 3** библиотеку от Android Jetpack для эффективной пагинации данных. Архитектура спроектирована с расчётом на переиспользование для разных сущностей (Products, Services, Favorites, и т.д.).

## 🏗 Архитектура

### 1. BasePagingSource<T>

Базовый класс для всех PagingSource в приложении. Инкапсулирует общую логику пагинации.

**Расположение:** `sharedUI/src/commonMain/kotlin/info/javaway/sc/shared/data/paging/BasePagingSource.kt`

**Ключевые методы:**
- `loadPage(page: Int, pageSize: Int): Result<List<T>>` - абстрактный метод для загрузки данных
- `load(params: LoadParams<Int>): LoadResult<Int, T>` - стандартная логика обработки пагинации
- `getRefreshKey(state: PagingState<Int, T>): Int?` - логика refresh для текущей позиции

**Преимущества:**
- ✅ Единообразная обработка ошибок
- ✅ Логирование через Napier
- ✅ Автоматическое определение nextKey/prevKey
- ✅ DRY принцип - нет дублирования логики

### 2. ProductPagingSource

Конкретная реализация для товаров. Наследует BasePagingSource<Product>.

**Расположение:** `sharedUI/src/commonMain/kotlin/info/javaway/sc/shared/data/paging/ProductPagingSource.kt`

**Фильтры:**
```kotlin
data class ProductFilters(
    val categoryId: Long? = null,
    val status: ProductStatus? = null,
    val condition: ProductCondition? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val search: String? = null
)
```

**Пример использования:**
```kotlin
class ProductPagingSource(
    private val apiClient: ApiClient,
    private val filters: ProductFilters = ProductFilters()
) : BasePagingSource<Product>() {

    override suspend fun loadPage(page: Int, pageSize: Int): Result<List<Product>> {
        return apiClient.getProducts(/* параметры */)
            .fold(
                onSuccess = { response ->
                    val domainProducts = response.products.map { it.toDomain() }
                    Result.success(domainProducts)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
    }
}
```

### 3. Repository Layer

**ProductRepository** содержит метод `getProductsPaged()`, который возвращает `Flow<PagingData<Product>>`:

```kotlin
fun getProductsPaged(
    categoryId: Long? = null,
    status: ProductStatus? = null,
    condition: ProductCondition? = null,
    minPrice: Double? = null,
    maxPrice: Double? = null,
    search: String? = null
): Flow<PagingData<Product>>
```

**Реализация в ProductRepositoryImpl:**
```kotlin
override fun getProductsPaged(...): Flow<PagingData<Product>> {
    val filters = ProductFilters(...)

    return Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false,
            initialLoadSize = 20,
            prefetchDistance = 5,
            maxSize = PagingConfig.MAX_SIZE_UNBOUNDED
        ),
        pagingSourceFactory = {
            ProductPagingSource(apiClient, filters)
        }
    ).flow
}
```

**Конфигурация PagingConfig:**
- `pageSize = 20` - размер страницы (количество элементов)
- `enablePlaceholders = false` - не показывать placeholder'ы для незагруженных данных
- `initialLoadSize = 20` - размер первой загрузки
- `prefetchDistance = 5` - загружать следующую страницу за 5 элементов до конца
- `maxSize = UNBOUNDED` - без ограничения размера кэша

### 4. ViewModel Layer

**ProductListViewModel** работает с `Flow<PagingData<Product>>`:

```kotlin
class ProductListViewModel(
    private val productRepository: ProductRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Фильтры
    private val _filters = MutableStateFlow(ProductFiltersState())
    val filters: StateFlow<ProductFiltersState> = _filters.asStateFlow()

    // PagingData Flow с автоматическим refresh при изменении фильтров
    val productsFlow: Flow<PagingData<Product>> = _filters
        .flatMapLatest { filters ->
            productRepository.getProductsPaged(
                categoryId = filters.categoryId,
                status = filters.status,
                // ...
            )
        }
        .cachedIn(viewModelScope) // ✅ ВАЖНО: кэширование в scope

    fun updateFilters(...) {
        _filters.value = ProductFiltersState(...)
    }
}
```

**Ключевые моменты:**
- ✅ `flatMapLatest` - автоматический пересоздание PagingSource при изменении фильтров
- ✅ `cachedIn(viewModelScope)` - кэширование данных, переживает configuration changes
- ✅ Управление фильтрами через StateFlow

### 5. UI Layer (Compose)

**ProductListScreen** использует `collectAsLazyPagingItems()`:

```kotlin
@Composable
fun ProductListScreen(
    viewModel: ProductListViewModel = koinInject(),
    onProductClick: (Long) -> Unit = {}
) {
    // Collect PagingData as LazyPagingItems
    val lazyPagingItems: LazyPagingItems<Product> =
        viewModel.productsFlow.collectAsLazyPagingItems()

    LazyColumn {
        items(
            count = lazyPagingItems.itemCount,
            key = { index -> lazyPagingItems[index]?.id ?: index }
        ) { index ->
            val product = lazyPagingItems[index]
            if (product != null) {
                ProductCard(product = product, onClick = { onProductClick(product.id) })
            }
        }
    }
}
```

**Обработка состояний загрузки:**
```kotlin
// Первая загрузка
if (lazyPagingItems.loadState.refresh is LoadState.Loading) {
    CircularProgressIndicator()
}

// Ошибка первой загрузки
if (lazyPagingItems.loadState.refresh is LoadState.Error) {
    val error = lazyPagingItems.loadState.refresh as LoadState.Error
    ErrorState(message = error.error.message, onRetry = { lazyPagingItems.retry() })
}

// Загрузка следующей страницы
if (lazyPagingItems.loadState.append is LoadState.Loading) {
    CircularProgressIndicator()
}

// Пустой список
if (lazyPagingItems.itemCount == 0) {
    EmptyState()
}
```

## 🔧 Как добавить пагинацию для новой сущности (Service)

### Шаг 1: Создать ServicePagingSource

```kotlin
// sharedUI/src/commonMain/kotlin/info/javaway/sc/shared/data/paging/ServicePagingSource.kt

data class ServiceFilters(
    val categoryId: Long? = null,
    val status: ServiceStatus? = null,
    val search: String? = null
)

class ServicePagingSource(
    private val apiClient: ApiClient,
    private val filters: ServiceFilters = ServiceFilters()
) : BasePagingSource<Service>() {

    override suspend fun loadPage(page: Int, pageSize: Int): Result<List<Service>> {
        return apiClient.getServices(
            categoryId = filters.categoryId,
            status = filters.status?.toApi(),
            search = filters.search,
            page = page,
            pageSize = pageSize
        ).fold(
            onSuccess = { response ->
                val domainServices = response.services.map { it.toDomain() }
                Result.success(domainServices)
            },
            onFailure = { exception ->
                Result.failure(exception)
            }
        )
    }
}
```

### Шаг 2: Добавить метод в ServiceRepository

```kotlin
interface ServiceRepository {
    fun getServicesPaged(
        categoryId: Long? = null,
        status: ServiceStatus? = null,
        search: String? = null
    ): Flow<PagingData<Service>>
}

class ServiceRepositoryImpl(
    private val apiClient: ApiClient
) : ServiceRepository {

    override fun getServicesPaged(...): Flow<PagingData<Service>> {
        val filters = ServiceFilters(...)

        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20,
                prefetchDistance = 5
            ),
            pagingSourceFactory = { ServicePagingSource(apiClient, filters) }
        ).flow
    }
}
```

### Шаг 3: Создать ServiceListViewModel

```kotlin
class ServiceListViewModel(
    private val serviceRepository: ServiceRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _filters = MutableStateFlow(ServiceFiltersState())
    val filters: StateFlow<ServiceFiltersState> = _filters.asStateFlow()

    val servicesFlow: Flow<PagingData<Service>> = _filters
        .flatMapLatest { filters ->
            serviceRepository.getServicesPaged(
                categoryId = filters.categoryId,
                status = filters.status,
                search = filters.search
            )
        }
        .cachedIn(viewModelScope)
}
```

### Шаг 4: Создать ServiceListScreen

```kotlin
@Composable
fun ServiceListScreen(
    viewModel: ServiceListViewModel = koinInject(),
    onServiceClick: (Long) -> Unit = {}
) {
    val lazyPagingItems: LazyPagingItems<Service> =
        viewModel.servicesFlow.collectAsLazyPagingItems()

    LazyColumn {
        items(
            count = lazyPagingItems.itemCount,
            key = { index -> lazyPagingItems[index]?.id ?: index }
        ) { index ->
            val service = lazyPagingItems[index]
            if (service != null) {
                ServiceCard(service = service, onClick = { onServiceClick(service.id) })
            }
        }
    }
}
```

## ✅ Преимущества Paging 3

1. **Автоматическая пагинация** - библиотека управляет загрузкой страниц
2. **Кэширование** - данные сохраняются при configuration changes
3. **Обработка ошибок** - встроенная поддержка retry логики
4. **Производительность** - эффективное использование памяти
5. **Расширяемость** - легко добавлять новые сущности через BasePagingSource
6. **Состояния загрузки** - LoadState (Loading, Error, NotLoading)
7. **Pull-to-refresh** - встроенная поддержка через `lazyPagingItems.refresh()`

## 📦 Зависимости

```toml
# gradle/libs.versions.toml
[versions]
paging = "3.3.6"

[libraries]
paging-common = { module = "androidx.paging:paging-common", version.ref = "paging" }
paging-compose = { module = "androidx.paging:paging-compose", version.ref = "paging" }
```

```kotlin
// sharedUI/build.gradle.kts
commonMain.dependencies {
    implementation(libs.paging.common)
}

androidMain.dependencies {
    implementation(libs.paging.compose)
}
```

## 🔍 Отладка

Включите логирование в BasePagingSource через Napier:
- `"Loading page X with size Y"` - начало загрузки страницы
- `"Page X loaded successfully: N items"` - успешная загрузка
- `"Error loading page X"` - ошибка загрузки

## 📚 Дополнительная информация

- [Paging 3 Overview](https://developer.android.com/topic/libraries/architecture/paging/v3-overview)
- [Paging 3 with Compose](https://developer.android.com/jetpack/androidx/releases/paging)
