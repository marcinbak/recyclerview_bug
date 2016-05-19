package de.neofonie.recyclerviewbug

import and.universal.club.toggolino.de.toggolino.utils.bindView
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

const val MAX_ITEM_ID = 20
const val MAX_PROGRESS = 10000


class MainActivity : AppCompatActivity() {

  private val recyclerView by bindView<RecyclerView>(R.id.recycler)
  private val progressSubscriptions = CompositeSubscription()
  private val progressGenerator = ProgressGenerator()

  private lateinit var adapter: SimpleRecyclerAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    adapter = SimpleRecyclerAdapter({ model ->
      // start/stop sending events to observable
      progressGenerator.startStop(model)
    })
    recyclerView.adapter = adapter
  }

  override fun onStop() {
    progressSubscriptions.clear()
    super.onStop()
  }

  override fun onDestroy() {
    progressSubscriptions.unsubscribe()
    super.onDestroy()
  }

  override fun onStart() {
    super.onStart()
    subscribeForProgress((0..MAX_ITEM_ID).map {
      ItemModel(it, State.NONE)
    })
  }

  private fun subscribeForProgress(items: List<ItemModel>) {
    adapter.data = items
    progressSubscriptions.clear()

    items.forEachIndexed { i, item ->
      val sub = progressGenerator.getProgress(item.id)
          .subscribeOn(Schedulers.computation())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe {
            item.progress = it.progress
            item.state = it.state
            adapter.notifyItemChanged(i)
          }
      progressSubscriptions.add(sub)
    }
  }
}

class SimpleRecyclerAdapter(val listener: (model: ItemModel) -> Unit) : RecyclerView.Adapter<SimpleViewHolder>() {

  var data: List<ItemModel>? = null
    set(value) {
      field = value
      notifyDataSetChanged()
    }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SimpleViewHolder(parent, listener)

  override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
    holder.bind(data!![position])
  }

  override fun getItemCount() = data?.size ?: 0

}


class SimpleViewHolder(parent: ViewGroup, listener: (ItemModel) -> Unit) : RecyclerView.ViewHolder(parent.inflate(R.layout.progress_item, parent, false)) {

  lateinit var model: ItemModel

  private val progressBar by bindView<ProgressBar>(R.id.progress)

  init {
    itemView.setOnClickListener {
      model.state = when (model.state) {
        State.NONE -> State.INDETERMINATE
        else -> State.NONE
      }
      listener(model)
    }
  }

  fun bind(itemModel: ItemModel) {
    model = itemModel

    when (itemModel.state) {
      State.NONE -> {
        progressBar.visibility = View.GONE
      }
      State.DETERMINATE -> {
        if (progressBar.isIndeterminate) progressBar.isIndeterminate = false
        progressBar.visibility = View.VISIBLE
        progressBar.progress = itemModel.progress
      }
      State.INDETERMINATE -> {
        progressBar.isIndeterminate = true
        progressBar.visibility = View.VISIBLE
      }
    }
  }

}

data class ItemModel(var id: Int, var state: State, var progress: Int = -1)

enum class State {
  DETERMINATE, INDETERMINATE, NONE
}

fun View.inflate(@LayoutRes resource: Int, root: ViewGroup?, attachToRoot: Boolean) = LayoutInflater.from(context).inflate(resource, root, attachToRoot)