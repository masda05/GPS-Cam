package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.data.AppDatabase
import com.example.data.repository.FieldRepository
import com.example.ui.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MainViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configure Local Database Instance
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = FieldRepository(database.projectDao(), database.photoMetadataDao())

        // ViewModel Factory Initialization
        val factory = MainViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        // Handle Global Application Toast Flow on main thread
        lifecycleScope.launch {
            viewModel.toastMessage.collectLatest { message ->
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            }
        }

        setContent {
            val isDarkTheme by viewModel.isDarkMode.collectAsState()
            val fontSizeScale by viewModel.appFontSizeScale.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme, appFontSizeScale = fontSizeScale) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigation(viewModel)
                }
            }
        }
    }
}
