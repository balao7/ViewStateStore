###### This library is created on an idea of *Fabio Collini* ( https://proandroiddev.com/unidirectional-data-flow-using-coroutines-f5a792bf34e5 ).

# ViewStateStore

[![Download](https://api.bintray.com/packages/4face/ViewStateStore/studio.forface.viewstatestore/images/download.svg)](https://bintray.com/4face/ViewStateStore/studio.forface.viewstatestore/_latestVersion)  ![MinSDK](https://img.shields.io/badge/MinSDK-14-f44336.svg)  [![star this repo](http://githubbadges.com/star.svg?user=4face-studi0&repo=ViewStateStore&style=flat&color=fff&background=4caf50)](https://github.com/4face-studi0/ViewStateStore)  [![fork this repo](http://githubbadges.com/fork.svg?user=4face-studi0&repo=ViewStateStore&style=flat&color=fff&background=4caf50)](https://github.com/4face-studi0/ViewStateStore/fork)


**ViewStateStore** wraps a *LiveData* for deliver **ViewState**s to the *UI*.

Supported **ViewState** types are;

* *Success* holds the real data
* *Error* holds and error ( which could be a custom class ) with its *Throwable* and an optiona *customMessageRes*.
* *Loading*
* *None* ( default initial value )



## Installation

#### ViewStateStore

`implementation( "studio.forface.viewstatestore:viewstatestore:last_version" )`

#### Paging extension

`implementation( "studio.forface.viewstatestore:viewstatestore-paging:last_version" )`

# Minimal usage



## Create

```kotlin
class CarsViewModel(val getCars: GetCars): ViewModel() {
    val cars = ViewStateStore<List<Car>>()

    init {
        cars.setLoading()
        viewModelScope.launch {
            runCatching { withContext(IO) { getCars() } }
                .onSuccess { cars::set(it) }
                .onFailure { cars.setError(it) }
        }
    }
}
```



## Get

#### `observe` ( with LifecycleOwner )

```kotlin
class CarsFragment: Fragment(), ViewStateFragment {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        // carsViewModel.cars.observeData { cars -> ... }
        carsViewModel.cars.observe {
            doOnData(::updateCars)
            doOnError(::showError)
            doOnLoading { isLoading -> progressBar.isVisible = isLoading }
        }
    }
}
```
#### `Iterator` ( with CoroutineScope )

```kotlin
class CarsFragment: Fragment(), ViewStateFragment {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            // for (viewState in carsViewModel.cars) { ... }
            // for (cars in carsViewModel.cars.data) { ... }
            for ((onData, onError, onLoadingChange) in carsViewModel.cars.composed) {
                onData?let(::updateCars)
                onError?.let(::showError)
                onLoadingChange?let { isLoading -> progressBar.isVisible = isLoading }
            }
        }
    }
}
```

#### `await` ( with CoroutineScope )

```kotlin
class CarsFragment: Fragment(), ViewStateFragment {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            val currentOrNextViewState = carsViewModel.cars.await()
            val currentOrNextData = carsViewModel.cars.awaitData()
            val onlyNextViewState = carsViewModel.cars.awaitNext()
            val onlyNextData = carsViewModel.cars.awaitNextData()
        }
    }
}
```

#### get ( nullable )

```kotlin
class CarsFragment: Fragment(), ViewStateFragment {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        val nullableCurrentViewState = carsViewModel.cars.state()
        val nullableCurrentData = carsViewModel.cars.data()
        try {
        	val currentViewState = carsViewModel.cars.unsafeState()
        	val currentData = carsViewModel.cars.unsafeData()
        } catch(e: KotlinNullPointerException) {
            ...
        }
    }
}
```



---



You can set also an `ErrorResolution`

```kotlin
// CarsViewModel
    init {
        loadCars()
    }

    private fun loadCars() {
        cars.setLoading()
        viewModelScope.launch {
            runCatching { withContext(IO) { getCars() } }
                .onSuccess(cars::setData)
                .onFailure { cars.setError(it, ::loadCars) }
        }
    }
}

// CarsFragment
    override fun onActivityCreated( savedInstanceState: Bundle? ) {
        carsViewModel.cars.observe {
            ...
            doOnError(::showError)
        }
    }

    fun showError(error: ViewState.Error) {
        Snackbar.make(
            coordinatorLayout,
            error.getMessage(requireContext()),
            Snackbar.LENGTH_SHORT
        ).apply {
            if (error.hasResolution())
                setAction("Retry") { error.resolve() } }
            show()
        }
    }
}
```



---



It's also possible to `lock` the `ViewStateStore` for make it be mutable only from a `ViewStateStoreScope`

```kotlin
class CarsViewModel( val getCars: GetCars ): ViewModel(), ViewStateStoreScope {
    val cars = ViewStateStore<List<Car>>().lock // Locking the ViewStateStore
}

class CarsFragment: Fragment(), ViewStateFragment {
    override fun onActivityCreated( savedInstanceState: Bundle? ) {
        carsViewModel.cars.postLoading() // Does NOT compile, LockedViewStateStore.postLoading not resolved
    }
}
```



### From external source

A `ViewStateStore` can also be created from a `LiveData` or a `DataSource.Factory` ( see _paging_ artifact for last one ).

```kotlin
val carsLiveData: LiveData<Car> = roomDatabase.getCars()
val cars = ViewStateStore.from( carsLiveData )
```

```kotlin
val carsDataSource: DataSource.Factory<Int, Car> = roomDatabase.getCars()
val cars = ViewStateStore.from( carsDataSource )
```



## Wiki

#### Full Wiki [here](https://github.com/4face-studi0/ViewStateStore/wiki)

#### ViewStateStore Doc [here](https://4face-studi0.github.io/ViewStateStore/viewstatestore/)

#### Paging Doc [here](https://4face-studi0.github.io/ViewStateStore/paging/)
