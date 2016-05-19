package de.neofonie.recyclerviewbug

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

const val MAX_ITEM_ID = 20
const val MAX_PROGRESS = 10000


class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    findViewById(R.id.button_failing)!!.setOnClickListener { RecyclerActivity.start(this, false) }
    findViewById(R.id.button_working)!!.setOnClickListener { RecyclerActivity.start(this, true) }
  }

}