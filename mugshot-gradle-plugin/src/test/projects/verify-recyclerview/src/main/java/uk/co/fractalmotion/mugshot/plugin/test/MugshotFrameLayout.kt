package uk.co.fractalmotion.mugshot.plugin.test

import android.content.Context
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import uk.co.fractalmotion.mugshot.plugin.test.databinding.InnerViewInflateBinding

class MugshotFrameLayout(context: Context) : FrameLayout(context) {
  init {
    inflate(context, R.layout.inner_view_inflate, this)
    val binding = InnerViewInflateBinding.bind(this)
    val recyclerView = binding.list
    recyclerView.adapter = MugshotRecyclerView.Adapter()
    recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
  }
}
