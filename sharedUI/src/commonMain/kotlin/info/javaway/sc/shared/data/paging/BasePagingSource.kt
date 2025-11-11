package info.javaway.sc.shared.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.aakira.napier.Napier

/**
 * Базовый PagingSource для переиспользования
 * Инкапсулирует общую логику пагинации для всех сущностей
 *
 * @param T Тип элемента (Product, Service, и т.д.)
 */
abstract class BasePagingSource<T : Any> : PagingSource<Int, T>() {

    /**
     * Загрузка данных для конкретной страницы
     * Реализуется в дочерних классах
     *
     * @param page Номер страницы (начиная с 1)
     * @param pageSize Размер страницы
     * @return Result с списком элементов или ошибкой
     */
    protected abstract suspend fun loadPage(page: Int, pageSize: Int): Result<List<T>>

    /**
     * Стандартная логика загрузки страницы
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        return try {
            // Текущая страница (по умолчанию 1 для первой загрузки)
            val currentPage = params.key ?: 1
            val pageSize = params.loadSize

            Napier.d(tag = "BasePagingSource") {
                "Loading page $currentPage with size $pageSize"
            }

            // Загружаем данные через абстрактный метод
            val result = loadPage(currentPage, pageSize)

            result.fold(
                onSuccess = { items ->
                    Napier.d(tag = "BasePagingSource") {
                        "Page $currentPage loaded successfully: ${items.size} items"
                    }

                    // Определяем есть ли следующая страница
                    val nextKey = if (items.isEmpty() || items.size < pageSize) {
                        null // Больше данных нет
                    } else {
                        currentPage + 1
                    }

                    // Определяем есть ли предыдущая страница
                    val prevKey = if (currentPage == 1) {
                        null
                    } else {
                        currentPage - 1
                    }

                    LoadResult.Page(
                        data = items,
                        prevKey = prevKey,
                        nextKey = nextKey
                    )
                },
                onFailure = { exception ->
                    Napier.e(exception, tag = "BasePagingSource") {
                        "Error loading page $currentPage"
                    }
                    LoadResult.Error(exception)
                }
            )
        } catch (e: Exception) {
            Napier.e(e, tag = "BasePagingSource") {
                "Unexpected error loading page"
            }
            LoadResult.Error(e)
        }
    }

    /**
     * Ключ для обновления данных (refresh)
     * Вычисляет страницу на основе текущей позиции прокрутки
     */
    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        // Находим страницу ближайшую к текущей позиции якоря
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
