@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package studio.forface.viewstatestore.paging

import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import studio.forface.viewstatestore.ViewState
import studio.forface.viewstatestore.ViewStateStoreConfig

/**
 * A `ViewStateStore` that support Android's Paging
 *
 * Inherit from [LockedPagedViewStateStore] and implements [PagedViewStateStoreScope], so [setState] and [postState]
 * can be called without any additional scope.
 *
 * Use [setDataSource] for generate your data [V]
 * instead of [setData], like you would do a "classic" `ViewStateStore`,
 *
 *
 * @param pageSize the size of the page of the internal [pagedLiveData].
 * @see LivePagedListBuilder constructor.
 * Default is 25
 *
 * @param dropOnSame This [Boolean] defines whether a publishing should be dropped if the same [ViewState] is already
 * the last [state]
 * @see ViewStateStoreConfig.dropOnSame
 * Default value is inherited from [ViewStateStoreConfig.dropOnSame]
 *
 *
 * @author Davide Giuseppe Farella
 */
class PagedViewStateStore<V> internal constructor(
    dataSourceFactory: DataSource.Factory<Int, V>,
    pageSize: Int = DEFAULT_PAGE_SIZE,
    dropOnSame: Boolean = ViewStateStoreConfig.dropOnSame
) : LockedPagedViewStateStore<V>(
    LivePagedListBuilder(dataSourceFactory, pageSize).build(),
    pageSize,
    dropOnSame
), PagedViewStateStoreScope {

    /**
     * @return [LockedPagedViewStateStore] obtained by casting `this` instance.
     * Use this for forbid access to `set` and `post` functions without a [PagedViewStateStoreScope]
     */
    val lock: LockedPagedViewStateStore<V> get() = this

    internal companion object {
        /** The default value for [LockedPagedViewStateStore.pageSize] */
        const val DEFAULT_PAGE_SIZE = 25
    }
}
