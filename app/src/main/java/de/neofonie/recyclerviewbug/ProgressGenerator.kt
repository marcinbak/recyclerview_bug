package de.neofonie.recyclerviewbug

import rx.Observable
import rx.Subscription
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import rx.subscriptions.Subscriptions
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by marcinbak on 19/05/16.
 */
class ProgressGenerator {

  private val subjectsMap = HashMap<Int, BehaviorSubject<ItemModel>>()
  private val subscriptionsMap = HashMap<Int, Subscription>()

  init {
    (0..MAX_ITEM_ID).forEach {
      subjectsMap.put(it, BehaviorSubject.create())
      subscriptionsMap.put(it, Subscriptions.empty())
    }
  }

  fun startStop(model: ItemModel) {
    val start = when (model.state) {
      State.DETERMINATE -> true
      State.INDETERMINATE -> true
      State.NONE -> false
    }

    subscriptionsMap[model.id]!!.unsubscribe()

    subscriptionsMap[model.id] =
        if (start) {
          Observable
              .range(0, MAX_PROGRESS)
              .concatMap { Observable.just(it).delay(20, TimeUnit.MILLISECONDS) }
              .delay(3, TimeUnit.SECONDS)
              .map { model.copy(progress = it, state = State.DETERMINATE) }
              .startWith(Observable.just(model.copy(state = State.INDETERMINATE)))
              .concatWith(Observable.just(model.copy(state = State.NONE)))

        } else {
          Observable
              .just(model.copy(state = State.NONE))
              .delay(2, TimeUnit.SECONDS)
        }
            .subscribeOn(Schedulers.computation())
            .doOnNext {
              subjectsMap[model.id]!!.onNext(it)
            }
            .subscribe()


  }

  fun getProgress(id: Int) = subjectsMap[id]!!.asObservable().onBackpressureLatest()

}