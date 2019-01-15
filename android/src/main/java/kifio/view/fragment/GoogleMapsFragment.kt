package kifio.view.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kifio.R

class GoogleMapsFragment: Fragment() {

    companion object {
        fun newInstance() = GoogleMapsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_placeholder, container, false)
    }
}