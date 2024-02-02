import android.content.Context
import android.widget.ImageButton
import androidx.appcompat.widget.PopupMenu
import com.facebook.video_demo.R

class SettingsHandler(private val context: Context, private val settingsButton: ImageButton) {

    init {
        setupSettingsButton()
    }

    private fun setupSettingsButton() {
        settingsButton.setOnClickListener {
            val popupMenu = PopupMenu(context, it)
            popupMenu.inflate(R.menu.settings_menu)

            // Set click listener for each menu item
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_playback_speed -> {
                        // Handle playback speed option
                        // You can show a dialog or perform any other action
                        true
                    }
                    R.id.menu_quality -> {
                        // Handle quality option
                        // You can show a dialog or perform any other action
                        true
                    }
                    else -> false
                }
            }

            // Show the popup menu
            popupMenu.show()
        }
    }
}
