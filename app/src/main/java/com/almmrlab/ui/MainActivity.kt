package com.almmrlab.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.almmrlab.R
import com.almmrlab.databinding.ActivityMainBinding
import com.almmrlab.util.LanguageManager
import com.almmrlab.util.SyncManager
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout

    @Inject
    lateinit var languageManager: LanguageManager

    @Inject
    lateinit var syncManager: SyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply language configuration
        updateLocale()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)

        // Setup navigation
        drawerLayout = binding.drawerLayout
        val navView = binding.navView
        navController = findNavController(R.id.nav_host_fragment)

        // Configure the navigation drawer
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_perfume_studio,
                R.id.nav_raw_materials,
                R.id.nav_manufacturers,
                R.id.nav_registration,
                R.id.nav_search,
                R.id.nav_export
            ),
            drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(this)

        // Setup offline mode indicator
        setupOfflineIndicator()
    }

    private fun updateLocale() {
        val currentLanguage = languageManager.getCurrentLanguage()
        languageManager.setLanguage(currentLanguage)
    }

    private fun setupOfflineIndicator() {
        val headerView = binding.navView.getHeaderView(0)
        val offlineChip = headerView.findViewById<com.google.android.material.chip.Chip>(R.id.offline_status_chip)
        
        // Update offline status
        syncManager.getLastBackupInfo().let { backupInfo ->
            if (backupInfo.timestamp != null) {
                offlineChip.visibility = android.view.View.VISIBLE
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_language -> {
                navController.navigate(R.id.nav_language)
            }
            R.id.nav_theme -> {
                navController.navigate(R.id.nav_theme)
            }
            R.id.nav_backup -> {
                navController.navigate(R.id.nav_backup)
            }
            else -> {
                // Let the NavigationUI handle other navigation
                return item.onNavDestinationSelected(navController)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
