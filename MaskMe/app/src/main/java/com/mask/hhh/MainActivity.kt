package com.mask.hhh

import android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider

import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import org.torproject.android.core.TorService
import org.torproject.android.core.TorServiceConstants
import org.torproject.android.core.TorSettings





class MainActivity : AppCompatActivity() {

    private lateinit var menuIcon: android.view.View
    private lateinit var requestManageStorageLauncher: ActivityResultLauncher<String>

    private lateinit var searchBar: EditText
    private lateinit var preventTrackingButton: SwitchMaterial
    private lateinit var maskIpButton: SwitchMaterial
    private lateinit var maskDeviceButton: SwitchMaterial
    private lateinit var webView: WebView
    private var lastBackPressTime = 0L
    internal var doubleBackToExitPressedOnce = false
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        setEditTextListeners()
        // Get a reference to the bottom sheet layout
        // Set up the bottom sheet behavior
        findViewById<View>(R.id.dimView).setBackgroundColor(Color.parseColor("#88000000"))

        val bottomSheetLayout = findViewById<LinearLayout>(R.id.bottomSheetLayout)
        // Set up the bottom sheet behavior
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.peekHeight = 200
        bottomSheetLayout.clipToOutline = true
        menuIcon = findViewById(R.id.menu)

        menuIcon.setOnClickListener { showPopupMenu() }


// Set up the dimmed background
        val bottomSheetColors = intArrayOf(Color.parseColor("#FFFFFF"), Color.parseColor("#D3D3D3"))
        val bottomSheetBackground = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, bottomSheetColors)

        val randomIpToggle = findViewById<SwitchMaterial>(R.id.random_ip)
        val virtualDeviceToggle = findViewById<SwitchMaterial>(R.id.virtual_device)
        val untraceableToggle = findViewById<SwitchMaterial>(R.id.untraceable)
// Get a reference to the button that will expand the bottom sheet


// Check if the permission is already granted


// Set an OnClickListener on the expandButton to trigger expansion
        val expandButton = findViewById<Button>(R.id.expandButton)
        expandButton.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                //bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
// Hide the toggles when the bottom sheet is collapsed
        randomIpToggle.visibility = View.GONE
        virtualDeviceToggle.visibility = View.GONE
        untraceableToggle.visibility = View.GONE
        bottomSheetLayout.background = bottomSheetBackground
        bottomSheetLayout.setBackgroundColor(getColor(R.color.black))
        val dimView = findViewById<View>(R.id.dimView)

// Dim the background when the bottom sheet is expanded or partially expanded
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState ==  BottomSheetBehavior.STATE_COLLAPSED ) {  randomIpToggle.visibility = View.GONE
                    virtualDeviceToggle.visibility = View.GONE
                    untraceableToggle.visibility = View.GONE
                    dimView.visibility = View.GONE
                    bottomSheetLayout.setBackgroundColor(getColor(R.color.black)) }
                else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetLayout.setBackgroundColor(getColor(R.color.white))
                    randomIpToggle.visibility = View.VISIBLE
                    virtualDeviceToggle.visibility = View.VISIBLE
                    untraceableToggle.visibility = View.VISIBLE
                    dimView.visibility = View.VISIBLE

                } else if (newState == BottomSheetBehavior.STATE_HIDDEN) { bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    dimView.visibility = View.GONE
                    randomIpToggle.visibility = View.GONE
                    virtualDeviceToggle.visibility = View.GONE
                    untraceableToggle.visibility = View.GONE
                    dimView.visibility = View.GONE
                } else if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    bottomSheetLayout.setBackgroundColor(getColor(R.color.white))
                    val currentHeight = bottomSheet.height
                    val minHeight = bottomSheetBehavior.peekHeight
                    if (currentHeight < minHeight) {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        dimView.visibility = View.GONE
                    } else { randomIpToggle.visibility = View.GONE
                        virtualDeviceToggle.visibility = View.GONE
                        untraceableToggle.visibility = View.GONE
                        dimView.visibility = View.GONE
                        bottomSheetLayout.setBackgroundColor(getColor(R.color.white))
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Set the alpha of the dim view based on the slide offset
                bottomSheetLayout.setBackgroundColor(getColor(R.color.white))
                dimView.alpha = slideOffset
                randomIpToggle.visibility = View.GONE
                virtualDeviceToggle.visibility = View.GONE
                untraceableToggle.visibility = View.GONE
                dimView.visibility = View.GONE
                // Set the background color of the WebView based on the slide offset
                val color = Color.argb((slideOffset * 50).toInt(), 0, 0, 0)
                webView.setBackgroundColor(color)

                val minHeight = bottomSheetBehavior.peekHeight
                if (slideOffset == 0f) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                } else if (slideOffset == 1f) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }

                val minOffset = 0.0f
                val maxOffset = 1.0f - bottomSheetBehavior.peekHeight.toFloat() / bottomSheet.height
                dimView.alpha = maxOffset.coerceAtLeast(slideOffset).coerceIn(minOffset, maxOffset)
            }
        })
        dimView.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

// Add curved corners to the bottom sheet
        bottomSheetLayout.clipToOutline = true
        fun dpToPx(context: Context, dp: Int): Float {
            val density = context.resources.displayMetrics.density
            return dp * density
        }

        bottomSheetBehavior.setMaxHeight(800)
        bottomSheetLayout.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, dpToPx(view.context, 16))
            }
        }

// Handle clicks on the bottom sheet toggles
        findViewById<SwitchMaterial>(R.id.random_ip).setOnCheckedChangeListener { _, isChecked ->
            // Do something when the toggle is checked or unchecked
        }
        findViewById<SwitchMaterial>(R.id.virtual_device).setOnCheckedChangeListener { _, isChecked ->
            // Do something when the toggle is checked or unchecked
        }
        findViewById<SwitchMaterial>(R.id.untraceable).setOnCheckedChangeListener { _, isChecked ->
            // Do something when the toggle is checked or unchecked
        }


        //showBottomSheet()
        preventTrackingButton = findViewById(R.id.random_ip)
        maskIpButton = findViewById(R.id.virtual_device)
        maskDeviceButton = findViewById(R.id.untraceable)
        webView = findViewById<WebView>(R.id.webView)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                Log.d("MyApp", "Trigerred")
                // Update the toolbar with the current URL and security status
                updateToolbar(url)
                return false // Let the WebView handle the URL loading
            }
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                // Update the toolbar with the current URL and security status
                updateToolbar(url)
            }
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)


        val editText = toolbar.findViewById<EditText>(R.id.url_edittext)
        val imageView = toolbar.findViewById<ImageView>(R.id.security_icon)

        val searchUrl = "https://www.1lywin.com/bar.html"


        webView.loadUrl(searchUrl)
        webView.settings.javaScriptEnabled = true
        val enginesText = findViewById<TextView>(R.id.engines_text)
        enginesText.setOnClickListener {
            enginesText.visibility = View.GONE
        }
        val editT = toolbar.findViewById<EditText>(R.id.url_edittext)




        preventTrackingButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Enable tracking prevention
                // You can write code to implement this feature here
            } else {
                // Disable tracking prevention
                // You can write code to implement this feature here
            }
        }

        maskIpButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Enable IP masking
                // You can write code to implement this feature here
            } else {
                // Disable IP masking
                // You can write code to implement this feature here
            }
        }
// Enable caching
        var webView = WebView(this)
        val webSettings = webView.settings


// Enable caching

        webView.isFocusable = true
        webView.isFocusableInTouchMode = true

        webView.settings.userAgentString =
            "Mozilla/5.0 (Linux; Android 10; SM-A205U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.5359.128 Mobile Safari/537.36."
        webView.settings.javaScriptEnabled = true
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        WebSettings.RenderPriority.HIGH
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        webView.settings.domStorageEnabled = true
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.loadsImagesAutomatically = true
        webView.settings.domStorageEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webView.settings.databaseEnabled = true
        //webView.getSettings().setDatabasePath(
        //        this.getFilesDir().getPath() + this.getPackageName() + "/databases/");
        webView.settings.allowFileAccess = true // Enable file access
        webView.settings.allowFileAccessFromFileURLs = true // Enable file access from file URLs
        webView.settings.allowUniversalAccessFromFileURLs = true // Enable universal access from file URLs
        // this force use chromeWebClient
        webView.settings.setSupportMultipleWindows(false)

        webView = findViewById<WebView>(R.id.webView)
        webView.setDownloadListener(MyWebViewClient())




        maskDeviceButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Enable device masking
                // You can write code to implement this feature here
            } else {
                // Disable device masking
                // You can write code to implement this feature here
            }
        }

    }
    inner class MyWebViewClient : WebViewClient(), DownloadListener {


        override fun onDownloadStart(url: String?, userAgent: String?, contentDisposition: String?, mimeType: String?, contentLength: Long) {
            // Show a confirmation dialog to the user and initiate the download
            Log.d("MyApp", "DownloadListener called")
            Log.d("MyApp", "URL: $url")
            Log.d("MyApp", "UserAgent: $userAgent")
            Log.d("MyApp", "ContentDisposition: $contentDisposition")
            Log.d("MyApp", "MimeType: $mimeType")
            Log.d("MyApp", "ContentLength: $contentLength")
            showDownloadConfirmationDialog(url.toString())

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_popup, menu)

        val menuItem3 = menu.findItem(R.id.menu_item3)
        menuItem3?.setActionView(R.layout.cm)

        val actionView = menuItem3?.actionView
        val menuItemTitle = actionView?.findViewById<TextView>(R.id.menu_item_title)
        val menuItemIcon = actionView?.findViewById<ImageView>(R.id.menu_item_icon)

        menuItemTitle?.text = "Settings"
        menuItemIcon?.setImageResource(R.drawable.ic_settings)

        // Set any additional properties or event listeners for the custom layout

        return true
    }
    private fun clearAppData() {
        val cacheDir = cacheDir
        val appDir = File(cacheDir.parent)
        if (appDir.exists()) {
            val children = appDir.list()
            for (s in children) {
                if (!s.equals("lib", ignoreCase = true)) {
                    deleteDir(File(appDir, s))
                    Log.i("TAG", "File /data/data/$packageName/$s DELETED")
                }
            }
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
            return dir.delete()
        } else return dir?.delete() ?: false
    }
    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun showPopupMenu() {
        val popupMenu = PopupMenu(this, menuIcon)
        popupMenu.inflate(R.menu.menu_popup)
        val menuItem3 = popupMenu.menu.findItem(R.id.menu_item3)

        // Set a custom action view for the MenuItem
        menuItem3.actionView = LayoutInflater.from(this).inflate(R.layout.cm, null)
        // Optional: Set an item click listener
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_item1 -> {
                    // Handle menu item 1 click
                    clearAppData()
                    restartApp()
                    true
                }
                R.id.menu_item2 -> {
                    // Handle menu item 2 click
                    val bottomSheetLayout = findViewById<LinearLayout>(R.id.bottomSheetLayout)
                    // Set up the bottom sheet behavior
                    val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    true
                }
                R.id.menu_item3 -> {
                    // Handle menu item 3 click
                    openDownloadsFolder()
                    true
                }
                R.id.menu_item4 -> {
                    // Handle menu item 3 click
                    openPlayStore()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun performSearch(query: String) {
        // You can write code here to perform a search using the query text
        // For example, you might open a WebView and load a search engine URL with the query parameter:

    }
    private fun setEditTextListeners() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val editText = toolbar.findViewById<EditText>(R.id.url_edittext)
        val imageView = toolbar.findViewById<ImageView>(R.id.security_icon)




        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                val url = editText.text.toString()
                webView.loadUrl(url)
                true
            } else {
                false
            }
        }
        editText.setOnClickListener {
            val currentUrl = webView.url
            val currentDomainName = getDomainName(currentUrl)
            val isDomain1lywin = currentDomainName == "1lywin.com"

            if (isDomain1lywin && editText.text.isEmpty()) {
                editText.hint = "Search With Privacy.."
            } else {
                editText.setText(currentUrl)
                editText.hint = ""
            }
        }


        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                Log.d("MyApp", "focused")
                val currentUrl = editText.text.toString()
                val currentDomainName = getDomainName(currentUrl)

                if (currentDomainName == "1lywin.com") {
                    editText.setText("")
                    editText.hint = "Search With Privacy"
                } else {
                    editText.setText(getDomainName(currentUrl))
                    editText.hint = "Search With Privacy"
                }
            }  else {
                val webViewUrl = webView.url
                val webViewDomainName = getDomainName(webViewUrl.toString())

                if (webViewDomainName == "1lywin.com") {
                    editText.setText("")
                    editText.hint = "Search With Privacy"
                } else {
                    editText.setText(webViewUrl)
                    editText.hint = ""
                }
            }
        }











}
    /*private fun showBottomSheet() {
        // Get the bottom sheet view and behavior
        val bottomSheet = findViewById<LinearLayout>(R.id.sheet_layout)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        // Set the peek height to the height of the layout containing the toggle buttons
        val toggleLayout = findViewById<LinearLayout>(R.id.toggle_layout)
        bottomSheetBehavior.peekHeight = toggleLayout.height
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        // Set the state change callback to hide the bottom sheet when it's collapsed
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheet.visibility = View.GONE
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        // Show the bottom sheet with a small portion visible
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheet.visibility = View.VISIBLE

        // Set the touch listener to detect when the user clicks outside the bottom sheet
        findViewById<View>(R.id.webView).setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                val outRect = Rect()
                bottomSheet.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    return@setOnTouchListener true
                }
            }
            false
        }
    }*/
    private fun openPlayStore() {
        val appPackageName = "com.whatsapp" // Replace with the actual package name of the app you want to redirect to

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // If the Play Store app is not installed, open the app page in the browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    private fun getDomainName(url: String?): String {
        val uri = Uri.parse(url)
        val domain = uri.host ?: ""
        return if (domain.startsWith("www.")) {
            domain.substring(4)
        } else {
            domain
        }
    }


    private fun updateToolbar(url: String?) {
        Log.d("MyApp","Toolbar is updated")
        val secure = url?.startsWith("https") == true
        val iconRes = if (secure) R.drawable.secure else R.drawable.warn
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val editText = toolbar.findViewById<EditText>(R.id.url_edittext)
        val imageView = toolbar.findViewById<ImageView>(R.id.security_icon)

        val uri = Uri.parse(url)
        val domainName = uri.host ?: ""
        val isDomain1lywin = domainName == "1lywin.com"

        if (isDomain1lywin) {
            editText.setText("")
            editText.hint = "Search With Privacy.."
        } else {
            editText.setText(getDomainName(url))
            editText.hint = "Search With Privacy"
        }

        imageView.setImageResource(iconRes)
    }


    // Inside your onCreate() or initialization method








    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            val currentTime = System.currentTimeMillis()
            if (lastBackPressTime + 2000 > currentTime) {
                super.onBackPressed()
                return
            } else {
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
            }
            lastBackPressTime = currentTime
        }
    }

    fun showDownloadConfirmationDialog(url: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Download Confirmation")
            .setMessage("Unknown files can be dangerous. Are you sure you want to download?")
            .setPositiveButton("Download") { dialog, which ->
                downloadFile(url)
            }
            .setNegativeButton("Cancel") { dialog, which ->
                // Do nothing or handle cancellation
            }
            .show()
    }
    private fun openDownloadsFolder() {
        val currentApiVersion = android.os.Build.VERSION.SDK_INT

        if (currentApiVersion < android.os.Build.VERSION_CODES.Q) {
            // Versions below Android 10 (API level 29)
            openDownloadsFolderLegacy()
        } else {
            // Android 10 (API level 29) and above
            openDownloadsFolderScopedStorage()
        }
    }

    private fun openDownloadsFolderLegacy() {
        // Code for versions below Android 10 (API level 29)
        val permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        val requestCode = 123 // You can use any request code here

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            val downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
            val downloadsFolder = File(downloadsPath)
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", downloadsFolder)
            intent.setDataAndType(uri, "resource/folder")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // Handle the case where the file manager app is not found
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }
    }

    private fun openDownloadsFolderScopedStorage() {
        // Code for Android 10 (API level 29) and above
        val downloadsUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(downloadsUri, DocumentsContract.Document.MIME_TYPE_DIR)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Handle the case where the file manager app is not found
        }
    }




    private fun downloadFile(url: String) {
        Log.d("MyApp","download")
        val request = DownloadManager.Request(Uri.parse(url))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle("Downloadable File")
            .setDescription("File is being downloaded...")

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

    private val downloadCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)

                if (cursor.moveToFirst()) {
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    if (statusIndex != -1) {
                        val status = cursor.getInt(statusIndex)
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                            if (uriIndex != -1) {
                                val fileUri = cursor.getString(uriIndex)
                                val fileName = "file_name.ext" // Extract the file name from the fileUri
                                showDownloadNotification(fileUri, fileName)
                            }
                        }
                    }
                }


                cursor.close()
            }
        }
    }
    fun isDownloadLink(url: String): Boolean {
        val fileExtensions = listOf(".pdf", ".doc", ".zip") // Add more file extensions as needed

        var isDownloadable = false
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "HEAD"
                val contentDisposition = connection.getHeaderField("Content-Disposition")

                val contentType = connection.contentType

                val lowerCaseUrl = url.toLowerCase(Locale.ROOT)

                val isFileExtensionMatched = fileExtensions.any { lowerCaseUrl.endsWith(it) }
                val isContentDispositionMatched = contentDisposition?.contains("attachment") == true || contentDisposition?.contains("filename") == true
                val isContentTypeMatched = contentType?.startsWith("application/") == true

                isDownloadable = isFileExtensionMatched || isContentDispositionMatched || isContentTypeMatched
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return isDownloadable
    }


    private fun showDownloadNotification(fileUri: String, fileName: String) {
        // Create and show a notification for the completed download
        // Include the fileUri and fileName to open the downloaded file on click
        // Use NotificationCompat.Builder or other methods to customize the notification
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        registerReceiver(downloadCompleteReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(downloadCompleteReceiver)
    }





}
